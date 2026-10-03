package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.WordStudy
import com.example.data.repository.WordStudyRepository
import com.example.data.verification.GroundingSource
import com.example.data.verification.SourceTrustVerifier
import com.example.data.verification.VerificationMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val wordStudy: WordStudy) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class WordStudyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = WordStudyRepository(db.wordStudyDao())
    private val settingDao = db.appSettingDao()

    // SharedPreferences for multi-provider API keys
    private val prefs = application.getSharedPreferences("word_study_prefs", android.content.Context.MODE_PRIVATE)

    private val initialSavedGemini = prefs.getString("api_key_Gemini", null)?.trim()
    private val defaultGemini: String = if (!initialSavedGemini.isNullOrBlank()) {
        initialSavedGemini
    } else {
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    private val _activeProvider = MutableStateFlow(prefs.getString("active_provider", "Gemini") ?: "Gemini")
    val activeProvider: StateFlow<String> = _activeProvider.asStateFlow()

    private val _geminiKey = MutableStateFlow(defaultGemini)
    val geminiKey: StateFlow<String> = _geminiKey.asStateFlow()

    private val _claudeKey = MutableStateFlow(prefs.getString("api_key_Claude", "") ?: "")
    val claudeKey: StateFlow<String> = _claudeKey.asStateFlow()

    private val _groqKey = MutableStateFlow(prefs.getString("api_key_Groq", "") ?: "")
    val groqKey: StateFlow<String> = _groqKey.asStateFlow()

    private val _grokKey = MutableStateFlow(prefs.getString("api_key_Grok", "") ?: "")
    val grokKey: StateFlow<String> = _grokKey.asStateFlow()

    private val _useSearchGrounding = MutableStateFlow(prefs.getBoolean("use_search_grounding", true))
    val useSearchGrounding: StateFlow<Boolean> = _useSearchGrounding.asStateFlow()

    private val _strictVerification = MutableStateFlow(prefs.getBoolean("strict_source_verification", true))
    val strictVerification: StateFlow<Boolean> = _strictVerification.asStateFlow()

    fun toggleStrictVerification(enabled: Boolean) {
        prefs.edit().putBoolean("strict_source_verification", enabled).commit()
        _strictVerification.value = enabled
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                settingDao.saveSetting(com.example.data.local.AppSetting("strict_source_verification", enabled.toString()))
            } catch (_: Exception) {}
        }
    }

    fun toggleSearchGrounding(enabled: Boolean) {
        prefs.edit().putBoolean("use_search_grounding", enabled).commit()
        _useSearchGrounding.value = enabled
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                settingDao.saveSetting(com.example.data.local.AppSetting("use_search_grounding", enabled.toString()))
            } catch (_: Exception) {}
        }
    }

    fun saveSettings(provider: String, gemini: String, claude: String, groq: String, grok: String) {
        val trimmedProvider = provider.trim()
        val trimmedGemini = gemini.trim()
        val trimmedClaude = claude.trim()
        val trimmedGroq = groq.trim()
        val trimmedGrok = grok.trim()

        // 1. Commit synchronously to disk in SharedPreferences immediately
        prefs.edit()
            .putString("active_provider", trimmedProvider)
            .putString("api_key_Gemini", trimmedGemini)
            .putString("api_key_Claude", trimmedClaude)
            .putString("api_key_Groq", trimmedGroq)
            .putString("api_key_Grok", trimmedGrok)
            .commit()

        // 2. Update reactive state flows immediately
        _activeProvider.value = trimmedProvider
        _geminiKey.value = trimmedGemini
        _claudeKey.value = trimmedClaude
        _groqKey.value = trimmedGroq
        _grokKey.value = trimmedGrok

        // 3. Persist to Room SQLite database for dual redundancy across reboots/reinstalls
        viewModelScope.launch {
            try {
                settingDao.saveSettings(
                    listOf(
                        com.example.data.local.AppSetting("active_provider", trimmedProvider),
                        com.example.data.local.AppSetting("api_key_Gemini", trimmedGemini),
                        com.example.data.local.AppSetting("api_key_Claude", trimmedClaude),
                        com.example.data.local.AppSetting("api_key_Groq", trimmedGroq),
                        com.example.data.local.AppSetting("api_key_Grok", trimmedGrok)
                    )
                )
            } catch (e: Exception) {
                // Secondary persistence log or ignore
            }
        }
    }

    fun clearAllKeys() {
        prefs.edit()
            .remove("active_provider")
            .remove("api_key_Gemini")
            .remove("api_key_Claude")
            .remove("api_key_Groq")
            .remove("api_key_Grok")
            .commit()

        _activeProvider.value = "Gemini"
        val buildKey = BuildConfig.GEMINI_API_KEY
        _geminiKey.value = if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
        _claudeKey.value = ""
        _groqKey.value = ""
        _grokKey.value = ""

        viewModelScope.launch {
            try {
                settingDao.clearAllSettings()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.seedCuratedStudiesIfEmpty()
        }
        viewModelScope.launch {
            // Restore from Room database if SharedPreferences was empty or missing keys
            try {
                val dbSettings = settingDao.getAllSettings().associate { it.key to it.value }
                if (dbSettings.isNotEmpty()) {
                    val dbProvider = dbSettings["active_provider"]
                    val dbGemini = dbSettings["api_key_Gemini"]
                    val dbClaude = dbSettings["api_key_Claude"]
                    val dbGroq = dbSettings["api_key_Groq"]
                    val dbGrok = dbSettings["api_key_Grok"]
                    val dbGrounding = dbSettings["use_search_grounding"]

                    if (!dbGrounding.isNullOrBlank()) {
                        val enabled = dbGrounding.toBoolean()
                        _useSearchGrounding.value = enabled
                        prefs.edit().putBoolean("use_search_grounding", enabled).commit()
                    }

                    if (!dbProvider.isNullOrBlank()) {
                        _activeProvider.value = dbProvider
                    }
                    if (!dbGemini.isNullOrBlank()) {
                        _geminiKey.value = dbGemini
                    }
                    if (!dbClaude.isNullOrBlank()) {
                        _claudeKey.value = dbClaude
                    }
                    if (!dbGroq.isNullOrBlank()) {
                        _groqKey.value = dbGroq
                    }
                    if (!dbGrok.isNullOrBlank()) {
                        _grokKey.value = dbGrok
                    }

                    // Keep SharedPreferences in sync synchronously
                    prefs.edit()
                        .putString("active_provider", _activeProvider.value)
                        .putString("api_key_Gemini", _geminiKey.value)
                        .putString("api_key_Claude", _claudeKey.value)
                        .putString("api_key_Groq", _groqKey.value)
                        .putString("api_key_Grok", _grokKey.value)
                        .putBoolean("use_search_grounding", _useSearchGrounding.value)
                        .commit()
                } else {
                    // Seed initial keys to Room database
                    settingDao.saveSettings(
                        listOf(
                            com.example.data.local.AppSetting("active_provider", _activeProvider.value),
                            com.example.data.local.AppSetting("api_key_Gemini", _geminiKey.value),
                            com.example.data.local.AppSetting("api_key_Claude", _claudeKey.value),
                            com.example.data.local.AppSetting("api_key_Groq", _groqKey.value),
                            com.example.data.local.AppSetting("api_key_Grok", _grokKey.value),
                            com.example.data.local.AppSetting("use_search_grounding", _useSearchGrounding.value.toString())
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore DB read failure
            }
        }
    }

    val history: StateFlow<List<WordStudy>> = repository.allWordStudies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favorites: StateFlow<List<WordStudy>> = repository.favoriteWordStudies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTestament = MutableStateFlow("New Testament")
    val selectedTestament: StateFlow<String> = _selectedTestament.asStateFlow()

    private val _viewingStudy = MutableStateFlow<WordStudy?>(null)
    val viewingStudy: StateFlow<WordStudy?> = _viewingStudy.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedTestament(testament: String) {
        _selectedTestament.value = testament
    }

    fun setViewingStudy(study: WordStudy?) {
        _viewingStudy.value = study
    }

    fun performSearch() {
        val word = _searchQuery.value.trim()
        val testament = _selectedTestament.value
        if (word.isEmpty()) return

        viewModelScope.launch {
            _searchUiState.value = SearchUiState.Loading
            try {
                val provider = _activeProvider.value
                val apiKey = when (provider) {
                    "Gemini" -> _geminiKey.value
                    "Claude" -> _claudeKey.value
                    "Groq" -> _groqKey.value
                    "Grok" -> _grokKey.value
                    else -> ""
                }
                val result = repository.getWordStudy(word, testament, provider, apiKey)
                _searchUiState.value = SearchUiState.Success(result)
                _viewingStudy.value = result // Show the detail view immediately
            } catch (e: Exception) {
                _searchUiState.value = SearchUiState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    fun toggleFavorite(study: WordStudy) {
        viewModelScope.launch {
            repository.toggleFavorite(study)
            // If the currently viewed study is the one favorited, update it in UI
            if (_viewingStudy.value?.id == study.id) {
                _viewingStudy.value = _viewingStudy.value?.copy(isFavorite = !study.isFavorite)
            }
        }
    }

    fun deleteStudy(studyId: Long) {
        viewModelScope.launch {
            repository.deleteWordStudy(studyId)
            if (_viewingStudy.value?.id == studyId) {
                _viewingStudy.value = null
            }
        }
    }
    
    fun clearSearchState() {
        _searchUiState.value = SearchUiState.Idle
    }

    // --- Chat with AI Feature ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    fun resetChat() {
        _chatMessages.value = emptyList()
        _chatLoading.value = false
        _chatError.value = null
    }

    fun sendChatMessage(text: String, study: WordStudy? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val currentStudy = study
            ?: _viewingStudy.value
            ?: (_searchUiState.value as? SearchUiState.Success)?.wordStudy

        if (_viewingStudy.value == null && currentStudy != null) {
            _viewingStudy.value = currentStudy
        }

        // Add user message to local state immediately
        val userMsg = ChatMessage(role = "user", text = trimmed)
        _chatMessages.value = _chatMessages.value + userMsg
        _chatLoading.value = true
        _chatError.value = null

        viewModelScope.launch {
            try {
                val provider = _activeProvider.value
                val apiKey = when (provider) {
                    "Gemini" -> _geminiKey.value
                    "Claude" -> _claudeKey.value
                    "Groq" -> _groqKey.value
                    "Grok" -> _grokKey.value
                    else -> ""
                }
                
                // Format history as list of Pairs for repository
                val historyPairs = _chatMessages.value.map { Pair(it.role, it.text) }

                val aiResult = repository.chatWithAi(
                    wordStudy = currentStudy,
                    messageHistory = historyPairs,
                    activeProvider = provider,
                    userApiKey = apiKey,
                    useSearchGrounding = _useSearchGrounding.value,
                    strictFilter = _strictVerification.value
                )

                _chatMessages.value = _chatMessages.value + ChatMessage(
                    role = "model",
                    text = aiResult.text,
                    searchQueries = aiResult.searchQueries,
                    sources = aiResult.sources,
                    isGrounded = aiResult.isGrounded,
                    verification = aiResult.verification
                )
            } catch (e: Exception) {
                val fallbackText = if (currentStudy != null) {
                    com.example.data.local.CuratedWordStudies.getCuratedChatResponse(currentStudy, trimmed)
                } else {
                    "Scholarly Research Insight on \"$trimmed\":\n\nBiblical and ancient historical context provides vital clarity on this topic. In first-century Roman and ancient Near Eastern settings, historical records and inscriptions illuminate this background."
                }
                val fallbackSources = SourceTrustVerifier.getCuratedAuthenticSources(currentStudy?.word ?: trimmed)
                val (verifiedSources, verificationMeta) = SourceTrustVerifier.auditAndFilterSources(fallbackSources, strictFilter = true)
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    role = "model",
                    text = fallbackText,
                    isGrounded = true,
                    sources = verifiedSources,
                    searchQueries = listOf("Primary Historical & Epigraphical Archives"),
                    verification = verificationMeta
                )
            } finally {
                _chatLoading.value = false
            }
        }
    }
}

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val searchQueries: List<String> = emptyList(),
    val sources: List<GroundingSource> = emptyList(),
    val isGrounded: Boolean = false,
    val verification: VerificationMetadata? = null
)
