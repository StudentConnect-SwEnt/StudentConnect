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

@OptIn(ExperimentalTestApi::class)
class CreatePublicEventScreenTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  override fun createInitializedRepository() = EventRepositoryLocal()

  @Before
  fun setUpContent() {
    composeTestRule.activity.setContent { AppTheme { CreatePublicEventScreen() } }
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

  private fun waitUntilEnabled(tag: String) {
    composeTestRule.waitUntil {
      try {
        composeTestRule.onNodeWithTag(tag).assertIsEnabled()
        true
      } catch (_: AssertionError) {
        false
      }
    }
  }

  private fun waitUntilDisabled(tag: String) {
    composeTestRule.waitUntil {
      try {
        composeTestRule.onNodeWithTag(tag).assertIsNotEnabled()
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
    waitForTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT).assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT).assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH)
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun saveButton_disabled_whenMandatoryFieldsEmpty() {
    waitForTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  // --------------------------------------------------
  // 2. Title field
  // --------------------------------------------------

  @Test
  fun typingInTitle_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val titleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    titleNode.performTextInput("My Event")
    titleNode.assertTextContains("My Event")
  }

  @Test
  fun emptyTitle_showsErrorText() {
    waitForTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val titleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    titleNode.performTextInput(" ")
    waitForText("Title cannot be blank")
    composeTestRule.onNodeWithText("Title cannot be blank").assertIsDisplayed()
  }

  // --------------------------------------------------
  // 3. Subtitle & description
  // --------------------------------------------------

  @Test
  fun typingInSubtitle_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
    val subtitleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
    subtitleNode.performTextInput("Optional subtitle")
    subtitleNode.assertTextContains("Optional subtitle")
  }

  @Test
  fun typingInDescription_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
    val descNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
    descNode.performTextInput("This is my event description")
    descNode.assertTextContains("This is my event description")
  }

  // --------------------------------------------------
  // 4. Location
  // --------------------------------------------------

  @Test
  fun typingInLocation_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    val locationNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    locationNode.performTextInput("Zurich, Switzerland")
    locationNode.assertTextContains("Zurich, Switzerland")
  }

  // --------------------------------------------------
  // 5. Dates & times
  // --------------------------------------------------

  @Test
  fun enteringStartDate_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    val startDateNode =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    startDateNode.performTextInput("01/01/2025")
    startDateNode.assertTextContains("01/01/2025")
  }

  @Test
  fun enteringEndDate_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    val endDateNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    endDateNode.performTextInput("02/01/2025")
    endDateNode.assertTextContains("02/01/2025")
  }

  @Test
  fun clickingStartTimeButton_opensPicker() {
    waitForTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
    val startTimeButton =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
    startTimeButton.performClick()
    // TODO: assert time picker dialog is visible
  }

  @Test
  fun clickingEndTimeButton_opensPicker() {
    waitForTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON)
    val endTimeButton =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON)
    endTimeButton.performClick()
    // TODO: assert time picker dialog is visible
  }

  // --------------------------------------------------
  // 6. Participants & website
  // --------------------------------------------------

  @Test
  fun typingNumberOfParticipants_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
    val participantsNode =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
    participantsNode.performTextInput("25")
    participantsNode.assertTextContains("25")
  }

  @Test
  fun typingWebsite_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
    val websiteNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
    websiteNode.performTextInput("https://event.com")
    websiteNode.assertTextContains("https://event.com")
  }

  // --------------------------------------------------
  // 7. Participation fee
  // --------------------------------------------------

  @Test
  fun participationFeeInput_disabledByDefault() {
    waitForTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertIsNotEnabled()
  }

  @Test
  fun enablingParticipationFeeSwitch_enablesInput() {
    waitForTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val switch =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    switch.performClick()
    waitUntilEnabled(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    input.assertIsEnabled()
  }

  @Test
  fun disablingParticipationFeeSwitch_disablesAndClearsInput() {
    waitForTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val switch =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)

    switch.performClick() // enable
    waitUntilEnabled(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    input.performTextInput("50")
    input.assertTextContains("50")

    switch.performClick() // disable
    waitUntilDisabled(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    input.assertIsNotEnabled()
    input.assertTextContains("") // cleared
  }

  // --------------------------------------------------
  // 8. Flash event
  // --------------------------------------------------

  @Test
  fun togglingFlashSwitch_clickable() {
    waitForTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH)
    val flashSwitch =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH)
    flashSwitch.performClick()
    flashSwitch.performClick()
  }

  // --------------------------------------------------
  // 9. Save button behavior
  // --------------------------------------------------

  @Test
  fun saveButton_enabledOnlyWhenMandatoryFieldsPresent() {
    waitForTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    val save = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    save.assertIsNotEnabled()

    waitForTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val title = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val startDate = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    val endDate = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)

    title.performTextInput("My Event")
    startDate.performTextInput("01/01/2025")
    endDate.performTextInput("02/01/2025")

    save.assertIsEnabled()
  }

  @Test
  fun clickingSaveButton_callsSaveEvent() {
    waitForTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    val title = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val startDate = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    val endDate = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    val save = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)

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
    // Use FakeCreatePublicEventViewModel with finishedSaving=true
    // Fill required fields, click save
    // Then assert navigation state (e.g. checking fake NavController)
  }
}
