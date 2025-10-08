package com.github.se.studentconnect.model.user

import org.junit.Assert
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
          updatedAt = 1000L
      )

  @Test
  fun testUserCreation() {
      Assert.assertNotNull(validUser)
      Assert.assertEquals("user123", validUser.userId)
      Assert.assertEquals("test@epfl.ch", validUser.email)
      Assert.assertEquals("John", validUser.firstName)
      Assert.assertEquals("Doe", validUser.lastName)
      Assert.assertEquals("EPFL", validUser.university)
      Assert.assertEquals(2, validUser.hobbies.size)
      Assert.assertEquals("Football", validUser.hobbies[0])
      Assert.assertEquals("Gaming", validUser.hobbies[1])
      Assert.assertEquals("https://example.com/pic.jpg", validUser.profilePictureUrl)
      Assert.assertEquals("Computer science student", validUser.bio)
      Assert.assertEquals(1000L, validUser.createdAt)
      Assert.assertEquals(1000L, validUser.updatedAt)
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
            updatedAt = 2000L
        )
      Assert.assertEquals(0, user.hobbies.size)
      Assert.assertNull(user.profilePictureUrl)
      Assert.assertNull(user.bio)
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
            university = "ETHZ"
        )
    val afterCreation = System.currentTimeMillis()

      Assert.assertTrue(user.createdAt >= beforeCreation)
      Assert.assertTrue(user.createdAt <= afterCreation)
      Assert.assertTrue(user.updatedAt >= beforeCreation)
      Assert.assertTrue(user.updatedAt <= afterCreation)
      Assert.assertEquals(user.createdAt, user.updatedAt)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankUid() {
      User(
          userId = "",
          email = "test@epfl.ch",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL"
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankEmail() {
      User(
          userId = "user123",
          email = "",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL"
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithInvalidEmail() {
      User(
          userId = "user123",
          email = "invalid-email",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL"
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankFirstName() {
      User(
          userId = "user123",
          email = "test@epfl.ch",
          firstName = "",
          lastName = "Doe",
          university = "EPFL"
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankLastName() {
      User(
          userId = "user123",
          email = "test@epfl.ch",
          firstName = "John",
          lastName = "",
          university = "EPFL"
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithBlankUniversity() {
      User(
          userId = "user123",
          email = "test@epfl.ch",
          firstName = "John",
          lastName = "Doe",
          university = ""
      )
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
          updatedAt = 1000L
      )
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
          updatedAt = -1L
      )
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
          updatedAt = 1000L
      )
  }

  @Test
  fun testGetFullName() {
      Assert.assertEquals("John Doe", validUser.getFullName())
  }

  @Test
  fun testHasProfilePicture() {
      Assert.assertTrue(validUser.hasProfilePicture())

    val userWithoutPicture =
        User(
            userId = "user456",
            email = "test@unil.ch",
            firstName = "Jane",
            lastName = "Smith",
            university = "UNIL",
            profilePictureUrl = null,
            createdAt = 1000L,
            updatedAt = 1000L
        )
      Assert.assertFalse(userWithoutPicture.hasProfilePicture())

    val userWithBlankPicture =
        User(
            userId = "user789",
            email = "test@ethz.ch",
            firstName = "Alice",
            lastName = "Johnson",
            university = "ETHZ",
            profilePictureUrl = "",
            createdAt = 1000L,
            updatedAt = 1000L
        )
      Assert.assertFalse(userWithBlankPicture.hasProfilePicture())
  }

  @Test
  fun testHasBio() {
      Assert.assertTrue(validUser.hasBio())

    val userWithoutBio =
        User(
            userId = "user456",
            email = "test@unil.ch",
            firstName = "Jane",
            lastName = "Smith",
            university = "UNIL",
            bio = null,
            createdAt = 1000L,
            updatedAt = 1000L
        )
      Assert.assertFalse(userWithoutBio.hasBio())

    val userWithBlankBio =
        User(
            userId = "user789",
            email = "test@ethz.ch",
            firstName = "Alice",
            lastName = "Johnson",
            university = "ETHZ",
            bio = "",
            createdAt = 1000L,
            updatedAt = 1000L
        )
      Assert.assertFalse(userWithBlankBio.hasBio())
  }

  @Test
  fun testUpdateEmail() {
    val beforeUpdate = System.currentTimeMillis()
    val updatedUser = validUser.update(email = User.UpdateValue.SetValue("newemail@epfl.ch"))
    val afterUpdate = System.currentTimeMillis()

      Assert.assertEquals("newemail@epfl.ch", updatedUser.email)
      Assert.assertEquals(validUser.firstName, updatedUser.firstName)
      Assert.assertEquals(validUser.lastName, updatedUser.lastName)
      Assert.assertEquals(validUser.university, updatedUser.university)
      Assert.assertEquals(validUser.hobbies, updatedUser.hobbies)
      Assert.assertEquals(validUser.createdAt, updatedUser.createdAt)
      Assert.assertTrue(updatedUser.updatedAt >= beforeUpdate)
      Assert.assertTrue(updatedUser.updatedAt <= afterUpdate)
      Assert.assertTrue(updatedUser.updatedAt > validUser.updatedAt)
  }

  @Test
  fun testUpdateFirstName() {
    val updatedUser = validUser.update(firstName = User.UpdateValue.SetValue("Jack"))
      Assert.assertEquals("Jack", updatedUser.firstName)
      Assert.assertEquals(validUser.email, updatedUser.email)
  }

  @Test
  fun testUpdateLastName() {
    val updatedUser = validUser.update(lastName = User.UpdateValue.SetValue("Brown"))
      Assert.assertEquals("Brown", updatedUser.lastName)
      Assert.assertEquals(validUser.firstName, updatedUser.firstName)
  }

  @Test
  fun testUpdateUniversity() {
    val updatedUser = validUser.update(university = User.UpdateValue.SetValue("UNIL"))
      Assert.assertEquals("UNIL", updatedUser.university)
      Assert.assertEquals(validUser.firstName, updatedUser.firstName)
  }

  @Test
  fun testUpdateHobbies() {
    val newHobbies = listOf("Reading", "Swimming", "Cooking")
    val updatedUser = validUser.update(hobbies = User.UpdateValue.SetValue(newHobbies))
      Assert.assertEquals(newHobbies, updatedUser.hobbies)
      Assert.assertEquals(3, updatedUser.hobbies.size)
      Assert.assertEquals(validUser.firstName, updatedUser.firstName)
  }

  @Test
  fun testUpdateProfilePictureUrl() {
    val newUrl = "https://example.com/newpic.jpg"
    val updatedUser = validUser.update(profilePictureUrl = User.UpdateValue.SetValue(newUrl))
      Assert.assertEquals(newUrl, updatedUser.profilePictureUrl)
      Assert.assertEquals(validUser.email, updatedUser.email)
  }

  @Test
  fun testUpdateBio() {
    val newBio = "Engineering student passionate about AI"
    val updatedUser = validUser.update(bio = User.UpdateValue.SetValue(newBio))
      Assert.assertEquals(newBio, updatedUser.bio)
      Assert.assertEquals(validUser.email, updatedUser.email)
  }

  @Test
  fun testUpdateMultipleFields() {
    val updatedUser =
        validUser.update(
            email = User.UpdateValue.SetValue("newemail@epfl.ch"),
            firstName = User.UpdateValue.SetValue("Jack"),
            hobbies = User.UpdateValue.SetValue(listOf("Reading")))
      Assert.assertEquals("newemail@epfl.ch", updatedUser.email)
      Assert.assertEquals("Jack", updatedUser.firstName)
      Assert.assertEquals(1, updatedUser.hobbies.size)
      Assert.assertEquals("Reading", updatedUser.hobbies[0])
      Assert.assertEquals(validUser.lastName, updatedUser.lastName)
      Assert.assertEquals(validUser.university, updatedUser.university)
  }

  @Test
  fun testUpdateWithNoChanges() {
    val beforeUpdate = System.currentTimeMillis()
    val updatedUser = validUser.update()
    val afterUpdate = System.currentTimeMillis()

      Assert.assertEquals(validUser.userId, updatedUser.userId)
      Assert.assertEquals(validUser.email, updatedUser.email)
      Assert.assertEquals(validUser.firstName, updatedUser.firstName)
      Assert.assertEquals(validUser.lastName, updatedUser.lastName)
      Assert.assertEquals(validUser.university, updatedUser.university)
      Assert.assertEquals(validUser.hobbies, updatedUser.hobbies)
      Assert.assertEquals(validUser.profilePictureUrl, updatedUser.profilePictureUrl)
      Assert.assertEquals(validUser.bio, updatedUser.bio)
      Assert.assertEquals(validUser.createdAt, updatedUser.createdAt)
      Assert.assertTrue(updatedUser.updatedAt >= beforeUpdate)
      Assert.assertTrue(updatedUser.updatedAt <= afterUpdate)
  }

  @Test
  fun testToMap() {
    val map = validUser.toMap()

      Assert.assertEquals("user123", map["userId"])
      Assert.assertEquals("test@epfl.ch", map["email"])
      Assert.assertEquals("John", map["firstName"])
      Assert.assertEquals("Doe", map["lastName"])
      Assert.assertEquals("EPFL", map["university"])
      Assert.assertEquals(listOf("Football", "Gaming"), map["hobbies"])
      Assert.assertEquals("https://example.com/pic.jpg", map["profilePictureUrl"])
      Assert.assertEquals("Computer science student", map["bio"])
      Assert.assertEquals(1000L, map["createdAt"])
      Assert.assertEquals(1000L, map["updatedAt"])
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
            updatedAt = 1000L
        )
    val map = user.toMap()

      Assert.assertEquals("user456", map["userId"])
      Assert.assertEquals("test@unil.ch", map["email"])
      Assert.assertEquals("Jane", map["firstName"])
      Assert.assertEquals("Smith", map["lastName"])
      Assert.assertEquals("UNIL", map["university"])
      Assert.assertEquals(emptyList<String>(), map["hobbies"])
      Assert.assertNull(map["profilePictureUrl"])
      Assert.assertNull(map["bio"])
      Assert.assertEquals(1000L, map["createdAt"])
      Assert.assertEquals(1000L, map["updatedAt"])
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

      Assert.assertNotNull(user)
      Assert.assertEquals("user123", user!!.userId)
      Assert.assertEquals("test@epfl.ch", user.email)
      Assert.assertEquals("John", user.firstName)
      Assert.assertEquals("Doe", user.lastName)
      Assert.assertEquals("EPFL", user.university)
      Assert.assertEquals(2, user.hobbies.size)
      Assert.assertEquals("Football", user.hobbies[0])
      Assert.assertEquals("Gaming", user.hobbies[1])
      Assert.assertEquals("https://example.com/pic.jpg", user.profilePictureUrl)
      Assert.assertEquals("Computer science student", user.bio)
      Assert.assertEquals(1000L, user.createdAt)
      Assert.assertEquals(1000L, user.updatedAt)
  }

  @Test
  fun testFromMapWithMissingOptionalFields() {
    val now = System.currentTimeMillis()
    val map =
        mapOf(
            "userId" to "user456",
            "email" to "test@unil.ch",
            "firstName" to "Jane",
            "lastName" to "Smith",
            "university" to "UNIL",
            "updatedAt" to now)

    val user = User.fromMap(map)

      Assert.assertNotNull(user)
      Assert.assertEquals("user456", user!!.userId)
      Assert.assertEquals("test@unil.ch", user.email)
      Assert.assertEquals("Jane", user.firstName)
      Assert.assertEquals("Smith", user.lastName)
      Assert.assertEquals("UNIL", user.university)
      Assert.assertEquals(0, user.hobbies.size)
      Assert.assertNull(user.profilePictureUrl)
      Assert.assertNull(user.bio)
      Assert.assertTrue(user.createdAt > 0)
      Assert.assertEquals(now, user.updatedAt)
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

      Assert.assertNotNull(user)
      Assert.assertEquals(2, user!!.hobbies.size) // Only strings should be included
      Assert.assertEquals("Reading", user.hobbies[0])
      Assert.assertEquals("Swimming", user.hobbies[1])
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

      Assert.assertNotNull(user)
      Assert.assertEquals(1000L, user!!.createdAt)
      Assert.assertEquals(2000L, user.updatedAt)
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
      Assert.assertNull(user)
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
      Assert.assertNull(user)
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
      Assert.assertNull(user)
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
      Assert.assertNull(user)
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
      Assert.assertNull(user)
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
      Assert.assertNull(user) // Should return null due to invalid data
  }

  @Test
  fun testFromMapWithEmptyMap() {
    val user = User.fromMap(emptyMap())
      Assert.assertNull(user)
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
      Assert.assertNotNull(user)

    val resultMap = user!!.toMap()
      Assert.assertEquals(originalMap["userId"], resultMap["userId"])
      Assert.assertEquals(originalMap["email"], resultMap["email"])
      Assert.assertEquals(originalMap["firstName"], resultMap["firstName"])
      Assert.assertEquals(originalMap["lastName"], resultMap["lastName"])
      Assert.assertEquals(originalMap["university"], resultMap["university"])
      Assert.assertEquals(originalMap["hobbies"], resultMap["hobbies"])
      Assert.assertEquals(originalMap["profilePictureUrl"], resultMap["profilePictureUrl"])
      Assert.assertEquals(originalMap["bio"], resultMap["bio"])
      Assert.assertEquals(originalMap["createdAt"], resultMap["createdAt"])
      Assert.assertEquals(originalMap["updatedAt"], resultMap["updatedAt"])
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
            updatedAt = 1000L
        )

    val user2 =
        User(
            userId = "user123",
            email = "test@epfl.ch",
            firstName = "John",
            lastName = "Doe",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L
        )

      Assert.assertEquals(user1, user2)
      Assert.assertEquals(user1.hashCode(), user2.hashCode())
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
            updatedAt = 1000L
        )

    val user2 =
        User(
            userId = "user456",
            email = "test@unil.ch",
            firstName = "Jane",
            lastName = "Smith",
            university = "UNIL",
            createdAt = 1000L,
            updatedAt = 1000L
        )

      Assert.assertNotEquals(user1, user2)
  }

  @Test
  fun testCopyFunction() {
    val copiedUser = validUser.copy(firstName = "Jack")

      Assert.assertEquals("Jack", copiedUser.firstName)
      Assert.assertEquals(validUser.userId, copiedUser.userId)
      Assert.assertEquals(validUser.email, copiedUser.email)
      Assert.assertEquals(validUser.lastName, copiedUser.lastName)
      Assert.assertEquals(validUser.university, copiedUser.university)
  }

  // Edge case tests for extremely long strings
  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithVeryLongFirstName() {
    val longName = "a".repeat(101) // 101 characters
      User(
          userId = "user123",
          email = "test@epfl.ch",
          firstName = longName,
          lastName = "Doe",
          university = "EPFL"
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithVeryLongLastName() {
    val longName = "a".repeat(101) // 101 characters
      User(
          userId = "user123",
          email = "test@epfl.ch",
          firstName = "John",
          lastName = longName,
          university = "EPFL"
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithVeryLongUniversity() {
    val longUniversity = "a".repeat(201) // 201 characters
      User(
          userId = "user123",
          email = "test@epfl.ch",
          firstName = "John",
          lastName = "Doe",
          university = longUniversity
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithVeryLongBio() {
    val longBio = "a".repeat(501) // 501 characters
      User(
          userId = "user123",
          email = "test@epfl.ch",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL",
          bio = longBio
      )
  }

  @Test
  fun testUserCreationWithMaxLengthFields() {
    val maxFirstName = "a".repeat(100)
    val maxLastName = "b".repeat(100)
    val maxUniversity = "c".repeat(200)
    val maxBio = "d".repeat(500)

    val user =
        User(
            userId = "user123",
            email = "test@epfl.ch",
            firstName = maxFirstName,
            lastName = maxLastName,
            university = maxUniversity,
            bio = maxBio
        )

      Assert.assertEquals(maxFirstName, user.firstName)
      Assert.assertEquals(maxLastName, user.lastName)
      Assert.assertEquals(maxUniversity, user.university)
      Assert.assertEquals(maxBio, user.bio)
  }

  // Edge case tests for special characters
  @Test
  fun testUserCreationWithSpecialCharactersInNames() {
    val user =
        User(
            userId = "user123",
            email = "test@epfl.ch",
            firstName = "José-María",
            lastName = "O'Connor-Smith",
            university = "École Polytechnique Fédérale de Lausanne"
        )

      Assert.assertEquals("José-María", user.firstName)
      Assert.assertEquals("O'Connor-Smith", user.lastName)
      Assert.assertEquals("École Polytechnique Fédérale de Lausanne", user.university)
  }

  @Test
  fun testUserCreationWithUnicodeCharacters() {
    val user =
        User(
            userId = "user123",
            email = "test@epfl.ch",
            firstName = "张三",
            lastName = "李四",
            university = "苏黎世联邦理工学院"
        )

      Assert.assertEquals("张三", user.firstName)
      Assert.assertEquals("李四", user.lastName)
      Assert.assertEquals("苏黎世联邦理工学院", user.university)
  }

  // Edge case tests for email validation
  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithMultipleAtSymbols() {
      User(
          userId = "user123",
          email = "test@@epfl.ch",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL"
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithInvalidEmailDomain() {
      User(
          userId = "user123",
          email = "test@",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL"
      )
  }

  @Test(expected = IllegalArgumentException::class)
  fun testUserCreationWithInvalidEmailMissingTld() {
      User(
          userId = "user123",
          email = "test@epfl",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL"
      )
  }

  @Test
  fun testUserCreationWithValidComplexEmails() {
    val emails =
        listOf(
            "test.user+tag@epfl.ch",
            "user.name-123@university.edu",
            "a@b.co",
            "very.long.email.address@very-long-domain.university.edu")

    emails.forEach { email ->
      val user =
          User(
              userId = "user123",
              email = email,
              firstName = "John",
              lastName = "Doe",
              university = "EPFL"
          )
        Assert.assertEquals(email, user.email)
    }
  }

  // Timestamp ordering edge cases
  @Test
  fun testTimestampOrdering() {
    val now = System.currentTimeMillis()
    val user1 =
        User(
            userId = "user1",
            email = "test1@epfl.ch",
            firstName = "John",
            lastName = "Doe",
            university = "EPFL",
            createdAt = now,
            updatedAt = now
        )

    Thread.sleep(1) // Ensure different timestamps

    val user2 =
        User(
            userId = "user2",
            email = "test2@epfl.ch",
            firstName = "Jane",
            lastName = "Doe",
            university = "EPFL",
            createdAt = now + 1,
            updatedAt = now + 1
        )

      Assert.assertTrue(user2.createdAt > user1.createdAt)
      Assert.assertTrue(user2.updatedAt > user1.updatedAt)
  }

  // Test fromMap with missing updatedAt (should fail with new implementation)
  @Test
  fun testFromMapWithMissingUpdatedAt() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@epfl.ch",
            "firstName" to "John",
            "lastName" to "Doe",
            "university" to "EPFL",
            "createdAt" to 1000L
            // missing updatedAt
            )

    val user = User.fromMap(map)
      Assert.assertNull(user) // Should fail due to missing updatedAt
  }

  // Test UpdateValue functionality
  @Test
  fun testUpdateValueSetToNull() {
    val user =
        validUser.update(
            profilePictureUrl = User.UpdateValue.SetValue(null),
            bio = User.UpdateValue.SetValue(null))

      Assert.assertNull(user.profilePictureUrl)
      Assert.assertNull(user.bio)
      Assert.assertEquals(validUser.firstName, user.firstName)
  }

  @Test
  fun testUpdateValueNoChange() {
    val user =
        validUser.update(
            profilePictureUrl = User.UpdateValue.NoChange(), bio = User.UpdateValue.NoChange())

      Assert.assertEquals(validUser.profilePictureUrl, user.profilePictureUrl)
      Assert.assertEquals(validUser.bio, user.bio)
  }

  @Test
  fun testUpdateValueSetNewValue() {
    val newUrl = "https://new.example.com/pic.jpg"
    val newBio = "New bio"

    val user =
        validUser.update(
            profilePictureUrl = User.UpdateValue.SetValue(newUrl),
            bio = User.UpdateValue.SetValue(newBio))

      Assert.assertEquals(newUrl, user.profilePictureUrl)
      Assert.assertEquals(newBio, user.bio)
  }
}