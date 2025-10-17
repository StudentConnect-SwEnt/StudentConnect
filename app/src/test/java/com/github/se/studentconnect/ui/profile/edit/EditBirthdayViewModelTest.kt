package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditBirthdayViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: EditBirthdayViewModel
  private val testUser =
      User(
          userId = "test_user",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthday = "15/03/1998",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio",
          profilePictureUrl = null)

  @Before
  fun setUp() {
    repository = FakeUserRepository(testUser)
    viewModel = EditBirthdayViewModel(repository, testUser.userId)
  }

  @Test
  fun `initial state loads user birthday correctly`() = runTest {
    assertEquals("15/03/1998", viewModel.birthdayString.value)
  }

  @Test
  fun `updateSelectedDate updates date and birthday string`() {
    val testDate = Date(1000000000000L)
    viewModel.updateSelectedDate(testDate.time)

    assertEquals(testDate.time, viewModel.selectedDateMillis.value)
    assertTrue(viewModel.birthdayString.value?.isNotEmpty() == true)
  }

  @Test
  fun `updateSelectedDate with null clears date`() {
    viewModel.updateSelectedDate(null)
    assertNull(viewModel.selectedDateMillis.value)
    assertNull(viewModel.birthdayString.value)
  }

  @Test
  fun `resetState resets UI state to idle`() {
    viewModel.resetState()
    assertEquals(EditBirthdayViewModel.UiState.Idle, viewModel.uiState.value)
  }

  /** Simple Fake UserRepository for testing */
  private class FakeUserRepository(private val user: User? = null) : UserRepository {
    override suspend fun getUserById(userId: String): User? = user

    override suspend fun saveUser(user: User) = Unit

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserByEmail(email: String): User? = null

    override suspend fun getAllUsers(): List<User> = emptyList()

    override suspend fun getUsersPaginated(
        limit: Int,
        lastUserId: String?
    ): Pair<List<User>, Boolean> = emptyList<User>() to false

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

    override suspend fun deleteUser(userId: String) = Unit

    override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

    override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

    override suspend fun getNewUid(): String = "new_uid"

    override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

    override suspend fun addEventToUser(eventId: String, userId: String) = Unit

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        Unit

    override suspend fun getInvitations(
        userId: String
    ): List<com.github.se.studentconnect.ui.screen.activities.Invitation> = emptyList()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit
  }
}
