package com.banglaenglish.speaker.ui.screen.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.data.TtsManager
import com.banglaenglish.speaker.data.UserPreferencesRepository
import com.banglaenglish.speaker.data.local.AppDatabase
import com.banglaenglish.speaker.data.local.ConversationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val searchQuery: String = "",
    val selectedTab: Int = 0, // 0 = All History, 1 = Bookmarks
    val items: List<ConversationItem> = emptyList(),
    val showClearDialog: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    private val db = AppDatabase.getInstance(application)
    private val conversationDao = db.conversationDao()
    private val userPrefsRepo = UserPreferencesRepository(application)
    private val tts = TtsManager(application) { }

    init {
        viewModelScope.launch {
            _state.flatMapLatest { s ->
                when {
                    s.searchQuery.isNotBlank() -> conversationDao.searchFlow(s.searchQuery)
                    s.selectedTab == 1 -> conversationDao.getBookmarkedFlow()
                    else -> conversationDao.getAllFlow()
                }
            }.collect { entities ->
                val domainItems = entities.map { it.toDomainModel() }
                _state.update { it.copy(items = domainItems) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun setSelectedTab(tabIndex: Int) {
        _state.update { it.copy(selectedTab = tabIndex) }
    }

    fun toggleBookmark(item: ConversationItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(isBookmarked = !item.isBookmarked)
            conversationDao.update(ConversationEntity.fromDomainModel(updated))
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationDao.deleteById(id)
        }
    }

    fun showClearConfirmation(show: Boolean) {
        _state.update { it.copy(showClearDialog = show) }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            conversationDao.deleteAll()
            _state.update { it.copy(showClearDialog = false) }
        }
    }

    fun playItem(item: ConversationItem) {
        val prefs = userPrefsRepo.userPreferences.value
        tts.speak(item.getActiveEnglishText(), prefs.speechSpeed, prefs.accent)
    }

    override fun onCleared() {
        tts.shutdown()
        super.onCleared()
    }
}
