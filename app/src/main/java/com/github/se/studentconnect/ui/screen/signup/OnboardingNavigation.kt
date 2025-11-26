package com.github.se.studentconnect.ui.screen.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpOrchestrator
import com.github.se.studentconnect.ui.screen.signup.regularuser.SignUpViewModel

@Composable
fun OnboardingNavigation(
    firebaseUserId: String,
    email: String,
    userRepository: UserRepository,
    onOnboardingComplete: (isLogout: Boolean) -> Unit
) {
  val signUpViewModel: SignUpViewModel = viewModel()
  val signUpState by signUpViewModel.state

  if (signUpState.accountTypeSelection == null) {
    AccountTypeSelectionScreen(
        onContinue = { accountType -> signUpViewModel.setAccountTypeSelection(accountType) },
        onBack = { onOnboardingComplete(true) })
  } else {
    when (signUpState.accountTypeSelection) {
      AccountTypeOption.RegularUser -> {
        SignUpOrchestrator(
            firebaseUserId = firebaseUserId,
            email = email,
            userRepository = userRepository,
            onSignUpComplete = { _ -> onOnboardingComplete(false) },
            signUpViewModel = signUpViewModel,
            onBackToSelection = { signUpViewModel.setAccountTypeSelection(null) })
      }
      AccountTypeOption.Organization -> {
        OrganizationSignUpOrchestrator(
            firebaseUserId = firebaseUserId,
            onSignUpComplete = { onOnboardingComplete(false) },
            onLogout = { onOnboardingComplete(true) },
            onBackToSelection = { signUpViewModel.setAccountTypeSelection(null) })
      }
      null -> {}
    }
  }
}
