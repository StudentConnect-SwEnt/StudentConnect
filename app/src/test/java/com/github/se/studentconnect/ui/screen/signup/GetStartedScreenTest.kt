package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.github.se.studentconnect.model.authentication.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GetStartedScreenTest {

  private val credentialManager: CredentialManager = mockk()
  private val credentialResponse: GetCredentialResponse = mockk()
  private val credential: CustomCredential = mockk()

  @Before
  fun setUp() {
    mockkObject(CredentialManager.Companion)
    every { CredentialManager.create(any<Context>()) } returns credentialManager
    every { credentialResponse.credential } returns credential
    every { credential.type } returns
        com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion
            .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    coEvery { credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>()) } returns
        credentialResponse
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `successful sign-in triggers callback with uid`() {
    // Arrange
    val firebaseUser = mockk<FirebaseUser> { every { uid } returns "uid-123" }
    val repository = RecordingAuthRepository(Result.success(firebaseUser))
    val viewModel = GetStartedViewModel(repository)
    var capturedUid: String? = null

    val controller = launchController(viewModel, { capturedUid = it }, {})
    val activity = controller.get()

    viewModel.signIn(activity, credentialManager)
    Robolectric.flushForegroundThreadScheduler()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

    assertEquals("uid-123", capturedUid)
    assertEquals(credential, repository.recordedCredential)

    controller.pause().stop().destroy()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }

  @Test
  fun `sign-in failure surfaces error callback`() {
    // Arrange
    val error = IllegalStateException("Network down")
    val repository = RecordingAuthRepository(Result.failure(error))
    val viewModel = GetStartedViewModel(repository)
    var reportedError: String? = null

    val controller = launchController(viewModel, {}, { reportedError = it })
    val activity = controller.get()

    viewModel.signIn(activity, credentialManager)
    Robolectric.flushForegroundThreadScheduler()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

    assertEquals(error.localizedMessage, reportedError)
    assertEquals(credential, repository.recordedCredential)

    controller.pause().stop().destroy()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }

  private fun launchController(
      viewModel: GetStartedViewModel,
      onSignedIn: (String) -> Unit,
      onSignInError: (String) -> Unit
  ) =
      Robolectric.buildActivity(ComponentActivity::class.java).apply {
        setup()
        get().setContent {
          GetStartedScreen(
              onSignedIn = onSignedIn,
              onSignInError = onSignInError,
              viewModel = viewModel,
              context = get(),
              credentialManager = credentialManager)
        }
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
      }

  private class RecordingAuthRepository(private val result: Result<FirebaseUser>) : AuthRepository {
    var recordedCredential: Credential? = null
      private set

    override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> {
      recordedCredential = credential
      return result
    }

    override fun signOut(): Result<Unit> = Result.success(Unit)
  }
}
