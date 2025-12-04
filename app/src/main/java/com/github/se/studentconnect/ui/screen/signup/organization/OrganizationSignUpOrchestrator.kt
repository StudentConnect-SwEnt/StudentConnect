package com.github.se.studentconnect.ui.screen.signup.organization

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import kotlinx.coroutines.launch

/**
 * Orchestrates the organization signup flow after authentication is complete.
 *
 * @param firebaseUserId The Firebase user ID from authentication (must not be null)
 * @param onSignUpComplete Callback when signup is complete with the created Organization
 * @param onLogout Callback to log the user out
 * @param onBackToSelection Callback to return to organization/user selection screen
 * @param viewModel ViewModel managing the signup state
 */
@Composable
fun OrganizationSignUpOrchestrator(
    firebaseUserId: String,
    onLogout: () -> Unit,
    onBackToSelection: () -> Unit,
    viewModel: OrganizationSignUpViewModel = viewModel()
) {
  val state by viewModel.state
  val coroutineScope = rememberCoroutineScope()
  var isSubmitting by remember { mutableStateOf(false) }

  // State to track if we should show the confirmation screen
  var createdOrganization by remember { mutableStateOf<Organization?>(null) }

  // IF organization is created, show the "To Be Continued" screen
  if (createdOrganization != null) {
    OrganizationToBeContinuedScreen(organization = createdOrganization, onLogout = onLogout)
    return
  }

  // Otherwise, show the signup flow steps
  when (state.currentStep) {
    OrganizationSignUpStep.Info -> {
      OrganizationInfoScreen(
          viewModel = viewModel,
          onContinue = { viewModel.nextStep() },
          onBack = { onBackToSelection() })
    }
    OrganizationSignUpStep.Logo -> {
      OrganizationLogoScreen(
          viewModel = viewModel,
          onContinue = { viewModel.nextStep() },
          onSkip = {
            viewModel.setLogoUri(null)
            viewModel.nextStep()
          },
          onBack = { viewModel.prevStep() })
    }
    OrganizationSignUpStep.Description -> {
      OrganizationDescriptionScreen(
          viewModel = viewModel,
          onContinue = { viewModel.nextStep() },
          onBack = { viewModel.prevStep() })
    }
    OrganizationSignUpStep.Socials -> {
      BrandOrganizationScreen(
          viewModel = viewModel,
          onSkip = {
            viewModel.setWebsite("")
            viewModel.setInstagram("")
            viewModel.setX("")
            viewModel.setLinkedin("")
            viewModel.nextStep()
          },
          onContinue = { viewModel.nextStep() },
          onBack = { viewModel.prevStep() })
    }
    OrganizationSignUpStep.ProfileSetup -> {
      OrganizationProfileSetupScreen(
          viewModel = viewModel,
          onBack = { viewModel.prevStep() },
          onStartNow = { viewModel.nextStep() })
    }
    OrganizationSignUpStep.Team -> {
      TeamRolesScreen(
          viewModel = viewModel,
          onBack = {},
          onContinue = {
            // This "Continue" acts as "Submit"
            if (!isSubmitting) {
              isSubmitting = true
              coroutineScope.launch {
                try {
                  val orgRepository = OrganizationRepositoryProvider.repository
                  val mediaRepository = MediaRepositoryProvider.repository

                  // Generate ID
                  val newOrgId = orgRepository.getNewOrganizationId()

                  // Upload Logo
                  val logoUrl =
                      state.logoUri?.let { uri ->
                        if (shouldUpload(uri)) {
                          mediaRepository.upload(uri, "organizations/$newOrgId/logo")
                        } else null
                      }

                  // Create Model
                  val organization =
                      viewModel.createOrganizationModel(
                          orgId = newOrgId,
                          currentUserId = firebaseUserId,
                          uploadedLogoUrl = logoUrl)

                  // Save
                  orgRepository.saveOrganization(organization)

                  // Show Confirmation Screen instead of exiting immediately
                  createdOrganization = organization
                } catch (e: Exception) {
                  Log.e("OrganizationSignUp", "Failed to create organization", e)
                  isSubmitting = false
                }
              }
            }
          })
    }
  }
}

private fun shouldUpload(uri: Uri): Boolean {
  val scheme = uri.scheme?.lowercase()
  return scheme != "http" && scheme != "https"
}
