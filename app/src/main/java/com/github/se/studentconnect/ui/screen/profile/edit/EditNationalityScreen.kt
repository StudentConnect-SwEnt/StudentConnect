package com.github.se.studentconnect.ui.screen.profile.edit

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.profile.edit.EditNationalityViewModel
import java.util.Locale

private val RowHorizontalPadding = 16.dp
private val RowVerticalPadding = 12.dp
private val CountryRowHorizontalPadding = 12.dp
private val CountryFlagSize = 40.dp
private val SelectedBackgroundAlpha = 0.12f
private val SelectedBorderAlpha = 0.4f
private val FlagCircleAlpha = 0.15f

// Common spacing values
private val SmallSpacing = 4.dp
private val MediumSpacing = 16.dp
private val LargeSpacing = 24.dp

// Common text styles
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

// Common spacers
@Composable private fun SmallSpacer() = Spacer(Modifier.height(SmallSpacing))

@Composable private fun MediumSpacer() = Spacer(Modifier.height(MediumSpacing))

@Composable private fun LargeSpacer() = Spacer(Modifier.height(LargeSpacing))

@Composable
private fun CountryListSurface(
    modifier: Modifier = Modifier,
    filteredCountries: List<Country>,
    selectedCode: String?,
    onCountrySelect: (Country) -> Unit
) {
  Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
      border =
          BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
      modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              items(filteredCountries) { country ->
                CountryRow(
                    country = country,
                    isSelected = selectedCode == country.code,
                    onSelect = { onCountrySelect(country) })
              }
            }
      }
}

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

/**
 * Screen for editing user nationality. Uses the same logic as the signup nationality screen.
 *
 * @param userId The ID of the user whose nationality is being edited
 * @param userRepository Repository for user data operations
 * @param viewModel ViewModel for edit nationality screen
 * @param onNavigateBack Callback to navigate back to profile screen
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNationalityScreen(
    userId: String,
    userRepository: UserRepository,
    viewModel: EditNationalityViewModel = viewModel {
      EditNationalityViewModel(userRepository, userId)
    },
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
  val user by viewModel.user.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val successMessage by viewModel.successMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  // Show success and error messages
  LaunchedEffect(successMessage) {
    successMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearSuccessMessage()
    }
  }

  LaunchedEffect(errorMessage) {
    errorMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearErrorMessage()
    }
  }

  var query by rememberSaveable { mutableStateOf("") }
  val countries = remember { loadCountries() }
  val filteredCountries = remember(query, countries) { filterCountries(query, countries) }

  var selectedCountry by remember { mutableStateOf<Country?>(null) }

  LaunchedEffect(user?.country) {
    // Find the country by name from the list
    selectedCountry = countries.find { it.name == user?.country }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Edit Nationality",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
              IconButton(onClick = { onNavigateBack?.invoke() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface))
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      modifier = modifier) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start) {
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
                  selectedCode = selectedCountry?.code,
                  onCountrySelect = { country -> selectedCountry = country })

              LargeSpacer()

              Button(
                  onClick = {
                    selectedCountry?.let { country -> viewModel.updateNationality(country.name) }
                  },
                  modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                  enabled = selectedCountry != null && !isLoading) {
                    if (isLoading) {
                      CircularProgressIndicator(
                          modifier = Modifier.size(16.dp),
                          color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                      Text("Save Changes")
                    }
                  }
            }
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
