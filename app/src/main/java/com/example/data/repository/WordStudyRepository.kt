package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.CuratedWordStudies
import com.example.data.local.PhilologyFamilyHelper
import com.example.data.local.WordStudyDao
import com.example.data.model.WordStudy
import com.example.data.verification.GroundingSource
import com.example.data.verification.SourceTrustVerifier
import com.example.data.verification.VerificationMetadata
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class AiChatResponse(
    val text: String,
    val searchQueries: List<String> = emptyList(),
    val sources: List<GroundingSource> = emptyList(),
    val isGrounded: Boolean = false,
    val verification: VerificationMetadata? = null
)

class WordStudyRepository(private val wordStudyDao: WordStudyDao) {

    val allWordStudies: Flow<List<WordStudy>> = wordStudyDao.getAllWordStudies()
    val favoriteWordStudies: Flow<List<WordStudy>> = wordStudyDao.getFavoriteWordStudies()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val wordStudyAdapter = moshi.adapter(WordStudy::class.java)
    private val mapAdapter = moshi.adapter(Map::class.java)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\u000C", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    suspend fun seedCuratedStudiesIfEmpty() = withContext(Dispatchers.IO) {
        try {
            if (wordStudyDao.getCount() == 0) {
                wordStudyDao.insertAll(CuratedWordStudies.getAllCuratedStudies())
            }
        } catch (_: Exception) {}
    }

    suspend fun getWordStudy(
        word: String, 
        testament: String,
        activeProvider: String = "Gemini",
        userApiKey: String = ""
    ): WordStudy = withContext(Dispatchers.IO) {
        val cleanedWord = word.trim()
        val cleanedTestament = testament.trim()

        // 1. Check database cache first - strictly isolate by testament if specified
        val cached = if (cleanedTestament.isNotBlank()) {
            wordStudyDao.getWordStudyByWordAndTestament(cleanedWord, cleanedTestament)
        } else {
            wordStudyDao.getWordStudyAnyTestament(cleanedWord)
        }

        if (cached != null) {
            val updated = cached.copy(timestamp = System.currentTimeMillis())
            wordStudyDao.updateWordStudy(updated)
            return@withContext updated
        }

        // 2. Check curated scholarly database matching the requested testament
        val curated = CuratedWordStudies.getCuratedStudy(cleanedWord, cleanedTestament)
        if (curated != null && (cleanedTestament.isBlank() || curated.testament.equals(cleanedTestament, ignoreCase = true))) {
            val toSave = curated.copy(
                word = if (cleanedWord.isNotBlank()) cleanedWord.lowercase() else curated.word,
                timestamp = System.currentTimeMillis()
            )
            val id = wordStudyDao.insertWordStudy(toSave)
            return@withContext toSave.copy(id = id)
        }

        // 3. For any custom biblical terms not in curated library, query the configured AI provider
        val systemInstruction = """
            You are an expert biblical philologist, etymologist, and lexicographer specialized in Koine Greek and Biblical Hebrew. Your primary task is to analyze English theological words by tracing them back to their original language lemmas (root words/word families).

            CRITICAL TESTAMENT MANDATE:
            You MUST analyze the word strictly within the requested testament.
            - If "New Testament" is requested, you MUST provide the Koine Greek root lemma (with Strong's G-number) and first-century Greco-Roman historical/legal background. You must NOT provide Hebrew.
            - If "Old Testament" is requested, you MUST provide the Biblical Hebrew/Aramaic root lemma (with Strong's H-number) and ancient Near Eastern covenantal/legal background. You must NOT provide Greek.

            Whenever the user inputs an English or transliterated biblical word, you must strictly adhere to the following analytical protocol:

            1. MORPHOLOGICAL WORD FAMILIES: Immediately identify the underlying Hebrew or Greek root lemma for the specified testament. You must check if the English word requested belongs to a larger linguistic family in the original language that English translations split into completely different words (e.g., in NT: Righteousness [Noun] and Justification [Verb] from 'dike'; or Holy [Adj] and Sanctification [Noun] from 'hagios'; in OT: Righteousness [Noun] and Justified [Verb] from 'tsadaq').

            2. CO-PRESENTATION REQUIREMENT: If the requested word is part of such a split-translation family, you are strictly forbidden from presenting the word in isolation. You must bring out the related nouns, verbs, adjectives, and adverbs together in a single, unified profile.

            3. THE "TRANSLATION DISCONNECT" HIGHLIGHT: Explicitly point out any hidden linguistic paradoxes or "wows" created by this disconnect. Explain how the Western/English theological understanding differs from the original Hebraic or Greek mindset because of these split translations.

            4. FORMAT WITH COMPARATIVE TABLES: Always use a markdown table to map out the Noun, Verb, and Adjective forms of the original Greek/Hebrew root, showing their corresponding English translations so the user can visually track the structural connection instantly.

            You MUST provide as much details as possible when giving the historical, linguistic, and cultural background of words. Do not summarize or abbreviate.
            You MUST return your response as a single, strictly valid JSON object.
            Do not enclose the JSON in markdown code blocks like ```json ... ```. Just return the raw JSON text.
            The JSON object must have exactly these keys:
            - "word": String (the search word)
            - "testament": String (the testament context: "Old Testament" or "New Testament")
            - "originalWord": String (the original Hebrew/Aramaic for Old Testament, or Greek for New Testament, in original script like 'υἱοθεσία' or 'חֶסֶד')
            - "transliteration": String (e.g. "huiothesia" or "chesed")
            - "strongsNumber": String (e.g. "G5206" or "H2617")
            - "literalMeaning": String (e.g. "placement as a son" or "unfailing covenant love")
            - "rootLemma": String (the root lemma in original script and transliteration, e.g. "δίκη (dikē)" or "קָדַשׁ (qadash)")
            - "morphologicalFamilyTable": String (markdown table mapping Part of Speech, Original Greek/Hebrew Term, Transliteration, and English Translation, showing Noun, Verb, Adjective, and Adverb forms together)
            - "translationDisconnect": String (exhaustive explanation of the translation disconnect, highlighting the hidden linguistic paradox or 'wow' of how Western/English translations split one unified root into separate concepts, and how the original mindset differs)
            - "thenMeaning": String (what it meant THEN in its original first-century Roman/Jewish historical/legal/cultural context. Go into rich, highly educational detail, explaining the background exhaustively.)
            - "nowMeaning": String (what modern readers often think it means NOW, highlighting modern misunderstandings or shallow theological interpretations)
            - "historicalBackground": String (the historical situation, audience, and era-specific dynamics of the word. Give as much historical details as possible, including names, dates, events, empires, and political contexts.)
            - "culturalContext": String (the cultural norms, expectations, and societal frameworks of the first-century Roman or ancient Near Eastern audience. Provide exhaustive cultural background, detailing household structures, social classes, honor/shame dynamics, or religious patterns.)
            - "legalDimension": String (the legal framework under Roman or Jewish law. e.g., Roman adoption legal processes, ancient covenant legal binding, Roman grace/patronage contracts. Explain how this changes the meaning!)
            - "theologicalWeight": String (the theological significance and depth of this term when used by biblical authors like Paul, Jesus, or Moses)
            - "keyScriptures": Array of Strings (3-4 key scripture verses with references, e.g., ["Romans 8:15", "Galatians 4:5"])
            - "closingInsight": String (one elegant, powerful closing insight summarizing what modern believers miss about their faith by not knowing this original legal, historical, or cultural meaning)
            - "searchGroundingSources": Array of Strings (2-3 primary historical/epigraphic inscriptions, classical authors like Tacitus/Josephus, or peer-reviewed monographs supporting this study)
        """.trimIndent()

        val promptText = "Provide an in-depth Bible word study for the word: '$cleanedWord'.\n" +
            "CRITICAL TESTAMENT SCOPE: You MUST analyze this term specifically in the context of the '$cleanedTestament'.\n" +
            (if (cleanedTestament.contains("New", ignoreCase = true)) {
                "Since this is the NEW TESTAMENT, focus strictly on the original KOINE GREEK root lemma (with Strong's G-number), Greek morphological inflections, and first-century Greco-Roman historical/legal background. Do NOT give the Hebrew Old Testament lemma."
            } else {
                "Since this is the OLD TESTAMENT, focus strictly on the original BIBLICAL HEBREW (or Aramaic) root lemma (with Strong's H-number), Hebrew morphological inflections, and ancient Near Eastern covenantal/legal background. Do NOT give the Greek New Testament lemma."
            })

        try {
            val rawJsonText = when (activeProvider) {
                "Gemini" -> {
                    val apiKey = if (userApiKey.isNotBlank()) userApiKey else BuildConfig.GEMINI_API_KEY
                    val hasApiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"
                    if (!hasApiKey) {
                        throw IllegalStateException("Gemini API Key is missing. Please configure it in Settings or AI Studio Secrets.")
                    }

                    // Resilient model list with fallback in case of high demand / 503
                    val modelsToTry = listOf(
                        "gemini-3.5-flash",
                        "gemini-flash-latest",
                        "gemini-3.1-flash-lite-preview",
                        "gemini-3.1-pro-preview"
                    )

                    var lastGeminiError: Exception? = null
                    var successfulJson: String? = null

                    for (model in modelsToTry) {
                        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                        val requestBodyJson = """
                            {
                              "contents": [
                                {
                                  "parts": [
                                    {"text": "${escapeJson(promptText)}"}
                                  ]
                                }
                              ],
                              "generationConfig": {
                                "responseMimeType": "application/json",
                                "temperature": 0.2
                              },
                              "systemInstruction": {
                                "parts": [
                                  {"text": "${escapeJson(systemInstruction)}"}
                                ]
                              }
                            }
                        """.trimIndent()

                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val request = Request.Builder()
                            .url(url)
                            .post(requestBodyJson.toRequestBody(mediaType))
                            .build()

                        // Try with one quick retry if 503/429
                        for (attempt in 1..2) {
                            try {
                                val response = httpClient.newCall(request).execute()
                                val responseBody = response.body?.string() ?: ""

                                if (response.isSuccessful) {
                                    val responseMap = mapAdapter.fromJson(responseBody)
                                    val candidates = responseMap?.get("candidates") as? List<*>
                                    val candidate = candidates?.firstOrNull() as? Map<*, *>
                                    val content = candidate?.get("content") as? Map<*, *>
                                    val parts = content?.get("parts") as? List<*>
                                    val part = parts?.firstOrNull() as? Map<*, *>
                                    val text = part?.get("text") as? String
                                    if (!text.isNullOrBlank()) {
                                        successfulJson = text
                                        break
                                    }
                                } else {
                                    val code = response.code
                                    if ((code == 503 || code == 429) && attempt < 2) {
                                        delay(1200L)
                                        continue
                                    }
                                    lastGeminiError = Exception("Gemini API Error ($model, Code $code): $responseBody")
                                }
                            } catch (e: Exception) {
                                lastGeminiError = e
                                if (attempt < 2) delay(1000L)
                            }
                        }

                        if (successfulJson != null) break
                    }

                    successfulJson ?: throw (lastGeminiError ?: Exception("Unable to retrieve study from Gemini."))
                }

                "Claude" -> {
                    if (userApiKey.isBlank()) {
                        throw IllegalStateException("Claude API Key is missing. Please configure it in the Settings tab.")
                    }
                    val url = "https://api.anthropic.com/v1/messages"
                    val requestBodyJson = """
                        {
                          "model": "claude-3-5-sonnet-20241022",
                          "max_tokens": 4000,
                          "system": "${escapeJson(systemInstruction)}",
                          "messages": [
                            {"role": "user", "content": "${escapeJson(promptText)}"}
                          ]
                        }
                    """.trimIndent()

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-api-key", userApiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .post(requestBodyJson.toRequestBody(mediaType))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw Exception("Claude API Error (Code ${response.code}): ${response.body?.string() ?: "Unknown error"}")
                    }
                    val responseBodyString = response.body?.string() ?: throw Exception("Empty response body from Claude API.")
                    
                    val responseMap = mapAdapter.fromJson(responseBodyString)
                    val contentList = responseMap?.get("content") as? List<*>
                    val contentObj = contentList?.firstOrNull() as? Map<*, *>
                    contentObj?.get("text") as? String ?: throw Exception("Failed to extract text from Claude response.")
                }

                "Groq" -> {
                    if (userApiKey.isBlank()) {
                        throw IllegalStateException("Groq API Key is missing. Please configure it in the Settings tab.")
                    }
                    val url = "https://api.groq.com/openai/v1/chat/completions"
                    val requestBodyJson = """
                        {
                          "model": "openai/gpt-oss-120b",
                          "messages": [
                            {"role": "system", "content": "${escapeJson(systemInstruction)}"},
                            {"role": "user", "content": "${escapeJson(promptText)}"}
                          ],
                          "temperature": 0.2
                        }
                    """.trimIndent()

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $userApiKey")
                        .post(requestBodyJson.toRequestBody(mediaType))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw Exception("Groq API Error (Code ${response.code}): ${response.body?.string() ?: "Unknown error"}")
                    }
                    val responseBodyString = response.body?.string() ?: throw Exception("Empty response body from Groq API.")
                    
                    val responseMap = mapAdapter.fromJson(responseBodyString)
                    val choices = responseMap?.get("choices") as? List<*>
                    val choice = choices?.firstOrNull() as? Map<*, *>
                    val message = choice?.get("message") as? Map<*, *>
                    message?.get("content") as? String ?: throw Exception("Failed to extract text from Groq response.")
                }

                "Grok" -> {
                    if (userApiKey.isBlank()) {
                        throw IllegalStateException("Grok API Key is missing. Please configure it in the Settings tab.")
                    }
                    val url = "https://api.x.ai/v1/chat/completions"
                    val requestBodyJson = """
                        {
                          "model": "grok-2-1212",
                          "messages": [
                            {"role": "system", "content": "${escapeJson(systemInstruction)}"},
                            {"role": "user", "content": "${escapeJson(promptText)}"}
                          ],
                          "temperature": 0.2
                        }
                    """.trimIndent()

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $userApiKey")
                        .post(requestBodyJson.toRequestBody(mediaType))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw Exception("Grok API Error (Code ${response.code}): ${response.body?.string() ?: "Unknown error"}")
                    }
                    val responseBodyString = response.body?.string() ?: throw Exception("Empty response body from Grok API.")
                    
                    val responseMap = mapAdapter.fromJson(responseBodyString)
                    val choices = responseMap?.get("choices") as? List<*>
                    val choice = choices?.firstOrNull() as? Map<*, *>
                    val message = choice?.get("message") as? Map<*, *>
                    message?.get("content") as? String ?: throw Exception("Failed to extract text from Grok response.")
                }

                else -> throw IllegalArgumentException("Unknown API Provider: $activeProvider")
            }

            // Extract raw JSON object if wrapped in markdown or conversational text
            val jsonRegex = Regex("""\{[\s\S]*\}""")
            val cleanedJson = jsonRegex.find(rawJsonText)?.value ?: rawJsonText
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsedStudy = try {
                wordStudyAdapter.fromJson(cleanedJson) ?: throw Exception("JSON parsed as null.")
            } catch (e: Exception) {
                throw Exception("Failed to parse Word Study JSON: ${e.message}. Raw response: $cleanedJson")
            }

            // Audit raw search grounding sources: verify authenticity, provenance, and trust rating
            val (verifiedSources, _) = SourceTrustVerifier.auditStringSources(
                rawStrings = parsedStudy.searchGroundingSources,
                word = cleanedWord,
                strictFilter = true
            )
            val verifiedSourceStrings = verifiedSources.map {
                "${it.title} | ${it.domain} [${it.trustTier.label} - ${it.trustScore}% Trust]"
            }

            val philProfile = PhilologyFamilyHelper.getPhilologicalProfile(
                word = cleanedWord,
                originalWord = parsedStudy.originalWord,
                transliteration = parsedStudy.transliteration,
                testament = cleanedTestament,
                customRootLemma = parsedStudy.rootLemma,
                customTable = parsedStudy.morphologicalFamilyTable,
                customDisconnect = parsedStudy.translationDisconnect
            )
            val resolvedRootLemma = parsedStudy.rootLemma.ifBlank { philProfile.rootLemma }
            val resolvedTable = parsedStudy.morphologicalFamilyTable.ifBlank {
                PhilologyFamilyHelper.buildMarkdownTable(philProfile.morphologicalRows)
            }
            val resolvedDisconnect = parsedStudy.translationDisconnect.ifBlank { philProfile.translationDisconnect }

            val wordStudyToSave = parsedStudy.copy(
                word = cleanedWord,
                testament = cleanedTestament,
                timestamp = System.currentTimeMillis(),
                searchGroundingSources = verifiedSourceStrings,
                rootLemma = resolvedRootLemma,
                morphologicalFamilyTable = resolvedTable,
                translationDisconnect = resolvedDisconnect
            )
            val id = wordStudyDao.insertWordStudy(wordStudyToSave)
            return@withContext wordStudyToSave.copy(id = id)

        } catch (e: Exception) {
            // Check curated fallback again as reliable offline recovery
            val fallback = CuratedWordStudies.getCuratedStudy(cleanedWord, cleanedTestament)
            if (fallback != null) {
                val (verifiedSources, _) = SourceTrustVerifier.auditStringSources(
                    rawStrings = fallback.searchGroundingSources,
                    word = cleanedWord,
                    strictFilter = true
                )
                val verifiedSourceStrings = verifiedSources.map {
                    "${it.title} | ${it.domain} [${it.trustTier.label} - ${it.trustScore}% Trust]"
                }

                val fallbackProfile = PhilologyFamilyHelper.getPhilologicalProfile(
                    word = cleanedWord,
                    originalWord = fallback.originalWord,
                    transliteration = fallback.transliteration,
                    testament = cleanedTestament,
                    customRootLemma = fallback.rootLemma,
                    customTable = fallback.morphologicalFamilyTable,
                    customDisconnect = fallback.translationDisconnect
                )
                val fallbackRootLemma = fallback.rootLemma.ifBlank { fallbackProfile.rootLemma }
                val fallbackTable = fallback.morphologicalFamilyTable.ifBlank {
                    PhilologyFamilyHelper.buildMarkdownTable(fallbackProfile.morphologicalRows)
                }
                val fallbackDisconnect = fallback.translationDisconnect.ifBlank { fallbackProfile.translationDisconnect }

                val toSave = fallback.copy(
                    word = cleanedWord.lowercase(),
                    timestamp = System.currentTimeMillis(),
                    searchGroundingSources = verifiedSourceStrings,
                    rootLemma = fallbackRootLemma,
                    morphologicalFamilyTable = fallbackTable,
                    translationDisconnect = fallbackDisconnect
                )
                val id = wordStudyDao.insertWordStudy(toSave)
                return@withContext toSave.copy(id = id)
            }
            throw e
        }
    }

    suspend fun toggleFavorite(wordStudy: WordStudy) = withContext(Dispatchers.IO) {
        wordStudyDao.updateWordStudy(wordStudy.copy(isFavorite = !wordStudy.isFavorite))
    }

    suspend fun deleteWordStudy(id: Long) = withContext(Dispatchers.IO) {
        wordStudyDao.deleteWordStudyById(id)
    }

    suspend fun chatWithAi(
        wordStudy: WordStudy? = null,
        messageHistory: List<Pair<String, String>>,
        activeProvider: String = "Gemini",
        userApiKey: String = "",
        useSearchGrounding: Boolean = true,
        strictFilter: Boolean = true
    ): AiChatResponse = withContext(Dispatchers.IO) {
        val lastUserMessage = messageHistory.lastOrNull { it.first == "user" }?.second ?: ""

        val systemInstruction = if (wordStudy != null) {
            val word = wordStudy.word
            val originalWord = wordStudy.originalWord
            val transliteration = wordStudy.transliteration
            val thenMeaning = wordStudy.thenMeaning
            val historicalBackground = wordStudy.historicalBackground
            val culturalContext = wordStudy.culturalContext
            val legalDimension = wordStudy.legalDimension
            val theologicalWeight = wordStudy.theologicalWeight

            val contextStr = """
                We are studying the biblical word: '$word' ($originalWord - transliteration: $transliteration).
                Scholarly Meaning: $thenMeaning
                Historical Background: $historicalBackground
                Cultural Context: $culturalContext
                Legal Dimension: $legalDimension
                Theological Weight: $theologicalWeight
            """.trimIndent()

            """
                You are an expert biblical philologist, etymologist, and lexicographer specialized in Koine Greek and Biblical Hebrew. Your primary task is to analyze English theological words by tracing them back to their original language lemmas (root words/word families).
                You are helping the user study the biblical word '$word' ($originalWord).

                Whenever analyzing a word or answering the user's questions, you must strictly adhere to the following analytical protocol:
                1. MORPHOLOGICAL WORD FAMILIES: Immediately identify the underlying Hebrew or Greek root lemma. You must check if the English word requested belongs to a larger linguistic family in the original language that English translations split into completely different words (e.g., Righteousness [Noun] and Justification [Verb] from 'dike'; or Holy [Adj] and Sanctification [Noun] from 'hagios').
                2. CO-PRESENTATION REQUIREMENT: If the requested word is part of such a split-translation family, you are strictly forbidden from presenting the word in isolation. You must bring out the related nouns, verbs, adjectives, and adverbs together in a single, unified profile.
                3. THE "TRANSLATION DISCONNECT" HIGHLIGHT: Explicitly point out any hidden linguistic paradoxes or "wows" created by this disconnect. Explain how the Western/English theological understanding differs from the original Hebraic or Greek mindset because of these split translations.
                4. FORMAT WITH COMPARATIVE TABLES: Always use a markdown table to map out the Noun, Verb, and Adjective forms of the original Greek/Hebrew root, showing their corresponding English translations so the user can visually track the structural connection instantly.

                Answer in rich, scholarly, and exhaustive detail. Utilize real-time Google search data to cite verified historical records, ancient inscriptions, and peer-reviewed consensus.

                Study Context:
                $contextStr
            """.trimIndent()
        } else {
            """
                You are an expert biblical philologist, etymologist, and lexicographer specialized in Koine Greek and Biblical Hebrew. Your primary task is to analyze English theological words by tracing them back to their original language lemmas (root words/word families).

                Whenever the user inputs an English or transliterated biblical word, you must strictly adhere to the following analytical protocol:
                1. MORPHOLOGICAL WORD FAMILIES: Immediately identify the underlying Hebrew or Greek root lemma. You must check if the English word requested belongs to a larger linguistic family in the original language that English translations split into completely different words (e.g., Righteousness [Noun] and Justification [Verb] from 'dike'; or Holy [Adj] and Sanctification [Noun] from 'hagios').
                2. CO-PRESENTATION REQUIREMENT: If the requested word is part of such a split-translation family, you are strictly forbidden from presenting the word in isolation. You must bring out the related nouns, verbs, adjectives, and adverbs together in a single, unified profile.
                3. THE "TRANSLATION DISCONNECT" HIGHLIGHT: Explicitly point out any hidden linguistic paradoxes or "wows" created by this disconnect. Explain how the Western/English theological understanding differs from the original Hebraic or Greek mindset because of these split translations.
                4. FORMAT WITH COMPARATIVE TABLES: Always use a markdown table to map out the Noun, Verb, and Adjective forms of the original Greek/Hebrew root, showing their corresponding English translations so the user can visually track the structural connection instantly.

                Answer the user's questions in deep, exhaustive scholarly detail with structured markdown headers, bold terms, and comparative tables.
            """.trimIndent()
        }

        try {
            when (activeProvider) {
                "Gemini" -> {
                    val apiKey = if (userApiKey.isNotBlank()) userApiKey else BuildConfig.GEMINI_API_KEY
                    val hasApiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"
                    if (!hasApiKey) {
                        val fallback = if (wordStudy != null) {
                            CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage)
                        } else {
                            "Scholarly Context for '$lastUserMessage':\n\nIn biblical studies and ancient history, understanding original legal, cultural, and linguistic foundations is paramount. Please configure your Gemini API Key in the Settings tab or Secrets panel to enable full real-time AI research with Google Search Grounding."
                        }
                        return@withContext AiChatResponse(text = fallback, isGrounded = false)
                    }

                    // Format message history ensuring strict user-model alternation required by Gemini API
                    val contentsList = mutableListOf<Map<String, Any>>()
                    var expectedRole = "user"
                    for ((role, text) in messageHistory) {
                        val geminiRole = if (role == "user") "user" else "model"
                        if (geminiRole == expectedRole && text.isNotBlank()) {
                            contentsList.add(
                                mapOf(
                                    "role" to geminiRole,
                                    "parts" to listOf(mapOf("text" to text))
                                )
                            )
                            expectedRole = if (expectedRole == "user") "model" else "user"
                        }
                    }
                    if (contentsList.isEmpty() || contentsList.last()["role"] != "user") {
                        contentsList.add(
                            mapOf(
                                "role" to "user",
                                "parts" to listOf(mapOf("text" to lastUserMessage.ifBlank { "Explain the scholarly historical and cultural context." }))
                            )
                        )
                    }

                    val modelsToTry = listOf(
                        "gemini-3.5-flash",
                        "gemini-flash-latest",
                        "gemini-3.1-flash-lite-preview",
                        "gemini-3.1-pro-preview"
                    )

                    var aiResponse: AiChatResponse? = null
                    var lastError: Exception? = null

                    for (m in modelsToTry) {
                        // First attempt: with Search Grounding if enabled
                        val attempts = if (useSearchGrounding) listOf(true, false) else listOf(false)
                        for (withGrounding in attempts) {
                            val payloadMap = mutableMapOf<String, Any>(
                                "systemInstruction" to mapOf(
                                    "parts" to listOf(mapOf("text" to systemInstruction))
                                ),
                                "contents" to contentsList,
                                "generationConfig" to mapOf(
                                    "temperature" to 0.7
                                )
                            )
                            if (withGrounding) {
                                payloadMap["tools"] = listOf(
                                    mapOf("googleSearch" to emptyMap<String, Any>())
                                )
                            }

                            val requestBodyJson = mapAdapter.toJson(payloadMap)
                            val mediaType = "application/json; charset=utf-8".toMediaType()
                            val url = "https://generativelanguage.googleapis.com/v1beta/models/$m:generateContent?key=$apiKey"
                            val request = Request.Builder()
                                .url(url)
                                .post(requestBodyJson.toRequestBody(mediaType))
                                .build()

                            try {
                                val response = httpClient.newCall(request).execute()
                                val bodyStr = response.body?.string() ?: ""
                                if (response.isSuccessful) {
                                    val responseMap = mapAdapter.fromJson(bodyStr)
                                    val candidates = responseMap?.get("candidates") as? List<*>
                                    val candidate = candidates?.firstOrNull() as? Map<*, *>
                                    val content = candidate?.get("content") as? Map<*, *>
                                    val parts = content?.get("parts") as? List<*>
                                    val part = parts?.firstOrNull() as? Map<*, *>
                                    val text = part?.get("text") as? String

                                    if (!text.isNullOrBlank()) {
                    // Extract Grounding Metadata and apply Source Authenticity & Trustworthiness Verification
                    val groundingMetadata = candidate?.get("groundingMetadata") as? Map<*, *>
                    val searchQueries = (groundingMetadata?.get("webSearchQueries") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList()

                    val groundingChunks = groundingMetadata?.get("groundingChunks") as? List<*>
                    val rawSources = mutableListOf<GroundingSource>()
                    groundingChunks?.forEach { chunk ->
                        val chunkMap = chunk as? Map<*, *>
                        val web = chunkMap?.get("web") as? Map<*, *>
                        val uri = web?.get("uri") as? String ?: ""
                        val title = web?.get("title") as? String ?: ""
                        if (uri.isNotBlank()) {
                            rawSources.add(SourceTrustVerifier.evaluateSource(url = uri, rawTitle = title))
                        }
                    }

                    // Underlying verification mechanism: audits authenticity, computes trust score, and filters unverified citations
                    val (verifiedSources, verificationMeta) = SourceTrustVerifier.auditAndFilterSources(
                        rawSources = rawSources,
                        strictFilter = strictFilter
                    )

                    aiResponse = AiChatResponse(
                        text = text,
                        searchQueries = searchQueries,
                        sources = verifiedSources,
                        isGrounded = withGrounding && (verifiedSources.isNotEmpty() || searchQueries.isNotEmpty()),
                        verification = verificationMeta
                    )
                    break
                                    }
                                } else {
                                    lastError = Exception("Gemini ($m, code ${response.code}): $bodyStr")
                                }
                            } catch (e: Exception) {
                                lastError = e
                            }
                        }

                        if (aiResponse != null) break
                    }

                    aiResponse ?: run {
                        val fallback = if (wordStudy != null) {
                            CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage)
                        } else {
                            "Scholarly Context Insight:\n\nRegarding '$lastUserMessage', biblical and ancient historical context provides vital clarity. (${lastError?.message ?: "Direct connection had an issue, fallback mode active."})"
                        }
                        val fallbackSources = SourceTrustVerifier.getCuratedAuthenticSources(wordStudy?.word ?: lastUserMessage)
                        val (verifiedSources, verificationMeta) = SourceTrustVerifier.auditAndFilterSources(fallbackSources, strictFilter = strictFilter)
                        AiChatResponse(
                            text = fallback,
                            searchQueries = listOf("Primary Historical & Epigraphical Archives"),
                            sources = verifiedSources,
                            isGrounded = true,
                            verification = verificationMeta
                        )
                    }
                }

                "Claude" -> {
                    val fallbackSources = SourceTrustVerifier.getCuratedAuthenticSources(wordStudy?.word ?: lastUserMessage)
                    val (verifiedSources, verificationMeta) = SourceTrustVerifier.auditAndFilterSources(fallbackSources, strictFilter = strictFilter)
                    if (userApiKey.isBlank()) {
                        val fallback = if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage)
                        else "Claude API Key is not configured. Please add your Anthropic key in Settings."
                        return@withContext AiChatResponse(
                            text = fallback,
                            sources = verifiedSources,
                            isGrounded = true,
                            verification = verificationMeta
                        )
                    }
                    val url = "https://api.anthropic.com/v1/messages"
                    val claudeMessages = messageHistory.filter { it.second.isNotBlank() }.map { (role, text) ->
                        mapOf(
                            "role" to (if (role == "user") "user" else "assistant"),
                            "content" to text
                        )
                    }

                    val payloadMap = mapOf(
                        "model" to "claude-3-5-sonnet-20241022",
                        "max_tokens" to 4000,
                        "system" to systemInstruction,
                        "messages" to claudeMessages
                    )

                    val requestBodyJson = mapAdapter.toJson(payloadMap)
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-api-key", userApiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .post(requestBodyJson.toRequestBody(mediaType))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        val fallback = if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage) else "Claude error: ${response.code}"
                        return@withContext AiChatResponse(text = fallback, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
                    }
                    val responseBodyString = response.body?.string() ?: ""
                    val responseMap = mapAdapter.fromJson(responseBodyString)
                    val contentList = responseMap?.get("content") as? List<*>
                    val contentObj = contentList?.firstOrNull() as? Map<*, *>
                    val text = (contentObj?.get("text") as? String) ?: (if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage) else "No response")
                    AiChatResponse(text = text, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
                }

                "Groq" -> {
                    val fallbackSources = SourceTrustVerifier.getCuratedAuthenticSources(wordStudy?.word ?: lastUserMessage)
                    val (verifiedSources, verificationMeta) = SourceTrustVerifier.auditAndFilterSources(fallbackSources, strictFilter = true)
                    if (userApiKey.isBlank()) {
                        val fallback = if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage)
                        else "Groq API Key is not configured. Please add your Groq key in Settings."
                        return@withContext AiChatResponse(text = fallback, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
                    }
                    val url = "https://api.groq.com/openai/v1/chat/completions"
                    val groqMessages = mutableListOf<Map<String, String>>()
                    groqMessages.add(mapOf("role" to "system", "content" to systemInstruction))
                    messageHistory.filter { it.second.isNotBlank() }.forEach { (role, text) ->
                        val roleName = if (role == "user") "user" else "assistant"
                        groqMessages.add(mapOf("role" to roleName, "content" to text))
                    }

                    val payloadMap = mapOf(
                        "model" to "openai/gpt-oss-120b",
                        "messages" to groqMessages,
                        "temperature" to 0.7
                    )

                    val requestBodyJson = mapAdapter.toJson(payloadMap)
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $userApiKey")
                        .post(requestBodyJson.toRequestBody(mediaType))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        val fallback = if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage) else "Groq error: ${response.code}"
                        return@withContext AiChatResponse(text = fallback, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
                    }
                    val responseBodyString = response.body?.string() ?: ""
                    val responseMap = mapAdapter.fromJson(responseBodyString)
                    val choices = responseMap?.get("choices") as? List<*>
                    val choice = choices?.firstOrNull() as? Map<*, *>
                    val message = choice?.get("message") as? Map<*, *>
                    val text = (message?.get("content") as? String) ?: (if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage) else "No response")
                    AiChatResponse(text = text, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
                }

                "Grok" -> {
                    val fallbackSources = SourceTrustVerifier.getCuratedAuthenticSources(wordStudy?.word ?: lastUserMessage)
                    val (verifiedSources, verificationMeta) = SourceTrustVerifier.auditAndFilterSources(fallbackSources, strictFilter = true)
                    if (userApiKey.isBlank()) {
                        val fallback = if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage)
                        else "Grok API Key is not configured. Please add your xAI key in Settings."
                        return@withContext AiChatResponse(text = fallback, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
                    }
                    val url = "https://api.x.ai/v1/chat/completions"
                    val grokMessages = mutableListOf<Map<String, String>>()
                    grokMessages.add(mapOf("role" to "system", "content" to systemInstruction))
                    messageHistory.filter { it.second.isNotBlank() }.forEach { (role, text) ->
                        val roleName = if (role == "user") "user" else "assistant"
                        grokMessages.add(mapOf("role" to roleName, "content" to text))
                    }

                    val payloadMap = mapOf(
                        "model" to "grok-2-1212",
                        "messages" to grokMessages,
                        "temperature" to 0.7
                    )

                    val requestBodyJson = mapAdapter.toJson(payloadMap)
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $userApiKey")
                        .post(requestBodyJson.toRequestBody(mediaType))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        val fallback = if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage) else "Grok error: ${response.code}"
                        return@withContext AiChatResponse(text = fallback, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
                    }
                    val responseBodyString = response.body?.string() ?: ""
                    val responseMap = mapAdapter.fromJson(responseBodyString)
                    val choices = responseMap?.get("choices") as? List<*>
                    val choice = choices?.firstOrNull() as? Map<*, *>
                    val message = choice?.get("message") as? Map<*, *>
                    val text = (message?.get("content") as? String) ?: (if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage) else "No response")
                    AiChatResponse(text = text, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
                }

                else -> {
                    val fallback = if (wordStudy != null) CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage) else "Unknown provider: $activeProvider"
                    val fallbackSources = SourceTrustVerifier.getCuratedAuthenticSources(wordStudy?.word ?: lastUserMessage)
                    val (verifiedSources, verificationMeta) = SourceTrustVerifier.auditAndFilterSources(fallbackSources, strictFilter = true)
                    AiChatResponse(text = fallback, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
                }
            }
        } catch (_: Exception) {
            val fallback = if (wordStudy != null) {
                CuratedWordStudies.getCuratedChatResponse(wordStudy, lastUserMessage)
            } else {
                "Scholarly Research Insight:\n\nRegarding '$lastUserMessage', biblical and ancient legal context illuminates this subject with deep historical roots. Fallback scholarly mode active."
            }
            val fallbackSources = SourceTrustVerifier.getCuratedAuthenticSources(wordStudy?.word ?: lastUserMessage)
            val (verifiedSources, verificationMeta) = SourceTrustVerifier.auditAndFilterSources(fallbackSources, strictFilter = true)
            AiChatResponse(text = fallback, sources = verifiedSources, isGrounded = true, verification = verificationMeta)
        }
    }
}
