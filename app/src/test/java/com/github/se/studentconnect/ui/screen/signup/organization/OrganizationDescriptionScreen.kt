package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.resources.C
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class OrganizationDescriptionScreen {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun organizationDescriptionScreen_displaysCorrectly_andUpdatesViewModel() {
    val viewModel = OrganizationSignUpViewModel()
    var backClicked = false
    var continueClicked = false

    composeTestRule.setContent {
      OrganizationDescriptionScreen(
          viewModel = viewModel,
          onBack = { backClicked = true },
          onContinue = { continueClicked = true })
    }

    // Check if elements are displayed
    composeTestRule.onNodeWithTag(C.Tag.about_screen_container).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.about_title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.about_input).assertIsDisplayed()

    // Test input
    val testDescription = "This is a test description."
    composeTestRule.onNodeWithTag(C.Tag.about_input).performTextInput(testDescription)
    assertEquals(testDescription, viewModel.state.value.description)

    // Test buttons
    composeTestRule.onNodeWithTag(C.Tag.about_back).performClick()
    assert(backClicked)

    composeTestRule.onNodeWithTag(C.Tag.about_continue).performClick()
    assert(continueClicked)
  }
}
