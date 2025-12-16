package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeminiPromptDialogTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `dialog displays title correctly`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = false) }
    }

    composeTestRule.onNodeWithText("Generate with Gemini").assertIsDisplayed()
  }

  @Test
  fun `dialog displays description text`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = false) }
    }

    composeTestRule
        .onNodeWithText("Provide a short prompt and Gemini will generate content for your event.")
        .assertIsDisplayed()
  }

  @Test
  fun `dialog displays prompt input field`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = false) }
    }

    composeTestRule.onNodeWithText("Prompt").assertIsDisplayed()
  }

  @Test
  fun `dialog displays placeholder text`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = false) }
    }

    composeTestRule
        .onNodeWithText("Describe the event (theme, tone, duration)...")
        .assertIsDisplayed()
  }

  @Test
  fun `generate button is disabled when prompt is empty`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = false) }
    }

    composeTestRule.onNodeWithText("Generate").assertIsNotEnabled()
  }

  @Test
  fun `generate button is enabled when prompt is not empty`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = false) }
    }

    composeTestRule.onNodeWithText("Prompt").performTextInput("Test prompt")
    composeTestRule.onNodeWithText("Generate").assertIsEnabled()
  }

  @Test
  fun `generate button is disabled when loading`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = true) }
    }

    composeTestRule.onNodeWithText("Generate").assertIsNotEnabled()
  }

  @Test
  fun `cancel button is displayed`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = false) }
    }

    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  @Test
  fun `cancel button is disabled when loading`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = true) }
    }

    composeTestRule.onNodeWithText("Cancel").assertIsNotEnabled()
  }

  @Test
  fun `loading indicator is displayed when loading`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = true) }
    }

    composeTestRule.onNodeWithText("Conjuring your masterpiece... ✨").assertIsDisplayed()
  }

  @Test
  fun `loading indicator is not displayed when not loading`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = false) }
    }

    composeTestRule.onNodeWithText("Conjuring your masterpiece... ✨").assertDoesNotExist()
  }

  @Test
  fun `onDismiss is called when cancel button is clicked`() {
    var dismissCalled = false

    composeTestRule.setContent {
      AppTheme {
        GeminiPromptDialog(onDismiss = { dismissCalled = true }, onGenerate = {}, isLoading = false)
      }
    }

    composeTestRule.onNodeWithText("Cancel").performClick()
    assertTrue(dismissCalled)
  }

  @Test
  fun `onGenerate is called with prompt when generate button is clicked`() {
    var generatedPrompt = ""

    composeTestRule.setContent {
      AppTheme {
        GeminiPromptDialog(onDismiss = {}, onGenerate = { generatedPrompt = it }, isLoading = false)
      }
    }

    composeTestRule.onNodeWithText("Prompt").performTextInput("Modern neon party")
    composeTestRule.onNodeWithText("Generate").performClick()
    assertEquals("Modern neon party", generatedPrompt)
  }

  @Test
  fun `text field is disabled when loading`() {
    composeTestRule.setContent {
      AppTheme { GeminiPromptDialog(onDismiss = {}, onGenerate = {}, isLoading = true) }
    }

    // When loading, the text field should be disabled
    composeTestRule.onNodeWithText("Prompt").assertIsNotEnabled()
  }
}
