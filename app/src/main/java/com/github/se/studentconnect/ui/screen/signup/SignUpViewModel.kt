package com.github.se.studentconnect.ui.screen.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import java.util.Locale

/**
 * Represents the various steps of the signup flow, each corresponding to a specific screen in the
 * onboarding process.
 */
enum class SignUpStep {
  GettingStarted,
  BasicInfo,
  Nationality,
  AddPicture,
  Description,
  Experiences
}

/** Immutable data class holding all information entered by the user throughout the signup flow. */
data class SignUpState(
    val userId: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val birthdate: Timestamp? = null,
    val nationality: String? = null,
    val profilePictureUri: String? = null,
    val bio: String? = null,
    val interests: Set<String> = emptySet(),
    val currentStep: SignUpStep = SignUpStep.GettingStarted
)

/**
 * ViewModel managing the signup flow state.
 *
 * Handles user input updates, navigation between steps, and validation of individual form fields.
 */
class SignUpViewModel : ViewModel() {

  private val _state = mutableStateOf(SignUpState())
  val state: State<SignUpState> = _state

  private fun update(block: (SignUpState) -> SignUpState) {
    _state.value = block(_state.value)
  }

  // Field updates
  fun setUserId(userId: String) = update { it.copy(userId = userId.trim().ifBlank { null }) }

  fun setFirstName(firstName: String) = update { it.copy(firstName = firstName.trim()) }

  fun setLastName(lastName: String) = update { it.copy(lastName = lastName.trim()) }

  fun setBirthdate(birthdate: Timestamp?) = update { it.copy(birthdate = birthdate) }

  fun setNationality(nationality: String) = update {
    it.copy(nationality = nationality.trim().uppercase(Locale.US))
  }

  fun setProfilePictureUri(uri: String?) = update {
    it.copy(profilePictureUri = uri?.ifBlank { null })
  }

  fun setBio(bio: String?) = update { it.copy(bio = bio?.ifBlank { null }) }

  fun toggleInterest(key: String) = update {
    val k = key.trim().uppercase(Locale.US) // normalize
    it.copy(interests = it.interests.toMutableSet().apply { if (!add(k)) remove(k) })
  }

  // Navigation step helpers
  fun goTo(step: SignUpStep) = update { it.copy(currentStep = step) }

  fun nextStep() = update { it.copy(currentStep = it.currentStep.next()) }

  fun prevStep() = update { it.copy(currentStep = it.currentStep.prev()) }

  // Validation checks
  val isBasicInfoValid: Boolean
    get() =
        state.value.firstName.isNotBlank() &&
            state.value.firstName.length <= 100 &&
            state.value.lastName.isNotBlank() &&
            state.value.lastName.length <= 100

  val isNationalityValid: Boolean
    get() = !state.value.nationality.isNullOrBlank()

  val isBioValid: Boolean
    get() = (state.value.bio?.length ?: 0) <= 500

  fun reset() = update { SignUpState() }
}

/** Returns the next logical step in the signup sequence. */
private fun SignUpStep.next(): SignUpStep =
    when (this) {
      SignUpStep.GettingStarted -> SignUpStep.BasicInfo
      SignUpStep.BasicInfo -> SignUpStep.Nationality
      SignUpStep.Nationality -> SignUpStep.AddPicture
      SignUpStep.AddPicture -> SignUpStep.Description
      SignUpStep.Description -> SignUpStep.Experiences
      SignUpStep.Experiences -> SignUpStep.Experiences
    }

/** Returns the previous step in the signup sequence. */
private fun SignUpStep.prev(): SignUpStep =
    when (this) {
      SignUpStep.GettingStarted -> SignUpStep.GettingStarted
      SignUpStep.BasicInfo -> SignUpStep.GettingStarted
      SignUpStep.Nationality -> SignUpStep.BasicInfo
      SignUpStep.AddPicture -> SignUpStep.Nationality
      SignUpStep.Description -> SignUpStep.AddPicture
      SignUpStep.Experiences -> SignUpStep.Description
    }
