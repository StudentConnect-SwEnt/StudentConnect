package com.github.se.studentconnect.ui.screen.visitorprofile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.resources.C
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

  @Test
  fun showsCancelAndRequestSent_whenStatusIsSent_andCancelInvokes() {
    var cancelled = false

    composeTestRule.setContent {
      MaterialTheme {
        VisitorProfileContent(
            user = sampleUser,
            onBackClick = {},
            onAddFriendClick = {},
            onCancelFriendClick = { cancelled = true },
            onRemoveFriendClick = {},
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
            onBackClick = {},
            onAddFriendClick = {},
            onCancelFriendClick = {},
            onRemoveFriendClick = { removed = true },
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
            onBackClick = {},
            onAddFriendClick = {},
            onCancelFriendClick = {},
            onRemoveFriendClick = { removed = true },
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
            onBackClick = {},
            onAddFriendClick = {},
            onCancelFriendClick = {},
            onRemoveFriendClick = {},
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
}
