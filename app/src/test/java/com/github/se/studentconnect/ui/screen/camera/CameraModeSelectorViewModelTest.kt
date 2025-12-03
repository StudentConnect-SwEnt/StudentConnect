package com.github.se.studentconnect.ui.screen.camera

import androidx.lifecycle.ViewModel
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.story.StoryRepository
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.ui.components.EventSelectionState
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraModeSelectorViewModelTest {

  private lateinit var mockRepository: StoryRepository
  private var originalTestUserId: String? = null
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    mockRepository = mockk(relaxed = true)
    originalTestUserId = AuthenticationProvider.testUserId
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    AuthenticationProvider.testUserId = originalTestUserId
  }

  @Test
  fun loadJoinedEvents_withUserId_loadsEventsSuccessfully() =
      runTest(testDispatcher) {
        val userId = "user123"
        val events =
            listOf(
                Event.Public(
                    uid = "1",
                    ownerId = "owner1",
                    title = "Event 1",
                    description = "Description",
                    start = Timestamp.now(),
                    isFlash = false,
                    subtitle = "Subtitle"))

        AuthenticationProvider.testUserId = userId
        coEvery { mockRepository.getUserJoinedEvents(userId) } returns events

        val viewModel = CameraModeSelectorViewModel(mockRepository)
        viewModel.loadJoinedEvents()
        advanceUntilIdle()

        val state = viewModel.eventSelectionState.value
        assertTrue(state is EventSelectionState.Success)
        assertEquals(events, (state as EventSelectionState.Success).events)
      }

  @Test
  fun loadJoinedEvents_withUserId_handlesError() =
      runTest(testDispatcher) {
        val userId = "user123"
        val errorMessage = "Network error"

        AuthenticationProvider.testUserId = userId
        coEvery { mockRepository.getUserJoinedEvents(userId) } throws RuntimeException(errorMessage)

        val viewModel = CameraModeSelectorViewModel(mockRepository)
        viewModel.loadJoinedEvents()
        advanceUntilIdle()

        val state = viewModel.eventSelectionState.value
        assertTrue(state is EventSelectionState.Error)
        assertEquals(errorMessage, (state as EventSelectionState.Error).error)
      }

  @Test
  fun loadJoinedEvents_withoutUserId_returnsEmptyList() {
    AuthenticationProvider.testUserId = ""

    val viewModel = CameraModeSelectorViewModel(mockRepository)
    viewModel.loadJoinedEvents()

    val state = viewModel.eventSelectionState.value
    assertTrue(state is EventSelectionState.Success)
    assertTrue((state as EventSelectionState.Success).events.isEmpty())
  }

  @Test
  fun factory_create_withWrongClass_throwsException() {
    val factory = CameraModeSelectorViewModelFactory(mockRepository)

    try {
      @Suppress("UNCHECKED_CAST")
      factory.create(String::class.java as Class<androidx.lifecycle.ViewModel>)
      assertTrue(false) // Should not reach here
    } catch (e: IllegalArgumentException) {
      assertNotNull(e)
    }
  }
}
