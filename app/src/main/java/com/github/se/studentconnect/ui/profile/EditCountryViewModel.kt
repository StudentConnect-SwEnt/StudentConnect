package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import java.util.Locale
import kotlin.code
import kotlin.collections.filter
import kotlin.collections.firstOrNull
import kotlin.collections.map
import kotlin.collections.sorted
import kotlin.text.any
import kotlin.text.contains
import kotlin.text.isBlank
import kotlin.text.uppercase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditCountryViewModel(private val userRepository: UserRepository, private val userId: String) :
    ViewModel() {

  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  private val _availableCountries = MutableStateFlow<List<String>>(emptyList())
  val availableCountries: StateFlow<List<String>> = _availableCountries.asStateFlow()

  private val _selectedCountry = MutableStateFlow<Set<String>>(emptySet())
  val selectedCountry: StateFlow<Set<String>> = _selectedCountry.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // Filtered Country based on search
  private val _filteredCountries = MutableStateFlow<List<String>>(emptyList())
  val filteredCountries: StateFlow<List<String>> = _filteredCountries.asStateFlow()

  // Loading state
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Error state
  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  // Success state
  private val _successMessage = MutableStateFlow<String?>(null)
  val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

  init {
    loadUserProfile()
    initializeAvailableCountries()
  }

  private fun loadUserProfile() {
    viewModelScope.launch {
      userRepository.getUserById(
          userId = userId,
          onSuccess = { user ->
            _user.value = user
            _selectedCountry.value = setOfNotNull(user?.country)
          },
          onFailure = { exception ->
            _errorMessage.value = exception.message ?: "Failed to load profile"
          })
    }
  }

  private fun initializeAvailableCountries() {
    // On génère la liste de tous les pays du monde à partir du code ISO
    val countries =
        Locale.getISOCountries()
            .map { iso ->
              val locale = Locale("", iso)
              val name = locale.getDisplayCountry(Locale.US)
              val flag = countryCodeToEmoji(iso)
              "$flag $name"
            }
            .sorted()

    _availableCountries.value = countries
    _filteredCountries.value = countries
  }

  private fun countryCodeToEmoji(countryCode: String): String {
    val normalized = countryCode.uppercase(Locale.US)
    if (normalized.length != 2 || normalized.any { it !in 'A'..'Z' }) return "🌐"
    val first = normalized[0].code - 'A'.code + 0x1F1E6
    val second = normalized[1].code - 'A'.code + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
  }

  fun selectCountry(country: String) {
    _selectedCountry.value = setOf(country)
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
    val filtered =
        if (query.isBlank()) {
          _availableCountries.value
        } else {
          _availableCountries.value.filter { country -> country.contains(query, ignoreCase = true) }
        }
    _filteredCountries.value = filtered
  }

  fun isCountrySelected(country: String): Boolean {
    return _selectedCountry.value.contains(country)
  }

  fun saveCountry() {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        _errorMessage.value = null

        val currentUser = _user.value ?: throw kotlin.Exception("User not found")

        // Récupère le pays sélectionné (ou null si rien)
        val selected = _selectedCountry.value.firstOrNull()

        // Met à jour l’objet utilisateur
        val updatedUser = currentUser.update(country = User.UpdateValue.SetValue(selected))

        // Sauvegarde dans Firestore
        userRepository.saveUser(
            user = updatedUser,
            onSuccess = {
              _user.value = updatedUser
              _isLoading.value = false
              _successMessage.value = "Country updated successfully!"
            },
            onFailure = { exception ->
              _isLoading.value = false
              _errorMessage.value = exception.message ?: "Failed to update country"
            })
      } catch (e: Exception) {
        _isLoading.value = false
        _errorMessage.value = e.message ?: "Failed to update country"
      }
    }
  }

  fun clearErrorMessage() {
    _errorMessage.value = null
  }

  fun clearSuccessMessage() {
    _successMessage.value = null
  }
}
