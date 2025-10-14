package com.github.se.studentconnect.ui.screen.signup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoScreen(
    viewModel: SignUpViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onContinueEnabledChanged: ((Boolean) -> Unit)? = null
) {
  val signUpState by viewModel.state
  val dateFormatter = remember {
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
  }

  val datePickerState =
      rememberDatePickerState(
          initialDisplayMode = DisplayMode.Picker,
          initialSelectedDateMillis = signUpState.birthdateMillis)
  var showDateDialog by rememberSaveable { mutableStateOf(false) }
  var birthdayText by rememberSaveable { mutableStateOf("") }
  var isBirthdateValid by remember { mutableStateOf(signUpState.birthdateMillis != null) }

  LaunchedEffect(signUpState.birthdateMillis) {
    val storedDate = signUpState.birthdateMillis
    if (storedDate == null) {
      birthdayText = ""
      isBirthdateValid = false
    } else {
      val formatted = dateFormatter.format(Date(storedDate))
      birthdayText = formatted
      isBirthdateValid = true
      if (datePickerState.selectedDateMillis != storedDate) {
        datePickerState.selectedDateMillis = storedDate
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
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.Start) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Who are you ?",
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary))
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Let others know who you are !",
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant))

        Spacer(Modifier.height(24.dp))

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
            onClick = { showDateDialog = true },
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

        if (showDateDialog) {
          DatePickerDialog(
              onDismissRequest = { showDateDialog = false },
              confirmButton = {
                Button(
                    onClick = {
                      val millis = datePickerState.selectedDateMillis
                      if (millis != null) {
                        birthdayText = dateFormatter.format(Date(millis))
                        isBirthdateValid = true
                        viewModel.setBirthdate(millis)
                      }
                      showDateDialog = false
                    }) { Text("OK") }
              },
              dismissButton = { Button(onClick = { showDateDialog = false }) { Text("Cancel") } }) {
                DatePicker(state = datePickerState)
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryActionButton(
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
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop)
      }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes iconRes: Int? = null
) {
  Button(
      onClick = onClick,
      enabled = enabled,
      modifier = modifier.height(56.dp),
      shape = RoundedCornerShape(40.dp),
      contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
              disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)),
      elevation =
          ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
              Text(
                  text = text,
                  style =
                      MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
              if (iconRes != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary)
              }
            }
      }
}

@Preview(showBackground = true)
@Composable
private fun BasicInfoScreenPreview() {
  AppTheme {
    BasicInfoScreen(viewModel = SignUpViewModel(), onContinue = {}, onBack = {})
  }
}
