package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.organization.Organization
import java.util.UUID

/**
 * Represents a repository that manages a local list of organizations. This class is intended for
 * testing and development purposes.
 */
class OrganizationRepositoryLocal : OrganizationRepository {
  private val organizations = mutableListOf<Organization>()

  override suspend fun saveOrganization(organization: Organization) {
    val index = organizations.indexOfFirst { it.id == organization.id }
    if (index != -1) {
      organizations[index] = organization
    } else {
      organizations.add(organization)
    }
  }

  override suspend fun getOrganizationById(organizationId: String): Organization? {
    return organizations.find { it.id == organizationId }
  }

  override suspend fun getNewOrganizationId(): String {
    return UUID.randomUUID().toString()
  }

  suspend fun getAllOrganizations(): List<Organization> {
    return organizations.toList()
  }
}
