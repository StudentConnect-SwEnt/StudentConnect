package com.github.se.studentconnect.ui.screen.description

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationDescriptionContent
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationDescriptionScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OrganizationDescriptionScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun organizationDescriptionRendersAllElementsAndCallbacks() {
    var backClicks = 0
    var continueClicks = 0

    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        OrganizationDescriptionScreen(
            about = text,
            onAboutChange = { text = it },
            onBackClick = { backClicks++ },
            onContinueClick = { continueClicks++ })
      }
    }

    val title = composeRule.activity.getString(R.string.org_description_title)
    val subtitle = composeRule.activity.getString(R.string.about_subtitle)
    val placeholder = composeRule.activity.getString(R.string.org_description_placeholder)

    composeRule.onNodeWithTag(C.Tag.about_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.about_title).assertIsDisplayed().assertTextEquals(title)
    composeRule.onNodeWithTag(C.Tag.about_subtitle).assertTextEquals(subtitle)
    composeRule.onNodeWithTag(C.Tag.about_prompt_container).assertIsDisplayed()
    composeRule.onNodeWithText(placeholder).assertIsDisplayed()

    composeRule.onNodeWithTag(C.Tag.about_back).performClick()
    composeRule.onNodeWithTag(C.Tag.about_continue).performClick()

    composeRule.runOnIdle {
      Assert.assertEquals(1, backClicks)
      Assert.assertEquals(1, continueClicks)
    }
  }

  @Test
  fun typingAboutUpdatesState() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        OrganizationDescriptionScreen(
            about = text, onAboutChange = { text = it }, onBackClick = {}, onContinueClick = {})
      }
    }

    val input = "We organize weekly meetups"
    composeRule.onNodeWithTag(C.Tag.about_input).performTextInput(input)
    composeRule.onNodeWithTag(C.Tag.about_input).assertTextEquals(input)
  }

  @Test
  fun placeholderDisappearsWhenAboutFilled() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        OrganizationDescriptionScreen(
            about = text, onAboutChange = { text = it }, onBackClick = {}, onContinueClick = {})
      }
    }

    val placeholder = composeRule.activity.getString(R.string.org_description_placeholder)
    composeRule.onNodeWithText(placeholder).assertIsDisplayed()

    composeRule.onNodeWithTag(C.Tag.about_input).performTextInput("Testing placeholder hide")

    composeRule.onNodeWithText(placeholder).assertDoesNotExist()
  }

  @Test
  fun placeholderReappearsWhenAboutCleared() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        OrganizationDescriptionScreen(
            about = text, onAboutChange = { text = it }, onBackClick = {}, onContinueClick = {})
      }
    }

    val placeholder = composeRule.activity.getString(R.string.org_description_placeholder)
    val field = composeRule.onNodeWithTag(C.Tag.about_input)

    field.performTextInput("Temporary text")
    field.assertTextEquals("Temporary text")

    field.performTextReplacement("")

    composeRule.onNodeWithText(placeholder).assertIsDisplayed()
  }

  @Test
  fun organizationDescriptionDisplaysNavigationElements() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        OrganizationDescriptionScreen(
            about = text, onAboutChange = { text = it }, onBackClick = {}, onContinueClick = {})
      }
    }

    val title = composeRule.activity.getString(R.string.org_description_title)
    val subtitle = composeRule.activity.getString(R.string.about_subtitle)
    val continueText = composeRule.activity.getString(R.string.button_continue)

    composeRule.onNodeWithTag(C.Tag.about_app_bar).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.about_back).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.about_title).assertTextEquals(title)
    composeRule.onNodeWithTag(C.Tag.about_subtitle).assertTextEquals(subtitle)

    composeRule.onNodeWithTag(C.Tag.about_continue).assertIsDisplayed()
    composeRule.onNodeWithText(continueText).assertIsDisplayed()
  }

  @Test
  fun organizationContentWithPrefilledTextStillDisplaysControls() {
    var latestAbout = ""

    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("Existing summary") }
        OrganizationDescriptionContent(
            about = text,
            onAboutChange = {
              text = it
              latestAbout = it
            },
            onBackClick = {},
            onContinueClick = {})
      }
    }

    composeRule.onNodeWithTag(C.Tag.about_input).assertTextEquals("Existing summary")

    val updatedText = "Existing summary updated"
    composeRule.onNodeWithTag(C.Tag.about_input).performTextReplacement(updatedText)

    composeRule.runOnIdle { Assert.assertEquals(updatedText, latestAbout) }
    composeRule.onNodeWithTag(C.Tag.about_input).assertTextEquals(updatedText)
  }

  @Test
  fun organizationDescriptionPropagatesAboutChange() {
    var latest: String? = null

    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        OrganizationDescriptionScreen(
            about = text,
            onAboutChange = {
              text = it
              latest = it
            },
            onBackClick = {},
            onContinueClick = {})
      }
    }

    val value = "Compose all the things"
    composeRule.onNodeWithTag(C.Tag.about_input).performTextReplacement(value)

    composeRule.runOnIdle { Assert.assertEquals(value, latest) }
  }

  @Test
  fun organizationContentInvokesAllCallbacks() {
    var back = 0
    var forward = 0

    composeRule.setContent {
      AppTheme {
        OrganizationDescriptionContent(
            about = "",
            onAboutChange = {},
            onBackClick = { back++ },
            onContinueClick = { forward++ })
      }
    }

    composeRule.onNodeWithTag(C.Tag.about_back).performClick()
    composeRule.onNodeWithTag(C.Tag.about_continue).performClick()

    composeRule.runOnIdle {
      Assert.assertEquals(1, back)
      Assert.assertEquals(1, forward)
    }
  }

  @Test
  fun organizationDescriptionDisplaysProvidedAbout() {
    composeRule.setContent {
      AppTheme {
        OrganizationDescriptionScreen(
            about = "Existing description",
            onAboutChange = {},
            onBackClick = {},
            onContinueClick = {})
      }
    }

    composeRule.onNodeWithTag(C.Tag.about_input).assertTextEquals("Existing description")
  }

  @Test
  fun organizationDescriptionHonorsModifier() {
    composeRule.setContent {
      AppTheme {
        OrganizationDescriptionScreen(
            about = "",
            onAboutChange = {},
            onBackClick = {},
            onContinueClick = {},
            modifier = Modifier.testTag("custom_root"))
      }
    }

    composeRule.onNodeWithTag("custom_root").assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.about_screen_container).assertIsDisplayed()
  }

  @Test
  fun organizationContentHonorsModifier() {
    composeRule.setContent {
      AppTheme {
        OrganizationDescriptionContent(
            about = "",
            onAboutChange = {},
            onBackClick = {},
            onContinueClick = {},
            modifier = Modifier.testTag("custom_content").padding(16.dp))
      }
    }

    composeRule.onNodeWithTag("custom_content").assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.about_screen_container).assertIsDisplayed()
  }

  @Test
  fun organizationDescription_defaultParametersWork() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        OrganizationDescriptionScreen(about = text, onAboutChange = { text = it })
      }
    }

    composeRule.onNodeWithTag(C.Tag.about_screen_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.about_back).performClick() // Should not crash
    composeRule.onNodeWithTag(C.Tag.about_continue).performClick() // Should not crash
  }

  @Test
  fun organizationDescription_textClearingRestoresPlaceholder() {
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("Initial text") }
        OrganizationDescriptionScreen(about = text, onAboutChange = { text = it })
      }
    }

    val placeholder = composeRule.activity.getString(R.string.org_description_placeholder)
    composeRule.onNodeWithTag(C.Tag.about_input).assertTextEquals("Initial text")
    composeRule.onNodeWithTag(C.Tag.about_input).performTextClearance()
    composeRule.onNodeWithText(placeholder).assertIsDisplayed()
  }

  @Test
  fun organizationDescription_multipleTextUpdates() {
    val textHistory = mutableListOf<String>()
    composeRule.setContent {
      AppTheme {
        var text by remember { mutableStateOf("") }
        OrganizationDescriptionScreen(
            about = text,
            onAboutChange = {
              text = it
              textHistory.add(it)
            })
      }
    }

    composeRule.onNodeWithTag(C.Tag.about_input).performTextInput("First")
    composeRule.onNodeWithTag(C.Tag.about_input).performTextInput(" Second")
    composeRule.onNodeWithTag(C.Tag.about_input).performTextInput(" Third")

    composeRule.runOnIdle {
      Assert.assertTrue(textHistory.contains("First"))
      Assert.assertTrue(textHistory.contains("First Second"))
      Assert.assertTrue(textHistory.contains("First Second Third"))
    }
  }

  @Test
  fun organizationContent_allComponentsVisible() {
    composeRule.setContent {
      AppTheme {
        OrganizationDescriptionContent(
            about = "Test description", onAboutChange = {}, onBackClick = {}, onContinueClick = {})
      }
    }

    composeRule.onNodeWithTag(C.Tag.about_app_bar).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.about_prompt_container).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.about_continue).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.about_input).assertIsDisplayed()
  }
}
