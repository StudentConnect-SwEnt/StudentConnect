package com.github.se.studentconnect.model.organization

/**
 * Local in-memory implementation of OrganizationRepository for testing.
 *
 * This implementation stores organizations in a simple in-memory map and is intended for use in
 * unit tests and local development.
 */
class OrganizationRepositoryLocal : OrganizationRepository {
  private val organizations = mutableMapOf<String, Organization>()
  private var idCounter = 0

  override suspend fun saveOrganization(organization: Organization) {
    organizations[organization.id] = organization
  }

  override suspend fun getOrganizationById(organizationId: String): Organization? {
    return organizations[organizationId]
  }

  override suspend fun getAllOrganizations(): List<Organization> {
    return organizations.values.toList()
  }

  override suspend fun getNewOrganizationId(): String {
    return "org_local_${idCounter++}"
  }

  /** Clears all organizations from the local repository. Useful for test setup/teardown. */
  fun clear() {
    organizations.clear()
    idCounter = 0
  }
}
