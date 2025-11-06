package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditActivitiesViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: TestableUserRepositoryLocal
  private lateinit var viewModel: EditActivitiesViewModel
  private val testUser =
      User(
          userId = "test_user",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthday = "01/01/2000",
          hobbies = listOf("Football", "Coding", "Reading"),
          bio = "Test bio",
          profilePictureUrl = null)

  @Before
  fun setUp() = runTest {
    repository = TestableUserRepositoryLocal()
    repository.saveUser(testUser)
    viewModel = EditActivitiesViewModel(repository, testUser.userId)
  }

  @Test
  fun `initial state loads user activities correctly`() = runTest {
    kotlinx.coroutines.delay(200)

    assertEquals(3, viewModel.selectedActivities.value.size)
    assertTrue(viewModel.selectedActivities.value.contains("Football"))
    assertTrue(viewModel.selectedActivities.value.contains("Coding"))
    assertTrue(viewModel.selectedActivities.value.contains("Reading"))
    assertTrue(viewModel.uiState.value is EditActivitiesViewModel.UiState.Idle)
  }

  @Test
  fun `initial state with no activities`() = runTest {
    val userWithNoHobbies = testUser.copy(hobbies = emptyList())
    val testRepository = TestableUserRepositoryLocal()
    testRepository.saveUser(userWithNoHobbies)
    val testViewModel = EditActivitiesViewModel(testRepository, testUser.userId)

    kotlinx.coroutines.delay(200)

    assertEquals(0, testViewModel.selectedActivities.value.size)
    assertTrue(testViewModel.uiState.value is EditActivitiesViewModel.UiState.Idle)
  }

  @Test
  fun `initial state handles user not found`() = runTest {
    val testRepository = TestableUserRepositoryLocal()
    val testViewModel = EditActivitiesViewModel(testRepository, "non_existent_user")

    kotlinx.coroutines.delay(200)

    assertEquals(0, testViewModel.selectedActivities.value.size)
    assertTrue(testViewModel.uiState.value is EditActivitiesViewModel.UiState.Idle)
  }

  @Test
  fun `initial state handles repository error`() = runTest {
    val testRepository = TestableUserRepositoryLocal()
    testRepository.shouldThrowOnLoad = RuntimeException("Load failed")
    val testViewModel = EditActivitiesViewModel(testRepository, testUser.userId)

    kotlinx.coroutines.delay(200)

    assertTrue(testViewModel.uiState.value is EditActivitiesViewModel.UiState.Error)
  }

  @Test
  fun `filteredActivities initially contains all activities`() = runTest {
    kotlinx.coroutines.delay(100)

    assertTrue(viewModel.filteredActivities.value.isNotEmpty())
    assertTrue(viewModel.filteredActivities.value.contains("Football"))
    assertTrue(viewModel.filteredActivities.value.contains("Tennis"))
    assertTrue(viewModel.filteredActivities.value.contains("AI"))
    assertTrue(viewModel.filteredActivities.value.contains("Painting"))
  }

  @Test
  fun `filteredActivities are sorted alphabetically`() = runTest {
    kotlinx.coroutines.delay(100)

    val activities = viewModel.filteredActivities.value
    val sortedActivities = activities.sorted()
    assertEquals(sortedActivities, activities)
  }

  @Test
  fun `updateSearchQuery filters activities case-insensitive`() = runTest {
    kotlinx.coroutines.delay(100)

    viewModel.updateSearchQuery("foot")

    assertEquals("foot", viewModel.searchQuery.value)
    assertTrue(viewModel.filteredActivities.value.contains("Football"))
    assertFalse(viewModel.filteredActivities.value.contains("Tennis"))
  }

  @Test
  fun `updateSearchQuery with uppercase filters correctly`() = runTest {
    kotlinx.coroutines.delay(100)

    viewModel.updateSearchQuery("FOOT")

    assertTrue(viewModel.filteredActivities.value.contains("Football"))
    assertFalse(viewModel.filteredActivities.value.contains("Tennis"))
  }

  @Test
  fun `updateSearchQuery with empty string shows all activities`() = runTest {
    kotlinx.coroutines.delay(100)
    val allActivitiesCount = viewModel.filteredActivities.value.size

    viewModel.updateSearchQuery("foot")
    assertTrue(viewModel.filteredActivities.value.size < allActivitiesCount)

    viewModel.updateSearchQuery("")
    assertEquals(allActivitiesCount, viewModel.filteredActivities.value.size)
  }

  @Test
  fun `updateSearchQuery with blank string shows all activities`() = runTest {
    kotlinx.coroutines.delay(100)
    val allActivitiesCount = viewModel.filteredActivities.value.size

    viewModel.updateSearchQuery("   ")
    assertEquals(allActivitiesCount, viewModel.filteredActivities.value.size)
  }

  @Test
  fun `updateSearchQuery with no matches returns empty list`() = runTest {
    kotlinx.coroutines.delay(100)

    viewModel.updateSearchQuery("xyz123nonexistent")

    assertEquals(0, viewModel.filteredActivities.value.size)
  }

  @Test
  fun `updateSearchQuery with partial match filters correctly`() = runTest {
    kotlinx.coroutines.delay(100)

    viewModel.updateSearchQuery("ball")

    assertTrue(viewModel.filteredActivities.value.contains("Football"))
    assertTrue(viewModel.filteredActivities.value.contains("Volleyball"))
    assertTrue(viewModel.filteredActivities.value.contains("Baseball"))
    assertFalse(viewModel.filteredActivities.value.contains("Tennis"))
  }

  @Test
  fun `toggleActivity adds new activity`() = runTest {
    kotlinx.coroutines.delay(200)

    assertFalse(viewModel.selectedActivities.value.contains("Tennis"))

    viewModel.toggleActivity("Tennis")

    assertTrue(viewModel.selectedActivities.value.contains("Tennis"))
    assertEquals(4, viewModel.selectedActivities.value.size)
  }

  @Test
  fun `toggleActivity removes existing activity`() = runTest {
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.selectedActivities.value.contains("Football"))

    viewModel.toggleActivity("Football")

    assertFalse(viewModel.selectedActivities.value.contains("Football"))
    assertEquals(2, viewModel.selectedActivities.value.size)
  }

  @Test
  fun `toggleActivity multiple times toggles correctly`() = runTest {
    kotlinx.coroutines.delay(200)

    viewModel.toggleActivity("Tennis")
    assertTrue(viewModel.selectedActivities.value.contains("Tennis"))

    viewModel.toggleActivity("Tennis")
    assertFalse(viewModel.selectedActivities.value.contains("Tennis"))

    viewModel.toggleActivity("Tennis")
    assertTrue(viewModel.selectedActivities.value.contains("Tennis"))
  }

  @Test
  fun `toggleActivity preserves other selections`() = runTest {
    kotlinx.coroutines.delay(200)

    val initialSize = viewModel.selectedActivities.value.size
    viewModel.toggleActivity("Tennis")

    assertTrue(viewModel.selectedActivities.value.contains("Football"))
    assertTrue(viewModel.selectedActivities.value.contains("Coding"))
    assertTrue(viewModel.selectedActivities.value.contains("Reading"))
    assertTrue(viewModel.selectedActivities.value.contains("Tennis"))
    assertEquals(initialSize + 1, viewModel.selectedActivities.value.size)
  }

  @Test
  fun `saveActivities saves successfully with valid data`() = runTest {
    kotlinx.coroutines.delay(200)

    viewModel.toggleActivity("Tennis")
    viewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertTrue(savedUser != null)
    assertTrue(savedUser!!.hobbies.contains("Tennis"))
    assertTrue(savedUser.hobbies.contains("Football"))
    assertTrue(viewModel.uiState.value is EditActivitiesViewModel.UiState.Success)
  }

  @Test
  fun `saveActivities updates timestamp`() = runTest {
    kotlinx.coroutines.delay(200)

    val originalTimestamp = testUser.updatedAt
    viewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertTrue(savedUser != null)
    assertTrue(savedUser!!.updatedAt > originalTimestamp)
  }

  @Test
  fun `saveActivities handles user not found`() = runTest {
    kotlinx.coroutines.delay(200)
    val testRepository = TestableUserRepositoryLocal()
    val testViewModel = EditActivitiesViewModel(testRepository, "non_existent_user")

    kotlinx.coroutines.delay(200)

    testViewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    assertTrue(testViewModel.uiState.value is EditActivitiesViewModel.UiState.Error)
    val savedUser = testRepository.getUserById("non_existent_user")
    assertTrue(savedUser == null)
  }

  @Test
  fun `saveActivities handles repository error`() = runTest {
    kotlinx.coroutines.delay(200)

    repository.shouldThrowOnSave = RuntimeException("Save failed")
    viewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is EditActivitiesViewModel.UiState.Error)
  }

  @Test
  fun `saveActivities converts set to list`() = runTest {
    kotlinx.coroutines.delay(200)

    viewModel.toggleActivity("Tennis")
    viewModel.toggleActivity("Running")
    viewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertTrue(savedUser != null)
    assertTrue(savedUser!!.hobbies is List)
    assertTrue(savedUser.hobbies.contains("Tennis"))
    assertTrue(savedUser.hobbies.contains("Running"))
  }

  @Test
  fun `saveActivities with empty selection`() = runTest {
    kotlinx.coroutines.delay(200)

    // Remove all activities
    viewModel.toggleActivity("Football")
    viewModel.toggleActivity("Coding")
    viewModel.toggleActivity("Reading")

    viewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertTrue(savedUser != null)
    assertTrue(savedUser!!.hobbies.isEmpty())
    assertTrue(viewModel.uiState.value is EditActivitiesViewModel.UiState.Success)
  }

  @Test
  fun `saveActivities multiple times saves correctly`() = runTest {
    kotlinx.coroutines.delay(200)

    viewModel.toggleActivity("Tennis")
    viewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    viewModel.toggleActivity("Running")
    viewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertTrue(savedUser != null)
    assertTrue(savedUser!!.hobbies.contains("Running"))
  }

  @Test
  fun `resetState changes state to Idle`() = runTest {
    kotlinx.coroutines.delay(200)

    repository.shouldThrowOnSave = RuntimeException("Test error")
    viewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is EditActivitiesViewModel.UiState.Error)

    viewModel.resetState()

    assertTrue(viewModel.uiState.value is EditActivitiesViewModel.UiState.Idle)
  }

  @Test
  fun `uiState transitions correctly during save`() = runTest {
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is EditActivitiesViewModel.UiState.Idle)

    viewModel.saveActivities()

    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is EditActivitiesViewModel.UiState.Success)
  }

  @Test
  fun `search and toggle workflow`() = runTest {
    kotlinx.coroutines.delay(200)

    viewModel.updateSearchQuery("tenn")
    assertTrue(viewModel.filteredActivities.value.contains("Tennis"))

    viewModel.toggleActivity("Tennis")
    assertTrue(viewModel.selectedActivities.value.contains("Tennis"))

    viewModel.updateSearchQuery("")
    assertTrue(viewModel.selectedActivities.value.contains("Tennis"))
  }

  @Test
  fun `all available activities are present`() = runTest {
    kotlinx.coroutines.delay(100)

    val activities = viewModel.filteredActivities.value

    // Sports
    assertTrue(activities.contains("Football"))
    assertTrue(activities.contains("Tennis"))
    assertTrue(activities.contains("Running"))

    // Arts
    assertTrue(activities.contains("Painting"))
    assertTrue(activities.contains("Photo"))
    assertTrue(activities.contains("Film"))

    // Technology
    assertTrue(activities.contains("AI"))
    assertTrue(activities.contains("Web"))
    assertTrue(activities.contains("Robotics"))

    // Science
    assertTrue(activities.contains("Biology"))
    assertTrue(activities.contains("Research"))
  }

  @Test
  fun `selectedActivities maintains uniqueness`() = runTest {
    kotlinx.coroutines.delay(200)

    val initialSize = viewModel.selectedActivities.value.size

    viewModel.toggleActivity("Tennis")
    assertTrue(viewModel.selectedActivities.value.contains("Tennis"))
    assertEquals(initialSize + 1, viewModel.selectedActivities.value.size)

    viewModel.toggleActivity("Tennis")
    assertFalse(viewModel.selectedActivities.value.contains("Tennis"))
    assertEquals(initialSize, viewModel.selectedActivities.value.size)

    viewModel.toggleActivity("Tennis")
    assertTrue(viewModel.selectedActivities.value.contains("Tennis"))
    assertEquals(initialSize + 1, viewModel.selectedActivities.value.size)
  }

  @Test
  fun `load activities with special characters`() = runTest {
    val userWithSpecialActivities = testUser.copy(hobbies = listOf("AI", "Web", "Mobile"))
    val testRepository = TestableUserRepositoryLocal()
    testRepository.saveUser(userWithSpecialActivities)
    val testViewModel = EditActivitiesViewModel(testRepository, testUser.userId)

    kotlinx.coroutines.delay(200)

    assertTrue(testViewModel.selectedActivities.value.contains("AI"))
    assertTrue(testViewModel.selectedActivities.value.contains("Web"))
    assertTrue(testViewModel.selectedActivities.value.contains("Mobile"))
  }

  @Test
  fun `search with special characters`() = runTest {
    kotlinx.coroutines.delay(100)

    viewModel.updateSearchQuery("AR/VR")

    assertTrue(viewModel.filteredActivities.value.contains("AR/VR"))
    assertEquals(1, viewModel.filteredActivities.value.size)
  }

  /**
   * TestableUserRepositoryLocal wraps UserRepositoryLocal to add error injection capabilities for
   * testing error scenarios.
   */
  private class TestableUserRepositoryLocal : UserRepository {
    private val delegate = UserRepositoryLocal()
    var shouldThrowOnSave: Throwable? = null
    var shouldThrowOnLoad: Throwable? = null

    override suspend fun getUserById(userId: String): User? {
      shouldThrowOnLoad?.let { throw it }
      return delegate.getUserById(userId)
    }

    override suspend fun saveUser(user: User) {
      shouldThrowOnSave?.let { throw it }
      delegate.saveUser(user)
    }

    override suspend fun getUserByEmail(email: String) = delegate.getUserByEmail(email)

    override suspend fun getAllUsers() = delegate.getAllUsers()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        delegate.getUsersPaginated(limit, lastUserId)

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
      shouldThrowOnSave?.let { throw it }
      delegate.updateUser(userId, updates)
    }

    override suspend fun deleteUser(userId: String) = delegate.deleteUser(userId)

    override suspend fun getUsersByUniversity(university: String) =
        delegate.getUsersByUniversity(university)

    override suspend fun getUsersByHobby(hobby: String) = delegate.getUsersByHobby(hobby)

    override suspend fun getNewUid() = delegate.getNewUid()

    override suspend fun getJoinedEvents(userId: String) = delegate.getJoinedEvents(userId)

    override suspend fun addEventToUser(eventId: String, userId: String) =
        delegate.addEventToUser(eventId, userId)

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        delegate.addInvitationToUser(eventId, userId, fromUserId)

    override suspend fun getInvitations(userId: String) = delegate.getInvitations(userId)

    override suspend fun acceptInvitation(eventId: String, userId: String) =
        delegate.acceptInvitation(eventId, userId)

    override suspend fun declineInvitation(eventId: String, userId: String) =
        delegate.declineInvitation(eventId, userId)

    override suspend fun joinEvent(eventId: String, userId: String) =
        delegate.joinEvent(eventId, userId)

    override suspend fun leaveEvent(eventId: String, userId: String) =
        delegate.leaveEvent(eventId, userId)

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        delegate.sendInvitation(eventId, fromUserId, toUserId)

    override suspend fun addFavoriteEvent(userId: String, eventId: String) =
        delegate.addFavoriteEvent(userId, eventId)

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) =
        delegate.removeFavoriteEvent(userId, eventId)

    override suspend fun getFavoriteEvents(userId: String) = delegate.getFavoriteEvents(userId)
  }
}
