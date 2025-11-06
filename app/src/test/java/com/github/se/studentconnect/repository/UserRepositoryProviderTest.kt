package com.github.se.studentconnect.repository

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
  fun repository_populatesUsersWithUsernames() = runBlocking {
    val repository = UserRepositoryProvider.repository as? UserRepositoryLocal
    assertNotNull(repository)

    val users = repository?.getAllUsers() ?: emptyList()
    // Verify users exist and have usernames (covers lines 52 and 60)
    assertTrue(users.isNotEmpty())
    users.forEach { user ->
      assertNotNull(user.username)
      assertTrue(user.username.isNotBlank())
    }
  }
}
