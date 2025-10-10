package com.github.se.studentconnect.ui.screen.description

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DescriptionScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun descriptionScreenRendersAllElementsAndCallbacks() {
    var backClicks = 0
    var skipClicks = 0
    var continueClicks = 0

    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = { text = it },
            onBackClick = { backClicks++ },
            onSkipClick = { skipClicks++ },
            onContinueClick = { continueClicks++ })
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule
        .onNodeWithTag(C.Tag.description_title)
        .assertIsDisplayed()
        .assertTextEquals("Tell us more about you")
    composeRule
        .onNodeWithTag(C.Tag.description_subtitle)
        .assertTextEquals("What should others know")
    composeRule.onNodeWithTag(C.Tag.description_prompt_container).assertIsDisplayed()
    composeRule.onNodeWithText("What should other students know about you?").assertIsDisplayed()

    // Verify skip button text is visible
    composeRule.onNodeWithTag(C.Tag.description_skip).assertIsDisplayed()
    composeRule.onNodeWithText("Skip").assertIsDisplayed()

    composeRule.onNodeWithTag(C.Tag.description_back).performClick()
    composeRule.onNodeWithTag(C.Tag.description_skip).performClick()
    composeRule.onNodeWithTag(C.Tag.description_continue).performClick()

    composeRule.runOnIdle {
      Assert.assertEquals(1, backClicks)
      Assert.assertEquals(1, skipClicks)
      Assert.assertEquals(1, continueClicks)
    }
  }

  @Test
  fun typingDescriptionUpdatesState() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = { text = it },
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {})
      }
    }

    val input = "I love Compose UI testing!"
    composeRule.onNodeWithTag(C.Tag.description_input).performTextInput(input)
    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals(input)
  }

  @Test
  fun placeholderDisappearsWhenDescriptionFilled() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = { text = it },
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {})
      }
    }

    val placeholder = "What should other students know about you?"
    composeRule.onNodeWithText(placeholder).assertIsDisplayed()

    composeRule.onNodeWithTag(C.Tag.description_input).performTextInput("Testing placeholder hide")

    composeRule.onNodeWithText(placeholder).assertDoesNotExist()
  }

  @Test
  fun placeholderReappearsWhenDescriptionCleared() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = { text = it },
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {})
      }
    }

    val placeholder = "What should other students know about you?"
    val field = composeRule.onNodeWithTag(C.Tag.description_input)

    field.performTextInput("Temporary text")
    field.assertTextEquals("Temporary text")

    field.performTextReplacement("")

    composeRule.onNodeWithText(placeholder).assertIsDisplayed()
  }

  @Test
  fun descriptionTopBarDisplaysNavigationElements() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = { text = it },
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {})
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_app_bar).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_back).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_title).assertTextEquals("Tell us more about you")
    composeRule
        .onNodeWithTag(C.Tag.description_subtitle)
        .assertTextEquals("What should others know")

    // Continue button should remain visible with correct text
    composeRule.onNodeWithTag(C.Tag.description_continue).assertIsDisplayed()
    composeRule.onNodeWithText("Continue").assertIsDisplayed()
  }

  @Test
  fun descriptionContentWithPrefilledTextStillDisplaysControls() {
    var latestDescription = ""

    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("Existing summary") }
        DescriptionContent(
            description = text,
            onDescriptionChange = {
              text = it
              latestDescription = it
            },
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {})
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Existing summary")

    val updatedText = "Existing summary updated"
    composeRule.onNodeWithTag(C.Tag.description_input).performTextReplacement(updatedText)

    composeRule.runOnIdle { Assert.assertEquals(updatedText, latestDescription) }
    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals(updatedText)
  }

  @Test
  fun descriptionScreenPropagatesDescriptionChange() {
    var latest: String? = null

    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = {
              text = it
              latest = it
            },
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {})
      }
    }

    val value = "Compose all the things"
    composeRule.onNodeWithTag(C.Tag.description_input).performTextReplacement(value)

    composeRule.runOnIdle { Assert.assertEquals(value, latest) }
  }

  @Test
  fun descriptionTopBarInvokesBackAndSkipCallbacks() {
    var back = 0
    var skip = 0

    composeRule.setContent {
      AppTheme { DescriptionTopBar(onBackClick = { back++ }, onSkipClick = { skip++ }) }
    }

    composeRule.onNodeWithTag(C.Tag.description_back).performClick()
    composeRule.onNodeWithTag(C.Tag.description_skip).performClick()

    composeRule.runOnIdle {
      Assert.assertEquals(1, back)
      Assert.assertEquals(1, skip)
    }
  }

  @Test
  fun descriptionContentInvokesAllCallbacks() {
    var back = 0
    var skip = 0
    var forward = 0

    composeRule.setContent {
      AppTheme {
        DescriptionContent(
            description = "",
            onDescriptionChange = {},
            onBackClick = { back++ },
            onSkipClick = { skip++ },
            onContinueClick = { forward++ })
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_back).performClick()
    composeRule.onNodeWithTag(C.Tag.description_skip).performClick()
    composeRule.onNodeWithTag(C.Tag.description_continue).performClick()

    composeRule.runOnIdle {
      Assert.assertEquals(1, back)
      Assert.assertEquals(1, skip)
      Assert.assertEquals(1, forward)
    }
  }

  @Test
  fun descriptionPromptDisplaysPrefilledValue() {
    composeRule.setContent {
      AppTheme { DescriptionPrompt(description = "Prefilled", onDescriptionChange = {}) }
    }

    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Prefilled")
    composeRule.onNodeWithText("What should other students know about you?").assertDoesNotExist()
  }

  @Test
  fun descriptionPromptPropagatesChangeCallback() {
    val captured = mutableListOf<String>()

    composeRule.setContent {
      AppTheme { DescriptionPrompt(description = "", onDescriptionChange = { captured += it }) }
    }

    val newValue = "Collaborates across campuses"
    composeRule.onNodeWithTag(C.Tag.description_input).performTextReplacement(newValue)

    composeRule.runOnIdle { Assert.assertTrue(captured.contains(newValue)) }
  }

  @Test
  fun continueButtonInvokesCallback() {
    var invoked = false
    composeRule.setContent { AppTheme { ContinueButton(onContinueClick = { invoked = true }) } }

    composeRule.onNodeWithText("Continue").performClick()
    composeRule.runOnIdle { Assert.assertTrue(invoked) }
  }

  @Test
  fun continueButtonDisplaysArrowIconForAccessibility() {
    composeRule.setContent { AppTheme { ContinueButton(onContinueClick = {}) } }

    composeRule.onNodeWithContentDescription("Continue").assertIsDisplayed()
  }

  @Test
  fun descriptionScreenDisplaysProvidedDescription() {
    composeRule.setContent {
      AppTheme {
        DescriptionScreen(
            description = "Existing description",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {})
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Existing description")
  }

  @Test
  fun descriptionScreenHonorsModifier() {
    composeRule.setContent {
      AppTheme {
        DescriptionScreen(
            description = "",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = Modifier.testTag("custom_root"))
      }
    }

    composeRule.onNodeWithTag("custom_root").assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
  }
}
