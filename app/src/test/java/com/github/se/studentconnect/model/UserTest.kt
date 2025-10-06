package com.github.se.studentconnect.model

import org.junit.Assert.*
import org.junit.Test

class UserTest {

  private val validUser =
      User(
          userId = "user123",
          email = "test@epfl.ch",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL",
          hobbies = listOf("Football", "Gaming"),
          profilePictureUrl = "https://example.com/pic.jpg",
          bio = "Computer science student",
          createdAt = 1000L,
          updatedAt = 1000L)

  @Test
  fun testUserCreation() {
    assertNotNull(validUser)
    assertEquals("user123", validUser.userId)
    assertEquals("test@epfl.ch", validUser.email)
    assertEquals("John", validUser.firstName)
    assertEquals("Doe", validUser.lastName)
    assertEquals("EPFL", validUser.university)
    assertEquals(2, validUser.hobbies.size)
    assertEquals("Football", validUser.hobbies[0])
    assertEquals("Gaming", validUser.hobbies[1])
    assertEquals("https://example.com/pic.jpg", validUser.profilePictureUrl)
    assertEquals("Computer science student", validUser.bio)
    assertEquals(1000L, validUser.createdAt)
    assertEquals(1000L, validUser.updatedAt)
  }

  @Test
  fun testUserWithEmptyHobbies() {
    val user =
        User(
            userId = "user456",
            email = "test@unil.ch",
            firstName = "Jane",
            lastName = "Smith",
            university = "UNIL",
            hobbies = emptyList(),
            createdAt = 2000L,
            updatedAt = 2000L)
    assertEquals(0, user.hobbies.size)
    assertNull(user.profilePictureUrl)
    assertNull(user.bio)
  }

