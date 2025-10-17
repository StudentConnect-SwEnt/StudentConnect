package com.github.se.studentconnect.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileConstantsTest {

  @Test
  fun `MAX_BIO_LENGTH is correct`() {
    assertEquals(500, ProfileConstants.MAX_BIO_LENGTH)
  }

  @Test
  fun `MAX_NAME_LENGTH is correct`() {
    assertEquals(100, ProfileConstants.MAX_NAME_LENGTH)
  }

  @Test
  fun `MAX_UNIVERSITY_LENGTH is correct`() {
    assertEquals(200, ProfileConstants.MAX_UNIVERSITY_LENGTH)
  }

  @Test
  fun `DATE_FORMAT is correct`() {
    assertEquals("dd/MM/yyyy", ProfileConstants.DATE_FORMAT)
  }

  @Test
  fun `NAVIGATION_DELAY_MS is correct`() {
    assertEquals(500L, ProfileConstants.NAVIGATION_DELAY_MS)
  }

  @Test
  fun `SUCCESS_NAME_UPDATED is correct`() {
    assertEquals("Name updated successfully", ProfileConstants.SUCCESS_NAME_UPDATED)
  }

  @Test
  fun `SUCCESS_UNIVERSITY_UPDATED is correct`() {
    assertEquals("University updated successfully", ProfileConstants.SUCCESS_UNIVERSITY_UPDATED)
  }

  @Test
  fun `SUCCESS_COUNTRY_UPDATED is correct`() {
    assertEquals("Country updated successfully", ProfileConstants.SUCCESS_COUNTRY_UPDATED)
  }

  @Test
  fun `SUCCESS_BIRTHDAY_UPDATED is correct`() {
    assertEquals("Birthday updated successfully", ProfileConstants.SUCCESS_BIRTHDAY_UPDATED)
  }

  @Test
  fun `SUCCESS_ACTIVITIES_UPDATED is correct`() {
    assertEquals("Activities updated successfully", ProfileConstants.SUCCESS_ACTIVITIES_UPDATED)
  }

  @Test
  fun `SUCCESS_BIO_UPDATED is correct`() {
    assertEquals("Bio updated successfully", ProfileConstants.SUCCESS_BIO_UPDATED)
  }

  @Test
  fun `SUCCESS_PROFILE_UPDATED is correct`() {
    assertEquals("Profile updated successfully", ProfileConstants.SUCCESS_PROFILE_UPDATED)
  }

  @Test
  fun `SUCCESS_PROFILE_PICTURE_UPDATED is correct`() {
    assertEquals(
        "Profile picture updated successfully", ProfileConstants.SUCCESS_PROFILE_PICTURE_UPDATED)
  }

  @Test
  fun `ERROR_FIRST_NAME_EMPTY is correct`() {
    assertEquals("First name cannot be empty", ProfileConstants.ERROR_FIRST_NAME_EMPTY)
  }

  @Test
  fun `ERROR_LAST_NAME_EMPTY is correct`() {
    assertEquals("Last name cannot be empty", ProfileConstants.ERROR_LAST_NAME_EMPTY)
  }

  @Test
  fun `ERROR_NAME_EMPTY is correct`() {
    assertEquals("First name and last name cannot be empty", ProfileConstants.ERROR_NAME_EMPTY)
  }

  @Test
  fun `ERROR_UNIVERSITY_EMPTY is correct`() {
    assertEquals("University cannot be empty", ProfileConstants.ERROR_UNIVERSITY_EMPTY)
  }

  @Test
  fun `ERROR_BIO_EMPTY is correct`() {
    assertEquals("Bio cannot be empty", ProfileConstants.ERROR_BIO_EMPTY)
  }

  @Test
  fun `ERROR_BIO_TOO_LONG is correct`() {
    assertEquals("Bio exceeds 500 characters", ProfileConstants.ERROR_BIO_TOO_LONG)
  }

  @Test
  fun `ERROR_DATE_FORMAT is correct`() {
    assertEquals("Please use DD/MM/YYYY format", ProfileConstants.ERROR_DATE_FORMAT)
  }

  @Test
  fun `ERROR_USER_NOT_FOUND is correct`() {
    assertEquals("User not found", ProfileConstants.ERROR_USER_NOT_FOUND)
  }

  @Test
  fun `ERROR_LOAD_PROFILE is correct`() {
    assertEquals("Failed to load profile", ProfileConstants.ERROR_LOAD_PROFILE)
  }

  @Test
  fun `ERROR_SAVE_NAME is correct`() {
    assertEquals("Failed to save name", ProfileConstants.ERROR_SAVE_NAME)
  }

  @Test
  fun `ERROR_SAVE_BIO is correct`() {
    assertEquals("Failed to save bio", ProfileConstants.ERROR_SAVE_BIO)
  }

  @Test
  fun `ERROR_UNEXPECTED is correct`() {
    assertEquals("An unexpected error occurred", ProfileConstants.ERROR_UNEXPECTED)
  }

  @Test
  fun `ERROR_PROFILE_NOT_FOUND is correct`() {
    assertEquals("Profile not found", ProfileConstants.ERROR_PROFILE_NOT_FOUND)
  }

  @Test
  fun `LABEL_FIRST_NAME is correct`() {
    assertEquals("First Name", ProfileConstants.LABEL_FIRST_NAME)
  }

  @Test
  fun `LABEL_LAST_NAME is correct`() {
    assertEquals("Last Name", ProfileConstants.LABEL_LAST_NAME)
  }

  @Test
  fun `LABEL_UNIVERSITY is correct`() {
    assertEquals("University", ProfileConstants.LABEL_UNIVERSITY)
  }

  @Test
  fun `LABEL_COUNTRY is correct`() {
    assertEquals("Country", ProfileConstants.LABEL_COUNTRY)
  }

  @Test
  fun `LABEL_BIRTHDAY is correct`() {
    assertEquals("Birthday", ProfileConstants.LABEL_BIRTHDAY)
  }

  @Test
  fun `LABEL_ACTIVITIES is correct`() {
    assertEquals("Favourite Activities", ProfileConstants.LABEL_ACTIVITIES)
  }

  @Test
  fun `LABEL_BIO is correct`() {
    assertEquals("More About Me", ProfileConstants.LABEL_BIO)
  }

  @Test
  fun `PLACEHOLDER_FIRST_NAME is correct`() {
    assertEquals("John", ProfileConstants.PLACEHOLDER_FIRST_NAME)
  }

  @Test
  fun `PLACEHOLDER_LAST_NAME is correct`() {
    assertEquals("Doe", ProfileConstants.PLACEHOLDER_LAST_NAME)
  }

  @Test
  fun `PLACEHOLDER_BIO is correct`() {
    assertEquals("Tell us about yourself...", ProfileConstants.PLACEHOLDER_BIO)
  }

  @Test
  fun `PLACEHOLDER_NOT_SPECIFIED is correct`() {
    assertEquals("Not specified", ProfileConstants.PLACEHOLDER_NOT_SPECIFIED)
  }

  @Test
  fun `INSTRUCTION_ENTER_NAME is correct`() {
    assertEquals("Enter your full name", ProfileConstants.INSTRUCTION_ENTER_NAME)
  }

  @Test
  fun `INSTRUCTION_TELL_ABOUT_YOURSELF is correct`() {
    assertEquals("Tell us about yourself", ProfileConstants.INSTRUCTION_TELL_ABOUT_YOURSELF)
  }
}
