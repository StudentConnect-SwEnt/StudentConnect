package com.github.se.studentconnect.ui.signup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.screen.signup.OrganizationInfoScreen
import com.github.se.studentconnect.ui.screen.signup.OrganizationInfoScreenTestTags
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OrganizationInfoScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun organizationScreenInitialState_and_cta_enables_on_input_and_chip() {
    var continued = false
    composeRule.setContent {
      AppTheme {
        OrganizationInfoScreen(
            onContinue = { continued = true },
            onBack = {},
            avatarResIds = listOf(R.drawable.avatar_12))
      }
    }

    // Title and subtitle visible
    composeRule
        .onNodeWithText(composeRule.activity.getString(R.string.instruction_who_are_you))
        .assertIsDisplayed()
    composeRule
        .onNodeWithText(composeRule.activity.getString(R.string.instruction_who_are_you_subtitle))
        .assertIsDisplayed()

    // Org name input visible
    composeRule.onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT).assertIsDisplayed()

    // Chip exists (Association)
    val assoc = composeRule.activity.getString(R.string.organization_type_association)
    composeRule.onNodeWithText(assoc).assertIsDisplayed()

    // Continue button initially disabled
    composeRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).assertIsNotEnabled()

    // Enter organization name and select chip
    composeRule
        .onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT)
        .performTextInput("ACME")
    // Click Association chip via its text
    composeRule.onNodeWithText(assoc).performClick()

    // Now the continue button should be enabled
    composeRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).assertIsEnabled()

    // Click continue
    composeRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).performClick()
    composeRule.runOnIdle { Assert.assertTrue(continued) }
  }

  @Test
  fun chipSelection_toggles_off_and_cta_disables_when_no_selection() {
    composeRule.setContent {
      AppTheme {
        OrganizationInfoScreen(
            onContinue = {}, onBack = {}, avatarResIds = listOf(R.drawable.avatar_12))
      }
    }

    val assoc = composeRule.activity.getString(R.string.organization_type_association)
    // Enter org name
    composeRule.onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT).performTextInput("Org")

    // select and then deselect chip
    composeRule.onNodeWithText(assoc).performClick()
    // ensure CTA enabled
    composeRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).assertIsEnabled()

    // click again to deselect
    composeRule.onNodeWithText(assoc).performClick()
    // CTA should be disabled because no type selected
    composeRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).assertIsNotEnabled()
  }
}
