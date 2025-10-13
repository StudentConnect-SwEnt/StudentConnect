package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.test.core.app.ActivityScenario
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

    val scenario =
        launchScreen(viewModel = viewModel, onSignedIn = { capturedUid = it }, onSignInError = {})

    // Act
    scenario.onActivity { activity -> viewModel.signIn(activity, credentialManager) }
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

    // Assert
    assertEquals("uid-123", capturedUid)
    assertEquals(credential, repository.recordedCredential)
    scenario.close()
  }

  @Test
  fun `sign-in failure surfaces error callback`() {
    // Arrange
    val error = IllegalStateException("Network down")
    val repository = RecordingAuthRepository(Result.failure(error))
    val viewModel = GetStartedViewModel(repository)
    var reportedError: String? = null

    val scenario =
        launchScreen(viewModel = viewModel, onSignedIn = {}, onSignInError = { reportedError = it })

    // Act
    scenario.onActivity { activity -> viewModel.signIn(activity, credentialManager) }
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

    // Assert
    assertEquals(error.localizedMessage, reportedError)
    assertEquals(credential, repository.recordedCredential)
    scenario.close()
  }

  private fun launchScreen(
      viewModel: GetStartedViewModel,
      onSignedIn: (String) -> Unit,
      onSignInError: (String) -> Unit
  ): ActivityScenario<ComponentActivity> {
    val scenario = ActivityScenario.launch(ComponentActivity::class.java)
    scenario.onActivity { activity ->
      activity.setContent {
        GetStartedScreen(
            onSignedIn = onSignedIn, onSignInError = onSignInError, viewModel = viewModel)
      }
    }
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    return scenario
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
