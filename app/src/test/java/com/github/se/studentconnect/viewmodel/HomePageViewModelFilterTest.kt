package com.github.se.studentconnect.viewmodel

import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.ui.screen.home.HomePageViewModel
import com.github.se.studentconnect.ui.utils.FilterData
import com.google.firebase.Timestamp
import java.util.Date
import junit.framework.TestCase.assertEquals
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

@ExperimentalCoroutinesApi
class HomePageViewModelFilterTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: HomePageViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var organizationRepository: OrganizationRepositoryLocal

  // EPFL Rolex Learning Center location
  private val epflLocation = Location(46.5191, 6.5668, "EPFL")
  // Lausanne center location (about 3km from EPFL)
  private val lausanneCenterLocation = Location(46.5197, 6.6323, "Lausanne Center")
  // Geneva location (about 50km from EPFL)
  private val genevaLocation = Location(46.2044, 6.1432, "Geneva")

  // Create timestamps for future events (1 hour from now)
  private val futureTime = Timestamp(Date(System.currentTimeMillis() + 3600000))

  private val eventAtEPFL =
      Event.Public(
          uid = "event-epfl",
          title = "EPFL Event",
          subtitle = "At EPFL",
          description = "Event at EPFL",
          start = futureTime,
          end = futureTime,
          location = epflLocation,
          website = "https://event.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("Technology", "Science"))

  private val eventInLausanne =
      Event.Public(
          uid = "event-lausanne",
          title = "Lausanne Event",
          subtitle = "In Lausanne",
          description = "Event in Lausanne",
          start = futureTime,
          end = futureTime,
          location = lausanneCenterLocation,
          website = "https://event.com",
          ownerId = "owner2",
          isFlash = false,
          tags = listOf("Culture"))

  private val eventInGeneva =
      Event.Public(
          uid = "event-geneva",
          title = "Geneva Event",
          subtitle = "In Geneva",
          description = "Event in Geneva",
          start = futureTime,
          end = futureTime,
          location = genevaLocation,
          website = "https://event.com",
          ownerId = "owner3",
          isFlash = false,
          tags = listOf("Business"))

  private val eventWithFee =
      Event.Public(
          uid = "event-paid",
          title = "Paid Event",
          subtitle = "Costs money",
          description = "Event with participation fee",
          start = futureTime,
          end = futureTime,
          location = epflLocation,
          website = "https://event.com",
          ownerId = "owner4",
          isFlash = false,
          participationFee = 25u,
          tags = listOf("Workshop"))

  private val eventNoLocation =
      Event.Public(
          uid = "event-no-loc",
          title = "Online Event",
          subtitle = "Virtual",
          description = "Event without location",
          start = futureTime,
          end = futureTime,
          location = null,
          website = "https://event.com",
          ownerId = "owner5",
          isFlash = false,
          tags = listOf("Online"))

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    // Setup AuthenticationProvider with test user for tests that use toggleFavorite
    AuthenticationProvider.testUserId = "test-user-123"
    AuthenticationProvider.local = false

    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    organizationRepository = OrganizationRepositoryLocal()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    // Clean up authentication state
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false
  }

  @Test
  fun applyFilters_withLocationFilter_filtersEventsByDistance() = runTest {
    // Arrange
    eventRepository.addEvent(eventAtEPFL)
    eventRepository.addEvent(eventInLausanne)
    eventRepository.addEvent(eventInGeneva)

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Act - Apply filter for events within 10km of EPFL
    val filterData =
        FilterData(
            categories = emptyList(),
            location = epflLocation,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Should only include events within 10km
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.any { it.uid == "event-epfl" })
    assertTrue(filteredEvents.none { it.uid == "event-geneva" })
  }

  @Test
  fun applyFilters_withNoLocationFilter_returnsAllEvents() = runTest {
    // Arrange
    eventRepository.addEvent(eventAtEPFL)
    eventRepository.addEvent(eventInGeneva)

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Act - Apply filter with no location specified
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Should include all events
    val filteredEvents = viewModel.uiState.value.events
    assertEquals(2, filteredEvents.size)
  }

  @Test
  fun applyFilters_withEventNoLocation_includedIfRadiusIsLarge() = runTest {
    // Arrange
    eventRepository.addEvent(eventNoLocation)
    eventRepository.addEvent(eventAtEPFL)

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Act - Apply filter with large radius (>= 100km)
    val filterData =
        FilterData(
            categories = emptyList(),
            location = epflLocation,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Event without location should be included
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.any { it.uid == "event-no-loc" })
  }

  @Test
  fun applyFilters_withEventNoLocation_excludedIfRadiusIsSmall() = runTest {
    // Arrange
    eventRepository.addEvent(eventNoLocation)
    eventRepository.addEvent(eventAtEPFL)

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Act - Apply filter with small radius (< 100km)
    val filterData =
        FilterData(
            categories = emptyList(),
            location = epflLocation,
            radiusKm = 50f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Event without location should be excluded
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.none { it.uid == "event-no-loc" })
  }

  @Test
  fun applyFilters_withPriceFilter_filtersFreeEvents() = runTest {
    // Arrange
    eventRepository.addEvent(eventAtEPFL) // Free event
    eventRepository.addEvent(eventWithFee) // 25 CHF event

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Act - Apply filter for events starting at 10 CHF
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 100f,
            priceRange = 10f..50f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Only paid event should be included
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.any { it.uid == "event-paid" })
    assertTrue(filteredEvents.none { it.uid == "event-epfl" })
  }

  @Test
  fun applyFilters_withPriceRangeStartingAtZero_includesFreeEvents() = runTest {
    // Arrange
    eventRepository.addEvent(eventAtEPFL) // Free event
    eventRepository.addEvent(eventWithFee) // 25 CHF event

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Act - Apply filter for events from 0 CHF
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..30f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Both events should be included
    val filteredEvents = viewModel.uiState.value.events
    assertEquals(2, filteredEvents.size)
  }

  @Test
  fun applyFilters_withCategoryFilter_filtersEventsByTags() = runTest {
    // Arrange
    eventRepository.addEvent(eventAtEPFL) // Technology, Science
    eventRepository.addEvent(eventInLausanne) // Culture
    eventRepository.addEvent(eventWithFee) // Workshop

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Act - Filter for Technology events
    val filterData =
        FilterData(
            categories = listOf("Technology"),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Only Technology event should be included
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.any { it.uid == "event-epfl" })
    assertTrue(filteredEvents.none { it.uid == "event-lausanne" })
  }

  @Test
  fun applyFilters_withMultipleCategoryFilters_includesEventsMatchingAnyCategory() = runTest {
    // Arrange
    eventRepository.addEvent(eventAtEPFL) // Technology, Science
    eventRepository.addEvent(eventInLausanne) // Culture
    eventRepository.addEvent(eventWithFee) // Workshop

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Act - Filter for Technology OR Culture events
    val filterData =
        FilterData(
            categories = listOf("Technology", "Culture"),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Events with either tag should be included
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.any { it.uid == "event-epfl" })
    assertTrue(filteredEvents.any { it.uid == "event-lausanne" })
    assertTrue(filteredEvents.none { it.uid == "event-paid" })
  }

  @Test
  fun applyFilters_withFavoritesOnly_filtersNonFavoriteEvents() = runTest {
    // Arrange
    eventRepository.addEvent(eventAtEPFL)
    eventRepository.addEvent(eventInLausanne)

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Mark one event as favorite
    viewModel.toggleFavorite("event-epfl")
    advanceUntilIdle()

    // Act - Filter for favorites only
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = true)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Only favorite event should be included
    val filteredEvents = viewModel.uiState.value.events
    assertEquals(1, filteredEvents.size)
    assertTrue(filteredEvents.any { it.uid == "event-epfl" })
  }

  @Test
  fun applyFilters_withCombinedFilters_appliesAllFilters() = runTest {
    // Arrange
    eventRepository.addEvent(eventAtEPFL) // Technology @ EPFL, Free
    eventRepository.addEvent(eventWithFee) // Workshop @ EPFL, 25 CHF
    eventRepository.addEvent(eventInGeneva) // Business @ Geneva, Free

    viewModel =
        HomePageViewModel(eventRepository, userRepository, null, null, organizationRepository)
    advanceUntilIdle()

    // Act - Filter for Technology events within 10km of EPFL, free
    val filterData =
        FilterData(
            categories = listOf("Technology"),
            location = epflLocation,
            radiusKm = 10f,
            priceRange = 0f..10f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Only EPFL Technology free event should match
    val filteredEvents = viewModel.uiState.value.events
    assertEquals(1, filteredEvents.size)
    assertTrue(filteredEvents.any { it.uid == "event-epfl" })
  }
}
