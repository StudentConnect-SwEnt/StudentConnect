package com.github.se.studentconnect.viewmodel

import android.content.Context
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.repository.LocationRepository
import com.github.se.studentconnect.repository.LocationResult
import com.github.se.studentconnect.repository.OrganizationRepository
import com.github.se.studentconnect.ui.screen.home.HomePageViewModel
import com.github.se.studentconnect.ui.screen.home.HomeTabMode
import com.github.se.studentconnect.ui.screen.home.PreferredTimeOfDay
import com.github.se.studentconnect.ui.screen.home.UserPreferences
import com.github.se.studentconnect.ui.utils.FilterData
import com.google.firebase.Timestamp
import java.util.Calendar
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class HomePageViewModelScoringTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: HomePageViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var mockOrganizationRepository: OrganizationRepository
  private lateinit var mockContext: Context
  private lateinit var mockLocationRepository: LocationRepository

  // Helper to create timestamps at specific times
  private fun createTimestamp(daysFromNow: Int = 1, hourOfDay: Int = 10): Timestamp {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysFromNow)
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return Timestamp(calendar.time)
  }

  // Helper to create test events with default parameters
  private fun createTestEvent(
      uid: String,
      title: String,
      daysFromNow: Int = 1,
      hourOfDay: Int = 10,
      subtitle: String = "Subtitle",
      description: String = "Description",
      location: Location? = Location(46.5, 6.6, "EPFL"),
      website: String = "https://example.com",
      isFlash: Boolean = false,
      ownerId: String = "owner1",
      tags: List<String> = emptyList(),
      participationFee: UInt? = null
  ): Event.Public {
    return Event.Public(
        uid = uid,
        title = title,
        subtitle = subtitle,
        description = description,
        start = createTimestamp(daysFromNow, hourOfDay),
        end = createTimestamp(daysFromNow, hourOfDay + 2),
        location = location,
        website = website,
        isFlash = isFlash,
        ownerId = ownerId,
        tags = tags,
        participationFee = participationFee)
  }

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    mockOrganizationRepository = mock(OrganizationRepository::class.java)
    mockContext = mock(Context::class.java)
    mockLocationRepository = mock(LocationRepository::class.java)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ============ PreferredTimeOfDay enum tests ============

  @Test
  fun preferredTimeOfDay_hasAllValues() {
    val values = PreferredTimeOfDay.entries
    assertEquals(4, values.size)
    assertTrue(values.contains(PreferredTimeOfDay.MORNING))
    assertTrue(values.contains(PreferredTimeOfDay.AFTERNOON))
    assertTrue(values.contains(PreferredTimeOfDay.EVENING))
    assertTrue(values.contains(PreferredTimeOfDay.ANY))
  }

  // ============ UserPreferences data class tests ============

  @Test
  fun userPreferences_defaultValues() {
    val preferences = UserPreferences()
    assertEquals(null, preferences.preferredLocation)
    assertEquals(0f..100f, preferences.preferredPriceRange)
    assertEquals(PreferredTimeOfDay.ANY, preferences.preferredTimeOfDay)
  }

  // ============ Load User Hobbies tests ============

  @Test
  fun loadUserHobbies_withValidUser_loadsHobbies() = runTest {
    // Arrange
    val userId = "test-user-1"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            username = "johndoe",
            university = "EPFL",
            hobbies = listOf("Sports", "Music", "Reading")))

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(3, uiState.userHobbies.size)
    assertTrue(uiState.userHobbies.contains("Sports"))
    assertTrue(uiState.userHobbies.contains("Music"))
    assertTrue(uiState.userHobbies.contains("Reading"))
  }

  @Test
  fun loadUserHobbies_withNullUser_loadsEmptyList() = runTest {
    // Arrange
    val userId = "non-existent-user"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.userHobbies.isEmpty())
  }

  // ============ Load User Attended Events tests ============

  @Test
  fun loadUserAttendedEvents_withEvents_loadsAttendedEvents() = runTest {
    // Arrange
    val userId = "test-user-3"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Sports Event",
            subtitle = "Fun",
            description = "Sports",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            tags = listOf("Sports"))

    eventRepository.addEvent(event1)
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            username = "johndoe",
            university = "EPFL"))
    userRepository.addEventToUser("event-1", userId)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.attendedEvents.size)
    assertEquals("event-1", uiState.attendedEvents[0].uid)
  }

  @Test
  fun loadUserAttendedEvents_withNoEvents_loadsEmptyList() = runTest {
    // Arrange
    val userId = "test-user-4"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            username = "janesmith",
            university = "EPFL"))

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.attendedEvents.isEmpty())
  }

  // ============ Load User Preferences tests ============

  @Test
  fun loadUserPreferences_withLocation_loadsLocation() = runTest {
    // Arrange
    val userId = "test-user-5"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val androidLocation = mock(android.location.Location::class.java)
    `when`(androidLocation.latitude).thenReturn(46.5197)
    `when`(androidLocation.longitude).thenReturn(6.6323)
    `when`(mockLocationRepository.getCurrentLocation())
        .thenReturn(LocationResult.Success(androidLocation))

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull(uiState.userPreferences.preferredLocation)
    assertEquals(46.5197, uiState.userPreferences.preferredLocation?.latitude)
    assertEquals(6.6323, uiState.userPreferences.preferredLocation?.longitude)
  }

  @Test
  fun loadUserPreferences_withoutLocation_loadsDefaults() = runTest {
    // Arrange
    val userId = "test-user-6"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId
    `when`(mockLocationRepository.getCurrentLocation())
        .thenReturn(LocationResult.Error("No location"))

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(null, uiState.userPreferences.preferredLocation)
    assertEquals(0f..100f, uiState.userPreferences.preferredPriceRange)
    assertEquals(PreferredTimeOfDay.ANY, uiState.userPreferences.preferredTimeOfDay)
  }

  // ============ Tab filtering tests ============

  @Test
  fun applyFilters_forYouTab_showsAllEvents() = runTest {
    // Arrange
    val userId = "test-user-8"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            username = "testuser",
            university = "EPFL",
            hobbies = listOf("Sports")))

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Sports Event",
            subtitle = "Fun",
            description = "Sports",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            tags = listOf("Sports"))

    val event2 =
        Event.Public(
            uid = "event-2",
            title = "Music Event",
            subtitle = "Concert",
            description = "Music",
            start = createTimestamp(2, 14),
            end = createTimestamp(2, 16),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event2.com",
            isFlash = false,
            ownerId = "owner2",
            tags = listOf("Music"))

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()
    viewModel.selectTab(HomeTabMode.FOR_YOU)
    advanceUntilIdle()

    // Assert - FOR_YOU shows all events (sorted by score)
    val uiState = viewModel.uiState.value
    assertEquals(2, uiState.events.size)
  }

  @Test
  fun applyFilters_discoverTab_filtersOutMatchingHobbies() = runTest {
    // Arrange
    val userId = "test-user-9"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            username = "testuser",
            university = "EPFL",
            hobbies = listOf("Sports")))

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Sports Event",
            subtitle = "Fun",
            description = "Sports",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            tags = listOf("Sports"))

    val event2 =
        Event.Public(
            uid = "event-2",
            title = "Music Event",
            subtitle = "Concert",
            description = "Music",
            start = createTimestamp(2, 14),
            end = createTimestamp(2, 16),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event2.com",
            isFlash = false,
            ownerId = "owner2",
            tags = listOf("Music"))

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()
    viewModel.selectTab(HomeTabMode.DISCOVER)
    advanceUntilIdle()

    // Assert - DISCOVER shows only events that don't match user hobbies
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
    assertEquals("event-2", uiState.events[0].uid) // Music event, not matching Sports hobby
  }

  @Test
  fun applyFilters_eventsTab_showsAllEvents() = runTest {
    // Arrange
    val userId = "test-user-11"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Event 1",
            subtitle = "Subtitle",
            description = "Description",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1")

    eventRepository.addEvent(event1)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()
    viewModel.selectTab(HomeTabMode.EVENTS)
    advanceUntilIdle()

    // Assert - EVENTS shows all events
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
  }

  // ============ Event scoring tests - Tag Similarity ============

  @Test
  fun calculateEventScore_tagSimilarity_withMatchingTags_highScore() = runTest {
    // Arrange
    val userId = "test-user-12"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            username = "testuser",
            university = "EPFL",
            hobbies = listOf("Sports", "Music")))

    val event =
        Event.Public(
            uid = "event-1",
            title = "Sports & Music Festival",
            subtitle = "Fun",
            description = "Sports and Music",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            tags = listOf("Sports", "Music"))

    eventRepository.addEvent(event)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()
    viewModel.selectTab(HomeTabMode.FOR_YOU)
    advanceUntilIdle()

    // Assert - Event with matching tags should appear
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
    assertEquals("event-1", uiState.events[0].uid)
  }

  @Test
  fun calculateEventScore_tagSimilarity_withNoMatchingTags_lowerScore() = runTest {
    // Arrange
    val userId = "test-user-13"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            username = "testuser",
            university = "EPFL",
            hobbies = listOf("Sports")))

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Sports Event",
            subtitle = "Fun",
            description = "Sports",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            tags = listOf("Sports"))

    val event2 =
        Event.Public(
            uid = "event-2",
            title = "Cooking Event",
            subtitle = "Food",
            description = "Cooking",
            start = createTimestamp(2, 14),
            end = createTimestamp(2, 16),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event2.com",
            isFlash = false,
            ownerId = "owner2",
            tags = listOf("Cooking"))

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()
    viewModel.selectTab(HomeTabMode.FOR_YOU)
    advanceUntilIdle()

    // Assert - Sports event should be ranked higher due to matching tag
    val uiState = viewModel.uiState.value
    assertEquals(2, uiState.events.size)
    assertEquals("event-1", uiState.events[0].uid) // Sports event first
  }

  // ============ Event scoring tests - Distance Score ============

  @Test
  fun calculateEventScore_distanceScore_withNoLocation_defaultScore() = runTest {
    // Arrange
    val userId = "test-user-17"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId
    `when`(mockLocationRepository.getCurrentLocation())
        .thenReturn(LocationResult.Error("No location"))

    val event =
        Event.Public(
            uid = "event-1",
            title = "Event",
            subtitle = "Subtitle",
            description = "Description",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1")

    eventRepository.addEvent(event)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
  }

  // ============ Event scoring tests - Price Preference ============

  @Test
  fun calculateEventScore_pricePreference_freeEvent_highScore() = runTest {
    // Arrange
    val userId = "test-user-19"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event =
        Event.Public(
            uid = "event-1",
            title = "Free Event",
            subtitle = "No cost",
            description = "Free",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            participationFee = null)

    eventRepository.addEvent(event)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
  }

  @Test
  fun calculateEventScore_pricePreference_inRangeEvent_highScore() = runTest {
    // Arrange
    val userId = "test-user-20"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event =
        Event.Public(
            uid = "event-1",
            title = "Affordable Event",
            subtitle = "In budget",
            description = "Affordable",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            participationFee = 50u) // Within 0-100 range

    eventRepository.addEvent(event)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
  }

  // ============ Event scoring tests - Time Match ============

  @Test
  fun calculateEventScore_timeMatch_morningEvent() = runTest {
    // Arrange
    val userId = "test-user-23"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event =
        Event.Public(
            uid = "event-1",
            title = "Morning Event",
            subtitle = "Early",
            description = "Morning",
            start = createTimestamp(1, 8), // 8 AM - morning
            end = createTimestamp(1, 10),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1")

    eventRepository.addEvent(event)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
  }

  // ============ Event scoring tests - Recency Boost ============

  @Test
  fun calculateEventScore_recencyBoost_soonEvent_highBoost() = runTest {
    // Arrange
    val userId = "test-user-26"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Soon Event",
            subtitle = "Tomorrow",
            description = "Soon",
            start = createTimestamp(1, 10), // 1 day away
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1")

    val event2 =
        Event.Public(
            uid = "event-2",
            title = "Later Event",
            subtitle = "Far away",
            description = "Later",
            start = createTimestamp(40, 10), // 40 days away
            end = createTimestamp(40, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event2.com",
            isFlash = false,
            ownerId = "owner2")

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()
    viewModel.selectTab(HomeTabMode.FOR_YOU)
    advanceUntilIdle()

    // Assert - Soon event should be ranked higher
    val uiState = viewModel.uiState.value
    assertEquals(2, uiState.events.size)
    assertEquals("event-1", uiState.events[0].uid) // Soon event first
  }

  // ============ Event scoring tests - Attended Event Similarity ============

  @Test
  fun calculateEventScore_attendedSimilarity_withMatchingTags() = runTest {
    // Arrange
    val userId = "test-user-28"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val attendedEvent =
        Event.Public(
            uid = "attended-1",
            title = "Past Sports Event",
            subtitle = "Past",
            description = "Sports",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://attended.com",
            isFlash = false,
            ownerId = "owner1",
            tags = listOf("Sports"))

    val newEvent =
        Event.Public(
            uid = "event-1",
            title = "New Sports Event",
            subtitle = "New",
            description = "Sports",
            start = createTimestamp(2, 10),
            end = createTimestamp(2, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            tags = listOf("Sports"))

    eventRepository.addEvent(attendedEvent)
    eventRepository.addEvent(newEvent)
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            username = "testuser",
            university = "EPFL"))
    userRepository.addEventToUser("attended-1", userId)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.events.size >= 1)
  }

  @Test
  fun calculateEventScore_attendedSimilarity_withNoAttendedEvents_defaultScore() = runTest {
    // Arrange
    val userId = "test-user-29"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            username = "testuser",
            university = "EPFL"))

    val event =
        Event.Public(
            uid = "event-1",
            title = "Event",
            subtitle = "Subtitle",
            description = "Description",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            tags = listOf("Sports"))

    eventRepository.addEvent(event)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
  }

  // ============ getEventsForDate tests ============

  @Test
  fun getEventsForDate_withMatchingEvents_returnsEvents() = runTest {
    // Arrange
    val userId = "test-user-31"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 5)
    val targetDate = calendar.time

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Event on target date",
            subtitle = "Subtitle",
            description = "Description",
            start = Timestamp(targetDate),
            end = Timestamp(targetDate),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1")

    val event2 =
        Event.Public(
            uid = "event-2",
            title = "Event on different date",
            subtitle = "Subtitle",
            description = "Description",
            start = createTimestamp(10, 10),
            end = createTimestamp(10, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event2.com",
            isFlash = false,
            ownerId = "owner2")

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    val eventsOnDate = viewModel.getEventsForDate(targetDate)

    // Assert
    assertEquals(1, eventsOnDate.size)
    assertEquals("event-1", eventsOnDate[0].uid)
  }

  @Test
  fun getEventsForDate_withNoMatchingEvents_returnsEmpty() = runTest {
    // Arrange
    val userId = "test-user-32"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event =
        Event.Public(
            uid = "event-1",
            title = "Event",
            subtitle = "Subtitle",
            description = "Description",
            start = createTimestamp(5, 10),
            end = createTimestamp(5, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1")

    eventRepository.addEvent(event)

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 10)
    val targetDate = calendar.time

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    val eventsOnDate = viewModel.getEventsForDate(targetDate)

    // Assert
    assertTrue(eventsOnDate.isEmpty())
  }

  // ============ Additional filtering coverage tests ============

  @Test
  fun applyFilters_withTagFilter_filtersCorrectly() = runTest {
    // Arrange
    val userId = "test-user-34"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Sports Event",
            subtitle = "Fun",
            description = "Sports",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            tags = listOf("Sports"))

    val event2 =
        Event.Public(
            uid = "event-2",
            title = "Music Event",
            subtitle = "Concert",
            description = "Music",
            start = createTimestamp(2, 14),
            end = createTimestamp(2, 16),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event2.com",
            isFlash = false,
            ownerId = "owner2",
            tags = listOf("Music"))

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    val filterData =
        FilterData(
            categories = listOf("Sports"),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..200f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Only sports event should be visible
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
    assertEquals("event-1", uiState.events[0].uid)
  }

  @Test
  fun applyFilters_withPriceFilter_filtersCorrectly() = runTest {
    // Arrange
    val userId = "test-user-35"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Free Event",
            subtitle = "Free",
            description = "Free",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1",
            participationFee = null)

    val event2 =
        Event.Public(
            uid = "event-2",
            title = "Expensive Event",
            subtitle = "Costly",
            description = "Expensive",
            start = createTimestamp(2, 14),
            end = createTimestamp(2, 16),
            location = Location(46.5, 6.6, "EPFL"),
            website = "https://event2.com",
            isFlash = false,
            ownerId = "owner2",
            participationFee = 150u)

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..50f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Only free event should be visible
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
    assertEquals("event-1", uiState.events[0].uid)
  }

  @Test
  fun applyFilters_withLocationFilter_filtersCorrectly() = runTest {
    // Arrange
    val userId = "test-user-36"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event1 =
        Event.Public(
            uid = "event-1",
            title = "Nearby Event",
            subtitle = "Close",
            description = "Near",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = Location(46.5197, 6.6323, "EPFL"),
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1")

    val event2 =
        Event.Public(
            uid = "event-2",
            title = "Far Event",
            subtitle = "Far",
            description = "Far",
            start = createTimestamp(2, 14),
            end = createTimestamp(2, 16),
            location = Location(0.0, 0.0, "Far away"),
            website = "https://event2.com",
            isFlash = false,
            ownerId = "owner2")

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    val filterData =
        FilterData(
            categories = emptyList(),
            location = Location(46.5197, 6.6323, "EPFL"),
            radiusKm = 10f,
            priceRange = 0f..200f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Only nearby event should be visible
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
    assertEquals("event-1", uiState.events[0].uid)
  }

  @Test
  fun applyFilters_withNoEventLocation_smallRadiusFilter() = runTest {
    // Arrange
    val userId = "test-user-38"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    val event =
        Event.Public(
            uid = "event-1",
            title = "Event no location",
            subtitle = "No loc",
            description = "No location",
            start = createTimestamp(1, 10),
            end = createTimestamp(1, 12),
            location = null,
            website = "https://event1.com",
            isFlash = false,
            ownerId = "owner1")

    eventRepository.addEvent(event)

    // Act
    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    val filterData =
        FilterData(
            categories = emptyList(),
            location = Location(46.5197, 6.6323, "EPFL"),
            radiusKm = 5f,
            priceRange = 0f..200f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Event with no location should NOT be visible with small radius
    val uiState = viewModel.uiState.value
    assertEquals(0, uiState.events.size)
  }

  @Test
  fun refresh_reloadsAllData() = runTest {
    // Arrange
    val userId = "test-user-40"
    com.github.se.studentconnect.repository.AuthenticationProvider.testUserId = userId

    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            mockContext,
            mockLocationRepository,
            mockOrganizationRepository)
    advanceUntilIdle()

    // Add data after initialization
    userRepository.saveUser(
        User(
            userId = userId,
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            username = "testuser",
            university = "EPFL",
            hobbies = listOf("Sports")))

    // Act
    viewModel.refresh()
    advanceUntilIdle()

    // Assert - Hobbies should be loaded
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.userHobbies.size)
  }
}
