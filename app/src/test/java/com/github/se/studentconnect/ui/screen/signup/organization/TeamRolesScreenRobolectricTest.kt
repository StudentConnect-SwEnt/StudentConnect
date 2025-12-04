package com.github.se.studentconnect.ui.screen.signup.organization

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.organization.OrganizationRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], qualifiers = "w400dp-h900dp")
class TeamRolesScreenRobolectricTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val ctx: Context = ApplicationProvider.getApplicationContext()

  private val mockCallbacks =
      TeamRolesCallbacks(
          onRoleNameChange = {},
          onRoleDescriptionChange = {},
          onAddRole = {},
          onRemoveRole = {},
          onBackClick = {},
          onSkipClick = {},
          onContinueClick = {})

  @Test
  fun emptyRoles_showsEmptyStateTexts() {
    val emptyTitle = ctx.getString(R.string.team_roles_empty_state_title)
    val emptySubtitle = ctx.getString(R.string.team_roles_empty_state_subtitle)

    composeTestRule.setContent {
      TeamRolesContent(
          state = TeamRolesState(), suggestions = listOf("A", "B"), callbacks = mockCallbacks)
    }

    composeTestRule.waitForIdle()
    composeTestRule.mainClock.advanceTimeBy(1000)

    composeTestRule.onNodeWithText(emptyTitle).assertIsDisplayed()
    composeTestRule.onNodeWithText(emptySubtitle).assertIsDisplayed()
  }

  @Test
  fun roleList_showsRoleAndRemoveTriggersCallback() {
    val role = OrganizationRole(name = "Leader", description = "Makes decisions")
    var removedRole: OrganizationRole? = null
    val removeText = ctx.getString(R.string.team_roles_remove_button)

    composeTestRule.setContent {
      TeamRolesContent(
          state = TeamRolesState(roles = listOf(role)),
          suggestions = listOf("A"),
          callbacks = mockCallbacks.copy(onRemoveRole = { removedRole = it }))
    }

    composeTestRule.waitForIdle()
    composeTestRule.mainClock.advanceTimeBy(1000)

    // role name and description should be visible
    composeTestRule.onNodeWithText(role.name).assertExists()
    composeTestRule.onNodeWithText(role.description!!).assertIsDisplayed()

    // click remove and verify callback
    composeTestRule.onNodeWithText(removeText).performClick()
    assertEquals(role, removedRole)
  }

  @Test
  fun roleCard_hidesDescriptionWhenBlank() {
    val role = OrganizationRole(name = "Volunteer", description = null)

    composeTestRule.setContent {
      TeamRolesContent(
          state = TeamRolesState(roles = listOf(role)),
          suggestions = listOf("A"),
          callbacks = mockCallbacks)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(role.name).assertExists()
    // description should not exist; ensure remove button exists to prove card rendered
    val removeText = ctx.getString(R.string.team_roles_remove_button)
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText(removeText).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText(removeText).assertIsDisplayed()
  }

  @Test
  fun rolesForm_addButtonEnabledBasedOnName_andOnAddCalled() {
    val addText = ctx.getString(R.string.team_roles_add_button)
    var addCalled = false

    // Use mutable state for role name
    val roleNameState = mutableStateOf("")

    composeTestRule.setContent {
      RolesFormCard(
          roleName = roleNameState.value,
          roleDescription = "",
          suggestions = listOf("A"),
          onRoleNameChange = { roleNameState.value = it },
          onRoleDescriptionChange = {},
          onAddRole = { addCalled = true })
    }
    composeTestRule.waitForIdle()

    // 1. Test Disabled State (initial state)
    composeTestRule.onNodeWithText(addText).assertIsNotEnabled()

    // 2. Test Enabled State (update state)
    roleNameState.value = "President"
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(addText).assertIsEnabled()
    composeTestRule.onNodeWithText(addText).performClick()
    assertTrue(addCalled)
  }

  @Test
  fun continueButton_enabledOnlyWhenRolesExist() {
    val continueText = ctx.getString(R.string.button_start_now)
    var continueClicked = false

    // Use mutable state for roles list
    val rolesState = mutableStateOf<List<OrganizationRole>>(emptyList())

    composeTestRule.setContent {
      TeamRolesContent(
          state = TeamRolesState(roles = rolesState.value),
          suggestions = emptyList(),
          callbacks = mockCallbacks.copy(onContinueClick = { continueClicked = true }))
    }

    // Case 1: Empty roles list -> Button Disabled
    composeTestRule.onNodeWithText(continueText).assertIsNotEnabled()

    // Case 2: Roles exist -> Button Enabled
    rolesState.value = listOf(OrganizationRole("Dev", "Writes code"))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(continueText).assertIsEnabled()
    composeTestRule.onNodeWithText(continueText).performClick()
    assertTrue(continueClicked)
  }

  @Test
  fun navigationButtons_triggerCallbacks() {
    var skipClicked = false
    var backClicked = false

    composeTestRule.setContent {
      TeamRolesContent(
          state = TeamRolesState(),
          suggestions = emptyList(),
          callbacks =
              mockCallbacks.copy(
                  onSkipClick = { skipClicked = true }, onBackClick = { backClicked = true }))
    }

    // Test Skip
    val skipText = ctx.getString(R.string.button_skip)
    composeTestRule.onNodeWithText(skipText).performClick()
    assertTrue("Skip callback failed", skipClicked)

    // Test Back (Look for content description "Back" which is used in SignUpBackButton)
    val backDesc = ctx.getString(R.string.content_description_back)
    composeTestRule.onNodeWithContentDescription(backDesc).assertExists()
    composeTestRule.onNodeWithContentDescription(backDesc).performClick()
    assertTrue("Back callback failed", backClicked)
  }

  @Test
  fun roleNameDropdown_typingTriggersChangeAndShowsSuggestions() {
    val suggestion = "President"
    val suggestions = listOf(suggestion, "Treasurer")

    // Use mutable state for the values passed to Content
    val roleNameState = mutableStateOf("")

    composeTestRule.setContent {
      // We need to observe the state here
      RolesFormCard(
          roleName = roleNameState.value,
          roleDescription = "",
          suggestions = suggestions,
          onRoleNameChange = { roleNameState.value = it },
          onRoleDescriptionChange = {},
          onAddRole = {})
    }

    val label = ctx.getString(R.string.team_roles_name_label)

    // 1. Verify text input updates variable (via callback simulation)
    composeTestRule.onNodeWithText(label).performClick()
    composeTestRule.onNodeWithText(label).performTextInput("Pres")

    // Verify state was updated
    assertEquals("Pres", roleNameState.value)

    // 2. Verify suggestion appears
    // The state update happens automatically because we used mutableStateOf in setContent
    composeTestRule.waitForIdle()

    // The dropdown menu item with "President" should now be displayed
    composeTestRule.onNodeWithText(suggestion).assertIsDisplayed()
    composeTestRule.onNodeWithText(suggestion).assertHasClickAction()
  }

  @Test
  fun helper_functions_calculateExpandedAndShouldExpand_behaviour() {
    // calculateExpandedState
    val r1 =
        calculateExpandedState(
            exactMatch = true,
            isFocused = true,
            shouldShowDropdown = true,
            isValueBlank = false,
            currentExpanded = true)
    assertFalse("Should collapse on exact match", r1)

    val r2 =
        calculateExpandedState(
            exactMatch = false,
            isFocused = true,
            shouldShowDropdown = true,
            isValueBlank = false,
            currentExpanded = false)
    assertTrue("Should expand when focused and suggestions available", r2)

    val r3 =
        calculateExpandedState(
            exactMatch = false,
            isFocused = false,
            shouldShowDropdown = true,
            isValueBlank = true,
            currentExpanded = true)
    assertFalse("Should collapse when not focused and blank", r3)

    // shouldExpandOnTextChange
    val suggestions = listOf("President", "Secretary")
    val filtered = listOf("President")
    val s1 = shouldExpandOnTextChange("Pres", suggestions, filtered)
    assertTrue("Should expand on partial match", s1)

    val s2 = shouldExpandOnTextChange("", suggestions, emptyList())
    assertFalse("Should not expand on empty text", s2)

    val s3 = shouldExpandOnTextChange("President", suggestions, filtered)
    assertFalse("Should not expand (remain expanded) on exact match via text change logic", s3)
  }
}
