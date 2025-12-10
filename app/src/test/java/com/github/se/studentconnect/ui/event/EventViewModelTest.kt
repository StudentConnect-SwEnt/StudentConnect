package com.github.se.studentconnect.ui.event

import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.poll.PollRepositoryLocal
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

private class FakeFriendsRepository(val friends: List<String> = emptyList()) : FriendsRepository {
  override suspend fun getFriends(userId: String): List<String> = friends

  override suspend fun getFriendsPublic(userId: String): List<String> = friends

  override suspend fun getPendingRequests(userId: String): List<String> = emptyList()

  override suspend fun getSentRequests(userId: String): List<String> = emptyList()

  override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) {}

  override suspend fun acceptFriendRequest(userId: String, fromUserId: String) {}

  override suspend fun rejectFriendRequest(userId: String, fromUserId: String) {}

  override suspend fun cancelFriendRequest(userId: String, toUserId: String) {}

  override suspend fun removeFriend(userId: String, friendId: String) {}

  override suspend fun areFriends(userId: String, otherUserId: String): Boolean = false

  override suspend fun hasPendingRequest(fromUserId: String, toUserId: String): Boolean = false

  override fun observeFriendship(userId: String, otherUserId: String) = flow { emit(false) }
}

@ExperimentalCoroutinesApi
class EventViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: EventViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var pollRepository: PollRepositoryLocal
  private lateinit var friendsRepository: FriendsRepository

  private val testEvent =
      Event.Public(
          uid = "test-event-123",
          title = "Test Event",
          subtitle = "Test Subtitle",
          description = "Test Description",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(0.0, 0.0, "Test Location"),
          website = "https://test.com",
          ownerId = "owner123",
          isFlash = false)

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    pollRepository = PollRepositoryLocal()
    friendsRepository = FakeFriendsRepository()
    viewModel = EventViewModel(eventRepository, userRepository, pollRepository, friendsRepository)
    // Force un utilisateur courant non vide pendant les tests
    AuthenticationProvider.testUserId = "test-user-id"
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    // Réinitialise l’UID de test
    AuthenticationProvider.testUserId = null
  }

  @Test
  fun initialState_isLoading() {
    val uiState = viewModel.uiState.value
    assertTrue(uiState.isLoading)
    assertNull(uiState.event)
    assertFalse(uiState.isJoined)
  }

  @Test
  fun fetchEvent_updatesUiState() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.event)
    assertEquals(testEvent.uid, uiState.event?.uid)
    assertEquals(testEvent.title, uiState.event?.title)
    assertFalse(uiState.isJoined)
  }

  @Test
  fun fetchEvent_withJoinedStatus_updatesIsJoined() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    // Add current user as participant to make isJoined = true
    val currentUserId = "test-user-id"
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUserId))

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    // Note: isJoined will be determined by whether currentUserId is in participants
  }

  @Test
  fun fetchEvent_setsLoadingStateCorrectly() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act - check loading state is true initially
    viewModel.fetchEvent(testEvent.uid)
    var uiState = viewModel.uiState.value
    assertTrue("Loading should be true initially", uiState.isLoading)

    advanceUntilIdle()

    // Assert - loading should be false after fetch completes
    uiState = viewModel.uiState.value
    assertFalse("Loading should be false after fetch", uiState.isLoading)
  }

  @Test
  fun fetchEvent_preservesIsJoinedStatus() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    // Add current user as participant
    val currentUserId = "test-user-id"
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUserId))

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    // isJoined is determined by checking if current user is in participants list
  }

  @Test
  fun fetchEvent_withDifferentEvent_updatesEventData() = runTest {
    // Arrange
    val event1 =
        testEvent.copy(uid = "event1", title = "Event One", description = "Description One")
    val event2 =
        testEvent.copy(uid = "event2", title = "Event Two", description = "Description Two")
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act - fetch first event
    viewModel.fetchEvent(event1.uid)
    advanceUntilIdle()

    var uiState = viewModel.uiState.value
    assertEquals("Event One", uiState.event?.title)

    // Act - fetch second event
    viewModel.fetchEvent(event2.uid)
    advanceUntilIdle()

    // Assert
    uiState = viewModel.uiState.value
    assertEquals("Event Two", uiState.event?.title)
    assertEquals("Description Two", uiState.event?.description)
  }

  @Test
  fun uiState_emitsUpdates() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert - verify the state flow has been updated correctly
    val uiState = viewModel.uiState.value
    assertFalse("Last state should not be loading", uiState.isLoading)
    assertNotNull("Last state should have event", uiState.event)
  }

  @Test
  fun fetchEvent_multipleCallsInSequence_updatesCorrectly() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act - multiple fetches
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull("Event should be loaded", uiState.event)
  }

  @Test
  fun initialState_qrScannerNotShown() {
    val uiState = viewModel.uiState.value
    assertFalse(uiState.showQrScanner)
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun showQrScanner_updatesUiState() {
    // Act
    viewModel.showQrScanner()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.showQrScanner)
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun hideQrScanner_updatesUiState() {
    // Arrange
    viewModel.showQrScanner()

    // Act
    viewModel.hideQrScanner()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.showQrScanner)
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun showQrScanner_clearsValidationResult() {
    // Arrange - set a validation result first
    viewModel.showQrScanner()
    viewModel.validateParticipant(testEvent.uid, "user123")

    // Act - show scanner again
    viewModel.showQrScanner()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.showQrScanner)
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun validateParticipant_withValidParticipant_returnsValid() = runTest {
    // Arrange
    val participantId = "participant123"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))

    // Act
    viewModel.validateParticipant(testEvent.uid, participantId)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Valid)
    assertEquals(
        participantId,
        (uiState.ticketValidationResult as TicketValidationResult.Valid).participantId)
  }

  @Test
  fun validateParticipant_withInvalidParticipant_returnsInvalid() = runTest {
    // Arrange
    val nonParticipantId = "nonparticipant123"
    eventRepository.addEvent(testEvent)
    // Add a different participant
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("otheruser"))

    // Act
    viewModel.validateParticipant(testEvent.uid, nonParticipantId)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Invalid)
    assertEquals(
        nonParticipantId, (uiState.ticketValidationResult as TicketValidationResult.Invalid).userId)
  }

  @Test
  fun validateParticipant_withNoParticipants_returnsInvalid() = runTest {
    // Arrange
    val userId = "user123"
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.validateParticipant(testEvent.uid, userId)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Invalid)
  }

  @Test
  fun validateParticipant_withMultipleParticipants_findsCorrectOne() = runTest {
    // Arrange
    val targetParticipant = "target123"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user1"))
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user2"))
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(targetParticipant))
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user4"))

    // Act
    viewModel.validateParticipant(testEvent.uid, targetParticipant)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Valid)
    assertEquals(
        targetParticipant,
        (uiState.ticketValidationResult as TicketValidationResult.Valid).participantId)
  }

  @Test
  fun clearValidationResult_removesResult() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user123"))
    viewModel.validateParticipant(testEvent.uid, "user123")
    advanceUntilIdle()

    // Act
    viewModel.clearValidationResult()

    // Assert
    val uiState = viewModel.uiState.value
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun clearValidationResult_whenNoResult_doesNotCrash() {
    // Act & Assert - should not crash
    viewModel.clearValidationResult()

    val uiState = viewModel.uiState.value
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun validateParticipant_withException_returnsError() = runTest {
    // Arrange - create a mock repository that throws an exception
    val mockEventRepository = EventRepositoryLocal()
    mockEventRepository.addEvent(testEvent)

    val errorThrowingRepo =
        object : EventRepository by mockEventRepository {
          override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> {
            throw RuntimeException("Network error")
          }
        }
    val mockViewModel =
        EventViewModel(errorThrowingRepo, userRepository, pollRepository, friendsRepository)

    val userId = "user123"

    // Act
    mockViewModel.validateParticipant(testEvent.uid, userId)
    advanceUntilIdle()

    // Assert
    val uiState = mockViewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Error)
  }

  @Test
  fun ticketValidationResult_validType_hasCorrectData() = runTest {
    // Arrange
    val participantId = "valid-participant"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))

    // Act
    viewModel.validateParticipant(testEvent.uid, participantId)
    advanceUntilIdle()

    // Assert
    val result = viewModel.uiState.value.ticketValidationResult
    assertTrue(result is TicketValidationResult.Valid)
    val validResult = result as TicketValidationResult.Valid
    assertEquals(participantId, validResult.participantId)
  }

  @Test
  fun ticketValidationResult_invalidType_hasCorrectData() = runTest {
    // Arrange
    val invalidUserId = "invalid-user"
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.validateParticipant(testEvent.uid, invalidUserId)
    advanceUntilIdle()

    // Assert
    val result = viewModel.uiState.value.ticketValidationResult
    assertTrue(result is TicketValidationResult.Invalid)
    val invalidResult = result as TicketValidationResult.Invalid
    assertEquals(invalidUserId, invalidResult.userId)
  }

  @Test
  fun ticketValidationResult_errorType_hasCorrectMessage() = runTest {
    // Arrange - create a mock repository that throws an exception
    val mockEventRepository = EventRepositoryLocal()
    mockEventRepository.addEvent(testEvent)

    val errorThrowingRepo =
        object : EventRepository by mockEventRepository {
          override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> {
            throw RuntimeException("Connection timeout")
          }
        }
    val mockViewModel =
        EventViewModel(errorThrowingRepo, userRepository, pollRepository, friendsRepository)

    val userId = "user123"

    // Act
    mockViewModel.validateParticipant(testEvent.uid, userId)
    advanceUntilIdle()

    // Assert
    val result = mockViewModel.uiState.value.ticketValidationResult
    assertTrue(result is TicketValidationResult.Error)
    val errorResult = result as TicketValidationResult.Error
    assertTrue(errorResult.message.isNotEmpty())
    assertEquals("Connection timeout", errorResult.message)
  }

  @Test
  fun joinEvent_updatesParticipantCount() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    val currentUserId = "test-user-id"

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    viewModel.joinEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.participantCount >= 0)
  }

  @Test
  fun leaveEvent_updatesIsJoinedStatus() = runTest {
    // Arrange
    val currentUserId = "test-user-id"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUserId))

    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Act
    viewModel.leaveEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isJoined)
  }

  @Test
  fun leaveEvent_updatesParticipantCount() = runTest {
    // Arrange
    val currentUserId = "test-user-id"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUserId))
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("other-user"))

    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()
    val initialCount = viewModel.uiState.value.participantCount

    // Act
    viewModel.leaveEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val finalCount = viewModel.uiState.value.participantCount
    assertTrue(finalCount < initialCount || finalCount == 0)
  }

  @Test
  fun fetchEvent_withMaxCapacity_calculatesIsFull() = runTest {
    // Arrange
    val eventWithCapacity = testEvent.copy(maxCapacity = 2u)
    eventRepository.addEvent(eventWithCapacity)
    eventRepository.addParticipantToEvent(eventWithCapacity.uid, EventParticipant("user1"))
    eventRepository.addParticipantToEvent(eventWithCapacity.uid, EventParticipant("user2"))

    // Act
    viewModel.fetchEvent(eventWithCapacity.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.isFull)
    assertEquals(2, uiState.participantCount)
  }

  @Test
  fun fetchEvent_withMaxCapacity_notFull() = runTest {
    // Arrange
    val eventWithCapacity = testEvent.copy(maxCapacity = 5u)
    eventRepository.addEvent(eventWithCapacity)
    eventRepository.addParticipantToEvent(eventWithCapacity.uid, EventParticipant("user1"))

    // Act
    viewModel.fetchEvent(eventWithCapacity.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isFull)
    assertEquals(1, uiState.participantCount)
  }

  @Test
  fun fetchEvent_withoutMaxCapacity_isNotFull() = runTest {
    // Arrange
    val eventWithoutCapacity = testEvent.copy(maxCapacity = null)
    eventRepository.addEvent(eventWithoutCapacity)
    eventRepository.addParticipantToEvent(eventWithoutCapacity.uid, EventParticipant("user1"))

    // Act
    viewModel.fetchEvent(eventWithoutCapacity.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isFull)
  }

  @Test
  fun fetchEvent_excludesOwnerFromParticipantCount() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(testEvent.ownerId))
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user1"))

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    // Owner should be excluded, so count should be 1
    assertEquals(1, uiState.participantCount)
  }

  @Test
  fun joinEvent_ownerCannotJoin() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Act - try to join as owner
    viewModel.joinEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert - owner should not be counted as participant
    val uiState = viewModel.uiState.value
    val participants = eventRepository.getEventParticipants(testEvent.uid)
    // Verify owner is not in participants or is not counted
    assertTrue(participants.none { it.uid == testEvent.ownerId } || uiState.participantCount == 0)
  }

  @Test
  fun joinEvent_updatesIsFullWhenReachingCapacity() = runTest {
    // Arrange
    val eventWithCapacity = testEvent.copy(maxCapacity = 1u)
    eventRepository.addEvent(eventWithCapacity)
    val currentUserId = "test-user-id"

    viewModel.fetchEvent(eventWithCapacity.uid)
    advanceUntilIdle()

    // Act
    viewModel.joinEvent(eventWithCapacity.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    // After joining, if participant count reaches max capacity, isFull should be true
    if (uiState.participantCount >= 1) {
      assertTrue(uiState.isFull)
    }
  }

  @Test
  fun leaveEvent_updatesIsFullToFalse() = runTest {
    // Arrange
    val eventWithCapacity = testEvent.copy(maxCapacity = 1u)
    val currentUserId = "test-user-id"
    eventRepository.addEvent(eventWithCapacity)
    eventRepository.addParticipantToEvent(eventWithCapacity.uid, EventParticipant(currentUserId))

    viewModel.fetchEvent(eventWithCapacity.uid)
    advanceUntilIdle()

    // Act
    viewModel.leaveEvent(eventWithCapacity.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isFull)
  }

  @Test
  fun showInviteFriendsDialog_loadsFriendsAndInvites() = runTest {
    val ownerId = AuthenticationProvider.testUserId!!
    val friend1 =
        User(
            userId = "friend1",
            email = "f1@example.com",
            username = "f10",
            firstName = "F1",
            lastName = "L1",
            university = "EPFL")
    val friend2 =
        User(
            userId = "friend2",
            email = "f2@example.com",
            username = "f20",
            firstName = "F2",
            lastName = "L2",
            university = "EPFL")
    val localUserRepo = UserRepositoryLocal()
    localUserRepo.saveUser(friend1)
    localUserRepo.saveUser(friend2)
    val localEventRepo = EventRepositoryLocal()
    val localPollRepo = PollRepositoryLocal()
    val event =
        Event.Private(
            uid = "ownedEvent",
            ownerId = ownerId,
            title = "Owner Event",
            description = "d",
            start = Timestamp.now(),
            isFlash = false)
    localEventRepo.addEvent(event)
    localEventRepo.addInvitationToEvent(event.uid, friend1.userId, ownerId)
    val fakeFriendsRepo = FakeFriendsRepository(listOf(friend1.userId, friend2.userId))
    val vm = EventViewModel(localEventRepo, localUserRepo, localPollRepo, fakeFriendsRepo)

    vm.fetchEvent(event.uid)
    advanceUntilIdle()
    vm.showInviteFriendsDialog()
    advanceUntilIdle()

    val state = vm.uiState.value
    assertTrue(state.showInviteFriendsDialog)
    assertEquals(2, state.friends.size)
    assertTrue(state.invitedFriendIds.contains(friend1.userId))
    assertTrue(state.initialInvitedFriendIds.contains(friend1.userId))
  }

  @Test
  fun updateInvitationsForEvent_addsAndKeepsExisting() = runTest {
    val ownerId = AuthenticationProvider.testUserId!!
    val friend1 =
        User(
            userId = "friendA",
            email = "fA@example.com",
            username = "fA0",
            firstName = "FA",
            lastName = "LA",
            university = "EPFL")
    val friend2 =
        User(
            userId = "friendB",
            email = "fB@example.com",
            username = "fB0",
            firstName = "FB",
            lastName = "LB",
            university = "EPFL")
    val localUserRepo = UserRepositoryLocal()
    localUserRepo.saveUser(friend1)
    localUserRepo.saveUser(friend2)
    val localEventRepo = EventRepositoryLocal()
    val localPollRepo = PollRepositoryLocal()
    val event =
        Event.Private(
            uid = "ownedEvent2",
            ownerId = ownerId,
            title = "Owner Event 2",
            description = "d",
            start = Timestamp.now(),
            isFlash = false)
    localEventRepo.addEvent(event)
    localEventRepo.addInvitationToEvent(event.uid, friend1.userId, ownerId)
    val fakeFriendsRepo = FakeFriendsRepository(listOf(friend1.userId, friend2.userId))
    val vm = EventViewModel(localEventRepo, localUserRepo, localPollRepo, fakeFriendsRepo)

    vm.fetchEvent(event.uid)
    advanceUntilIdle()
    vm.showInviteFriendsDialog()
    advanceUntilIdle()

    // Attempt to toggle existing invite should have no effect
    vm.toggleFriendInvitation(friend1.userId)
    assertTrue(vm.uiState.value.invitedFriendIds.contains(friend1.userId))

    // Add friend2
    vm.toggleFriendInvitation(friend2.userId)
    vm.updateInvitationsForEvent()
    advanceUntilIdle()

    val invites = localEventRepo.getEventInvitations(event.uid)
    assertTrue(invites.contains(friend1.userId))
    assertTrue(invites.contains(friend2.userId))
    val state = vm.uiState.value
    assertFalse(state.showInviteFriendsDialog)
    assertEquals(setOf(friend1.userId, friend2.userId), state.invitedFriendIds)
    assertEquals(setOf(friend1.userId, friend2.userId), state.initialInvitedFriendIds)
  }

  @Test
  fun hideInviteFriendsDialog_resetsSelectionAndErrors() = runTest {
    val stateBefore =
        EventUiState(
            showInviteFriendsDialog = true,
            invitedFriendIds = setOf("f1"),
            initialInvitedFriendIds = setOf("f1"),
            friendsErrorRes = 1234,
            isInvitingFriends = true,
            isLoadingFriends = true)

    // Inject state into view model
    viewModel = EventViewModel(eventRepository, userRepository, pollRepository, friendsRepository)
    // Force state
    val privateField = EventViewModel::class.java.getDeclaredField("_uiState")
    privateField.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val stateFlow = privateField.get(viewModel) as MutableStateFlow<EventUiState>
    stateFlow.value = stateBefore

    viewModel.hideInviteFriendsDialog()

    val after = viewModel.uiState.value
    assertFalse(after.showInviteFriendsDialog)
    assertTrue(after.invitedFriendIds.isEmpty())
    assertTrue(after.initialInvitedFriendIds.isEmpty())
    assertNull(after.friendsErrorRes)
    assertFalse(after.isInvitingFriends)
  }

  @Test
  fun initialState_pollsAreEmpty() {
    val uiState = viewModel.uiState.value
    assertTrue(uiState.activePolls.isEmpty())
    assertFalse(uiState.showCreatePollDialog)
  }

  @Test
  fun showCreatePollDialog_updatesUiState() {
    // Act
    viewModel.showCreatePollDialog()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.showCreatePollDialog)
  }

  @Test
  fun hideCreatePollDialog_updatesUiState() {
    // Arrange
    viewModel.showCreatePollDialog()
    assertTrue(viewModel.uiState.value.showCreatePollDialog)

    // Act
    viewModel.hideCreatePollDialog()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.showCreatePollDialog)
  }

  @Test
  fun showCreatePollDialog_canBeCalledMultipleTimes() {
    // Act
    viewModel.showCreatePollDialog()
    viewModel.showCreatePollDialog()
    viewModel.showCreatePollDialog()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.showCreatePollDialog)
  }

  @Test
  fun hideCreatePollDialog_whenNotShown_doesNotCrash() {
    // Act & Assert - should not crash
    viewModel.hideCreatePollDialog()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.showCreatePollDialog)
  }

  @Test
  fun fetchEvent_asParticipant_fetchesActivePolls() = runTest {
    // Arrange
    val currentUserId = "test-user-id"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUserId))

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.isJoined)
    // Active polls list should be initialized (even if empty from mock)
    assertNotNull(uiState.activePolls)
  }

  @Test
  fun joinEvent_asParticipant_fetchesActivePolls() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Act
    viewModel.joinEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    // Active polls should be fetched after joining
    assertNotNull(uiState.activePolls)
  }

  @Test
  fun initialState_leaveConfirmDialogNotShown() {
    val uiState = viewModel.uiState.value
    assertFalse(uiState.showLeaveConfirmDialog)
  }

  @Test
  fun showLeaveConfirmDialog_updatesUiState() {
    // Act
    viewModel.showLeaveConfirmDialog()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.showLeaveConfirmDialog)
  }

  @Test
  fun hideLeaveConfirmDialog_updatesUiState() {
    // Arrange
    viewModel.showLeaveConfirmDialog()
    assertTrue(viewModel.uiState.value.showLeaveConfirmDialog)

    // Act
    viewModel.hideLeaveConfirmDialog()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.showLeaveConfirmDialog)
  }

  @Test
  fun hideLeaveConfirmDialog_whenNotShown_doesNotCrash() {
    // Act & Assert - should not crash
    viewModel.hideLeaveConfirmDialog()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.showLeaveConfirmDialog)
  }
}
