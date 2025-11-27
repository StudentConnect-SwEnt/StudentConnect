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
}
