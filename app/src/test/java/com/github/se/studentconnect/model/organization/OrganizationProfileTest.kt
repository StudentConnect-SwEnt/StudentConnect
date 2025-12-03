package com.github.se.studentconnect.model.organization

import org.junit.Assert.*
import org.junit.Test

class OrganizationProfileTest {

  @Test
  fun `OrganizationProfile with valid fields is created successfully`() {
    val organization =
        OrganizationProfile(
            organizationId = "org_1", name = "Test Org", description = "A test organization")

    assertEquals("org_1", organization.organizationId)
    assertEquals("Test Org", organization.name)
    assertEquals("A test organization", organization.description)
    assertNull(organization.logoUrl)
    assertFalse(organization.isFollowing)
    assertTrue(organization.events.isEmpty())
    assertTrue(organization.members.isEmpty())
  }

  @Test
  fun `OrganizationProfile with all optional fields is created successfully`() {
    val events = listOf(OrganizationEvent("e1", "Event 1", "Jan 1", "Title", "Subtitle"))
    val members = listOf(OrganizationMember("m1", "Member 1", "Owner"))

    val organization =
        OrganizationProfile(
            organizationId = "org_2",
            name = "Full Org",
            description = "Description",
            logoUrl = "https://example.com/logo.png",
            isFollowing = true,
            events = events,
            members = members)

    assertEquals("https://example.com/logo.png", organization.logoUrl)
    assertTrue(organization.isFollowing)
    assertEquals(1, organization.events.size)
    assertEquals(1, organization.members.size)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationProfile with blank id throws exception`() {
    OrganizationProfile(organizationId = "", name = "Test", description = "Desc")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationProfile with blank name throws exception`() {
    OrganizationProfile(organizationId = "org_1", name = "", description = "Desc")
  }

  @Test
  fun `OrganizationProfile with blank description is allowed`() {
    val organization =
        OrganizationProfile(organizationId = "org_1", name = "Test", description = "")
    assertEquals("", organization.description)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationProfile with name exceeding 200 characters throws exception`() {
    OrganizationProfile(
        organizationId = "org_1", name = "A".repeat(201), description = "Description")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationProfile with description exceeding 1000 characters throws exception`() {
    OrganizationProfile(organizationId = "org_1", name = "Test", description = "A".repeat(1001))
  }

  @Test
  fun `OrganizationProfile with maximum allowed name length succeeds`() {
    val organization =
        OrganizationProfile(
            organizationId = "org_1", name = "A".repeat(200), description = "Description")
    assertEquals(200, organization.name.length)
  }

  @Test
  fun `OrganizationProfile with maximum allowed description length succeeds`() {
    val organization =
        OrganizationProfile(organizationId = "org_1", name = "Test", description = "A".repeat(1000))
    assertEquals(1000, organization.description.length)
  }

  @Test
  fun `OrganizationProfile copy creates correct instance`() {
    val original =
        OrganizationProfile(
            organizationId = "org_1",
            name = "Original",
            description = "Description",
            isFollowing = false)
    val copied = original.copy(isFollowing = true)

    assertFalse(original.isFollowing)
    assertTrue(copied.isFollowing)
    assertEquals(original.organizationId, copied.organizationId)
  }

  @Test
  fun `OrganizationProfile equals compares all fields`() {
    val org1 = OrganizationProfile("id", "Name", "Desc")
    val org2 = OrganizationProfile("id", "Name", "Desc")
    val org3 = OrganizationProfile("id2", "Name", "Desc")

    assertEquals(org1, org2)
    assertNotEquals(org1, org3)
  }
}

class OrganizationEventTest {

  @Test
  fun `OrganizationEvent with valid fields is created successfully`() {
    val event =
        OrganizationEvent(
            eventId = "event_1",
            cardTitle = "Hackathon",
            cardDate = "Dec 15",
            title = "EPFL Hackathon",
            subtitle = "Tomorrow")

    assertEquals("event_1", event.eventId)
    assertEquals("Hackathon", event.cardTitle)
    assertEquals("Dec 15", event.cardDate)
    assertEquals("EPFL Hackathon", event.title)
    assertEquals("Tomorrow", event.subtitle)
    assertNull(event.location)
  }

  @Test
  fun `OrganizationEvent with location is created successfully`() {
    val event =
        OrganizationEvent(
            eventId = "event_1",
            cardTitle = "Event",
            cardDate = "Jan 1",
            title = "Title",
            subtitle = "Subtitle",
            location = "EPFL Campus")

    assertEquals("EPFL Campus", event.location)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationEvent with blank eventId throws exception`() {
    OrganizationEvent(
        eventId = "",
        cardTitle = "Title",
        cardDate = "Date",
        title = "Title",
        subtitle = "Subtitle")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationEvent with blank cardTitle throws exception`() {
    OrganizationEvent(
        eventId = "e1", cardTitle = "", cardDate = "Date", title = "Title", subtitle = "Subtitle")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationEvent with blank cardDate throws exception`() {
    OrganizationEvent(
        eventId = "e1", cardTitle = "Title", cardDate = "", title = "Title", subtitle = "Subtitle")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationEvent with blank title throws exception`() {
    OrganizationEvent(
        eventId = "e1", cardTitle = "Card", cardDate = "Date", title = "", subtitle = "Subtitle")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationEvent with blank subtitle throws exception`() {
    OrganizationEvent(
        eventId = "e1", cardTitle = "Card", cardDate = "Date", title = "Title", subtitle = "")
  }

  @Test
  fun `OrganizationEvent equals compares all fields`() {
    val event1 = OrganizationEvent("id", "Card", "Date", "Title", "Sub")
    val event2 = OrganizationEvent("id", "Card", "Date", "Title", "Sub")
    val event3 = OrganizationEvent("id2", "Card", "Date", "Title", "Sub")

    assertEquals(event1, event2)
    assertNotEquals(event1, event3)
  }
}

class OrganizationMemberTest {

  @Test
  fun `OrganizationMember with valid fields is created successfully`() {
    val member = OrganizationMember(memberId = "member_1", name = "John Doe", role = "Owner")

    assertEquals("member_1", member.memberId)
    assertEquals("John Doe", member.name)
    assertEquals("Owner", member.role)
    assertNull(member.avatarUrl)
  }

  @Test
  fun `OrganizationMember with avatarUrl is created successfully`() {
    val member =
        OrganizationMember(
            memberId = "member_1", name = "Jane", role = "Member", avatarUrl = "avatar_12")

    assertEquals("avatar_12", member.avatarUrl)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationMember with blank memberId throws exception`() {
    OrganizationMember(memberId = "", name = "Name", role = "Role")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationMember with blank name throws exception`() {
    OrganizationMember(memberId = "m1", name = "", role = "Role")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationMember with blank role throws exception`() {
    OrganizationMember(memberId = "m1", name = "Name", role = "")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationMember with name exceeding 100 characters throws exception`() {
    OrganizationMember(memberId = "m1", name = "A".repeat(101), role = "Role")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `OrganizationMember with role exceeding 50 characters throws exception`() {
    OrganizationMember(memberId = "m1", name = "Name", role = "A".repeat(51))
  }

  @Test
  fun `OrganizationMember with maximum allowed name length succeeds`() {
    val member = OrganizationMember(memberId = "m1", name = "A".repeat(100), role = "Role")
    assertEquals(100, member.name.length)
  }

  @Test
  fun `OrganizationMember with maximum allowed role length succeeds`() {
    val member = OrganizationMember(memberId = "m1", name = "Name", role = "A".repeat(50))
    assertEquals(50, member.role.length)
  }

  @Test
  fun `OrganizationMember equals compares all fields`() {
    val member1 = OrganizationMember("id", "Name", "Role")
    val member2 = OrganizationMember("id", "Name", "Role")
    val member3 = OrganizationMember("id2", "Name", "Role")

    assertEquals(member1, member2)
    assertNotEquals(member1, member3)
  }

  @Test
  fun `OrganizationMember copy creates correct instance`() {
    val original = OrganizationMember("m1", "Name", "Member")
    val copied = original.copy(role = "Owner")

    assertEquals("Member", original.role)
    assertEquals("Owner", copied.role)
  }
}
