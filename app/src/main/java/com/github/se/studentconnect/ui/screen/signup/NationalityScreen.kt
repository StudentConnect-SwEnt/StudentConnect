package com.github.se.studentconnect.ui.screen.signup

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.theme.AppTheme
import java.util.Locale

private val RowHorizontalPadding = 16.dp
private val RowVerticalPadding = 12.dp
private val CountryRowHorizontalPadding = 12.dp
private val CountryFlagSize = 40.dp
private val SelectedBackgroundAlpha = 0.12f
private val SelectedBorderAlpha = 0.4f
private val FlagCircleAlpha = 0.15f

@Composable
private fun getCountryRowColors(isSelected: Boolean, theme: MaterialTheme): Pair<Color, Color> {
  val background =
      if (isSelected) theme.colorScheme.primary.copy(alpha = SelectedBackgroundAlpha)
      else Color.Transparent
  val border =
      if (isSelected) theme.colorScheme.primary.copy(alpha = SelectedBorderAlpha)
      else Color.Transparent
  return background to border
}

@Composable
internal fun CountryRow(country: Country, isSelected: Boolean, onSelect: () -> Unit) {
  val (background, border) = getCountryRowColors(isSelected, MaterialTheme)
  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = CountryRowHorizontalPadding)
              .clickable(onClick = onSelect),
      color = background,
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(width = if (isSelected) 1.dp else 0.dp, color = border)) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = RowHorizontalPadding, vertical = RowVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RowHorizontalPadding)) {
              Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = FlagCircleAlpha),
                  modifier = Modifier.size(CountryFlagSize)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      Text(
                          text = country.flag,
                          style = MaterialTheme.typography.headlineSmall,
                          maxLines = 1)
                    }
                  }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style =
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
              }
            }
      }
}

private fun filterCountries(query: String, countries: List<Country>): List<Country> {
  val trimmed = query.trim()
  return if (trimmed.isBlank()) countries
  else countries.filter { it.name.startsWith(trimmed, ignoreCase = true) }
}

@Composable
fun NationalityScreen(viewModel: SignUpViewModel, onContinue: () -> Unit, onBack: () -> Unit) {
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

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Where are you from ?",
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary))
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Helps us connect you with other students and events",
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search countries...") },
            singleLine = true,
            trailingIcon = { Icon(Icons.Filled.Search, contentDescription = null) })

        Spacer(Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border =
                BorderStroke(
                    width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().weight(1f)) {
              LazyColumn(
                  modifier = Modifier.fillMaxWidth(),
                  contentPadding = PaddingValues(vertical = 8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredCountries) { country ->
                      CountryRow(
                          country = country,
                          isSelected = selectedCode == country.code,
                          onSelect = {
                            selectedCode = country.code
                            viewModel.setNationality(country.code)
                          })
                    }
                  }
            }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryActionButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "Continue",
            iconRes = R.drawable.ic_arrow_forward,
            onClick = onContinue,
            enabled = !selectedCode.isNullOrBlank())
      }
}

@VisibleForTesting
internal fun loadCountries(): List<Country> {
  return Locale.getISOCountries()
      .map { iso ->
        val locale = Locale("", iso)
        val name = locale.getDisplayCountry(Locale.US)
        Country(code = iso, name = name, flag = countryCodeToEmoji(iso))
      }
      .sortedBy { it.name }
}

private fun countryCodeToEmoji(countryCode: String): String {
  val normalized = countryCode.uppercase(Locale.US)
  if (normalized.length != 2 || normalized.any { it !in 'A'..'Z' }) return "🌐"
  val first = normalized[0].code - 'A'.code + 0x1F1E6
  val second = normalized[1].code - 'A'.code + 0x1F1E6
  return String(Character.toChars(first)) + String(Character.toChars(second))
}

internal data class Country(val code: String, val name: String, val flag: String)

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NationalityScreenPreview() {
  AppTheme { NationalityScreen(viewModel = SignUpViewModel(), onContinue = {}, onBack = {}) }
}
