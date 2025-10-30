package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockUserRepositoryTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: MockUserRepository
  private val initialUser =
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
  fun getUserById_returnsMockUserForValidId() = runTest {
    val user = repository.getUserById("mock_user_123")
    // verify important fields match
    assertEquals(initialUser.firstName, user?.firstName)
    assertEquals(initialUser.lastName, user?.lastName)
    assertEquals(initialUser.email, user?.email)
  }

  @Test
  fun getUserById_returnsNullForInvalidId() = runTest {
    val user = repository.getUserById("invalid")
    assertNull(user)
  }

  @Test
  fun getUserByEmail_returnsMockUserForValidEmail() = runTest {
    val user = repository.getUserByEmail("john.doe@epfl.ch")
    assertEquals("mock_user_123", user?.userId)
  }

  @Test
  fun getAllUsers_returnsListWithMockUser() = runTest {
    val users = repository.getAllUsers()
    assertEquals(1, users.size)
    assertEquals("mock_user_123", users.first().userId)
  }

  @Test
  fun getUsersPaginated_returnsMockUserAndHasMoreFalse() = runTest {
    val (users, hasMore) = repository.getUsersPaginated(10, null)
    assertEquals(1, users.size)
    assertFalse(hasMore)
  }

  @Test
  fun updateUser_updatesProvidedFieldsOnly() = runTest {
    repository.updateUser(
        "mock_user_123",
        mapOf(
            "firstName" to "Alice",
            "lastName" to "Smith",
            "university" to "ETHZ",
            "country" to "France",
            "birthday" to "01/01/2000",
            "hobbies" to listOf("Running"),
            "bio" to "New bio",
            "profilePictureUrl" to "http://example.com/pic.jpg"))

    val updated = repository.getUserById("mock_user_123")
    assertEquals("Alice", updated?.firstName)
    assertEquals("Smith", updated?.lastName)
    assertEquals("ETHZ", updated?.university)
    assertEquals("France", updated?.country)
    assertEquals("01/01/2000", updated?.birthday)
    assertEquals(listOf("Running"), updated?.hobbies)
    assertEquals("New bio", updated?.bio)
    assertEquals("http://example.com/pic.jpg", updated?.profilePictureUrl)
  }

  @Test
  fun updateUser_ignoresUnknownUserId() = runTest {
    // Should not throw and should not affect existing mock user
    repository.updateUser("someone_else", mapOf("firstName" to "X"))
    val user = repository.getUserById("mock_user_123")
    assertEquals(initialUser.firstName, user?.firstName)
  }

  @Test
  fun getUsersByUniversity_matchesEpflOnly() = runTest {
    assertEquals(1, repository.getUsersByUniversity("EPFL").size)
    assertTrue(repository.getUsersByUniversity("ETHZ").isEmpty())
  }

  @Test
  fun getUsersByHobby_matchesContainedHobby() = runTest {
    assertEquals(1, repository.getUsersByHobby("Football").size)
    assertTrue(repository.getUsersByHobby("Chess").isEmpty())
  }

  @Test
  fun getNewUid_returnsGeneratedLikeId() = runTest {
    val id = repository.getNewUid()
    assertTrue(id.startsWith("mock_user_"))
  }
}
