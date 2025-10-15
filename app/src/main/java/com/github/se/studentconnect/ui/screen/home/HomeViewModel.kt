package com.github.se.studentconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomePageUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
)

class HomePageViewModel @Inject constructor(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    // maybe will be used after for recommendations
    private val userRepositoryLocal: UserRepository = UserRepositoryProvider.repository

    ) :
    ViewModel() {

    private val _uiState = MutableStateFlow(HomePageUiState())
    val uiState: StateFlow<HomePageUiState> = _uiState.asStateFlow()

    init {
        loadAllEvents()
    }

    private fun loadAllEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allEvents = eventRepository.getAllVisibleEvents()
            _uiState.update { it.copy(events = allEvents, isLoading = false) }
        }
    }

    fun refresh() {
        loadAllEvents()
    }
}