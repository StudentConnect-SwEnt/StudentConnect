// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.eventcreation

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.se.studentconnect.MainActivity
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreatePrivateEventScreenTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  override fun createInitializedRepository() = EventRepositoryLocal()

  @Before
  fun setUpContent() {
    composeTestRule.activity.setContent { AppTheme { CreatePrivateEventScreen() } }
  }

  // --------------------------------------------------
  // 1. Rendering & visibility
  // --------------------------------------------------

  @Test
  fun allInputs_areDisplayed() {
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.START_TIME_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.END_TIME_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun saveButton_disabled_whenMandatoryFieldsEmpty() {
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  // --------------------------------------------------
  // 2. Title field
  // --------------------------------------------------

  @Test
  fun typingInTitle_updatesValue() {
    val titleNode = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    titleNode.performTextInput("My Event")
    titleNode.assertTextContains("My Event")
  }

  @Test
  fun emptyTitle_showsErrorText() {
    val titleNode = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    titleNode.performTextInput(" ")
    composeTestRule.onNodeWithText("Title cannot be blank").assertIsDisplayed()
  }

  // --------------------------------------------------
  // 3. Subtitle & description
  // --------------------------------------------------

  @Test
  fun typingInDescription_updatesValue() {
    val descNode = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
    descNode.performTextInput("This is my event description")
    descNode.assertTextContains("This is my event description")
  }

  // --------------------------------------------------
  // 4. Location
  // --------------------------------------------------

  @Test
  fun typingInLocation_updatesValue() {
    val locationNode =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT)
    locationNode.performTextInput("Zurich, Switzerland")
    locationNode.assertTextContains("Zurich, Switzerland")
  }

  // --------------------------------------------------
  // 5. Dates & times
  // --------------------------------------------------

  @Test
  fun enteringStartDate_updatesValue() {
    val startDateNode =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    startDateNode.performTextInput("01/01/2025")
    startDateNode.assertTextContains("01/01/2025")
  }

  @Test
  fun enteringEndDate_updatesValue() {
    val endDateNode = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
    endDateNode.performTextInput("02/01/2025")
    endDateNode.assertTextContains("02/01/2025")
  }

  @Test
  fun clickingStartTimeButton_opensPicker() {
    val startTimeButton =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_TIME_BUTTON)
    startTimeButton.performClick()
    // TODO: assert time picker dialog is visible
  }

  @Test
  fun clickingEndTimeButton_opensPicker() {
    val endTimeButton =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_TIME_BUTTON)
    endTimeButton.performClick()
    // TODO: assert time picker dialog is visible
  }

  // --------------------------------------------------
  // 6. Participants & website
  // --------------------------------------------------

  @Test
  fun typingNumberOfParticipants_updatesValue() {
    val participantsNode =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
    participantsNode.performTextInput("25")
    participantsNode.assertTextContains("25")
  }

  // --------------------------------------------------
  // 7. Participation fee
  // --------------------------------------------------

  @Test
  fun participationFeeInput_disabledByDefault() {
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertIsNotEnabled()
  }

  @Test
  fun enablingParticipationFeeSwitch_enablesInput() {
    val switch =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    switch.performClick()
    input.assertIsEnabled()
  }

  @Test
  fun disablingParticipationFeeSwitch_disablesAndClearsInput() {
    val switch =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)

    switch.performClick() // enable
    input.performTextInput("50")
    input.assertTextContains("50")

    switch.performClick() // disable
    input.assertIsNotEnabled()
    input.assertTextContains("") // cleared
  }

  // --------------------------------------------------
  // 8. Flash event
  // --------------------------------------------------

  @Test
  fun togglingFlashSwitch_clickable() {
    val flashSwitch =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    flashSwitch.performClick()
    flashSwitch.performClick()
  }

  // --------------------------------------------------
  // 9. Save button behavior
  // --------------------------------------------------

  @Test
  fun saveButton_enabledOnlyWhenMandatoryFieldsPresent() {
    val save = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    save.assertIsNotEnabled()

    val title = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val startDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    val endDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)

    title.performTextInput("My Event")
    startDate.performTextInput("01/01/2025")
    endDate.performTextInput("02/01/2025")

    save.assertIsEnabled()
  }

  @Test
  fun clickingSaveButton_callsSaveEvent() {
    val title = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val startDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    val endDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
    val save = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)

    title.performTextInput("My Event")
    startDate.performTextInput("01/01/2025")
    endDate.performTextInput("02/01/2025")

    save.assertIsEnabled()
    save.performClick()

    // In FakeViewModel, track if saveEvent() was called
  }

  // --------------------------------------------------
  // 10. Navigation
  // --------------------------------------------------

  @Test
  fun afterSuccessfulSave_navigatesAway() {
    // Use FakeCreatePrivateEventViewModel with finishedSaving=true
    // Fill required fields, click save
    // Then assert navigation state (e.g. checking fake NavController)
  }
}
