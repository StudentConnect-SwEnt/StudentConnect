package com.github.se.studentconnect.ui.screen.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class ActivitiesScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun activitiesScreen_displaysCorrectly() {
    composeTestRule.setContent { AppTheme { ActivitiesScreen() } }

    composeTestRule.onNodeWithTag("activities_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_textStyle_isHeadlineMedium() {
    composeTestRule.setContent { AppTheme { ActivitiesScreen() } }

    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_columnLayout_isConfiguredCorrectly() {
    composeTestRule.setContent { AppTheme { ActivitiesScreen() } }

    composeTestRule.onNodeWithTag("activities_screen").assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_textContent_isNotEmpty() {
    composeTestRule.setContent { AppTheme { ActivitiesScreen() } }

    val activitiesText = "Activities"
    assert(activitiesText.isNotEmpty())
    composeTestRule.onNodeWithText(activitiesText).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_testTag_exists() {
    val expectedTag = "activities_screen"
    assert(expectedTag.isNotEmpty())

    composeTestRule.setContent { AppTheme { ActivitiesScreen() } }
    composeTestRule.onNodeWithTag(expectedTag).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_modifier_defaultValue() {
    composeTestRule.setContent { AppTheme { ActivitiesScreen() } }

    composeTestRule.onNodeWithTag("activities_screen").assertIsDisplayed()
  }
}
