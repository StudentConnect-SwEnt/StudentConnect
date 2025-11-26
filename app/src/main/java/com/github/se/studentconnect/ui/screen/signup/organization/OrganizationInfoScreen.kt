package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.components.TopicFilterChip
import com.github.se.studentconnect.ui.screen.signup.SignUpBackButton
import com.github.se.studentconnect.ui.screen.signup.SignUpLargeSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpMediumSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpPrimaryButton
import com.github.se.studentconnect.ui.screen.signup.SignUpScreenConstants
import com.github.se.studentconnect.ui.screen.signup.SignUpSmallSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpSubtitle
import com.github.se.studentconnect.ui.screen.signup.SignUpTitle
import com.github.se.studentconnect.ui.screen.signup.regularuser.AvatarBanner

object OrganizationInfoScreenTestTags {
  const val SCREEN = "organization_info_screen"
  const val ORG_NAME_INPUT = "organization_name_input"
  const val CONTINUE_BUTTON = "organization_continue_button"
  const val BACK_BUTTON = "organization_back_button"
}

/**
 * OrganizationInfoScreen collects basic organization information during a sign-up flow.
 *
 * This screen displays an optional avatar banner, an input for the organization's name and a
 * selectable list of organization types. It reuses common sign-up components to avoid UI
 * duplication (SignUpBackButton, SignUpTitle, AvatarBanner, SignUpPrimaryButton, ...).
 *
 * @param onContinue callback invoked when the user continues
 * @param onBack callback invoked when the user navigates back
 * @param avatarResIds optional list of drawable resource ids to display in the avatar banner
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizationInfoScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    avatarResIds: List<Int> = listOf()
) {
  // Use screen dimensions to compute responsive spacing instead of hardcoded dp values
  val config = LocalConfiguration.current
  val screenHeight = config.screenHeightDp.dp
  val screenWidth = config.screenWidthDp.dp
  // helper fractions
  val largeSpacing: Dp = screenHeight * 0.03f
  val mediumSpacing: Dp = screenHeight * 0.02f
  val chipSpacing: Dp = screenWidth * 0.03f

  val orgName = remember { mutableStateOf("") }
  val selectedType = remember { mutableStateOf<String?>(null) }
  val types =
      listOf(
          stringResource(R.string.organization_type_association),
          stringResource(R.string.organization_type_student_club),
          stringResource(R.string.organization_type_company),
          stringResource(R.string.organization_type_ngo),
          stringResource(R.string.organization_type_other))

  val canContinue = orgName.value.isNotBlank() && selectedType.value != null

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(
                  horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                  vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING),
      horizontalAlignment = Alignment.Start) {
        SignUpBackButton(
            onClick = onBack,
            modifier = Modifier.Companion.size(SignUpScreenConstants.BACK_BUTTON_SIZE))

        SignUpMediumSpacer()
        SignUpTitle(text = stringResource(R.string.instruction_who_are_you))
        SignUpSmallSpacer()
        SignUpSubtitle(text = stringResource(R.string.instruction_who_are_you_subtitle))

        SignUpLargeSpacer()

        // reuse AvatarBanner from BasicInfoScreen
        if (avatarResIds.isNotEmpty()) {
          AvatarBanner(modifier = Modifier.fillMaxWidth(), avatarResIds = avatarResIds)
        }

        Spacer(modifier = Modifier.height(largeSpacing))

        OutlinedTextField(
            value = orgName.value,
            onValueChange = { orgName.value = it },
            modifier =
                Modifier.fillMaxWidth().testTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT),
            label = { Text(stringResource(R.string.organization_label_name)) },
            singleLine = true)

        Spacer(modifier = Modifier.height(mediumSpacing))

        // Use the same TopicChip used in ExperiencesScreen to keep visual consistency
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(chipSpacing, alignment = Alignment.CenterHorizontally)) {
              types.forEach { t ->
                val selected = selectedType.value == t
                TopicFilterChip(
                    label = t,
                    selected = selected,
                    onClick = { selectedType.value = if (selected) null else t },
                    icon = {
                      Icon(
                          imageVector = Icons.Outlined.Star,
                          contentDescription = null,
                          modifier = Modifier.size(20.dp))
                    })
              }
            }

        // Push the primary button to the bottom of the screen (consistent with other sign-up
        // screens)
        Spacer(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(mediumSpacing))

        SignUpPrimaryButton(
            text = stringResource(R.string.button_continue),
            onClick = onContinue,
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON),
            enabled = canContinue,
            iconRes = null)
      }
}
