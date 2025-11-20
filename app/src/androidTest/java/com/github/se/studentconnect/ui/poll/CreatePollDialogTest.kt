package com.github.se.studentconnect.ui.poll

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.poll.PollRepositoryLocal
import com.github.se.studentconnect.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreatePollDialogTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var pollRepository: PollRepositoryLocal
  private var dismissCalled = false
  private var pollCreatedCalled = false
  private val testEventUid = "test-event-123"

  @Before
  fun setup() {
    pollRepository = PollRepositoryLocal()
    dismissCalled = false
    pollCreatedCalled = false
  }

  @After
  fun tearDown() {
    pollRepository.clear()
  }

  private fun setContent() {
    composeTestRule.setContent {
      AppTheme {
        CreatePollDialog(
            eventUid = testEventUid,
            onDismiss = { dismissCalled = true },
            onPollCreated = { pollCreatedCalled = true },
            pollRepository = pollRepository)
      }
    }
  }

  @Test
  fun createPollDialog_initialState_createButtonDisabled() {
    setContent()

    // Find the create button and verify it's disabled
    composeTestRule.onAllNodesWithText("Create Poll").onLast().assertIsNotEnabled()
  }

  @Test
  fun createPollDialog_enterQuestion_updatesField() {
    setContent()

    // Enter question
    composeTestRule.onNodeWithText("Poll Question").performTextInput("What is your favorite color?")

    composeTestRule.waitForIdle()

    // Verify question was entered
    composeTestRule.onNodeWithText("What is your favorite color?").assertIsDisplayed()
  }

  @Test
  fun createPollDialog_enterOptions_updatesFields() {
    setContent()

    // Find and fill Option 1
    composeTestRule.onAllNodesWithText("Option 1").onFirst().performClick().performTextInput("Red")

    composeTestRule.waitForIdle()

    // Find and fill Option 2
    composeTestRule.onAllNodesWithText("Option 2").onFirst().performClick().performTextInput("Blue")

    composeTestRule.waitForIdle()

    // Verify options were entered
    composeTestRule.onNodeWithText("Red").assertIsDisplayed()
    composeTestRule.onNodeWithText("Blue").assertIsDisplayed()
  }

  @Test
  fun createPollDialog_addOption_addsNewOptionField() {
    setContent()

    // Initially should have 2 options
    composeTestRule.onNodeWithText("Option 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 2").assertIsDisplayed()

    // Add a new option
    composeTestRule.onNodeWithText("Add Option").performClick()
    composeTestRule.waitForIdle()

    // Should now have 3 options
    composeTestRule.onNodeWithText("Option 3").assertIsDisplayed()
  }

  @Test
  fun createPollDialog_addMultipleOptions_untilMax() {
    setContent()

    // Add options until max (6 total)
    repeat(4) {
      composeTestRule.onNodeWithText("Add Option").performClick()
      composeTestRule.waitForIdle()
    }

    // Scroll to make Option 6 visible and verify it exists
    composeTestRule.onAllNodesWithText("Option 6").onFirst().performScrollTo().assertExists()

    // Add option button should not be visible anymore
    composeTestRule.onNodeWithText("Add Option").assertDoesNotExist()
  }

  @Test
  fun createPollDialog_cannotRemoveWhenOnlyTwoOptions() {
    setContent()

    // Initially should have 2 options with no delete buttons visible
    composeTestRule.onNodeWithText("Option 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 2").assertIsDisplayed()

    // Delete buttons should not be available when only 2 options
    composeTestRule.onNodeWithContentDescription("Remove option").assertDoesNotExist()
  }

  @Test
  fun createPollDialog_validInput_enablesCreateButton() {
    setContent()

    // Enter question
    composeTestRule.onAllNodesWithText("Poll Question").onFirst().performTextInput("Test Question?")

    composeTestRule.waitForIdle()

    // Enter at least 2 options
    composeTestRule
        .onAllNodesWithText("Option 1")
        .onFirst()
        .performClick()
        .performTextInput("Option A")

    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodesWithText("Option 2")
        .onFirst()
        .performClick()
        .performTextInput("Option B")

    composeTestRule.waitForIdle()

    // Create button should now be enabled
    composeTestRule.onAllNodesWithText("Create Poll").onLast().assertIsEnabled()
  }

  @Test
  fun createPollDialog_createPoll_callsRepository() {
    setContent()

    // Enter question
    composeTestRule.onAllNodesWithText("Poll Question").onFirst().performTextInput("Test Question?")

    composeTestRule.waitForIdle()

    // Enter options
    composeTestRule
        .onAllNodesWithText("Option 1")
        .onFirst()
        .performClick()
        .performTextInput("Option A")

    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodesWithText("Option 2")
        .onFirst()
        .performClick()
        .performTextInput("Option B")

    composeTestRule.waitForIdle()

    // Click create button
    composeTestRule.onAllNodesWithText("Create Poll").onLast().performClick()

    composeTestRule.waitForIdle()

    // Verify poll was created in repository
    runBlocking {
      val polls = pollRepository.getActivePolls(testEventUid)
      assert(polls.isNotEmpty())
      assert(polls.first().question == "Test Question?")
    }
  }

  @Test
  fun createPollDialog_createPoll_callsOnPollCreatedAndDismiss() {
    setContent()

    // Enter valid data
    composeTestRule.onAllNodesWithText("Poll Question").onFirst().performTextInput("Test Question?")

    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodesWithText("Option 1")
        .onFirst()
        .performClick()
        .performTextInput("Option A")

    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodesWithText("Option 2")
        .onFirst()
        .performClick()
        .performTextInput("Option B")

    composeTestRule.waitForIdle()

    // Click create button
    composeTestRule.onAllNodesWithText("Create Poll").onLast().performClick()

    composeTestRule.waitForIdle()

    // Verify callbacks were called
    assert(pollCreatedCalled)
    assert(dismissCalled)
  }

  @Test
  fun createPollDialog_onlyQuestionFilled_buttonDisabled() {
    setContent()

    // Enter only question, no options
    composeTestRule.onAllNodesWithText("Poll Question").onFirst().performTextInput("Test Question?")

    composeTestRule.waitForIdle()

    // Create button should be disabled
    composeTestRule.onAllNodesWithText("Create Poll").onLast().assertIsNotEnabled()
  }

  @Test
  fun createPollDialog_onlyOneOptionFilled_buttonDisabled() {
    setContent()

    // Enter question
    composeTestRule.onAllNodesWithText("Poll Question").onFirst().performTextInput("Test Question?")

    composeTestRule.waitForIdle()

    // Enter only one option
    composeTestRule
        .onAllNodesWithText("Option 1")
        .onFirst()
        .performClick()
        .performTextInput("Option A")

    composeTestRule.waitForIdle()

    // Create button should be disabled (needs at least 2 options)
    composeTestRule.onAllNodesWithText("Create Poll").onLast().assertIsNotEnabled()
  }

  @Test
  fun createPollDialog_blankOptionsIgnored_buttonStillDisabled() {
    setContent()

    // Enter question
    composeTestRule.onAllNodesWithText("Poll Question").onFirst().performTextInput("Test Question?")

    composeTestRule.waitForIdle()

    // Add a third option but leave all options blank
    composeTestRule.onNodeWithText("Add Option").performClick()
    composeTestRule.waitForIdle()

    // Enter only one option
    composeTestRule
        .onAllNodesWithText("Option 1")
        .onFirst()
        .performClick()
        .performTextInput("Option A")

    composeTestRule.waitForIdle()

    // Create button should be disabled (needs at least 2 non-blank options)
    composeTestRule.onAllNodesWithText("Create Poll").onLast().assertIsNotEnabled()
  }

  @Test
  fun createPollDialog_multilineQuestion_displays() {
    setContent()

    // Enter a multiline question
    composeTestRule
        .onAllNodesWithText("Poll Question")
        .onFirst()
        .performTextInput(
            "This is a very long question\nthat spans multiple lines\nto test multiline support")

    composeTestRule.waitForIdle()

    // Verify text is displayed
    composeTestRule
        .onNodeWithText(
            "This is a very long question\nthat spans multiple lines\nto test multiline support")
        .assertIsDisplayed()
  }
}
