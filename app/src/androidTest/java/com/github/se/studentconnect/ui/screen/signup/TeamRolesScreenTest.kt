package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.organization.OrganizationRole
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any

@RunWith(AndroidJUnit4::class)
class TeamRolesScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Mock callbacks
  private val onRoleNameChange = mock<(String) -> Unit>()
  private val onRoleDescriptionChange = mock<(String) -> Unit>()
  private val onAddRole = mock<() -> Unit>()
  private val onRemoveRole = mock<(OrganizationRole) -> Unit>()
  private val onBackClick = mock<() -> Unit>()
  private val onSkipClick = mock<() -> Unit>()
  private val onContinueClick = mock<() -> Unit>()

  private val defaultCallbacks =
      TeamRolesCallbacks(
          onRoleNameChange = onRoleNameChange,
          onRoleDescriptionChange = onRoleDescriptionChange,
          onAddRole = onAddRole,
          onRemoveRole = onRemoveRole,
          onBackClick = onBackClick,
          onSkipClick = onSkipClick,
          onContinueClick = onContinueClick)

  @Test
  fun elementsAreDisplayed_InitialEmptyState() {
    val state = TeamRolesState()

    composeTestRule.setContent { TeamRolesScreen(state = state, callbacks = defaultCallbacks) }

    // Verify Title and Subtitle
    composeTestRule.onNodeWithText("Set up your team roles").assertIsDisplayed()
    composeTestRule
        .onNodeWithText(
            "Define the positions in your organization. You'll assign members to these roles later.")
        .assertIsDisplayed()

    // Verify Inputs
    composeTestRule.onNodeWithText("Role name").assertIsDisplayed()
    composeTestRule.onNodeWithText("Role description").assertIsDisplayed()

    // Verify Empty State
    composeTestRule
        .onNodeWithText("No roles defined yet.", useUnmergedTree = true)
        .assertIsDisplayed()

    // Verify Buttons
    composeTestRule
        .onNodeWithText("+ Add role")
        .assertIsDisplayed()
        .assertIsNotEnabled() // Disabled because name is empty
    composeTestRule
        .onNodeWithText("Start Now")
        .assertIsDisplayed()
        .assertIsNotEnabled() // Disabled because list is empty
  }

  @Test
  fun inputsTriggerCallbacks_And_AddButtonEnables() {
    val state = TeamRolesState(roleName = "Test Role", roleDescription = "Test Desc")

    composeTestRule.setContent { TeamRolesScreen(state = state, callbacks = defaultCallbacks) }

    // Type in fields
    composeTestRule.onNodeWithText("Role name").performTextInput("President")
    verify(onRoleNameChange).invoke(any())

    composeTestRule.onNodeWithText("Role description").performTextInput("Lead")
    verify(onRoleDescriptionChange).invoke(any())

    // Verify Add button is enabled (since state.roleName is not blank in our test setup)
    val addButton = composeTestRule.onNodeWithText("+ Add role")
    addButton.assertIsEnabled()
    addButton.performClick()
    verify(onAddRole).invoke()
  }

  @Test
  fun populatedList_DisplaysRoles_And_EnablesContinue() {
    val role1 = OrganizationRole("President", "Runs the show")
    val role2 = OrganizationRole("Treasurer", "Handles money")
    val state = TeamRolesState(roles = listOf(role1, role2))

    composeTestRule.setContent { TeamRolesScreen(state = state, callbacks = defaultCallbacks) }

    // Verify Empty State is GONE
    composeTestRule
        .onNodeWithText("No roles defined yet.", useUnmergedTree = true)
        .assertDoesNotExist()

    // Verify List Items
    composeTestRule.onNodeWithText("President", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Runs the show", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Treasurer", useUnmergedTree = true).assertIsDisplayed()

    // Verify Continue Button Enabled
    val continueButton = composeTestRule.onNodeWithText("Start Now")
    continueButton.assertIsEnabled()
    continueButton.performClick()
    verify(onContinueClick).invoke()
  }

  @Test
  fun removeRole_TriggersCallback() {
    val role1 = OrganizationRole("Secretary", "Notes")
    val state = TeamRolesState(roles = listOf(role1))

    composeTestRule.setContent { TeamRolesScreen(state = state, callbacks = defaultCallbacks) }

    // Find the remove button text ("Remove") and click it
    composeTestRule.onNodeWithText("Remove", useUnmergedTree = true).performClick()

    verify(onRemoveRole).invoke(role1)
  }

  @Test
  fun navigationButtons_TriggerCallbacks() {
    val state = TeamRolesState()

    composeTestRule.setContent { TeamRolesScreen(state = state, callbacks = defaultCallbacks) }

    // Back Button (using content description from SignUpBackButton usually, or icon)
    // Assuming SignUpBackButton uses "Back" content description
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    verify(onBackClick).invoke()

    // Skip Button
    composeTestRule.onNodeWithText("Skip").performClick()
    verify(onSkipClick).invoke()
  }

  @Test
  fun suggestionDropdown_AppearsOnFocus() {
    // This test verifies that the dropdown logic inside the composable
    // hooks up to the UI correctly.
    val state = TeamRolesState(roleName = "")

    composeTestRule.setContent { TeamRolesScreen(state = state, callbacks = defaultCallbacks) }

    // Click on the text field to focus it
    composeTestRule.onNodeWithText("Role name").performClick()

    // Since the field is focused and suggestions exist by default in the composable,
    // the dropdown items should appear.
    // We check for one of the hardcoded suggestions.
    composeTestRule.onNodeWithText("President", useUnmergedTree = true).assertIsDisplayed()
  }
}
