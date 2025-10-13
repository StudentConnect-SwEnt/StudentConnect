package com.github.se.studentconnect.ui.activities

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

// Implémentation factice (dummy) pour satisfaire la dépendance du ViewModel
class UserRepositoryDummy : UserRepository {
  override fun leaveEvent(eventId: String, userId: String) {
    TODO("Not yet implemented")
  }

  override fun getUserById(
      userId: String,
      onSuccess: (User?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun getUserByEmail(
      email: String,
      onSuccess: (User?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    TODO("Not yet implemented")
  }

  override fun getUsersPaginated(
      limit: Int,
      lastUserId: String?,
      onSuccess: (List<User>, Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    TODO("Not yet implemented")
  }

  override fun updateUser(
      userId: String,
      updates: Map<String, Any?>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    TODO("Not yet implemented")
  }

  override fun getUsersByUniversity(
      university: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun getUsersByHobby(
      hobby: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }
}

@ExperimentalCoroutinesApi
class ActivitiesViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: ActivitiesViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepository

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
    assertEquals(EventTab.JoinedEvents, uiState.selectedTab)
  }

  @Test
  fun testTabSelectionWorks() {
    viewModel.onTabSelected(EventTab.Invitations)
    assertEquals(EventTab.Invitations, viewModel.uiState.value.selectedTab)

    viewModel.onTabSelected(EventTab.JoinedEvents)
    assertEquals(EventTab.JoinedEvents, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun testRefreshEventsUpdatesList() = runTest {
    // Arrange
    val testEvent = createTestEvent("e1", "Event One")
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.refreshEvents("testUser")
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)
    assertEquals("Event One", uiState.events[0].title)
  }

  @Test
  fun testRefreshEventsWithNoEventsClearsList() = runTest {
    // Arrange: Start with one event in the state
    val testEvent = createTestEvent("e1", "Initial Event")
    eventRepository.addEvent(testEvent)
    viewModel.refreshEvents("testUser")
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(1, viewModel.uiState.value.events.size)

    // Act: Remove the event from the repo and refresh again
    eventRepository.deleteEvent("e1")
    viewModel.refreshEvents("testUser")
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert: The list in the state should now be empty
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

    // Act
    viewModel.refreshEvents("testUser")
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(3, uiState.events.size)
  }

  @Test
  fun testRefreshEventsDoesNotAffectSelectedTab() = runTest {
    // Arrange
    viewModel.onTabSelected(EventTab.Invitations)
    val testEvent = createTestEvent("e1", "Event One")
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.refreshEvents("testUser")
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    assertEquals(EventTab.Invitations, viewModel.uiState.value.selectedTab)
    assertEquals(1, viewModel.uiState.value.events.size)
  }
}
