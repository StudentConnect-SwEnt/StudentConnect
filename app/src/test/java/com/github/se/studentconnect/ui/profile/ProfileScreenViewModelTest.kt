package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventStatistics
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ProfileScreenViewModelTest {

  private lateinit var testDispatcher: TestDispatcher
  private lateinit var testScope: TestScope
  private lateinit var userRepository: TestUserRepository
  private lateinit var friendsRepository: TestFriendsRepository
  private lateinit var eventRepository: TestEventRepository
  private lateinit var viewModel: ProfileScreenViewModel

  private val testUser =
      User(
          userId = "test_user_123",
          username = "testuser",
          firstName = "Test",
          lastName = "User",
          email = "test@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthdate = "01/01/2000",
          hobbies = listOf("Reading", "Coding"),
          bio = "Test bio")

  @Before
  fun setUp() {
    testDispatcher = StandardTestDispatcher()
    testScope = TestScope(testDispatcher)
    Dispatchers.setMain(testDispatcher)

    userRepository = TestUserRepository(testUser)
    friendsRepository = TestFriendsRepository()
    eventRepository = TestEventRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `viewModel initializes and loads user profile`() =
      testScope.runTest {
        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(testUser, viewModel.user.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `viewModel loads friends count correctly`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("friend1", "friend2", "friend3")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(3, viewModel.friendsCount.value)
      }

  @Test
  fun `viewModel loads events count correctly`() =
      testScope.runTest {
        userRepository.joinedEventIds = listOf("event1", "event2", "event3", "event4")
        eventRepository.existingEventIds = setOf("event1", "event2", "event3", "event4")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(4, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel handles empty friends list`() =
      testScope.runTest {
        friendsRepository.friendsList = emptyList()

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(0, viewModel.friendsCount.value)
      }

  @Test
  fun `viewModel handles empty events list`() =
      testScope.runTest {
        userRepository.joinedEventIds = emptyList()

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(0, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel shows loading state during data fetch`() =
      testScope.runTest {
        userRepository.delay = 100L

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel handles user fetch error`() =
      testScope.runTest {
        userRepository.shouldThrowError = true

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertNull(viewModel.user.value)
        assertEquals("User fetch failed", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel handles friends fetch error gracefully`() =
      testScope.runTest {
        friendsRepository.shouldThrowError = true

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(testUser, viewModel.user.value)
        assertEquals(0, viewModel.friendsCount.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `viewModel handles events fetch error gracefully`() =
      testScope.runTest {
        userRepository.shouldThrowErrorOnGetJoinedEvents = true

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(testUser, viewModel.user.value)
        assertEquals(0, viewModel.eventsCount.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `loadUserProfile reloads user data`() =
      testScope.runTest {
        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        val updatedUser = testUser.copy(firstName = "Updated")
        userRepository.user = updatedUser

        viewModel.loadUserProfile()
        advanceUntilIdle()

        assertEquals(updatedUser, viewModel.user.value)
      }

  @Test
  fun `loadUserProfile updates friends count`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("friend1")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()
        assertEquals(1, viewModel.friendsCount.value)

        friendsRepository.friendsList = listOf("friend1", "friend2", "friend3")

        viewModel.loadUserProfile()
        advanceUntilIdle()

        assertEquals(3, viewModel.friendsCount.value)
      }

  @Test
  fun `loadUserProfile updates events count`() =
      testScope.runTest {
        userRepository.joinedEventIds = listOf("event1")
        eventRepository.existingEventIds = setOf("event1", "event2")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()
        assertEquals(1, viewModel.eventsCount.value)

        userRepository.joinedEventIds = listOf("event1", "event2")

        viewModel.loadUserProfile()
        advanceUntilIdle()

        assertEquals(2, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel initializes pinned events as empty`() =
      testScope.runTest {
        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertTrue(viewModel.pinnedEvents.value.isEmpty())
      }

  @Test
  fun `loadPinnedEvents returns empty list`() =
      testScope.runTest {
        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        viewModel.loadPinnedEvents()
        advanceUntilIdle()

        assertTrue(viewModel.pinnedEvents.value.isEmpty())
      }

  @Test
  fun `viewModel handles large friends count`() =
      testScope.runTest {
        friendsRepository.friendsList = (1..1000).map { "friend$it" }

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(1000, viewModel.friendsCount.value)
      }

  @Test
  fun `viewModel handles large events count`() =
      testScope.runTest {
        userRepository.joinedEventIds = (1..500).map { "event$it" }
        eventRepository.existingEventIds = (1..500).map { "event$it" }.toSet()

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(500, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel error state is cleared on successful reload`() =
      testScope.runTest {
        userRepository.shouldThrowError = true

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()
        assertEquals("User fetch failed", viewModel.error.value)

        userRepository.shouldThrowError = false
        viewModel.loadUserProfile()
        advanceUntilIdle()

        assertNull(viewModel.error.value)
        assertEquals(testUser, viewModel.user.value)
      }

  @Test
  fun `viewModel counts both joined and created events`() =
      testScope.runTest {
        // User has joined 2 events
        userRepository.joinedEventIds = listOf("event1", "event2")

        // User has created 3 events
        val createdEvent1 =
            Event.Private(
                uid = "event3",
                ownerId = testUser.userId,
                title = "Created Event 1",
                description = "Test",
                start = com.google.firebase.Timestamp.now(),
                isFlash = false)
        val createdEvent2 =
            Event.Private(
                uid = "event4",
                ownerId = testUser.userId,
                title = "Created Event 2",
                description = "Test",
                start = com.google.firebase.Timestamp.now(),
                isFlash = false)
        val createdEvent3 =
            Event.Private(
                uid = "event5",
                ownerId = testUser.userId,
                title = "Created Event 3",
                description = "Test",
                start = com.google.firebase.Timestamp.now(),
                isFlash = false)

        eventRepository.createdEvents = listOf(createdEvent1, createdEvent2, createdEvent3)
        eventRepository.existingEventIds = setOf("event1", "event2", "event3", "event4", "event5")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        // Should count 2 joined + 3 created = 5 total
        assertEquals(5, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel avoids counting duplicate events when user joined their own created event`() =
      testScope.runTest {
        // User has joined events including one they created
        userRepository.joinedEventIds = listOf("event1", "event2", "event3")

        // User has created events (event2 is also in joinedEvents)
        val createdEvent1 =
            Event.Private(
                uid = "event2",
                ownerId = testUser.userId,
                title = "Created Event 1",
                description = "Test",
                start = com.google.firebase.Timestamp.now(),
                isFlash = false)
        val createdEvent2 =
            Event.Private(
                uid = "event4",
                ownerId = testUser.userId,
                title = "Created Event 2",
                description = "Test",
                start = com.google.firebase.Timestamp.now(),
                isFlash = false)

        eventRepository.createdEvents = listOf(createdEvent1, createdEvent2)
        eventRepository.existingEventIds = setOf("event1", "event2", "event3", "event4")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        // Should count unique events: event1, event2, event3, event4 = 4 total (no duplicates)
        assertEquals(4, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel handles created events fetch error gracefully`() =
      testScope.runTest {
        // User has joined 2 events
        userRepository.joinedEventIds = listOf("event1", "event2")
        eventRepository.existingEventIds = setOf("event1", "event2")

        // EventRepository will throw an error when fetching created events
        eventRepository.shouldThrowError = true

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        // Should only count joined events since created events fetch failed
        assertEquals(2, viewModel.eventsCount.value)
        // User profile should still load successfully
        assertEquals(testUser, viewModel.user.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `viewModel excludes deleted events from count`() =
      testScope.runTest {
        // User has joined 4 events, but 2 of them have been deleted
        userRepository.joinedEventIds = listOf("event1", "event2", "event3", "event4")
        // Only event1 and event3 still exist
        eventRepository.existingEventIds = setOf("event1", "event3")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                eventRepository = eventRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        // Should only count existing events (event1 and event3), not deleted ones (event2 and
        // event4)
        assertEquals(2, viewModel.eventsCount.value)
      }

  // Test helper classes
  private class TestUserRepository(
      var user: User?,
      var delay: Long = 0L,
      var shouldThrowError: Boolean = false,
      var shouldThrowErrorOnGetJoinedEvents: Boolean = false,
      var joinedEventIds: List<String> = emptyList()
  ) : UserRepository {

    override suspend fun getUserById(userId: String): User? {
      if (delay > 0) delay(delay)
      if (shouldThrowError) throw Exception("User fetch failed")
      return if (userId == user?.userId) user else null
    }

    override suspend fun getJoinedEvents(userId: String): List<String> {
      if (shouldThrowErrorOnGetJoinedEvents) throw Exception("Events fetch failed")
      return joinedEventIds
    }

    override suspend fun saveUser(user: User) = Unit

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = emptyList<User>()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        emptyList<User>() to false

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

    override suspend fun deleteUser(userId: String) = Unit

    override suspend fun getUsersByUniversity(university: String) = emptyList<User>()

    override suspend fun getUsersByHobby(hobby: String) = emptyList<User>()

    override suspend fun getNewUid() = "new_uid"

    override suspend fun addEventToUser(eventId: String, userId: String) = Unit

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        Unit

    override suspend fun getInvitations(userId: String) = emptyList<Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun removeInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit

    override suspend fun addFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun getFavoriteEvents(userId: String) = emptyList<String>()

    override suspend fun checkUsernameAvailability(username: String) = true

    override suspend fun addPinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun removePinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun getPinnedEvents(userId: String) = emptyList<String>()
  }

  private class TestFriendsRepository(
      var friendsList: List<String> = emptyList(),
      var shouldThrowError: Boolean = false
  ) : FriendsRepository {

    override suspend fun getFriends(userId: String): List<String> {
      if (shouldThrowError) throw Exception("Friends fetch failed")
      return friendsList
    }

    override suspend fun getPendingRequests(userId: String): List<String> = emptyList()

    override suspend fun getSentRequests(userId: String): List<String> = emptyList()

    override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) = Unit

    override suspend fun acceptFriendRequest(userId: String, fromUserId: String) = Unit

    override suspend fun rejectFriendRequest(userId: String, fromUserId: String) = Unit

    override suspend fun cancelFriendRequest(userId: String, toUserId: String) = Unit

    override suspend fun removeFriend(userId: String, friendId: String) = Unit

    override suspend fun areFriends(userId: String, otherUserId: String) = false

    override suspend fun hasPendingRequest(fromUserId: String, toUserId: String) = false

    override fun observeFriendship(userId: String, otherUserId: String): Flow<Boolean> {
      TODO("Not yet implemented")
    }
  }

  private class TestEventRepository(
      var createdEvents: List<Event> = emptyList(),
      var shouldThrowError: Boolean = false,
      var existingEventIds: Set<String> = emptySet()
  ) : EventRepository {

    override fun getNewUid(): String = "new_event_uid"

    override suspend fun getAllVisibleEvents(): List<Event> = emptyList()

    override suspend fun getAllVisibleEventsSatisfying(predicate: (Event) -> Boolean): List<Event> =
        emptyList()

    override suspend fun getEventsByOrganization(organizationId: String): List<Event> {
      if (shouldThrowError) throw Exception("Events fetch failed")
      return createdEvents.filter { it.ownerId == organizationId }
    }

    override suspend fun getEvent(eventUid: String): Event {
      // If shouldThrowError is true, throw for getEventsByOrganization but not for getEvent
      // getEvent is used to verify event existence, so we need it to work separately

      // If existingEventIds is specified, use it to determine which events exist
      if (existingEventIds.isNotEmpty()) {
        if (eventUid in existingEventIds) {
          // Return a dummy event
          return Event.Private(
              uid = eventUid,
              ownerId = "owner",
              title = "Test Event",
              description = "Test",
              start = com.google.firebase.Timestamp.now(),
              isFlash = false)
        } else {
          throw IllegalArgumentException("Event $eventUid does not exist")
        }
      }

      // Otherwise, check in createdEvents or throw if not found
      return createdEvents.find { it.uid == eventUid }
          ?: throw IllegalArgumentException("Event $eventUid does not exist")
    }

    override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> =
        emptyList()

    override suspend fun addEvent(event: Event) = Unit

    override suspend fun editEvent(eventUid: String, newEvent: Event) = Unit

    override suspend fun deleteEvent(eventUid: String) = Unit

    override suspend fun addParticipantToEvent(eventUid: String, participant: EventParticipant) =
        Unit

    override suspend fun addInvitationToEvent(
        eventUid: String,
        invitedUser: String,
        currentUserId: String
    ) = Unit

    override suspend fun getEventInvitations(eventUid: String): List<String> = emptyList()

    override suspend fun removeInvitationFromEvent(
        eventUid: String,
        invitedUser: String,
        currentUserId: String
    ) = Unit

    override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) = Unit

    override suspend fun getEventStatistics(eventUid: String, followerCount: Int): EventStatistics {
      throw NotImplementedError("Not needed for tests")
    }
  }
}
