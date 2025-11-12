package com.github.se.studentconnect.ui.profile

/**
 * Constants used throughout the profile package.
 *
 * This object centralizes all magic numbers, strings, and configuration values used in the profile
 * editing functionality.
 *
 * Note: String constants have been migrated to strings.xml for i18n support.
 * This object maintains hardcoded strings for test compatibility only.
 * Production code should use stringResource() or ResourceProvider.
 */
object ProfileConstants {

  // Character limits
  const val MAX_BIO_LENGTH = 500
  const val MAX_NAME_LENGTH = 100
  const val MAX_UNIVERSITY_LENGTH = 200

  // Date format
  const val DATE_FORMAT = "dd/MM/yyyy"

  // Navigation delays
  const val NAVIGATION_DELAY_MS = 500L

  // Success messages - hardcoded for test compatibility
  const val SUCCESS_NAME_UPDATED = "Name updated successfully"
  const val SUCCESS_UNIVERSITY_UPDATED = "University updated successfully"
  const val SUCCESS_COUNTRY_UPDATED = "Country updated successfully"
  const val SUCCESS_BIRTHDAY_UPDATED = "Birthday updated successfully"
  const val SUCCESS_ACTIVITIES_UPDATED = "Activities updated successfully"
  const val SUCCESS_BIO_UPDATED = "Bio updated successfully"
  const val SUCCESS_PROFILE_UPDATED = "Profile updated successfully"
  const val SUCCESS_PROFILE_PICTURE_UPDATED = "Profile picture updated successfully"

  // Error messages - hardcoded for test compatibility
  const val ERROR_FIRST_NAME_EMPTY = "First name cannot be empty"
  const val ERROR_LAST_NAME_EMPTY = "Last name cannot be empty"
  const val ERROR_NAME_EMPTY = "First name and last name cannot be empty"
  const val ERROR_UNIVERSITY_EMPTY = "University cannot be empty"
  const val ERROR_BIO_EMPTY = "Bio cannot be empty"
  const val ERROR_BIO_TOO_LONG = "Bio exceeds $MAX_BIO_LENGTH characters"
  const val ERROR_DATE_FORMAT = "Please use DD/MM/YYYY format"
  const val ERROR_USER_NOT_FOUND = "User not found"
  const val ERROR_LOAD_PROFILE = "Failed to load profile"
  const val ERROR_SAVE_NAME = "Failed to save name"
  const val ERROR_SAVE_BIO = "Failed to save bio"
  const val ERROR_UNEXPECTED = "An unexpected error occurred"
  const val ERROR_PROFILE_NOT_FOUND = "Profile not found"

  // Field labels - hardcoded for test compatibility
  const val LABEL_FIRST_NAME = "First Name"
  const val LABEL_LAST_NAME = "Last Name"
  const val LABEL_UNIVERSITY = "University"
  const val LABEL_COUNTRY = "Country"
  const val LABEL_BIRTHDAY = "Birthday"
  const val LABEL_ACTIVITIES = "Favourite Activities"
  const val LABEL_BIO = "More About Me"

  // Placeholders - hardcoded for test compatibility
  const val PLACEHOLDER_FIRST_NAME = "John"
  const val PLACEHOLDER_LAST_NAME = "Doe"
  const val PLACEHOLDER_BIO = "Tell us about yourself…"
  const val PLACEHOLDER_NOT_SPECIFIED = "Not specified"

  // Instructions - hardcoded for test compatibility
  const val INSTRUCTION_ENTER_NAME = "Enter your full name"
  const val INSTRUCTION_TELL_ABOUT_YOURSELF = "Tell us about yourself"
}
