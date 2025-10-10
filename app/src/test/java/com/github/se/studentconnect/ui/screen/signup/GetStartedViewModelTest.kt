package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetStartedViewModelTest {

  @get:Rule val dispatcherRule = MainDispatcherRule()

  private fun mockContext(): Context =
      mockk(relaxed = true) {
        every { getString(R.string.default_web_client_id) } returns "client-id"
      }

  private fun mockCredentialResponse(credential: Credential): GetCredentialResponse =
      mockk(relaxed = true) { every { this@mockk.credential } returns credential }

  @Test
  fun signIn_success_updatesState() = runTest {
    // Arrange
    val context = mockContext()
    val credential = mockk<Credential>()
    val response = mockCredentialResponse(credential)
    val credentialManager =
        mockk<CredentialManager> {
          coEvery { getCredential(any(), any<GetCredentialRequest>()) } returns response
        }
    val firebaseUser = mockk<FirebaseUser>()
    val repository =
        mockk<AuthRepository> {
          coEvery { signInWithGoogle(credential) } returns Result.success(firebaseUser)
        }
    val viewModel = GetStartedViewModel(repository)

    // Act
    viewModel.signIn(context, credentialManager)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(firebaseUser, state.user)
    assertNull(state.errorMsg)
    assertFalse(state.signedOut)
    coVerify(exactly = 1) { repository.signInWithGoogle(credential) }
  }

  @Test
  fun signIn_repositoryFailure_setsErrorAndSignedOut() = runTest {
    // Arrange
    val context = mockContext()
    val credential = mockk<Credential>()
    val response = mockCredentialResponse(credential)
    val credentialManager =
        mockk<CredentialManager> {
          coEvery { getCredential(any(), any<GetCredentialRequest>()) } returns response
        }
    val error = RuntimeException("Sign-in failed")
    val repository =
        mockk<AuthRepository> {
          coEvery { signInWithGoogle(credential) } returns Result.failure(error)
        }
    val viewModel = GetStartedViewModel(repository)

    // Act
    viewModel.signIn(context, credentialManager)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.user)
    assertEquals(error.localizedMessage, state.errorMsg)
    assertTrue(state.signedOut)
  }

  @Test
  fun signIn_cancellation_setsUserCancelledMessage() = runTest {
    // Arrange
    val context = mockContext()
    val credentialManager =
        mockk<CredentialManager> {
          coEvery { getCredential(any(), any<GetCredentialRequest>()) } throws
              GetCredentialCancellationException()
        }
    val repository = mockk<AuthRepository>(relaxed = true)
    val viewModel = GetStartedViewModel(repository)

    // Act
    viewModel.signIn(context, credentialManager)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Sign-in cancelled", state.errorMsg)
    assertTrue(state.signedOut)
    assertNull(state.user)
    coVerify(exactly = 0) { repository.signInWithGoogle(any()) }
  }

  @Test
  fun signIn_getCredentialException_setsFailureMessage() = runTest {
    // Arrange
    val context = mockContext()
    val exception =
        mockk<GetCredentialException> { every { localizedMessage } returns "network down" }
    val credentialManager =
        mockk<CredentialManager> {
          coEvery { getCredential(any(), any<GetCredentialRequest>()) } throws exception
        }
    val repository = mockk<AuthRepository>(relaxed = true)
    val viewModel = GetStartedViewModel(repository)

    // Act
    viewModel.signIn(context, credentialManager)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(
        "Failed to get credentials: ${exception.localizedMessage}",
        state.errorMsg,
    )
    assertTrue(state.signedOut)
    assertNull(state.user)
  }

  @Test
  fun signIn_unexpectedException_setsGenericMessage() = runTest {
    // Arrange
    val context = mockContext()
    val exception = IllegalStateException("boom")
    val credentialManager =
        mockk<CredentialManager> {
          coEvery { getCredential(any(), any<GetCredentialRequest>()) } throws exception
        }
    val repository = mockk<AuthRepository>(relaxed = true)
    val viewModel = GetStartedViewModel(repository)

    // Act
    viewModel.signIn(context, credentialManager)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Unexpected error: ${exception.localizedMessage}", state.errorMsg)
    assertTrue(state.signedOut)
    assertNull(state.user)
  }

  @Test
  fun clearErrorMsg_resetsErrorField() = runTest {
    // Arrange
    val context = mockContext()
    val credentialManager =
        mockk<CredentialManager> {
          val exception =
              mockk<GetCredentialException> { every { localizedMessage } returns "error" }
          coEvery { getCredential(any(), any<GetCredentialRequest>()) } throws exception
        }
    val repository = mockk<AuthRepository>(relaxed = true)
    val viewModel = GetStartedViewModel(repository)
    viewModel.signIn(context, credentialManager)
    advanceUntilIdle()

    // Act
    viewModel.clearErrorMsg()

    // Assert
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun signIn_whenAlreadyLoading_ignoresSubsequentCalls() = runTest {
    // Arrange
    val context = mockContext()
    val credential = mockk<Credential>()
    val response = mockCredentialResponse(credential)
    val latch = CompletableDeferred<Unit>()
    val credentialManager =
        mockk<CredentialManager> {
          coEvery { getCredential(any(), any<GetCredentialRequest>()) } coAnswers
              {
                latch.await()
                response
              }
        }
    val firebaseUser = mockk<FirebaseUser>()
    val repository =
        mockk<AuthRepository> {
          coEvery { signInWithGoogle(credential) } returns Result.success(firebaseUser)
        }
    val viewModel = GetStartedViewModel(repository)

    // Act
    viewModel.signIn(context, credentialManager)
    runCurrent() // ensure loading state is set before second call
    viewModel.signIn(context, credentialManager)
    val intermediateState = viewModel.uiState.value

    // Assert
    assertTrue(intermediateState.isLoading)
    coVerify(exactly = 1) { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }

    // Act
    latch.complete(Unit)
    advanceUntilIdle()

    // Assert
    val finalState = viewModel.uiState.value
    assertFalse(finalState.isLoading)
    assertEquals(firebaseUser, finalState.user)
  }
}
