package com.github.se.studentconnect.viewmodel

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.OrganizationRepositoryLocal
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.screen.home.HomePageViewModel
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomePageViewModelGetEventsForDateTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: HomePageViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var organizationRepository: OrganizationRepositoryLocal

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    organizationRepository = OrganizationRepositoryLocal()
    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun getEventsForDate_returnsOnlyEventsFromThatDay() = runTest {
    // Use future dates to pass temporality filter
    val todayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }

    val eventToday =
        Event.Public(
            uid = "e-today",
            ownerId = "owner",
            title = "Today Event",
            description = "",
            location = Location(0.0, 0.0, "Loc"),
            start = Timestamp(todayCal.time),
            end = Timestamp(todayCal.time),
            isFlash = false,
            subtitle = "")
    val eventTomorrow =
        Event.Public(
            uid = "e-tomorrow",
            ownerId = "owner",
            title = "Tomorrow Event",
            description = "",
            location = Location(0.0, 0.0, "Loc"),
            start = Timestamp(tomorrowCal.time),
            end = Timestamp(tomorrowCal.time),
            isFlash = false,
            subtitle = "")

    eventRepository.addEvent(eventToday)
    eventRepository.addEvent(eventTomorrow)

    // refresh state
    viewModel.refresh()
    advanceUntilIdle()

    val eventsForToday = viewModel.getEventsForDate(todayCal.time)
    assertEquals(1, eventsForToday.size)
    assertEquals("e-today", eventsForToday[0].uid)

    val eventsForTomorrow = viewModel.getEventsForDate(tomorrowCal.time)
    assertEquals(1, eventsForTomorrow.size)
    assertEquals("e-tomorrow", eventsForTomorrow[0].uid)
  }
}
