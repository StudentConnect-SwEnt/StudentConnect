package com.github.se.studentconnect.ui.screen.signup

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.eventcreation.FormTextField

/** Small, reusable social link field that reuses FormTextField to avoid duplication. */
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

@Composable
fun BrandOrganizationContent(
    modifier: Modifier = Modifier,
    website: String,
    onWebsiteChange: (String) -> Unit,
    insta: String,
    onInstaChange: (String) -> Unit,
    x: String,
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

@Composable
fun BrandOrganizationScreen(
    viewModel: SignUpViewModel,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
  BrandOrganizationContent(
      website = "",
      onWebsiteChange = { /* TODO: hook to viewModel when fields exist */},
      insta = "",
      onInstaChange = {},
      x = "",
      onXChange = {},
      linkedin = "",
      onLinkedinChange = {},
      onBack = onBack,
      onSkip = onSkip,
      onContinue = onContinue)
}
