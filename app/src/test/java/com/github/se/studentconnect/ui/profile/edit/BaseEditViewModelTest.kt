package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseEditViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: TestUserRepository
  private lateinit var viewModel: TestBaseEditViewModel

  @Before
  fun setUp() {
    repository = TestUserRepository()
    viewModel = TestBaseEditViewModel(repository, "test_user_id")
  }

  @Test
  fun `initial state is idle`() {
    assertEquals(BaseEditViewModel.UiState.Idle, viewModel.uiState.value)
  }

  @Test
  fun `resetState sets state to idle`() {
    viewModel.testSetLoading()
    viewModel.resetState()
    assertEquals(BaseEditViewModel.UiState.Idle, viewModel.uiState.value)
  }

  @Test
  fun `setLoading sets state to loading`() {
    viewModel.testSetLoading()
    assertEquals(BaseEditViewModel.UiState.Loading, viewModel.uiState.value)
  }

  @Test
  fun `setSuccess sets state to success with message`() {
    val message = "Operation completed successfully"
    viewModel.testSetSuccess(message)
    val state = viewModel.uiState.value
    assertTrue(state is BaseEditViewModel.UiState.Success)
    assertEquals(message, (state as BaseEditViewModel.UiState.Success).message)
  }

  @Test
  fun `setError sets state to error with message`() {
    val message = "Operation failed"
    viewModel.testSetError(message)
    val state = viewModel.uiState.value
    assertTrue(state is BaseEditViewModel.UiState.Error)
    assertEquals(message, (state as BaseEditViewModel.UiState.Error).message)
  }

  @Test
  fun `executeWithErrorHandling executes operation successfully`() = runTest {
    var operationExecuted = false
    var successCallbackExecuted = false

    viewModel.testExecuteWithErrorHandling(
        operation = { operationExecuted = true }, onSuccess = { successCallbackExecuted = true })

    // Wait for coroutines to complete
    Thread.sleep(100)

    assertTrue(operationExecuted)
    assertTrue(successCallbackExecuted)
    assertTrue(viewModel.uiState.value is BaseEditViewModel.UiState.Success)
  }

  @Test
  fun `executeWithErrorHandling handles exceptions`() = runTest {
    val errorMessage = "Test error"
    var errorCallbackExecuted = false

    viewModel.testExecuteWithErrorHandling(
        operation = { throw RuntimeException(errorMessage) },
        onError = { errorCallbackExecuted = true })

    // Wait for coroutines to complete
    Thread.sleep(100)

    assertTrue(errorCallbackExecuted)
    assertTrue(viewModel.uiState.value is BaseEditViewModel.UiState.Error)
    assertEquals(errorMessage, (viewModel.uiState.value as BaseEditViewModel.UiState.Error).message)
  }

  @Test
  fun `executeWithErrorHandling handles exceptions with null message`() = runTest {
    viewModel.testExecuteWithErrorHandling(operation = { throw RuntimeException() })

    // Wait for coroutines to complete
    Thread.sleep(100)

    assertTrue(viewModel.uiState.value is BaseEditViewModel.UiState.Error)
    assertEquals(
        "An unexpected error occurred",
        (viewModel.uiState.value as BaseEditViewModel.UiState.Error).message)
  }

  @Test
  fun `executeWithErrorHandling sets loading state during operation`() = runTest {
    var loadingStateObserved = false

    viewModel.testExecuteWithErrorHandling(
        operation = {
          loadingStateObserved = viewModel.uiState.value is BaseEditViewModel.UiState.Loading
        })

    // Wait for coroutines to complete
    Thread.sleep(100)

    assertTrue(loadingStateObserved)
  }

  private class TestBaseEditViewModel(userRepository: UserRepository, userId: String) :
      BaseEditViewModel(userRepository, userId) {
    fun testSetLoading() = setLoading()

    fun testSetSuccess(message: String) = setSuccess(message)

    fun testSetError(message: String) = setError(message)

    fun testExecuteWithErrorHandling(
        operation: suspend () -> Unit,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = { testSetError(it) }
    ) = executeWithErrorHandling(operation, onSuccess, onError)
  }

  private class TestUserRepository : UserRepository {
    override suspend fun getUserById(userId: String): User? = null

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
