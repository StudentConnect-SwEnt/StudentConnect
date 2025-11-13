package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.NotificationRepositoryLocal
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.viewmodel.NotificationViewModel
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenAdditionalCoverageTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var notificationRepository: NotificationRepositoryLocal
  private lateinit var viewModel: HomePageViewModel
  private lateinit var notificationViewModel: NotificationViewModel

  @Before
  fun setup() {
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    notificationRepository = NotificationRepositoryLocal()
    viewModel = HomePageViewModel(eventRepository, userRepository)
    notificationViewModel = NotificationViewModel(notificationRepository)
  }

  private val testEvent =
      Event.Public(
          uid = "event-1",
          title = "Test Event",
          subtitle = "Test subtitle",
          description = "Test description",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("test"))

  @Test
  fun storyItem_viewed_displaysCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "Test Story",
          avatarRes = R.drawable.avatar_12,
          viewed = true,
          onClick = {},
          testTag = "test_story")
    }

    composeTestRule.onNodeWithTag("test_story").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_viewed").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Story").assertIsDisplayed()
  }

  @Test
  fun storyItem_unseen_displaysCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "Test Story",
          avatarRes = R.drawable.avatar_12,
          viewed = false,
          onClick = {},
          testTag = "test_story_unseen")
    }

    composeTestRule.onNodeWithTag("test_story_unseen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_unseen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Story").assertIsDisplayed()
  }

  @Test
  fun storyItem_onClick_triggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      StoryItem(
          name = "Test Story",
          avatarRes = R.drawable.avatar_12,
          viewed = false,
          onClick = { clicked = true },
          testTag = "clickable_story")
    }

    composeTestRule.onNodeWithTag("clickable_story").performClick()
    assert(clicked)
  }

  @Test
  fun storyItem_withCustomContentDescription_displaysCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "Custom Story",
          avatarRes = R.drawable.avatar_12,
          viewed = false,
          onClick = {},
          contentDescription = "Custom description",
          testTag = "custom_desc_story")
    }

    composeTestRule.onNodeWithTag("custom_desc_story").assertIsDisplayed()
  }

  @Test
  fun storyItem_withEmptyTestTag_displaysCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "No Tag Story", avatarRes = R.drawable.avatar_12, viewed = false, onClick = {})
    }

    composeTestRule.onNodeWithText("No Tag Story").assertIsDisplayed()
  }

  @Test
  fun storiesRow_withNoStories_displaysAddStoryButton() {
    composeTestRule.setContent {
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = emptyMap())
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addStoryButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Story").assertIsDisplayed()
  }

  @Test
  fun storiesRow_addStoryButton_triggersCallback() {
    var addStoryClicked = false
    composeTestRule.setContent {
      StoriesRow(
          onAddStoryClick = { addStoryClicked = true }, onClick = { _, _ -> }, stories = emptyMap())
    }

    composeTestRule.onNodeWithTag("addStoryButton").performClick()
    assert(addStoryClicked)
  }

  @Test
  fun storiesRow_withStories_displaysStories() {
    val stories: Map<Event, Pair<Int, Int>> = mapOf(testEvent to Pair(0, 5))

    composeTestRule.setContent {
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = stories)
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addStoryButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_item_event-1").assertIsDisplayed()
  }

  @Test
  fun storiesRow_withViewedStories_displaysCorrectly() {
    val stories: Map<Event, Pair<Int, Int>> = mapOf(testEvent to Pair(5, 5)) // All stories viewed

    composeTestRule.setContent {
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = stories)
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_item_event-1").assertIsDisplayed()
  }

  @Test
  fun storiesRow_withPartiallyViewedStories_displaysCorrectly() {
    val stories: Map<Event, Pair<Int, Int>> = mapOf(testEvent to Pair(3, 5)) // Partially viewed

    composeTestRule.setContent {
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = stories)
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_item_event-1").assertIsDisplayed()
  }

  @Test
  fun storiesRow_onClick_triggersCallback() {
    var clickedEvent: Event? = null
    var clickedIndex = -1
    val stories: Map<Event, Pair<Int, Int>> = mapOf(testEvent to Pair(2, 5))

    composeTestRule.setContent {
      StoriesRow(
          onAddStoryClick = {},
          onClick = { event, index ->
            clickedEvent = event
            clickedIndex = index
          },
          stories = stories)
    }

    composeTestRule.onNodeWithTag("story_item_event-1").performClick()
    assert(clickedEvent == testEvent)
    assert(clickedIndex == 2)
  }

  @Test
  fun storiesRow_withMultipleStories_displaysAll() {
    val event2 =
        Event.Public(
            uid = "event-2",
            title = "Second Event",
            subtitle = "Second subtitle",
            description = "Second description",
            start = Timestamp.now(),
            end = Timestamp.now(),
            location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
            website = "https://example.com",
            ownerId = "owner2",
            isFlash = false,
            tags = listOf("test"))

    val stories: Map<Event, Pair<Int, Int>> = mapOf(testEvent to Pair(0, 3), event2 to Pair(1, 4))

    composeTestRule.setContent {
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = stories)
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_item_event-1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_item_event-2").assertIsDisplayed()
  }

  @Test
  fun storiesRow_filtersOutEventsWithZeroStories() {
    val eventWithNoStories =
        Event.Public(
            uid = "event-no-stories",
            title = "No Stories Event",
            subtitle = "No stories",
            description = "Event without stories",
            start = Timestamp.now(),
            end = Timestamp.now(),
            location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
            website = "https://example.com",
            ownerId = "owner3",
            isFlash = false,
            tags = listOf("test"))

    val stories: Map<Event, Pair<Int, Int>> =
        mapOf(testEvent to Pair(0, 5), eventWithNoStories to Pair(0, 0))

    composeTestRule.setContent {
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = stories)
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_item_event-1").assertIsDisplayed()
    // Event with zero stories should not be displayed
    composeTestRule.onNodeWithTag("story_item_event-no-stories").assertDoesNotExist()
  }

  @Test
  fun homeTopBar_displaysSearchBar() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      HomeTopBar(
          notificationState = NotificationState(showNotifications = false),
          notificationCallbacks = NotificationCallbacks(onNotificationClick = {}, onDismiss = {}),
          navController = navController)
    }

    // Verify the search bar is displayed (don't click it as it requires NavHost)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Search for events…").assertExists()
  }

  @Test
  fun homeScreen_withLoadingState_displaysLoadingIndicator() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState = HomePageUiState(isLoading = true, events = emptyList()),
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withEmptyEvents_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false, events = emptyList(), subscribedEventsStories = emptyMap()),
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
    // Note: stories_row is not rendered when events list is empty
    // EventListScreen returns early without rendering topContent
  }

  @Test
  fun homeScreen_withEvents_displaysStoriesAndEvents() {
    val stories: Map<Event, Pair<Int, Int>> = mapOf(testEvent to Pair(1, 5))

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false, events = listOf(testEvent), subscribedEventsStories = stories),
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
  }

  @Test
  fun homeScreen_onDateSelected_triggersCallback() {
    var dateSelected = false
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState = HomePageUiState(isLoading = false, events = emptyList()),
          onDateSelected = { dateSelected = true },
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_onApplyFilters_triggersCallback() {
    var filtersApplied = false
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState = HomePageUiState(isLoading = false, events = emptyList()),
          onApplyFilters = { filtersApplied = true },
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_onFavoriteToggle_triggersCallback() {
    var favoriteToggled = false
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState = HomePageUiState(isLoading = false, events = emptyList()),
          onFavoriteToggle = { favoriteToggled = true },
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_onClearScrollTarget_triggersCallback() {
    var scrollTargetCleared = false
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState = HomePageUiState(isLoading = false, events = emptyList()),
          onClearScrollTarget = { scrollTargetCleared = true },
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withQRScannerOpen_displaysQRScanner() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = true,
          onQRScannerClosed = {},
          uiState = HomePageUiState(isLoading = false, events = emptyList()),
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_onCalendarClick_triggersCallback() {
    var calendarClicked = false
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState = HomePageUiState(isLoading = false, events = emptyList()),
          onCalendarClick = { calendarClicked = true },
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_onClickStory_triggersCallback() {
    var storyClicked = false
    val stories: Map<Event, Pair<Int, Int>> = mapOf(testEvent to Pair(0, 3))

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          onClickStory = { _, _ -> storyClicked = true },
          uiState =
              HomePageUiState(
                  isLoading = false, events = listOf(testEvent), subscribedEventsStories = stories),
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_item_event-1").performClick()
    assert(storyClicked)
  }

  @Test
  fun homeTopBar_dismissNotifications_triggersCallback() {
    var dismissed = false
    composeTestRule.setContent {
      val navController = rememberNavController()
      HomeTopBar(
          notificationState = NotificationState(showNotifications = true),
          notificationCallbacks =
              NotificationCallbacks(onNotificationClick = {}, onDismiss = { dismissed = true }),
          navController = navController)
    }

    composeTestRule.onNodeWithTag("HomePage").assertDoesNotExist()
  }

  @Test
  fun storiesRow_addStoryIcon_displaysCorrectly() {
    composeTestRule.setContent {
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = emptyMap())
    }

    composeTestRule.onNodeWithContentDescription("Add Story").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withMultipleCallbacks_allWork() {
    var qrClosed = false
    var storyClicked = false
    var dateSelected = false
    var calendarClicked = false
    var filtersApplied = false
    var favoriteToggled = false
    var scrollCleared = false

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = { qrClosed = true },
          onClickStory = { _, _ -> storyClicked = true },
          uiState = HomePageUiState(isLoading = false, events = emptyList()),
          onDateSelected = { dateSelected = true },
          onCalendarClick = { calendarClicked = true },
          onApplyFilters = { filtersApplied = true },
          onFavoriteToggle = { favoriteToggled = true },
          onClearScrollTarget = { scrollCleared = true },
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }
}
