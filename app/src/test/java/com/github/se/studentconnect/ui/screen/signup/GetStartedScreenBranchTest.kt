package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.credentials.CredentialManager
import com.github.se.studentconnect.model.authentication.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GetStartedScreenBranchTest {

  private lateinit var viewModel: GetStartedViewModel
  private lateinit var stateFlow: MutableStateFlow<AuthUIState>
  private lateinit var controller:
      org.robolectric.android.controller.ActivityController<ComponentActivity>
  private val credentialManager: CredentialManager = mockk(relaxed = true)

  @Before
  fun setUp() {
    mockkObject(CredentialManager.Companion)
    every { CredentialManager.create(any<Context>()) } returns credentialManager
    viewModel = GetStartedViewModel(NoopAuthRepository())
    val field = GetStartedViewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    stateFlow = field.get(viewModel) as MutableStateFlow<AuthUIState>
    controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
  }

  @After
  fun tearDown() {
    controller.pause().stop().destroy()
    runOnIdle()
    unmockkAll()
  }

  @Test
  fun `error state invokes onSignInError and clears message`() {
    val error = "display me"
    var reported: String? = null

    val activity = controller.get()
    activity.setContent {
      GetStartedScreen(
          onSignedIn = {},
          onSignInError = { reported = it },
          viewModel = viewModel,
          context = activity,
          credentialManager = credentialManager)
    }
    runOnIdle()

    stateFlow.value = AuthUIState(errorMsg = error)
    runOnIdle()

    assertEquals(error, reported)
    assertNull(stateFlow.value.errorMsg)
  }

  @Test
  fun `signed in state forwards uid`() {
    val fakeUser = mockk<FirebaseUser> { every { uid } returns "uid-42" }
    var received: String? = null

    val activity = controller.get()
    activity.setContent {
      GetStartedScreen(
          onSignedIn = { received = it },
          onSignInError = {},
          viewModel = viewModel,
          context = activity,
          credentialManager = credentialManager)
    }
    runOnIdle()

    stateFlow.value = AuthUIState(user = fakeUser)
    runOnIdle()

    assertEquals("uid-42", received)
  }

  private fun runOnIdle() {
    Robolectric.flushForegroundThreadScheduler()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }

  private class NoopAuthRepository : AuthRepository {
    override suspend fun signInWithGoogle(
        credential: androidx.credentials.Credential
    ): Result<FirebaseUser> = Result.failure(UnsupportedOperationException())

    override fun signOut(): Result<Unit> = Result.success(Unit)
  }
}