  @Test
  fun testUserWithDefaultTimestamps() {
    val beforeCreation = System.currentTimeMillis()
    val user =
        User(
            userId = "user789",
            email = "test@ethz.ch",
            firstName = "Alice",
            lastName = "Johnson",
            university = "ETHZ")
    val afterCreation = System.currentTimeMillis()

    assertTrue(user.createdAt >= beforeCreation)
    assertTrue(user.createdAt <= afterCreation)
    assertTrue(user.updatedAt >= beforeCreation)
    assertTrue(user.updatedAt <= afterCreation)
    assertEquals(user.createdAt, user.updatedAt)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankUid() {
    User(
        userId = "",
        email = "test@epfl.ch",
        firstName = "John",
        lastName = "Doe",
        university = "EPFL")
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankEmail() {
    User(userId = "user123", email = "", firstName = "John", lastName = "Doe", university = "EPFL")
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithInvalidEmail() {
    User(
        userId = "user123",
        email = "invalid-email",
        firstName = "John",
        lastName = "Doe",
        university = "EPFL")
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankFirstName() {
    User(
        userId = "user123",
        email = "test@epfl.ch",
        firstName = "",
        lastName = "Doe",
        university = "EPFL")
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankLastName() {
    User(
        userId = "user123",
        email = "test@epfl.ch",
        firstName = "John",
        lastName = "",
        university = "EPFL")
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankUniversity() {
    User(
        userId = "user123",
        email = "test@epfl.ch",
        firstName = "John",
        lastName = "Doe",
        university = "")
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithNegativeCreatedAt() {
    User(
        userId = "user123",
        email = "test@epfl.ch",
        firstName = "John",
        lastName = "Doe",
        university = "EPFL",
        createdAt = -1L,
        updatedAt = 1000L)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithNegativeUpdatedAt() {
    User(
        userId = "user123",
        email = "test@epfl.ch",
        firstName = "John",
        lastName = "Doe",
        university = "EPFL",
        createdAt = 1000L,
        updatedAt = -1L)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithUpdatedBeforeCreated() {
    User(
        userId = "user123",
        email = "test@epfl.ch",
        firstName = "John",
        lastName = "Doe",
        university = "EPFL",
        createdAt = 2000L,
        updatedAt = 1000L)
  }

  @Test
  fun testGetFullName() {
    assertEquals("John Doe", validUser.getFullName())
  }

  @Test
  fun testHasProfilePicture() {
    assertTrue(validUser.hasProfilePicture())

    val userWithoutPicture =
        User(
            userId = "user456",
            email = "test@unil.ch",
            firstName = "Jane",
            lastName = "Smith",
            university = "UNIL",
            profilePictureUrl = null,
            createdAt = 1000L,
            updatedAt = 1000L)
    assertFalse(userWithoutPicture.hasProfilePicture())

    val userWithBlankPicture =
        User(
            userId = "user789",
            email = "test@ethz.ch",
            firstName = "Alice",
            lastName = "Johnson",
            university = "ETHZ",
            profilePictureUrl = "",
            createdAt = 1000L,
            updatedAt = 1000L)
    assertFalse(userWithBlankPicture.hasProfilePicture())
  }

  @Test
  fun testHasBio() {
    assertTrue(validUser.hasBio())

    val userWithoutBio =
        User(
            userId = "user456",
            email = "test@unil.ch",
            firstName = "Jane",
            lastName = "Smith",
            university = "UNIL",
            bio = null,
            createdAt = 1000L,
            updatedAt = 1000L)
    assertFalse(userWithoutBio.hasBio())

    val userWithBlankBio =
        User(
            userId = "user789",
            email = "test@ethz.ch",
            firstName = "Alice",
            lastName = "Johnson",
            university = "ETHZ",
            bio = "",
            createdAt = 1000L,
            updatedAt = 1000L)
    assertFalse(userWithBlankBio.hasBio())
  }

  @Test
  fun testUpdateEmail() {
    val beforeUpdate = System.currentTimeMillis()
    val updatedUser = validUser.update(email = "newemail@epfl.ch")
    val afterUpdate = System.currentTimeMillis()

    assertEquals("newemail@epfl.ch", updatedUser.email)
    assertEquals(validUser.firstName, updatedUser.firstName)
    assertEquals(validUser.lastName, updatedUser.lastName)
    assertEquals(validUser.university, updatedUser.university)
    assertEquals(validUser.hobbies, updatedUser.hobbies)
    assertEquals(validUser.createdAt, updatedUser.createdAt)
    assertTrue(updatedUser.updatedAt >= beforeUpdate)
    assertTrue(updatedUser.updatedAt <= afterUpdate)
    assertTrue(updatedUser.updatedAt > validUser.updatedAt)
  }

  @Test
  fun testUpdateFirstName() {
    val updatedUser = validUser.update(firstName = "Jack")
    assertEquals("Jack", updatedUser.firstName)
    assertEquals(validUser.email, updatedUser.email)
  }

  @Test
  fun testUpdateLastName() {
    val updatedUser = validUser.update(lastName = "Brown")
    assertEquals("Brown", updatedUser.lastName)
    assertEquals(validUser.firstName, updatedUser.firstName)
  }

  @Test
  fun testUpdateUniversity() {
    val updatedUser = validUser.update(university = "UNIL")
    assertEquals("UNIL", updatedUser.university)
    assertEquals(validUser.firstName, updatedUser.firstName)
  }

  @Test
  fun testUpdateHobbies() {
    val newHobbies = listOf("Reading", "Swimming", "Cooking")
    val updatedUser = validUser.update(hobbies = newHobbies)
    assertEquals(newHobbies, updatedUser.hobbies)
    assertEquals(3, updatedUser.hobbies.size)
    assertEquals(validUser.firstName, updatedUser.firstName)
  }

  @Test
  fun testUpdateProfilePictureUrl() {
    val newUrl = "https://example.com/newpic.jpg"
    val updatedUser = validUser.update(profilePictureUrl = newUrl)
    assertEquals(newUrl, updatedUser.profilePictureUrl)
    assertEquals(validUser.email, updatedUser.email)
  }

  @Test
  fun testUpdateBio() {
    val newBio = "Engineering student passionate about AI"
    val updatedUser = validUser.update(bio = newBio)
    assertEquals(newBio, updatedUser.bio)
    assertEquals(validUser.email, updatedUser.email)
  }

  @Test
  fun testUpdateMultipleFields() {
    val updatedUser =
        validUser.update(
            email = "newemail@epfl.ch", firstName = "Jack", hobbies = listOf("Reading"))
    assertEquals("newemail@epfl.ch", updatedUser.email)
    assertEquals("Jack", updatedUser.firstName)
    assertEquals(1, updatedUser.hobbies.size)
    assertEquals("Reading", updatedUser.hobbies[0])
    assertEquals(validUser.lastName, updatedUser.lastName)
    assertEquals(validUser.university, updatedUser.university)
  }

  @Test
  fun testUpdateWithNoChanges() {
    val beforeUpdate = System.currentTimeMillis()
    val updatedUser = validUser.update()
    val afterUpdate = System.currentTimeMillis()

    assertEquals(validUser.userId, updatedUser.userId)
    assertEquals(validUser.email, updatedUser.email)
    assertEquals(validUser.firstName, updatedUser.firstName)
    assertEquals(validUser.lastName, updatedUser.lastName)
    assertEquals(validUser.university, updatedUser.university)
    assertEquals(validUser.hobbies, updatedUser.hobbies)
    assertEquals(validUser.profilePictureUrl, updatedUser.profilePictureUrl)
    assertEquals(validUser.bio, updatedUser.bio)
    assertEquals(validUser.createdAt, updatedUser.createdAt)
    assertTrue(updatedUser.updatedAt >= beforeUpdate)
    assertTrue(updatedUser.updatedAt <= afterUpdate)
  }

  @Test
  fun testToMap() {
    val map = validUser.toMap()

    assertEquals("user123", map["userId"])
    assertEquals("test@epfl.ch", map["email"])
    assertEquals("John", map["firstName"])
    assertEquals("Doe", map["lastName"])
    assertEquals("EPFL", map["university"])
    assertEquals(listOf("Football", "Gaming"), map["hobbies"])
    assertEquals("https://example.com/pic.jpg", map["profilePictureUrl"])
    assertEquals("Computer science student", map["bio"])
    assertEquals(1000L, map["createdAt"])
    assertEquals(1000L, map["updatedAt"])
  }

  @Test
  fun testToMapWithNullValues() {
    val user =
        User(
            userId = "user456",
            email = "test@unil.ch",
            firstName = "Jane",
            lastName = "Smith",
            university = "UNIL",
            createdAt = 1000L,
            updatedAt = 1000L)
    val map = user.toMap()

    assertEquals("user456", map["userId"])
    assertEquals("test@unil.ch", map["email"])
    assertEquals("Jane", map["firstName"])
    assertEquals("Smith", map["lastName"])
    assertEquals("UNIL", map["university"])
    assertEquals(emptyList<String>(), map["hobbies"])
    assertNull(map["profilePictureUrl"])
    assertNull(map["bio"])
    assertEquals(1000L, map["createdAt"])
    assertEquals(1000L, map["updatedAt"])
  }

  @Test
  fun testFromMap() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@epfl.ch",
            "firstName" to "John",
            "lastName" to "Doe",
            "university" to "EPFL",
            "hobbies" to listOf("Football", "Gaming"),
            "profilePictureUrl" to "https://example.com/pic.jpg",
            "bio" to "Computer science student",
            "createdAt" to 1000L,
            "updatedAt" to 1000L)

    val user = User.fromMap(map)

    assertNotNull(user)
    assertEquals("user123", user!!.userId)
    assertEquals("test@epfl.ch", user.email)
    assertEquals("John", user.firstName)
    assertEquals("Doe", user.lastName)
    assertEquals("EPFL", user.university)
    assertEquals(2, user.hobbies.size)
    assertEquals("Football", user.hobbies[0])
    assertEquals("Gaming", user.hobbies[1])
    assertEquals("https://example.com/pic.jpg", user.profilePictureUrl)
    assertEquals("Computer science student", user.bio)
    assertEquals(1000L, user.createdAt)
    assertEquals(1000L, user.updatedAt)
  }

  @Test
  fun testFromMapWithMissingOptionalFields() {
    val map =
        mapOf(
            "userId" to "user456",
            "email" to "test@unil.ch",
            "firstName" to "Jane",
            "lastName" to "Smith",
            "university" to "UNIL")

    val user = User.fromMap(map)

    assertNotNull(user)
    assertEquals("user456", user!!.userId)
    assertEquals("test@unil.ch", user.email)
    assertEquals("Jane", user.firstName)
    assertEquals("Smith", user.lastName)
    assertEquals("UNIL", user.university)
    assertEquals(0, user.hobbies.size)
    assertNull(user.profilePictureUrl)
    assertNull(user.bio)
    assertTrue(user.createdAt > 0)
    assertTrue(user.updatedAt > 0)
  }

  @Test
  fun testFromMapWithInvalidHobbies() {
    val map =
        mapOf(
            "userId" to "user789",
            "email" to "test@ethz.ch",
            "firstName" to "Alice",
            "lastName" to "Johnson",
            "university" to "ETHZ",
            "hobbies" to listOf("Reading", 123, null, "Swimming"), // Mixed types
            "createdAt" to 1000L,
            "updatedAt" to 1000L)

    val user = User.fromMap(map)

    assertNotNull(user)
    assertEquals(2, user!!.hobbies.size) // Only strings should be included
    assertEquals("Reading", user.hobbies[0])
    assertEquals("Swimming", user.hobbies[1])
  }

  @Test
  fun testFromMapWithNumericTimestamps() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@epfl.ch",
            "firstName" to "John",
            "lastName" to "Doe",
            "university" to "EPFL",
            "createdAt" to 1000,
            "updatedAt" to 2000)

    val user = User.fromMap(map)

    assertNotNull(user)
    assertEquals(1000L, user!!.createdAt)
    assertEquals(2000L, user.updatedAt)
  }

  @Test
  fun testFromMapWithMissingUid() {
    val map =
        mapOf(
            "email" to "test@epfl.ch",
            "firstName" to "John",
            "lastName" to "Doe",
            "university" to "EPFL")

    val user = User.fromMap(map)
    assertNull(user)
  }

  @Test
  fun testFromMapWithMissingEmail() {
    val map =
        mapOf(
            "userId" to "user123",
            "firstName" to "John",
            "lastName" to "Doe",
            "university" to "EPFL")

    val user = User.fromMap(map)
    assertNull(user)
  }

  @Test
  fun testFromMapWithMissingFirstName() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@epfl.ch",
            "lastName" to "Doe",
            "university" to "EPFL")

    val user = User.fromMap(map)
    assertNull(user)
  }

  @Test
  fun testFromMapWithMissingLastName() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@epfl.ch",
            "firstName" to "John",
            "university" to "EPFL")

    val user = User.fromMap(map)
    assertNull(user)
  }

  @Test
  fun testFromMapWithMissingUniversity() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@epfl.ch",
            "firstName" to "John",
            "lastName" to "Doe")

