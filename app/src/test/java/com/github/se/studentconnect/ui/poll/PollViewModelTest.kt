package com.github.se.studentconnect.ui.poll

import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollOption
import com.github.se.studentconnect.model.poll.PollRepository
import com.github.se.studentconnect.model.poll.PollVote
import com.google.firebase.Timestamp
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PollViewModelTest {

  private lateinit var pollRepository: PollRepository
  private lateinit var viewModel: PollViewModel
  private val testDispatcher = StandardTestDispatcher()

  private val testEventUid = "event123"
  private val testPollUid = "poll123"
  private val testUserId = "user123"

  private val testPoll =
      Poll(
          uid = testPollUid,
          eventUid = testEventUid,
          question = "What is your favorite color?",
          options =
              listOf(
                  PollOption("opt1", "Red", 5),
                  PollOption("opt2", "Blue", 3),
                  PollOption("opt3", "Green", 2)),
          createdAt = Timestamp.now(),
          isActive = true)

  private val testVote =
      PollVote(
          userId = testUserId, pollUid = testPollUid, optionId = "opt1", votedAt = Timestamp.now())

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    pollRepository = mockk(relaxed = true)
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns testUserId
    viewModel = PollViewModel(pollRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `fetchPoll updates state with poll and user vote when successful`() = runTest {
    coEvery { pollRepository.getPoll(testEventUid, testPollUid) } returns testPoll
    coEvery { pollRepository.getUserVote(testEventUid, testPollUid, testUserId) } returns testVote

    viewModel.fetchPoll(testEventUid, testPollUid)
    advanceUntilIdle()

    val state = viewModel.pollUiState.value
    assert(!state.isLoading)
    assert(state.poll == testPoll)
    assert(state.userVote == testVote)
    assert(state.hasVoted)
    assert(state.error == null)
  }

  @Test
  fun `fetchPoll updates state with poll and no vote when user has not voted`() = runTest {
    coEvery { pollRepository.getPoll(testEventUid, testPollUid) } returns testPoll
    coEvery { pollRepository.getUserVote(testEventUid, testPollUid, testUserId) } answers { null }

    viewModel.fetchPoll(testEventUid, testPollUid)
    advanceUntilIdle()

    val state = viewModel.pollUiState.value
    assert(!state.isLoading)
    assert(state.poll == testPoll)
    assert(state.userVote == null)
    assert(!state.hasVoted)
    assert(state.error == null)
  }

  @Test
  fun `fetchPoll updates state with error when poll not found`() = runTest {
    coEvery { pollRepository.getPoll(testEventUid, testPollUid) } answers { null }

    viewModel.fetchPoll(testEventUid, testPollUid)
    advanceUntilIdle()

    val state = viewModel.pollUiState.value
    assert(!state.isLoading)
    assert(state.poll == null)
    assert(state.error == "Poll not found")
  }

  @Test
  fun `fetchPoll updates state with error when exception occurs`() = runTest {
    val errorMessage = "Network error"
    coEvery { pollRepository.getPoll(testEventUid, testPollUid) } throws Exception(errorMessage)

    viewModel.fetchPoll(testEventUid, testPollUid)
    advanceUntilIdle()

    val state = viewModel.pollUiState.value
    assert(!state.isLoading)
    assert(state.error == errorMessage)
  }

  @Test
  fun `fetchPoll handles null user when fetching vote`() = runTest {
    every { AuthenticationProvider.currentUser } returns ""
    val nullUserViewModel = PollViewModel(pollRepository)
    coEvery { pollRepository.getPoll(testEventUid, testPollUid) } returns testPoll

    nullUserViewModel.fetchPoll(testEventUid, testPollUid)
    advanceUntilIdle()

    val state = nullUserViewModel.pollUiState.value
    assert(!state.isLoading)
    assert(state.poll == testPoll)
    assert(state.userVote == null)
    assert(!state.hasVoted)
    coVerify(exactly = 0) { pollRepository.getUserVote(any(), any(), any()) }
  }

  @Test
  fun `fetchAllPolls updates state with polls and user votes when successful`() = runTest {
    val polls = listOf(testPoll, testPoll.copy(uid = "poll456"))
    val votes = mapOf(testPollUid to testVote)

    coEvery { pollRepository.getActivePolls(testEventUid) } returns polls
    coEvery { pollRepository.getUserVote(testEventUid, testPollUid, testUserId) } returns testVote
    coEvery { pollRepository.getUserVote(testEventUid, "poll456", testUserId) } answers { null }

    viewModel.fetchAllPolls(testEventUid)
    advanceUntilIdle()

    val state = viewModel.pollsListUiState.value
    assert(!state.isLoading)
    assert(state.polls == polls)
    assert(state.userVotes.size == 1)
    assert(state.userVotes[testPollUid] == testVote)
    assert(state.error == null)
  }

  @Test
  fun `fetchAllPolls updates state with empty votes when user is null`() = runTest {
    every { AuthenticationProvider.currentUser } returns ""
    val nullUserViewModel = PollViewModel(pollRepository)
    val polls = listOf(testPoll)
    coEvery { pollRepository.getActivePolls(testEventUid) } returns polls

    nullUserViewModel.fetchAllPolls(testEventUid)
    advanceUntilIdle()

    val state = nullUserViewModel.pollsListUiState.value
    assert(!state.isLoading)
    assert(state.polls == polls)
    assert(state.userVotes.isEmpty())
    assert(state.error == null)
  }

  @Test
  fun `fetchAllPolls updates state with error when exception occurs`() = runTest {
    val errorMessage = "Failed to fetch polls"
    coEvery { pollRepository.getActivePolls(testEventUid) } throws Exception(errorMessage)

    viewModel.fetchAllPolls(testEventUid)
    advanceUntilIdle()

    val state = viewModel.pollsListUiState.value
    assert(!state.isLoading)
    assert(state.error == errorMessage)
  }

  @Test
  fun `submitVote submits vote and refreshes poll when successful`() = runTest {
    coEvery { pollRepository.submitVote(testEventUid, any()) } just Runs
    coEvery { pollRepository.getPoll(testEventUid, testPollUid) } returns testPoll
    coEvery { pollRepository.getUserVote(testEventUid, testPollUid, testUserId) } returns testVote

    viewModel.submitVote(testEventUid, testPollUid, "opt1")
    advanceUntilIdle()

    coVerify { pollRepository.submitVote(testEventUid, match { it.optionId == "opt1" }) }
    coVerify { pollRepository.getPoll(testEventUid, testPollUid) }

    val state = viewModel.pollUiState.value
    assert(state.hasVoted)
  }

  @Test
  fun `submitVote does nothing when user is null`() = runTest {
    every { AuthenticationProvider.currentUser } returns ""
    val nullUserViewModel = PollViewModel(pollRepository)

    nullUserViewModel.submitVote(testEventUid, testPollUid, "opt1")
    advanceUntilIdle()

    coVerify(exactly = 0) { pollRepository.submitVote(any(), any()) }
  }

  @Test
  fun `submitVote updates state with error when exception occurs`() = runTest {
    val errorMessage = "Failed to submit vote"
    coEvery { pollRepository.submitVote(testEventUid, any()) } throws Exception(errorMessage)

    viewModel.submitVote(testEventUid, testPollUid, "opt1")
    advanceUntilIdle()

    val state = viewModel.pollUiState.value
    assert(state.error == errorMessage)
  }

  @Test
  fun `clearError clears errors from both states`() = runTest {
    // Set errors in both states
    coEvery { pollRepository.getPoll(testEventUid, testPollUid) } throws Exception("Error 1")
    viewModel.fetchPoll(testEventUid, testPollUid)
    advanceUntilIdle()

    coEvery { pollRepository.getActivePolls(testEventUid) } throws Exception("Error 2")
    viewModel.fetchAllPolls(testEventUid)
    advanceUntilIdle()

    assert(viewModel.pollUiState.value.error != null)
    assert(viewModel.pollsListUiState.value.error != null)

    viewModel.clearError()

    assert(viewModel.pollUiState.value.error == null)
    assert(viewModel.pollsListUiState.value.error == null)
  }
}
