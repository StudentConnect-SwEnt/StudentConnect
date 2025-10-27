package com.github.se.studentconnect.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileConstantsTest {

  @Test
  fun `ProfileConstants contains all required success messages`() {
    assertEquals("Name updated successfully", ProfileConstants.SUCCESS_NAME_UPDATED)
    assertEquals("University updated successfully", ProfileConstants.SUCCESS_UNIVERSITY_UPDATED)
    assertEquals("Country updated successfully", ProfileConstants.SUCCESS_COUNTRY_UPDATED)
    assertEquals("Birthday updated successfully", ProfileConstants.SUCCESS_BIRTHDAY_UPDATED)
    assertEquals("Activities updated successfully", ProfileConstants.SUCCESS_ACTIVITIES_UPDATED)
    assertEquals("Bio updated successfully", ProfileConstants.SUCCESS_BIO_UPDATED)
    assertEquals(
        "Profile picture updated successfully", ProfileConstants.SUCCESS_PROFILE_PICTURE_UPDATED)
  }

  @Test
  fun `ProfileConstants contains all required error messages`() {
    assertEquals("First name cannot be empty", ProfileConstants.ERROR_FIRST_NAME_EMPTY)
    assertEquals("Last name cannot be empty", ProfileConstants.ERROR_LAST_NAME_EMPTY)
    assertEquals("Name cannot be empty", ProfileConstants.ERROR_NAME_EMPTY)
    assertEquals("University cannot be empty", ProfileConstants.ERROR_UNIVERSITY_EMPTY)
    assertEquals("Invalid date format. Please use DD/MM/YYYY", ProfileConstants.ERROR_DATE_FORMAT)
    assertEquals("Profile not found", ProfileConstants.ERROR_PROFILE_NOT_FOUND)
  }

  @Test
  fun `ProfileConstants success messages are not empty`() {
    assertFalse(ProfileConstants.SUCCESS_NAME_UPDATED.isBlank())
    assertFalse(ProfileConstants.SUCCESS_UNIVERSITY_UPDATED.isBlank())
    assertFalse(ProfileConstants.SUCCESS_COUNTRY_UPDATED.isBlank())
    assertFalse(ProfileConstants.SUCCESS_BIRTHDAY_UPDATED.isBlank())
    assertFalse(ProfileConstants.SUCCESS_ACTIVITIES_UPDATED.isBlank())
    assertFalse(ProfileConstants.SUCCESS_BIO_UPDATED.isBlank())
    assertFalse(ProfileConstants.SUCCESS_PROFILE_PICTURE_UPDATED.isBlank())
  }

  @Test
  fun `ProfileConstants error messages are not empty`() {
    assertFalse(ProfileConstants.ERROR_FIRST_NAME_EMPTY.isBlank())
    assertFalse(ProfileConstants.ERROR_LAST_NAME_EMPTY.isBlank())
    assertFalse(ProfileConstants.ERROR_NAME_EMPTY.isBlank())
    assertFalse(ProfileConstants.ERROR_UNIVERSITY_EMPTY.isBlank())
    assertFalse(ProfileConstants.ERROR_DATE_FORMAT.isBlank())
    assertFalse(ProfileConstants.ERROR_PROFILE_NOT_FOUND.isBlank())
  }

  @Test
  fun `ProfileConstants success messages contain appropriate keywords`() {
    assertTrue(ProfileConstants.SUCCESS_NAME_UPDATED.contains("updated"))
    assertTrue(ProfileConstants.SUCCESS_UNIVERSITY_UPDATED.contains("updated"))
    assertTrue(ProfileConstants.SUCCESS_COUNTRY_UPDATED.contains("updated"))
    assertTrue(ProfileConstants.SUCCESS_BIRTHDAY_UPDATED.contains("updated"))
    assertTrue(ProfileConstants.SUCCESS_ACTIVITIES_UPDATED.contains("updated"))
    assertTrue(ProfileConstants.SUCCESS_BIO_UPDATED.contains("updated"))
    assertTrue(ProfileConstants.SUCCESS_PROFILE_PICTURE_UPDATED.contains("updated"))
  }

  @Test
  fun `ProfileConstants error messages contain appropriate keywords`() {
    assertTrue(ProfileConstants.ERROR_FIRST_NAME_EMPTY.contains("empty"))
    assertTrue(ProfileConstants.ERROR_LAST_NAME_EMPTY.contains("empty"))
    assertTrue(ProfileConstants.ERROR_NAME_EMPTY.contains("empty"))
    assertTrue(ProfileConstants.ERROR_UNIVERSITY_EMPTY.contains("empty"))
    assertTrue(ProfileConstants.ERROR_DATE_FORMAT.contains("format"))
    assertTrue(ProfileConstants.ERROR_PROFILE_NOT_FOUND.contains("not found"))
  }

  @Test
  fun `ProfileConstants success messages are user-friendly`() {
    assertTrue(ProfileConstants.SUCCESS_NAME_UPDATED.contains("successfully"))
    assertTrue(ProfileConstants.SUCCESS_UNIVERSITY_UPDATED.contains("successfully"))
    assertTrue(ProfileConstants.SUCCESS_COUNTRY_UPDATED.contains("successfully"))
    assertTrue(ProfileConstants.SUCCESS_BIRTHDAY_UPDATED.contains("successfully"))
    assertTrue(ProfileConstants.SUCCESS_ACTIVITIES_UPDATED.contains("successfully"))
    assertTrue(ProfileConstants.SUCCESS_BIO_UPDATED.contains("successfully"))
    assertTrue(ProfileConstants.SUCCESS_PROFILE_PICTURE_UPDATED.contains("successfully"))
  }

  @Test
  fun `ProfileConstants error messages provide helpful guidance`() {
    assertTrue(ProfileConstants.ERROR_DATE_FORMAT.contains("DD/MM/YYYY"))
    assertTrue(ProfileConstants.ERROR_FIRST_NAME_EMPTY.contains("First name"))
    assertTrue(ProfileConstants.ERROR_LAST_NAME_EMPTY.contains("Last name"))
    assertTrue(ProfileConstants.ERROR_NAME_EMPTY.contains("Name"))
    assertTrue(ProfileConstants.ERROR_UNIVERSITY_EMPTY.contains("University"))
  }

  @Test
  fun `ProfileConstants messages are properly capitalized`() {
    assertTrue(ProfileConstants.SUCCESS_NAME_UPDATED[0].isUpperCase())
    assertTrue(ProfileConstants.SUCCESS_UNIVERSITY_UPDATED[0].isUpperCase())
    assertTrue(ProfileConstants.SUCCESS_COUNTRY_UPDATED[0].isUpperCase())
    assertTrue(ProfileConstants.SUCCESS_BIRTHDAY_UPDATED[0].isUpperCase())
    assertTrue(ProfileConstants.SUCCESS_ACTIVITIES_UPDATED[0].isUpperCase())
    assertTrue(ProfileConstants.SUCCESS_BIO_UPDATED[0].isUpperCase())
    assertTrue(ProfileConstants.SUCCESS_PROFILE_PICTURE_UPDATED[0].isUpperCase())

    assertTrue(ProfileConstants.ERROR_FIRST_NAME_EMPTY[0].isUpperCase())
    assertTrue(ProfileConstants.ERROR_LAST_NAME_EMPTY[0].isUpperCase())
    assertTrue(ProfileConstants.ERROR_NAME_EMPTY[0].isUpperCase())
    assertTrue(ProfileConstants.ERROR_UNIVERSITY_EMPTY[0].isUpperCase())
    assertTrue(ProfileConstants.ERROR_DATE_FORMAT[0].isUpperCase())
    assertTrue(ProfileConstants.ERROR_PROFILE_NOT_FOUND[0].isUpperCase())
  }

  @Test
  fun `ProfileConstants messages do not end with periods`() {
    assertFalse(ProfileConstants.SUCCESS_NAME_UPDATED.endsWith("."))
    assertFalse(ProfileConstants.SUCCESS_UNIVERSITY_UPDATED.endsWith("."))
    assertFalse(ProfileConstants.SUCCESS_COUNTRY_UPDATED.endsWith("."))
    assertFalse(ProfileConstants.SUCCESS_BIRTHDAY_UPDATED.endsWith("."))
    assertFalse(ProfileConstants.SUCCESS_ACTIVITIES_UPDATED.endsWith("."))
    assertFalse(ProfileConstants.SUCCESS_BIO_UPDATED.endsWith("."))
    assertFalse(ProfileConstants.SUCCESS_PROFILE_PICTURE_UPDATED.endsWith("."))

    assertFalse(ProfileConstants.ERROR_FIRST_NAME_EMPTY.endsWith("."))
    assertFalse(ProfileConstants.ERROR_LAST_NAME_EMPTY.endsWith("."))
    assertFalse(ProfileConstants.ERROR_NAME_EMPTY.endsWith("."))
    assertFalse(ProfileConstants.ERROR_UNIVERSITY_EMPTY.endsWith("."))
    assertFalse(ProfileConstants.ERROR_DATE_FORMAT.endsWith("."))
    assertFalse(ProfileConstants.ERROR_PROFILE_NOT_FOUND.endsWith("."))
  }

  @Test
  fun `ProfileConstants messages are consistent in style`() {
    // All success messages should follow the same pattern: "[Field] updated successfully"
    val successMessages =
        listOf(
            ProfileConstants.SUCCESS_NAME_UPDATED,
            ProfileConstants.SUCCESS_UNIVERSITY_UPDATED,
            ProfileConstants.SUCCESS_COUNTRY_UPDATED,
            ProfileConstants.SUCCESS_BIRTHDAY_UPDATED,
            ProfileConstants.SUCCESS_ACTIVITIES_UPDATED,
            ProfileConstants.SUCCESS_BIO_UPDATED,
            ProfileConstants.SUCCESS_PROFILE_PICTURE_UPDATED)

    successMessages.forEach { message -> assertTrue(message.endsWith("updated successfully")) }

    // All error messages should be clear and actionable
    val errorMessages =
        listOf(
            ProfileConstants.ERROR_FIRST_NAME_EMPTY,
            ProfileConstants.ERROR_LAST_NAME_EMPTY,
            ProfileConstants.ERROR_NAME_EMPTY,
            ProfileConstants.ERROR_UNIVERSITY_EMPTY,
            ProfileConstants.ERROR_DATE_FORMAT,
            ProfileConstants.ERROR_PROFILE_NOT_FOUND)

    errorMessages.forEach { message ->
      assertTrue(
          message.contains("cannot be") ||
              message.contains("not found") ||
              message.contains("format"))
    }
  }

  @Test
  fun `ProfileConstants messages are appropriate length`() {
    val allMessages =
        listOf(
            ProfileConstants.SUCCESS_NAME_UPDATED,
            ProfileConstants.SUCCESS_UNIVERSITY_UPDATED,
            ProfileConstants.SUCCESS_COUNTRY_UPDATED,
            ProfileConstants.SUCCESS_BIRTHDAY_UPDATED,
            ProfileConstants.SUCCESS_ACTIVITIES_UPDATED,
            ProfileConstants.SUCCESS_BIO_UPDATED,
            ProfileConstants.SUCCESS_PROFILE_PICTURE_UPDATED,
            ProfileConstants.ERROR_FIRST_NAME_EMPTY,
            ProfileConstants.ERROR_LAST_NAME_EMPTY,
            ProfileConstants.ERROR_NAME_EMPTY,
            ProfileConstants.ERROR_UNIVERSITY_EMPTY,
            ProfileConstants.ERROR_DATE_FORMAT,
            ProfileConstants.ERROR_PROFILE_NOT_FOUND)

    allMessages.forEach { message ->
      assertTrue("Message too short: $message", message.length >= 10)
      assertTrue("Message too long: $message", message.length <= 100)
    }
  }
}
