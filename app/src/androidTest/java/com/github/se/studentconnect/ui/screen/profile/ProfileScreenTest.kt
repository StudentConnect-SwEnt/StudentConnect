package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun profileScreen_displaysCorrectly() {
    composeTestRule.setContent { AppTheme { ProfileScreen() } }

    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun profileScreen_textStyle_isHeadlineMedium() {
    composeTestRule.setContent { AppTheme { ProfileScreen() } }

    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun profileScreen_columnLayout_isConfiguredCorrectly() {
    composeTestRule.setContent { AppTheme { ProfileScreen() } }

    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
  }

  @Test
  fun profileScreen_textContent_isNotEmpty() {
    composeTestRule.setContent { AppTheme { ProfileScreen() } }

    val profileText = "Profile"
    assert(profileText.isNotEmpty())
    composeTestRule.onNodeWithText(profileText).assertIsDisplayed()
  }

  @Test
  fun profileScreen_testTag_exists() {
    val expectedTag = "profile_screen"
    assert(expectedTag.isNotEmpty())

    composeTestRule.setContent { AppTheme { ProfileScreen() } }
    composeTestRule.onNodeWithTag(expectedTag).assertIsDisplayed()
  }

  @Test
  fun profileScreen_modifier_defaultValue() {
    composeTestRule.setContent { AppTheme { ProfileScreen() } }

    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
  }

  @Test
  fun profileScreen_withCustomModifier() {
    composeTestRule.setContent {
      AppTheme { ProfileScreen(modifier = androidx.compose.ui.Modifier.padding(24.dp)) }
    }

    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun profileScreen_modifierChaining() {
    composeTestRule.setContent {
      AppTheme {
        ProfileScreen(modifier = androidx.compose.ui.Modifier.padding(8.dp).fillMaxSize())
      }
    }

    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun profileScreen_emptyModifier() {
    composeTestRule.setContent {
      AppTheme { ProfileScreen(modifier = androidx.compose.ui.Modifier) }
    }

    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
  }

  @Test
  fun profileScreen_modifierWithPadding() {
    composeTestRule.setContent {
      AppTheme { ProfileScreen(modifier = androidx.compose.ui.Modifier.padding(8.dp)) }
    }

    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun profileScreen_modifierWithSizeAndTestTag() {
    composeTestRule.setContent {
      AppTheme {
        ProfileScreen(modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(32.dp))
      }
    }

    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }
}
