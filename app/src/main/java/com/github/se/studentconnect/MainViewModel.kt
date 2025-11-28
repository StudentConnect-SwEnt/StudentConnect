package com.github.se.studentconnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** App state machine with clear states for managing authentication and onboarding flow. */
enum class AppState {
  LOADING, // Checking auth status on app start
  AUTHENTICATION, // Show GetStartedScreen (not authenticated)
  ONBOARDING, // Show SignUpOrchestrator (authenticated but no profile)
  MAIN_APP // Show main app with navigation (authenticated with profile)
}

/**
 * Data class representing the current authentication and user state of the app.
 *
 * @property appState The current state of the app in the state machine
 * @property currentUserId The Firebase user ID of the authenticated user, or null if not
 *   authenticated
 * @property currentUserEmail The email of the authenticated user, or null if not authenticated
 */
data class MainUIState(
    val appState: AppState = AppState.LOADING,
    val currentUserId: String? = null,
    val currentUserEmail: String? = null
)

/**
 * ViewModel for MainActivity that manages the app-wide authentication and onboarding state.
 *
 * This ViewModel encapsulates the logic for determining which screen to show based on the user's
 * authentication status and profile existence, following MVVM principles by keeping business logic
 * out of the Activity.
 *
 * **State Machine Flow:**
 *
 * ```
 * LOADING (Initial)
 *   ├─> AUTHENTICATION (if no Firebase user)
 *   │     └─> ONBOARDING (after sign-in, if no profile exists)
 *   │           └─> MAIN_APP (after profile creation)
 *   ├─> ONBOARDING (if Firebase user exists but no profile)
 *   │     └─> MAIN_APP (after profile creation)
 *   └─> MAIN_APP (if Firebase user and profile both exist)
 * ```
 *
 * **First-Time User Flow:**
 * 1. App starts → LOADING state
 * 2. No Firebase user found → AUTHENTICATION state (GetStartedScreen)
 * 3. User signs in with Google → currentUserId updated via onUserSignedIn()
 * 4. ViewModel checks profile → no profile exists → ONBOARDING state
 * 5. User completes onboarding → profile saved to Firestore
 * 6. onSignUpComplete callback → MAIN_APP state via onUserProfileCreated()
 *
 * **Returning User Flow:**
 * 1. App starts → LOADING state
 * 2. Firebase user found → checks Firestore for profile via UserRepository
 * 3. Profile exists → directly to MAIN_APP state
 *
 * @property userRepository Repository for checking user profile existence
 */
class MainViewModel(private val userRepository: UserRepository) : ViewModel() {

  private val _uiState = MutableStateFlow(MainUIState())
  val uiState: StateFlow<MainUIState> = _uiState

  /**
   * Performs initial authentication check on app start.
   *
   * Checks Firebase Auth state and user profile existence to determine which screen to show.
   */
  fun checkInitialAuthState() {
    viewModelScope.launch {
      if (!AuthenticationProvider.local) {
        // Production mode: Check Firebase Auth state
        val firebaseUser = Firebase.auth.currentUser

        if (firebaseUser == null) {
          _uiState.update {
            it.copy(
                appState = AppState.AUTHENTICATION, currentUserId = null, currentUserEmail = null)
          }
        } else {
          // Firebase user exists - check if profile exists in Firestore
          android.util.Log.d("MainViewModel", "Found authenticated user: ${firebaseUser.uid}")
          checkUserProfile(firebaseUser.uid, firebaseUser.email ?: "")
        }
      } else {
        // Local testing mode
        android.util.Log.d("MainViewModel", "Local testing mode")
        val userId = AuthenticationProvider.currentUser
        val email = "test@epfl.ch"
        checkUserProfile(userId, email)
      }
    }
  }

  /**
   * Checks if a user profile exists in Firestore and updates state accordingly.
   *
   * @param userId The Firebase user ID to check
   * @param email The user's email address
   */
  private suspend fun checkUserProfile(userId: String?, email: String?) {
    if (userId.isNullOrEmpty()) return
    val existingUser = userRepository.getUserById(userId)
    if (existingUser != null) {
      android.util.Log.d("MainViewModel", "User profile found - showing main app")
      _uiState.update {
        it.copy(appState = AppState.MAIN_APP, currentUserId = userId, currentUserEmail = email)
      }
    } else {
      android.util.Log.d("MainViewModel", "No user profile - showing onboarding")
      _uiState.update {
        it.copy(appState = AppState.ONBOARDING, currentUserId = userId, currentUserEmail = email)
      }
    }
  }

  /**
   * Called when a user successfully signs in via GetStartedScreen.
   *
   * Triggers a profile check to determine if onboarding is needed.
   *
   * @param userId The Firebase user ID of the newly authenticated user
   * @param email The email of the newly authenticated user
   */
  fun onUserSignedIn(userId: String, email: String) {
    android.util.Log.d("MainViewModel", "User signed in: $userId")
    viewModelScope.launch {
      _uiState.update { it.copy(currentUserId = userId, currentUserEmail = email) }
      // Check if profile exists to determine next state
      checkUserProfile(userId, email)
    }
  }

  /**
   * Called when a user completes the onboarding flow and their profile is created.
   *
   * Transitions the app to the main app state.
   */
  fun onUserProfileCreated() {
    android.util.Log.d("MainViewModel", "User profile created - showing main app")
    _uiState.update { it.copy(appState = AppState.MAIN_APP) }
  }

  /** Called when the user logs out. */
  fun onLogoutComplete() {
    try {
      if (!AuthenticationProvider.local) {
        Firebase.auth.signOut()
      }
    } catch (e: Exception) {
      android.util.Log.e("MainViewModel", "Error signing out", e)
    }
    _uiState.update {
      it.copy(appState = AppState.AUTHENTICATION, currentUserId = null, currentUserEmail = null)
    }
  }
}

/**
 * Factory for creating MainViewModel instances with the required UserRepository dependency.
 *
 * @property userRepository The UserRepository to inject into the MainViewModel
 */
class MainViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
      return MainViewModel(userRepository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
