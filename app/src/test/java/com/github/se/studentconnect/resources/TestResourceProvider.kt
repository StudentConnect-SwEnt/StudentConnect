package com.github.se.studentconnect.resources

import com.github.se.studentconnect.R

/**
 * Test implementation that returns actual string values from strings.xml. This allows tests to
 * verify the correct error/success messages are used.
 */
class TestResourceProvider {
  private val stringMap =
      mapOf(
          // Error messages
          R.string.error_network to "Network error",
          R.string.error_unexpected to "An unexpected error occurred",
          R.string.error_user_not_found to "User not found",
          R.string.error_profile_not_found to "Profile not found",
          R.string.error_failed_to_load_profile to "Failed to load profile",
          R.string.error_failed_to_save_name to "Failed to save name",
          R.string.error_failed_to_save_bio to "Failed to save bio",
          R.string.error_failed_to_update_nationality to "Failed to update nationality",
          R.string.error_failed_to_save_activities to "Failed to save activities",
          R.string.error_failed_to_load_activities to "Failed to load activities",
          R.string.error_first_name_empty to "First name cannot be empty",
          R.string.error_last_name_empty to "Last name cannot be empty",
          R.string.error_name_empty to "First name and last name cannot be empty",
          R.string.error_university_empty to "University cannot be empty",
          R.string.error_bio_empty to "Bio cannot be empty",
          R.string.error_bio_too_long to "Bio exceeds %d characters",
          R.string.error_date_format to "Please use DD/MM/YYYY format",
          R.string.error_failed_to_upload_photo to "Failed to upload photo",
          R.string.error_username_length to "Username must be 3-20 characters long",
          R.string.error_username_format to
              "Only alphanumeric characters, underscores, hyphens, and periods are allowed",
          R.string.error_username_taken to "This username is already taken",
          // Success messages
          R.string.success_name_updated to "Name updated successfully",
          R.string.success_university_updated to "University updated successfully",
          R.string.success_country_updated to "Country updated successfully",
          R.string.success_nationality_updated to "Nationality updated successfully",
          R.string.success_birthday_updated to "Birthday updated successfully",
          R.string.success_activities_updated to "Activities updated successfully",
          R.string.success_bio_updated to "Bio updated successfully",
          R.string.success_profile_updated to "Profile updated successfully",
          R.string.success_profile_picture_updated to "Profile picture updated successfully",
      )

  fun getString(resId: Int): String {
    return stringMap[resId] ?: "Unknown string resource: $resId"
  }

  fun getString(resId: Int, vararg formatArgs: Any): String {
    val template = stringMap[resId] ?: "Unknown string resource: $resId"
    return try {
      String.format(template, *formatArgs)
    } catch (e: Exception) {
      template
    }
  }
}
