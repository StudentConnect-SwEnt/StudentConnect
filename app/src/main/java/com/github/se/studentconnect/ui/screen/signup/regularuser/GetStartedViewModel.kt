// From Bootcamp

package com.github.se.studentconnect.ui.screen.signup.regularuser

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthRepository
import com.github.se.studentconnect.model.authentication.AuthRepositoryFirebase
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state for authentication.
 *
 * @property isLoading Whether an authentication operation is in progress.
 * @property user The currently signed-in [FirebaseUser], or null if not signed in.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property signedOut True if a sign-out operation has completed.
 */
data class AuthUIState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val errorMsg: String? = null,
    val signedOut: Boolean = false
)

/**
 * ViewModel for the Sign-In view (GetStartedScreen).
 *
 * This ViewModel handles Google authentication via Firebase. It only handles the authentication
 * step - determining whether to show onboarding or the main app is handled by MainActivity based on
 * whether a user profile exists in Firestore.
 *
 * **Authentication Flow:**
 * 1. User clicks sign in button on GetStartedScreen
 * 2. Google Credential Manager shows account picker
 * 3. User selects account and authenticates
 * 4. Firebase Auth creates/updates the authenticated user session
 * 5. uiState.user is updated with the FirebaseUser
 * 6. MainActivity's LaunchedEffect detects the authenticated user
 * 7. MainActivity checks if user profile exists in Firestore:
 *     - If profile exists: Show main app (returning user)
 *     - If profile doesn't exist: Show SignUpOrchestrator (first-time user)
 *
 * @property repository The repository used to perform authentication operations.
 */
class GetStartedViewModel(private val repository: AuthRepository = AuthRepositoryFirebase()) :
    ViewModel() {

  private val _uiState = MutableStateFlow(AuthUIState())
  val uiState: StateFlow<AuthUIState> = _uiState

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  private fun getSignInOptions(context: Context) =
      GetSignInWithGoogleOption.Builder(
              serverClientId = context.getString(R.string.default_web_client_id))
          .build()

  private fun signInRequest(signInOptions: GetSignInWithGoogleOption) =
      GetCredentialRequest.Builder().addCredentialOption(signInOptions).build()

  private suspend fun getCredential(
      context: Context,
      request: GetCredentialRequest,
      credentialManager: CredentialManager
  ) = credentialManager.getCredential(context, request).credential

  /** Initiates the Google sign-in flow and updates the UI state on success or failure. */
  fun signIn(context: Context, credentialManager: CredentialManager) {
    if (_uiState.value.isLoading) return

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMsg = null) }

      val signInOptions = getSignInOptions(context)
      val signInRequest = signInRequest(signInOptions)

      try {
        // Launch Credential Manager UI safely
        val credential = getCredential(context, signInRequest, credentialManager)

        // Pass the credential to your repository
        repository.signInWithGoogle(credential).fold({ user ->
          _uiState.update {
            it.copy(isLoading = false, user = user, errorMsg = null, signedOut = false)
          }
        }) { failure ->
          _uiState.update {
            it.copy(
                isLoading = false,
                errorMsg = failure.localizedMessage,
                signedOut = true,
                user = null)
          }
        }
      } catch (e: GetCredentialCancellationException) {
        // User cancelled the sign-in flow
        Log.e("GetStartedViewModel", "Sign-in cancelled by user", e)
        _uiState.update {
          it.copy(isLoading = false, errorMsg = "Sign-in cancelled", signedOut = true, user = null)
        }
      } catch (e: GetCredentialException) {
        // Other credential errors - usually SHA-1 fingerprint not registered
        Log.e("GetStartedViewModel", "Credential error: ${e.javaClass.simpleName}", e)
        _uiState.update {
          it.copy(
              isLoading = false,
              errorMsg =
                  "Authentication failed. Check that your app's SHA-1 fingerprint is registered in Firebase Console.\n\nError: ${e.javaClass.simpleName}",
              signedOut = true,
              user = null)
        }
      } catch (e: Exception) {
        // Unexpected errors
        Log.e("GetStartedViewModel", "Unexpected error during sign-in", e)
        _uiState.update {
          it.copy(
              isLoading = false,
              errorMsg = "Unexpected error: ${e.localizedMessage ?: e.javaClass.simpleName}",
              signedOut = true,
              user = null)
        }
      }
    }
  }
}
