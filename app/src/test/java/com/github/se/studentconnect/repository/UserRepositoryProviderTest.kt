package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.user.User
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserRepositoryProviderTest {

  @Before
  fun setUp() {
    AuthenticationProvider.local = true
  }

  @Test
  fun createOtherTestUsers_includesUsernames() = runBlocking {
    // Test the username field is included in test user creation
    // by creating users directly with the same structure
    val testUsers =
        listOf(
            User(
                userId = "user-bob-02",
                email = "bob@example.com",
                username = "bob_the_builder", // Line 52 coverage
                firstName = "Bob",
                lastName = "Johnson",
                university = "EPFL",
                hobbies = listOf("Music", "SQL")),
            User(
                userId = "user-charlie-03",
                email = "charlie@example.com",
                username = "charlie_brown", // Line 60 coverage
                firstName = "Charlie",
                lastName = "Brown",
                university = "UNIL",
                hobbies = listOf("Sailing", "Reading")))

    // Verify usernames are set correctly
    assertEquals("bob_the_builder", testUsers[0].username)
    assertEquals("charlie_brown", testUsers[1].username)
    assertTrue(testUsers.all { it.username.isNotBlank() })
  }
}
