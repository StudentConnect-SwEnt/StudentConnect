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
import com.github.se.studentconnect.ui.screen.visitorProfile.VisitorProfileScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import io.mockk.every
import io.mockk.mockk
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
  fun visitorProfileShowsInitialsFromUserIdWhenNamesBlank() {
    val user =
        mockk<User>(relaxed = true) {
          every { userId } returns "xy123"
          every { firstName } returns ""
          every { lastName } returns ""
          every { getFullName() } returns ""
        }

    composeTestRule.setContent {
      AppTheme { VisitorProfileScreen(user = user, onBackClick = {}, onAddFriendClick = {}) }
    }

    composeTestRule.onNodeWithText("XY").assertIsDisplayed()
  }
}
