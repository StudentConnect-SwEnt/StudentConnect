package com.github.se.studentconnect.ui.screen.signup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignUpViewModelTest {

  private lateinit var viewModel: SignUpViewModel

  @Before
  fun setUp() {
    viewModel = SignUpViewModel()
  }

  @Test
  fun setFirstName_trimsAndUpdatesState() {
    // Arrange
    val spacedName = "  Ada  "

    // Act
    viewModel.setFirstName(spacedName)

    // Assert
    assertEquals("Ada", viewModel.state.value.firstName)
  }

  @Test
  fun setLastName_trimsAndUpdatesState() {
    // Arrange
    val spacedName = "  Lovelace  "

    // Act
    viewModel.setLastName(spacedName)

    // Assert
    assertEquals("Lovelace", viewModel.state.value.lastName)
  }

  @Test
  fun setBirthdate_updatesValue() {
    // Arrange
    val birthdate = 1_234_567_890L

    // Act
    viewModel.setBirthdate(birthdate)

    // Assert
    assertEquals(birthdate, viewModel.state.value.birthdateMillis)
  }

  @Test
  fun setNationality_normalizesToUppercase() {
    // Arrange
    val nationality = "   swiss   "

    // Act
    viewModel.setNationality(nationality)

    // Assert
    assertEquals("SWISS", viewModel.state.value.nationality)
  }

  @Test
  fun setProfilePictureUri_handlesBlankInput() {
    // Arrange
    val blank = "   "

    // Act
    viewModel.setProfilePictureUri(blank)

    // Assert
    assertNull(viewModel.state.value.profilePictureUri)
  }

  @Test
  fun setProfilePictureUri_handlesNullAfterValue() {
    // Arrange
    viewModel.setProfilePictureUri("photo.jpg")

    // Act
    viewModel.setProfilePictureUri(null)

    // Assert
    assertNull(viewModel.state.value.profilePictureUri)
  }

  @Test
  fun setBio_handlesBlankInput() {
    // Arrange
    val blank = " "

    // Act
    viewModel.setBio(blank)

    // Assert
    assertNull(viewModel.state.value.bio)
  }

  @Test
  fun setBio_handlesNullAfterValue() {
    // Arrange
    viewModel.setBio("Existing bio")

    // Act
    viewModel.setBio(null)

    // Assert
    assertNull(viewModel.state.value.bio)
  }

  @Test
  fun toggleInterest_addsAndRemovesNormalizedKey() {
    // Arrange
    val interest = "  Hiking  "

    // Act
    viewModel.toggleInterest(interest)

    // Assert
    val interests = viewModel.state.value.interests
    assertTrue(interests.contains("HIKING"))

    // Act
    viewModel.toggleInterest(interest)

    // Assert
    assertFalse(viewModel.state.value.interests.contains("HIKING"))
  }

  @Test
  fun nextStep_walksThroughSequenceUntilWelcome() {
    // Arrange
    val visitedSteps = mutableListOf<SignUpStep>()

    // Act
    repeat(SignUpStep.values().size) {
      visitedSteps += viewModel.state.value.currentStep
      viewModel.nextStep()
    }

    // Assert
    assertEquals(
        listOf(
            SignUpStep.GettingStarted,
            SignUpStep.BasicInfo,
            SignUpStep.Nationality,
            SignUpStep.AddPicture,
            SignUpStep.Bio,
            SignUpStep.Interests,
            SignUpStep.Welcome),
        visitedSteps)
    assertEquals(SignUpStep.Welcome, viewModel.state.value.currentStep)
  }

  @Test
  fun prevStep_walksBackThroughSequenceUntilStart() {
    // Arrange
    viewModel.goTo(SignUpStep.Welcome)

    // Act
    val visitedSteps = mutableListOf<SignUpStep>()
    repeat(SignUpStep.values().size) {
      visitedSteps += viewModel.state.value.currentStep
      viewModel.prevStep()
    }

    // Assert
    assertEquals(
        listOf(
            SignUpStep.Welcome,
            SignUpStep.Interests,
            SignUpStep.Bio,
            SignUpStep.AddPicture,
            SignUpStep.Nationality,
            SignUpStep.BasicInfo,
            SignUpStep.GettingStarted),
        visitedSteps)
    assertEquals(SignUpStep.GettingStarted, viewModel.state.value.currentStep)
  }

  @Test
  fun isBasicInfoValid_requiresNonBlankAndWithinBounds() {
    // Arrange
    viewModel.setFirstName("Grace")
    viewModel.setLastName("Hopper")

    // Act
    val validResult = viewModel.isBasicInfoValid

    // Assert
    assertTrue(validResult)

    // Arrange
    val longName = "a".repeat(101)
    viewModel.setFirstName(longName)

    // Act
    val invalidResult = viewModel.isBasicInfoValid

    // Assert
    assertFalse(invalidResult)

    // Arrange
    viewModel.setFirstName("Grace")
    viewModel.setLastName("")

    // Act
    val blankLastNameResult = viewModel.isBasicInfoValid

    // Assert
    assertFalse(blankLastNameResult)

    // Arrange
    val longLastName = "b".repeat(101)
    viewModel.setLastName(longLastName)

    // Act
    val longLastNameResult = viewModel.isBasicInfoValid

    // Assert
    assertFalse(longLastNameResult)
  }

  @Test
  fun isNationalityValid_checksForNonBlankValue() {
    // Arrange
    viewModel.setNationality("Swiss")

    // Act
    val validResult = viewModel.isNationalityValid

    // Assert
    assertTrue(validResult)

    // Act
    viewModel.setNationality(" ")
    val invalidResult = viewModel.isNationalityValid

    // Assert
    assertFalse(invalidResult)

    // Act
    viewModel.reset()
    val defaultResult = viewModel.isNationalityValid

    // Assert
    assertFalse(defaultResult)
  }

  @Test
  fun isBioValid_enforcesMaxLength() {
    // Arrange
    viewModel.setBio("Short bio")

    // Act
    val validResult = viewModel.isBioValid

    // Assert
    assertTrue(validResult)

    // Arrange
    val longBio = "x".repeat(501)
    viewModel.setBio(longBio)

    // Act
    val invalidResult = viewModel.isBioValid

    // Assert
    assertFalse(invalidResult)

    // Act
    viewModel.setBio(null)
    val nullResult = viewModel.isBioValid

    // Assert
    assertTrue(nullResult)
  }

  @Test
  fun reset_restoresInitialState() {
    // Arrange
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(42L)
    viewModel.setNationality("UK")
    viewModel.setProfilePictureUri("uri")
    viewModel.setBio("bio")
    viewModel.toggleInterest("math")
    viewModel.goTo(SignUpStep.Welcome)

    // Act
    viewModel.reset()

    // Assert
    val state = viewModel.state.value
    assertEquals(SignUpState(), state)
  }
}
