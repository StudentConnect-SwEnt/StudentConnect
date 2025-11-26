package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.github.se.studentconnect.R
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.signup.regularuser.DescriptionLayout
import com.github.se.studentconnect.ui.screen.signup.regularuser.DescriptionLayoutCallbacks
import com.github.se.studentconnect.ui.screen.signup.regularuser.DescriptionLayoutTags
import com.github.se.studentconnect.ui.screen.signup.regularuser.DescriptionLayoutTextConfig

/**
 * Organization description screen.
 *
 * This screen wraps the reusable [DescriptionLayout] to collect the organization's description. It
 * connects to the [OrganizationSignUpViewModel] to manage state.
 */
@Composable
fun OrganizationDescriptionScreen(
    viewModel: OrganizationSignUpViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
  val state by viewModel.state

  OrganizationDescriptionContent(
      about = state.description,
      onAboutChange = { viewModel.setDescription(it) },
      modifier = modifier,
      onBackClick = { onBack() },
      onContinueClick = { onContinue() },
      onSkipClick = {})
}

/**
 * Stateless content for the organization description step.
 *
 * Delegates to the reusable [DescriptionLayout] to keep UI consistent with the Regular User flow,
 * while injecting organization-specific strings and tags.
 */
@Composable
fun OrganizationDescriptionContent(
    about: String,
    onAboutChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSkipClick: () -> Unit = {},
) {
  DescriptionLayout(
      modifier = modifier,
      tags =
          DescriptionLayoutTags(
              containerTag = C.Tag.about_screen_container,
              appBarTag = C.Tag.about_app_bar,
              backTag = C.Tag.about_back,
              skipTag = C.Tag.about_skip,
              titleTag = C.Tag.about_title,
              subtitleTag = C.Tag.about_subtitle,
              promptContainerTag = C.Tag.about_prompt_container,
              inputTag = C.Tag.about_input,
              continueTag = C.Tag.about_continue),
      textConfig =
          DescriptionLayoutTextConfig(
              titleRes = R.string.org_description_title,
              subtitleRes = R.string.about_subtitle,
              placeholderRes = R.string.org_description_placeholder,
              text = about,
              onTextChange = onAboutChange,
              showSkip = false),
      callbacks =
          DescriptionLayoutCallbacks(
              onBackClick = onBackClick,
              onSkipClick = onSkipClick,
              onContinueClick = onContinueClick))
}
