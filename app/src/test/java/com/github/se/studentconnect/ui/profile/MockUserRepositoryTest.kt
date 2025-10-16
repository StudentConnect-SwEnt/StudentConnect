package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockUserRepositoryTest {

  private lateinit var repository: MockUserRepository
  private lateinit var testUser: User

  @Before
  fun setUp() {
    testUser =
        User(
            userId = "test_123",
            email = "test@epfl.ch",
            firstName = "Test",
            lastName = "User",
            university = "EPFL",
            hobbies = listOf("Testing", "Debugging"),
            profilePictureUrl = null,
            bio = "Test bio",
            country = "Switzerland",
            birthday = "01/01/2000",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis())
    repository = MockUserRepository(previewUser = testUser, generatedUid = "new_test_uid")
  }

  @Test
  fun `getUserById returns preview user`() = runTest {
    val user = repository.getUserById("any_id")
    assertNotNull(user)
    assertEquals(testUser, user)
  }

  @Test
  fun `getUserByEmail returns preview user`() = runTest {
    val user = repository.getUserByEmail("any@email.com")
    assertNotNull(user)
    assertEquals(testUser, user)
  }

  @Test
  fun `getAllUsers returns list with preview user`() = runTest {
    val users = repository.getAllUsers()
    assertEquals(1, users.size)
    assertEquals(testUser, users[0])
  }

  @Test
  fun `getUsersPaginated returns preview user and false hasMore`() = runTest {
    val (users, hasMore) = repository.getUsersPaginated(10, null)
    assertEquals(1, users.size)
    assertEquals(testUser, users[0])
    assertEquals(false, hasMore)
  }

  @Test
  fun `saveUser does not throw exception`() = runTest {
    // Should complete without exception
    repository.saveUser(testUser)
  }

  @Test
  fun `updateUser does not throw exception`() = runTest {
    // Should complete without exception
    repository.updateUser("test_123", mapOf("firstName" to "Updated"))
  }

  @Test
  fun `deleteUser does not throw exception`() = runTest {
    // Should complete without exception
    repository.deleteUser("test_123")
  }

  @Test
  fun `getUsersByUniversity returns preview user`() = runTest {
    val users = repository.getUsersByUniversity("EPFL")
    assertEquals(1, users.size)
    assertEquals(testUser, users[0])
  }

  @Test
  fun `getUsersByHobby returns preview user`() = runTest {
    val users = repository.getUsersByHobby("Testing")
    assertEquals(1, users.size)
    assertEquals(testUser, users[0])
  }

  @Test
  fun `getNewUid returns generated uid`() = runTest {
    val uid = repository.getNewUid()
    assertEquals("new_test_uid", uid)
  }

  @Test
  fun `getJoinedEvents returns empty list`() = runTest {
    val events = repository.getJoinedEvents("test_123")
    assertTrue(events.isEmpty())
  }

  @Test
  fun `addEventToUser does not throw exception`() = runTest {
    // Should complete without exception
    repository.addEventToUser("event_123", "user_123")
  }

  @Test
  fun `leaveEvent does not throw exception`() = runTest {
    // Should complete without exception
    repository.leaveEvent("event_123", "user_123")
  }

  @Test
  fun `addInvitationToUser does not throw exception`() = runTest {
    // Should complete without exception
    repository.addInvitationToUser("event_123", "user_123", "from_user_123")
  }

  @Test
  fun `getInvitations returns empty list`() = runTest {
    val invitations = repository.getInvitations("test_123")
    assertTrue(invitations.isEmpty())
  }

  @Test
  fun `acceptInvitation does not throw exception`() = runTest {
    // Should complete without exception
    repository.acceptInvitation("event_123", "user_123")
  }

  @Test
  fun `joinEvent does not throw exception`() = runTest {
    // Should complete without exception
    repository.joinEvent("event_123", "user_123")
  }

  @Test
  fun `sendInvitation does not throw exception`() = runTest {
    // Should complete without exception
    repository.sendInvitation("event_123", "from_user_123", "to_user_123")
  }

  @Test
  fun `default constructor creates repository with default user`() = runTest {
    val defaultRepository = MockUserRepository()
    val user = defaultRepository.getUserById("any_id")
    assertNotNull(user)
    assertEquals("mock_user_123", user?.userId)
    assertEquals("Forest", user?.firstName)
    assertEquals("Gump", user?.lastName)
  }

  @Test
  fun `repository with null bio handles correctly`() = runTest {
    val userWithNullBio = testUser.copy(bio = null)
    val repo = MockUserRepository(previewUser = userWithNullBio)
    val user = repo.getUserById("any_id")
    assertNull(user?.bio)
  }

  @Test
  fun `repository with empty hobbies handles correctly`() = runTest {
    val userWithNoHobbies = testUser.copy(hobbies = emptyList())
    val repo = MockUserRepository(previewUser = userWithNoHobbies)
    val user = repo.getUserById("any_id")
    assertTrue(user?.hobbies?.isEmpty() == true)
  }

  @Test
  fun `repository with null country handles correctly`() = runTest {
    val userWithNullCountry = testUser.copy(country = null)
    val repo = MockUserRepository(previewUser = userWithNullCountry)
    val user = repo.getUserById("any_id")
    assertNull(user?.country)
  }

  @Test
  fun `repository with null birthday handles correctly`() = runTest {
    val userWithNullBirthday = testUser.copy(birthday = null)
    val repo = MockUserRepository(previewUser = userWithNullBirthday)
    val user = repo.getUserById("any_id")
    assertNull(user?.birthday)
  }
}
