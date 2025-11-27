package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationToBeContinuedScreen
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationToBeContinuedScreen {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun organizationToBeContinuedScreen_displaysOrganizationInfo_andHandlesLogout() {
    val organization =
        Organization(
            id = "org123",
            name = "Test Org",
            type = OrganizationType.Association,
            description = "A test organization",
            location = "Lausanne",
            mainDomains = listOf("Tech", "Science"),
            createdAt = Timestamp.now(),
            createdBy = "user123")

    var logoutClicked = false

    composeTestRule.setContent {
      OrganizationToBeContinuedScreen(
          organization = organization, onLogout = { logoutClicked = true })
    }

    // Check if info is displayed
    composeTestRule.onNodeWithText("Test Org").assertIsDisplayed()
    composeTestRule.onNodeWithText("Association").assertIsDisplayed()
    composeTestRule.onNodeWithText("A test organization").assertIsDisplayed()
    composeTestRule.onNodeWithText("Location: Lausanne").assertIsDisplayed()
    composeTestRule.onNodeWithText("Domains: Tech, Science").assertIsDisplayed()

    // Test logout button
    composeTestRule.onNodeWithText("Logout").performClick()
    assert(logoutClicked)
  }

  @Test
  fun organizationToBeContinuedScreen_handlesNullOrganization() {
    composeTestRule.setContent { OrganizationToBeContinuedScreen(organization = null) }

    composeTestRule.onNodeWithText("Organization information unavailable").assertIsDisplayed()
  }
}
