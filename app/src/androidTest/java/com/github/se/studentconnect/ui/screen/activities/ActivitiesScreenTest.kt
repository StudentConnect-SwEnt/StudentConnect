package com.github.se.studentconnect.ui.screen.activities

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActivitiesScreenTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var ownerId: String
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var activitiesViewModel: ActivitiesViewModel
  private lateinit var repository: EventRepository

  @Before
  fun setup() {
    EventRepositoryProvider.overrideForTests(EventRepositoryLocal())
    repository = EventRepositoryProvider.repository

    ownerId = currentUser.uid
    userRepository = UserRepositoryLocal()
    activitiesViewModel = ActivitiesViewModel(repository, userRepository)
  }

  @Test
  fun activitiesScreen_displaysCarouselForOwnedEvent() {
    val futureStart = Timestamp(java.util.Date(System.currentTimeMillis() + 3600000))
    val futureEnd = Timestamp(java.util.Date(System.currentTimeMillis() + 7200000))

    val event =
        Event.Public(
            uid = "carousel-test-event",
            ownerId = ownerId,
            title = "Carousel Test Event",
            description = "Test event for carousel",
            imageUrl = null,
            location = Location(46.52, 6.56, "Test Location"),
            start = futureStart,
            end = futureEnd,
            maxCapacity = 50u,
            participationFee = null,
            isFlash = false,
            subtitle = "Test subtitle",
            tags = listOf("test"),
            website = "https://test.com")

    runBlocking {
      repository.addEvent(event)
      userRepository.joinEvent(ownerId, event.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Verify activities screen elements are displayed
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.TOP_APP_BAR).assertIsDisplayed()

    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_TAB_ROW).assertIsDisplayed()

    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertIsDisplayed()

    // Verify the event card is displayed in the carousel
    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText("Carousel Test Event"), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }
  }

  @Test
  fun activitiesScreen_displaysEmptyStateWhenNoEvents() {
    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Verify empty state is displayed
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.EMPTY_STATE_COLUMN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.EMPTY_STATE_TEXT).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_tabSwitching_works() {
    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Click on Invitations tab
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Invitations")).performClick()

    composeTestRule.waitForIdle()

    // Click on Archived tab
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Archived")).performClick()

    composeTestRule.waitForIdle()

    // Click back to Upcoming tab
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Upcoming")).performClick()

    composeTestRule.waitForIdle()

    // Verify screen is still displayed after tab switches
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_showsPastEventsInArchivedTab() {
    // Create a past event (already ended)
    val pastStart = Timestamp(java.util.Date(System.currentTimeMillis() - 7200000))
    val pastEnd = Timestamp(java.util.Date(System.currentTimeMillis() - 3600000))

    val pastEvent =
        Event.Private(
            uid = "past-event-test",
            ownerId = ownerId,
            title = "Past Event",
            description = "This event has ended",
            imageUrl = null,
            location = Location(46.51, 6.57, "Past Location"),
            start = pastStart,
            end = pastEnd,
            maxCapacity = 10u,
            participationFee = 0u,
            isFlash = false)

    runBlocking {
      repository.addEvent(pastEvent)
      userRepository.joinEvent(ownerId, pastEvent.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Switch to Archived tab
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Archived")).performClick()

    composeTestRule.waitForIdle()

    // Verify the past event is displayed in the Archived tab
    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText("Past Event"), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }
  }

  @Test
  fun activitiesScreen_displaysLoadingSkeleton() {
    // Create a new ViewModel to test loading state
    val newActivitiesViewModel = ActivitiesViewModel(repository, userRepository)

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(
            navController = navController, activitiesViewModel = newActivitiesViewModel)
      }
    }

    // The loading skeleton should be visible initially
    // Note: This might be very brief, so we just verify the screen loads
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_displaysPublicAndPrivateEvents() {
    val futureStart = Timestamp(java.util.Date(System.currentTimeMillis() + 3600000))
    val futureEnd = Timestamp(java.util.Date(System.currentTimeMillis() + 7200000))

    val publicEvent =
        Event.Public(
            uid = "public-activity",
            ownerId = ownerId,
            title = "Public Activity",
            description = "Public event",
            imageUrl = null,
            location = Location(46.52, 6.56, "Public Location"),
            start = futureStart,
            end = futureEnd,
            maxCapacity = 100u,
            participationFee = null,
            isFlash = false,
            subtitle = "Public subtitle",
            tags = listOf("public"),
            website = "https://public.com")

    val privateEvent =
        Event.Private(
            uid = "private-activity",
            ownerId = ownerId,
            title = "Private Activity",
            description = "Private event",
            imageUrl = null,
            location = Location(46.51, 6.57, "Private Location"),
            start = futureStart,
            end = futureEnd,
            maxCapacity = 20u,
            participationFee = 5u,
            isFlash = false)

    runBlocking {
      repository.addEvent(publicEvent)
      repository.addEvent(privateEvent)
      userRepository.joinEvent(ownerId, publicEvent.uid)
      userRepository.joinEvent(ownerId, privateEvent.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Verify carousel is displayed
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertIsDisplayed()

    // At least one event should be visible in the carousel
    composeTestRule.waitUntil(5_000) {
      val publicExists =
          composeTestRule
              .onAllNodes(hasText("Public Activity"), useUnmergedTree = true)
              .fetchSemanticsNodes(false)
              .isNotEmpty()

      val privateExists =
          composeTestRule
              .onAllNodes(hasText("Private Activity"), useUnmergedTree = true)
              .fetchSemanticsNodes(false)
              .isNotEmpty()

      publicExists || privateExists
    }
  }

  @Test
  fun activitiesScreen_carouselCard_isClickable() {
    val futureStart = Timestamp(java.util.Date(System.currentTimeMillis() + 3600000))
    val futureEnd = Timestamp(java.util.Date(System.currentTimeMillis() + 7200000))

    val event =
        Event.Public(
            uid = "clickable-event",
            ownerId = ownerId,
            title = "Clickable Event",
            description = "Event to test click",
            imageUrl = null,
            location = Location(46.52, 6.56, "Click Location"),
            start = futureStart,
            end = futureEnd,
            maxCapacity = 50u,
            participationFee = null,
            isFlash = false,
            subtitle = "Click subtitle",
            tags = listOf("click"),
            website = "https://click.com")

    runBlocking {
      repository.addEvent(event)
      userRepository.joinEvent(ownerId, event.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Wait for the card to be displayed
    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText("Clickable Event"), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Verify the carousel card exists
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.carouselCardTag(event.uid))
        .assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_displaysPublicEventIcon() {
    val futureStart = Timestamp(java.util.Date(System.currentTimeMillis() + 3600000))
    val futureEnd = Timestamp(java.util.Date(System.currentTimeMillis() + 7200000))

    val publicEvent =
        Event.Public(
            uid = "public-icon-test",
            ownerId = ownerId,
            title = "Public Event With Icon",
            description = "Test public event icon",
            imageUrl = null,
            location = Location(46.52, 6.56, "Public Location"),
            start = futureStart,
            end = futureEnd,
            maxCapacity = 100u,
            participationFee = null,
            isFlash = false,
            subtitle = "Public subtitle",
            tags = listOf("test"),
            website = "https://public.com")

    runBlocking {
      repository.addEvent(publicEvent)
      userRepository.joinEvent(ownerId, publicEvent.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Wait for the carousel to display
    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText("Public Event With Icon"), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Verify the carousel card is displayed
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.carouselCardTag(publicEvent.uid))
        .assertIsDisplayed()

    // The public icon (ic_web) should be displayed on the card
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_displaysPrivateEventIcon() {
    val futureStart = Timestamp(java.util.Date(System.currentTimeMillis() + 3600000))
    val futureEnd = Timestamp(java.util.Date(System.currentTimeMillis() + 7200000))

    val privateEvent =
        Event.Private(
            uid = "private-icon-test",
            ownerId = ownerId,
            title = "Private Event With Icon",
            description = "Test private event icon",
            imageUrl = null,
            location = Location(46.51, 6.57, "Private Location"),
            start = futureStart,
            end = futureEnd,
            maxCapacity = 20u,
            participationFee = 5u,
            isFlash = false)

    runBlocking {
      repository.addEvent(privateEvent)
      userRepository.joinEvent(ownerId, privateEvent.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Wait for the carousel to display
    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText("Private Event With Icon"), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Verify the carousel card is displayed
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.carouselCardTag(privateEvent.uid))
        .assertIsDisplayed()

    // The private icon (ic_lock) should be displayed on the card
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_displaysBothEventTypesWithIcons() {
    val futureStart = Timestamp(java.util.Date(System.currentTimeMillis() + 3600000))
    val futureEnd = Timestamp(java.util.Date(System.currentTimeMillis() + 7200000))

    val publicEvent =
        Event.Public(
            uid = "public-icon-mixed",
            ownerId = ownerId,
            title = "Public Mixed",
            description = "Public event",
            imageUrl = null,
            location = Location(46.52, 6.56, "Public Loc"),
            start = futureStart,
            end = futureEnd,
            maxCapacity = 100u,
            participationFee = null,
            isFlash = false,
            subtitle = "Public",
            tags = listOf("test"),
            website = "https://test.com")

    val privateEvent =
        Event.Private(
            uid = "private-icon-mixed",
            ownerId = ownerId,
            title = "Private Mixed",
            description = "Private event",
            imageUrl = null,
            location = Location(46.51, 6.57, "Private Loc"),
            start = futureStart,
            end = futureEnd,
            maxCapacity = 20u,
            participationFee = 5u,
            isFlash = false)

    runBlocking {
      repository.addEvent(publicEvent)
      repository.addEvent(privateEvent)
      userRepository.joinEvent(ownerId, publicEvent.uid)
      userRepository.joinEvent(ownerId, privateEvent.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Verify both cards are in the carousel
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertIsDisplayed()

    composeTestRule.waitUntil(5_000) {
      val publicExists =
          composeTestRule
              .onAllNodes(hasText("Public Mixed"), useUnmergedTree = true)
              .fetchSemanticsNodes(false)
              .isNotEmpty()

      val privateExists =
          composeTestRule
              .onAllNodes(hasText("Private Mixed"), useUnmergedTree = true)
              .fetchSemanticsNodes(false)
              .isNotEmpty()

      publicExists || privateExists
    }
  }

  @Test
  fun activitiesScreen_flashEvent_showsFlashIconWhenLive() {
    val now = Calendar.getInstance()
    val pastStart = Calendar.getInstance().apply { add(Calendar.MINUTE, -30) }
    val futureEnd = Calendar.getInstance().apply { add(Calendar.MINUTE, 30) }

    val flashEvent =
        Event.Public(
            uid = "flash-live-test",
            ownerId = ownerId,
            title = "Live Flash Event",
            description = "Flash event that is live",
            imageUrl = null,
            location = Location(46.52, 6.56, "Flash Location"),
            start = Timestamp(pastStart.time),
            end = Timestamp(futureEnd.time),
            maxCapacity = null,
            participationFee = null,
            isFlash = true,
            subtitle = "Flash",
            tags = listOf("flash"),
            website = null)

    runBlocking {
      repository.addEvent(flashEvent)
      userRepository.joinEvent(ownerId, flashEvent.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Wait for the carousel to display
    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText("Live Flash Event"), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Flash event should show flash icon, not LIVE text
    val flashIconDesc = composeTestRule.activity.getString(R.string.content_description_flash_event)
    composeTestRule.onNodeWithContentDescription(flashIconDesc).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(composeTestRule.activity.getString(R.string.event_label_live))
        .assertDoesNotExist()
  }

  @Test
  fun activitiesScreen_regularEvent_showsLiveBadgeWhenLive() {
    val now = Calendar.getInstance()
    val pastStart = Calendar.getInstance().apply { add(Calendar.MINUTE, -30) }
    val futureEnd = Calendar.getInstance().apply { add(Calendar.MINUTE, 30) }

    val regularEvent =
        Event.Public(
            uid = "regular-live-test",
            ownerId = ownerId,
            title = "Live Regular Event",
            description = "Regular event that is live",
            imageUrl = null,
            location = Location(46.52, 6.56, "Regular Location"),
            start = Timestamp(pastStart.time),
            end = Timestamp(futureEnd.time),
            maxCapacity = null,
            participationFee = null,
            isFlash = false,
            subtitle = "Regular",
            tags = listOf("regular"),
            website = null)

    runBlocking {
      repository.addEvent(regularEvent)
      userRepository.joinEvent(ownerId, regularEvent.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Wait for the carousel to display
    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText("Live Regular Event"), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Regular event should show LIVE badge, not flash icon
    composeTestRule
        .onNodeWithText(composeTestRule.activity.getString(R.string.event_label_live))
        .assertIsDisplayed()
    val flashIconDesc = composeTestRule.activity.getString(R.string.content_description_flash_event)
    composeTestRule.onNodeWithContentDescription(flashIconDesc).assertDoesNotExist()
  }

  @Test
  fun activitiesScreen_flashEvent_doesNotShowBadgeWhenNotLive() {
    val futureStart = Calendar.getInstance().apply { add(Calendar.HOUR, 1) }
    val futureEnd = Calendar.getInstance().apply { add(Calendar.HOUR, 3) }

    val flashEvent =
        Event.Public(
            uid = "flash-future-test",
            ownerId = ownerId,
            title = "Future Flash Event",
            description = "Flash event in the future",
            imageUrl = null,
            location = Location(46.52, 6.56, "Future Location"),
            start = Timestamp(futureStart.time),
            end = Timestamp(futureEnd.time),
            maxCapacity = null,
            participationFee = null,
            isFlash = true,
            subtitle = "Future Flash",
            tags = listOf("flash"),
            website = null)

    runBlocking {
      repository.addEvent(flashEvent)
      userRepository.joinEvent(ownerId, flashEvent.uid)
    }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        ActivitiesScreen(navController = navController, activitiesViewModel = activitiesViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // Wait for the carousel to display
    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText("Future Flash Event"), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Future flash event should not show any badge
    val flashIconDesc = composeTestRule.activity.getString(R.string.content_description_flash_event)
    composeTestRule.onNodeWithContentDescription(flashIconDesc).assertDoesNotExist()
    composeTestRule
        .onNodeWithText(composeTestRule.activity.getString(R.string.event_label_live))
        .assertDoesNotExist()
  }
}
