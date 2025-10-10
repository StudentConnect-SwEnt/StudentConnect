package com.github.se.studentconnect.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.isNotEmpty
import kotlin.let

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCountryScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: EditCountryViewModel = viewModel { EditCountryViewModel(userRepository, userId) },
    modifier: Modifier = Modifier
) {
  // --- Observe les états du ViewModel ---
  val user by viewModel.user.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val filteredCountries by viewModel.filteredCountries.collectAsState()
  val selectedCountry by viewModel.selectedCountry.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val successMessage by viewModel.successMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  // --- Affiche les messages de succès/erreur ---
  LaunchedEffect(successMessage) {
    successMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearSuccessMessage()
      onNavigateBack() // retour au profil
    }
  }

  LaunchedEffect(errorMessage) {
    errorMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearErrorMessage()
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Select Your Country",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface))
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      modifier = modifier) { paddingValues ->
        when (user) {
          null -> {
            // --- État de chargement ---
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator()
                }
          }
          else -> {
            // --- Contenu principal ---
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                  // 🧠 Barre de recherche
                  OutlinedTextField(
                      value = searchQuery,
                      onValueChange = { viewModel.updateSearchQuery(it) },
                      label = { Text("Search countries...") },
                      leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                      modifier = Modifier.fillMaxWidth(),
                      singleLine = true)

                  //  Liste de pays
                  LazyColumn(
                      verticalArrangement = Arrangement.spacedBy(8.dp),
                      modifier = Modifier.weight(1f)) {
                        items(filteredCountries) { country ->
                          CountryItem(
                              country = country,
                              isSelected = selectedCountry.contains(country),
                              onClick = { viewModel.selectCountry(country) })
                        }
                      }

                  //  Bouton de sauvegarde
                  Button(
                      onClick = { viewModel.saveCountry() },
                      enabled = !isLoading && selectedCountry.isNotEmpty(),
                      modifier = Modifier.fillMaxWidth()) {
                        if (isLoading) {
                          CircularProgressIndicator(
                              modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                        }
                        Text(text = if (isLoading) "Saving..." else "Save Country")
                      }
                }
          }
        }
      }
}

// --- Affichage d’un pays ---
@Composable
private fun CountryItem(
    country: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  val backgroundColor =
      if (isSelected) MaterialTheme.colorScheme.primaryContainer
      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

  val borderColor =
      if (isSelected) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

  Card(
      modifier = modifier.fillMaxWidth().clickable { onClick() },
      colors = CardDefaults.cardColors(containerColor = backgroundColor),
      border = BorderStroke(1.dp, borderColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Text(
            text = country,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
            modifier = Modifier.padding(16.dp))
      }
}
