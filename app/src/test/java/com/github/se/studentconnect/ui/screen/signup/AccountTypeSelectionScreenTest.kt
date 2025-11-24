package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.screen.signup.organization.AccountTypeOption
import com.github.se.studentconnect.ui.screen.signup.organization.AccountTypeSelectionScreen
import com.github.se.studentconnect.ui.screen.signup.regularuser.SignUpViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AccountTypeSelectionScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var viewModel: SignUpViewModel
  private lateinit var regularTitle: String
  private lateinit var organizationTitle: String
  private lateinit var continueText: String
  private lateinit var regularFeatureStrings: List<String>
  private lateinit var organizationFeatureStrings: List<String>

  @Before
  fun setUp() {
    viewModel = SignUpViewModel()
    val activity = composeTestRule.activity
    regularTitle = activity.getString(R.string.account_type_regular_user)
    organizationTitle = activity.getString(R.string.account_type_organization)
    continueText = activity.getString(R.string.button_continue)
    regularFeatureStrings =
        listOf(
                R.string.account_type_regular_user_feature_events,
                R.string.account_type_regular_user_feature_friends,
                R.string.account_type_regular_user_feature_create_event)
            .map(activity::getString)
    organizationFeatureStrings =
        listOf(
                R.string.account_type_organization_feature_promote,
                R.string.account_type_organization_feature_analytics,
                R.string.account_type_organization_feature_hire,
                R.string.account_type_organization_feature_operations)
            .map(activity::getString)
  }

  private fun setScreen(onContinue: (AccountTypeOption) -> Unit = {}) {
    composeTestRule.setContent {
      MaterialTheme {
        AccountTypeSelectionScreen(viewModel = viewModel, onContinue = onContinue, onBack = {})
      }
    }
  }

  @Test
  fun initialState_showsBothAccountTypesAndDisabledContinue() {
    setScreen()

    composeTestRule.onNodeWithText(regularTitle, useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText(organizationTitle, useUnmergedTree = true).assertExists()

    composeTestRule.onNodeWithText(continueText).assertIsNotEnabled()
  }

  @Test
  fun selectingAccount_switchesCardsAndInvokesContinueCallback() {
    var continueInvocation: AccountTypeOption? = null
    setScreen { continueInvocation = it }

    // Select organization card
    composeTestRule.onNodeWithText(organizationTitle, useUnmergedTree = true).performClick()

    organizationFeatureStrings.forEach { feature ->
      composeTestRule.onNodeWithText(feature, useUnmergedTree = true).assertExists()
    }
    regularFeatureStrings.forEach { feature ->
      composeTestRule.onNodeWithText(feature, useUnmergedTree = true).assertDoesNotExist()
    }
    composeTestRule.onNodeWithText(continueText).assertIsEnabled().performClick()
    assertEquals(AccountTypeOption.Organization, continueInvocation)
    composeTestRule.runOnIdle {
      assertEquals(AccountTypeOption.Organization, viewModel.state.value.accountTypeSelection)
    }

    // Tap the collapsed regular card using its content description
    val regularCta =
        composeTestRule.activity.getString(R.string.content_description_select_regular_user)
    regularFeatureStrings.forEach { feature ->
      composeTestRule.onNodeWithText(feature, useUnmergedTree = true).assertDoesNotExist()
    }
    composeTestRule.onNodeWithContentDescription(regularCta).performClick()

    regularFeatureStrings.forEach { feature ->
      composeTestRule.onNodeWithText(feature, useUnmergedTree = true).assertExists()
    }
    organizationFeatureStrings.forEach { feature ->
      composeTestRule.onNodeWithText(feature, useUnmergedTree = true).assertDoesNotExist()
    }
    composeTestRule.runOnIdle {
      assertEquals(AccountTypeOption.RegularUser, viewModel.state.value.accountTypeSelection)
    }
  }
}
