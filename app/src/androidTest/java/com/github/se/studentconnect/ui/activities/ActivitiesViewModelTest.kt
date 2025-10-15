package com.github.se.studentconnect.ui.activities

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.activities.ActivitiesViewModel
import com.github.se.studentconnect.ui.screen.activities.EventTab
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserRepositoryDummy : UserRepository {
  private val joinedEventsByUser = mutableMapOf<String, MutableList<String>>()

  override suspend fun getJoinedEvents(userId: String): List<String> {
    return joinedEventsByUser[userId] ?: emptyList()
  }

  override suspend fun addEventToUser(eventId: String, userId: String) {
    joinedEventsByUser.getOrPut(userId) { mutableListOf() }.add(eventId)
  }

  override suspend fun leaveEvent(eventId: String, userId: String) {
    joinedEventsByUser[userId]?.remove(eventId)
  }

  override suspend fun getUserById(userId: String): User? {
    TODO("Not yet implemented")
  }

  override suspend fun getUserByEmail(email: String): User? {
    TODO("Not yet implemented")
  }

  override suspend fun getAllUsers(): List<User> {
    TODO("Not yet implemented")
  }

  override suspend fun getUsersPaginated(
      limit: Int,
      lastUserId: String?
  ): Pair<List<User>, Boolean> {
    TODO("Not yet implemented")
  }

  override suspend fun saveUser(user: User) {
    TODO("Not yet implemented")
  }

  override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteUser(userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun getUsersByUniversity(university: String): List<User> {
    TODO("Not yet implemented")
  }

  override suspend fun getUsersByHobby(hobby: String): List<User> {
    TODO("Not yet implemented")
  }

  override suspend fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun getInvitations(userId: String): List<Invitation> {
    TODO("Not yet implemented")
  }

  override suspend fun acceptInvitation(eventId: String, userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun joinEvent(eventId: String, userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {
    TODO("Not yet implemented")
  }
}

@ExperimentalCoroutinesApi
class ActivitiesViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: ActivitiesViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepository

  private val testUserId = "testUser"

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryDummy()
    viewModel =
      ActivitiesViewModel(eventRepository = eventRepository, userRepository = userRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createTestEvent(uid: String, title: String): Event.Public {
    return Event.Public(
        uid = uid,
        title = title,
        description = "Test Description",
        start = Timestamp.now(),
        end = Timestamp.now(),
        location = Location(0.0, 0.0, "Test Location"),
        ownerId = "owner1",
        subtitle = "Test Subtitle",
        isFlash = false,
    )
  }

  @Test
  fun testInitialStateIsCorrect() {
    val uiState = viewModel.uiState.value
    assertEquals(true, uiState.events.isEmpty())
    assertEquals(EventTab.Upcoming, uiState.selectedTab)
  }

  @Test
  fun testTabSelectionWorks() {
    viewModel.onTabSelected(EventTab.Upcoming)
    assertEquals(EventTab.Upcoming, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun testRefreshEventsUpdatesList() = runTest {
    // Arrange
    val testEvent = createTestEvent("e1", "Event One")
    eventRepository.addEvent(testEvent)
    (userRepository as UserRepositoryDummy).addEventToUser(testEvent.uid, testUserId)

    // Act
    viewModel.refreshEvents(testUserId)
    advanceUntilIdle() // wait for all coroutines to finish

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
    assertEquals("Event One", uiState.events[0].title)
  }

  @Test
  fun testRefreshEventsWithNoJoinedEventsClearsList() = runTest {
    val testEvent = createTestEvent("e1", "Initial Event")
    eventRepository.addEvent(testEvent)
    (userRepository as UserRepositoryDummy).addEventToUser(testEvent.uid, testUserId)

    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()
    assertEquals(
        "Setup failed: event should be present initially", 1, viewModel.uiState.value.events.size)

    // Act
    (userRepository as UserRepositoryDummy).leaveEvent(testEvent.uid, testUserId)
    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()

    // Assert
    assertEquals(true, viewModel.uiState.value.events.isEmpty())
  }

  @Test
  fun testRefreshEventsWithMultipleEvents() = runTest {
    // Arrange
    val event1 = createTestEvent("e1", "Event One")
    val event2 = createTestEvent("e2", "Event Two")
    val event3 = createTestEvent("e3", "Event Three")

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)
    (userRepository as UserRepositoryDummy).addEventToUser(event1.uid, testUserId)
    (userRepository as UserRepositoryDummy).addEventToUser(event2.uid, testUserId)
    (userRepository as UserRepositoryDummy).addEventToUser(event3.uid, testUserId)

    // Act
    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(3, uiState.events.size)
    val titles = uiState.events.map { it.title }.toSet()
    assertEquals(setOf("Event One", "Event Two", "Event Three"), titles)
  }

  @Test
  fun testRefreshEventsDoesNotAffectSelectedTab() = runTest {
    // Arrange

    val testEvent = createTestEvent("e1", "Event One")
    eventRepository.addEvent(testEvent)
    (userRepository as UserRepositoryDummy).addEventToUser(testEvent.uid, testUserId)

    // Act
    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()

    // Assert
    assertEquals(1, viewModel.uiState.value.events.size)
  }
}
