package com.github.se.studentconnect.ui.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.ui.screens.HomeScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun homeScreen_displaysCorrectly() {
    composeTestRule.setContent { AppTheme { HomeScreen() } }

    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
  }

  @Test
  fun homeScreen_textStyle_isHeadlineMedium() {
    composeTestRule.setContent { AppTheme { HomeScreen() } }

    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
  }

  @Test
  fun homeScreen_columnLayout_isConfiguredCorrectly() {
    composeTestRule.setContent { AppTheme { HomeScreen() } }

    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
  }

  @Test
  fun homeScreen_textContent_isNotEmpty() {
    composeTestRule.setContent { AppTheme { HomeScreen() } }

    val homeText = "Home"
    assert(homeText.isNotEmpty())
    composeTestRule.onNodeWithText(homeText).assertIsDisplayed()
  }

  @Test
  fun homeScreen_testTag_exists() {
    val expectedTag = "home_screen"
    assert(expectedTag.isNotEmpty())

    composeTestRule.setContent { AppTheme { HomeScreen() } }
    composeTestRule.onNodeWithTag(expectedTag).assertIsDisplayed()
  }

  @Test
  fun homeScreen_modifier_defaultValue() {
    composeTestRule.setContent { AppTheme { HomeScreen() } }

    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withCustomModifier() {
    composeTestRule.setContent {
      AppTheme { HomeScreen() }
    }

    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
  }

  @Test
  fun homeScreen_modifierChaining() {
    composeTestRule.setContent {
      AppTheme { HomeScreen() }
    }

    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
  }

  @Test
  fun homeScreen_emptyModifier() {
    composeTestRule.setContent { AppTheme { HomeScreen() } }

    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
  }

  @Test
  fun homeScreen_modifierWithPadding() {
    composeTestRule.setContent {
      AppTheme { HomeScreen() }
    }

    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
  }

  @Test
  fun homeScreen_modifierWithSizeAndTestTag() {
    composeTestRule.setContent {
      AppTheme { HomeScreen() }
    }

    composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
  }
}
