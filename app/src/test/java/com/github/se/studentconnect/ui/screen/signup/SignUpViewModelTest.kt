package com.github.se.studentconnect.ui.screen.signup

import androidx.core.net.toUri
import com.github.se.studentconnect.ui.screen.signup.regularuser.SignUpState
import com.github.se.studentconnect.ui.screen.signup.regularuser.SignUpStep
import com.github.se.studentconnect.ui.screen.signup.regularuser.SignUpViewModel
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SignUpViewModelTest {

  private lateinit var viewModel: SignUpViewModel

  @Before
  fun setUp() {
    viewModel = SignUpViewModel()
  }

  @Test
  fun `setUserId stores trimmed value`() {
    // Arrange
    val trimmedId = "uid_123"

    // Act
    viewModel.setUserId("  $trimmedId  ")

    // Assert
    assertEquals(trimmedId, viewModel.state.value.userId)
  }

  @Test
  fun `setUserId with blank clears value`() {
    // Arrange
    viewModel.setUserId("existing")

    // Act
    viewModel.setUserId("   ")

    // Assert
    assertNull(viewModel.state.value.userId)
  }

  @Test
  fun `setters update corresponding fields`() {
    // Arrange
    val firstName = "Ada"
    val lastName = "Lovelace"
    val username = "adalovelace"
    val birthdate = Timestamp(Date(42L))
    val nationality = "ch"
    val profileUri = "content://avatar".toUri()
    val bio = "Hello world"

    // Act
    viewModel.setFirstName("  $firstName ")
    viewModel.setLastName(" $lastName  ")
    viewModel.setUsername("  $username  ")
    viewModel.setBirthdate(birthdate)
    viewModel.setNationality(nationality)
    viewModel.setProfilePictureUri(profileUri)
    viewModel.setBio(bio)

    // Assert
    val state = viewModel.state.value
    assertEquals(firstName, state.firstName)
    assertEquals(lastName, state.lastName)
    assertEquals(username, state.username)
    assertEquals(birthdate, state.birthdate)
    assertEquals("CH", state.nationality)
    assertEquals(profileUri, state.profilePictureUri)
    assertEquals(bio, state.bio)
  }

  @Test
  fun `setProfilePictureUri blanks reset the field`() {
    // Arrange
    viewModel.setProfilePictureUri("content://photo".toUri())

    // Act
    viewModel.setProfilePictureUri("   ".toUri())

    // Assert
    assertNull(viewModel.state.value.profilePictureUri)
  }

  @Test
  fun `toggleInterest alternates membership`() {
    // Act & Assert
    viewModel.toggleInterest(" hiking ")
    assertTrue(viewModel.state.value.interests.contains("HIKING"))

    viewModel.toggleInterest("hiking")
    assertFalse(viewModel.state.value.interests.contains("HIKING"))
  }

  @Test
  fun `navigation helpers move between steps`() {
    // Arrange
    assertEquals(SignUpStep.GettingStarted, viewModel.state.value.currentStep)

    // Act
    viewModel.nextStep()

    // Assert
    assertEquals(SignUpStep.BasicInfo, viewModel.state.value.currentStep)

    // Act again
    viewModel.goTo(SignUpStep.Experiences)
    viewModel.prevStep()

    // Assert
    assertEquals(SignUpStep.Description, viewModel.state.value.currentStep)
  }

  @Test
  fun `validation flags reflect state`() {
    // Arrange
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setUsername("adalovelace")
    viewModel.setNationality(" UK ")
    viewModel.setBio("short bio")

    // Act & Assert
    assertTrue(viewModel.isBasicInfoValid)
    assertTrue(viewModel.isNationalityValid)
    assertTrue(viewModel.isBioValid)

    // Introduce invalid data
    viewModel.setFirstName("")
    viewModel.setUsername("ab")
    viewModel.setBio("x".repeat(600))

    assertFalse(viewModel.isBasicInfoValid)
    assertFalse(viewModel.isBioValid)
  }

  @Test
  fun `reset returns to initial state`() {
    // Arrange
    viewModel.setFirstName("Test")
    viewModel.setUserId("id")
    viewModel.goTo(SignUpStep.Experiences)

    // Act
    viewModel.reset()

    // Assert
    val state = viewModel.state.value
    assertEquals(SignUpState(), state)
  }

  @Test
  fun `nextStep remains on experiences once reached`() {
    // Arrange
    viewModel.goTo(SignUpStep.Experiences)

    // Act
    viewModel.nextStep()

    // Assert
    assertEquals(SignUpStep.Experiences, viewModel.state.value.currentStep)
  }

  @Test
  fun `prevStep keeps getting started at beginning`() {
    // Arrange
    viewModel.goTo(SignUpStep.GettingStarted)

    // Act
    viewModel.prevStep()

    // Assert
    assertEquals(SignUpStep.GettingStarted, viewModel.state.value.currentStep)
  }

  @Test
  fun `setBio blank clears value`() {
    viewModel.setBio("Some bio")
    viewModel.setBio("   ")

    assertNull(viewModel.state.value.bio)
  }

  @Test
  fun `setBirthdate handles null`() {
    viewModel.setBirthdate(Timestamp(Date(1234L)))
    viewModel.setBirthdate(null)

    assertNull(viewModel.state.value.birthdate)
  }

  @Test
  fun `nextStep iterates entire flow`() {
    val expectedOrder =
        listOf(
            SignUpStep.GettingStarted,
            SignUpStep.BasicInfo,
            SignUpStep.Nationality,
            SignUpStep.AddPicture,
            SignUpStep.Description,
            SignUpStep.Experiences)

    expectedOrder.drop(1).forEach { expected ->
      viewModel.nextStep()
      assertEquals(expected, viewModel.state.value.currentStep)
    }
  }

  @Test
  fun `prevStep walks backwards correctly`() {
    val sequence =
        listOf(
            SignUpStep.Experiences,
            SignUpStep.Description,
            SignUpStep.AddPicture,
            SignUpStep.Nationality,
            SignUpStep.BasicInfo,
            SignUpStep.GettingStarted)

    viewModel.goTo(SignUpStep.Experiences)
    sequence.drop(1).forEach { expected ->
      viewModel.prevStep()
      assertEquals(expected, viewModel.state.value.currentStep)
    }
  }

  @Test
  fun `interests stored uppercased`() {
    viewModel.toggleInterest("music")

    assertTrue(viewModel.state.value.interests.contains("MUSIC"))
  }

  // Username tests
  @Test
  fun `setUsername stores trimmed and lowercased value`() {
    // Arrange
    val username = "JohnDoe"

    // Act
    viewModel.setUsername("  $username  ")

    // Assert
    assertEquals("johndoe", viewModel.state.value.username)
  }

  @Test
  fun `setUsername normalizes to lowercase`() {
    viewModel.setUsername("USERNAME123")
    assertEquals("username123", viewModel.state.value.username)
  }

  @Test
  fun `setUsername trims whitespace`() {
    viewModel.setUsername("  testuser  ")
    assertEquals("testuser", viewModel.state.value.username)
  }

  @Test
  fun `isBasicInfoValid returns false when username is blank`() {
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setUsername("")

    assertFalse(viewModel.isBasicInfoValid)
  }

  @Test
  fun `isBasicInfoValid returns false when username is too short`() {
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setUsername("ab")

    assertFalse(viewModel.isBasicInfoValid)
  }

  @Test
  fun `isBasicInfoValid returns false when username is too long`() {
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setUsername("a".repeat(21))

    assertFalse(viewModel.isBasicInfoValid)
  }

  @Test
  fun `isBasicInfoValid returns false when username has invalid characters`() {
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setUsername("user@name")

    assertFalse(viewModel.isBasicInfoValid)
  }

  @Test
  fun `isBasicInfoValid returns true when username is valid`() {
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setUsername("validuser123")

    assertTrue(viewModel.isBasicInfoValid)
  }

  @Test
  fun `isBasicInfoValid accepts valid username formats`() {
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")

    val validUsernames = listOf("abc", "user123", "john_doe", "test-user", "user.name")

    validUsernames.forEach { username ->
      viewModel.setUsername(username)
      assertTrue("Username '$username' should be valid", viewModel.isBasicInfoValid)
    }
  }

  @Test
  fun `isBasicInfoValid requires valid username even with valid name fields`() {
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")

    assertFalse(viewModel.isBasicInfoValid)
  }

  @Test
  fun `reset clears username`() {
    viewModel.setUsername("testuser")
    viewModel.reset()

    assertEquals("", viewModel.state.value.username)
  }
}
