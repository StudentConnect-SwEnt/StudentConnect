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
  private val mockUser =
      User(
          userId = "mock_user",
          firstName = "Mock",
          lastName = "User",
          email = "mock@example.com",
          university = "Mock University",
          country = "Mock Country",
          birthday = "01/01/2000",
          hobbies = listOf("Mock Hobby"),
          bio = "Mock bio",
          profilePictureUrl = null)

  @Before
  fun setUp() {
    repository = MockUserRepository()
  }

  @Test
  fun `getUserById returns mock user for valid ID`() = runTest {
    val user = repository.getUserById("mock_user")
    assertEquals(mockUser, user)
  }

  @Test
  fun `getUserById returns null for invalid ID`() = runTest {
    val user = repository.getUserById("invalid_id")
    assertNull(user)
  }

  @Test
  fun `getAllUsers returns list with mock user`() = runTest {
    val users = repository.getAllUsers()
    assertEquals(1, users.size)
    assertEquals(mockUser, users.first())
  }

  @Test
  fun `getUsersPaginated returns mock user with hasMore false`() = runTest {
    val (users, hasMore) = repository.getUsersPaginated(10, null)
    assertEquals(1, users.size)
    assertEquals(mockUser, users.first())
    assertFalse(hasMore)
  }

  @Test
  fun `getUsersPaginated with lastUserId returns empty list`() = runTest {
    val (users, hasMore) = repository.getUsersPaginated(10, "mock_user")
    assertTrue(users.isEmpty())
    assertFalse(hasMore)
  }

  @Test
  fun `getUsersByUniversity returns mock user when university matches`() = runTest {
    val users = repository.getUsersByUniversity("Mock University")
    assertEquals(1, users.size)
    assertEquals(mockUser, users.first())
  }

  @Test
  fun `getUsersByUniversity returns empty list when university does not match`() = runTest {
    val users = repository.getUsersByUniversity("Different University")
    assertTrue(users.isEmpty())
  }

  @Test
  fun `getUsersByHobby returns mock user when hobby matches`() = runTest {
    val users = repository.getUsersByHobby("Mock Hobby")
    assertEquals(1, users.size)
    assertEquals(mockUser, users.first())
  }

  @Test
  fun `getUsersByHobby returns empty list when hobby does not match`() = runTest {
    val users = repository.getUsersByHobby("Different Hobby")
    assertTrue(users.isEmpty())
  }

  @Test
  fun `updateUser updates user fields correctly`() = runTest {
    val updates =
        mapOf("firstName" to "Updated", "lastName" to "Name", "university" to "Updated University")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("Updated", updatedUser?.firstName)
    assertEquals("Name", updatedUser?.lastName)
    assertEquals("Updated University", updatedUser?.university)
    // Other fields should remain unchanged
    assertEquals(mockUser.email, updatedUser?.email)
    assertEquals(mockUser.country, updatedUser?.country)
  }

  @Test
  fun `updateUser handles null values by keeping original values`() = runTest {
    val updates = mapOf("firstName" to null, "lastName" to "Updated Last Name")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals(mockUser.firstName, updatedUser?.firstName) // Should remain unchanged
    assertEquals("Updated Last Name", updatedUser?.lastName)
  }

  @Test
  fun `updateUser handles non-existent user gracefully`() = runTest {
    val updates = mapOf("firstName" to "New Name")

    // Should not throw exception
    repository.updateUser("non_existent_user", updates)

    // Original user should remain unchanged
    val user = repository.getUserById("mock_user")
    assertEquals(mockUser.firstName, user?.firstName)
  }

  @Test
  fun `updateUser handles hobbies list correctly`() = runTest {
    val updates = mapOf("hobbies" to listOf("New Hobby 1", "New Hobby 2"))

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals(listOf("New Hobby 1", "New Hobby 2"), updatedUser?.hobbies)
  }

  @Test
  fun `updateUser handles empty hobbies list`() = runTest {
    val updates = mapOf("hobbies" to emptyList<String>())

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertTrue(updatedUser?.hobbies?.isEmpty() == true)
  }

  @Test
  fun `updateUser handles null hobbies`() = runTest {
    val updates = mapOf("hobbies" to null)

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals(mockUser.hobbies, updatedUser?.hobbies) // Should remain unchanged
  }

  @Test
  fun `updateUser handles profilePictureUrl correctly`() = runTest {
    val updates = mapOf("profilePictureUrl" to "http://example.com/new-pic.jpg")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("http://example.com/new-pic.jpg", updatedUser?.profilePictureUrl)
  }

  @Test
  fun `updateUser handles null profilePictureUrl`() = runTest {
    val updates = mapOf("profilePictureUrl" to null)

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertNull(updatedUser?.profilePictureUrl)
  }

  @Test
  fun `updateUser handles bio correctly`() = runTest {
    val updates = mapOf("bio" to "Updated bio content")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("Updated bio content", updatedUser?.bio)
  }

  @Test
  fun `updateUser handles null bio`() = runTest {
    val updates = mapOf("bio" to null)

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertNull(updatedUser?.bio)
  }

  @Test
  fun `updateUser handles birthday correctly`() = runTest {
    val updates = mapOf("birthday" to "15/03/1995")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("15/03/1995", updatedUser?.birthday)
  }

  @Test
  fun `updateUser handles null birthday`() = runTest {
    val updates = mapOf("birthday" to null)

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertNull(updatedUser?.birthday)
  }

  @Test
  fun `updateUser handles country correctly`() = runTest {
    val updates = mapOf("country" to "France")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("France", updatedUser?.country)
  }

  @Test
  fun `updateUser handles null country`() = runTest {
    val updates = mapOf("country" to null)

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertNull(updatedUser?.country)
  }

  @Test
  fun `updateUser handles email correctly`() = runTest {
    val updates = mapOf("email" to "updated@example.com")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("updated@example.com", updatedUser?.email)
  }

  @Test
  fun `updateUser handles multiple fields at once`() = runTest {
    val updates =
        mapOf(
            "firstName" to "John",
            "lastName" to "Smith",
            "university" to "ETHZ",
            "country" to "Switzerland",
            "bio" to "New bio",
            "hobbies" to listOf("Swimming", "Reading"))

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("John", updatedUser?.firstName)
    assertEquals("Smith", updatedUser?.lastName)
    assertEquals("ETHZ", updatedUser?.university)
    assertEquals("Switzerland", updatedUser?.country)
    assertEquals("New bio", updatedUser?.bio)
    assertEquals(listOf("Swimming", "Reading"), updatedUser?.hobbies)
  }

  @Test
  fun `updateUser handles empty string values`() = runTest {
    val updates = mapOf("firstName" to "", "lastName" to "", "bio" to "", "country" to "")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("", updatedUser?.firstName)
    assertEquals("", updatedUser?.lastName)
    assertEquals("", updatedUser?.bio)
    assertEquals("", updatedUser?.country)
  }

  @Test
  fun `updateUser handles special characters in strings`() = runTest {
    val updates =
        mapOf(
            "firstName" to "José-María",
            "lastName" to "O'Connor-Smith",
            "university" to "École Polytechnique",
            "country" to "Côte d'Ivoire",
            "bio" to "Bio with unicode: αβγδε, 中文")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("José-María", updatedUser?.firstName)
    assertEquals("O'Connor-Smith", updatedUser?.lastName)
    assertEquals("École Polytechnique", updatedUser?.university)
    assertEquals("Côte d'Ivoire", updatedUser?.country)
    assertEquals("Bio with unicode: αβγδε, 中文", updatedUser?.bio)
  }

  @Test
  fun `updateUser handles very long strings`() = runTest {
    val longString = "A".repeat(1000)
    val updates = mapOf("firstName" to longString, "bio" to longString)

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals(longString, updatedUser?.firstName)
    assertEquals(longString, updatedUser?.bio)
  }

  @Test
  fun `updateUser handles large hobbies list`() = runTest {
    val largeHobbiesList = (1..100).map { "Hobby $it" }
    val updates = mapOf("hobbies" to largeHobbiesList)

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals(largeHobbiesList, updatedUser?.hobbies)
  }

  @Test
  fun `updateUser handles unknown field gracefully`() = runTest {
    val updates = mapOf("unknownField" to "some value")

    // Should not throw exception
    repository.updateUser("mock_user", updates)

    // User should remain unchanged
    val user = repository.getUserById("mock_user")
    assertEquals(mockUser, user)
  }

  @Test
  fun `updateUser handles mixed valid and invalid fields`() = runTest {
    val updates =
        mapOf("firstName" to "Valid", "unknownField" to "Invalid", "lastName" to "Also Valid")

    repository.updateUser("mock_user", updates)

    val updatedUser = repository.getUserById("mock_user")
    assertEquals("Valid", updatedUser?.firstName)
    assertEquals("Also Valid", updatedUser?.lastName)
    // Other fields should remain unchanged
    assertEquals(mockUser.university, updatedUser?.university)
  }
}
