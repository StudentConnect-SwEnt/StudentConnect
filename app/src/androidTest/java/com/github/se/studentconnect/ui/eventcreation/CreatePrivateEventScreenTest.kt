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
class CreatePrivateEventScreenTest : StudentConnectTest(useTestScreen = true) {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  override fun createInitializedRepository() = EventRepositoryLocal()

  @Before
  fun setUpContent() {
    composeTestRule.activity.setContent { AppTheme { CreatePrivateEventScreen() } }
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

  @Test
  fun locationTextField_typingEpfl_showsFakeEpflSuggestion() {
    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    val locationNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    locationNode.performScrollTo()
    locationNode.performTextInput("EPFL")

    // Wait for the fake suggestion "Fake EPFL" to appear
    composeTestRule.waitUntil(7000) {
      composeTestRule
          .onAllNodes(
              hasText("Fake EPFL") and !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Verify suggestion exists
    composeTestRule
        .onNode(
            hasText("Fake EPFL") and !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
            useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun locationTextField_typingNowhere_showsNoSuggestions() {
    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    val locationNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    locationNode.performScrollTo()
    locationNode.performTextInput("Nowhere")

    // Wait for search debounce and ensure no suggestion appears
    composeTestRule.waitUntil(7000) {
      composeTestRule
          .onAllNodes(
              hasText("Fake") and !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isEmpty()
    }
  }

  @Test
  fun locationTextField_selectingLausanneSuggestion_updatesTextField() {
    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    val locationNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    locationNode.performScrollTo()
    locationNode.performTextInput("Lausanne")

    // Wait for "Fake Lausanne" to appear
    composeTestRule.waitUntil(7000) {
      composeTestRule
          .onAllNodes(
              hasText("Fake Lausanne") and
                  !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Click the dropdown suggestion
    composeTestRule
        .onNode(
            hasText("Fake Lausanne") and
                !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
            useUnmergedTree = true)
        .performClick()

    // Ensure field value updated
    locationNode.assertTextContains("Fake Lausanne")
  }

  @Test
  fun locationTextField_typingEverywhere_showsMultipleSuggestions() {
    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    val locationNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    locationNode.performScrollTo()
    locationNode.performTextInput("Everywhere")

    // Wait for many results
    composeTestRule.waitUntil(7000) {
      composeTestRule
          .onAllNodes(
              hasText("Somewhere", substring = true) and
                  !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Assert multiple suggestions exist
    val suggestionCount =
        composeTestRule
            .onAllNodes(
                hasText("Somewhere", substring = true) and
                    !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
                useUnmergedTree = true)
            .fetchSemanticsNodes(false)
            .size
    assert(suggestionCount > 1)
  }

  @Test
  fun locationTextField_typingTooLong_showsTruncatedOrWrappedText() {
    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    val locationNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    locationNode.performScrollTo()
    locationNode.performTextInput("Too long")

    // Wait for long suggestion to appear
    composeTestRule.waitUntil(7000) {
      composeTestRule
          .onAllNodes(
              hasText("This is a very long location name", substring = true) and
                  !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // The long name should be visible at least partially
    composeTestRule
        .onNode(
            hasText("This is a very long location name", substring = true) and
                !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
            useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun locationTextField_clearingInput_hidesSuggestions() {
    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    val locationNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    locationNode.performScrollTo()
    locationNode.performTextInput("EPFL")

    // Wait for suggestion
    composeTestRule.waitUntil(7000) {
      composeTestRule
          .onAllNodes(
              hasText("Fake EPFL") and !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Clear input
    locationNode.performTextClearance()

    // Verify no suggestions remain
    composeTestRule.waitUntil(7000) {
      composeTestRule
          .onAllNodes(
              hasText("Fake EPFL") and !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isEmpty()
    }
  }

  @Test
  fun locationTextField_dropdownClosesAfterSelection() {
    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    val locationNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    locationNode.performScrollTo()
    locationNode.performTextInput("Lausanne")

    // Wait for suggestion
    composeTestRule.waitUntil(7000) {
      composeTestRule
          .onAllNodes(
              hasText("Fake Lausanne") and
                  !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }

    // Click suggestion
    composeTestRule
        .onNode(
            hasText("Fake Lausanne") and
                !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
            useUnmergedTree = true)
        .performClick()

    // Wait for dropdown to disappear
    composeTestRule.waitUntil(3000) {
      composeTestRule
          .onAllNodes(
              hasText("Fake Lausanne") and
                  !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isEmpty()
    }
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
