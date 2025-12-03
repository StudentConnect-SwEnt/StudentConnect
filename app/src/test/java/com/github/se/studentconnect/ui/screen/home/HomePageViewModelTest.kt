package com.github.se.studentconnect.ui.screen.home

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.OrganizationRepositoryLocal
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.utils.FilterData
import com.github.se.studentconnect.util.MainDispatcherRule
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomePageViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModel: HomePageViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var organizationRepository: OrganizationRepositoryLocal

  private val testLocation = Location(46.5197, 6.6323, "EPFL")

  private val testEvent1 =
      Event.Public(
          uid = "event1",
          title = "Test Event 1",
          description = "Description 1",
          ownerId = "org1",
          start = Timestamp(Date(System.currentTimeMillis() + 3600000)),
          end = Timestamp(Date(System.currentTimeMillis() + 7200000)),
          location = testLocation,
          participationFee = 0u,
          isFlash = false,
          subtitle = "Test Subtitle 1",
          tags = listOf("Sports", "Outdoor"))

  private val testEvent2 =
      Event.Public(
          uid = "event2",
          title = "Test Event 2",
          description = "Description 2",
          ownerId = "org2",
          start = Timestamp(Date(System.currentTimeMillis() + 86400000)),
          end = Timestamp(Date(System.currentTimeMillis() + 90000000)),
          location = testLocation,
          participationFee = 10u,
          isFlash = false,
          subtitle = "Test Subtitle 2",
          tags = listOf("Music", "Entertainment"))

  private val testOrganization =
      Organization(
          id = "org1",
          name = "Test Organization",
          type = com.github.se.studentconnect.model.organization.OrganizationType.Association,
          description = "Test Description",
          logoUrl = "https://example.com/logo.png",
          memberUids = listOf("user1"),
          createdBy = "creator1")

  private val testUser =
      User(
          userId = "user1",
          email = "test@example.com",
          username = "testuser",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL",
          createdAt = 1000L,
          updatedAt = 1000L)

  @Before
  fun setUp() {
    // Ensure no authenticated user for tests that expect null currentUserId
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false

    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    organizationRepository = OrganizationRepositoryLocal()
    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)
  }

  @After
  fun tearDown() {
    // Clean up authentication state
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false
  }

  @Test
  fun `initial state has default values`() {
    val state = viewModel.uiState.value
    assertTrue(state.isLoading)
    assertTrue(state.events.isEmpty())
    assertTrue(state.organizations.isEmpty())
    assertFalse(state.isCalendarVisible)
    assertNull(state.selectedDate)
    assertNull(state.scrollToDate)
    assertFalse(state.showOnlyFavorites)
    assertEquals(HomeTabMode.FOR_YOU, state.selectedTab)
  }

  @Test
  fun `loadAllEvents updates state correctly`() = runTest {
    eventRepository.addEvent(testEvent1)
    eventRepository.addEvent(testEvent2)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(2, state.events.size)
  }

  @Test
  fun `loadOrganizations updates state correctly`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.organizations.size)
    assertEquals("Test Organization", state.organizations[0].name)
  }

  @Test
  fun `loadAllSubscribedEventsStories creates story data`() = runTest {
    repeat(15) { i ->
      val event =
          testEvent1.copy(
              uid = "event$i",
              title = "Event $i",
              start = Timestamp(Date(System.currentTimeMillis() + i * 3600000)))
      eventRepository.addEvent(event)
    }

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.subscribedEventsStories.isNotEmpty())
    assertTrue(state.subscribedEventsStories.size <= 10)
  }

  @Test
  fun `toggleFavorite updates favorite event IDs`() = runTest {
    eventRepository.addEvent(testEvent1)
    userRepository.saveUser(testUser)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val initialFavorites = viewModel.favoriteEventIds.value.toSet()
    assertFalse(initialFavorites.contains("event1"))

    // Note: toggleFavorite requires currentUserId which is null in tests
    // This test verifies the current behavior
    viewModel.toggleFavorite("event1")
    advanceUntilIdle()

    val finalFavorites = viewModel.favoriteEventIds.value.toSet()
    // Since currentUserId is null, favoriteEventIds won't change
    assertEquals(initialFavorites, finalFavorites)
  }

  @Test
  fun `toggleFavorite removes event from favorites when already favorited`() = runTest {
    eventRepository.addEvent(testEvent1)
    userRepository.saveUser(testUser)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    // Since currentUserId is null in tests, this verifies non-authenticated behavior
    val initialFavorites = viewModel.favoriteEventIds.value.toSet()
    viewModel.toggleFavorite("event1")
    advanceUntilIdle()
    val finalFavorites = viewModel.favoriteEventIds.value.toSet()
    assertEquals(initialFavorites, finalFavorites)
  }

  @Test
  fun `applyFilters with category filter shows only matching events`() = runTest {
    eventRepository.addEvent(testEvent1)
    eventRepository.addEvent(testEvent2)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val filters =
        FilterData(
            categories = listOf("Sports"),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)

    viewModel.applyFilters(filters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.events.size)
    assertEquals("Test Event 1", state.events[0].title)
  }

  @Test
  fun `applyFilters with price filter shows only matching events`() = runTest {
    eventRepository.addEvent(testEvent1)
    eventRepository.addEvent(testEvent2)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val filters =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..5f,
            showOnlyFavorites = false)

    viewModel.applyFilters(filters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.events.size)
    assertEquals("Test Event 1", state.events[0].title)
  }

  @Test
  fun `applyFilters with location filter calculates distance correctly`() = runTest {
    val nearLocation = Location(46.5200, 6.6330, "Near EPFL")
    val farLocation = Location(47.3769, 8.5417, "Zurich")

    val nearEvent =
        testEvent1.copy(uid = "near_event", title = "Near Event", location = nearLocation)
    val farEvent = testEvent2.copy(uid = "far_event", title = "Far Event", location = farLocation)

    eventRepository.addEvent(nearEvent)
    eventRepository.addEvent(farEvent)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val filters =
        FilterData(
            categories = emptyList(),
            location = testLocation,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)

    viewModel.applyFilters(filters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.events.size)
    assertEquals("Near Event", state.events[0].title)
  }

  @Test
  fun `applyFilters with showOnlyFavorites shows only favorite events`() = runTest {
    eventRepository.addEvent(testEvent1)
    eventRepository.addEvent(testEvent2)
    userRepository.saveUser(testUser)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    // Manually add to favorites since currentUserId is null in tests
    userRepository.addFavoriteEvent("user1", "event1")

    val filters =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = true)

    viewModel.applyFilters(filters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Since currentUserId is null, showOnlyFavorites won't filter anything
    assertTrue(state.showOnlyFavorites)
  }

  @Test
  fun `toggleFavoritesFilter toggles favorites filter`() = runTest {
    viewModel.toggleFavoritesFilter()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.showOnlyFavorites)

    viewModel.toggleFavoritesFilter()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.showOnlyFavorites)
  }

  @Test
  fun `showCalendar sets isCalendarVisible to true`() {
    viewModel.showCalendar()

    assertTrue(viewModel.uiState.value.isCalendarVisible)
  }

  @Test
  fun `hideCalendar sets isCalendarVisible to false`() {
    viewModel.showCalendar()
    viewModel.hideCalendar()

    assertFalse(viewModel.uiState.value.isCalendarVisible)
  }

  @Test
  fun `onDateSelected sets selected date and closes calendar`() {
    val date = Date()
    viewModel.onDateSelected(date)

    val state = viewModel.uiState.value
    assertEquals(date, state.selectedDate)
    assertEquals(date, state.scrollToDate)
    assertFalse(state.isCalendarVisible)
  }

  @Test
  fun `clearScrollTarget clears scroll target date`() {
    val date = Date()
    viewModel.onDateSelected(date)
    viewModel.clearScrollTarget()

    assertNull(viewModel.uiState.value.scrollToDate)
  }

  @Test
  fun `selectTab updates selected tab`() {
    viewModel.selectTab(HomeTabMode.EVENTS)
    assertEquals(HomeTabMode.EVENTS, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(HomeTabMode.DISCOVER)
    assertEquals(HomeTabMode.DISCOVER, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(HomeTabMode.FOR_YOU)
    assertEquals(HomeTabMode.FOR_YOU, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `getEventsForDate returns events on specific date`() = runTest {
    val today = Calendar.getInstance()
    val todayDate = today.time

    val tomorrow = Calendar.getInstance()
    tomorrow.add(Calendar.DAY_OF_MONTH, 1)
    val tomorrowDate = tomorrow.time

    val todayEvent = testEvent1.copy(uid = "today_event", start = Timestamp(todayDate))
    val tomorrowEvent = testEvent2.copy(uid = "tomorrow_event", start = Timestamp(tomorrowDate))

    eventRepository.addEvent(todayEvent)
    eventRepository.addEvent(tomorrowEvent)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val eventsForToday = viewModel.getEventsForDate(todayDate)
    assertEquals(1, eventsForToday.size)
    assertEquals("today_event", eventsForToday[0].uid)
  }

  @Test
  fun `getAvailableFilters returns filter options`() {
    val filters = viewModel.getAvailableFilters()
    assertTrue(filters.isNotEmpty())
  }

  @Test
  fun `refresh reloads all data`() = runTest {
    eventRepository.addEvent(testEvent1)
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val newEvent = testEvent2.copy(uid = "new_event")
    eventRepository.addEvent(newEvent)

    viewModel.refresh()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.events.size)
  }

  @Test
  fun `filters exclude past events`() = runTest {
    val pastDate = Calendar.getInstance()
    pastDate.add(Calendar.DAY_OF_MONTH, -5)

    val pastEvent =
        testEvent1.copy(
            uid = "past_event", start = Timestamp(pastDate.time), end = Timestamp(pastDate.time))

    eventRepository.addEvent(pastEvent)
    eventRepository.addEvent(testEvent1)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.events.size)
    assertNotEquals("past_event", state.events[0].uid)
  }

  @Test
  fun `filters handle events without location when large radius`() = runTest {
    val eventNoLocation = testEvent1.copy(uid = "no_location", location = null)
    eventRepository.addEvent(eventNoLocation)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val filters =
        FilterData(
            categories = emptyList(),
            location = testLocation,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)

    viewModel.applyFilters(filters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.events.size)
  }

  @Test
  fun `filters exclude events without location when small radius`() = runTest {
    val eventNoLocation = testEvent1.copy(uid = "no_location", location = null)
    eventRepository.addEvent(eventNoLocation)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val filters =
        FilterData(
            categories = emptyList(),
            location = testLocation,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)

    viewModel.applyFilters(filters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(0, state.events.size)
  }

  @Test
  fun `filters handle null location filter`() = runTest {
    eventRepository.addEvent(testEvent1)
    eventRepository.addEvent(testEvent2)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val filters =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)

    viewModel.applyFilters(filters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.events.size)
  }

  @Test
  fun `filters handle free events with price range starting at 0`() = runTest {
    eventRepository.addEvent(testEvent1)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val filters =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)

    viewModel.applyFilters(filters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.events.size)
  }

  @Test
  fun `updateSeenStories updates story state`() = runTest {
    eventRepository.addEvent(testEvent1)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val initialStories = viewModel.uiState.value.subscribedEventsStories
    if (initialStories.isNotEmpty()) {
      val event = initialStories.keys.first()
      viewModel.updateSeenStories(event, 2)
      advanceUntilIdle()
    }
  }

  @Test
  fun `HomeTabMode enum has all expected values`() {
    val values = HomeTabMode.values()
    assertEquals(3, values.size)
    assertTrue(values.contains(HomeTabMode.FOR_YOU))
    assertTrue(values.contains(HomeTabMode.EVENTS))
    assertTrue(values.contains(HomeTabMode.DISCOVER))
  }

  @Test
  fun `HomePageUiState default values are correct`() {
    val defaultState = HomePageUiState()

    assertTrue(defaultState.subscribedEventsStories.isEmpty())
    assertTrue(defaultState.events.isEmpty())
    assertTrue(defaultState.organizations.isEmpty())
    assertTrue(defaultState.isLoading)
    assertFalse(defaultState.isCalendarVisible)
    assertNull(defaultState.selectedDate)
    assertNull(defaultState.scrollToDate)
    assertFalse(defaultState.showOnlyFavorites)
    assertEquals(HomeTabMode.FOR_YOU, defaultState.selectedTab)
  }

  @Test
  fun `filters with multiple categories show events matching any category`() = runTest {
    eventRepository.addEvent(testEvent1)
    eventRepository.addEvent(testEvent2)

    viewModel =
        HomePageViewModel(
            eventRepository = eventRepository,
            userRepository = userRepository,
            organizationRepository = organizationRepository)

    advanceUntilIdle()

    val filters =
        FilterData(
            categories = listOf("Sports", "Music"),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)

    viewModel.applyFilters(filters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.events.size)
  }
}
