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
import net.bytebuddy.matcher.ElementMatchers.returns
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

  private val credentialManager: CredentialManager = mockk(relaxed = true)
  private val credentialResponse: GetCredentialResponse = mockk()
  private val credential: CustomCredential = mockk()
  private lateinit var controller:
      org.robolectric.android.controller.ActivityController<ComponentActivity>

  @Before
  fun setUp() {
    mockkObject(CredentialManager.Companion)
    every { CredentialManager.create(any<Context>()) } returns credentialManager
    every { credentialResponse.credential } returns credential
    every { credential.type } returns
        com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion
            .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
  }

  @After
  fun tearDown() {
    controller.pause().stop().destroy()
    runOnIdle()
    unmockkAll()
  }

  @Test
  fun `successful sign-in triggers callback with uid`() {
    val firebaseUser = mockk<FirebaseUser> { every { uid } returns "uid-123" }
    val repository = RecordingAuthRepository(Result.success(firebaseUser))
    val viewModel = GetStartedViewModel(repository)
    var capturedUid: String? = null

    coEvery { credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>()) } returns
        credentialResponse

    val activity = controller.get()
    activity.setContent {
      GetStartedScreen(
          onSignedIn = { capturedUid = it },
          onSignInError = {},
          viewModel = viewModel,
          context = activity,
          credentialManager = credentialManager)
    }
    runOnIdle()

    viewModel.signIn(controller.get(), credentialManager)
    runOnIdle()

    assertEquals("uid-123", capturedUid)
    assertEquals(credential, repository.recordedCredential)
  }

  @Test
  fun `sign-in failure surfaces error callback`() {
    val error = IllegalStateException("Network down")
    val repository = RecordingAuthRepository(Result.failure(error))
    val viewModel = GetStartedViewModel(repository)
    var reportedError: String? = null

    coEvery { credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>()) } returns
        credentialResponse

    val activity = controller.get()
    activity.setContent {
      GetStartedScreen(
          onSignedIn = {},
          onSignInError = { reportedError = it },
          viewModel = viewModel,
          context = activity,
          credentialManager = credentialManager)
    }
    runOnIdle()

    viewModel.signIn(controller.get(), credentialManager)
    runOnIdle()

    assertEquals("Network down", reportedError)
    assertEquals(credential, repository.recordedCredential)
  }

  private fun runOnIdle() {
    Robolectric.flushForegroundThreadScheduler()
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
