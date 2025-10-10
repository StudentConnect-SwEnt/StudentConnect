package com.github.se.studentconnect.ui.activities

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.repository.UserRepository
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ActivitiesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ActivitiesViewModel
    private lateinit var mockEventRepository: EventRepository
    private lateinit var mockUserRepository: UserRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockEventRepository = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)
        viewModel = ActivitiesViewModel(mockEventRepository, mockUserRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRefreshEvents() = runTest {
        val events =
            listOf(
                Event.Public(
                    uid = "1",
                    ownerId = "user1",
                    title = "Event 1",
                    description = "Description 1",
                    start = Timestamp.now(),
                    isFlash = false,
                    subtitle = "Subtitle 1"))
        coEvery { mockEventRepository.getEventsAttendedByUser("user1") } returns events

        viewModel.refreshEvents("user1")
        testDispatcher.scheduler.advanceUntilIdle() // Ensure all coroutines have completed

        val uiState = viewModel.uiState.first()
        assertEquals(events, uiState.events)
    }

    @Test
    fun testLeaveEvent() = runTest {
        coEvery { mockUserRepository.leaveEvent(any(), any()) } returns Unit
        coEvery { mockEventRepository.removeParticipantFromEvent(any(), any()) } returns Unit

        viewModel.leaveEvent("event1")
        testDispatcher.scheduler.advanceUntilIdle() // Ensure all coroutines have completed

        // No direct state change to assert, but we can verify interactions if needed with MockK's
        // coVerify
    }
}