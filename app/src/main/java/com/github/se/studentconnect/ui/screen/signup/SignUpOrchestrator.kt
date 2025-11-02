package com.github.se.studentconnect.ui.screen.signup

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Orchestrates the user onboarding flow after authentication is complete.
 *
 * This component handles the profile creation steps for authenticated users who don't yet have a
 * profile in Firestore. It coordinates between different onboarding screens and manages the overall
 * state and navigation.
 *
 * **Flow for First-Time Users:**
 * 1. User authenticates via GetStartedScreen (handled by MainActivity)
 * 2. MainActivity detects no user profile exists in Firestore
 * 3. SignUpOrchestrator is shown: BasicInfo -> Nationality -> AddPicture -> Description ->
 *    Experiences
 * 4. User profile is created and saved to Firestore in the Experiences step
 * 5. onSignUpComplete callback is triggered, returning user to MainActivity
 * 6. MainActivity shows main app with bottom navigation
 *
 * **Flow for Returning Users:**
 * 1. User authenticates via GetStartedScreen
 * 2. MainActivity detects user profile exists in Firestore
 * 3. SignUpOrchestrator is skipped entirely
 * 4. MainActivity shows main app directly
 *
 * Note: Back navigation is disabled from BasicInfo to prevent returning to authentication once the
 * onboarding flow has started.
 *
 * @param firebaseUserId The Firebase user ID from authentication (must not be null)
 * @param email The user's email from Firebase Auth (must not be null)
 * @param userRepository Repository for saving user data
 * @param onSignUpComplete Callback when onboarding is complete with the created User
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpOrchestrator(
    firebaseUserId: String,
    email: String,
    userRepository: UserRepository,
    onSignUpComplete: (User) -> Unit,
    signUpViewModel: SignUpViewModel = viewModel()
) {
  val signUpState by signUpViewModel.state
  var selectedExperienceFilter by remember { mutableStateOf("Sports") }
  var selectedExperienceTopics by remember { mutableStateOf<Set<String>>(emptySet()) }
  var isSavingUser by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val coroutineScope = rememberCoroutineScope()

  // Initialize user ID and start at BasicInfo (skip auth screen)
  LaunchedEffect(firebaseUserId) {
    signUpViewModel.setUserId(firebaseUserId)

    // Always start at BasicInfo since authentication is already complete
    if (signUpState.currentStep == SignUpStep.GettingStarted) {
      signUpViewModel.goTo(SignUpStep.BasicInfo)
    }
  }

  when (signUpState.currentStep) {
    SignUpStep.GettingStarted -> {
      // This should never be reached since we start at BasicInfo
      // LaunchedEffect above ensures we skip to BasicInfo immediately
      LaunchedEffect(Unit) { signUpViewModel.goTo(SignUpStep.BasicInfo) }
    }
    SignUpStep.BasicInfo -> {
      BasicInfoScreen(
          viewModel = signUpViewModel,
          userRepository = userRepository,
          onContinue = { signUpViewModel.nextStep() },
          // No back navigation from BasicInfo since user is already authenticated
          // and committed to onboarding flow
          onBack = { /* No-op: can't go back to auth after authentication */})
    }
    SignUpStep.Nationality -> {
      NationalityScreen(
          viewModel = signUpViewModel,
          onContinue = { signUpViewModel.nextStep() },
          onBack = { signUpViewModel.prevStep() })
    }
    SignUpStep.AddPicture -> {
      AddPictureScreen(
          viewModel = signUpViewModel,
          onSkip = { signUpViewModel.nextStep() },
          onContinue = { signUpViewModel.nextStep() },
          onBack = { signUpViewModel.prevStep() })
    }
    SignUpStep.Description -> {
      DescriptionScreen(
          description = signUpState.bio ?: "",
          onDescriptionChange = { signUpViewModel.setBio(it) },
          onBackClick = { signUpViewModel.prevStep() },
          onSkipClick = { signUpViewModel.nextStep() },
          onContinueClick = { signUpViewModel.nextStep() })
    }
    SignUpStep.Experiences -> {
      ExperiencesScreen(
          selectedFilter = selectedExperienceFilter,
          selectedTopics = selectedExperienceTopics,
          onFilterSelected = { selectedExperienceFilter = it },
          onTopicToggle = { topic ->
            selectedExperienceTopics =
                if (topic in selectedExperienceTopics) {
                  selectedExperienceTopics - topic
                } else {
                  selectedExperienceTopics + topic
                }
          },
          onBackClick = { signUpViewModel.prevStep() },
          onStartClick = {
            // Save user to Firestore - this creates the user profile
            isSavingUser = true
            errorMessage = null

            coroutineScope.launch {
              try {
                android.util.Log.d(
                    "SignUpOrchestrator", "Creating user profile for: $firebaseUserId")

                val user =
                    User(
                        userId = firebaseUserId,
                        email = email,
                        username = signUpState.username,
                        firstName = signUpState.firstName,
                        lastName = signUpState.lastName,
                        birthdate = signUpState.birthdate,
                        university = "EPFL", // TODO: Add university selection
                        hobbies = selectedExperienceTopics.toList(),
                        profilePictureUrl = signUpState.profilePictureUri,
                        bio = signUpState.bio,
                        country = signUpState.nationality)

                // Save user to repository (Firestore in production, local in test mode)
                userRepository.saveUser(user)
                android.util.Log.d(
                    "SignUpOrchestrator", "User profile saved successfully: ${user.userId}")

                // Notify MainActivity that onboarding is complete
                onSignUpComplete(user)
              } catch (e: Exception) {
                android.util.Log.e("SignUpOrchestrator", "Failed to save user profile", e)
                errorMessage = "Failed to save profile: ${e.message}"
              } finally {
                isSavingUser = false
              }
            }
          },
          isSaving = isSavingUser,
          errorMessage = errorMessage)
    }
  }
}
