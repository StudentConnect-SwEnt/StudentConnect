package com.github.se.studentconnect.ui.screen.signup.organization

import org.junit.Assert
import org.junit.Test

class TeamRolesScreenTest {

  @Test
  fun calculateExpandedState_exactMatch_returnsFalse() {
    val result =
        calculateExpandedState(
            exactMatch = true,
            isFocused = true,
            shouldShowDropdown = true,
            isValueBlank = false,
            currentExpanded = true)
    Assert.assertFalse(result)
  }

  @Test
  fun calculateExpandedState_focusedAndShouldShow_returnsTrue() {
    val result =
        calculateExpandedState(
            exactMatch = false,
            isFocused = true,
            shouldShowDropdown = true,
            isValueBlank = false,
            currentExpanded = false)
    Assert.assertTrue(result)
  }

  @Test
  fun calculateExpandedState_notFocusedAndBlank_returnsFalse() {
    val result =
        calculateExpandedState(
            exactMatch = false,
            isFocused = false,
            shouldShowDropdown = false,
            isValueBlank = true,
            currentExpanded = true)
    Assert.assertFalse(result)
  }

  @Test
  fun calculateExpandedState_else_returnsCurrentExpanded() {
    val resultTrue =
        calculateExpandedState(
            exactMatch = false,
            isFocused = false,
            shouldShowDropdown = false,
            isValueBlank = false,
            currentExpanded = true)
    val resultFalse =
        calculateExpandedState(
            exactMatch = false,
            isFocused = false,
            shouldShowDropdown = false,
            isValueBlank = false,
            currentExpanded = false)

    Assert.assertTrue(resultTrue)
    Assert.assertFalse(resultFalse)
  }

  @Test
  fun shouldExpandOnTextChange_blankOrExactMatch_returnsFalse() {
    val suggestions = listOf("President", "Secretary")

    // blank
    Assert.assertFalse(shouldExpandOnTextChange("", suggestions, emptyList()))

    // exact match (case-insensitive)
    Assert.assertFalse(shouldExpandOnTextChange("president", suggestions, listOf("president")))
  }

  @Test
  fun shouldExpandOnTextChange_nonExactWithFilteredSuggestions_returnsTrue() {
    val suggestions = listOf("President", "Vice President", "Secretary")
    val filtered = listOf("President")
    Assert.assertTrue(shouldExpandOnTextChange("Pres", suggestions, filtered))
  }

  @Test
  fun shouldExpandOnTextChange_nonExactButNoFilteredSuggestions_returnsFalse() {
    val suggestions = listOf("President", "Secretary")
    val filtered = emptyList<String>()
    Assert.assertFalse(shouldExpandOnTextChange("x", suggestions, filtered))
  }

  @Test
  fun teamRolesState_defaultsAndValues() {
    val defaultState = TeamRolesState()
    Assert.assertTrue(defaultState.roleName.isEmpty())
    Assert.assertTrue(defaultState.roleDescription.isEmpty())
    Assert.assertTrue(defaultState.roles.isEmpty())

    val custom = TeamRolesState(roleName = "A", roleDescription = "B", roles = listOf())
    Assert.assertEquals("A", custom.roleName)
    Assert.assertEquals("B", custom.roleDescription)
  }
}
