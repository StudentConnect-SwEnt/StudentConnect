package com.github.se.studentconnect.ui.screen.signup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.components.BirthdayFormatter
import com.github.se.studentconnect.ui.components.BirthdayPickerDialog
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Collects and persists the user's core profile details (names and birthdate) while controlling
 * navigation callbacks for the sign-up flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoScreen(
    viewModel: SignUpViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onContinueEnabledChanged: ((Boolean) -> Unit)? = null,
    datePickerState: DatePickerState? = null,
    showDateDialogState: MutableState<Boolean>? = null
) {
  val signUpState by viewModel.state
  val dateFormatter = BirthdayFormatter.dateFormatter

  val selectedMillis = signUpState.birthdate?.toDate()?.time
  val pickerState =
      datePickerState
          ?: rememberDatePickerState(
              initialDisplayMode = DisplayMode.Picker, initialSelectedDateMillis = selectedMillis)
  val dialogState = showDateDialogState ?: rememberSaveable { mutableStateOf(false) }

  var birthdayText by rememberSaveable { mutableStateOf("") }
  var isBirthdateValid by remember { mutableStateOf(signUpState.birthdate != null) }

  LaunchedEffect(signUpState.birthdate) {
    val storedDate = signUpState.birthdate
    if (storedDate == null) {
      birthdayText = ""
      isBirthdateValid = false
    } else {
      val formatted = dateFormatter.format(storedDate.toDate())
      birthdayText = formatted
      isBirthdateValid = true
      val millis = storedDate.toDate().time
      if (pickerState.selectedDateMillis != millis) {
        pickerState.selectedDateMillis = millis
      }
    }
  }

  val firstNameText = signUpState.firstName
  val lastNameText = signUpState.lastName
  val isFirstNameValid = firstNameText.isNotBlank()
  val isLastNameValid = lastNameText.isNotBlank()
  val isContinueEnabled = isFirstNameValid && isLastNameValid && isBirthdateValid
  LaunchedEffect(isContinueEnabled) { onContinueEnabledChanged?.invoke(isContinueEnabled) }

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(
                  horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                  vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING),
      horizontalAlignment = Alignment.Start) {
        SignUpBackButton(onClick = onBack)

        SignUpMediumSpacer()

        SignUpTitle(text = "Who are you ?")
        SignUpSmallSpacer()
        SignUpSubtitle(text = "Let others know who you are !")

        SignUpLargeSpacer()

        AvatarBanner(
            avatarResIds = listOf(R.drawable.avatar_12, R.drawable.avatar_13, R.drawable.avatar_23))

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = firstNameText,
            onValueChange = { text -> viewModel.setFirstName(text) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("First name") },
            placeholder = { Text("Enter your first name") },
            singleLine = true)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = lastNameText,
            onValueChange = { text -> viewModel.setLastName(text) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Last name") },
            placeholder = { Text("Enter your last name") },
            singleLine = true)

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { dialogState.value = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
              Column(modifier = Modifier.fillMaxWidth()) {
                Text("Birthday", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = birthdayText.ifEmpty { "Select your birthdate" },
                    style = MaterialTheme.typography.bodyLarge,
                    color =
                        if (birthdayText.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

        BirthdayPickerDialog(
            showDialog = dialogState.value,
            datePickerState = pickerState,
            onDismiss = { dialogState.value = false },
            onConfirm = { millis ->
              birthdayText = BirthdayFormatter.formatDate(millis)
              isBirthdateValid = true
              viewModel.setBirthdate(Timestamp(Date(millis)))
            })

        Spacer(modifier = Modifier.weight(1f))

        SignUpPrimaryButton(
            text = "Continue",
            iconRes = R.drawable.ic_arrow_forward,
            onClick = onContinue,
            enabled = isContinueEnabled,
            modifier = Modifier.align(Alignment.CenterHorizontally))
      }
}

@Composable
private fun AvatarBanner(modifier: Modifier = Modifier, avatarResIds: List<Int>) {
  val primary = MaterialTheme.colorScheme.primary
  val borderColor =
      remember(primary) {
        Color(ColorUtils.blendARGB(primary.toArgb(), Color.White.toArgb(), 0.55f))
      }
  val backgroundColor =
      remember(primary) {
        Color(ColorUtils.blendARGB(primary.toArgb(), Color.White.toArgb(), 0.15f))
      }

  Surface(
      shape = RoundedCornerShape(28.dp),
      border = BorderStroke(width = 6.dp, color = borderColor),
      modifier = modifier.fillMaxWidth(),
      color = backgroundColor) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically) {
              avatarResIds.forEach { resId -> AvatarItem(avatarResId = resId) }
            }
      }
}

@Composable
private fun AvatarItem(@DrawableRes avatarResId: Int) {
  Surface(
      modifier = Modifier.size(82.dp),
      shape = CircleShape,
      color = MaterialTheme.colorScheme.onPrimary,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp) {
        Image(
            painter = painterResource(id = avatarResId),
            contentDescription = "Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop)
      }
}
