package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking

/**
 * Provides instances of UserRepository. Allows switching between Firestore and a local, in-memory
 * repository for testing.
 */
object UserRepositoryProvider {
  private val firestoreRepository: UserRepository = UserRepositoryFirestore(Firebase.firestore)
  private val localRepository: UserRepository = UserRepositoryLocal()

  /**
   * The currently active repository. Change the value of `useLocal` to switch between
   * implementations.
   */
  var repository: UserRepository

  init {
    EventRepositoryProvider
    runBlocking { populateLocalRepositoryWithFakeData() }
    val useLocal = true

    repository = if (useLocal) localRepository else firestoreRepository
  }

  /**
   * Populates the local user repository with predefined users and links them to events from the
   * EventRepositoryProvider.
   */
  private suspend fun populateLocalRepositoryWithFakeData() {
    val fakeUsers = createFakeUsers()
    fakeUsers.forEach { user -> localRepository.saveUser(user) }

    linkUsersToEvents()
  }

  private fun createFakeUsers(): List<User> {
    return listOf(
        User(
            userId = "user-charlie-02",
            email = "alice@example.com",
            firstName = "Alice",
            lastName = "Smith",
            university = "EPFL"),
        User(
            userId = "user-bob-02",
            email = "bob@example.com",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            hobbies = listOf("Music", "SQL")),
        User(
            userId = "user-charlie-03",
            email = "charlie@example.com",
            firstName = "Charlie",
            lastName = "Brown",
            university = "UNIL",
            hobbies = listOf("Sailing", "Reading")))
  }

  private suspend fun linkUsersToEvents() {
    val eventIds = EventRepositoryProvider.createFakeEvents().map { it.uid }
    if (eventIds.size < 4) return
    val currentUser = AuthentificationProvider.currentUser
    localRepository.joinEvent(eventId = "event-balelec-03", userId = currentUser)
    localRepository.joinEvent(eventId = "event-rlc-study-04", userId = currentUser)

    localRepository.joinEvent(eventId = "event-sql-workshop-02", userId = "user-bob-02")
    localRepository.joinEvent(eventId = "event-killer-concert-01", userId = currentUser)

    localRepository.joinEvent(eventId = "event-balelec-03", userId = "user-charlie-03")
    localRepository.addInvitationToUser(
        eventId = "event-sql-workshop-02", userId = currentUser, fromUserId = "user-bob-02")
  }
}
