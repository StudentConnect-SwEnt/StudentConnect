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
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.signup.DescriptionContent
import com.github.se.studentconnect.ui.screen.signup.DescriptionScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
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
  fun descriptionScreenDisplaysNavigationElements() {
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

  @Test
  fun descriptionScreen_backIconHasCorrectContentDescription() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(description = text, onDescriptionChange = { text = it })
      }
    }

    composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
  }

  @Test
  fun descriptionScreen_continueButtonDisplaysCorrectly() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        DescriptionScreen(description = text, onDescriptionChange = { text = it })
      }
    }

    composeRule.onNodeWithTag(C.Tag.description_continue).assertIsDisplayed()
    composeRule.onNodeWithText("Continue").assertIsDisplayed()
  }
}
