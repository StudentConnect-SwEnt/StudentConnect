package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.organization.OrganizationModel

/**
 * Repository interface for Organization operations.
 *
 * This interface defines the contract for organization data operations, allowing for different
 * implementations (e.g., Firestore, local database, mock for testing).
 */
interface OrganizationRepository {
  /**
   * Creates or updates an organization in the database.
   *
   * @param organization The OrganizationModel to save.
   */
  suspend fun saveOrganization(organization: OrganizationModel)

  /**
   * Retrieves an organization by its unique identifier.
   *
   * @param organizationId The unique identifier of the organization.
   * @return The OrganizationModel, or null if not found.
   */
  suspend fun getOrganizationById(organizationId: String): OrganizationModel?

  /**
   * Returns a unique ID for a new organization document.
   */
  suspend fun getNewOrganizationId(): String
}

