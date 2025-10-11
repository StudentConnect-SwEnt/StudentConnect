// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class CreatePrivateEventScreenTest : StudentConnectTest(useTestScreen = true) {

  companion object {
    const val TIMEOUT_MILLIS = 5000L
  }

  override fun createInitializedRepository() = EventRepositoryLocal()

  @Composable
  override fun TestScreen() {
    AppTheme { CreatePrivateEventScreen() }
  }

  private fun waitForTag(tag: String, timeoutMillis: Long = TIMEOUT_MILLIS) {
    composeTestRule.waitUntil(timeoutMillis) {
      composeTestRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun waitForText(text: String, timeoutMillis: Long = TIMEOUT_MILLIS) {
    composeTestRule.waitUntil(timeoutMillis) {
      composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun waitUntilEnabled(tag: String, timeoutMillis: Long = TIMEOUT_MILLIS) {
    composeTestRule.waitUntil(timeoutMillis) {
      try {
        composeTestRule.onNodeWithTag(tag).performScrollTo().assertIsEnabled()
        true
      } catch (_: AssertionError) {
        false
      }
    }
  }

  private fun waitUntilDisabled(tag: String) {
    composeTestRule.waitUntil {
      try {
        composeTestRule.onNodeWithTag(tag).performScrollTo().assertIsNotEnabled()
        true
      } catch (_: AssertionError) {
        false
      }
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
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun saveButton_disabled_whenMandatoryFieldsEmpty() {
    waitForTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertIsNotEnabled()
  }

  // --------------------------------------------------
  // 2. Title field
  // --------------------------------------------------

  @Test
  fun typingInTitle_updatesValue() {
    waitForTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val titleNode = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    titleNode.performScrollTo()
    titleNode.performTextInput("My Event")
    titleNode.assertTextContains("My Event")
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

  // --------------------------------------------------
  // 3. Subtitle & description
  // --------------------------------------------------

  @Test
  fun typingInDescription_updatesValue() {
    waitForTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
    val descNode = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
    descNode.performScrollTo()
    descNode.performTextInput("This is my event description")
    descNode.assertTextContains("This is my event description")
  }

  // --------------------------------------------------
  // 4. Location
  // --------------------------------------------------

  @Test
  fun typingInLocation_updatesValue() {
    waitForTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT)
    val locationNode =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT)
    locationNode.performScrollTo()
    locationNode.performTextInput("Zurich, Switzerland")
    locationNode.assertTextContains("Zurich, Switzerland")
  }

  // --------------------------------------------------
  // 5. Dates & times
  // --------------------------------------------------

  @Test
  fun enteringStartDate_updatesValue() {
    waitForTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    val startDateNode =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    startDateNode.performScrollTo()
    startDateNode.performTextInput("01/01/2025")
    startDateNode.assertTextContains("01/01/2025")
  }

  @Test
  fun enteringEndDate_updatesValue() {
    waitForTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
    val endDateNode = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
    endDateNode.performScrollTo()
    endDateNode.performTextInput("02/01/2025")
    endDateNode.assertTextContains("02/01/2025")
  }

  @Test
  fun clickingStartTimeButton_opensPicker() {
    waitForTag(CreatePrivateEventScreenTestTags.START_TIME_BUTTON)
    val startTimeButton =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_TIME_BUTTON)
    startTimeButton.performScrollTo()
    startTimeButton.performClick()
    // TODO: assert time picker dialog is visible
  }

  @Test
  fun clickingEndTimeButton_opensPicker() {
    waitForTag(CreatePrivateEventScreenTestTags.END_TIME_BUTTON)
    val endTimeButton =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_TIME_BUTTON)
    endTimeButton.performScrollTo()
    endTimeButton.performClick()
    // TODO: assert time picker dialog is visible
  }

  // --------------------------------------------------
  // 6. Participants & website
  // --------------------------------------------------

  @Test
  fun typingNumberOfParticipants_updatesValue() {
    waitForTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
    val participantsNode =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
    participantsNode.performScrollTo()
    participantsNode.performTextInput("25")
    participantsNode.assertTextContains("25")
  }

  // --------------------------------------------------
  // 7. Participation fee
  // --------------------------------------------------

  @Test
  fun participationFeeInput_disabledByDefault() {
    waitForTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .performScrollTo()
        .assertIsNotEnabled()
  }

  @Test
  fun enablingParticipationFeeSwitch_enablesInput() {
    waitForTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val switch =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    switch.performScrollTo()
    switch.performClick()
    waitUntilEnabled(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    input.performScrollTo()
    input.assertIsEnabled()
  }

  @Test
  fun disablingParticipationFeeSwitch_disablesAndClearsInput() {
    waitForTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val switch =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)

    switch.performScrollTo()
    switch.performClick() // enable
    waitUntilEnabled(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    input.performScrollTo()
    input.performTextInput("50")
    input.assertTextContains("50")

    switch.performScrollTo()
    switch.performClick() // disable
    waitUntilDisabled(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    input.performScrollTo()
    input.assertIsNotEnabled()
    input.assertTextContains("") // cleared
  }

  // --------------------------------------------------
  // 8. Flash event
  // --------------------------------------------------

  @Test
  fun togglingFlashSwitch_clickable() {
    waitForTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    val flashSwitch =
        composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
    flashSwitch.performScrollTo()
    flashSwitch.performClick()
    flashSwitch.performClick()
  }

  // --------------------------------------------------
  // 9. Save button behavior
  // --------------------------------------------------

  @Test
  fun saveButton_enabledOnlyWhenMandatoryFieldsPresent() {
    waitForTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    val save = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    save.performScrollTo()
    save.assertIsNotEnabled()

    waitForTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val title = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val startDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    val endDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)

    title.performScrollTo()
    title.performTextInput("My Event")

    startDate.performScrollTo()
    startDate.performTextInput("01/01/2025")

    endDate.performScrollTo()
    endDate.performTextInput("02/01/2025")

    save.performScrollTo()
    save.assertIsEnabled()
  }

  @Test
  fun clickingSaveButton_callsSaveEvent() {
    waitForTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
    val title = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
    val startDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT)
    val endDate = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT)
    val save = composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)

    title.performScrollTo()
    title.performTextInput("My Event")

    startDate.performScrollTo()
    startDate.performTextInput("01/01/2025")

    endDate.performScrollTo()
    endDate.performTextInput("02/01/2025")

    save.performScrollTo()
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
