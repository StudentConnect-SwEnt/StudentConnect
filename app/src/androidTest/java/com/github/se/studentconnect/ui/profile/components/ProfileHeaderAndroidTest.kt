package com.github.se.studentconnect.ui.profile.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileHeaderAndroidTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var testUser: User

  @Before
  fun setUp() {
    testUser =
        User(
            userId = "test123",
            username = "testuser",
            firstName = "John",
            lastName = "Doe",
            email = "john@test.com",
            university = "EPFL",
            country = "Switzerland",
            bio = "Software Engineering student")
  }

  @Test
  fun profileHeader_displaysAllUserInformation() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 42, eventsCount = 15),
          onFriendsClick = {},
          onEventsClick = {},
          onEditClick = {},
          onUserCardClick = {})
    }

    // Verify name
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()

    // Verify bio
    composeTestRule.onNodeWithText("Software Engineering student").assertIsDisplayed()

    // Verify university
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()

    // Verify country
    composeTestRule.onNodeWithText("Switzerland").assertIsDisplayed()

    // Verify counts
    composeTestRule.onNodeWithText("42").assertIsDisplayed()
    composeTestRule.onNodeWithText("Friends").assertIsDisplayed()
    composeTestRule.onNodeWithText("15").assertIsDisplayed()
    composeTestRule.onNodeWithText("Events").assertIsDisplayed()

    // Verify buttons
    composeTestRule.onNodeWithText("Edit").assertIsDisplayed()
    composeTestRule.onNodeWithText("Card").assertIsDisplayed()
  }

  @Test
  fun profileHeader_friendsCountClickable() {
    var friendsClicked = false

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = { friendsClicked = true },
          onEventsClick = {})
    }

    composeTestRule.onNodeWithText("Friends").performClick()
    assert(friendsClicked)
  }

  @Test
  fun profileHeader_eventsCountClickable() {
    var eventsClicked = false

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = { eventsClicked = true })
    }

    composeTestRule.onNodeWithText("Events").performClick()
    assert(eventsClicked)
  }

  @Test
  fun profileHeader_editButtonClickable() {
    var editClicked = false

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {},
          onEditClick = { editClicked = true })
    }

    composeTestRule.onNodeWithText("Edit").performClick()
    assert(editClicked)
  }

  @Test
  fun profileHeader_userCardButtonClickable() {
    var cardClicked = false

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {},
          onUserCardClick = { cardClicked = true })
    }

    composeTestRule.onNodeWithText("Card").performClick()
    assert(cardClicked)
  }

  @Test
  fun profileHeader_hidesEditButtonWhenCallbackNull() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {},
          onEditClick = null,
          onUserCardClick = {})
    }

    composeTestRule.onNodeWithText("Edit").assertDoesNotExist()
    composeTestRule.onNodeWithText("Card").assertIsDisplayed()
  }

  @Test
  fun profileHeader_hidesCardButtonWhenCallbackNull() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {},
          onEditClick = {},
          onUserCardClick = null)
    }

    composeTestRule.onNodeWithText("Edit").assertIsDisplayed()
    composeTestRule.onNodeWithText("Card").assertDoesNotExist()
  }

  @Test
  fun profileHeader_handlesUserWithoutBio() {
    val userWithoutBio = testUser.copy(bio = null)

    composeTestRule.setContent {
      ProfileHeader(
          user = userWithoutBio,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {})
    }

    // Should display other info
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
  }

  @Test
  fun profileHeader_handlesUserWithoutCountry() {
    val userWithoutCountry = testUser.copy(country = null)

    composeTestRule.setContent {
      ProfileHeader(
          user = userWithoutCountry,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {})
    }

    // Should display other info
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()

    // Location icon should not exist
    composeTestRule.onNodeWithContentDescription("Location").assertDoesNotExist()
  }

  @Test
  fun profileHeader_displaysZeroCounts() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 0, eventsCount = 0),
          onFriendsClick = {},
          onEventsClick = {})
    }

    composeTestRule.onAllNodesWithText("0").assertCountEquals(2)
    composeTestRule.onNodeWithText("Friends").assertIsDisplayed()
    composeTestRule.onNodeWithText("Events").assertIsDisplayed()
  }

  @Test
  fun profileHeader_displaysLargeCounts() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 999, eventsCount = 500),
          onFriendsClick = {},
          onEventsClick = {})
    }

    composeTestRule.onNodeWithText("999").assertIsDisplayed()
    composeTestRule.onNodeWithText("500").assertIsDisplayed()
  }

  @Test
  fun profileHeader_displaysProfilePictureIcon() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {})
    }

    composeTestRule.onNodeWithContentDescription("Profile Picture").assertIsDisplayed()
  }

  @Test
  fun profileHeader_displaysLocationIcon() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {})
    }

    composeTestRule.onNodeWithContentDescription("Location").assertIsDisplayed()
  }

  @Test
  fun profileHeader_displaysEditIcon() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {},
          onEditClick = {})
    }

    composeTestRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
  }

  @Test
  fun profileHeader_displaysUserCardIcon() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {},
          onUserCardClick = {})
    }

    composeTestRule.onNodeWithContentDescription("User Card").assertIsDisplayed()
  }

  @Test
  fun profileHeader_handlesSpecialCharactersInName() {
    val specialUser = testUser.copy(firstName = "José-María", lastName = "O'Connor")

    composeTestRule.setContent {
      ProfileHeader(
          user = specialUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {})
    }

    composeTestRule.onNodeWithText("José-María O'Connor").assertIsDisplayed()
  }

  @Test
  fun profileHeader_handlesLongUniversityName() {
    val longUniversity = "Swiss Federal Institute of Technology in Lausanne"
    val userWithLongUni = testUser.copy(university = longUniversity)

    composeTestRule.setContent {
      ProfileHeader(
          user = userWithLongUni,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {})
    }

    composeTestRule.onNodeWithText(longUniversity).assertIsDisplayed()
  }

  @Test
  fun profileHeader_handlesUserWithProfilePictureUrl() {
    // User with profile picture URL (image won't load in test but we verify no crash)
    val userWithPicture = testUser.copy(profilePictureUrl = "test_profile_picture_id")

    composeTestRule.setContent {
      ProfileHeader(
          user = userWithPicture,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          onFriendsClick = {},
          onEventsClick = {})
    }

    // Should still display profile picture area (placeholder while loading)
    composeTestRule.onNodeWithContentDescription("Profile Picture").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
  }
}
