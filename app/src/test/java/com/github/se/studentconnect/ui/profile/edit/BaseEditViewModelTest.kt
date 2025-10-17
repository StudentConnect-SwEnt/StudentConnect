package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BaseEditViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: TestBaseEditViewModel

  @Before
  fun setUp() {
    repository = FakeUserRepository()
    viewModel = TestBaseEditViewModel(repository, "test_user_id")
  }

  @Test
  fun `initial state is idle`() {
    assertEquals(BaseEditViewModel.UiState.Idle, viewModel.uiState.value)
  }

  @Test
  fun `resetState sets state to idle`() {
    viewModel.setLoadingState()
    assertEquals(BaseEditViewModel.UiState.Loading, viewModel.uiState.value)

    viewModel.resetState()
    assertEquals(BaseEditViewModel.UiState.Idle, viewModel.uiState.value)
  }

  @Test
  fun `setLoading sets state to loading`() {
    viewModel.setLoadingState()
    assertEquals(BaseEditViewModel.UiState.Loading, viewModel.uiState.value)
  }

  @Test
  fun `setSuccess sets state to success with message`() {
    val message = "Operation completed successfully"
    viewModel.setSuccessState(message)

    val state = viewModel.uiState.value
    assertTrue(state is BaseEditViewModel.UiState.Success)
    assertEquals(message, (state as BaseEditViewModel.UiState.Success).message)
  }

  @Test
  fun `setError sets state to error with message`() {
    val message = "Operation failed"
    viewModel.setErrorState(message)

    val state = viewModel.uiState.value
    assertTrue(state is BaseEditViewModel.UiState.Error)
    assertEquals(message, (state as BaseEditViewModel.UiState.Error).message)
  }

  /** Test implementation of BaseEditViewModel for testing purposes. */
  private class TestBaseEditViewModel(userRepository: UserRepository, userId: String) :
      BaseEditViewModel(userRepository, userId) {

    fun setLoadingState() = setLoading()

    fun setSuccessState(message: String) = setSuccess(message)

    fun setErrorState(message: String) = setError(message)
  }

  /** Simple Fake UserRepository for testing */
  private class FakeUserRepository : UserRepository {
    override suspend fun getUserById(userId: String) = null

    override suspend fun saveUser(user: com.github.se.studentconnect.model.User) = Unit

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = emptyList<com.github.se.studentconnect.model.User>()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        emptyList<com.github.se.studentconnect.model.User>() to false

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

    override suspend fun deleteUser(userId: String) = Unit

    override suspend fun getUsersByUniversity(university: String) =
        emptyList<com.github.se.studentconnect.model.User>()

    override suspend fun getUsersByHobby(hobby: String) =
        emptyList<com.github.se.studentconnect.model.User>()

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
