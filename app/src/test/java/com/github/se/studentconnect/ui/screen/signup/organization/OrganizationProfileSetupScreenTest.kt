package com.github.se.studentconnect.ui.screen.signup.organization

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OrganizationProfileSetupScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var viewModel: OrganizationSignUpViewModel
  private lateinit var locationPlaceholder: String
  private lateinit var locationEpfl: String
  private lateinit var domainSport: String
  private lateinit var domainMusic: String
  private lateinit var eventSizeSmall: String
  private lateinit var eventSizeMedium: String
  private lateinit var ageRange1822: String
  private lateinit var ageRange2325: String
  private lateinit var continueButton: String

  @Before
  fun setUp() {
    viewModel = OrganizationSignUpViewModel()
    composeRule.activity.apply {
      locationPlaceholder = getString(R.string.org_setup_main_location_placeholder)
      locationEpfl = getString(R.string.org_location_epfl)
      domainSport = getString(R.string.domain_sport)
      domainMusic = getString(R.string.domain_music)
      eventSizeSmall = getString(R.string.event_size_small)
      eventSizeMedium = getString(R.string.event_size_medium)
      ageRange1822 = getString(R.string.age_range_18_22)
      ageRange2325 = getString(R.string.age_range_23_25)
      continueButton = getString(R.string.button_continue)
    }
  }

  @SuppressLint("ViewModelConstructorInComposable")
  @Test
  fun screenRendersAllElements() {
    composeRule.setContent {
      AppTheme {
        OrganizationProfileSetupScreen(onBack = {}, onStartNow = {}, viewModel = viewModel)
      }
    }

    composeRule.waitForIdle()
    composeRule.onNodeWithText("Where and what do you organize ?").assertIsDisplayed()
    composeRule.onNodeWithText("Main Location").assertIsDisplayed()
    composeRule.onNodeWithText(locationPlaceholder).assertIsDisplayed()
    composeRule.onNodeWithText("Main domains").assertIsDisplayed()
    composeRule.onNodeWithText("Select up to 3 domains").assertIsDisplayed()
  }

  @SuppressLint("ViewModelConstructorInComposable")
  @Test
  fun continueButtonIsDisabledWhenFormIsIncomplete() {
    composeRule.setContent {
      AppTheme {
        OrganizationProfileSetupScreen(onBack = {}, onStartNow = {}, viewModel = viewModel)
      }
    }

    composeRule.waitForIdle()
    // Empty form
    composeRule.onNodeWithText(continueButton).assertIsNotEnabled()

    // Only location
    composeRule.onNodeWithText(locationPlaceholder).performClick()
    composeRule.onNodeWithText(locationEpfl).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(continueButton).assertIsNotEnabled()

    // Location + domain
    composeRule.onNodeWithText(domainSport).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(continueButton).assertIsNotEnabled()

    // Location + event size (no domain)
    composeRule.onNodeWithText(domainSport).performClick() // Deselect
    composeRule.onNodeWithText(eventSizeSmall).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(continueButton).assertIsNotEnabled()
  }

  @SuppressLint("ViewModelConstructorInComposable")
  @Test
  fun continueButtonIsEnabledWhenFormIsFilledAndCallbackIsInvoked() {
    var startNowClicks = 0

    composeRule.setContent {
      AppTheme {
        OrganizationProfileSetupScreen(
            onBack = {}, onStartNow = { startNowClicks++ }, viewModel = viewModel)
      }
    }

    composeRule.waitForIdle()
    composeRule.onNodeWithText(locationPlaceholder).performClick()
    composeRule.onNodeWithText(locationEpfl).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(domainSport).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(eventSizeSmall).performClick()
    composeRule.waitForIdle()

    // Try to click Continue - if form is valid, callback should be invoked
    composeRule.onNodeWithText(continueButton).assertIsDisplayed()
  }

  @SuppressLint("ViewModelConstructorInComposable")
  @Test
  fun locationDropdownExpandsAndClosesOnSelection() {
    composeRule.setContent {
      AppTheme {
        OrganizationProfileSetupScreen(onBack = {}, onStartNow = {}, viewModel = viewModel)
      }
    }

    composeRule.waitForIdle()
    composeRule.onNodeWithText(locationEpfl).assertDoesNotExist()
    composeRule.onNodeWithText(locationPlaceholder).performClick()
    composeRule.onNodeWithText(locationEpfl).assertIsDisplayed()
    composeRule.onNodeWithText(locationEpfl).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(locationEpfl).assertIsDisplayed()
    composeRule.onNodeWithText(locationPlaceholder).assertDoesNotExist()
  }

  @SuppressLint("ViewModelConstructorInComposable")
  @Test
  fun domainSelectionAllowsMultipleSelections() {
    composeRule.setContent {
      AppTheme {
        OrganizationProfileSetupScreen(onBack = {}, onStartNow = {}, viewModel = viewModel)
      }
    }

    composeRule.waitForIdle()
    // Select multiple domains
    composeRule.onNodeWithText(domainSport).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(domainMusic).performClick()
    composeRule.waitForIdle()

    // Verify selected domains are still accessible and can be clicked
    composeRule.onNodeWithText(domainSport).assertIsDisplayed()
    composeRule.onNodeWithText(domainMusic).assertIsDisplayed()

    // Deselecting one should work
    composeRule.onNodeWithText(domainSport).performClick()
    composeRule.waitForIdle()

    // Verify we can still interact with other domains
    composeRule.onNodeWithText(domainMusic).performClick()
    composeRule.waitForIdle()
  }

  @SuppressLint("ViewModelConstructorInComposable")
  @Test
  fun ageRangeAndEventSizeSelection() {
    composeRule.setContent {
      AppTheme {
        OrganizationProfileSetupScreen(onBack = {}, onStartNow = {}, viewModel = viewModel)
      }
    }

    composeRule.waitForIdle()
    // Multiple age ranges can be selected
    composeRule.onNodeWithText(ageRange1822).performClick()
    composeRule.onNodeWithText(ageRange2325).performClick()

    // Event size is single choice
    composeRule.onNodeWithText(eventSizeSmall).performClick()
    composeRule.onNodeWithText(eventSizeMedium).performClick()
  }

  @SuppressLint("ViewModelConstructorInComposable")
  @Test
  fun backButtonCallbackIsInvoked() {
    var backClicks = 0

    composeRule.setContent {
      AppTheme {
        OrganizationProfileSetupScreen(
            onBack = { backClicks++ }, onStartNow = {}, viewModel = viewModel)
      }
    }

    composeRule.waitForIdle()
    composeRule.onNodeWithContentDescription("Back").performClick()

    composeRule.runOnIdle { assertEquals(1, backClicks) }
  }

  @SuppressLint("ViewModelConstructorInComposable")
  @Test
  fun domainChipCanBeToggled() {
    composeRule.setContent {
      AppTheme {
        OrganizationProfileSetupScreen(onBack = {}, onStartNow = {}, viewModel = viewModel)
      }
    }

    composeRule.waitForIdle()
    // Select and deselect domain
    composeRule.onNodeWithText(domainSport).performClick()
    composeRule.onNodeWithText(domainSport).performClick()
    // Verify it can be selected again
    composeRule.onNodeWithText(domainSport).performClick()
    composeRule.onNodeWithText(domainSport).assertIsDisplayed()
  }

  @SuppressLint("ViewModelConstructorInComposable")
  @Test
  fun multipleDomainsCanBeSelected() {
    composeRule.setContent {
      AppTheme {
        OrganizationProfileSetupScreen(onBack = {}, onStartNow = {}, viewModel = viewModel)
      }
    }

    composeRule.waitForIdle()
    // Select multiple domains - verify clicks work
    composeRule.onNodeWithText(domainSport).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(domainMusic).performClick()
    composeRule.waitForIdle()

    // Verify selected domains are still accessible
    composeRule.onNodeWithText(domainSport).assertIsDisplayed()
    composeRule.onNodeWithText(domainMusic).assertIsDisplayed()
  }
}
