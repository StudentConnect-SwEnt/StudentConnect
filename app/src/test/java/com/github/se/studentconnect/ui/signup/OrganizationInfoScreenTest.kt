package com.github.se.studentconnect.ui.signup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationInfoScreen
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationInfoScreenTestTags
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

    // select and then deselect chip via test tag
    composeRule.onNodeWithText(assoc).performClick()
    // ensure CTA enabled
    composeRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).assertIsEnabled()

    // click again to deselect
    composeRule.onNodeWithText(assoc).performClick()
    // CTA should be disabled because no type selected
    composeRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun backButton_invokes_onBack() {
    var backClicked = false
    composeRule.setContent {
      AppTheme {
        OrganizationInfoScreen(
            onContinue = {},
            onBack = { backClicked = true },
            avatarResIds = listOf(R.drawable.avatar_12))
      }
    }

    val backDesc = composeRule.activity.getString(R.string.content_description_back)
    composeRule.onNodeWithContentDescription(backDesc).performClick()
    composeRule.runOnIdle { Assert.assertTrue(backClicked) }
  }
}
