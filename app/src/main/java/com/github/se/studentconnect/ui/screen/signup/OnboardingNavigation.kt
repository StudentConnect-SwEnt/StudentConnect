package com.github.se.studentconnect.ui.screen.signup

import androidx.compose.runtime.Composable
import com.github.se.studentconnect.model.user.UserRepository

@Composable
fun OnboardingNavigation(
    firebaseUserId: String,
    email: String,
    userRepository: UserRepository,
    onOnboardingComplete: (isLogout: Boolean) -> Unit
) {
  // Directly show the regular user sign-up flow without account type selection
  SignUpOrchestrator(
      firebaseUserId = firebaseUserId,
      email = email,
      userRepository = userRepository,
      onSignUpComplete = { _ -> onOnboardingComplete(false) },
      onBackToSelection = { onOnboardingComplete(true) })
}
