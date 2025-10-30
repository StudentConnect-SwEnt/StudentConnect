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
    var onSuccessCalled = false

    viewModel.testExecuteWithErrorHandling(
        operation = { operationExecuted = true }, onSuccess = { onSuccessCalled = true })

    kotlinx.coroutines.delay(200)

    assertTrue(operationExecuted)
    assertTrue(onSuccessCalled)
  }

  @Test
  fun `executeWithErrorHandling sets loading state during operation`() = runTest {
    var loadingStateObserved = false

    viewModel.testExecuteWithErrorHandling(
        operation = {
          kotlinx.coroutines.delay(100)
          loadingStateObserved = viewModel.uiState.value is BaseEditViewModel.UiState.Loading
        })

    kotlinx.coroutines.delay(300)

    assertTrue(loadingStateObserved)
  }

  @Test
  fun `executeWithErrorHandling handles exceptions and calls onError`() = runTest {
    var onErrorCalled = false
    var errorMessage: String? = null

    viewModel.testExecuteWithErrorHandling(
        operation = { throw IllegalStateException("Test error") },
        onError = { message ->
          onErrorCalled = true
          errorMessage = message
        })

    kotlinx.coroutines.delay(200)

    assertTrue(onErrorCalled)
    assertEquals("Test error", errorMessage)
  }

  @Test
  fun `executeWithErrorHandling handles exceptions with null message`() = runTest {
    var errorMessage: String? = null

    viewModel.testExecuteWithErrorHandling(
        operation = { throw RuntimeException(null as String?) },
        onError = { message -> errorMessage = message })

    kotlinx.coroutines.delay(200)

    assertEquals("An unexpected error occurred", errorMessage)
  }

  @Test
  fun `executeWithErrorHandling uses default onError when not provided`() = runTest {
    viewModel.testExecuteWithErrorHandling(
        operation = { throw IllegalStateException("Default error handling") })

    kotlinx.coroutines.delay(200)

    val state = viewModel.uiState.value
    assertTrue(state is BaseEditViewModel.UiState.Error)
    assertEquals("Default error handling", (state as BaseEditViewModel.UiState.Error).message)
  }

  @Test
  fun `state transitions from Idle to Loading to Success`() = runTest {
    assertEquals(BaseEditViewModel.UiState.Idle, viewModel.uiState.value)

    viewModel.testExecuteWithErrorHandling(
        operation = { kotlinx.coroutines.delay(50) },
        onSuccess = { viewModel.testSetSuccess("Operation completed") })

    kotlinx.coroutines.delay(30)
    assertEquals(BaseEditViewModel.UiState.Loading, viewModel.uiState.value)

    kotlinx.coroutines.delay(100)
    val state = viewModel.uiState.value
    assertTrue(state is BaseEditViewModel.UiState.Success)
  }

  @Test
  fun `state transitions from Idle to Loading to Error`() = runTest {
    assertEquals(BaseEditViewModel.UiState.Idle, viewModel.uiState.value)

    viewModel.testExecuteWithErrorHandling(
        operation = {
          kotlinx.coroutines.delay(50)
          throw RuntimeException("Operation failed")
        })

    kotlinx.coroutines.delay(30)
    assertEquals(BaseEditViewModel.UiState.Loading, viewModel.uiState.value)

    kotlinx.coroutines.delay(100)
    val state = viewModel.uiState.value
    assertTrue(state is BaseEditViewModel.UiState.Error)
  }

  @Test
  fun `resetState can reset from success state`() {
    viewModel.testSetSuccess("Success message")
    assertTrue(viewModel.uiState.value is BaseEditViewModel.UiState.Success)

    viewModel.resetState()
    assertEquals(BaseEditViewModel.UiState.Idle, viewModel.uiState.value)
  }

  @Test
  fun `resetState can reset from error state`() {
    viewModel.testSetError("Error message")
    assertTrue(viewModel.uiState.value is BaseEditViewModel.UiState.Error)

    viewModel.resetState()
    assertEquals(BaseEditViewModel.UiState.Idle, viewModel.uiState.value)
  }

  @Test
  fun `multiple operations can be executed sequentially`() = runTest {
    var firstOperationCount = 0
    var secondOperationCount = 0

    viewModel.testExecuteWithErrorHandling(operation = { firstOperationCount++ })

    kotlinx.coroutines.delay(200)

    viewModel.testExecuteWithErrorHandling(operation = { secondOperationCount++ })

    kotlinx.coroutines.delay(200)

    assertEquals(1, firstOperationCount)
    assertEquals(1, secondOperationCount)
  }

  @Test
  fun `executeWithErrorHandling without onSuccess callback`() = runTest {
    var operationExecuted = false

    viewModel.testExecuteWithErrorHandling(operation = { operationExecuted = true })

    kotlinx.coroutines.delay(200)

    assertTrue(operationExecuted)
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
