// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.eventcreation

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class CreatePublicEventScreenTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  override fun createInitializedRepository() = EventRepositoryLocal()

  @Before
  fun setUpContent() {
    composeTestRule.setContent { AppTheme { CreatePublicEventScreen() } }
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
    waitForTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH)
        .performScrollTo()
        .assertIsDisplayed()

    waitForTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun bannerPicker_isDisplayed() {
    waitForTag(CreatePublicEventScreenTestTags.BANNER_PICKER)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.BANNER_PICKER)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun saveButton_disabled_whenMandatoryFieldsEmpty() {
    waitForTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertIsNotEnabled()
  }

  // --------------------------------------------------
  // 2. Title field
  // --------------------------------------------------

  @Test
  fun typingInTitle_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val titleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    titleNode.performScrollTo()
    titleNode.performTextInput("My Event")
    titleNode.assertTextContains("My Event")
  }

  @Test
  fun emptyTitle_showsErrorText() {
    waitForTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val titleNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    titleNode.performScrollTo()
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
    subtitleNode.performScrollTo()
    subtitleNode.performTextInput("Optional subtitle")
    subtitleNode.assertTextContains("Optional subtitle")
  }

  @Test
  fun typingInDescription_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
    val descNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
    descNode.performScrollTo()
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
    waitForTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    val startDateNode =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    startDateNode.performScrollTo()
    startDateNode.performTextInput("01/01/2025")
    startDateNode.assertTextContains("01/01/2025")
  }

  @Test
  fun enteringEndDate_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    val endDateNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    endDateNode.performScrollTo()
    endDateNode.performTextInput("02/01/2025")
    endDateNode.assertTextContains("02/01/2025")
  }

  @Test
  fun clickingStartTimeButton_opensPicker() {
    waitForTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
    val startTimeButton =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON)
    startTimeButton.performScrollTo()
    startTimeButton.performClick()
    // TODO: assert time picker dialog is visible
  }

  @Test
  fun clickingEndTimeButton_opensPicker() {
    waitForTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON)
    val endTimeButton =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON)
    endTimeButton.performScrollTo()
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
    participantsNode.performScrollTo()
    participantsNode.performTextInput("25")
    participantsNode.assertTextContains("25")
  }

  @Test
  fun typingWebsite_updatesValue() {
    waitForTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
    val websiteNode = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
    websiteNode.performScrollTo()
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
        .performScrollTo()
        .assertIsNotEnabled()
  }

  @Test
  fun enablingParticipationFeeSwitch_enablesInput() {
    waitForTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val switch =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    switch.performScrollTo()
    switch.performClick()
    waitUntilEnabled(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    input.performScrollTo()
    input.assertIsEnabled()
  }

  @Test
  fun disablingParticipationFeeSwitch_disablesAndClearsInput() {
    waitForTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val switch =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
    val input =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)

    switch.performScrollTo()
    switch.performClick() // enable
    waitUntilEnabled(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    input.performScrollTo()
    input.performTextInput("50")
    input.assertTextContains("50")

    switch.performScrollTo()
    switch.performClick() // disable
    waitUntilDisabled(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
    input.performScrollTo()
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
    flashSwitch.performScrollTo()
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
    save.performScrollTo()
    save.assertIsNotEnabled()

    waitForTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val title = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val startDate = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    val endDate = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)

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
    waitForTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    val title = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    val startDate = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    val endDate = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    val save = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)

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
    // Use FakeCreatePublicEventViewModel with finishedSaving=true
    // Fill required fields, click save
    // Then assert navigation state (e.g. checking fake NavController)
  }

  // --------------------------------------------------
  // 11. New UI Components - Scaffold, TopAppBar, FAB
  // --------------------------------------------------

  @Test
  fun scaffold_isDisplayed() {
    waitForTag(CreatePublicEventScreenTestTags.SCAFFOLD)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SCAFFOLD).assertIsDisplayed()
  }

  @Test
  fun topAppBar_isDisplayed() {
    waitForTag(CreatePublicEventScreenTestTags.TOP_APP_BAR)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TOP_APP_BAR).assertIsDisplayed()
  }

  @Test
  fun topAppBar_showsCorrectTitle_whenCreatingNewEvent() {
    waitForTag(CreatePublicEventScreenTestTags.TOP_APP_BAR)
    composeTestRule.onNodeWithText("Create Public Event").assertIsDisplayed()
  }

  @Test
  fun backButton_isDisplayed() {
    waitForTag(CreatePublicEventScreenTestTags.BACK_BUTTON)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun backButton_isClickable() {
    waitForTag(CreatePublicEventScreenTestTags.BACK_BUTTON)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.BACK_BUTTON).performClick()
    // Navigation callback should be triggered
  }

  @Test
  fun floatingActionButton_isDisplayedAsFAB() {
    waitForTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun floatingActionButton_hasCorrectIcon() {
    waitForTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
    // FAB should contain save icon
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).assertExists()
  }

  // --------------------------------------------------
  // 12. Required Field Indicators (Asterisks)
  // --------------------------------------------------

  @Test
  fun titleField_showsRequiredIndicator() {
    waitForTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
    // Check that Title field has asterisk indicator
    composeTestRule.onNodeWithText("Title *", substring = true).assertExists()
  }

  @Test
  fun startDateField_showsRequiredIndicator() {
    waitForTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
        .performScrollTo()
    // Check that Start date field has asterisk indicator
    composeTestRule.onNodeWithText("Start of the event *", substring = true).assertExists()
  }

  @Test
  fun endDateField_showsRequiredIndicator() {
    waitForTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT).performScrollTo()
    // Check that End date field has asterisk indicator
    composeTestRule.onNodeWithText("End of the event *", substring = true).assertExists()
  }

  @Test
  fun optionalFields_doNotShowRequiredIndicator() {
    waitForTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
    // Subtitle should not have asterisk
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT).performScrollTo()
    // Should show "Subtitle" without asterisk
    composeTestRule.onNodeWithText("Subtitle", substring = true).assertExists()
  }

  @Test
  fun descriptionField_doesNotShowRequiredIndicator() {
    waitForTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
        .performScrollTo()
    // Should show "Description" without asterisk
    composeTestRule.onNodeWithText("Description", substring = true).assertExists()
  }

  @Test
  fun websiteField_doesNotShowRequiredIndicator() {
    waitForTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT).performScrollTo()
    // Should show "Event website" without asterisk
    composeTestRule.onNodeWithText("Event website", substring = true).assertExists()
  }

  // --------------------------------------------------
  // 13. Location Field - Suggestion Hiding After Selection
  // --------------------------------------------------

  @Test
  fun locationTextField_suggestionDisappearsAfterSelection() {
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

    // Verify field value updated
    locationNode.assertTextContains("Fake Lausanne")
  }

  @Test
  fun locationTextField_suggestionsReappearAfterTypingAgain() {
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

    // Select suggestion
    composeTestRule
        .onNode(
            hasText("Fake EPFL") and !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
            useUnmergedTree = true)
        .performClick()

    // Wait for dropdown to close
    composeTestRule.waitUntil(3000) {
      composeTestRule
          .onAllNodes(
              hasText("Fake EPFL") and !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isEmpty()
    }

    // Type again to trigger suggestions
    locationNode.performTextClearance()
    locationNode.performTextInput("Lausanne")

    // Suggestions should reappear
    composeTestRule.waitUntil(7000) {
      composeTestRule
          .onAllNodes(
              hasText("Fake Lausanne") and
                  !hasTestTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
              useUnmergedTree = true)
          .fetchSemanticsNodes(false)
          .isNotEmpty()
    }
  }

  @Test
  fun locationTextField_suggestionsHiddenWhenLocationWasSelected() {
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

    // Verify suggestions are hidden
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
}
