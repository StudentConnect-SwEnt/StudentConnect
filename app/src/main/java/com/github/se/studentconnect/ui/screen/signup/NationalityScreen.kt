package com.github.se.studentconnect.ui.screen.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.components.CountryListSurface
import com.github.se.studentconnect.ui.components.filterCountries
import com.github.se.studentconnect.ui.components.loadCountries
import com.github.se.studentconnect.ui.theme.AppTheme

// Spacing values
private val SmallSpacing = 4.dp
private val MediumSpacing = 16.dp
private val LargeSpacing = 24.dp

// Text styles
@Composable
private fun TitleText(text: String) {
  Text(
      text = text,
      style =
          MaterialTheme.typography.headlineMedium.copy(
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary))
}

@Composable
private fun SubtitleText(text: String) {
  Text(
      text = text,
      style =
          MaterialTheme.typography.bodyMedium.copy(
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Normal,
              color = MaterialTheme.colorScheme.onSurfaceVariant),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis)
}

// Spacers
@Composable private fun SmallSpacer() = Spacer(Modifier.height(SmallSpacing))

@Composable private fun MediumSpacer() = Spacer(Modifier.height(MediumSpacing))

@Composable private fun LargeSpacer() = Spacer(Modifier.height(LargeSpacing))

@Composable
fun NationalityScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    viewModel: SignUpViewModel = viewModel()
) {
  val signUpState by viewModel.state
  var query by rememberSaveable { mutableStateOf("") }
  val countries = remember { loadCountries() }
  val filteredCountries = remember(query, countries) { filterCountries(query, countries) }

  var selectedCode by remember { mutableStateOf(signUpState.nationality) }

  LaunchedEffect(signUpState.nationality) { selectedCode = signUpState.nationality }

  Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.Start) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        MediumSpacer()

        TitleText("Where are you from ?")
        SmallSpacer()
        SubtitleText("Helps us connect you with other students and events")

        LargeSpacer()

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search countries...") },
            singleLine = true,
            trailingIcon = { Icon(Icons.Filled.Search, contentDescription = null) })

        MediumSpacer()

        CountryListSurface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            filteredCountries = filteredCountries,
            selectedCode = selectedCode,
            onCountrySelect = { country ->
              selectedCode = country.code
              viewModel.setNationality(country.code)
            })

        LargeSpacer()

        PrimaryActionButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "Continue",
            iconRes = R.drawable.ic_arrow_forward,
            onClick = onContinue,
            enabled = !selectedCode.isNullOrBlank())
      }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NationalityScreenPreview() {
  AppTheme { NationalityScreen(onContinue = {}, onBack = {}) }
}
