package com.github.se.studentconnect.model

import android.content.Context
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OrganizationMappersTest {

  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var mockContext: Context

  private val testOrganization =
      Organization(
          id = "org123",
          name = "Test Organization",
          type = com.github.se.studentconnect.model.organization.OrganizationType.Association,
          description = "A test organization description",
          logoUrl = "https://example.com/logo.png",
          memberUids = listOf("user1", "user2"),
          createdBy = "creator1")

  private val testUser =
      User(
          userId = "user1",
          email = "test@example.com",
          username = "testuser",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL",
          profilePictureUrl = "https://example.com/avatar.jpg",
          createdAt = 1000L,
          updatedAt = 1000L)

  private val testEvent =
      Event.Public(
          uid = "event1",
          title = "Test Event",
          description = "Test Description",
          ownerId = "org123",
          start = Timestamp(Date()),
          end = Timestamp(Date(System.currentTimeMillis() + 3600000)),
          location = Location(46.5197, 6.6323, "EPFL"),
          participationFee = 0u,
          isFlash = false,
          subtitle = "Test Subtitle",
          tags = listOf("Sports"))

  @Before
  fun setUp() {
    userRepository = UserRepositoryLocal()

    // Mock Context for string resources
    mockContext = mockk(relaxed = true)
    every { mockContext.getString(R.string.org_event_time_today) } returns "Today"
    every { mockContext.getString(R.string.org_event_time_tomorrow) } returns "Tomorrow"
    every { mockContext.getString(R.string.org_event_time_in_days, any()) } answers
        {
          val days = arg<Int>(0)
          "In $days days"
        }
  }

  @Test
  fun `toOrganizationProfile converts organization with default parameters`() {
    val profile = testOrganization.toOrganizationProfile()

    assertEquals("org123", profile.organizationId)
    assertEquals("Test Organization", profile.name)
    assertEquals("A test organization description", profile.description)
    assertEquals("https://example.com/logo.png", profile.logoUrl)
    assertFalse(profile.isFollowing)
    assertTrue(profile.events.isEmpty())
    assertTrue(profile.members.isEmpty())
  }

  @Test
  fun `toOrganizationProfile converts organization with isFollowing true`() {
    val profile = testOrganization.toOrganizationProfile(isFollowing = true)

    assertTrue(profile.isFollowing)
  }

  @Test
  fun `toOrganizationProfile converts organization with events`() {
    val events = listOf(testEvent.toOrganizationEvent(mockContext))
    val profile = testOrganization.toOrganizationProfile(events = events)

    assertEquals(1, profile.events.size)
    assertEquals("Test Event", profile.events[0].title)
  }

  @Test
  fun `toOrganizationProfile converts organization with members`() {
    val members = listOf(testUser.toOrganizationMember())
    val profile = testOrganization.toOrganizationProfile(members = members)

    assertEquals(1, profile.members.size)
    assertEquals("John Doe", profile.members[0].name)
  }

  @Test
  fun `toOrganizationProfile converts null description to empty string`() {
    val orgWithNullDescription = testOrganization.copy(description = null)
    val profile = orgWithNullDescription.toOrganizationProfile()
    // Test that null description is converted to empty string
    assertEquals("", profile.description)
  }

  @Test
  fun `toOrganizationProfile preserves non-null description`() {
    val profile = testOrganization.toOrganizationProfile()
    // Test that when organization has description it's preserved
    assertEquals("A test organization description", profile.description)
  }

  @Test
  fun `toOrganizationProfile handles null logoUrl`() {
    val orgWithoutLogo = testOrganization.copy(logoUrl = null)
    val profile = orgWithoutLogo.toOrganizationProfile()

    assertNull(profile.logoUrl)
  }

  @Test
  fun `toOrganizationData generates correct handle from name`() {
    val organizationData = testOrganization.toOrganizationData()

    assertEquals("org123", organizationData.id)
    assertEquals("Test Organization", organizationData.name)
    // Handle is sanitized: spaces replaced with underscores, lowercase
    assertEquals("@test_organization", organizationData.handle)
  }

  @Test
  fun `toOrganizationData handles organization name with spaces`() {
    val org = testOrganization.copy(name = "My Test Organization")
    val organizationData = org.toOrganizationData()

    // Spaces are replaced with underscores
    assertEquals("@my_test_organization", organizationData.handle)
  }

  @Test
  fun `toOrganizationData handles organization name with special characters`() {
    val org = testOrganization.copy(name = "EPFL - Test")
    val organizationData = org.toOrganizationData()

    // Special characters are removed, spaces become underscores
    assertEquals("@epfl_test", organizationData.handle)
  }

  @Test
  fun `toOrganizationDataList converts multiple organizations`() {
    val org2 = testOrganization.copy(id = "org456", name = "Another Org")
    val organizations = listOf(testOrganization, org2)

    val dataList = organizations.toOrganizationDataList()

    assertEquals(2, dataList.size)
    assertEquals("org123", dataList[0].id)
    assertEquals("org456", dataList[1].id)
    // Handles are sanitized
    assertEquals("@test_organization", dataList[0].handle)
    assertEquals("@another_org", dataList[1].handle)
  }

  @Test
  fun `toOrganizationDataList handles empty list`() {
    val dataList = emptyList<Organization>().toOrganizationDataList()

    assertTrue(dataList.isEmpty())
  }

  @Test
  fun `toOrganizationMember converts user with default role`() {
    val member = testUser.toOrganizationMember()

    assertEquals("user1", member.memberId)
    assertEquals("John Doe", member.name)
    assertEquals("Member", member.role)
    assertEquals("https://example.com/avatar.jpg", member.avatarUrl)
  }

  @Test
  fun `toOrganizationMember converts user with custom role`() {
    val member = testUser.toOrganizationMember(role = "Admin")

    assertEquals("Admin", member.role)
  }

  @Test
  fun `toOrganizationMember handles user without profile picture`() {
    val userWithoutPic = testUser.copy(profilePictureUrl = null)
    val member = userWithoutPic.toOrganizationMember()

    assertNull(member.avatarUrl)
  }

  @Test
  fun `toOrganizationMember concatenates first and last name`() {
    val user = testUser.copy(firstName = "Jane", lastName = "Smith")
    val member = user.toOrganizationMember()

    assertEquals("Jane Smith", member.name)
  }

  @Test
  fun `fetchOrganizationMembers retrieves all members`() = runTest {
    userRepository.saveUser(testUser)
    val user2 = testUser.copy(userId = "user2", firstName = "Jane", lastName = "Smith")
    userRepository.saveUser(user2)

    val members =
        fetchOrganizationMembers(listOf("user1", "user2"), userRepository, defaultRole = "Member")

    assertEquals(2, members.size)
    assertEquals("John Doe", members[0].name)
    assertEquals("Jane Smith", members[1].name)
  }

  @Test
  fun `fetchOrganizationMembers handles non-existent users`() = runTest {
    userRepository.saveUser(testUser)

    val members =
        fetchOrganizationMembers(
            listOf("user1", "nonexistent"), userRepository, defaultRole = "Member")

    assertEquals(1, members.size)
    assertEquals("John Doe", members[0].name)
  }

  @Test
  fun `fetchOrganizationMembers returns empty list for empty input`() = runTest {
    val members = fetchOrganizationMembers(emptyList(), userRepository, defaultRole = "Member")

    assertTrue(members.isEmpty())
  }

  @Test
  fun `fetchOrganizationMembers applies custom role`() = runTest {
    userRepository.saveUser(testUser)

    val members =
        fetchOrganizationMembers(listOf("user1"), userRepository, defaultRole = "Moderator")

    assertEquals(1, members.size)
    assertEquals("Moderator", members[0].role)
  }

  @Test
  fun `toOrganizationEvent converts event with today date`() {
    val today = Date()
    val eventToday = testEvent.copy(start = Timestamp(today))

    val orgEvent = eventToday.toOrganizationEvent(mockContext)

    assertEquals("event1", orgEvent.eventId)
    assertEquals("Test Event", orgEvent.cardTitle)
    assertEquals("Test Event", orgEvent.title)
    assertEquals("Today", orgEvent.subtitle)
    assertEquals("EPFL", orgEvent.location)
  }

  @Test
  fun `toOrganizationEvent converts event with tomorrow date`() {
    val tomorrow =
        Calendar.getInstance()
            .apply {
              add(Calendar.DAY_OF_MONTH, 1)
              // Set to noon to avoid edge cases with time
              set(Calendar.HOUR_OF_DAY, 12)
              set(Calendar.MINUTE, 0)
              set(Calendar.SECOND, 0)
              set(Calendar.MILLISECOND, 0)
            }
            .time
    val eventTomorrow = testEvent.copy(start = Timestamp(tomorrow))

    val orgEvent = eventTomorrow.toOrganizationEvent(mockContext)

    assertEquals("Tomorrow", orgEvent.subtitle)
  }

  @Test
  fun `toOrganizationEvent handles event without location`() {
    val eventWithoutLocation = testEvent.copy(location = null)

    val orgEvent = eventWithoutLocation.toOrganizationEvent(mockContext)

    assertNull(orgEvent.location)
  }

  @Test
  fun `toOrganizationEvent formats card date correctly`() {
    val calendar = Calendar.getInstance()
    calendar.set(2025, Calendar.MARCH, 15)
    val eventDate = testEvent.copy(start = Timestamp(calendar.time))

    val orgEvent = eventDate.toOrganizationEvent(mockContext)

    assertEquals("15 Mar, 2025", orgEvent.cardDate)
  }

  @Test
  fun `toOrganizationEvent handles past event`() {
    val pastDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -5) }.time
    val eventPast = testEvent.copy(start = Timestamp(pastDate))

    val orgEvent = eventPast.toOrganizationEvent(mockContext)

    assertNotEquals("Today", orgEvent.subtitle)
    assertNotEquals("Tomorrow", orgEvent.subtitle)
    assertFalse(orgEvent.subtitle.startsWith("In"))
  }

  @Test
  fun `toOrganizationEvents converts multiple events`() {
    val event2 =
        testEvent.copy(
            uid = "event2",
            title = "Second Event",
            start = Timestamp(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time))

    val events = listOf(testEvent, event2)
    val orgEvents = events.toOrganizationEvents(mockContext)

    assertEquals(2, orgEvents.size)
    assertEquals("Test Event", orgEvents[0].title)
    assertEquals("Second Event", orgEvents[1].title)
  }

  @Test
  fun `toOrganizationEvents handles empty list`() {
    val orgEvents = emptyList<Event>().toOrganizationEvents(mockContext)

    assertTrue(orgEvents.isEmpty())
  }

  @Test
  fun `toOrganizationEvent preserves all event properties`() {
    val orgEvent = testEvent.toOrganizationEvent(mockContext)

    assertEquals(testEvent.uid, orgEvent.eventId)
    assertEquals(testEvent.title, orgEvent.cardTitle)
    assertEquals(testEvent.title, orgEvent.title)
    assertEquals(testEvent.location?.name, orgEvent.location)
  }
}
