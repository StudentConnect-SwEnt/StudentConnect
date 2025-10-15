package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import com.github.se.studentconnect.model.authentication.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GetStartedViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private val mainDispatcher = UnconfinedTestDispatcher(testDispatcher.scheduler)

  private lateinit var repository: AuthRepository
  private lateinit var credentialManager: CredentialManager
  private lateinit var credentialResponse: GetCredentialResponse
  private lateinit var viewModel: GetStartedViewModel
  private lateinit var context: Context

  @Before
  fun setUp() {
    Dispatchers.setMain(mainDispatcher)
    repository = mockk()
    credentialManager = mockk()
    credentialResponse = mockk()
    context = mockk(relaxed = true)
    every { context.getString(any()) } returns "client-id"
    viewModel = GetStartedViewModel(repository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `successful sign-in updates state with user`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val credential = mockk<CustomCredential>()
        val firebaseUser = mockk<FirebaseUser>()
        every { credential.type } returns
            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion
                .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        every { credentialResponse.credential } returns credential
        coEvery {
          credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } returns credentialResponse
        coEvery { repository.signInWithGoogle(credential) } returns Result.success(firebaseUser)

        // Act
        viewModel.signIn(context, credentialManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertSame(firebaseUser, state.user)
        assertNull(state.errorMsg)
        assertFalse(state.signedOut)
      }

  @Test
  fun `repository failure propagates error state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val credential = mockk<CustomCredential>()
        every { credential.type } returns
            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion
                .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        every { credentialResponse.credential } returns credential
        coEvery {
          credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } returns credentialResponse
        val failure = IllegalStateException("Network down")
        coEvery { repository.signInWithGoogle(credential) } returns Result.failure(failure)

        // Act
        viewModel.signIn(context, credentialManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.user)
        assertEquals(failure.localizedMessage, state.errorMsg)
        assertTrue(state.signedOut)
      }

  @Test
  fun `non Google credential triggers failure`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val credential = mockk<Credential>()
        every { credentialResponse.credential } returns credential
        coEvery {
          credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } returns credentialResponse
        val failure = IllegalStateException("Login failed: not a Google ID credential")
        coEvery { repository.signInWithGoogle(credential) } returns Result.failure(failure)

        // Act
        viewModel.signIn(context, credentialManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertNull(state.user)
        assertEquals(failure.localizedMessage, state.errorMsg)
        assertTrue(state.signedOut)
        coVerify(exactly = 1) { repository.signInWithGoogle(credential) }
      }

  @Test
  fun `cancellation exception reports cancellation`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        coEvery {
          credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } throws GetCredentialCancellationException("User cancelled")

        // Act
        viewModel.signIn(context, credentialManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals("Sign-in cancelled", state.errorMsg)
        assertTrue(state.signedOut)
        assertNull(state.user)
      }

  @Test
  fun `unexpected exception surfaces message`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        coEvery {
          credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } throws RuntimeException("Boom")

        // Act
        viewModel.signIn(context, credentialManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.errorMsg!!.contains("Unexpected error"))
        assertTrue(state.signedOut)
        assertNull(state.user)
      }

  @Test
  fun `credential exception yields descriptive error`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        coEvery {
          credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } throws GetCredentialInterruptedException("Interrupted")

        // Act
        viewModel.signIn(context, credentialManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.errorMsg!!.startsWith("Failed to get credentials"))
        assertTrue(state.signedOut)
        assertNull(state.user)
      }

  @Test
  fun `signIn ignores additional requests while loading`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val credential = mockk<CustomCredential>()
        every { credential.type } returns
            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion
                .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        every { credentialResponse.credential } returns credential

        val field = GetStartedViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<AuthUIState>
        flow.value = AuthUIState(isLoading = true)

        // Act
        viewModel.signIn(context, credentialManager)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) {
          credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        }
      }

  @Test
  fun `clearErrorMsg removes error`() =
      runTest(testDispatcher.scheduler) {
        // Arrange: force an error state
        val credential = mockk<CustomCredential>()
        every { credential.type } returns
            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion
                .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        every { credentialResponse.credential } returns credential
        coEvery {
          credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } returns credentialResponse
        coEvery { repository.signInWithGoogle(credential) } returns
            Result.failure(IllegalStateException("failure"))

        viewModel.signIn(context, credentialManager)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMsg)

        // Act
        viewModel.clearErrorMsg()

        // Assert
        assertNull(viewModel.uiState.value.errorMsg)
      }
}
