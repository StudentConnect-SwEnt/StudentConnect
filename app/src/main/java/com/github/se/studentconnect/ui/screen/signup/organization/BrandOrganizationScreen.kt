package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.eventcreation.FormTextField
import com.github.se.studentconnect.ui.screen.signup.SignUpBackButton
import com.github.se.studentconnect.ui.screen.signup.SignUpLargeSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpMediumSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpPrimaryButton
import com.github.se.studentconnect.ui.screen.signup.SignUpScreenConstants
import com.github.se.studentconnect.ui.screen.signup.SignUpSkipButton
import com.github.se.studentconnect.ui.screen.signup.SignUpSmallSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpSubtitle
import com.github.se.studentconnect.ui.screen.signup.SignUpTitle

/**
 * Composable for a social link input field with optional leading icon.
 *
 * @param label The label for the text field.
 * @param value The current value of the text field.
 * @param onValueChange Callback when the text field value changes.
 * @param modifier Modifier for the text field.
 * @param leadingIcon Optional leading icon composable.
 */
@Composable
fun SocialLinkField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
  FormTextField(
      modifier = modifier.fillMaxWidth(),
      label = label,
      value = value,
      onValueChange = onValueChange,
      leadingIcon = leadingIcon)
}

/**
 * Composable for the Brand/Organization sign-up screen content.
 *
 * @param modifier Modifier for the content layout.
 * @param website Current value of the website field.
 * @param onWebsiteChange Callback when the website field value changes.
 * @param insta Current value of the Instagram field.
 * @param onInstaChange Callback when the Instagram field value changes.
 * @param x Current value of the X (Twitter) field.
 * @param onXChange Callback when the X (Twitter) field value changes.
 * @param linkedin Current value of the LinkedIn field.
 * @param onLinkedinChange Callback when the LinkedIn field value changes.
 * @param onBack Callback when the back button is pressed.
 * @param onSkip Callback when the skip button is pressed.
 * @param onContinue Callback when the continue button is pressed.
 */
@Composable
fun BrandOrganizationContent(
    modifier: Modifier = Modifier,
    website: String,
    onWebsiteChange: (String) -> Unit,
    insta: String,
    onInstaChange: (String) -> Unit,
    x: String, // Twitter rebranded to X
    onXChange: (String) -> Unit,
    linkedin: String,
    onLinkedinChange: (String) -> Unit,
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(
                  horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                  vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.Top) {

        // top app bar row with back and skip to match other sign-up screens
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              SignUpBackButton(onClick = onBack)
              SignUpSkipButton(onClick = onSkip)
            }

        SignUpMediumSpacer()

        SignUpTitle(text = stringResource(R.string.title_brand_organization))

        SignUpSmallSpacer()

        SignUpSubtitle(text = stringResource(R.string.subtitle_brand_organization))

        SignUpLargeSpacer()

        SocialLinkField(
            label = stringResource(R.string.label_website),
            value = website,
            onValueChange = onWebsiteChange,
            leadingIcon = {
              Icon(painter = painterResource(R.drawable.ic_web_globe), contentDescription = null)
            })
        Spacer(modifier = Modifier.height(8.dp))
        SocialLinkField(
            label = stringResource(R.string.label_insta),
            value = insta,
            onValueChange = onInstaChange,
            leadingIcon = {
              Icon(
                  painter = painterResource(R.drawable.ic_logo_instagram),
                  contentDescription = null)
            })
        Spacer(modifier = Modifier.height(8.dp))
        SocialLinkField(
            label = stringResource(R.string.label_x),
            value = x,
            onValueChange = onXChange,
            leadingIcon = {
              Icon(painter = painterResource(R.drawable.ic_logo_x), contentDescription = null)
            })
        Spacer(modifier = Modifier.height(8.dp))
        SocialLinkField(
            label = stringResource(R.string.label_linkedin),
            value = linkedin,
            onValueChange = onLinkedinChange,
            leadingIcon = {
              Icon(
                  painter = painterResource(R.drawable.ic_logo_linkedin), contentDescription = null)
            })

        // push the primary button to the bottom to match other screens layout
        Spacer(modifier = Modifier.weight(1f))

        SignUpPrimaryButton(
            text = stringResource(R.string.button_continue),
            onClick = onContinue,
            modifier = Modifier.align(Alignment.CenterHorizontally))
      }
}

/**
 * Composable for the Brand/Organization sign-up screen (Social Links Step).
 *
 * @param viewModel The view model managing the organization sign-up state.
 * @param onSkip Callback when the skip button is pressed.
 * @param onContinue Callback when the continue button is pressed.
 * @param onBack Callback when the back button is pressed.
 */
@Composable
fun BrandOrganizationScreen(
    viewModel: OrganizationSignUpViewModel,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
  val state by viewModel.state

  BrandOrganizationContent(
      website = state.websiteUrl,
      onWebsiteChange = { viewModel.setWebsite(it) },
      insta = state.instagramHandle,
      onInstaChange = { viewModel.setInstagram(it) },
      x = state.xHandle,
      onXChange = { viewModel.setX(it) },
      linkedin = state.linkedinUrl,
      onLinkedinChange = { viewModel.setLinkedin(it) },
      onBack = { onBack() },
      onSkip = {
        viewModel.setWebsite("")
        viewModel.setInstagram("")
        viewModel.setX("")
        viewModel.setLinkedin("")
        onSkip()
      },
      onContinue = { onContinue() })
}
