package com.github.se.studentconnect.ui.eventcreation

import com.github.se.studentconnect.model.event.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class EventTemplateSelectionViewModelTest {

  private lateinit var viewModel: EventTemplateSelectionViewModel
  private lateinit var mockEventRepository: EventRepository
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockEventRepository = mock(EventRepository::class.java)
    viewModel = EventTemplateSelectionViewModel(mockEventRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state has empty events list`() {
    val state = viewModel.uiState.value
    assertTrue(state.events.isEmpty())
  }

  @Test
  fun `initial state is not loading`() {
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
  }

  @Test
  fun `initial state has no error message`() {
    val state = viewModel.uiState.value
    assertNull(state.errorMessage)
  }

  @Test
  fun `uiState is accessible`() {
    val uiState = viewModel.uiState
    assertEquals(EventTemplateSelectionUiState(), uiState.value)
  }
}
