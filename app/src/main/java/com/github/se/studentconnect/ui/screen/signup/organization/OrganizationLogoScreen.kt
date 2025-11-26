package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.screen.signup.regularuser.AddPictureScreen

/**
 * Small wrapper around AddPictureScreen to provide a customized title/subtitle for uploading an
 * organization logo. Reuses the UploadCard and business logic from AddPictureScreen to avoid
 * duplication.
 */
@Composable
fun OrganizationLogoScreen(
    viewModel: OrganizationSignUpViewModel,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
  val state by viewModel.state

  AddPictureScreen(
      selectedUri = state.logoUri,
      onUriSelected = { uri -> viewModel.setLogoUri(uri) },
      onSkip = {
        viewModel.setLogoUri(null) // Or set a default logo if you have one
        onSkip()
      },
      onContinue = { onContinue() },
      onBack = { onBack() },
      titleRes = R.string.title_upload_logo,
      subtitleRes = R.string.subtitle_upload_org_logo)
}
