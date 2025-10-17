package com.github.se.studentconnect.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.visitorProfile.FriendRequestStatus
import com.github.se.studentconnect.ui.screen.visitorProfile.VisitorProfileScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisitorProfileScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun visitorProfileScreenDisplaysUserInformation() {
    val user =
        User(
            userId = "user-789",
            email = "guest@studentconnect.ch",
            firstName = "Jamie",
            lastName = "River",
            university = "University of Geneva",
            bio = "Exploring new study buddies.")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_user_name).assertTextEquals("Jamie River")
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_user_id).assertTextEquals("user-789")
    composeTestRule
        .onNodeWithTag(C.Tag.visitor_profile_bio)
        .assertTextEquals("Exploring new study buddies.")
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_avatar).assertIsDisplayed()
  }

  @Test
  fun visitorProfileScreenSignalsBackNavigation() {
    var backClicks = 0
    val user =
        User(
            userId = "user-101",
            email = "guest101@studentconnect.ch",
            firstName = "Sam",
            lastName = "Case",
            university = "University of Zurich")

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(user = user, onBackClick = { backClicks++ }, onAddFriendClick = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_back).performClick()

    composeTestRule.runOnIdle { assertEquals(1, backClicks) }
  }

  @Test
  fun visitorProfileScreenShowsPlaceholderWhenBioMissing() {
    val user =
        User(
            userId = "user-202",
            email = "guest202@studentconnect.ch",
            firstName = "Taylor",
            lastName = "Lee",
            university = "ETH Zurich",
            bio = null)

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule
        .onNodeWithTag(C.Tag.visitor_profile_bio)
        .assertTextEquals("No biography available yet.")
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_empty_state).assertIsDisplayed()
  }

  @Test
  fun visitorProfileScreenSignalsAddFriend() {
    var addFriendClicks = 0
    val user =
        User(
            userId = "user-404",
            email = "guest404@studentconnect.ch",
            firstName = "Morgan",
            lastName = "Sky",
            university = "HEC Lausanne")

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user, onBackClick = {}, onAddFriendClick = { addFriendClicks++ })
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).performClick()

    composeTestRule.runOnIdle { assertEquals(1, addFriendClicks) }
  }

  @Test
  fun visitorProfileTopBarShowsUserHandle() {
    val user =
        User(
            userId = "handle",
            email = "handle@studentconnect.ch",
            firstName = "H",
            lastName = "D",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule.onNodeWithText("@handle").assertIsDisplayed()
  }

  @Test
  fun visitorProfilePinnedEventsSectionShowsTitle() {
    val user =
        User(
            userId = "user-999",
            email = "u999@studentconnect.ch",
            firstName = "P",
            lastName = "E",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule.onNodeWithText("Pinned Events").assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_pinned_section).assertIsDisplayed()
  }

  @Test
  fun visitorProfileShowsInitialsFromName() {
    val user =
        User(
            userId = "id-1",
            email = "id1@studentconnect.ch",
            firstName = "Jamie",
            lastName = "River",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule.onNodeWithText("JR").assertIsDisplayed()
  }

  @Test
  fun visitorProfileShowsInitialsFromSingleLetterNames() {
    val user =
        User(
            userId = "xy123",
            email = "xy@studentconnect.ch",
            firstName = "X",
            lastName = "Y",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule.onNodeWithText("XY").assertIsDisplayed()
  }

  @Test
  fun visitorProfileShowsUserIdInitialsWhenNamesAreEmpty() {
    val user =
        User(
            userId = "abcd123",
            email = "test@studentconnect.ch",
            firstName = "",
            lastName = "",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    // Should show first 2 chars of userId in uppercase
    composeTestRule.onNodeWithText("AB").assertIsDisplayed()
  }

  @Test
  fun visitorProfileShowsInitialsFromFirstNameOnly() {
    val user =
        User(
            userId = "user-id",
            email = "test@studentconnect.ch",
            firstName = "Solo",
            lastName = "",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule.onNodeWithText("S").assertIsDisplayed()
  }

  @Test
  fun visitorProfileShowsInitialsFromLastNameOnly() {
    val user =
        User(
            userId = "user-id",
            email = "test@studentconnect.ch",
            firstName = "",
            lastName = "Alone",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule.onNodeWithText("A").assertIsDisplayed()
  }

  @Test
  fun visitorProfileHandlesBlankBio() {
    val user =
        User(
            userId = "user-blank-bio",
            email = "test@studentconnect.ch",
            firstName = "Test",
            lastName = "User",
            university = "Uni",
            bio = "   ")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule
        .onNodeWithTag(C.Tag.visitor_profile_bio)
        .assertTextEquals("No biography available yet.")
  }

  @Test
  fun visitorProfileShowsBlankBioInDifferentColor() {
    val user =
        User(
            userId = "user-no-bio",
            email = "test@studentconnect.ch",
            firstName = "No",
            lastName = "Bio",
            university = "Uni",
            bio = "")

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    // The placeholder text should be displayed
    composeTestRule
        .onNodeWithTag(C.Tag.visitor_profile_bio)
        .assertTextEquals("No biography available yet.")
  }

  @Test
  fun visitorProfileShowsAddFriendButtonWhenIdle() {
    val user =
        User(
            userId = "user-idle",
            email = "test@studentconnect.ch",
            firstName = "Idle",
            lastName = "User",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = {},
            friendRequestStatus = FriendRequestStatus.IDLE)
      }
    }

    composeTestRule.onNodeWithText("Add Friend").assertIsDisplayed()
  }

  @Test
  fun visitorProfileShowsSendingWhenSending() {
    val user =
        User(
            userId = "user-sending",
            email = "test@studentconnect.ch",
            firstName = "Sending",
            lastName = "User",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = {},
            friendRequestStatus = FriendRequestStatus.SENDING)
      }
    }

    composeTestRule.onNodeWithText("Sending...").assertIsDisplayed()
  }

  @Test
  fun visitorProfileShowsRequestSentWhenSent() {
    val user =
        User(
            userId = "user-sent",
            email = "test@studentconnect.ch",
            firstName = "Sent",
            lastName = "User",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = {},
            friendRequestStatus = FriendRequestStatus.SENT)
      }
    }

    composeTestRule.onNodeWithText("Request Sent").assertIsDisplayed()
  }

  @Test
  fun visitorProfileShowsAlreadyFriendsWhenFriends() {
    val user =
        User(
            userId = "user-friend",
            email = "test@studentconnect.ch",
            firstName = "Friend",
            lastName = "User",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = {},
            friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS)
      }
    }

    composeTestRule.onNodeWithText("Already Friends").assertIsDisplayed()
  }

  @Test
  fun visitorProfileShowsRequestPendingWhenAlreadySent() {
    val user =
        User(
            userId = "user-pending",
            email = "test@studentconnect.ch",
            firstName = "Pending",
            lastName = "User",
            university = "Uni")

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = {},
            friendRequestStatus = FriendRequestStatus.ALREADY_SENT)
      }
    }

    composeTestRule.onNodeWithText("Request Pending").assertIsDisplayed()
  }

  @Test
  fun visitorProfileButtonDisabledWhenSending() {
    val user =
        User(
            userId = "user-disabled",
            email = "test@studentconnect.ch",
            firstName = "Disabled",
            lastName = "User",
            university = "Uni")

    var addFriendClicks = 0

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = { addFriendClicks++ },
            friendRequestStatus = FriendRequestStatus.SENDING)
      }
    }

    // Button should be disabled, so clicking should not trigger callback
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).performClick()

    // Give time for any potential callback
    composeTestRule.waitForIdle()

    // Clicks should remain 0 because button is disabled
    assertEquals(0, addFriendClicks)
  }

  @Test
  fun visitorProfileButtonDisabledWhenSent() {
    val user =
        User(
            userId = "user-sent-disabled",
            email = "test@studentconnect.ch",
            firstName = "Sent",
            lastName = "Disabled",
            university = "Uni")

    var addFriendClicks = 0

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = { addFriendClicks++ },
            friendRequestStatus = FriendRequestStatus.SENT)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).performClick()
    composeTestRule.waitForIdle()

    assertEquals(0, addFriendClicks)
  }

  @Test
  fun visitorProfileButtonDisabledWhenAlreadyFriends() {
    val user =
        User(
            userId = "user-already-friends",
            email = "test@studentconnect.ch",
            firstName = "Already",
            lastName = "Friends",
            university = "Uni")

    var addFriendClicks = 0

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = { addFriendClicks++ },
            friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).performClick()
    composeTestRule.waitForIdle()

    assertEquals(0, addFriendClicks)
  }

  @Test
  fun visitorProfileButtonDisabledWhenAlreadySent() {
    val user =
        User(
            userId = "user-already-sent",
            email = "test@studentconnect.ch",
            firstName = "Already",
            lastName = "Sent",
            university = "Uni")

    var addFriendClicks = 0

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = { addFriendClicks++ },
            friendRequestStatus = FriendRequestStatus.ALREADY_SENT)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).performClick()
    composeTestRule.waitForIdle()

    assertEquals(0, addFriendClicks)
  }

  @Test
  fun visitorProfileButtonEnabledWhenError() {
    val user =
        User(
            userId = "user-error",
            email = "test@studentconnect.ch",
            firstName = "Error",
            lastName = "User",
            university = "Uni")

    var addFriendClicks = 0

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = { addFriendClicks++ },
            friendRequestStatus = FriendRequestStatus.ERROR)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).performClick()

    composeTestRule.runOnIdle { assertEquals(1, addFriendClicks) }
  }

  @Test
  fun visitorProfileButtonEnabledWhenIdle() {
    val user =
        User(
            userId = "user-idle-enabled",
            email = "test@studentconnect.ch",
            firstName = "Idle",
            lastName = "Enabled",
            university = "Uni")

    var addFriendClicks = 0

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileScreen(
            user = user,
            onBackClick = {},
            onAddFriendClick = { addFriendClicks++ },
            friendRequestStatus = FriendRequestStatus.IDLE)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).performClick()

    composeTestRule.runOnIdle { assertEquals(1, addFriendClicks) }
  }
}