    val user = User.fromMap(map)
    assertNull(user)
  }

  @Test
  fun testFromMapWithInvalidData() {
    val map =
        mapOf(
            "userId" to "",
            "email" to "invalid-email",
            "firstName" to "John",
            "lastName" to "Doe",
            "university" to "EPFL")

    val user = User.fromMap(map)
    assertNull(user) // Should return null due to invalid data
  }

  @Test
  fun testFromMapWithEmptyMap() {
    val user = User.fromMap(emptyMap())
    assertNull(user)
  }

  @Test
  fun testFromMapAndBackToMap() {
    val originalMap =
        mapOf(
            "userId" to "user123",
            "email" to "test@epfl.ch",
            "firstName" to "John",
            "lastName" to "Doe",
            "university" to "EPFL",
            "hobbies" to listOf("Football", "Gaming"),
            "profilePictureUrl" to "https://example.com/pic.jpg",
            "bio" to "Computer science student",
            "createdAt" to 1000L,
            "updatedAt" to 1000L)

    val user = User.fromMap(originalMap)
    assertNotNull(user)

    val resultMap = user!!.toMap()
    assertEquals(originalMap["userId"], resultMap["userId"])
    assertEquals(originalMap["email"], resultMap["email"])
    assertEquals(originalMap["firstName"], resultMap["firstName"])
    assertEquals(originalMap["lastName"], resultMap["lastName"])
    assertEquals(originalMap["university"], resultMap["university"])
    assertEquals(originalMap["hobbies"], resultMap["hobbies"])
    assertEquals(originalMap["profilePictureUrl"], resultMap["profilePictureUrl"])
    assertEquals(originalMap["bio"], resultMap["bio"])
    assertEquals(originalMap["createdAt"], resultMap["createdAt"])
    assertEquals(originalMap["updatedAt"], resultMap["updatedAt"])
  }

  @Test
  fun testUserEquality() {
    val user1 =
        User(
            userId = "user123",
            email = "test@epfl.ch",
            firstName = "John",
            lastName = "Doe",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    val user2 =
        User(
            userId = "user123",
            email = "test@epfl.ch",
            firstName = "John",
            lastName = "Doe",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    assertEquals(user1, user2)
    assertEquals(user1.hashCode(), user2.hashCode())
  }

  @Test
  fun testUserInequality() {
    val user1 =
        User(
            userId = "user123",
            email = "test@epfl.ch",
            firstName = "John",
            lastName = "Doe",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    val user2 =
        User(
            userId = "user456",
            email = "test@unil.ch",
            firstName = "Jane",
            lastName = "Smith",
            university = "UNIL",
            createdAt = 1000L,
            updatedAt = 1000L)

    assertNotEquals(user1, user2)
  }

  @Test
  fun testCopyFunction() {
    val copiedUser = validUser.copy(firstName = "Jack")

    assertEquals("Jack", copiedUser.firstName)
    assertEquals(validUser.userId, copiedUser.userId)
    assertEquals(validUser.email, copiedUser.email)
    assertEquals(validUser.lastName, copiedUser.lastName)
    assertEquals(validUser.university, copiedUser.university)
  }
}
