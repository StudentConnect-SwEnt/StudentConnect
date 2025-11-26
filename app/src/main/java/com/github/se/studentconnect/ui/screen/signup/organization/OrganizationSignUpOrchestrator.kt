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
import com.github.se.studentconnect.repository.OrganizationRepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun OrganizationSignUpOrchestrator(
    firebaseUserId: String,
    onSignUpComplete: (Organization) -> Unit,
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
    OrganizationToBeContinuedScreen(
        organization = createdOrganization, onLogout = onLogout, onBack = {})
    return
  }

  // Otherwise, show the signup flow steps
  // 1. Info Screen
  if (state.currentStep == OrganizationSignUpStep.Info) {
    OrganizationInfoScreen(
        viewModel = viewModel,
        onContinue = { viewModel.nextStep() },
        onBack = { onBackToSelection() })
  }

  // 2. Logo Screen
  if (state.currentStep == OrganizationSignUpStep.Logo) {
    OrganizationLogoScreen(
        viewModel = viewModel,
        onContinue = { viewModel.nextStep() },
        onSkip = {
          viewModel.setLogoUri(null)
          viewModel.nextStep()
        },
        onBack = { viewModel.prevStep() })
  }

  // 3. Description Screen
  if (state.currentStep == OrganizationSignUpStep.Description) {
    OrganizationDescriptionScreen(
        viewModel = viewModel,
        onContinue = { viewModel.nextStep() },
        onBack = { viewModel.prevStep() })
  }

  // 4. Socials Screen
  if (state.currentStep == OrganizationSignUpStep.Socials) {
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

  // 5. Profile Setup
  if (state.currentStep == OrganizationSignUpStep.ProfileSetup) {
    OrganizationProfileSetupScreen(
        viewModel = viewModel,
        onBack = { viewModel.prevStep() },
        onStartNow = { viewModel.nextStep() })
  }

  // 6. Team Screen (Final Step)
  if (state.currentStep == OrganizationSignUpStep.Team) {
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
                        orgId = newOrgId, currentUserId = firebaseUserId, uploadedLogoUrl = logoUrl)

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

private fun shouldUpload(uri: Uri): Boolean {
  val scheme = uri.scheme?.lowercase()
  return scheme != "http" && scheme != "https"
}
