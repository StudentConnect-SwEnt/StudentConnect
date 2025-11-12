package com.github.se.studentconnect.ui.screen.profile.edit

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.components.Country
import com.github.se.studentconnect.ui.components.CountryListSurface
import com.github.se.studentconnect.ui.components.filterCountries
import com.github.se.studentconnect.ui.components.loadCountries
import com.github.se.studentconnect.ui.profile.edit.EditNationalityViewModel

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
      val context = LocalContext.current
      EditNationalityViewModel(userRepository, userId, AndroidResourceProvider(context))
    },
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
  val user by viewModel.user.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val successMessage by viewModel.successMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  // Navigate back immediately after successful save
  LaunchedEffect(successMessage) {
    successMessage?.let {
      // Navigate back immediately after successful save
      onNavigateBack?.invoke()
      // Clear message after navigation to avoid cancelling LaunchedEffect
      viewModel.clearSuccessMessage()
    }
  }

  // Show error messages
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
                  text = stringResource(R.string.screen_title_edit_nationality),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
              IconButton(onClick = { onNavigateBack?.invoke() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_back))
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
              TitleText(stringResource(R.string.instruction_where_are_you_from))
              SmallSpacer()
              SubtitleText(stringResource(R.string.instruction_nationality_helps_connect))

              LargeSpacer()

              OutlinedTextField(
                  value = query,
                  onValueChange = { query = it },
                  modifier = Modifier.fillMaxWidth(),
                  placeholder = { Text(stringResource(R.string.placeholder_search_countries)) },
                  singleLine = true,
                  trailingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = stringResource(R.string.content_description_search))
                  })

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
                      Text(stringResource(R.string.button_save_changes))
                    }
                  }
            }
      }
}
