package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.resources.ResourceProvider
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Profile Settings screen with inline editing functionality.
 *
 * Manages user profile data and handles individual field updates with proper state management,
 * validation, and error handling.
 *
 * @param userRepository Repository for user data operations
 * @param currentUserId The ID of the current user
 * @param resourceProvider Provider for accessing string resources
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val currentUserId: String,
    private val resourceProvider: ResourceProvider
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

  /** Loads the current user's profile from the repository. */
  fun loadUserProfile() {
    viewModelScope.launch {
      try {
        val loadedUser = userRepository.getUserById(currentUserId)
        _user.value = loadedUser
      } catch (exception: Exception) {
        _fieldErrors.value =
            mapOf(EditingField.None to (exception.message ?: resourceProvider.getString(R.string.error_failed_to_load_profile)))
      }
    }
  }

  /**
   * Starts editing a specific field.
   *
   * @param field The field to start editing
   */
  fun startEditing(field: EditingField) {
    _editingField.value = field
    _fieldErrors.value = _fieldErrors.value - field
  }

  /** Cancels editing the current field. */
  fun cancelEditing() {
    _editingField.value = EditingField.None
    _fieldErrors.value = _fieldErrors.value - _editingField.value
  }

  /**
   * Updates the user's name (firstName and lastName).
   *
   * @param firstName The new first name
   * @param lastName The new last name
   */
  fun updateName(firstName: String, lastName: String) {
    if (firstName.isBlank() || lastName.isBlank()) {
      _fieldErrors.value =
          _fieldErrors.value + (EditingField.Name to resourceProvider.getString(R.string.error_name_empty))
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

  /**
   * Updates the user's university.
   *
   * @param university The new university name
   */
  fun updateUniversity(university: String) {
    if (university.isBlank()) {
      _fieldErrors.value =
          _fieldErrors.value + (EditingField.University to resourceProvider.getString(R.string.error_university_empty))
      return
    }

    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.University, true)

    val updatedUser = currentUser.update(university = User.UpdateValue.SetValue(university.trim()))

    updateUserInFirebase(updatedUser, EditingField.University)
  }

  /**
   * Updates the user's country.
   *
   * @param country The new country name
   */
  fun updateCountry(country: String) {
    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.Country, true)

    val updatedUser =
        currentUser.update(
            country = User.UpdateValue.SetValue(country.trim().takeIf { it.isNotBlank() }))

    updateUserInFirebase(updatedUser, EditingField.Country)
  }

  /**
   * Updates the user's birthday.
   *
   * @param birthday The new birthday in DD/MM/YYYY format
   */
  fun updateBirthday(birthday: String) {
    if (birthday.isNotBlank() && !isValidDateFormat(birthday)) {
      _fieldErrors.value =
          _fieldErrors.value + (EditingField.Birthday to resourceProvider.getString(R.string.error_date_format))
      return
    }

    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.Birthday, true)

    val updatedUser =
        currentUser.update(
            birthday = User.UpdateValue.SetValue(birthday.trim().takeIf { it.isNotBlank() }))

    updateUserInFirebase(updatedUser, EditingField.Birthday)
  }

  /**
   * Updates the user's hobbies/activities.
   *
   * @param activities Comma-separated string of activities
   */
  fun updateActivities(activities: String) {
    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.Activities, true)

    val hobbiesList = activities.split(",").map { it.trim() }.filter { it.isNotBlank() }

    val updatedUser = currentUser.update(hobbies = User.UpdateValue.SetValue(hobbiesList))

    updateUserInFirebase(updatedUser, EditingField.Activities)
  }

  /**
   * Updates the user's bio.
   *
   * @param bio The new bio text
   */
  fun updateBio(bio: String) {
    val currentUser = _user.value ?: return
    setFieldLoading(EditingField.Bio, true)

    val updatedUser =
        currentUser.update(bio = User.UpdateValue.SetValue(bio.trim().takeIf { it.isNotBlank() }))

    updateUserInFirebase(updatedUser, EditingField.Bio)
  }

  /** Clears the success message. */
  fun clearSuccessMessage() {
    _successMessage.value = null
  }

  /** Clears all field errors. */
  fun clearFieldErrors() {
    _fieldErrors.value = emptyMap()
  }

  /**
   * Helper method to update user in the repository.
   *
   * @param updatedUser The updated user object
   * @param field The field being updated
   */
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
                (field to (exception.message ?: String.format("Failed to update %s", field.displayName)))
      } finally {
        setFieldLoading(field, false)
      }
    }
  }

  /**
   * Helper method to set loading state for a field.
   *
   * @param field The field to set loading state for
   * @param isLoading Whether the field is loading
   */
  private fun setFieldLoading(field: EditingField, isLoading: Boolean) {
    _loadingFields.value =
        if (isLoading) {
          _loadingFields.value + field
        } else {
          _loadingFields.value - field
        }
  }

  /**
   * Validates date format (DD/MM/YYYY).
   *
   * @param date The date string to validate
   * @return true if the date format is valid, false otherwise
   */
  private fun isValidDateFormat(date: String): Boolean {
    return try {
      val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
      format.isLenient = false
      format.parse(date)
      true
    } catch (e: Exception) {
      false
    }
  }

  /**
   * Gets success message for a field update.
   *
   * @param field The field that was updated
   * @return The success message for the field
   */
  private fun getSuccessMessage(field: EditingField): String {
    return when (field) {
      EditingField.Name -> resourceProvider.getString(R.string.success_name_updated)
      EditingField.University -> resourceProvider.getString(R.string.success_university_updated)
      EditingField.Country -> resourceProvider.getString(R.string.success_country_updated)
      EditingField.Birthday -> resourceProvider.getString(R.string.success_birthday_updated)
      EditingField.Activities -> resourceProvider.getString(R.string.success_activities_updated)
      EditingField.Bio -> resourceProvider.getString(R.string.success_bio_updated)
      EditingField.None -> resourceProvider.getString(R.string.success_profile_updated)
    }
  }
}

/**
 * Represents which field is currently being edited.
 *
 * This sealed class defines all the editable fields in the user profile, allowing for type-safe
 * field management and UI state handling.
 */
sealed class EditingField {
  object Name : EditingField()

  object University : EditingField()

  object Country : EditingField()

  object Birthday : EditingField()

  object Activities : EditingField()

  object Bio : EditingField()

  object None : EditingField()

  /**
   * Returns the display name for the field.
   *
   * @return The human-readable name of the field
   */
  val displayName: String
    get() =
        when (this) {
          is Name -> "Name"
          is University -> "University"
          is Country -> "Country"
          is Birthday -> "Birthday"
          is Activities -> "Activities"
          is Bio -> "Bio"
          is None -> "Profile"
        }
}
