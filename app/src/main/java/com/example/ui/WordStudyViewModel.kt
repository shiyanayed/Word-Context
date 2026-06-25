package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.WordStudy
import com.example.data.repository.WordStudyRepository
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

    // SharedPreferences for multi-provider API keys
    private val prefs = application.getSharedPreferences("word_study_prefs", android.content.Context.MODE_PRIVATE)

    private val _activeProvider = MutableStateFlow(prefs.getString("active_provider", "Gemini") ?: "Gemini")
    val activeProvider: StateFlow<String> = _activeProvider.asStateFlow()

    private val _geminiKey = MutableStateFlow(prefs.getString("api_key_Gemini", "") ?: "")
    val geminiKey: StateFlow<String> = _geminiKey.asStateFlow()

    private val _claudeKey = MutableStateFlow(prefs.getString("api_key_Claude", "") ?: "")
    val claudeKey: StateFlow<String> = _claudeKey.asStateFlow()

    private val _groqKey = MutableStateFlow(prefs.getString("api_key_Groq", "") ?: "")
    val groqKey: StateFlow<String> = _groqKey.asStateFlow()

    private val _grokKey = MutableStateFlow(prefs.getString("api_key_Grok", "") ?: "")
    val grokKey: StateFlow<String> = _grokKey.asStateFlow()

    fun saveSettings(provider: String, gemini: String, claude: String, groq: String, grok: String) {
        prefs.edit().apply {
            putString("active_provider", provider)
            putString("api_key_Gemini", gemini)
            putString("api_key_Claude", claude)
            putString("api_key_Groq", groq)
            putString("api_key_Grok", grok)
        }.apply()

        _activeProvider.value = provider
        _geminiKey.value = gemini
        _claudeKey.value = claude
        _groqKey.value = groq
        _grokKey.value = grok
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
}
