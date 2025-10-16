package com.github.se.studentconnect.ui.screen.description

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
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

  @Test
  fun descriptionContentHonorsModifier() {
    composeRule.setContent {
      AppTheme {
        DescriptionContent(
            description = "",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = Modifier.testTag("custom_content").padding(16.dp))
      }
    }

    composeRule.onNodeWithTag("custom_content").assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
  }

  @Test
  fun descriptionScreen_defaultParametersWork() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(description = text, onDescriptionChange = { text = it })
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_back).performClick() // Should not crash
    composeRule.onNodeWithTag(C.Tag.description_skip).performClick() // Should not crash
    composeRule.onNodeWithTag(C.Tag.description_continue).performClick() // Should not crash
  }

  @Test
  fun descriptionTopBar_displaysCorrectTexts() {
    composeRule.setContent { AppTheme { DescriptionTopBar(onBackClick = {}, onSkipClick = {}) } }

    composeRule.onNodeWithTag(C.Tag.description_title).assertTextEquals("Tell us more about you")
    composeRule
        .onNodeWithTag(C.Tag.description_subtitle)
        .assertTextEquals("What should others know")
    composeRule.onNodeWithText("Skip").assertIsDisplayed()
  }

  @Test
  fun descriptionPrompt_nonEmptyDescriptionHidesPlaceholder() {
    composeRule.setContent {
      AppTheme { DescriptionPrompt(description = "Test content", onDescriptionChange = {}) }
    }

    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Test content")
    composeRule.onNodeWithText("What should other students know about you?").assertDoesNotExist()
  }

  @Test
  fun descriptionScreen_textClearingRestoresPlaceholder() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("Initial text") }
        DescriptionScreen(description = text, onDescriptionChange = { text = it })
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Initial text")
    composeRule.onNodeWithTag(C.Tag.description_input).performTextClearance()
    composeRule.onNodeWithText("What should other students know about you?").assertIsDisplayed()
  }

  //  @Test
  //  fun descriptionPrompt_handlesSpecialCharacters() {
  //    var capturedText = ""
  //    composeRule.setContent {
  //      AppTheme { DescriptionPrompt(description = "", onDescriptionChange = { capturedText = it
  // }) }
  //    }
  //
  //    val specialText = "Special chars: @#$%^&*()_+-=[]{}|;:,.<>?"
  //    composeRule.onNodeWithTag(C.Tag.description_input).performTextInput(specialText)
  //    composeRule.runOnIdle { Assert.assertEquals(specialText, capturedText) }
  //  }

  @Test
  fun descriptionScreen_multipleTextUpdates() {
    val textHistory = mutableListOf<String>()
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = {
              text = it
              textHistory.add(it)
            })
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_input).performTextInput("First")
    composeRule.onNodeWithTag(C.Tag.description_input).performTextInput(" Second")
    composeRule.onNodeWithTag(C.Tag.description_input).performTextInput(" Third")

    composeRule.runOnIdle {
      Assert.assertTrue(textHistory.contains("First"))
      Assert.assertTrue(textHistory.contains("First Second"))
      Assert.assertTrue(textHistory.contains("First Second Third"))
    }
  }

  @Test
  fun descriptionTopBar_backIconHasCorrectContentDescription() {
    composeRule.setContent { AppTheme { DescriptionTopBar(onBackClick = {}, onSkipClick = {}) } }

    composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
  }

  @Test
  fun continueButton_displaysCorrectIconAndText() {
    composeRule.setContent { AppTheme { ContinueButton(onContinueClick = {}) } }

    composeRule.onNodeWithText("Continue").assertIsDisplayed()
    composeRule.onNodeWithContentDescription("Continue").assertIsDisplayed()
  }

  @Test
  fun descriptionContent_allComponentsVisible() {
    composeRule.setContent {
      AppTheme {
        DescriptionContent(
            description = "Test description",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {})
      }
    }

    // Verify all major components are present
    composeRule.onNodeWithTag(C.Tag.description_app_bar).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_prompt_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_continue).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_input).assertIsDisplayed()
  }

  @Test
  fun descriptionPrompt_behaviorWithComplexModifier() {
    composeRule.setContent {
      AppTheme { DescriptionPrompt(description = "Complex test", onDescriptionChange = {}) }
    }

    composeRule.onNodeWithTag(C.Tag.description_prompt_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Complex test")
  }

  // === COMPREHENSIVE MODIFIER BRANCH COVERAGE TESTS ===

  @Test
  fun descriptionScreen_withCustomModifier() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = { text = it },
            modifier = Modifier.padding(24.dp))
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_input).assertIsDisplayed()
  }

  @Test
  fun descriptionScreen_modifierChaining() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = { text = it },
            modifier = Modifier.padding(8.dp).fillMaxSize())
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithText("Tell us more about you").assertIsDisplayed()
  }

  @Test
  fun descriptionScreen_emptyModifier() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text, onDescriptionChange = { text = it }, modifier = Modifier)
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
  }

  @Test
  fun descriptionScreen_modifierWithSize() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = { text = it },
            modifier = Modifier.size(400.dp))
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_continue).assertIsDisplayed()
  }

  @Test
  fun descriptionContent_withCustomModifier() {
    composeRule.setContent {
      AppTheme {
        DescriptionContent(
            description = "Test",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = Modifier.padding(32.dp))
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Test")
  }

  @Test
  fun descriptionContent_modifierChaining() {
    composeRule.setContent {
      AppTheme {
        DescriptionContent(
            description = "Chained modifiers",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = Modifier.fillMaxSize().padding(16.dp))
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Chained modifiers")
  }

  @Test
  fun descriptionContent_emptyModifier() {
    composeRule.setContent {
      AppTheme {
        DescriptionContent(
            description = "Empty modifier test",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = Modifier)
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
  }

  @Test
  fun descriptionContent_surfaceModifierBehavior() {
    composeRule.setContent {
      AppTheme {
        DescriptionContent(
            description = "Surface test",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = Modifier.testTag("surface_modifier"))
      }
    }

    composeRule.onNodeWithTag("surface_modifier").assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
  }

  @Test
  fun descriptionTopBar_columnModifierLayout() {
    composeRule.setContent { AppTheme { DescriptionTopBar(onBackClick = {}, onSkipClick = {}) } }

    composeRule.onNodeWithTag(C.Tag.description_app_bar).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_back).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_skip).assertIsDisplayed()
  }

  @Test
  fun descriptionTopBar_iconButtonModifiers() {
    var backClicked = false
    var skipClicked = false

    composeRule.setContent {
      AppTheme {
        DescriptionTopBar(
            onBackClick = { backClicked = true }, onSkipClick = { skipClicked = true })
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_back).performClick()
    composeRule.onNodeWithTag(C.Tag.description_skip).performClick()

    composeRule.runOnIdle {
      Assert.assertTrue(backClicked)
      Assert.assertTrue(skipClicked)
    }
  }

  @Test
  fun descriptionTopBar_surfaceModifierForSkipButton() {
    composeRule.setContent { AppTheme { DescriptionTopBar(onBackClick = {}, onSkipClick = {}) } }

    composeRule.onNodeWithTag(C.Tag.description_skip).assertIsDisplayed()
    composeRule.onNodeWithText("Skip").assertIsDisplayed()
  }

  @Test
  fun continueButton_boxModifierBehavior() {
    var clicked = false
    composeRule.setContent { AppTheme { ContinueButton(onContinueClick = { clicked = true }) } }

    composeRule.onNodeWithTag(C.Tag.description_continue).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_continue).performClick()

    composeRule.runOnIdle { Assert.assertTrue(clicked) }
  }

  @Test
  fun continueButton_buttonModifierWithSemantics() {
    composeRule.setContent { AppTheme { ContinueButton(onContinueClick = {}) } }

    composeRule.onNodeWithTag(C.Tag.description_continue).assertIsDisplayed()
    composeRule.onNodeWithText("Continue").assertIsDisplayed()
    composeRule.onNodeWithContentDescription("Continue").assertIsDisplayed()
  }

  @Test
  fun continueButton_rowModifierWithArrangement() {
    composeRule.setContent { AppTheme { ContinueButton(onContinueClick = {}) } }

    composeRule.onNodeWithTag(C.Tag.description_continue).assertIsDisplayed()
    composeRule.onNodeWithText("Continue").assertIsDisplayed()
    composeRule.onNodeWithContentDescription("Continue").assertIsDisplayed()
  }

  @Test
  fun descriptionPrompt_columnModifierWithSemantics() {
    composeRule.setContent {
      AppTheme { DescriptionPrompt(description = "Column modifier test", onDescriptionChange = {}) }
    }

    composeRule.onNodeWithTag(C.Tag.description_prompt_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Column modifier test")
  }

  @Test
  fun descriptionPrompt_outlinedTextFieldModifier() {
    var textChanged = false
    composeRule.setContent {
      AppTheme { DescriptionPrompt(description = "", onDescriptionChange = { textChanged = true }) }
    }

    composeRule.onNodeWithTag(C.Tag.description_input).performTextInput("Test input")
    composeRule.runOnIdle { Assert.assertTrue(textChanged) }
  }

  @Test
  fun descriptionScreen_allModifierCombinations() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("Combined test") }
        DescriptionScreen(
            description = text,
            onDescriptionChange = { text = it },
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = Modifier.testTag("combined_modifier").padding(8.dp).fillMaxSize())
      }
    }

    composeRule.onNodeWithTag("combined_modifier").assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Combined test")
  }

  @Test
  fun descriptionContent_allModifierCombinations() {
    composeRule.setContent {
      AppTheme {
        DescriptionContent(
            description = "All modifiers",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {},
            modifier = Modifier.testTag("all_modifiers").size(500.dp).padding(24.dp))
      }
    }

    composeRule.onNodeWithTag("all_modifiers").assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("All modifiers")
  }

  @Test
  fun descriptionScreen_defaultModifierBehavior() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(description = text, onDescriptionChange = { text = it })
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_app_bar).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_continue).assertIsDisplayed()
  }

  @Test
  fun descriptionContent_defaultModifierBehavior() {
    composeRule.setContent {
      AppTheme {
        DescriptionContent(
            description = "Default behavior",
            onDescriptionChange = {},
            onBackClick = {},
            onSkipClick = {},
            onContinueClick = {})
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.description_input).assertTextEquals("Default behavior")
  }
}
