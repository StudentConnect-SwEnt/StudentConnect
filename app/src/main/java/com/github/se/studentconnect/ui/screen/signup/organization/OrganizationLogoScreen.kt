package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.compose.runtime.Composable
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.screen.signup.regularuser.AddPictureScreen
import com.github.se.studentconnect.ui.screen.signup.regularuser.SignUpViewModel

/**
 * Small wrapper around AddPictureScreen to provide a customized title/subtitle for uploading an
 * organization logo. Reuses the UploadCard and business logic from AddPictureScreen to avoid
 * duplication.
 */
@Composable
fun OrganizationLogoScreen(
    viewModel: SignUpViewModel,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
  AddPictureScreen(
      viewModel = viewModel,
      onSkip = onSkip,
      onContinue = onContinue,
      onBack = onBack,
      titleRes = R.string.title_upload_logo,
      subtitleRes = R.string.subtitle_upload_org_logo)
}
