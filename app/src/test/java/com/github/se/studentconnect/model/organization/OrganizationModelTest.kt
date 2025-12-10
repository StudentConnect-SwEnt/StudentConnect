package com.github.se.studentconnect.model.organization

import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test

class OrganizationModelTest {

  @Test
  fun organizationType_fromString_known_and_unknown() {
    assertEquals(OrganizationType.Association, OrganizationType.fromString("Association"))
    assertEquals(OrganizationType.StudentClub, OrganizationType.fromString("studentclub"))
    assertEquals(OrganizationType.Other, OrganizationType.fromString(null))
    assertEquals(OrganizationType.Other, OrganizationType.fromString("unknown-type"))
  }

  @Test
  fun socialLinks_toMap_contains_expected_keys() {
    val s = SocialLinks(website = "w", instagram = "i", x = "xv", linkedin = "li")
    val map = s.toMap()
    assertEquals("w", map["website"])
    assertEquals("i", map["instagram"])
    assertEquals("xv", map["x"])
    assertEquals("li", map["linkedin"])
  }

  @Test
  fun toMap_and_fromMap_roundtrip() {
    val ts = Timestamp.now()
    val role = OrganizationRole("Dev", "Writes code")

    val model =
        Organization(
            id = "o1",
            name = "Org1",
            type = OrganizationType.Company,
            description = "desc",
            logoUrl = "u",
            location = "here",
            mainDomains = listOf("A"),
            ageRanges = listOf("18-25"),
            typicalEventSize = "S",
            roles = listOf(role),
            socialLinks = SocialLinks(website = "w"),
            createdAt = ts,
            createdBy = "creator")

    val map = model.toMap()
    // basic invariants
    assertEquals("o1", map["id"])
    assertEquals("Org1", map["name"])
    assertEquals("Company", map["type"])
    assertEquals(ts, map["createdAt"])

    val from = Organization.fromMap(map)
    assertNotNull(from)
    assertEquals(model.id, from?.id)
    assertEquals(model.name, from?.name)
    assertEquals(model.type, from?.type)
    assertEquals(model.description, from?.description)
    assertEquals(model.logoUrl, from?.logoUrl)
    assertEquals(model.location, from?.location)
    assertEquals(model.mainDomains, from?.mainDomains)
    assertEquals(model.ageRanges, from?.ageRanges)
    assertEquals(model.typicalEventSize, from?.typicalEventSize)
    assertEquals(model.roles, from?.roles)
    assertEquals(model.socialLinks.website, from?.socialLinks?.website)
    assertEquals(model.createdBy, from?.createdBy)
    assertEquals(ts, from?.createdAt)
  }

  @Test
  fun fromMap_returns_null_when_missing_critical_fields() {
    val base = mutableMapOf<String, Any?>()
    base["id"] = "x"
    base["name"] = "n"
    base["createdBy"] = "c"

    // ok when present
    assertNotNull(Organization.fromMap(base))

    // missing id
    val m1 = base.toMutableMap()
    m1.remove("id")
    assertNull(Organization.fromMap(m1))

    // missing name
    val m2 = base.toMutableMap()
    m2.remove("name")
    assertNull(Organization.fromMap(m2))

    // missing createdBy
    val m3 = base.toMutableMap()
    m3.remove("createdBy")
    assertNull(Organization.fromMap(m3))
  }

  @Test
  fun toMap_includes_memberUids_and_memberRoles() {
    val model =
        Organization(
            id = "o1",
            name = "Org1",
            type = OrganizationType.Company,
            memberUids = listOf("user1", "user2", "user3"),
            memberRoles = mapOf("user1" to "Owner", "user2" to "Member", "user3" to "Admin"),
            createdBy = "creator")

    val map = model.toMap()

    assertEquals(listOf("user1", "user2", "user3"), map["memberUids"])
    assertEquals(
        mapOf("user1" to "Owner", "user2" to "Member", "user3" to "Admin"), map["memberRoles"])
  }

  @Test
  fun fromMap_parses_memberUids_correctly() {
    val map =
        mapOf<String, Any?>(
            "id" to "o1",
            "name" to "Org1",
            "type" to "Company",
            "createdBy" to "creator",
            "memberUids" to listOf("user1", "user2", "user3"))

    val org = Organization.fromMap(map)

    assertNotNull(org)
    assertEquals(3, org?.memberUids?.size)
    assertTrue(org?.memberUids?.contains("user1") == true)
    assertTrue(org?.memberUids?.contains("user2") == true)
    assertTrue(org?.memberUids?.contains("user3") == true)
  }

