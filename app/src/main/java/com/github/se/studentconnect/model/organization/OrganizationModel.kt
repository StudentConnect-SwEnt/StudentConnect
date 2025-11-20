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

