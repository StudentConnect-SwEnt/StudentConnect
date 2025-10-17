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
  fun profileScreen_withCustomModifier() {
    composeTestRule.setContent {
      AppTheme { ProfileScreen(modifier = androidx.compose.ui.Modifier.padding(24.dp)) }
    }
    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun profileScreen_withChainedModifier() {
    composeTestRule.setContent {
      AppTheme {
        ProfileScreen(modifier = androidx.compose.ui.Modifier.padding(8.dp).fillMaxSize())
      }
    }
    composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
  }
}
