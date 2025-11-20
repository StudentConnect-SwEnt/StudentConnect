package com.github.se.studentconnect.model.organization

/**
 * Represents the type of organization in the StudentConnect application.
 *
 * Used in the organization signup flow to categorize organizations.
 */
enum class OrganizationType {
  Association,
  StudentClub,
  Company,
  NGO,
  Other
}

/**
 * Represents a role within an organization.
 *
 * @property name The name of the role (e.g., "President", "Treasurer", "Event Coordinator").
 * @property description Optional description of the role's responsibilities.
 */
data class OrganizationRole(
    val name: String,
    val description: String? = null
) {
  init {
    require(name.isNotBlank()) { "Role name cannot be blank" }
    require(name.length <= MAX_NAME_LENGTH) {
      "Role name cannot exceed $MAX_NAME_LENGTH characters"
    }
    description?.let {
      require(it.length <= MAX_DESCRIPTION_LENGTH) {
        "Role description cannot exceed $MAX_DESCRIPTION_LENGTH characters"
      }
    }
  }

  companion object {
    const val MAX_NAME_LENGTH = 100
    const val MAX_DESCRIPTION_LENGTH = 500
  }
}

/**
 * Represents social media and web links for an organization.
 *
 * @property website Optional website URL.
 * @property instagram Optional Instagram profile URL.
 * @property x Optional X (Twitter) profile URL.
 * @property linkedin Optional LinkedIn profile URL.
 */
data class SocialLinks(
    val website: String? = null,
    val instagram: String? = null,
    val x: String? = null,
    val linkedin: String? = null
) {
  init {
    website?.let {
      require(it.isNotBlank()) { "Website URL cannot be blank" }
      require(isValidUrl(it)) { "Website URL must be valid" }
      require(it.length <= MAX_URL_LENGTH) {
        "Website URL cannot exceed $MAX_URL_LENGTH characters"
      }
    }
    instagram?.let {
      require(it.isNotBlank()) { "Instagram URL cannot be blank" }
      require(isValidUrl(it)) { "Instagram URL must be valid" }
      require(it.length <= MAX_URL_LENGTH) {
        "Instagram URL cannot exceed $MAX_URL_LENGTH characters"
      }
    }
    x?.let {
      require(it.isNotBlank()) { "X URL cannot be blank" }
      require(isValidUrl(it)) { "X URL must be valid" }
      require(it.length <= MAX_URL_LENGTH) {
        "X URL cannot exceed $MAX_URL_LENGTH characters"
      }
    }
    linkedin?.let {
      require(it.isNotBlank()) { "LinkedIn URL cannot be blank" }
      require(isValidUrl(it)) { "LinkedIn URL must be valid" }
      require(it.length <= MAX_URL_LENGTH) {
        "LinkedIn URL cannot exceed $MAX_URL_LENGTH characters"
      }
    }
  }

  companion object {
    const val MAX_URL_LENGTH = 500

    private fun isValidUrl(url: String): Boolean {
      return try {
        val uri = java.net.URI(url)
        uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https")
      } catch (e: Exception) {
        false
      }
    }
  }
}

