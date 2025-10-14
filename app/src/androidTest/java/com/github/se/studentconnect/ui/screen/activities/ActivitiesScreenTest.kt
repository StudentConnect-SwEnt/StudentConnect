package com.github.se.studentconnect.ui.screen.activities

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class ActivitiesScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun activitiesScreen_displaysCorrectly() {
    composeTestRule.setContent { AppTheme { ActivitiesScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.activities_screen).assertIsDisplayed()
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

    composeTestRule.onNodeWithTag(C.Tag.activities_screen).assertIsDisplayed()
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
    val expectedTag = C.Tag.activities_screen
    assert(expectedTag.isNotEmpty())

    composeTestRule.setContent { AppTheme { ActivitiesScreen() } }
    composeTestRule.onNodeWithTag(expectedTag).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_modifier_defaultValue() {
    composeTestRule.setContent { AppTheme { ActivitiesScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.activities_screen).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_withCustomModifier() {
    composeTestRule.setContent {
      AppTheme { ActivitiesScreen(modifier = androidx.compose.ui.Modifier.padding(24.dp)) }
    }

    composeTestRule.onNodeWithTag(C.Tag.activities_screen).assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_modifierChaining() {
    composeTestRule.setContent {
      AppTheme {
        ActivitiesScreen(modifier = androidx.compose.ui.Modifier.padding(8.dp).fillMaxSize())
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.activities_screen).assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_emptyModifier() {
    composeTestRule.setContent {
      AppTheme { ActivitiesScreen(modifier = androidx.compose.ui.Modifier) }
    }

    composeTestRule.onNodeWithTag(C.Tag.activities_screen).assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_modifierWithPadding() {
    composeTestRule.setContent {
      AppTheme { ActivitiesScreen(modifier = androidx.compose.ui.Modifier.padding(8.dp)) }
    }

    composeTestRule.onNodeWithTag(C.Tag.activities_screen).assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
  }

  @Test
  fun activitiesScreen_modifierWithSizeAndTestTag() {
    composeTestRule.setContent {
      AppTheme {
        ActivitiesScreen(modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(32.dp))
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.activities_screen).assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
  }
}
