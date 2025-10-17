package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
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
class EditActivitiesViewModelSimpleTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: SimpleFakeUserRepository
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
  fun setUp() {
    repository = SimpleFakeUserRepository(testUser)
    viewModel = EditActivitiesViewModel(repository, testUser.userId)
  }

  @Test
  fun `initial state loads user activities correctly`() = runTest {
    // Wait for initial load to complete
    Thread.sleep(600) // Wait for the 500ms delay in MockUserRepository

    assertEquals(setOf("Football", "Coding", "Reading"), viewModel.selectedActivities.value)
    assertEquals(EditActivitiesViewModel.UiState.Idle, viewModel.uiState.value)
  }

  @Test
  fun `initial state handles user with no activities`() = runTest {
    val userWithoutActivities = testUser.copy(hobbies = emptyList())
    repository = SimpleFakeUserRepository(userWithoutActivities)
    val viewModelWithoutActivities = EditActivitiesViewModel(repository, testUser.userId)

    // Wait for initial load to complete
    Thread.sleep(600) // Wait for the 500ms delay in MockUserRepository

    assertTrue(viewModelWithoutActivities.selectedActivities.value.isEmpty())
    assertEquals(EditActivitiesViewModel.UiState.Idle, viewModelWithoutActivities.uiState.value)
  }

  @Test
  fun `updateSearchQuery filters activities correctly`() {
    // Test empty query shows all activities
    viewModel.updateSearchQuery("")
    assertTrue(viewModel.filteredActivities.value.contains("Football"))
    assertTrue(viewModel.filteredActivities.value.contains("Coding"))
    assertTrue(viewModel.filteredActivities.value.contains("Reading"))

    // Test search query filters activities
    viewModel.updateSearchQuery("foot")
    assertTrue(viewModel.filteredActivities.value.contains("Football"))
    assertFalse(viewModel.filteredActivities.value.contains("Coding"))
    assertFalse(viewModel.filteredActivities.value.contains("Reading"))
  }

  @Test
  fun `toggleActivity adds activity when not selected`() {
    val initialSelected = viewModel.selectedActivities.value.toMutableSet()
    assertFalse(initialSelected.contains("Basketball"))

    viewModel.toggleActivity("Basketball")

    val updatedSelected = viewModel.selectedActivities.value
    assertTrue(updatedSelected.contains("Basketball"))
    assertTrue(updatedSelected.containsAll(initialSelected))
  }

  @Test
  fun `toggleActivity removes activity when already selected`() {
    // First add an activity that's not in the initial set
    viewModel.toggleActivity("Basketball")
    assertTrue(viewModel.selectedActivities.value.contains("Basketball"))

    // Then remove it
    viewModel.toggleActivity("Basketball")
    assertFalse(viewModel.selectedActivities.value.contains("Basketball"))
  }

  @Test
  fun `isActivitySelected returns correct values`() = runTest {
    // Wait for initial load to complete
    Thread.sleep(600) // Wait for the 500ms delay in MockUserRepository

    // Initially selected activities
    assertTrue(viewModel.isActivitySelected("Football"))
    assertTrue(viewModel.isActivitySelected("Coding"))
    assertTrue(viewModel.isActivitySelected("Reading"))

    // Not selected activities
    assertFalse(viewModel.isActivitySelected("Basketball"))
    assertFalse(viewModel.isActivitySelected("Tennis"))

    // After adding an activity
    viewModel.toggleActivity("Basketball")
    assertTrue(viewModel.isActivitySelected("Basketball"))
  }

  @Test
  fun `resetState resets UI state to idle`() {
    viewModel.resetState()
    assertEquals(EditActivitiesViewModel.UiState.Idle, viewModel.uiState.value)
  }

  @Test
  fun `initial state handles user not found`() = runTest {
    repository = SimpleFakeUserRepository(null)
    val errorViewModel = EditActivitiesViewModel(repository, "non_existent_user")

    // Wait for initial load to complete
    Thread.sleep(600) // Wait for the 500ms delay in MockUserRepository

    assertTrue(errorViewModel.selectedActivities.value.isEmpty())
  }

  private class SimpleFakeUserRepository(private val user: User?) : UserRepository {
    override suspend fun getUserById(userId: String): User? = user

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

    override suspend fun getJoinedEvents(userId: String) = emptyList<String>()

    override suspend fun addEventToUser(eventId: String, userId: String) = Unit

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        Unit

    override suspend fun getInvitations(userId: String) =
        emptyList<com.github.se.studentconnect.ui.screen.activities.Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit
  }
}
