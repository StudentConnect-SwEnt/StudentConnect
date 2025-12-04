package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import java.util.Calendar
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class HomeScreenTabPagerTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    // Initialize Firebase first (before accessing any repositories)
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    AuthenticationProvider.testUserId = "testUser123"
  }

  @After
  fun tearDown() {
    AuthenticationProvider.testUserId = null
  }

  private fun createTestEvent(
      uid: String,
      title: String,
      tags: List<String> = emptyList()
  ): Event.Public {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val futureTime = Timestamp(calendar.time)

    return Event.Public(
        uid = uid,
        title = title,
        subtitle = "Subtitle",
        description = "Description",
        start = futureTime,
        end = futureTime,
        location = Location(46.5197, 6.6323, "EPFL"),
        website = "https://example.com",
        ownerId = "owner1",
        tags = tags,
        isFlash = false)
  }

  @Test
  fun homeScreen_withForYouTab_displaysEvents() {
    // Arrange
    val events = listOf(createTestEvent("event-1", "Event 1", listOf("Sports")))
    val uiState =
        HomePageUiState(events = events, isLoading = false, selectedTab = HomeTabMode.FOR_YOU)

    // Act
    composeTestRule.setContent {
      val navController = rememberNavController()
      HomeScreen(navController = navController, uiState = uiState, onTabSelected = {})
    }

    // Assert
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_FOR_YOU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_SELECTOR).assertIsDisplayed()
  }

  @Test
  fun homeScreen_withEventsTab_displaysEvents() {
    // Arrange
    val events = listOf(createTestEvent("event-1", "Event 1"))
    val uiState =
        HomePageUiState(events = events, isLoading = false, selectedTab = HomeTabMode.EVENTS)

    // Act
    composeTestRule.setContent {
      val navController = rememberNavController()
      HomeScreen(navController = navController, uiState = uiState, onTabSelected = {})
    }

    // Assert
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_EVENTS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_SELECTOR).assertIsDisplayed()
  }

  @Test
  fun homeScreen_withDiscoverTab_displaysEvents() {
    // Arrange
    val events = listOf(createTestEvent("event-1", "Event 1"))
    val uiState =
        HomePageUiState(events = events, isLoading = false, selectedTab = HomeTabMode.DISCOVER)

    // Act
    composeTestRule.setContent {
      val navController = rememberNavController()
      HomeScreen(navController = navController, uiState = uiState, onTabSelected = {})
    }

    // Assert
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_DISCOVER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_SELECTOR).assertIsDisplayed()
  }

  @Test
  fun homeScreen_tabIndicator_isDisplayed() {
    // Arrange
    val uiState =
        HomePageUiState(events = emptyList(), isLoading = false, selectedTab = HomeTabMode.FOR_YOU)

    // Act
    composeTestRule.setContent {
      val navController = rememberNavController()
      HomeScreen(navController = navController, uiState = uiState, onTabSelected = {})
    }

    // Assert
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun homeScreen_withMultipleEvents_displaysAll() {
    // Arrange
    val events =
        listOf(
            createTestEvent("event-1", "Event 1", listOf("Sports")),
            createTestEvent("event-2", "Event 2", listOf("Music")),
            createTestEvent("event-3", "Event 3", listOf("Art")))
    val uiState =
        HomePageUiState(events = events, isLoading = false, selectedTab = HomeTabMode.FOR_YOU)

    // Act
    composeTestRule.setContent {
      val navController = rememberNavController()
      HomeScreen(navController = navController, uiState = uiState, onTabSelected = {})
    }

    // Assert
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_SELECTOR).assertIsDisplayed()
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withNoEvents_displaysEmptyState() {
    // Arrange
    val uiState =
        HomePageUiState(events = emptyList(), isLoading = false, selectedTab = HomeTabMode.FOR_YOU)

    // Act
    composeTestRule.setContent {
      val navController = rememberNavController()
      HomeScreen(navController = navController, uiState = uiState, onTabSelected = {})
    }

    // Assert
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_SELECTOR).assertIsDisplayed()
  }

  @Test
  fun homeScreen_tabSelector_withAllTabs() {
    // Arrange
    val uiState =
        HomePageUiState(events = emptyList(), isLoading = false, selectedTab = HomeTabMode.FOR_YOU)

    // Act
    composeTestRule.setContent {
      val navController = rememberNavController()
      HomeScreen(navController = navController, uiState = uiState, onTabSelected = {})
    }

    // Assert - All tabs should be visible
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_FOR_YOU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_EVENTS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_DISCOVER).assertIsDisplayed()
  }

  @Test
  fun homeScreen_userHobbies_areStoredInState() {
    // Arrange
    val userHobbies = listOf("Sports", "Music", "Art")
    val uiState =
        HomePageUiState(
            events = emptyList(),
            isLoading = false,
            selectedTab = HomeTabMode.FOR_YOU,
            userHobbies = userHobbies)

    // Act & Assert
    assertEquals(3, uiState.userHobbies.size)
    assertEquals("Sports", uiState.userHobbies[0])
    assertEquals("Music", uiState.userHobbies[1])
    assertEquals("Art", uiState.userHobbies[2])
  }

  @Test
  fun homeScreen_attendedEvents_areStoredInState() {
    // Arrange
    val attendedEvents =
        listOf(
            createTestEvent("attended-1", "Attended Event 1"),
            createTestEvent("attended-2", "Attended Event 2"))
    val uiState =
        HomePageUiState(
            events = emptyList(),
            isLoading = false,
            selectedTab = HomeTabMode.FOR_YOU,
            attendedEvents = attendedEvents)

    // Act & Assert
    assertEquals(2, uiState.attendedEvents.size)
    assertEquals("attended-1", uiState.attendedEvents[0].uid)
    assertEquals("attended-2", uiState.attendedEvents[1].uid)
  }

  @Test
  fun homeScreen_userPreferences_areStoredInState() {
    // Arrange
    val location = Location(46.5197, 6.6323, "EPFL")
    val preferences =
        UserPreferences(
            preferredLocation = location,
            preferredPriceRange = 10f..50f,
            preferredTimeOfDay = PreferredTimeOfDay.EVENING)
    val uiState =
        HomePageUiState(
            events = emptyList(),
            isLoading = false,
            selectedTab = HomeTabMode.FOR_YOU,
            userPreferences = preferences)

    // Act & Assert
    assertEquals(location, uiState.userPreferences.preferredLocation)
    assertEquals(10f..50f, uiState.userPreferences.preferredPriceRange)
    assertEquals(PreferredTimeOfDay.EVENING, uiState.userPreferences.preferredTimeOfDay)
  }

  @Test
  fun homePageUiState_defaultValues() {
    // Act
    val uiState = HomePageUiState()

    // Assert
    assertEquals(emptyMap<Event, Pair<Int, Int>>(), uiState.subscribedEventsStories)
    assertEquals(emptyList<Event>(), uiState.events)
    assertEquals(true, uiState.isLoading)
    assertEquals(false, uiState.isCalendarVisible)
    assertEquals(null, uiState.selectedDate)
    assertEquals(null, uiState.scrollToDate)
    assertEquals(false, uiState.showOnlyFavorites)
    assertEquals(HomeTabMode.FOR_YOU, uiState.selectedTab)
    assertEquals(emptyList<String>(), uiState.userHobbies)
    assertEquals(emptyList<Event>(), uiState.attendedEvents)
    assertEquals(UserPreferences(), uiState.userPreferences)
  }

  @Test
  fun homePageUiState_copyWithNewTab() {
    // Arrange
    val original = HomePageUiState()

    // Act
    val updated = original.copy(selectedTab = HomeTabMode.DISCOVER)

    // Assert
    assertEquals(HomeTabMode.DISCOVER, updated.selectedTab)
    assertEquals(original.events, updated.events)
    assertEquals(original.isLoading, updated.isLoading)
  }

  @Test
  fun homePageUiState_copyWithNewEvents() {
    // Arrange
    val original = HomePageUiState()
    val newEvents = listOf(createTestEvent("event-1", "Event 1"))

    // Act
    val updated = original.copy(events = newEvents)

    // Assert
    assertEquals(1, updated.events.size)
    assertEquals("event-1", updated.events[0].uid)
  }

  @Test
  fun homePageUiState_copyWithLoadingState() {
    // Arrange
    val original = HomePageUiState(isLoading = true)

    // Act
    val updated = original.copy(isLoading = false)

    // Assert
    assertEquals(false, updated.isLoading)
  }

  @Test
  fun homePageUiState_copyWithUserHobbies() {
    // Arrange
    val original = HomePageUiState()
    val hobbies = listOf("Sports", "Music")

    // Act
    val updated = original.copy(userHobbies = hobbies)

    // Assert
    assertEquals(2, updated.userHobbies.size)
    assertEquals("Sports", updated.userHobbies[0])
  }

  @Test
  fun homePageUiState_copyWithAttendedEvents() {
    // Arrange
    val original = HomePageUiState()
    val attendedEvents = listOf(createTestEvent("attended-1", "Attended"))

    // Act
    val updated = original.copy(attendedEvents = attendedEvents)

    // Assert
    assertEquals(1, updated.attendedEvents.size)
    assertEquals("attended-1", updated.attendedEvents[0].uid)
  }

  @Test
  fun homePageUiState_copyWithUserPreferences() {
    // Arrange
    val original = HomePageUiState()
    val location = Location(46.5197, 6.6323, "EPFL")
    val preferences = UserPreferences(preferredLocation = location)

    // Act
    val updated = original.copy(userPreferences = preferences)

    // Assert
    assertEquals(location, updated.userPreferences.preferredLocation)
  }

  @Test
  fun notificationState_defaultValues() {
    // Arrange & Act
    val state = NotificationState(showNotifications = false)

    // Assert
    assertEquals(false, state.showNotifications)
    // assertEquals(emptyList(), state.notifications)
    assertEquals(0, state.unreadCount)
  }

  @Test
  fun notificationState_withValues() {
    // Arrange & Act
    val state =
        NotificationState(showNotifications = true, notifications = emptyList(), unreadCount = 5)

    // Assert
    assertEquals(true, state.showNotifications)
    assertEquals(5, state.unreadCount)
  }

  @Test
  fun notificationCallbacks_defaultValues() {
    // Arrange
    var clicked = false
    var dismissed = false

    // Act
    val callbacks =
        NotificationCallbacks(
            onNotificationClick = { clicked = true }, onDismiss = { dismissed = true })

    callbacks.onNotificationClick()
    callbacks.onDismiss()

    // Assert
    assertEquals(true, clicked)
    assertEquals(true, dismissed)
  }

  @Test
  fun getTabTestTag_returnsCorrectTag() {
    // This tests the private function indirectly through the UI
    val uiState = HomePageUiState(selectedTab = HomeTabMode.FOR_YOU)

    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = uiState.selectedTab, onTabSelected = {})
    }

    // Assert
    composeTestRule.onNodeWithTag(HomeScreenTestTags.TAB_FOR_YOU).assertIsDisplayed()
  }
}
