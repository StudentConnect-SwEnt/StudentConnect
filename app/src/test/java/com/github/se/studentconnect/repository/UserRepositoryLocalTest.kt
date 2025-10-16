package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.ui.screen.activities.InvitationStatus
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UserRepositoryLocalTest {

  private lateinit var repository: UserRepositoryLocal

  private val testUser1 =
      User(
          userId = "user1",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          hobbies = listOf("reading", "coding"))

  private val testUser2 =
      User(
          userId = "user2",
          firstName = "Jane",
          lastName = "Smith",
          email = "jane.smith@example.com",
          university = "ETH",
          hobbies = listOf("sports", "music"))

  @Before
  fun setup() {
    repository = UserRepositoryLocal()
  }

  @Test
  fun getUserById_returnsNull_whenUserDoesNotExist() = runTest {
    val result = repository.getUserById("non-existent-user")
    assertNull(result)
  }

  @Test
  fun saveUser_andGetUserById_returnsUser() = runTest {
    repository.saveUser(testUser1)
    val result = repository.getUserById(testUser1.userId)
    assertNotNull(result)
    assertEquals(testUser1.userId, result?.userId)
    assertEquals(testUser1.firstName, result?.firstName)
  }

  @Test
  fun getUserByEmail_returnsNull_whenUserDoesNotExist() = runTest {
    val result = repository.getUserByEmail("non-existent@example.com")
    assertNull(result)
  }

  @Test
  fun saveUser_andGetUserByEmail_returnsUser() = runTest {
    repository.saveUser(testUser1)
    val result = repository.getUserByEmail(testUser1.email)
    assertNotNull(result)
    assertEquals(testUser1.email, result?.email)
    assertEquals(testUser1.firstName, result?.firstName)
  }

  @Test
  fun getAllUsers_returnsEmptyList_initially() = runTest {
    val result = repository.getAllUsers()
    assertTrue(result.isEmpty())
  }

  @Test
  fun getAllUsers_returnsAllSavedUsers() = runTest {
    repository.saveUser(testUser1)
    repository.saveUser(testUser2)

    val result = repository.getAllUsers()
    assertEquals(2, result.size)
    assertTrue(result.any { it.userId == testUser1.userId })
    assertTrue(result.any { it.userId == testUser2.userId })
  }

  @Test
  fun saveUser_replacesExistingUser() = runTest {
    repository.saveUser(testUser1)

    val updatedUser = testUser1.copy(firstName = "Johnny")
    repository.saveUser(updatedUser)

    val result = repository.getUserById(testUser1.userId)
    assertEquals("Johnny", result?.firstName)
  }

  @Test
  fun updateUser_updatesSpecificFields() = runTest {
    repository.saveUser(testUser1)

    repository.updateUser(testUser1.userId, mapOf("firstName" to "Jonathan"))

    val result = repository.getUserById(testUser1.userId)
    assertEquals("Jonathan", result?.firstName)
    assertEquals(testUser1.lastName, result?.lastName) // Other fields unchanged
  }

  @Test
  fun updateUser_doesNothing_whenUserDoesNotExist() = runTest {
    repository.updateUser("non-existent-user", mapOf("firstName" to "Test"))
    val result = repository.getUserById("non-existent-user")
    assertNull(result)
  }

  @Test
  fun deleteUser_removesUser() = runTest {
    repository.saveUser(testUser1)
    repository.deleteUser(testUser1.userId)

    val result = repository.getUserById(testUser1.userId)
    assertNull(result)
  }

  @Test
  fun deleteUser_removesJoinedEvents() = runTest {
    repository.saveUser(testUser1)
    repository.addEventToUser("event1", testUser1.userId)

    repository.deleteUser(testUser1.userId)

    val events = repository.getJoinedEvents(testUser1.userId)
    assertTrue(events.isEmpty())
  }

  @Test
  fun getUsersByUniversity_returnsMatchingUsers() = runTest {
    repository.saveUser(testUser1)
    repository.saveUser(testUser2)

    val result = repository.getUsersByUniversity("EPFL")
    assertEquals(1, result.size)
    assertEquals(testUser1.userId, result[0].userId)
  }

  @Test
  fun getUsersByHobby_returnsMatchingUsers() = runTest {
    repository.saveUser(testUser1)
    repository.saveUser(testUser2)

    val result = repository.getUsersByHobby("coding")
    assertEquals(1, result.size)
    assertEquals(testUser1.userId, result[0].userId)
  }

  @Test
  fun getNewUid_generatesUniqueId() = runTest {
    val uid1 = repository.getNewUid()
    val uid2 = repository.getNewUid()

    assertNotNull(uid1)
    assertNotNull(uid2)
    assertTrue(uid1 != uid2)
  }

  @Test
  fun getJoinedEvents_returnsEmptyList_initially() = runTest {
    val result = repository.getJoinedEvents("user1")
    assertTrue(result.isEmpty())
  }

  @Test
  fun addEventToUser_addsEvent() = runTest {
    repository.addEventToUser("event1", "user1")

    val result = repository.getJoinedEvents("user1")
    assertEquals(1, result.size)
    assertEquals("event1", result[0])
  }

  @Test
  fun addEventToUser_doesNotAddDuplicate() = runTest {
    repository.addEventToUser("event1", "user1")
    repository.addEventToUser("event1", "user1")

    val result = repository.getJoinedEvents("user1")
    assertEquals(1, result.size)
  }

  @Test
  fun addEventToUser_addsMultipleEvents() = runTest {
    repository.addEventToUser("event1", "user1")
    repository.addEventToUser("event2", "user1")

    val result = repository.getJoinedEvents("user1")
    assertEquals(2, result.size)
    assertTrue(result.contains("event1"))
    assertTrue(result.contains("event2"))
  }

  @Test
  fun joinEvent_addsEventToUser() = runTest {
    repository.joinEvent("event1", "user1")

    val result = repository.getJoinedEvents("user1")
    assertEquals(1, result.size)
    assertEquals("event1", result[0])
  }

  @Test
  fun leaveEvent_removesEvent() = runTest {
    repository.addEventToUser("event1", "user1")
    repository.leaveEvent("event1", "user1")

    val result = repository.getJoinedEvents("user1")
    assertTrue(result.isEmpty())
  }

  @Test
  fun getInvitations_returnsEmptyList_initially() = runTest {
    val result = repository.getInvitations("user1")
    assertTrue(result.isEmpty())
  }

  @Test
  fun addInvitationToUser_addsInvitation() = runTest {
    repository.addInvitationToUser("event1", "user1", "user2")

    val result = repository.getInvitations("user1")
    assertEquals(1, result.size)
    assertEquals("event1", result[0].eventId)
    assertEquals("user2", result[0].from)
  }

  @Test
  fun addInvitationToUser_doesNotAddDuplicate() = runTest {
    repository.addInvitationToUser("event1", "user1", "user2")
    repository.addInvitationToUser("event1", "user1", "user2")

    val result = repository.getInvitations("user1")
    assertEquals(1, result.size)
  }

  @Test
  fun acceptInvitation_removesInvitationAndAddsEvent() = runTest {
    repository.addInvitationToUser("event1", "user1", "user2")
    repository.acceptInvitation("event1", "user1")

    val invitations = repository.getInvitations("user1")
    assertTrue(invitations.isEmpty())

    val events = repository.getJoinedEvents("user1")
    assertEquals(1, events.size)
    assertEquals("event1", events[0])
  }

  @Test
  fun declineInvitation_changesStatus() = runTest {
    repository.addInvitationToUser("event1", "user1", "user2")
    repository.declineInvitation("event1", "user1")

    val invitations = repository.getInvitations("user1")
    assertEquals(1, invitations.size)
    assertEquals(InvitationStatus.Declined, invitations[0].status)
  }

  @Test
  fun sendInvitation_addsInvitationToRecipient() = runTest {
    repository.sendInvitation("event1", "user1", "user2")

    val invitations = repository.getInvitations("user2")
    assertEquals(1, invitations.size)
    assertEquals("event1", invitations[0].eventId)
    assertEquals("user1", invitations[0].from)
  }

  @Test
  fun getUsersPaginated_firstPage() = runTest {
    repository.saveUser(testUser1)
    repository.saveUser(testUser2)

    val (users, hasMore) = repository.getUsersPaginated(limit = 1, lastUserId = null)

    assertEquals(1, users.size)
    assertTrue(hasMore)
  }

  @Test
  fun getUsersPaginated_secondPage() = runTest {
    repository.saveUser(testUser1)
    repository.saveUser(testUser2)

    val (firstPage, _) = repository.getUsersPaginated(limit = 1, lastUserId = null)
    val (secondPage, hasMore) =
        repository.getUsersPaginated(limit = 1, lastUserId = firstPage[0].userId)

    assertEquals(1, secondPage.size)
    assertFalse(hasMore)
  }

  @Test
  fun getUsersPaginated_noMoreResults() = runTest {
    repository.saveUser(testUser1)

    val (users, hasMore) = repository.getUsersPaginated(limit = 5, lastUserId = null)

    assertEquals(1, users.size)
    assertFalse(hasMore)
  }

  @Test
  fun getUsersPaginated_emptyRepository() = runTest {
    val (users, hasMore) = repository.getUsersPaginated(limit = 5, lastUserId = null)

    assertTrue(users.isEmpty())
    assertFalse(hasMore)
  }

  @Test
  fun joinEvent_removesInvitation() = runTest {
    repository.addInvitationToUser("event1", "user1", "user2")
    repository.joinEvent("event1", "user1")

    val invitations = repository.getInvitations("user1")
    assertTrue(invitations.isEmpty())
  }

  @Test
  fun multipleUsers_canJoinSameEvent() = runTest {
    repository.addEventToUser("event1", "user1")
    repository.addEventToUser("event1", "user2")

    val user1Events = repository.getJoinedEvents("user1")
    val user2Events = repository.getJoinedEvents("user2")

    assertEquals(1, user1Events.size)
    assertEquals(1, user2Events.size)
    assertEquals("event1", user1Events[0])
    assertEquals("event1", user2Events[0])
  }

  @Test
  fun singleUser_canJoinMultipleEvents() = runTest {
    repository.addEventToUser("event1", "user1")
    repository.addEventToUser("event2", "user1")
    repository.addEventToUser("event3", "user1")

    val events = repository.getJoinedEvents("user1")
    assertEquals(3, events.size)
    assertTrue(events.contains("event1"))
    assertTrue(events.contains("event2"))
    assertTrue(events.contains("event3"))
  }
}
