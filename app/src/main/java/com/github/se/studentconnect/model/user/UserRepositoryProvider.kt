package com.github.se.studentconnect.model.user

import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking

/**
 * Provides instances of UserRepository. Allows switching between Firestore and a local, in-memory
 * repository for testing.
 *
 * The repository mode is controlled by `AuthenticationProvider.local`:
 * - local = true: Uses in-memory repository for testing
 * - local = false: Uses Firestore for production
 */
object UserRepositoryProvider {
  private val firestoreRepository: UserRepository = UserRepositoryFirestore(Firebase.firestore)
  private val localRepository: UserRepository = UserRepositoryLocal()

  /** The currently active repository. Automatically syncs with AuthenticationProvider.local */
  var repository: UserRepository

  init {
    EventRepositoryProvider
    val useLocal = AuthenticationProvider.local

    repository = if (useLocal) localRepository else firestoreRepository

    // Only populate fake data if needed for testing other users
    // The current user will go through signup flow
    if (useLocal) {
      runBlocking { populateLocalRepositoryWithOtherUsers() }
    }
  }

  /**
   * Populates the local user repository with other test users (NOT the current user). The current
   * user should go through the signup flow.
   */
  private suspend fun populateLocalRepositoryWithOtherUsers() {
    val otherUsers = createOtherTestUsers()
    otherUsers.forEach { user -> localRepository.saveUser(user) }
  }

  private fun createOtherTestUsers(): List<User> {
    // Create other users for testing, but NOT the current user (user-charlie-02)
    return listOf(
        User(
            userId = "user-bob-02",
            email = "bob@example.com",
            username = "bob_the_builder",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            hobbies = listOf("Music", "SQL")),
        User(
            userId = "user-charlie-03",
            email = "charlie@example.com",
            username = "charlie_brown",
            firstName = "Charlie",
            lastName = "Brown",
            university = "UNIL",
            hobbies = listOf("Sailing", "Reading")))
  }
}
