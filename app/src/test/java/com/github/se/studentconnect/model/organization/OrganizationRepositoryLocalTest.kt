package com.github.se.studentconnect.model.organization

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OrganizationRepositoryLocalTest {

  private lateinit var repository: OrganizationRepositoryLocal

  private val testOrganization1 =
      Organization(
          id = "org1",
          name = "Test Organization 1",
          type = OrganizationType.Association,
          description = "A test organization",
          logoUrl = "https://example.com/logo1.png",
          memberUids = listOf("user1", "user2"),
          createdBy = "creator1")

  private val testOrganization2 =
      Organization(
          id = "org2",
          name = "Test Organization 2",
          type = OrganizationType.Company,
          description = "Another test organization",
          logoUrl = "https://example.com/logo2.png",
          memberUids = listOf("user3", "user4"),
          createdBy = "creator2")

  @Before
  fun setup() {
    repository = OrganizationRepositoryLocal()
  }

  @Test
  fun `getOrganizationById returns null when organization does not exist`() = runTest {
    val result = repository.getOrganizationById("non-existent")
    assertNull(result)
  }

  @Test
  fun `saveOrganization and getOrganizationById returns organization`() = runTest {
    repository.saveOrganization(testOrganization1)
    val result = repository.getOrganizationById("org1")

    assertNotNull(result)
    assertEquals("org1", result?.id)
    assertEquals("Test Organization 1", result?.name)
    assertEquals(OrganizationType.Association, result?.type)
    assertEquals("A test organization", result?.description)
    assertEquals("https://example.com/logo1.png", result?.logoUrl)
    assertEquals(2, result?.memberUids?.size)
    assertEquals("creator1", result?.createdBy)
  }

  @Test
  fun `getAllOrganizations returns empty list initially`() = runTest {
    val result = repository.getAllOrganizations()
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getAllOrganizations returns all saved organizations`() = runTest {
    repository.saveOrganization(testOrganization1)
    repository.saveOrganization(testOrganization2)

    val result = repository.getAllOrganizations()
    assertEquals(2, result.size)
    assertTrue(result.any { it.id == "org1" })
    assertTrue(result.any { it.id == "org2" })
  }

  @Test
  fun `saveOrganization replaces existing organization with same id`() = runTest {
    repository.saveOrganization(testOrganization1)

    val updatedOrg = testOrganization1.copy(name = "Updated Organization")
    repository.saveOrganization(updatedOrg)

    val result = repository.getOrganizationById("org1")
    assertEquals("Updated Organization", result?.name)

    val allOrgs = repository.getAllOrganizations()
    assertEquals(1, allOrgs.size)
  }

  @Test
  fun `getNewOrganizationId returns unique ids`() = runTest {
    val id1 = repository.getNewOrganizationId()
    val id2 = repository.getNewOrganizationId()

    assertNotNull(id1)
    assertNotNull(id2)
    assertTrue(id1 != id2)
    assertTrue(id1.startsWith("org_local_"))
    assertTrue(id2.startsWith("org_local_"))
  }

  @Test
  fun `getNewOrganizationId generates sequential ids`() = runTest {
    val id1 = repository.getNewOrganizationId()
    val id2 = repository.getNewOrganizationId()
    val id3 = repository.getNewOrganizationId()

    assertEquals("org_local_0", id1)
    assertEquals("org_local_1", id2)
    assertEquals("org_local_2", id3)
  }

  @Test
  fun `clear removes all organizations`() = runTest {
    repository.saveOrganization(testOrganization1)
    repository.saveOrganization(testOrganization2)

    repository.clear()

    val result = repository.getAllOrganizations()
    assertTrue(result.isEmpty())

    val org1 = repository.getOrganizationById("org1")
    assertNull(org1)
  }

  @Test
  fun `clear resets id counter`() = runTest {
    val id1 = repository.getNewOrganizationId()
    assertEquals("org_local_0", id1)

    repository.clear()

    val id2 = repository.getNewOrganizationId()
    assertEquals("org_local_0", id2) // Counter should be reset
  }

  @Test
  fun `saveOrganization handles organization with null fields`() = runTest {
    val minimalOrg =
        Organization(
            id = "org_minimal",
            name = "Minimal Org",
            type = OrganizationType.NGO,
            description = null,
            logoUrl = null,
            memberUids = emptyList(),
            createdBy = "creator")

    repository.saveOrganization(minimalOrg)
    val result = repository.getOrganizationById("org_minimal")

    assertNotNull(result)
    assertEquals("Minimal Org", result?.name)
    assertNull(result?.description)
    assertNull(result?.logoUrl)
    assertTrue(result?.memberUids?.isEmpty() == true)
  }

  @Test
  fun `saveOrganization handles organization with all optional fields`() = runTest {
    val fullOrg =
        Organization(
            id = "org_full",
            name = "Full Organization",
            type = OrganizationType.Company,
            description = "Full description",
            logoUrl = "https://example.com/logo.png",
            location = "Lausanne",
            memberUids = listOf("user1", "user2", "user3"),
            mainDomains = listOf("Technology", "Education"),
            ageRanges = listOf("18-25", "26-35"),
            typicalEventSize = "Large",
            roles = listOf(),
            socialLinks = SocialLinks(website = "https://example.com"),
            createdBy = "creator")

    repository.saveOrganization(fullOrg)
    val result = repository.getOrganizationById("org_full")

    assertNotNull(result)
    assertEquals("Full Organization", result?.name)
    assertEquals("Full description", result?.description)
    assertEquals("Lausanne", result?.location)
    assertEquals(3, result?.memberUids?.size)
    assertEquals(2, result?.mainDomains?.size)
  }

  @Test
  fun `getAllOrganizations returns organizations in insertion order`() = runTest {
    repository.saveOrganization(testOrganization1)
    repository.saveOrganization(testOrganization2)

    val result = repository.getAllOrganizations()
    assertEquals(2, result.size)
    // Map maintains insertion order
    assertEquals("org1", result[0].id)
    assertEquals("org2", result[1].id)
  }

  @Test
  fun `multiple operations work correctly together`() = runTest {
    // Save organizations
    repository.saveOrganization(testOrganization1)
    repository.saveOrganization(testOrganization2)

    // Get all
    var all = repository.getAllOrganizations()
    assertEquals(2, all.size)

    // Update one
    val updatedOrg = testOrganization1.copy(name = "Updated")
    repository.saveOrganization(updatedOrg)

    // Verify update
    val org = repository.getOrganizationById("org1")
    assertEquals("Updated", org?.name)

    // Still only 2 organizations
    all = repository.getAllOrganizations()
    assertEquals(2, all.size)

    // Generate new ID
    val newId = repository.getNewOrganizationId()
    assertTrue(newId.isNotEmpty())

    // Save new organization with generated ID
    val newOrg = testOrganization1.copy(id = newId, name = "New Org")
    repository.saveOrganization(newOrg)

    // Should now have 3
    all = repository.getAllOrganizations()
    assertEquals(3, all.size)

    // Clear all
    repository.clear()

    // Should be empty
    all = repository.getAllOrganizations()
    assertTrue(all.isEmpty())
  }

  @Test
  fun `saveOrganization preserves all organization types`() = runTest {
    val association = testOrganization1.copy(id = "org_assoc", type = OrganizationType.Association)
    val company = testOrganization1.copy(id = "org_company", type = OrganizationType.Company)
    val ngo = testOrganization1.copy(id = "org_ngo", type = OrganizationType.NGO)
    val club = testOrganization1.copy(id = "org_club", type = OrganizationType.StudentClub)

    repository.saveOrganization(association)
    repository.saveOrganization(company)
    repository.saveOrganization(ngo)
    repository.saveOrganization(club)

    assertEquals(OrganizationType.Association, repository.getOrganizationById("org_assoc")?.type)
    assertEquals(OrganizationType.Company, repository.getOrganizationById("org_company")?.type)
    assertEquals(OrganizationType.NGO, repository.getOrganizationById("org_ngo")?.type)
    assertEquals(OrganizationType.StudentClub, repository.getOrganizationById("org_club")?.type)
  }
}
