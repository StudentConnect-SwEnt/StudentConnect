package com.github.se.studentconnect.ui.screen.signup

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.authentication.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GetStartedScreenTest {

  @get:Rule val dispatcherRule = MainDispatcherRule()

  private class DefaultViewModelActivity : ComponentActivity() {
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
      get() = pendingFactory ?: super.defaultViewModelProviderFactory

    companion object {
      var pendingFactory: ViewModelProvider.Factory? = null
    }
  }

  private fun setUiState(viewModel: GetStartedViewModel, state: AuthUIState) {
    val field = GetStartedViewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST") val flow = field.get(viewModel) as MutableStateFlow<AuthUIState>
    flow.value = state
  }

  @Test
  fun gettingStartedScreen_triggersOnSignedInWhenUserPresent() = runTest {
    // Arrange
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val credential = mockk<Credential>()
    val response =
        mockk<GetCredentialResponse> { every { this@mockk.credential } returns credential }
    val credentialManager =
        mockk<CredentialManager> {
          coEvery { getCredential(any(), any<GetCredentialRequest>()) } returns response
        }
    val firebaseUser = mockk<FirebaseUser>()
    val repository =
        object : AuthRepository {
          override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> =
              Result.success(firebaseUser)

          override fun signOut(): Result<Unit> = Result.success(Unit)
        }
    val viewModel = GetStartedViewModel(repository)
    viewModel.signIn(context, credentialManager)
    advanceUntilIdle()
    var signedInCount = 0
    val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

    // Act
    activity.runOnUiThread {
      activity.setContent {
        GettingStartedScreen(
            onSignedIn = { signedInCount += 1 }, onSignInError = {}, viewModel = viewModel)
      }
    }
    dispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    Shadows.shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertEquals(1, signedInCount)
    assertEquals(firebaseUser, viewModel.uiState.value.user)
  }

  @Test
  fun gettingStartedScreen_triggersOnSignInErrorWhenErrorPresent() = runTest {
    // Arrange
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val credential = mockk<Credential>()
    val response =
        mockk<GetCredentialResponse> { every { this@mockk.credential } returns credential }
    val credentialManager =
        mockk<CredentialManager> {
          coEvery { getCredential(any(), any<GetCredentialRequest>()) } returns response
        }
    val failure = IllegalArgumentException("Bad credentials")
    val repository =
        object : AuthRepository {
          override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> =
              Result.failure(failure)

          override fun signOut(): Result<Unit> = Result.success(Unit)
        }
    val viewModel = GetStartedViewModel(repository)
    viewModel.signIn(context, credentialManager)
    advanceUntilIdle()
    val receivedErrors = mutableListOf<String>()
    val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

    // Act
    activity.runOnUiThread {
      activity.setContent {
        GettingStartedScreen(
            onSignedIn = {}, onSignInError = { receivedErrors += it }, viewModel = viewModel)
      }
    }
    dispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    Shadows.shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertTrue(receivedErrors.isNotEmpty())
    assertEquals(failure.localizedMessage, receivedErrors.single())
  }

  @Test
  fun gettingStartedScreen_usesDefaultParametersFromActivityFactory() = runTest {
    // Arrange
    val firebaseUser = mockk<FirebaseUser>()
    val repository =
        object : AuthRepository {
          override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> =
              Result.failure(IllegalStateException("not expected"))

          override fun signOut(): Result<Unit> = Result.success(Unit)
        }
    val injectedViewModel = GetStartedViewModel(repository)
    setUiState(injectedViewModel, AuthUIState(user = firebaseUser))
    DefaultViewModelActivity.pendingFactory =
        object : ViewModelProvider.Factory {
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return injectedViewModel as T
          }

          override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
              create(modelClass)
        }
    var signedInCalls = 0
    val activity = Robolectric.buildActivity(DefaultViewModelActivity::class.java).setup().get()

    // Act
    activity.runOnUiThread {
      activity.setContent { GettingStartedScreen(onSignedIn = { signedInCalls += 1 }) }
    }
    dispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    Shadows.shadowOf(Looper.getMainLooper()).idle()

    // Assert
    assertEquals(1, signedInCalls)

    // Cleanup
    DefaultViewModelActivity.pendingFactory = null
  }
}
