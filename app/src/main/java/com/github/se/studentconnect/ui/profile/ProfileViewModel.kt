package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.minus
import kotlin.collections.plus
import kotlin.takeIf
import kotlin.text.isBlank
import kotlin.text.isNotBlank
import kotlin.text.split
import kotlin.text.trim
import kotlin.to
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Profile screen with inline editing functionality. Manages user profile data and
 * handles individual field updates.
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val currentUserId: String
) : ViewModel() {

  // User data state
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  // Editing state management
  private val _editingField = MutableStateFlow<EditingField>(EditingField.None)
  val editingField: StateFlow<EditingField> = _editingField.asStateFlow()

  // Loading states for individual fields
  private val _loadingFields = MutableStateFlow<Set<EditingField>>(emptySet())
  val loadingFields: StateFlow<Set<EditingField>> = _loadingFields.asStateFlow()

  // Error states for individual fields
  private val _fieldErrors = MutableStateFlow<Map<EditingField, String>>(emptyMap())
  val fieldErrors: StateFlow<Map<EditingField, String>> = _fieldErrors.asStateFlow()

  // Success message state
  private val _successMessage = MutableStateFlow<String?>(null)
  val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

  init {
    loadUserProfile()
  }

  /** Loads the current user's profile from Firebase. */
  public fun loadUserProfile() {
    viewModelScope.launch {
      try {
        val loadedUser = userRepository.getUserById(currentUserId)
        _user.value = loadedUser
      } catch (exception: Exception) {
        // Handle error - could show a snackbar or error state
        _fieldErrors.value =
            mapOf(EditingField.None to (exception.message ?: "Failed to load profile"))
      }
    }
  }

  /** Starts editing a specific field. */
  fun startEditing(field: EditingField) {
    _editingField.value = field
    _fieldErrors.value = _fieldErrors.value - field // Clear any previous error for this field
  }

  /** Cancels editing the current field. */
  fun cancelEditing() {
    val currentField = _editingField.value
    _fieldErrors.value = _fieldErrors.value - currentField
    _editingField.value = EditingField.None
  }

  /** Updates the user's name (firstName and lastName). */
  fun updateName(firstName: String, lastName: String) {
    if (firstName.isBlank() || lastName.isBlank()) {
      _fieldErrors.value =
          _fieldErrors.value + (EditingField.Name to "First name and last name cannot be empty")
      return
    }

    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.Name, true)

    val updatedUser =
        currentUser.update(
            firstName = User.UpdateValue.SetValue(firstName.trim()),
            lastName = User.UpdateValue.SetValue(lastName.trim()))

    updateUserInFirebase(updatedUser, EditingField.Name)
  }

  /** Updates the user's university. */
  fun updateUniversity(university: String) {
    if (university.isBlank()) {
      _fieldErrors.value =
          _fieldErrors.value + (EditingField.University to "University cannot be empty")
      return
    }

    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.University, true)

    val updatedUser = currentUser.update(university = User.UpdateValue.SetValue(university.trim()))

    updateUserInFirebase(updatedUser, EditingField.University)
  }

  /** Updates the user's country. */
  fun updateCountry(country: String) {
    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.Country, true)

    val updatedUser =
        currentUser.update(
            country = User.UpdateValue.SetValue(country.trim().takeIf { it.isNotBlank() }))

    updateUserInFirebase(updatedUser, EditingField.Country)
  }

  /** Updates the user's birthday. */
  fun updateBirthday(birthday: String) {
    // Validate date format (DD/MM/YYYY)
    if (birthday.isNotBlank() && !isValidDateFormat(birthday)) {
      _fieldErrors.value =
          _fieldErrors.value + (EditingField.Birthday to "Please use DD/MM/YYYY format")
      return
    }

    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.Birthday, true)

    val updatedUser =
        currentUser.update(
            birthday = User.UpdateValue.SetValue(birthday.trim().takeIf { it.isNotBlank() }))

    updateUserInFirebase(updatedUser, EditingField.Birthday)
  }

  /** Updates the user's hobbies/activities. */
  fun updateActivities(activities: String) {
    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.Activities, true)

    // Convert comma-separated string to list
    val hobbiesList = activities.split(",").map { it.trim() }.filter { it.isNotBlank() }

    val updatedUser = currentUser.update(hobbies = User.UpdateValue.SetValue(hobbiesList))

    updateUserInFirebase(updatedUser, EditingField.Activities)
  }

  /** Updates the user's bio. */
  fun updateBio(bio: String) {
    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.Bio, true)

    val updatedUser =
        currentUser.update(bio = User.UpdateValue.SetValue(bio.trim().takeIf { it.isNotBlank() }))

    updateUserInFirebase(updatedUser, EditingField.Bio)
  }

  /** Updates the user's profile picture URL. */
  fun updateProfilePicture(profilePictureUrl: String?) {
    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.ProfilePicture, true)

    val updatedUser =
        currentUser.update(profilePictureUrl = User.UpdateValue.SetValue(profilePictureUrl))

    updateUserInFirebase(updatedUser, EditingField.ProfilePicture)
  }

  /** Clears the success message. */
  fun clearSuccessMessage() {
    _successMessage.value = null
  }

  /** Clears field errors. */
  fun clearFieldErrors() {
    _fieldErrors.value = emptyMap()
  }

  /** Helper method to update user in Firebase. */
  private fun updateUserInFirebase(updatedUser: User, field: EditingField) {
    viewModelScope.launch {
      try {
        userRepository.saveUser(updatedUser)
        _user.value = updatedUser
        _editingField.value = EditingField.None
        _successMessage.value = getSuccessMessage(field)
      } catch (exception: Exception) {
        _fieldErrors.value =
            _fieldErrors.value +
                (field to (exception.message ?: "Failed to update ${field.displayName}"))
      } finally {
        setFieldLoading(field, false)
      }
    }
  }

  /** Helper method to set loading state for a field. */
  private fun setFieldLoading(field: EditingField, isLoading: Boolean) {
    _loadingFields.value =
        if (isLoading) {
          _loadingFields.value + field
        } else {
          _loadingFields.value - field
        }
  }

  /** Validates date format (DD/MM/YYYY). */
  private fun isValidDateFormat(date: String): Boolean {
    return try {
      val format = SimpleDateFormat("dd/MM/yyyy", Locale.UK)
      format.isLenient = false
      format.parse(date)
      true
    } catch (e: Exception) {
      false
    }
  }

  /** Gets success message for a field update. */
  private fun getSuccessMessage(field: EditingField): String {
    return when (field) {
      EditingField.Name -> "Name updated successfully"
      EditingField.University -> "University updated successfully"
      EditingField.Country -> "Country updated successfully"
      EditingField.Birthday -> "Birthday updated successfully"
      EditingField.Activities -> "Activities updated successfully"
      EditingField.Bio -> "Bio updated successfully"
      EditingField.ProfilePicture -> "Profile picture updated successfully"
      EditingField.None -> "Profile updated successfully"
    }
  }
}

/** Represents which field is currently being edited. */
sealed class EditingField {
  object Name : EditingField()

  object University : EditingField()

  object Country : EditingField()

  object Birthday : EditingField()

  object Activities : EditingField()

  object Bio : EditingField()

  object ProfilePicture : EditingField()

  object None : EditingField()

  /** Returns the display name for the field. */
  val displayName: String
    get() =
        when (this) {
          is Name -> "Name"
          is University -> "University"
          is Country -> "Country"
          is Birthday -> "Birthday"
          is Activities -> "Activities"
          is Bio -> "Bio"
          is ProfilePicture -> "Profile Picture"
          is None -> "Profile"
        }
}
