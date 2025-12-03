package com.github.se.studentconnect.ui.screen.camera

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraModeSelectorViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repository: StoryRepository
  private var originalUserId: String? = null

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repository = mockk()
    originalUserId = AuthenticationProvider.testUserId
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    AuthenticationProvider.testUserId = originalUserId
  }

  // Covers: userId!=null, try success (lines 26-31)
  @Test
  fun loadJoinedEvents_success() =
      runTest(testDispatcher) {
        val events =
            listOf(
                Event.Public(
                    uid = "1",
                    ownerId = "o",
                    title = "E",
                    description = "d",
                    start = Timestamp.now(),
                    isFlash = false,
                    subtitle = "s"))
        AuthenticationProvider.testUserId = "user1"
        coEvery { repository.getUserJoinedEvents("user1") } returns events

        val vm = CameraModeSelectorViewModel(repository)
        vm.loadJoinedEvents()
        advanceUntilIdle()

        assertTrue(vm.eventSelectionState.value is EventSelectionState.Success)
        assertEquals(events, (vm.eventSelectionState.value as EventSelectionState.Success).events)
      }

  // Covers: userId!=null, catch exception (lines 32-34)
  @Test
  fun loadJoinedEvents_error() =
      runTest(testDispatcher) {
        AuthenticationProvider.testUserId = "user1"
        coEvery { repository.getUserJoinedEvents("user1") } throws RuntimeException("fail")

        val vm = CameraModeSelectorViewModel(repository)
        vm.loadJoinedEvents()
        advanceUntilIdle()

        assertTrue(vm.eventSelectionState.value is EventSelectionState.Error)
        assertEquals("fail", (vm.eventSelectionState.value as EventSelectionState.Error).error)
      }

  // Covers: userId==null (lines 36-38)
  @Test
  fun loadJoinedEvents_noUser_returnsEmpty() =
      runTest(testDispatcher) {
        AuthenticationProvider.testUserId = ""

        val vm = CameraModeSelectorViewModel(repository)
        vm.loadJoinedEvents()
        advanceUntilIdle()

        assertTrue(vm.eventSelectionState.value is EventSelectionState.Success)
        assertTrue((vm.eventSelectionState.value as EventSelectionState.Success).events.isEmpty())
      }
}
