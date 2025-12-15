package com.github.se.studentconnect.ui.profile.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.user.User
import com.google.firebase.Timestamp
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
          callbacks =
              ProfileHeaderCallbacks(
                  onFriendsClick = {},
                  onEventsClick = {},
                  onEditClick = {},
                  onUserCardClick = {},
                  onLogoutClick = {}))
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
    composeTestRule.onNodeWithContentDescription("Logout").assertIsDisplayed()
    composeTestRule.onNodeWithText("Yes").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("No").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Are you sure you want to logout?").assertIsNotDisplayed()
  }

  @Test
  fun profileHeader_friendsCountClickable() {
    var friendsClicked = false

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks =
              ProfileHeaderCallbacks(
                  onFriendsClick = { friendsClicked = true }, onEventsClick = {}))
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
          callbacks =
              ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = { eventsClicked = true }))
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
          callbacks =
              ProfileHeaderCallbacks(
                  onFriendsClick = {}, onEventsClick = {}, onEditClick = { editClicked = true }))
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
          callbacks =
              ProfileHeaderCallbacks(
                  onFriendsClick = {},
                  onEventsClick = {},
                  onUserCardClick = { cardClicked = true }))
    }

    composeTestRule.onNodeWithText("Card").performClick()
    assert(cardClicked)
  }

  @Test
  fun profileHeader_logoutButtonClickable() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks =
              ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}, onLogoutClick = {}))
    }

    composeTestRule.onNodeWithContentDescription("Logout").assertHasClickAction()
  }

  @Test
  fun profileHeader_hidesEditButtonWhenCallbackNull() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks =
              ProfileHeaderCallbacks(
                  onFriendsClick = {},
                  onEventsClick = {},
                  onEditClick = null,
                  onUserCardClick = {}))
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
          callbacks =
              ProfileHeaderCallbacks(
                  onFriendsClick = {},
                  onEventsClick = {},
                  onEditClick = {},
                  onUserCardClick = null))
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
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
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
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
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
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
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
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
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
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
    }

    composeTestRule.onNodeWithContentDescription("Profile Picture").assertIsDisplayed()
  }

  @Test
  fun profileHeader_displaysLocationIcon() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
    }

    composeTestRule.onNodeWithContentDescription("Location").assertIsDisplayed()
  }

  @Test
  fun profileHeader_displaysEditIcon() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks =
              ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}, onEditClick = {}))
    }

    composeTestRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
  }

  @Test
  fun profileHeader_displaysUserCardIcon() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks =
              ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}, onUserCardClick = {}))
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
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
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
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
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
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
    }

    // Should still display profile picture area (placeholder while loading)
    composeTestRule.onNodeWithContentDescription("Profile Picture").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
  }

  @Test
  fun profileHeader_visitorMode_displaysFriendButtonsContent() {
    var friendButtonClicked = false

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          isVisitorMode = true,
          friendButtonsContent = {
            androidx.compose.material3.Button(onClick = { friendButtonClicked = true }) {
              androidx.compose.material3.Text("Add Friend")
            }
          })
    }

    // Verify visitor mode shows friend buttons instead of Edit/Card
    composeTestRule.onNodeWithText("Add Friend").assertIsDisplayed()
    composeTestRule.onNodeWithText("Edit").assertDoesNotExist()
    composeTestRule.onNodeWithText("Card").assertDoesNotExist()

    // Test button click
    composeTestRule.onNodeWithText("Add Friend").performClick()
    assert(friendButtonClicked)
  }

  @Test
  fun profileHeader_visitorMode_withoutFriendButtonsContent_showsNothing() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          isVisitorMode = true,
          friendButtonsContent = null)
    }

    // Should not show any buttons in visitor mode without friend buttons content
    composeTestRule.onNodeWithText("Edit").assertDoesNotExist()
    composeTestRule.onNodeWithText("Card").assertDoesNotExist()
  }

  @Test
  fun profileHeader_showsUsername_whenShowUsernameIsTrue() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          showUsername = true)
    }

    // Verify username is displayed
    composeTestRule.onNodeWithText("@testuser").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
  }

  @Test
  fun profileHeader_hidesUsername_whenShowUsernameIsFalse() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          showUsername = false)
    }

    // Verify username is not displayed
    composeTestRule.onNodeWithText("@testuser").assertDoesNotExist()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
  }

  @Test
  fun profileHeader_handlesEmptyBio() {
    val userWithEmptyBio = testUser.copy(bio = "")

    composeTestRule.setContent {
      ProfileHeader(
          user = userWithEmptyBio,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
    }

    // Should not display bio section when bio is empty string
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
  }

  @Test
  fun profileHeader_hidesBothButtons_whenBothCallbacksNull() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks =
              ProfileHeaderCallbacks(
                  onFriendsClick = {},
                  onEventsClick = {},
                  onEditClick = null,
                  onUserCardClick = null))
    }

    // Both buttons should be hidden
    composeTestRule.onNodeWithText("Edit").assertDoesNotExist()
    composeTestRule.onNodeWithText("Card").assertDoesNotExist()
  }

  @Test
  fun profileHeader_handlesWhitespaceBio() {
    val userWithWhitespaceBio = testUser.copy(bio = "   ")

    composeTestRule.setContent {
      ProfileHeader(
          user = userWithWhitespaceBio,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
    }

    // Should not display bio section when bio is only whitespace
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
  }

  @Test
  fun profileHeader_visitorModeWithShowUsername_displaysBoth() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          isVisitorMode = true,
          showUsername = true,
          friendButtonsContent = {
            androidx.compose.material3.Button(onClick = {}) {
              androidx.compose.material3.Text("Add Friend")
            }
          })
    }

    // Both username and visitor mode buttons should be displayed
    composeTestRule.onNodeWithText("@testuser").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Friend").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
  }

  @Test
  fun profileHeader_handlesLongBio() {
    val longBio =
        "This is a very long bio that contains a lot of information about the user. " +
            "It goes on and on with various details about their background, interests, " +
            "and experiences. This tests how the UI handles lengthy text content."
    val userWithLongBio = testUser.copy(bio = longBio)

    composeTestRule.setContent {
      ProfileHeader(
          user = userWithLongBio,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}))
    }

    // Should display the long bio
    composeTestRule.onNodeWithText(longBio).assertIsDisplayed()
  }

  @Test
  fun profileHeader_logoutConfirmationDialogAppears() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks =
              ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}, onLogoutClick = {}))
    }

    // Click the Logout button
    composeTestRule.onNodeWithContentDescription("Logout").performClick()

    // Verify that the confirmation dialog appears
    composeTestRule.onNodeWithText("Are you sure you want to logout?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Yes").assertIsDisplayed()
    composeTestRule.onNodeWithText("No").assertIsDisplayed()
  }

  @Test
  fun profileHeader_logoutConfirmationDialogCancels() {
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks =
              ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}, onLogoutClick = {}))
    }

    // Click the Logout button
    composeTestRule.onNodeWithContentDescription("Logout").performClick()

    // Click the No button to cancel logout
    composeTestRule.onNodeWithText("No").performClick()

    // Verify that the confirmation dialog is dismissed
    composeTestRule.onNodeWithText("Are you sure you want to logout?").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Yes").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("No").assertIsNotDisplayed()
  }

  @Test
  fun profileHeader_logoutConfirmationDialogConfirms() {
    var logoutConfirmed = false
    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks =
              ProfileHeaderCallbacks(
                  onFriendsClick = {},
                  onEventsClick = {},
                  onLogoutClick = { logoutConfirmed = true }))
    }

    // Click the Logout button
    composeTestRule.onNodeWithContentDescription("Logout").performClick()
    // Click the Yes button to confirm logout
    composeTestRule.onNodeWithText("Yes").performClick()
    // Verify that the logout was confirmed
    assert(logoutConfirmed)
  }

  // Tests for OrganizationBadge component
  @Test
  fun organizationBadge_displaysWithOrganization() {
    val organization =
        Organization(
            id = "org1",
            name = "Test Organization",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now())

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(organization))
    }

    // Verify organization badge is displayed (by checking organization name is accessible)
    composeTestRule.onNodeWithContentDescription("Test Organization").assertExists()
  }

  @Test
  fun organizationBadge_displaysWithOrganizationWithoutLogo() {
    val organization =
        Organization(
            id = "org1",
            name = "AGEPoly",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now(),
            logoUrl = null)

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(organization))
    }

    // Verify organization badge is displayed with star icon fallback (logoUrl is null)
    composeTestRule.onNodeWithContentDescription("AGEPoly").assertExists()
  }

  @Test
  fun organizationBadge_displaysWithOrganizationWithLogoUrl() {
    val organization =
        Organization(
            id = "org1",
            name = "EPFL",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now(),
            logoUrl = "test_logo_id")

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(organization))
    }

    // Verify organization badge is displayed (logo download may fail in test, but component
    // renders)
    composeTestRule.onNodeWithContentDescription("EPFL").assertExists()
  }

  @Test
  fun organizationBadge_handlesLongOrganizationName() {
    val organization =
        Organization(
            id = "org1",
            name = "Very Long Organization Name That Tests Curved Text",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now())

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(organization))
    }

    // Verify organization badge handles long names (curved text rendering)
    composeTestRule
        .onNodeWithContentDescription("Very Long Organization Name That Tests Curved Text")
        .assertExists()
  }

  @Test
  fun organizationBadge_handlesShortOrganizationName() {
    val organization =
        Organization(
            id = "org1",
            name = "EPFL",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now())

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(organization))
    }

    // Verify organization badge handles short names (tests charCount - 1 division by zero
    // protection)
    composeTestRule.onNodeWithContentDescription("EPFL").assertExists()
  }

  @Test
  fun organizationBadge_handlesSingleCharacterOrganizationName() {
    val organization =
        Organization(
            id = "org1",
            name = "A",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now())

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(organization))
    }

    // Verify organization badge handles single character names (tests coerceAtLeast(1))
    composeTestRule.onNodeWithContentDescription("A").assertExists()
  }

  @Test
  fun organizationBadge_handlesEmptyOrganizationName() {
    val organization =
        Organization(
            id = "org1",
            name = "",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now())

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(organization))
    }

    // Verify organization badge handles empty names gracefully
    composeTestRule.onNodeWithContentDescription("").assertExists()
  }

  @Test
  fun organizationBadge_displaysMultipleOrganizations() {
    val org1 =
        Organization(
            id = "org1",
            name = "First Org",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now())
    val org2 =
        Organization(
            id = "org2",
            name = "Second Org",
            type = OrganizationType.Company,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now())

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(org1, org2))
    }

    // Verify only first organization badge is displayed (firstOrNull() behavior)
    composeTestRule.onNodeWithContentDescription("First Org").assertExists()
    composeTestRule.onNodeWithContentDescription("Second Org").assertDoesNotExist()
  }

  @Test
  fun organizationBadge_handlesLogoDownloadFailure() {
    val organization =
        Organization(
            id = "org1",
            name = "Failed Logo Org",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now(),
            logoUrl = "invalid_logo_id_that_will_fail")

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(organization))
    }

    // Verify organization badge falls back to star icon when logo download fails
    // (logoBitmap will be null, so star icon is shown)
    composeTestRule.onNodeWithContentDescription("Failed Logo Org").assertExists()
  }

  @Test
  fun organizationBadge_handlesSpecialCharactersInOrganizationName() {
    val organization =
        Organization(
            id = "org1",
            name = "Org-Épfl & Co.",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf("test123"),
            createdBy = "test123",
            createdAt = Timestamp.now())

    composeTestRule.setContent {
      ProfileHeader(
          user = testUser,
          stats = ProfileStats(friendsCount = 10, eventsCount = 5),
          callbacks = ProfileHeaderCallbacks(onFriendsClick = {}, onEventsClick = {}),
          userOrganizations = listOf(organization))
    }

    // Verify organization badge handles special characters in curved text
    composeTestRule.onNodeWithContentDescription("Org-Épfl & Co.").assertExists()
  }
}
