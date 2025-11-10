package com.github.se.studentconnect.ui.screen.home

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.NotificationRepositoryLocal
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.eventcreation.CreatePrivateEventScreen
import com.github.se.studentconnect.ui.eventcreation.CreatePrivateEventScreenTestTags
import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreen
import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreenTestTags
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import com.github.se.studentconnect.viewmodel.NotificationViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeScreenEditEventTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  override fun createInitializedRepository() = EventRepositoryLocal()

  private lateinit var ownerId: String
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var notificationRepository: NotificationRepositoryLocal
  private lateinit var homeViewModel: HomePageViewModel
  private lateinit var notificationViewModel: NotificationViewModel

  @Before
  fun captureOwner() {
    ownerId = currentUser.uid
    userRepository = UserRepositoryLocal()
    notificationRepository = NotificationRepositoryLocal()
    homeViewModel = HomePageViewModel(repository, userRepository)
    notificationViewModel = NotificationViewModel(notificationRepository)
  }

  @Test
  fun editingPublicEvent_updatesHomeList() {
    val event =
        Event.Public(
            uid = "public-home-test",
            ownerId = ownerId,
            title = "Original Public Title",
            description = "Original description",
            imageUrl = null,
            location = Location(46.52, 6.56, "Original Location"),
            start = Timestamp.fromDate(2025, 4, 9),
            end = Timestamp.fromDate(2025, 4, 10),
            maxCapacity = null,
            participationFee = null,
            isFlash = false,
            subtitle = "Original subtitle",
            tags = listOf("music"),
            website = "https://example.com")

    runBlocking { repository.addEvent(event) }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        LaunchedEffect(Unit) { navController.navigate("edit_public") }
        NavHost(navController = navController, startDestination = "home") {
          composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                notificationViewModel = notificationViewModel)
          }
          composable("edit_public") {
            CreatePublicEventScreen(navController = navController, existingEventId = event.uid)
          }
        }
      }
    }

    val updatedTitle = "Updated Public Title"
    val titleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    titleNode.performScrollTo().performTextClearance()
    titleNode.performTextInput(updatedTitle)

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertIsEnabled()
        .performClick()

    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText(updatedTitle), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }
  }

  @Test
  fun editingPrivateEvent_updatesHomeList() {
    val event =
        Event.Private(
            uid = "private-home-test",
            ownerId = ownerId,
            title = "Original Private Title",
            description = "Original private description",
            imageUrl = null,
            location = Location(46.51, 6.57, "Private Location"),
            start = Timestamp.fromDate(2025, 5, 1),
            end = Timestamp.fromDate(2025, 5, 2),
            maxCapacity = 20u,
            participationFee = 5u,
            isFlash = true)

    runBlocking { repository.addEvent(event) }

    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        LaunchedEffect(Unit) { navController.navigate("edit_private") }
        NavHost(navController = navController, startDestination = "home") {
          composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                notificationViewModel = notificationViewModel)
          }
          composable("edit_private") {
            CreatePrivateEventScreen(navController = navController, existingEventId = event.uid)
          }
        }
      }
    }

    val updatedTitle = "Updated Private Title"
    val titleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    titleNode.performScrollTo().performTextClearance()
    titleNode.performTextInput(updatedTitle)

    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertIsEnabled()
        .performClick()

    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasText(updatedTitle), useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }
  }
}