  @Test
  fun fromMap_parses_empty_memberUids_as_empty_list() {
    val map =
        mapOf<String, Any?>(
            "id" to "o1",
            "name" to "Org1",
            "type" to "Company",
            "createdBy" to "creator",
            "memberUids" to emptyList<String>())

    val org = Organization.fromMap(map)

    assertNotNull(org)
    assertTrue(org?.memberUids?.isEmpty() == true)
  }

  @Test
  fun fromMap_parses_missing_memberUids_as_empty_list() {
    val map =
        mapOf<String, Any?>(
            "id" to "o1", "name" to "Org1", "type" to "Company", "createdBy" to "creator")

    val org = Organization.fromMap(map)

    assertNotNull(org)
    assertTrue(org?.memberUids?.isEmpty() == true)
  }

  @Test
  fun fromMap_parses_memberRoles_correctly() {
    val map =
        mapOf<String, Any?>(
            "id" to "o1",
            "name" to "Org1",
            "type" to "Company",
            "createdBy" to "creator",
            "memberRoles" to mapOf("user1" to "Owner", "user2" to "Member", "user3" to "Admin"))

    val org = Organization.fromMap(map)

    assertNotNull(org)
    assertEquals(3, org?.memberRoles?.size)
    assertEquals("Owner", org?.memberRoles?.get("user1"))
    assertEquals("Member", org?.memberRoles?.get("user2"))
    assertEquals("Admin", org?.memberRoles?.get("user3"))
  }

  @Test
  fun fromMap_parses_empty_memberRoles_as_empty_map() {
    val map =
        mapOf<String, Any?>(
            "id" to "o1",
            "name" to "Org1",
            "type" to "Company",
            "createdBy" to "creator",
            "memberRoles" to emptyMap<String, String>())

    val org = Organization.fromMap(map)

    assertNotNull(org)
    assertTrue(org?.memberRoles?.isEmpty() == true)
  }

  @Test
  fun fromMap_parses_missing_memberRoles_as_empty_map() {
    val map =
        mapOf<String, Any?>(
            "id" to "o1", "name" to "Org1", "type" to "Company", "createdBy" to "creator")

    val org = Organization.fromMap(map)

    assertNotNull(org)
    assertTrue(org?.memberRoles?.isEmpty() == true)
  }

  @Test
  fun fromMap_filters_invalid_memberRoles_keys() {
    val map =
        mapOf<String, Any?>(
            "id" to "o1",
            "name" to "Org1",
            "type" to "Company",
            "createdBy" to "creator",
            "memberRoles" to mapOf("user1" to "Owner", "" to "Invalid", 123 to "Invalid"))

    val org = Organization.fromMap(map)

    assertNotNull(org)
    assertEquals(1, org?.memberRoles?.size)
    assertEquals("Owner", org?.memberRoles?.get("user1"))
    assertNull(org?.memberRoles?.get(""))
  }

  @Test
  fun fromMap_handles_memberRoles_with_non_string_values() {
    val map =
        mapOf<String, Any?>(
            "id" to "o1",
            "name" to "Org1",
            "type" to "Company",
            "createdBy" to "creator",
            "memberRoles" to mapOf("user1" to "Owner", "user2" to 123, "user3" to null))

    val org = Organization.fromMap(map)

    assertNotNull(org)
    assertEquals("Owner", org?.memberRoles?.get("user1"))
    assertEquals("", org?.memberRoles?.get("user2")) // Non-string converted to empty string
    assertEquals("", org?.memberRoles?.get("user3")) // Null converted to empty string
  }

  @Test
  fun fromMap_handles_memberUids_with_non_string_values() {
    val map =
        mapOf<String, Any?>(
            "id" to "o1",
            "name" to "Org1",
            "type" to "Company",
            "createdBy" to "creator",
            "memberUids" to listOf("user1", 123, null, "user2"))

    val org = Organization.fromMap(map)

    assertNotNull(org)
    assertEquals(2, org?.memberUids?.size) // Only strings are kept
    assertTrue(org?.memberUids?.contains("user1") == true)
    assertTrue(org?.memberUids?.contains("user2") == true)
  }

  @Test
  fun toMap_and_fromMap_roundtrip_with_memberUids_and_memberRoles() {
    val model =
        Organization(
            id = "o1",
            name = "Org1",
            type = OrganizationType.Company,
            memberUids = listOf("user1", "user2"),
            memberRoles = mapOf("user1" to "Owner", "user2" to "Member"),
            createdBy = "creator")

    val map = model.toMap()
    val from = Organization.fromMap(map)

    assertNotNull(from)
    assertEquals(model.memberUids, from?.memberUids)
    assertEquals(model.memberRoles, from?.memberRoles)
  }
}
