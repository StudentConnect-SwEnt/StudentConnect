package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.se.studentconnect.R
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.signup.regularuser.DescriptionLayout
import com.github.se.studentconnect.ui.screen.signup.regularuser.DescriptionLayoutCallbacks
import com.github.se.studentconnect.ui.screen.signup.regularuser.DescriptionLayoutTags
import com.github.se.studentconnect.ui.screen.signup.regularuser.DescriptionLayoutTextConfig

/** Organization description screen reusing DescriptionLayout to avoid duplication. */
@Composable
fun OrganizationDescriptionScreen(
    about: String,
    onAboutChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
  OrganizationDescriptionContent(
      about = about,
      onAboutChange = onAboutChange,
      onBackClick = onBackClick,
      onSkipClick = onSkipClick,
      onContinueClick = onContinueClick,
      modifier = modifier)
}

/**
 * Content for the organization description step. Delegates to the reusable DescriptionLayout and
 * provides the proper string resources and test tags.
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
  // Delegate to the shared DescriptionLayout to keep UI consistent and avoid duplication.
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
