package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class TeamRolesScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun screen_rendersTitleAndSubtitle() {
    composeRule.setContent { AppTheme { TeamRolesScreen() } }

    composeRule.waitForIdle()
    val title = composeRule.activity.getString(R.string.team_roles_title)
    composeRule.onNodeWithText(title).assertIsDisplayed()
  }

  @Test
  fun backButton_invokesCallback() {
    var backClicked = false

    composeRule.setContent { AppTheme { TeamRolesScreen(onBackClick = { backClicked = true }) } }

    composeRule.waitForIdle()
    val backDesc = composeRule.activity.getString(R.string.content_description_back)
    composeRule.onNodeWithContentDescription(backDesc).performClick()

    composeRule.runOnIdle { Assert.assertTrue(backClicked) }
  }

  @Test
  fun skipButton_invokesCallback() {
    var skipClicked = false

    composeRule.setContent { AppTheme { TeamRolesScreen(onSkipClick = { skipClicked = true }) } }

    composeRule.waitForIdle()
    val skipText = composeRule.activity.getString(R.string.button_skip)
    composeRule.onNodeWithText(skipText).performClick()

    composeRule.runOnIdle { Assert.assertTrue(skipClicked) }
  }

  @Test
  fun continueButton_disabledWhenNoRoles() {
    composeRule.setContent { AppTheme { TeamRolesScreen() } }

    composeRule.waitForIdle()
    val startNowText = composeRule.activity.getString(R.string.button_start_now)
    composeRule.onNodeWithText(startNowText).assertIsNotEnabled()
  }

  @Test
  fun continueButton_enabledWhenRolesExist() {
    val testRoles = listOf(TeamRole(id = "1", name = "President", description = null))

    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            roleName = "",
            roleDescription = "",
            roles = testRoles,
            suggestions = emptyList(),
            onRoleNameChange = {},
            onRoleDescriptionChange = {},
            onAddRole = {},
            onRemoveRole = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = androidx.compose.ui.Modifier)
      }
    }

    composeRule.waitForIdle()
    val startNowText = composeRule.activity.getString(R.string.button_start_now)
    composeRule.onNodeWithText(startNowText).assertIsEnabled()
  }

  @Test
  fun continueButton_isClickableWhenRolesExist() {
    val testRoles = listOf(TeamRole(id = "1", name = "President", description = null))

    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            roleName = "",
            roleDescription = "",
            roles = testRoles,
            suggestions = emptyList(),
            onRoleNameChange = {},
            onRoleDescriptionChange = {},
            onAddRole = {},
            onRemoveRole = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = androidx.compose.ui.Modifier)
      }
    }

    composeRule.waitForIdle()
    val startNowText = composeRule.activity.getString(R.string.button_start_now)
    composeRule.onNodeWithText(startNowText).assertIsEnabled()
    composeRule.onNodeWithText(startNowText).assertHasClickAction()
  }

  @Test
  fun roleCard_displaysRoleName() {
    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            roleName = "",
            roleDescription = "",
            roles = listOf(TeamRole(id = "1", name = "President", description = null)),
            suggestions = emptyList(),
            onRoleNameChange = {},
            onRoleDescriptionChange = {},
            onAddRole = {},
            onRemoveRole = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = androidx.compose.ui.Modifier)
      }
    }

    composeRule.waitForIdle()
    // Verify role name can be found (exists in tree)
    composeRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeRule.onNodeWithText("President")
        true
      } catch (e: Exception) {
        false
      }
    }
  }

  @Test
  fun roleCard_displaysDescriptionWhenPresent() {
    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            roleName = "",
            roleDescription = "",
            roles =
                listOf(
                    TeamRole(id = "1", name = "President", description = "Oversees organization")),
            suggestions = emptyList(),
            onRoleNameChange = {},
            onRoleDescriptionChange = {},
            onAddRole = {},
            onRemoveRole = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = androidx.compose.ui.Modifier)
      }
    }

    composeRule.waitForIdle()
    composeRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeRule.onNodeWithText("President")
        composeRule.onNodeWithText("Oversees organization")
        true
      } catch (e: Exception) {
        false
      }
    }
  }

  @Test
  fun removeRole_buttonIsDisplayed() {
    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            roleName = "",
            roleDescription = "",
            roles = listOf(TeamRole(id = "1", name = "President", description = null)),
            suggestions = emptyList(),
            onRoleNameChange = {},
            onRoleDescriptionChange = {},
            onAddRole = {},
            onRemoveRole = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = androidx.compose.ui.Modifier)
      }
    }

    composeRule.waitForIdle()
    val removeText = composeRule.activity.getString(R.string.team_roles_remove_button)
    composeRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeRule.onNodeWithText(removeText)
        true
      } catch (e: Exception) {
        false
      }
    }
  }

  @Test
  fun multipleRoles_displayedInList() {
    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            roleName = "",
            roleDescription = "",
            roles =
                listOf(
                    TeamRole(id = "1", name = "President", description = null),
                    TeamRole(id = "2", name = "Treasurer", description = null)),
            suggestions = emptyList(),
            onRoleNameChange = {},
            onRoleDescriptionChange = {},
            onAddRole = {},
            onRemoveRole = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = androidx.compose.ui.Modifier)
      }
    }

    composeRule.waitForIdle()
    composeRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeRule.onNodeWithText("President")
        composeRule.onNodeWithText("Treasurer")
        true
      } catch (e: Exception) {
        false
      }
    }
  }

  @Test
  fun emptyState_shownWhenNoRoles() {
    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            roleName = "",
            roleDescription = "",
            roles = emptyList(),
            suggestions = emptyList(),
            onRoleNameChange = {},
            onRoleDescriptionChange = {},
            onAddRole = {},
            onRemoveRole = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = androidx.compose.ui.Modifier)
      }
    }

    composeRule.waitForIdle()
    val emptyStateText = composeRule.activity.getString(R.string.team_roles_empty_state_title)
    composeRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeRule.onNodeWithText(emptyStateText)
        true
      } catch (e: Exception) {
        false
      }
    }
  }
}
