package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.credentials.CredentialManager
import androidx.test.core.app.ActivityScenario
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GetStartedScreenBranchTest {

  private lateinit var viewModel: GetStartedViewModel
  private lateinit var stateFlow: MutableStateFlow<AuthUIState>

  @Before
  fun setUp() {
    mockkObject(CredentialManager.Companion)
    every { CredentialManager.create(any<Context>()) } returns mockk(relaxed = true)
    viewModel = GetStartedViewModel(NoopAuthRepository())
    val field = GetStartedViewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    stateFlow = field.get(viewModel) as MutableStateFlow<AuthUIState>
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `error state invokes onSignInError and clears message`() {
    val error = "display me"
    stateFlow.value = AuthUIState(errorMsg = error)
    var reported: String? = null

    val scenario = ActivityScenario.launch(ComponentActivity::class.java)
    scenario.onActivity { activity ->
      activity.setContent {
        GetStartedScreen(
            onSignedIn = {},
            onSignInError = { reported = it },
            viewModel = viewModel)
      }
    }

    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

    assertEquals(error, reported)
    assertNull(stateFlow.value.errorMsg)
    scenario.close()
  }

  @Test
  fun `signed in state forwards uid`() {
    val fakeUser = mockk<FirebaseUser> { every { uid } returns "uid-42" }
    stateFlow.value = AuthUIState(user = fakeUser)
    var received: String? = null

    val scenario = ActivityScenario.launch(ComponentActivity::class.java)
    scenario.onActivity { activity ->
      activity.setContent {
        GetStartedScreen(
            onSignedIn = { received = it }, onSignInError = {}, viewModel = viewModel)
      }
    }

    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

    assertEquals("uid-42", received)
    scenario.close()
  }

  private class NoopAuthRepository : AuthRepository {
    override suspend fun signInWithGoogle(
        credential: androidx.credentials.Credential
    ): Result<FirebaseUser> = Result.failure(UnsupportedOperationException())

    override fun signOut(): Result<Unit> = Result.success(Unit)
  }
}
