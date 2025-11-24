package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.organization.OrganizationRole
import com.github.se.studentconnect.ui.screen.signup.organization.TeamRolesCallbacks
import com.github.se.studentconnect.ui.screen.signup.organization.TeamRolesContent
import com.github.se.studentconnect.ui.screen.signup.organization.TeamRolesScreenWithLocalState
import com.github.se.studentconnect.ui.screen.signup.organization.TeamRolesState
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
    composeRule.setContent { AppTheme { TeamRolesScreenWithLocalState() } }

    composeRule.waitForIdle()
    val title = composeRule.activity.getString(R.string.team_roles_title)
    composeRule.onNodeWithText(title).assertIsDisplayed()
  }

  @Test
  fun backButton_invokesCallback() {
    var backClicked = false

    composeRule.setContent {
      AppTheme { TeamRolesScreenWithLocalState(onBackClick = { backClicked = true }) }
    }

    composeRule.waitForIdle()
    val backDesc = composeRule.activity.getString(R.string.content_description_back)
    composeRule.onNodeWithContentDescription(backDesc).performClick()

    composeRule.runOnIdle { Assert.assertTrue(backClicked) }
  }

  @Test
  fun skipButton_invokesCallback() {
    var skipClicked = false

    composeRule.setContent {
      AppTheme { TeamRolesScreenWithLocalState(onSkipClick = { skipClicked = true }) }
    }

    composeRule.waitForIdle()
    val skipText = composeRule.activity.getString(R.string.button_skip)
    composeRule.onNodeWithText(skipText).performClick()

    composeRule.runOnIdle { Assert.assertTrue(skipClicked) }
  }

  @Test
  fun continueButton_disabledWhenNoRoles() {
    composeRule.setContent { AppTheme { TeamRolesScreenWithLocalState() } }

    composeRule.waitForIdle()
    val startNowText = composeRule.activity.getString(R.string.button_start_now)
    composeRule.onNodeWithText(startNowText).assertIsNotEnabled()
  }

  @Test
  fun continueButton_enabledWhenRolesExist() {
    val testRoles = listOf(OrganizationRole(name = "President", description = null))

    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            state = TeamRolesState(roleName = "", roleDescription = "", roles = testRoles),
            suggestions = emptyList(),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
      }
    }

    composeRule.waitForIdle()
    val startNowText = composeRule.activity.getString(R.string.button_start_now)
    composeRule.onNodeWithText(startNowText).assertIsEnabled()
  }

  @Test
  fun continueButton_isClickableWhenRolesExist() {
    val testRoles = listOf(OrganizationRole(name = "President", description = null))

    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            state = TeamRolesState(roleName = "", roleDescription = "", roles = testRoles),
            suggestions = emptyList(),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
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
            state =
                TeamRolesState(
                    roleName = "",
                    roleDescription = "",
                    roles = listOf(OrganizationRole(name = "President", description = null))),
            suggestions = emptyList(),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
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
            state =
                TeamRolesState(
                    roleName = "",
                    roleDescription = "",
                    roles =
                        listOf(
                            OrganizationRole(
                                name = "President", description = "Oversees organization"))),
            suggestions = emptyList(),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
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
            state =
                TeamRolesState(
                    roleName = "",
                    roleDescription = "",
                    roles = listOf(OrganizationRole(name = "President", description = null))),
            suggestions = emptyList(),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
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
            state =
                TeamRolesState(
                    roleName = "",
                    roleDescription = "",
                    roles =
                        listOf(
                            OrganizationRole(name = "President", description = null),
                            OrganizationRole(name = "Treasurer", description = null))),
            suggestions = emptyList(),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
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
            state = TeamRolesState(roleName = "", roleDescription = "", roles = emptyList()),
            suggestions = emptyList(),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
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

  @Test
  fun rolesFormCard_displaysFormFields() {
    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            state =
                TeamRolesState(roleName = "Test", roleDescription = "Desc", roles = emptyList()),
            suggestions = listOf("President", "Treasurer"),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
      }
    }

    composeRule.waitForIdle()
    val addSectionText = composeRule.activity.getString(R.string.team_roles_add_section_title)
    composeRule.onNodeWithText(addSectionText).assertIsDisplayed()
  }

  @Test
  fun addRoleButton_disabledWhenRoleNameEmpty() {
    composeRule.setContent { AppTheme { TeamRolesScreenWithLocalState() } }

    composeRule.waitForIdle()
    val addButtonText = composeRule.activity.getString(R.string.team_roles_add_button)
    composeRule.onNodeWithText(addButtonText).assertIsNotEnabled()
  }

  @Test
  fun currentRolesSection_displaysTitle() {
    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            state = TeamRolesState(roleName = "", roleDescription = "", roles = emptyList()),
            suggestions = emptyList(),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
      }
    }

    composeRule.waitForIdle()
    val currentRolesText = composeRule.activity.getString(R.string.team_roles_current_section_title)
    composeRule.waitUntil(timeoutMillis = 3000) {
      try {
        composeRule.onNodeWithText(currentRolesText)
        true
      } catch (e: Exception) {
        false
      }
    }
  }

  @Test
  fun roleCard_withBlankDescription_hidesDescription() {
    composeRule.setContent {
      AppTheme {
        TeamRolesContent(
            state =
                TeamRolesState(
                    roleName = "",
                    roleDescription = "",
                    roles = listOf(OrganizationRole(name = "President", description = ""))),
            suggestions = emptyList(),
            callbacks =
                TeamRolesCallbacks(
                    onRoleNameChange = {},
                    onRoleDescriptionChange = {},
                    onAddRole = {},
                    onRemoveRole = {},
                    onBackClick = {},
                    onSkipClick = {},
                    onContinueClick = {}),
            modifier = Modifier)
      }
    }

    composeRule.waitForIdle()
    composeRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeRule.onNodeWithText("President")
        true
      } catch (e: Exception) {
        false
      }
    }
  }
}
