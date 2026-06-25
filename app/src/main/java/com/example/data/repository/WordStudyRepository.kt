package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.WordStudyDao
import com.example.data.model.WordStudy
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class WordStudyRepository(private val wordStudyDao: WordStudyDao) {

    val allWordStudies: Flow<List<WordStudy>> = wordStudyDao.getAllWordStudies()
    val favoriteWordStudies: Flow<List<WordStudy>> = wordStudyDao.getFavoriteWordStudies()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val wordStudyAdapter = moshi.adapter(WordStudy::class.java)
    private val mapAdapter = moshi.adapter(Map::class.java)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
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

    suspend fun getWordStudy(
        word: String, 
        testament: String,
        activeProvider: String = "Gemini",
        userApiKey: String = ""
    ): WordStudy = withContext(Dispatchers.IO) {
        val cleanedWord = word.trim()
        val cleanedTestament = testament.trim()

        // 1. Check database cache
        val cached = wordStudyDao.getWordStudyByWordAndTestament(cleanedWord, cleanedTestament)
        if (cached != null) {
            // Update timestamp to bring to top of history
            val updated = cached.copy(timestamp = System.currentTimeMillis())
            wordStudyDao.updateWordStudy(updated)
            return@withContext updated
        }

        // 2. Define System Instructions & Prompts
        val systemInstruction = """
            You are a world-class Biblical scholar and linguist expert in first-century history, ancient languages (Greek, Hebrew, Aramaic), Roman law, Jewish law (Torah/Talmudic jurisprudence), and Greco-Roman cultural history.
            Your task is to provide a deep, scholarly, and historically accurate word study of the Bible word provided.
            You MUST return your response as a single, strictly valid JSON object.
            Do not enclose the JSON in markdown code blocks like ```json ... ```. Just return the raw JSON text.
            The JSON object must have exactly these keys:
            - "word": String (the search word)
            - "testament": String (the testament context: "Old Testament" or "New Testament")
            - "originalWord": String (the original Hebrew/Aramaic for Old Testament, or Greek for New Testament, in original script like 'υἱοθεσία' or 'חֶסֶד')
            - "transliteration": String (e.g. "huiothesia" or "chesed")
            - "strongsNumber": String (e.g. "G5206" or "H2617")
            - "literalMeaning": String (e.g. "placement as a son" or "unfailing covenant love")
            - "thenMeaning": String (what it meant THEN in its original first-century Roman/Jewish historical/legal/cultural context. Go into rich, highly educational detail.)
            - "nowMeaning": String (what modern readers often think it means NOW, highlighting modern misunderstandings or shallow theological interpretations)
            - "historicalBackground": String (the historical situation, audience, and era-specific dynamics of the word)
            - "culturalContext": String (the cultural norms, expectations, and societal frameworks of the first-century Roman or ancient Near Eastern audience)
            - "legalDimension": String (the legal framework under Roman or Jewish law. e.g., Roman adoption legal processes, ancient covenant legal binding, Roman grace/patronage contracts. Explain how this changes the meaning!)
            - "theologicalWeight": String (the theological significance and depth of this term when used by biblical authors like Paul, Jesus, or Moses)
            - "keyScriptures": Array of Strings (3-4 key scripture verses with references, e.g., ["Romans 8:15", "Galatians 4:5"])
            - "closingInsight": String (one elegant, powerful closing insight summarizing what modern believers miss about their faith by not knowing this original legal, historical, or cultural meaning)
        """.trimIndent()

        val promptText = "Provide a Bible word study for the word: '$cleanedWord' under the context of: '$cleanedTestament'."

        try {
            val rawJsonText: String = when (activeProvider) {
                "Gemini" -> {
                    val apiKey = if (userApiKey.isNotBlank()) userApiKey else BuildConfig.GEMINI_API_KEY
                    val hasApiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"
                    if (!hasApiKey) {
                        throw IllegalStateException("Gemini API Key is missing. Please configure it in Settings or AI Studio Secrets.")
                    }
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
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

                    val response = httpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw Exception("Gemini API Error (Code ${response.code}): ${response.body?.string() ?: "Unknown error"}")
                    }
                    val responseBodyString = response.body?.string() ?: throw Exception("Empty response body from Gemini API.")
                    
                    val responseMap = mapAdapter.fromJson(responseBodyString)
                    val candidates = responseMap?.get("candidates") as? List<*>
                    val candidate = candidates?.firstOrNull() as? Map<*, *>
                    val content = candidate?.get("content") as? Map<*, *>
                    val parts = content?.get("parts") as? List<*>
                    val part = parts?.firstOrNull() as? Map<*, *>
                    part?.get("text") as? String ?: throw Exception("Failed to extract text from Gemini response.")
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
                          "model": "llama-3.3-70b-versatile",
                          "messages": [
                            {"role": "system", "content": "${escapeJson(systemInstruction)}"},
                            {"role": "user", "content": "${escapeJson(promptText)}"}
                          ],
                          "response_format": {"type": "json_object"},
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
                          "response_format": {"type": "json_object"},
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

            // Clean any markdown wrappers
            val cleanedJson = rawJsonText
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsedStudy = try {
                wordStudyAdapter.fromJson(cleanedJson) ?: throw Exception("JSON parsed as null.")
            } catch (e: Exception) {
                throw Exception("Failed to parse Word Study JSON: ${e.message}. Raw response was: $cleanedJson")
            }

            // 3. Save to database
            val wordStudyToSave = parsedStudy.copy(
                word = cleanedWord, // Ensure matching keys
                testament = cleanedTestament,
                timestamp = System.currentTimeMillis()
            )
            val id = wordStudyDao.insertWordStudy(wordStudyToSave)
            
            return@withContext wordStudyToSave.copy(id = id)

        } catch (e: Exception) {
            // Check for curated offline fallback as recovery
            val fallback = getLocalFallback(cleanedWord, cleanedTestament)
            if (fallback != null) {
                val id = wordStudyDao.insertWordStudy(fallback)
                return@withContext fallback.copy(id = id)
            }
            throw e
        }
    }

    private fun getLocalFallback(word: String, testament: String): WordStudy? {
        val lowerWord = word.lowercase().trim()
        val lowerTestament = testament.lowercase().trim()
        
        if (lowerTestament.contains("new")) {
            return when (lowerWord) {
                "grace" -> WordStudy(
                    word = "grace",
                    testament = "New Testament",
                    originalWord = "χάρις",
                    transliteration = "charis",
                    strongsNumber = "G5485",
                    literalMeaning = "unmerited favor, reciprocal patronage, gift",
                    thenMeaning = "In the first-century Roman Empire, 'charis' was the cornerstone of the patronage system. A wealthy patron bestowed a life-changing benefit ('charis') onto a client. In response, the client was legally and socially bound to express lifelong gratitude, public praise, and unwavering loyalty to the patron. Grace was never an abstract theological concept; it was a relational, active contract that forged a permanent bond of reciprocal allegiance.",
                    nowMeaning = "Modern readers often reduce 'grace' to a passive, abstract feeling or a transaction where God overlooks sins without any expectation of life change. It is viewed as 'free' with no call to action, missing the profound first-century implication of active, reciprocal loyalty and covenant partnership.",
                    historicalBackground = "The Roman social fabric was held together by complex patronage networks. Emperor Augustus styled himself as the 'Supreme Patron' of the world, dispensing peace and security to his subjects, who owed him absolute devotion and worship in return.",
                    culturalContext = "First-century culture was deeply collectivist, governed by the honor-shame paradigm. To receive a substantial gift (charis) and fail to honor the patron was considered the ultimate social crime—ingratitude—which resulted in public disgrace.",
                    legalDimension = "In Greco-Roman jurisprudence, 'charis' established a voluntary but legally binding relationship of reciprocal obligation. The benefactor was expected to continue protectiveness, while the recipient was legally obligated to avoid actions that damaged the patron's reputation or interests.",
                    theologicalWeight = "When Apostle Paul used 'charis' to define salvation (e.g., Ephesians 2:8), he was brilliantly co-opting this Roman framework. He declared that God is our ultimate, supreme Patron who has given us an infinite gift. Salvation is free because we can never repay it, but it demands our absolute, exclusive allegiance (faith/loyalty) to God over Caesar.",
                    keyScriptures = listOf("Ephesians 2:8-9", "Romans 5:2", "Titus 2:11"),
                    closingInsight = "By viewing grace as merely passive sentiment, modern believers miss the powerful calling of active, reciprocal allegiance. Grace is an invitation into a binding, life-transforming covenant under the patronage of God, demanding our loyalty, not just our intellectual agreement."
                )
                "adoption" -> WordStudy(
                    word = "adoption",
                    testament = "New Testament",
                    originalWord = "υἱοθεσία",
                    transliteration = "huiothesia",
                    strongsNumber = "G5206",
                    literalMeaning = "placement as a son, legal sonship",
                    thenMeaning = "Under Roman Law (Patria Potestas), a birth father held absolute, life-and-death authority over his children. When an adult male was adopted (huiothesia), his old life was legally eradicated. All his previous debts were cancelled, his old identity was erased, and he was granted a new name and full, co-equal inheritance rights in his new father's estate. He was legally born again into a new household.",
                    nowMeaning = "Modern readers often think of adoption in purely emotional or foster-care terms. While beautiful, they miss the massive, universe-altering legal transformation. We think of ourselves as 'adopted' as if we are secondary, lesser children, rather than full legal heirs with co-equal status and immediate access to the Father.",
                    historicalBackground = "Julius Caesar adopted Octavian (who became Emperor Augustus) through huiothesia, passing on the entire Roman Empire and his divine name to his adopted son. This was a supreme tool of political and familial succession in the ancient world.",
                    culturalContext = "Adoption was primarily practiced among the Roman elite to secure a worthy heir for the family name, wealth, and household gods, often choosing a mature, proven adult rather than an infant.",
                    legalDimension = "Legally, the adopted son was completely transferred into the new father's power. The court issued an absolute decree: all prior biological obligations and debts were permanently extinguished, and the adoptee gained full legal status as if born of the new father's blood.",
                    theologicalWeight = "Paul uses this exact legal metaphor in Romans 8 to describe our relationship with God. When we are adopted, our old debts (sin, spiritual slavery) are legally abolished. We receive the Spirit of sonship and the legal right to call God 'Abba' (Father), co-inheriting everything with Christ.",
                    keyScriptures = listOf("Romans 8:15", "Galatians 4:4-5", "Ephesians 1:5"),
                    closingInsight = "If you live with spiritual insecurity, feeling like an outsider or carrying the guilt of past debts, you are living like a slave, not an heir. Under Roman huiothesia, your old debts are legally non-existent. You are a full heir of God, clothed in absolute security."
                )
                "redemption" -> WordStudy(
                    word = "redemption",
                    testament = "New Testament",
                    originalWord = "ἀπολύτρωσις",
                    transliteration = "apolytrosis",
                    strongsNumber = "G629",
                    literalMeaning = "buying back, releasing by paying a ransom",
                    thenMeaning = "In the first-century Roman Empire, there were over 60 million slaves. 'Apolytrosis' was the technical legal term for purchasing a slave's freedom from the auction block. A benefactor would pay the full market ransom (lytron) to the slave owner in a temple court. The slave was then legally declared 'the property of the deity'—which was the ancient legal mechanism to make them permanently free from human masters.",
                    nowMeaning = "Today, redemption is treated as a vague religious buzzword meaning 'becoming a better person' or 'getting a second chance.' We miss the legal, commercial reality: we were captives on the auction block of sin and death, unable to free ourselves, and a literal, infinite ransom was paid to secure our permanent release.",
                    historicalBackground = "Manumission of slaves (releasing them from bondage) was a common legal practice in the Greco-Roman world, often performed through sacral manumission where the slave saved money or a patron paid the temple treasury.",
                    culturalContext = "Being a slave in Rome meant having zero legal rights, being treated as 'living tools' (instrumentum vocale). Freedom was not just an emotional relief; it was the recovery of human dignity, legal standing, and citizenship.",
                    legalDimension = "The legal process of apolytrosis involved a commercial exchange and a formal change of ownership. Because the ransom was paid to the god of the temple, the freed person could never be re-enslaved; they were legally protected under divine custody.",
                    theologicalWeight = "Jesus and Paul used this term to depict the cross. Jesus' life was the ransom (lytron) paid to buy us back from the power of darkness (Colossians 1:13). Our redemption means we are no longer slaves to sin, but now belong to God, our protector and liberator.",
                    keyScriptures = listOf("Ephesians 1:7", "Colossians 1:13-14", "Romans 3:24"),
                    closingInsight = "Without knowing this context, we try to earn our freedom or live in fear of being dragged back to the auction block. Your ransom has been paid in full at the highest legal level. You are legally, permanently free from human and spiritual bondage."
                )
                "faith" -> WordStudy(
                    word = "faith",
                    testament = "New Testament",
                    originalWord = "πίστις",
                    transliteration = "pistis",
                    strongsNumber = "G4102",
                    literalMeaning = "trust, allegiance, active faithfulness",
                    thenMeaning = "In the Greco-Roman world, 'pistis' was not merely mental agreement with a set of facts. It was a social and legal term representing absolute trust, fidelity, and sworn allegiance. To have 'pistis' in a ruler or a covenant partner meant aligning your entire life to support them. It was a term of personal and political loyalty, establishing mutual obligations of protection and fidelity.",
                    nowMeaning = "Modern readers often reduce 'faith' to a purely intellectual belief ('I agree that God exists') or a blind, emotional feeling. This strips the word of its active, loyal nature, separating belief from obedience and personal alignment with the Sovereign.",
                    historicalBackground = "In treaties and social contracts, 'pistis' was the mutual trust that allowed trade, diplomatic alliances, and legal contracts to exist across different regions of the Roman Empire.",
                    culturalContext = "In ancient client-patron relationships, 'pistis' was the client's public response of trust and absolute loyalty to a generous patron, reflecting their ongoing honor and commitment.",
                    legalDimension = "Legally, 'pistis' was a binding pledge of reliability. If an ally broke 'pistis,' they were legally and socially declared treaty-breakers, which justified military or economic intervention under Roman international law.",
                    theologicalWeight = "When biblical writers speak of 'faith in Christ,' they mean entering a covenant of absolute trust and lifelong allegiance to Jesus as the true King of the world, contrasting directly with swearing 'pistis' to Caesar.",
                    keyScriptures = listOf("Hebrews 11:1", "Romans 1:17", "Galatians 2:16"),
                    closingInsight = "Believing in God is not just about holding correct opinions in your mind; it is about pledging your life's allegiance. Biblical faith is active loyalty that finds its security in God's absolute faithfulness to us."
                )
                else -> null
            }
        } else if (lowerTestament.contains("old")) {
            return when (lowerWord) {
                "covenant" -> WordStudy(
                    word = "covenant",
                    testament = "Old Testament",
                    originalWord = "בְּרִית",
                    transliteration = "berit",
                    strongsNumber = "H1285",
                    literalMeaning = "shackle, bond, covenant agreement",
                    thenMeaning = "In the Ancient Near East, a 'berit' was not a standard business agreement. It was a solemn, life-and-death treaty that bound two unequal parties together as family. It was sealed by cutting sacrificial animals in half, with both parties walking between the pieces, essentially declaring: 'If I break this covenant, may I be slaughtered like these animals.' It established an unbreakable bond of kinship, loyalty, and mutual defense.",
                    nowMeaning = "Today, covenants are often confused with commercial contracts. A contract is a temporary, self-serving agreement based on mutual distrust ('if you do your part, I will do mine'). If one party fails, the contract is broken. But a biblical covenant is a permanent, sacrificial commitment based on love and kinship.",
                    historicalBackground = "Suzerain-Vassal treaties of the Hittite and Assyrian Empires (2nd millennium BC) heavily influenced biblical covenant structures, including the division of responsibilities, historical prologues, and lists of blessings and curses.",
                    culturalContext = "In ancient nomadic societies, survival depended entirely on tribal kinship. Covenants allowed non-relatives to be adopted into the tribe, receiving the full protection and inheritance rights of blood brothers.",
                    legalDimension = "The legal framework of 'berit' was sealed with blood oaths and binding stipulations. God's covenants are structurally asymmetrical (initiated by the Sovereign) yet legally bind God Himself to His promises, showing His infinite faithfulness to His people.",
                    theologicalWeight = "The entire biblical narrative revolves around covenants (Abrahamic, Mosaic, Davidic, and New). It shows God's relentless drive to bring humanity back into His royal household, culminating in Jesus' sacrifice—the ultimate sealing of the New Covenant in His own blood.",
                    keyScriptures = listOf("Genesis 15:17-18", "Jeremiah 31:31", "Hebrews 9:15"),
                    closingInsight = "When we see our relationship with God as a contract, we live in constant fear of failure, thinking our mistakes annul the deal. Recognizing it as a blood-sealed covenant reveals that God's commitment to us is unconditional and family-based, anchored in His absolute faithfulness."
                )
                "lovingkindness" -> WordStudy(
                    word = "lovingkindness",
                    testament = "Old Testament",
                    originalWord = "חֶסֶד",
                    transliteration = "chesed",
                    strongsNumber = "H2617",
                    literalMeaning = "covenant loyalty, steadfast love, active mercy",
                    thenMeaning = "In the Old Testament, 'chesed' is the active, passionate commitment to fulfill one's covenant obligations. It is not an emotion or a passive feeling; it is a relentless, action-oriented loyalty. When God exercises 'chesed' toward His people, He is acting in accordance with His covenant promises—even when His people fail. It is love that acts, rescues, and remains loyal when there is absolutely no benefit to the giver.",
                    nowMeaning = "We often translate 'chesed' as 'mercy' or 'love' in a modern romantic or sentimental sense. This strips the word of its steel. Sentimental love can fade when feelings change, but 'chesed' is a rock-solid, covenantal decision to remain loyal and supportive through adversity.",
                    historicalBackground = "In the rugged tribal realities of ancient Israel, loyalty (chesed) within a family or treaty was a matter of physical survival. David and Jonathan's covenant (1 Samuel 20) is a prime example of human 'chesed' overriding royal rivalry.",
                    culturalContext = "Ancient Semitic cultures highly valued hospitality and tribal loyalty. Showing 'chesed' to a stranger or ally was a sacred duty, establishing a bond of protective responsibility.",
                    legalDimension = "Legally, 'chesed' resides in the interface between law and relationship. It is the legal obligation of a covenant elevated and animated by deep, family-like devotion. It means doing far more than the letter of the law requires.",
                    theologicalWeight = "This is the primary word used to describe God's character in Exodus 34:6 ('abundant in lovingkindness and truth'). It is the foundation of biblical hope—God will not abandon His people because His covenant loyalty (chesed) is everlasting, outlasting human unfaithfulness.",
                    keyScriptures = listOf("Exodus 34:6", "Psalm 136:1", "Lamentations 3:22-23"),
                    closingInsight = "When you feel like God's love for you depends on your performance, you are confusing His love with human affection. God's 'chesed' is anchored in His covenant faithfulness. He remains loyal to you because of who He is, not because of what you do."
                )
                "redeemer" -> WordStudy(
                    word = "redeemer",
                    testament = "Old Testament",
                    originalWord = "גּוֹאֵל",
                    transliteration = "goel",
                    strongsNumber = "H1350",
                    literalMeaning = "kinsman-redeemer, family avenger/protector",
                    thenMeaning = "In ancient Israel, a 'goel' was the nearest male relative responsible for defending and restoring the family's honor, blood, and property. If a family member fell into debt and had to sell their ancestral land or sell themselves into slavery, the 'goel' was legally obligated to pay the debt, buy back the land, or purchase the relative's freedom. He stepped in as the family's legal champion.",
                    nowMeaning = "Today, 'redeemer' is viewed as a purely spiritual Savior who takes us to heaven when we die. This misses the raw, legal, and relational reality of the 'goel' as a family champion who steps into our physical, economic, and social brokenness to restore our inheritance and dignity.",
                    historicalBackground = "The Book of Ruth is the classic historical narrative of the kinsman-redeemer in action, where Boaz legally redeems Elimelech's land and marries Ruth to preserve the family line.",
                    culturalContext = "In tribal Israel, land was a sacred, permanent trust from God that could never be permanently sold out of the family. The 'goel' was the legal guardian who prevented the permanent loss of ancestral heritage.",
                    legalDimension = "Under Levitical law, the duties of the 'goel' were strictly codified, outlining the order of kinship eligibility and the precise calculations for redeeming land, property, or persons.",
                    theologicalWeight = "Job famously cried, 'I know that my Redeemer (Goel) lives!' indicating his supreme confidence that God Himself would act as his legal champion and restore him. When Isaiah calls God the 'Redeemer of Israel,' he is declaring that God is our nearest Kin who has taken legal responsibility for our restoration.",
                    keyScriptures = listOf("Leviticus 25:25", "Job 19:25", "Ruth 4:9-10"),
                    closingInsight = "Knowing God is your 'Goel' means realizing He is not a distant judge, but your closest Relative. He is legally, passionately committed to buying back everything you have lost, restoring your heritage, and championing your cause."
                )
                "righteousness" -> WordStudy(
                    word = "righteousness",
                    testament = "Old Testament",
                    originalWord = "צְדָקָה",
                    transliteration = "tsedakah",
                    strongsNumber = "H6666",
                    literalMeaning = "justice, covenant-standard, relational rightness",
                    thenMeaning = "In ancient Hebrew thought, 'tsedakah' was not an abstract moral perfection or compliance with a detached legal code. It was a relational term meaning 'faithfulness to the expectations of a relationship.' To be righteous meant fulfilling your relational obligations to both God and fellow human beings, especially caring for the vulnerable (widow, orphan, stranger) in accordance with the covenant.",
                    nowMeaning = "Modern readers often think of 'righteousness' as self-righteous moralism, legalistic perfection, or an individualistic, private holiness that is disconnected from social justice, community responsibility, and active relational care.",
                    historicalBackground = "In Hebrew poetry, 'tsedakah' is often paired with 'mishpat' (justice) to describe the foundational pillars of God's throne and the ideal standard for Israelite leaders.",
                    culturalContext = "Ancient Israelite culture was highly communal. Relational rightness (tsedakah) was measured by how well a citizen contributed to the wholeness and legal defense of the community's weakest members.",
                    legalDimension = "The legal framework of 'tsedakah' was covenantal. It is the active legal vindication of the oppressed, where the judge does not just decide a case neutrally but actively steps in to deliver and restore the victim of injustice.",
                    theologicalWeight = "God's righteousness is His active, saving faithfulness to His covenant promises. He acts righteously by rescuing His people from bondage and restoring order, showing that His holiness is dynamically aligned with mercy and rescue.",
                    keyScriptures = listOf("Genesis 15:6", "Amos 5:24", "Isaiah 58:6-7"),
                    closingInsight = "If you think righteousness is only about following rules and avoiding sin, you miss its heartbeat. Biblical righteousness is the active restoration of right relationships, calling us to be channels of God's covenant loyalty and justice to the world."
                )
                else -> null
            }
        }
        return null
    }

    suspend fun toggleFavorite(wordStudy: WordStudy) = withContext(Dispatchers.IO) {
        wordStudyDao.updateWordStudy(wordStudy.copy(isFavorite = !wordStudy.isFavorite))
    }

    suspend fun deleteWordStudy(id: Long) = withContext(Dispatchers.IO) {
        wordStudyDao.deleteWordStudyById(id)
    }
}
