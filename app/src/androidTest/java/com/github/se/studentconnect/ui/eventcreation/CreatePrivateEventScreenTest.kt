// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class CreatePrivateEventScreenTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @OptIn(ExperimentalMaterial3Api::class)
  @Before
  fun setUpContent() {
    EventRepositoryProvider.overrideForTests(EventRepositoryLocal())
    composeTestRule.setContent { AppTheme { CreatePrivateEventScreen(navController = null) } }
  }

  private fun waitForTag(tag: String) {
    composeTestRule.waitUntil {
      composeTestRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun waitForText(text: String) {
    composeTestRule.waitUntil {
      composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  // --------------------------------------------------
  // 1. Rendering & visibility
  // --------------------------------------------------

  @Test
  fun allInputs_areDisplayed() {
    waitForTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.START_TIME_BUTTON)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.START_TIME_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.END_TIME_BUTTON)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.END_TIME_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  // --------------------------------------------------
  // 2. Scaffold & TopAppBar (Shared Shell)
  // --------------------------------------------------

  @Test
  fun scaffold_isDisplayed() {
    waitForTag(CreatePrivateEventScreenTestTags.SCAFFOLD)
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SCAFFOLD).assertIsDisplayed()
  }

  @Test
  fun topAppBar_isDisplayed() {
    waitForTag(CreatePrivateEventScreenTestTags.TOP_APP_BAR)
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TOP_APP_BAR).assertIsDisplayed()
  }

  @Test
  fun topAppBar_showsCorrectTitle() {
    waitForTag(CreatePrivateEventScreenTestTags.TOP_APP_BAR)
    // Checks for "Create Private Event"
    composeTestRule.onNodeWithText("Create Private Event").assertIsDisplayed()
  }

  // --------------------------------------------------
  // 3. Required Field Indicators (Asterisks)
  // --------------------------------------------------

  @Test
  fun titleField_showsRequiredIndicator() {
    waitForTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT).performScrollTo()
    composeTestRule.onNodeWithText("Title *", substring = true).assertExists()
  }

  @Test
  fun startDateField_showsRequiredIndicator() {
    waitForTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
        .performScrollTo()
    composeTestRule.onNodeWithText("Start of the event *", substring = true).assertExists()
  }

  @Test
  fun endDateField_showsRequiredIndicator() {
    waitForTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT).performScrollTo()
    composeTestRule.onNodeWithText("End of the event *", substring = true).assertExists()
  }

  @Test
  fun optionalFields_doNotShowRequiredIndicator() {
    waitForTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
        .performScrollTo()
    // Should show "Description" without asterisk
    composeTestRule.onNodeWithText("Description", substring = true).assertExists()
    composeTestRule.onNodeWithText("Description *", substring = true).assertDoesNotExist()
  }

  // --------------------------------------------------
  // 4. Input Logic
  // --------------------------------------------------

  @Test
  fun typingInTitle_updatesValue() {
    waitForTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val titleNode = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    titleNode.performScrollTo()
    titleNode.performTextInput("Private Party")
    titleNode.assertTextContains("Private Party")
  }

  @Test
  fun emptyTitle_showsErrorText() {
    waitForTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val titleNode = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    titleNode.performScrollTo()
    titleNode.performTextInput(" ")
    waitForText("Title cannot be blank")
    composeTestRule.onNodeWithText("Title cannot be blank").assertIsDisplayed()
  }

  @Test
  fun typingInLocation_updatesValue() {
    waitForTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT)
    val locationNode =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT)
    locationNode.performScrollTo()
    locationNode.performTextInput("Secret Base")
    locationNode.assertTextContains("Secret Base")
  }

  // --------------------------------------------------
  // 5. Save Button Logic (Floating)
  // --------------------------------------------------

  @Test
  fun saveButton_isVisibleAtTopOfScreen() {
    waitForTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun saveButton_remainsVisibleAfterScrolling() {
    waitForTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
        .performScrollTo()

    waitForTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun saveButton_disabled_whenMandatoryFieldsEmpty() {
    waitForTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    val save = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    save.assertIsNotEnabled()
  }

  @Test
  fun saveButton_enabled_whenMandatoryFieldsFilled() {
    waitForTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val title = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val startDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    val endDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
    val save = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)

    title.performScrollTo()
    title.performTextInput("Secret Meeting")

    startDate.performScrollTo()
    startDate.performTextInput("01/01/2026")

    endDate.performScrollTo()
    endDate.performTextInput("02/01/2026")

    // The button should now be enabled
    save.assertIsEnabled()
  }

  @Test
  fun flashEventToggle_whenEnabled_showsDurationFields() {
    waitForTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    val flashSwitch =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    flashSwitch.performScrollTo()
    flashSwitch.performClick()

    // Wait for duration fields to appear
    waitForTag(CreatePrivateEventScreenTestTags.FLASH_DURATION_HOURS)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_DURATION_HOURS)
        .performScrollTo()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_DURATION_MINUTES)
        .performScrollTo()
        .assertIsDisplayed()

    // Date/time fields should not be visible
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
        .assertDoesNotExist()
  }

  @Test
  fun flashEventToggle_whenDisabled_showsDateTimeFields() {
    waitForTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    val flashSwitch =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    flashSwitch.performScrollTo()

    // Ensure flash is disabled (default state)
    // Duration fields should not be visible
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_DURATION_HOURS)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_DURATION_MINUTES)
        .assertDoesNotExist()

    // Date/time fields should be visible
    waitForTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun saveButton_enabled_whenFlashEventWithValidDuration() {
    waitForTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    val flashSwitch =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    flashSwitch.performScrollTo()
    flashSwitch.performClick()

    waitForTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val title = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    title.performScrollTo()
    title.performTextInput("Flash Event")

    // Flash event with default duration (1h 0m) should enable save button
    waitForTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON).assertIsEnabled()
  }

  @Test
  fun flashDurationFields_areDisplayedWhenFlashEnabled() {
    waitForTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
        .performScrollTo()
        .performClick()

    waitForTag(CreatePrivateEventScreenTestTags.FLASH_DURATION_HOURS)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_DURATION_HOURS)
        .performScrollTo()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_DURATION_MINUTES)
        .performScrollTo()
        .assertIsDisplayed()
  }

  // ========== NEW TESTS FOR ORGANIZATION SUPPORT ==========

  @Test
  fun organizationSwitch_notDisplayedWhenNoOrganizations() {
    // Default setup has no organizations loaded (async loading in ViewModel init)
    // The organization switch only appears when userOrganizations list is not empty
    composeTestRule.waitForIdle()

    // Switch should not exist when user owns no organizations
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.CREATE_AS_ORG_SWITCH)
        .assertDoesNotExist()
  }

  @Test
  fun organizationDropdown_notDisplayedByDefault() {
    // Dropdown only appears when createAsOrganization is true
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.SELECT_ORG_DROPDOWN)
        .assertDoesNotExist()
  }

  @Test
  fun organizationFields_testTagsAreDefinedCorrectly() {
    // This test verifies that the test tags are properly defined
    // Even if the UI elements don't exist, the constants should be accessible
    val switchTag = CreatePrivateEventScreenTestTags.CREATE_AS_ORG_SWITCH
    val dropdownTag = CreatePrivateEventScreenTestTags.SELECT_ORG_DROPDOWN

    // Verify tags are non-empty strings
    assert(switchTag.isNotEmpty())
    assert(dropdownTag.isNotEmpty())
  }
}
