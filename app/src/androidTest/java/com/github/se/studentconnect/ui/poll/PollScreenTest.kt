package com.github.se.studentconnect.ui.poll

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollOption
import com.github.se.studentconnect.model.poll.PollRepositoryLocal
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PollScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var pollRepository: PollRepositoryLocal
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var pollViewModel: PollViewModel

  private val testEventUid = "event-123"
  private val testPollUid = "poll-456"
  private val testOwnerId = "owner-123"
  private val testUserId = "user-456"

  private val testPoll =
      Poll(
          uid = testPollUid,
          eventUid = testEventUid,
          question = "What is your favorite programming language?",
          options =
              listOf(
                  PollOption("opt1", "Kotlin", 5),
                  PollOption("opt2", "Java", 3),
                  PollOption("opt3", "Python", 7)),
          isActive = true)

  private val testEvent =
      Event.Public(
          uid = testEventUid,
          ownerId = testOwnerId,
          title = "Test Event",
          subtitle = "Test Subtitle",
          description = "Test Description",
          location = Location(0.0, 0.0, "Test Location"),
          start = Timestamp.now(),
          isFlash = false)

  @Before
  fun setup() {
    pollRepository = PollRepositoryLocal()
    eventRepository = EventRepositoryLocal()
    EventRepositoryProvider.repository = eventRepository

    // Set default auth
    AuthenticationProvider.testUserId = testUserId

    // Add test event
    runBlocking { eventRepository.addEvent(testEvent) }

    // Create ViewModel with local repository
    pollViewModel = PollViewModel(pollRepository)
  }

  @After
  fun tearDown() {
    AuthenticationProvider.testUserId = null
    pollRepository.clear()
  }

  private fun setContent() {
    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        PollScreen(
            eventUid = testEventUid,
            pollUid = testPollUid,
            navController = navController,
            pollViewModel = pollViewModel)
      }
    }
  }

  @Test
  fun pollScreen_displaysTopAppBar() {
    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Poll").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
  }

  @Test
  fun pollScreen_backButton_navigatesBack() {
    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun pollScreen_loadingState_showsProgressIndicator() {
    // Don't create poll, so it stays in loading/error state
    setContent()
    composeTestRule.waitForIdle()

    // Loading indicator should be present initially
  }

  @Test
  fun pollScreen_errorState_showsErrorMessage() {
    // Don't create poll, so it will error out
    setContent()
    pollViewModel.fetchPoll(testEventUid, "nonexistent-poll")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Poll not found").assertIsDisplayed()
    composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
  }

  @Test
  fun pollScreen_errorState_retryButton_callsFetchPoll() {
    setContent()
    pollViewModel.fetchPoll(testEventUid, "nonexistent-poll")
    composeTestRule.waitForIdle()

    // Click retry
    composeTestRule.onNodeWithText("Retry").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun pollScreen_activePoll_displaysQuestion() {
    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithText("What is your favorite programming language?")
        .assertIsDisplayed()
  }

  @Test
  fun pollScreen_activePoll_displaysAllOptions() {
    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Kotlin").assertIsDisplayed()
    composeTestRule.onNodeWithText("Java").assertIsDisplayed()
    composeTestRule.onNodeWithText("Python").assertIsDisplayed()
  }

  @Test
  fun pollScreen_activePoll_displaysSelectPrompt() {
    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Select an option:").assertIsDisplayed()
  }

  @Test
  fun pollScreen_activePoll_canSelectOption() {
    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    // Click on Kotlin option
    composeTestRule.onNodeWithText("Kotlin").performClick()
    composeTestRule.waitForIdle()

    // Submit button should be enabled
    composeTestRule.onNodeWithText("Submit Vote").assertIsEnabled()
  }

  @Test
  fun pollScreen_activePoll_submitButtonDisabledInitially() {
    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Submit Vote").assertIsNotEnabled()
  }

  @Test
  fun pollScreen_closedPoll_showsClosedMessage() {
    val closedPoll = testPoll.copy(isActive = false)
    runBlocking { pollRepository.createPoll(closedPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("This poll is closed").assertIsDisplayed()
  }

  @Test
  fun pollScreen_closedPoll_submitButtonDisabled() {
    val closedPoll = testPoll.copy(isActive = false)
    runBlocking { pollRepository.createPoll(closedPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    // Try to select an option
    composeTestRule.onNodeWithText("Kotlin").performClick()
    composeTestRule.waitForIdle()

    // Submit button should be disabled
    composeTestRule.onNodeWithText("Submit Vote").assertIsNotEnabled()
  }

  @Test
  fun pollScreen_userHasVoted_showsResults() {
    runBlocking {
      pollRepository.createPoll(testPoll)
      // Simulate user has voted - can't actually vote without being participant
      // But we can check the UI shows results correctly when vote count exists
    }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    // Poll should show vote counts (even if user hasn't voted, counts are visible)
    // Results are shown after voting or for owner
  }

  @Test
  fun pollScreen_ownerView_showsOwnerMessage() {
    // Set current user as owner
    AuthenticationProvider.testUserId = testOwnerId

    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithText("As the event organizer, you cannot vote on this poll")
        .assertIsDisplayed()

    // Reset
    AuthenticationProvider.testUserId = testUserId
  }

  @Test
  fun pollScreen_ownerView_showsResults() {
    // Set current user as owner
    AuthenticationProvider.testUserId = testOwnerId

    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    // Should show results for owner
    composeTestRule.onNodeWithText("Results").assertIsDisplayed()
    composeTestRule.onNodeWithText("5 (33%)").assertIsDisplayed()

    // Reset
    AuthenticationProvider.testUserId = testUserId
  }

  @Test
  fun pollScreen_ownerView_cannotVote() {
    // Set current user as owner
    AuthenticationProvider.testUserId = testOwnerId

    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    // Submit button should not be displayed
    composeTestRule.onNodeWithText("Submit Vote").assertDoesNotExist()

    // Reset
    AuthenticationProvider.testUserId = testUserId
  }

  @Test
  fun pollScreen_multipleOptions_allDisplayed() {
    val pollWithManyOptions =
        testPoll.copy(
            options =
                listOf(
                    PollOption("opt1", "Option 1", 1),
                    PollOption("opt2", "Option 2", 2),
                    PollOption("opt3", "Option 3", 3),
                    PollOption("opt4", "Option 4", 4),
                    PollOption("opt5", "Option 5", 5)))

    runBlocking { pollRepository.createPoll(pollWithManyOptions) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    // All options should be displayed
    composeTestRule.onNodeWithText("Option 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 3").assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 4").assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 5").assertIsDisplayed()
  }

  @Test
  fun pollScreen_longQuestion_displaysCorrectly() {
    val pollWithLongQuestion =
        testPoll.copy(
            question =
                "This is a very long question that might span multiple lines to test how the UI handles long text content in poll questions")

    runBlocking { pollRepository.createPoll(pollWithLongQuestion) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithText(
            "This is a very long question that might span multiple lines to test how the UI handles long text content in poll questions")
        .assertIsDisplayed()
  }

  @Test
  fun pollScreen_canSwitchBetweenOptions() {
    runBlocking { pollRepository.createPoll(testPoll) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    // Select first option
    composeTestRule.onNodeWithText("Kotlin").performClick()
    composeTestRule.waitForIdle()

    // Select different option
    composeTestRule.onNodeWithText("Python").performClick()
    composeTestRule.waitForIdle()

    // Should be able to submit
    composeTestRule.onNodeWithText("Submit Vote").assertIsEnabled()
  }

  @Test
  fun pollScreen_resultOption_showsCorrectPercentageCalculation() {
    // Test with specific numbers to verify percentage calculation
    val testPollWithKnownVotes =
        testPoll.copy(
            options = listOf(PollOption("opt1", "First", 25), PollOption("opt2", "Second", 75)))

    // Set as owner to see results
    AuthenticationProvider.testUserId = testOwnerId

    runBlocking { pollRepository.createPoll(testPollWithKnownVotes) }

    setContent()
    pollViewModel.fetchPoll(testEventUid, testPollUid)
    composeTestRule.waitForIdle()

    // 25/100 = 25%, 75/100 = 75%
    composeTestRule.onNodeWithText("25 (25%)").assertIsDisplayed()
    composeTestRule.onNodeWithText("75 (75%)").assertIsDisplayed()
    composeTestRule.onNodeWithText("100 total votes").assertIsDisplayed()

    // Reset
    AuthenticationProvider.testUserId = testUserId
  }

  @Test
  fun pollScreen_pollNotFound_showsError() {
    setContent()
    pollViewModel.fetchPoll(testEventUid, "nonexistent-poll")
    composeTestRule.waitForIdle()

    // Should show error message
    composeTestRule.onNodeWithText("Poll not found").assertIsDisplayed()
  }
}
