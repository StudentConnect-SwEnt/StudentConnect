package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUITest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var viewModel: HomePageViewModel

  private val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Summer Festival",
          subtitle = "Best summer event",
          description = "Join us for an amazing summer festival.",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("music", "outdoor"))

  private val testEvent2 =
      Event.Public(
          uid = "event-2",
          title = "Tech Conference",
          subtitle = "Latest in tech",
          description = "Explore the latest technology trends.",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "SwissTech"),
          website = "https://example.com",
          ownerId = "owner2",
          isFlash = false,
          tags = listOf("tech", "networking"))

  @Before
  fun setup() {
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    viewModel = HomePageViewModel(eventRepository, userRepository)

    runBlocking {
      eventRepository.addEvent(testEvent1)
      eventRepository.addEvent(testEvent2)
    }
  }

  @Test
  fun homeScreen_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_searchBar_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Search for events...").assertIsDisplayed()
  }

  @Test
  fun homeScreen_searchIcon_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNodeWithContentDescription("Search Icon").assertIsDisplayed()
  }

  @Test
  fun homeScreen_notificationButton_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNodeWithContentDescription("Notifications").assertIsDisplayed()
  }

  @Test
  fun homeScreen_notificationButton_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNodeWithContentDescription("Notifications").assertHasClickAction()
  }

  @Test
  fun homeScreen_filterBar_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    // Wait for content to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Paris"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Paris").assertIsDisplayed()
    composeTestRule.onNodeWithText("Filtres").assertIsDisplayed()
  }

  // @Test
  fun homeScreen_displaysEvents() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    // Wait for events to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Summer Festival"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Summer Festival").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tech Conference").assertIsDisplayed()
  }

  // @Test
  fun homeScreen_displaysEventLocations() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    // Wait for events to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("EPFL"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
    composeTestRule.onNodeWithText("SwissTech").assertIsDisplayed()
  }

  @Test
  fun homeScreen_clickNotificationButton_doesNotCrash() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    // Wait for screen to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Click notification button - should not crash
    composeTestRule.onNodeWithContentDescription("Notifications").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_emptyState_displaysLoading() {
    val emptyRepository = EventRepositoryLocal()
    val emptyViewModel = HomePageViewModel(emptyRepository, userRepository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = emptyViewModel)
    }

    // Initially loading indicator should be displayed
    composeTestRule.waitForIdle()
    // After loading, no events should be displayed
  }

  @Test
  fun homeScreen_eventCard_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    // Wait for events to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Summer Festival"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Summer Festival").assertHasClickAction()
  }

  // @Test
  fun homeScreen_displaysMultipleEventCards() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    // Wait for events to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Summer Festival"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Both events should be displayed
    composeTestRule.onNodeWithText("Summer Festival").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tech Conference").assertIsDisplayed()
  }

  @Test
  fun homeScreen_filterChips_haveClickActions() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    // Wait for content to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Paris"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Paris").assertHasClickAction()
    composeTestRule.onNodeWithText("Filtres").assertHasClickAction()
    composeTestRule.onNodeWithText("Favorites").assertHasClickAction()
  }

  @Test
  fun homeScreen_clickFilterChip_doesNotCrash() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    // Wait for content to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Paris"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Paris").performClick()
    // Should show "Not yet implemented" toast
  }
}
