package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditNationalityViewModelSimpleTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: SimpleFakeUserRepository
  private lateinit var viewModel: EditNationalityViewModel
  private val testUser =
      User(
          userId = "test_user",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthday = "01/01/2000",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio",
          profilePictureUrl = null)

  @Before
  fun setUp() {
    repository = SimpleFakeUserRepository(testUser)
    viewModel = EditNationalityViewModel(repository, testUser.userId)
  }

  @Test
  fun `initial state loads user profile`() = runTest {
    assertEquals(testUser, viewModel.user.value)
    assertFalse(viewModel.isLoading.value)
    assertNull(viewModel.errorMessage.value)
    assertNull(viewModel.successMessage.value)
  }

  @Test
  fun `clearErrorMessage clears error message`() {
    viewModel.clearErrorMessage()
    assertNull(viewModel.errorMessage.value)
  }

  @Test
  fun `clearSuccessMessage clears success message`() {
    viewModel.clearSuccessMessage()
    assertNull(viewModel.successMessage.value)
  }

  @Test
  fun `initial state handles user not found`() = runTest {
    repository = SimpleFakeUserRepository(null)
    val errorViewModel = EditNationalityViewModel(repository, "non_existent_user")

    assertNull(errorViewModel.user.value)
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
