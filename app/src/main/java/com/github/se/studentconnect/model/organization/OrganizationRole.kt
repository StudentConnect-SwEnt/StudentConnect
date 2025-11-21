package com.github.se.studentconnect.model.organization

/**
 * Represents a role within an organization.
 *
 * This is a temporary implementation that will be replaced when OrganizationModel.kt
 * is merged from the next PR. This allows TeamRolesScreen to be prepared for ViewModel
 * integration while maintaining compilation.
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

