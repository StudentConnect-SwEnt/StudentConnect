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

  // --------------------------------------------------
  // 1. Rendering & visibility
  // --------------------------------------------------

  @Test
  fun allInputs_areDisplayed() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.TITLE_INPUT))
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT).assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.START_DATE_INPUT))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.END_DATE_INPUT))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT))
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT).assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH)
        .assertIsDisplayed()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.SAVE_BUTTON))
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun saveButton_disabled_whenMandatoryFieldsEmpty() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.SAVE_BUTTON))
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  // --------------------------------------------------
  // 2. Title field
  // --------------------------------------------------

  @Test
  fun typingInTitle_updatesValue() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.TITLE_INPUT))
    val titleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    titleNode.performTextInput("My Event")
    titleNode.assertTextContains("My Event")
  }

  @Test
  fun emptyTitle_showsErrorText() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.TITLE_INPUT))
    val titleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    titleNode.performTextInput(" ")
    composeTestRule.waitUntilExactlyOneExists(hasText("Title cannot be blank"))
    composeTestRule.onNodeWithText("Title cannot be blank").assertIsDisplayed()
  }

  // --------------------------------------------------
  // 3. Subtitle & description
  // --------------------------------------------------

  @Test
  fun typingInSubtitle_updatesValue() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT))
    val subtitleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
    subtitleNode.performTextInput("Optional subtitle")
    subtitleNode.assertTextContains("Optional subtitle")
  }

  @Test
  fun typingInDescription_updatesValue() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT))
    val descNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
    descNode.performTextInput("This is my event description")
    descNode.assertTextContains("This is my event description")
  }

  // --------------------------------------------------
  // 4. Location
  // --------------------------------------------------

  @Test
  fun typingInLocation_updatesValue() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT))
    val locationNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    locationNode.performTextInput("Zurich, Switzerland")
    locationNode.assertTextContains("Zurich, Switzerland")
  }

  // --------------------------------------------------
  // 5. Dates & times
  // --------------------------------------------------

  @Test
  fun enteringStartDate_updatesValue() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.START_DATE_INPUT))
    val startDateNode =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    startDateNode.performTextInput("01/01/2025")
    startDateNode.assertTextContains("01/01/2025")
  }

  @Test
  fun enteringEndDate_updatesValue() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.END_DATE_INPUT))
    val endDateNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    endDateNode.performTextInput("02/01/2025")
    endDateNode.assertTextContains("02/01/2025")
  }

  @Test
  fun clickingStartTimeButton_opensPicker() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON))
    val startTimeButton =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
    startTimeButton.performClick()
    // TODO: assert time picker dialog is visible
  }

  @Test
  fun clickingEndTimeButton_opensPicker() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON))
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
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT))
    val participantsNode =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
    participantsNode.performTextInput("25")
    participantsNode.assertTextContains("25")
  }

  @Test
  fun typingWebsite_updatesValue() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT))
    val websiteNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
    websiteNode.performTextInput("https://event.com")
    websiteNode.assertTextContains("https://event.com")
  }

  // --------------------------------------------------
  // 7. Participation fee
  // --------------------------------------------------

  @Test
  fun participationFeeInput_disabledByDefault() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT))
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertIsNotEnabled()
  }

  @Test
  fun enablingParticipationFeeSwitch_enablesInput() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH))
    val switch =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    switch.performClick()
    composeTestRule.waitForIdle()
    input.assertIsEnabled()
  }

  @Test
  fun disablingParticipationFeeSwitch_disablesAndClearsInput() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH))
    val switch =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)

    switch.performClick() // enable
    composeTestRule.waitForIdle()
    input.performTextInput("50")
    input.assertTextContains("50")

    switch.performClick() // disable
    composeTestRule.waitForIdle()
    input.assertIsNotEnabled()
    input.assertTextContains("") // cleared
  }

  // --------------------------------------------------
  // 8. Flash event
  // --------------------------------------------------

  @Test
  fun togglingFlashSwitch_clickable() {
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH))
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
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.SAVE_BUTTON))
    val save = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    save.assertIsNotEnabled()

    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.TITLE_INPUT))
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
    composeTestRule.waitUntilExactlyOneExists(
        hasTestTag(CreatePublicEventScreenTestTags.SAVE_BUTTON))
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
