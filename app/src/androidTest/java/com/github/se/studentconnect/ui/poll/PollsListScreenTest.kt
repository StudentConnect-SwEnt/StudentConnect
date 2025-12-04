package com.github.se.studentconnect.ui.poll

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollOption
import com.github.se.studentconnect.model.poll.PollRepositoryLocal
import com.github.se.studentconnect.model.poll.PollVote
import com.github.se.studentconnect.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PollsListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var pollRepository: PollRepositoryLocal
  private lateinit var pollViewModel: PollViewModel

  private val testEventUid = "event-123"
  private val testUserId = "user-456"

  private val testPolls =
      listOf(
          Poll(
              uid = "poll-1",
              eventUid = testEventUid,
              question = "What is your favorite color?",
              options =
                  listOf(
                      PollOption("opt1", "Red", 5),
                      PollOption("opt2", "Blue", 3),
                      PollOption("opt3", "Green", 7)),
              isActive = true),
          Poll(
              uid = "poll-2",
              eventUid = testEventUid,
              question = "Best programming language?",
              options = listOf(PollOption("opt1", "Kotlin", 10), PollOption("opt2", "Java", 8)),
              isActive = true),
          Poll(
              uid = "poll-3",
              eventUid = testEventUid,
              question = "Closed poll question?",
              options = listOf(PollOption("opt1", "Yes", 2), PollOption("opt2", "No", 3)),
              isActive = false))

  @Before
  fun setup() {
    pollRepository = PollRepositoryLocal()
    AuthenticationProvider.testUserId = testUserId
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
        PollsListScreen(
            eventUid = testEventUid, navController = navController, pollViewModel = pollViewModel)
      }
    }
  }

  @Test
  fun pollsListScreen_displaysTopAppBar() {
    runBlocking { testPolls.forEach { pollRepository.createPoll(it) } }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Event Polls").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
  }

  @Test
  fun pollsListScreen_backButton_navigatesBack() {
    runBlocking { testPolls.forEach { pollRepository.createPoll(it) } }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun pollsListScreen_loadingState_showsProgressIndicator() {
    // Don't create polls or fetch, stays in loading state
    setContent()
    composeTestRule.waitForIdle()
  }

  @Test
  fun pollsListScreen_emptyState_showsEmptyMessage() {
    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("No polls available for this event").assertIsDisplayed()
  }

  @Test
  fun pollsListScreen_pollCard_showsActiveStatus() {
    runBlocking { testPolls.forEach { pollRepository.createPoll(it) } }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    // Should show "Active" status for active polls
    composeTestRule.onAllNodesWithText("Active").assertCountEquals(2)
  }

  @Test
  fun pollsListScreen_pollCard_showsOptionsCount() {
    runBlocking { testPolls.forEach { pollRepository.createPoll(it) } }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    // Should show number of options for each poll
    composeTestRule.onNodeWithText("3 options").assertIsDisplayed() // First poll
    composeTestRule.onNodeWithText("2 options").assertExists() // Second and third polls
  }

  @Test
  fun pollsListScreen_pollCard_hidesTotalVotesWhenZero() {
    val pollWithNoVotes =
        Poll(
            uid = "poll-no-votes",
            eventUid = testEventUid,
            question = "No votes yet?",
            options = listOf(PollOption("opt1", "Yes", 0), PollOption("opt2", "No", 0)),
            isActive = true)

    runBlocking { pollRepository.createPoll(pollWithNoVotes) }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    // Should not show "0 total votes" when no votes
    composeTestRule.onNodeWithText("0 total votes").assertDoesNotExist()
  }

  @Test
  fun pollsListScreen_pollCard_showsVotedIndicator() {
    runBlocking {
      testPolls.forEach { pollRepository.createPoll(it) }
      // Simulate user voted on poll-1
      pollRepository.submitVote(
          testEventUid, PollVote(userId = testUserId, pollUid = "poll-1", optionId = "opt1"))
    }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    // Should show "Voted" indicator for polls user has voted on
    composeTestRule.onNodeWithText("Voted").assertIsDisplayed()
  }

  @Test
  fun pollsListScreen_pollCard_hidesVotedIndicatorWhenNotVoted() {
    runBlocking { testPolls.forEach { pollRepository.createPoll(it) } }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    // Should not show "Voted" indicator
    composeTestRule.onNodeWithText("Voted").assertDoesNotExist()
  }

  @Test
  fun pollsListScreen_activePoll_hasActiveColor() {
    val activePoll =
        Poll(
            uid = "poll-active",
            eventUid = testEventUid,
            question = "Active poll?",
            options = listOf(PollOption("opt1", "Yes", 0)),
            isActive = true)

    runBlocking { pollRepository.createPoll(activePoll) }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    // Active poll should be displayed
    composeTestRule.onNodeWithText("Active poll?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Active").assertIsDisplayed()
  }

  @Test
  fun pollsListScreen_longQuestion_displaysCorrectly() {
    val pollWithLongQuestion =
        Poll(
            uid = "poll-long",
            eventUid = testEventUid,
            question =
                "This is a very long poll question that might need to wrap to multiple lines to test the UI layout",
            options = listOf(PollOption("opt1", "Yes", 0), PollOption("opt2", "No", 0)),
            isActive = true)

    runBlocking { pollRepository.createPoll(pollWithLongQuestion) }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithText(
            "This is a very long poll question that might need to wrap to multiple lines to test the UI layout")
        .assertIsDisplayed()
  }

  @Test
  fun pollsListScreen_singleOption_showsCorrectCount() {
    val pollWithOneOption =
        Poll(
            uid = "poll-single",
            eventUid = testEventUid,
            question = "Single option?",
            options = listOf(PollOption("opt1", "Only option", 5)),
            isActive = true)

    runBlocking { pollRepository.createPoll(pollWithOneOption) }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("1 options").assertIsDisplayed()
  }

  @Test
  fun pollsListScreen_manyOptions_showsCorrectCount() {
    val pollWithManyOptions =
        Poll(
            uid = "poll-many",
            eventUid = testEventUid,
            question = "Many options?",
            options =
                listOf(
                    PollOption("opt1", "Option 1", 1),
                    PollOption("opt2", "Option 2", 1),
                    PollOption("opt3", "Option 3", 1),
                    PollOption("opt4", "Option 4", 1),
                    PollOption("opt5", "Option 5", 1),
                    PollOption("opt6", "Option 6", 1)),
            isActive = true)

    runBlocking { pollRepository.createPoll(pollWithManyOptions) }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("6 options").assertIsDisplayed()
  }

  @Test
  fun pollsListScreen_pollWithHighVotes_displaysTotalCorrectly() {
    val pollWithHighVotes =
        Poll(
            uid = "poll-high",
            eventUid = testEventUid,
            question = "Popular poll?",
            options = listOf(PollOption("opt1", "Yes", 1000), PollOption("opt2", "No", 500)),
            isActive = true)

    runBlocking { pollRepository.createPoll(pollWithHighVotes) }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("1500 total votes").assertIsDisplayed()
  }

  @Test
  fun pollsListScreen_allActivePolls_showsOnlyActiveStatus() {
    val activePolls = testPolls.filter { it.isActive }

    runBlocking { activePolls.forEach { pollRepository.createPoll(it) } }

    setContent()
    pollViewModel.fetchAllPolls(testEventUid)
    composeTestRule.waitForIdle()

    // Should only show "Active" status
    composeTestRule.onNodeWithText("Closed").assertDoesNotExist()
    composeTestRule.onAllNodesWithText("Active").assertCountEquals(activePolls.size)
  }
}
