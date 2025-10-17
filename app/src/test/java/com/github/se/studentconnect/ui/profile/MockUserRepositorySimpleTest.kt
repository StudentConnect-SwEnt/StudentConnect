package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockUserRepositorySimpleTest {

  private lateinit var repository: MockUserRepository
  private val defaultUser =
      User(
          userId = "mock_user_123",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@epfl.ch",
          university = "EPFL",
          country = "Switzerland",
          birthday = "15/03/1998",
          hobbies = listOf("Football", "Photography", "Cooking", "Reading"),
          bio =
              "I'm a computer science student at EPFL. I love technology, sports, and meeting new people!",
          profilePictureUrl = null)

  @Before
  fun setUp() {
    repository = MockUserRepository()
  }

  @Test
  fun `getUserById returns user if exists`() = runTest {
    val user = repository.getUserById(defaultUser.userId)
    assertEquals(defaultUser.userId, user?.userId)
    assertEquals(defaultUser.firstName, user?.firstName)
    assertEquals(defaultUser.lastName, user?.lastName)
    assertEquals(defaultUser.email, user?.email)
    assertEquals(defaultUser.university, user?.university)
    assertEquals(defaultUser.country, user?.country)
    assertEquals(defaultUser.birthday, user?.birthday)
    assertEquals(defaultUser.hobbies, user?.hobbies)
    assertEquals(defaultUser.bio, user?.bio)
    assertEquals(defaultUser.profilePictureUrl, user?.profilePictureUrl)
  }

  @Test
  fun `getUserById returns null if user does not exist`() = runTest {
    val user = repository.getUserById("non_existent_id")
    assertNull(user)
  }

  @Test
  fun `updateUser updates existing user fields`() = runTest {
    val updates =
        mapOf(
            "firstName" to "Jane",
            "lastName" to "Smith",
            "university" to "ETHZ",
            "country" to "Germany",
            "birthday" to "20/05/1995",
            "bio" to "Updated bio content",
            "profilePictureUrl" to "http://example.com/new-pic.jpg")
    repository.updateUser(defaultUser.userId, updates)
    val updatedUser = repository.getUserById(defaultUser.userId)

    assertEquals("Jane", updatedUser?.firstName)
    assertEquals("Smith", updatedUser?.lastName)
    assertEquals("ETHZ", updatedUser?.university)
    assertEquals("Germany", updatedUser?.country)
    assertEquals("20/05/1995", updatedUser?.birthday)
    assertEquals("Updated bio content", updatedUser?.bio)
    assertEquals("http://example.com/new-pic.jpg", updatedUser?.profilePictureUrl)
  }

  @Test
  fun `updateUser handles null values`() = runTest {
    val updates = mapOf("country" to null, "bio" to null, "profilePictureUrl" to null)
    repository.updateUser(defaultUser.userId, updates)
    val updatedUser = repository.getUserById(defaultUser.userId)

    // MockUserRepository doesn't actually set null values, it keeps the original values
    assertEquals(defaultUser.country, updatedUser?.country)
    assertEquals(defaultUser.bio, updatedUser?.bio)
    assertEquals(defaultUser.profilePictureUrl, updatedUser?.profilePictureUrl)
  }

  @Test
  fun `updateUser handles hobbies list correctly`() = runTest {
    val newHobbies = listOf("Gaming", "Reading", "Swimming")
    val updates = mapOf("hobbies" to newHobbies)
    repository.updateUser(defaultUser.userId, updates)
    val updatedUser = repository.getUserById(defaultUser.userId)

    assertEquals(newHobbies, updatedUser?.hobbies)
  }

  @Test
  fun `updateUser handles empty hobbies list`() = runTest {
    val emptyHobbies = emptyList<String>()
    val updates = mapOf("hobbies" to emptyHobbies)
    repository.updateUser(defaultUser.userId, updates)
    val updatedUser = repository.getUserById(defaultUser.userId)

    assertTrue(updatedUser?.hobbies?.isEmpty() == true)
  }

  @Test
  fun `updateUser does not update non-existent user`() = runTest {
    val updates = mapOf("firstName" to "New Name")
    repository.updateUser("non_existent_id", updates)
    val user = repository.getUserById(defaultUser.userId) // Should remain unchanged

    assertEquals(defaultUser.firstName, user?.firstName)
  }

  @Test
  fun `updateUser handles partial updates`() = runTest {
    val updates = mapOf("firstName" to "UpdatedName")
    repository.updateUser(defaultUser.userId, updates)
    val updatedUser = repository.getUserById(defaultUser.userId)

    assertEquals("UpdatedName", updatedUser?.firstName)
    assertEquals(defaultUser.lastName, updatedUser?.lastName) // Unchanged
    assertEquals(defaultUser.university, updatedUser?.university) // Unchanged
  }

  @Test
  fun `getJoinedEvents returns empty list`() = runTest {
    val events = repository.getJoinedEvents(defaultUser.userId)
    assertTrue(events.isEmpty())
  }

  @Test
  fun `declineInvitation simulates success`() = runTest {
    repository.declineInvitation("event1", defaultUser.userId)
    assertTrue(true) // No exception means success
  }

  @Test
  fun `saveUser does nothing in mock`() = runTest {
    val newUser =
        User(
            userId = "new",
            firstName = "New",
            lastName = "User",
            email = "new@example.com",
            university = "Test University",
            country = "Test Country",
            birthday = "01/01/2000",
            hobbies = emptyList(),
            bio = "New user bio",
            profilePictureUrl = null)
    repository.saveUser(newUser)
    val retrievedUser = repository.getUserById("new")
    assertNull(retrievedUser) // MockUserRepository doesn't actually save new users
  }

  @Test
  fun `getUserByEmail returns null`() = runTest {
    assertNull(repository.getUserByEmail("test@example.com"))
  }

  @Test
  fun `getAllUsers returns list with mock user`() = runTest {
    val users = repository.getAllUsers()
    assertEquals(1, users.size)
    assertEquals(defaultUser.userId, users[0].userId)
  }

  @Test
  fun `getUsersPaginated returns list with mock user and false`() = runTest {
    val (users, hasMore) = repository.getUsersPaginated(10)
    assertEquals(1, users.size)
    assertEquals(defaultUser.userId, users[0].userId)
    assertFalse(hasMore)
  }

  @Test
  fun `deleteUser does nothing in mock`() = runTest {
    repository.deleteUser(defaultUser.userId)
    assertNotNull(repository.getUserById(defaultUser.userId)) // User still exists in mock
  }

  @Test
  fun `getUsersByUniversity returns correct results`() = runTest {
    val epflUsers = repository.getUsersByUniversity("EPFL")
    assertEquals(1, epflUsers.size)
    assertEquals(defaultUser.userId, epflUsers[0].userId)

    val ethzUsers = repository.getUsersByUniversity("ETHZ")
    assertTrue(ethzUsers.isEmpty())
  }

  @Test
  fun `getUsersByHobby returns correct results`() = runTest {
    val readingUsers = repository.getUsersByHobby("Reading")
    assertEquals(1, readingUsers.size)
    assertEquals(defaultUser.userId, readingUsers[0].userId)

    val footballUsers = repository.getUsersByHobby("Football")
    assertEquals(1, footballUsers.size)
    assertEquals(defaultUser.userId, footballUsers[0].userId)

    val nonExistentHobbyUsers = repository.getUsersByHobby("NonExistentHobby")
    assertTrue(nonExistentHobbyUsers.isEmpty())
  }

  @Test
  fun `getNewUid returns a string`() = runTest {
    val uid1 = repository.getNewUid()
    val uid2 = repository.getNewUid()

    assertNotNull(uid1)
    assertNotNull(uid2)
    assertTrue(uid1.isNotBlank())
    assertTrue(uid2.isNotBlank())
    // Should be different each time
    assertTrue(uid1 != uid2)
  }

  @Test
  fun `addEventToUser does nothing in mock`() = runTest {
    repository.addEventToUser("event1", defaultUser.userId)
    assertTrue(true) // No exception means success
  }

  @Test
  fun `addInvitationToUser does nothing in mock`() = runTest {
    repository.addInvitationToUser("event1", defaultUser.userId, "fromUser")
    assertTrue(true) // No exception means success
  }

  @Test
  fun `getInvitations returns empty list`() = runTest {
    assertTrue(repository.getInvitations(defaultUser.userId).isEmpty())
  }

  @Test
  fun `acceptInvitation does nothing in mock`() = runTest {
    repository.acceptInvitation("event1", defaultUser.userId)
    assertTrue(true) // No exception means success
  }

  @Test
  fun `joinEvent does nothing in mock`() = runTest {
    repository.joinEvent("event1", defaultUser.userId)
    assertTrue(true) // No exception means success
  }

  @Test
  fun `sendInvitation does nothing in mock`() = runTest {
    repository.sendInvitation("event1", "fromUser", defaultUser.userId)
    assertTrue(true) // No exception means success
  }

  @Test
  fun `leaveEvent does nothing in mock`() = runTest {
    repository.leaveEvent("event1", defaultUser.userId)
    assertTrue(true) // No exception means success
  }
}
