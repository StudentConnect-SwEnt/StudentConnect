package com.github.se.studentconnect.ui.screen.visitorprofile

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.resources.C
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class VisitorProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleUser =
      User(
          userId = "u1",
          username = "jane",
          firstName = "Jane",
          lastName = "Doe",
          email = "jane@example.com",
          university = "Uni")

  private val sampleUserWithImage =
      User(
          userId = "u1",
          username = "jane",
          firstName = "Jane",
          lastName = "Doe",
          email = "jane@example.com",
          profilePictureUrl = "https://example.com/image.jpg",
          university = "Uni")

  @Before
  fun setup() {
    // Initialize MediaRepository with a fake implementation to avoid Firebase initialization
    MediaRepositoryProvider.overrideForTests(
        object : MediaRepository {
          override suspend fun upload(uri: Uri, path: String?): String = "fake-id"

          override suspend fun download(id: String): Uri {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            return Uri.parse("android.resource://${context.packageName}/${R.drawable.avatar_12}")
          }

          override suspend fun delete(id: String) = Unit
        })
  }

  @After
  fun tearDown() {
    MediaRepositoryProvider.cleanOverrideForTests()
  }

  @Test
  fun showsCancelAndRequestSent_whenStatusIsSent_andCancelInvokes() {
    var cancelled = false

    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            friendsCount = 0,
            eventsCount = 0,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = { cancelled = true },
                    onRemoveFriendClick = {}),
            friendRequestStatus = FriendRequestStatus.SENT)
      }
    }

    // Cancel button should exist and be clickable
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_cancel_friend).assertExists()
    composeTestRule.onNodeWithText("Request Sent").assertExists()

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_cancel_friend).performClick()
    composeTestRule.runOnIdle { assert(cancelled) }
  }

  @Test
  fun showsRemoveFriend_whenAlreadyFriends_andShowsDialogOnClick() {
    var removed = false

    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            friendsCount = 0,
            eventsCount = 0,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = {},
                    onRemoveFriendClick = { removed = true }),
            friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_remove_friend).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_remove_friend).performClick()

    // Dialog should appear with confirm and dismiss buttons
    composeTestRule.onNodeWithText("Are you sure you want to remove this friend?").assertExists()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_dialog_confirm).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_dialog_dismiss).assertExists()

    // Click confirm
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_dialog_confirm).performClick()
    composeTestRule.runOnIdle { assert(removed) }
  }

  @Test
  fun removeFriendDialog_dismissesOnNoClick() {
    var removed = false

    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            friendsCount = 0,
            eventsCount = 0,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = {},
                    onRemoveFriendClick = { removed = true }),
            friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_remove_friend).performClick()

    // Click dismiss
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_dialog_dismiss).performClick()

    // Dialog should disappear
    composeTestRule
        .onNodeWithText("Are you sure you want to remove this friend?")
        .assertDoesNotExist()
    composeTestRule.runOnIdle { assert(!removed) }
  }

  @Test
  fun buttonText_and_enabledState_forVariousStatuses() {
    // Use a state so we can change the friendRequestStatus without calling setContent repeatedly
    val status = mutableStateOf(FriendRequestStatus.SENDING)

    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            friendsCount = 0,
            eventsCount = 0,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = {},
                    onRemoveFriendClick = {}),
            friendRequestStatus = status.value)
      }
    }

    // SENDING -> shows "Sending..." and button should be disabled
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).assertExists()
    composeTestRule.onNodeWithText("Sending…").assertExists()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).assertIsNotEnabled()

    // Switch to ERROR -> shows "Try Again" and should be enabled
    composeTestRule.runOnIdle { status.value = FriendRequestStatus.ERROR }
    composeTestRule.onNodeWithText("Try Again").assertExists()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).assertIsEnabled()

    // Switch to IDLE -> shows "Add Friend" and should be enabled
    composeTestRule.runOnIdle { status.value = FriendRequestStatus.IDLE }
    composeTestRule.onNodeWithText("Add Friend").assertExists()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).assertIsEnabled()
  }

  @Test
  fun clickingFriendsCount_whenNotFriends_showsToast() {
    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            friendsCount = 5,
            eventsCount = 3,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = {},
                    onRemoveFriendClick = {},
                    onFriendsClick = {}),
            friendRequestStatus = FriendRequestStatus.IDLE)
      }
    }

    // Click on friends count (should show toast, not navigate)
    composeTestRule.onNodeWithText("5", useUnmergedTree = true).performClick()
    // Toast verification is limited in unit tests, but we verify no crash occurs
    composeTestRule.waitForIdle()
  }

  @Test
  fun clickingFriendsCount_whenAlreadyFriends_invokesCallback() {
    var friendsClicked = false

    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            friendsCount = 5,
            eventsCount = 3,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = {},
                    onRemoveFriendClick = {},
                    onFriendsClick = { friendsClicked = true }),
            friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS)
      }
    }

    // Click on friends count (should invoke callback)
    composeTestRule.onNodeWithText("5", useUnmergedTree = true).performClick()
    composeTestRule.runOnIdle { assert(friendsClicked) }
  }

  @Test
  fun clickingEventsCount_whenNotFriends_showsToast() {
    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            friendsCount = 5,
            eventsCount = 3,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = {},
                    onRemoveFriendClick = {},
                    onEventsClick = {}),
            friendRequestStatus = FriendRequestStatus.IDLE)
      }
    }

    // Click on events count (should show toast, not navigate)
    composeTestRule.onNodeWithText("3", useUnmergedTree = true).performClick()
    // Toast verification is limited in unit tests, but we verify no crash occurs
    composeTestRule.waitForIdle()
  }

  @Test
  fun clickingEventsCount_whenAlreadyFriends_invokesCallback() {
    var eventsClicked = false

    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            friendsCount = 5,
            eventsCount = 3,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = {},
                    onRemoveFriendClick = {},
                    onEventsClick = { eventsClicked = true }),
            friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS)
      }
    }

    // Click on events count (should invoke callback)
    composeTestRule.onNodeWithText("3", useUnmergedTree = true).performClick()
    composeTestRule.runOnIdle { assert(eventsClicked) }
  }

  @Test
  fun visitorProfileScreen_withoutImage_showsPlaceholder() {

    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            friendsCount = 0,
            eventsCount = 0,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = {},
                    onRemoveFriendClick = {}),
            friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS)
      }
    }

    composeTestRule.waitForIdle()

    // Check that the placeholder avatar is displayed
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_avatar).assertDoesNotExist()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_avatar_placeholder).assertExists()
  }

  @Test
  fun visitorProfileScreen_withImage_showsImage() {

    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUserWithImage,
            friendsCount = 0,
            eventsCount = 0,
            pinnedEvents = emptyList(),
            callbacks =
                VisitorProfileCallbacks(
                    onBackClick = {},
                    onAddFriendClick = {},
                    onCancelFriendClick = {},
                    onRemoveFriendClick = {}),
            friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS)
      }
    }

    composeTestRule.waitForIdle()

    // Check that the placeholder avatar is displayed
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_avatar).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_avatar_placeholder).assertDoesNotExist()
  }
}
