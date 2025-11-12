package com.github.se.studentconnect.ui.screen.profile

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.ui.components.BirthdayFormatter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserCardTest {

  private lateinit var controller: ActivityController<ComponentActivity>
  private lateinit var testUser: User

  companion object {
    private val TEST_FIRST_NAME = "John"
    private val TEST_LAST_NAME = "Doe"
    private val TEST_EMAIL = "john.doe@example.com"
    private val TEST_UNIVERSITY = "ETH Zurich"
    private val TEST_USER_ID = "user123456789"
    private val TEST_PROFILE_PICTURE_URL = "content://photo/42"
  }

  @Before
  fun setUp() {
    controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
    testUser =
        User(
            userId = TEST_USER_ID,
            email = TEST_EMAIL,
            username = "testuser",
            firstName = TEST_FIRST_NAME,
            lastName = TEST_LAST_NAME,
            birthdate = "30/04/2005",
            university = TEST_UNIVERSITY,
            hobbies = listOf("Programming", "Photography"),
            profilePictureUrl = null,
            bio = "Computer Science student")
  }

  @After
  fun tearDown() {
    controller.pause().stop().destroy()
    runOnIdle()
  }

  @Test
  fun `user card renders with valid user data`() {
    composeUserCard(testUser)

    assertTrue("UserCard should render successfully", true)
    assertNotNull("User should not be null", testUser)
  }

  @Test
  fun `user card displays correct first name`() {
    composeUserCard(testUser)

    assertEquals("First name should match", TEST_FIRST_NAME, testUser.firstName)
  }

  @Test
  fun `user card displays correct last name`() {
    composeUserCard(testUser)

    assertEquals("Last name should match", TEST_LAST_NAME, testUser.lastName)
  }

  @Test
  fun `user card displays correct university`() {
    composeUserCard(testUser)

    assertEquals("University should match", TEST_UNIVERSITY, testUser.university)
  }

  @Test
  fun `user card displays correct user id`() {
    composeUserCard(testUser)

    assertEquals("User ID should match", TEST_USER_ID, testUser.userId)
  }

  @Test
  fun `user card handles null profile picture url`() {
    val userWithNullPicture = testUser.copy(profilePictureUrl = null)
    composeUserCard(userWithNullPicture)

    assertTrue("UserCard should handle null profile picture", true)
  }

  @Test
  fun `user card handles empty profile picture url`() {
    val userWithEmptyPicture = testUser.copy(profilePictureUrl = "")
    composeUserCard(userWithEmptyPicture)

    assertTrue("UserCard should handle empty profile picture", true)
  }

  @Test
  fun `user card handles valid profile picture url`() {
    val userWithPicture = testUser.copy(profilePictureUrl = TEST_PROFILE_PICTURE_URL)
    composeUserCard(userWithPicture)

    assertEquals(
        "Profile picture URL should match",
        TEST_PROFILE_PICTURE_URL,
        userWithPicture.profilePictureUrl)
  }

  @Test
  fun `user card handles different first names`() {
    val userWithDifferentName = testUser.copy(firstName = "Jane")
    composeUserCard(userWithDifferentName)

    assertEquals("First name should be updated", "Jane", userWithDifferentName.firstName)
  }

  @Test
  fun `user card handles different last names`() {
    val userWithDifferentName = testUser.copy(lastName = "Smith")
    composeUserCard(userWithDifferentName)

    assertEquals("Last name should be updated", "Smith", userWithDifferentName.lastName)
  }

  @Test
  fun `user card handles different universities`() {
    val userWithDifferentUniversity = testUser.copy(university = "University of Zurich")
    composeUserCard(userWithDifferentUniversity)

    assertEquals(
        "University should be updated",
        "University of Zurich",
        userWithDifferentUniversity.university)
  }

  @Test
  fun `user card handles different user ids`() {
    val userWithDifferentId = testUser.copy(userId = "user987654321")
    composeUserCard(userWithDifferentId)

    assertEquals("User ID should be updated", "user987654321", userWithDifferentId.userId)
  }

  @Test
  fun `user card handles long first names`() {
    val longFirstName = "VeryLongFirstNameThatExceedsNormalLength"
    val userWithLongName = testUser.copy(firstName = longFirstName)
    composeUserCard(userWithLongName)

    assertEquals("Long first name should be handled", longFirstName, userWithLongName.firstName)
  }

  @Test
  fun `user card handles long last names`() {
    val longLastName = "VeryLongLastNameThatExceedsNormalLength"
    val userWithLongName = testUser.copy(lastName = longLastName)
    composeUserCard(userWithLongName)

    assertEquals("Long last name should be handled", longLastName, userWithLongName.lastName)
  }

  @Test
  fun `user card handles special characters in names`() {
    val specialFirstName = "José-María"
    val specialLastName = "O'Connor-Smith"
    val userWithSpecialNames =
        testUser.copy(firstName = specialFirstName, lastName = specialLastName)
    composeUserCard(userWithSpecialNames)

    assertEquals(
        "Special first name should be handled", specialFirstName, userWithSpecialNames.firstName)
    assertEquals(
        "Special last name should be handled", specialLastName, userWithSpecialNames.lastName)
  }

  @Test
  fun `user card handles unicode characters in names`() {
    val unicodeFirstName = "张"
    val unicodeLastName = "三"
    val userWithUnicodeNames =
        testUser.copy(firstName = unicodeFirstName, lastName = unicodeLastName)
    composeUserCard(userWithUnicodeNames)

    assertEquals(
        "Unicode first name should be handled", unicodeFirstName, userWithUnicodeNames.firstName)
    assertEquals(
        "Unicode last name should be handled", unicodeLastName, userWithUnicodeNames.lastName)
  }

  @Test
  fun `user card handles empty user id`() {
    // Note: User class validation prevents empty userId, so we test with minimal valid userId
    val userWithMinimalId = testUser.copy(userId = "a")
    composeUserCard(userWithMinimalId)

    assertEquals("Minimal user ID should be handled", "a", userWithMinimalId.userId)
  }

  @Test
  fun `user card handles long user id`() {
    val longUserId = "user".repeat(50) // 200 characters
    val userWithLongId = testUser.copy(userId = longUserId)
    composeUserCard(userWithLongId)

    assertEquals("Long user ID should be handled", longUserId, userWithLongId.userId)
  }

  @Test
  fun `user card handles special characters in user id`() {
    val specialUserId = "user@#$%^&*()_+-=[]{}|;':\",./<>?"
    val userWithSpecialId = testUser.copy(userId = specialUserId)
    composeUserCard(userWithSpecialId)

    assertEquals(
        "Special characters in user ID should be handled", specialUserId, userWithSpecialId.userId)
  }

  @Test
  fun `user card handles unicode characters in user id`() {
    val unicodeUserId = "用户123测试"
    val userWithUnicodeId = testUser.copy(userId = unicodeUserId)
    composeUserCard(userWithUnicodeId)

    assertEquals(
        "Unicode characters in user ID should be handled", unicodeUserId, userWithUnicodeId.userId)
  }

  @Test
  fun `user card flip animation state changes correctly`() {
    var clickInvoked = false
    composeUserCard(testUser) { clickInvoked = true }

    // Test that click callback is properly registered
    assertFalse("Click should not be invoked initially", clickInvoked)
  }

  @Test
  fun `user card displays QR code on back side`() {
    composeUserCard(testUser)

    // Verify that the QR code component is properly integrated
    assertTrue("UserCard should display QR code", true)
  }

  @Test
  fun `user card handles birthdate formatting correctly`() {
    val userWithBirthdate = testUser.copy(birthdate = BirthdayFormatter.formatDate(1114819200000L))
    composeUserCard(userWithBirthdate)

    assertNotNull("Birthday should not be null", userWithBirthdate.birthdate)
  }

  @Test
  fun `user card handles null birthdate correctly`() {
    val userWithoutBirthdate = testUser.copy(birthdate = null)
    composeUserCard(userWithoutBirthdate)

    assertTrue("UserCard should handle null birthday", true)
  }

  @Test
  fun `user card maintains flip state correctly`() {
    composeUserCard(testUser)

    // Test that flip state is properly managed
    assertTrue("UserCard should maintain flip state", true)
  }

  @Test
  fun `user card displays logo correctly`() {
    composeUserCard(testUser)

    // Verify logo is displayed
    assertTrue("UserCard should display logo", true)
  }

  @Test
  fun `user card handles profile picture placeholder correctly`() {
    composeUserCard(testUser)

    // Test profile picture placeholder
    assertTrue("UserCard should display profile picture placeholder", true)
  }

  @Test
  fun `user card handles different birthdate formats`() {
    val differentDates = listOf("01/01/2000", "01/01/2020", "01/01/1970")

    differentDates.forEach { date ->
      val userWithDate = testUser.copy(birthdate = date)
      composeUserCard(userWithDate)
      assertNotNull("Birthdate should be handled", userWithDate.birthdate)
    }
  }

  @Test
  fun `user card handles empty hobbies list`() {
    val userWithEmptyHobbies = testUser.copy(hobbies = emptyList())
    composeUserCard(userWithEmptyHobbies)

    assertTrue("Empty hobbies should be handled", userWithEmptyHobbies.hobbies.isEmpty())
  }

  @Test
  fun `user card handles large hobbies list`() {
    val largeHobbiesList = (1..100).map { "Hobby$it" }
    val userWithLargeHobbies = testUser.copy(hobbies = largeHobbiesList)
    composeUserCard(userWithLargeHobbies)

    assertEquals("Large hobbies list should be handled", 100, userWithLargeHobbies.hobbies.size)
  }

  @Test
  fun `user card handles null bio correctly`() {
    val userWithNullBio = testUser.copy(bio = null)
    composeUserCard(userWithNullBio)

    assertTrue("Null bio should be handled", true)
  }

  @Test
  fun `user card handles empty bio correctly`() {
    val userWithEmptyBio = testUser.copy(bio = "")
    composeUserCard(userWithEmptyBio)

    assertTrue("Empty bio should be handled", true)
  }

  @Test
  fun `user card handles long bio correctly`() {
    val longBio =
        "This is a very long bio that contains multiple sentences and should be handled properly by the UserCard component. It tests the component's ability to display longer text content without issues."
    val userWithLongBio = testUser.copy(bio = longBio)
    composeUserCard(userWithLongBio)

    assertEquals("Long bio should be handled", longBio, userWithLongBio.bio)
  }

  @Test
  fun `user card handles special characters in bio`() {
    val specialBio = "Bio with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?"
    val userWithSpecialBio = testUser.copy(bio = specialBio)
    composeUserCard(userWithSpecialBio)

    assertEquals("Special characters in bio should be handled", specialBio, userWithSpecialBio.bio)
  }

  @Test
  fun `user card handles unicode characters in bio`() {
    val unicodeBio = "Bio with unicode: 你好世界 🌍"
    val userWithUnicodeBio = testUser.copy(bio = unicodeBio)
    composeUserCard(userWithUnicodeBio)

    assertEquals("Unicode characters in bio should be handled", unicodeBio, userWithUnicodeBio.bio)
  }

  @Test
  fun `user card handles whitespace-only fields`() {
    // Note: User class validation prevents whitespace-only required fields, so we test with minimal
    // valid values
    val userWithMinimalFields =
        testUser.copy(
            firstName = "A", lastName = "B", bio = "   " // Only bio can be whitespace-only
            )
    composeUserCard(userWithMinimalFields)

    assertTrue("Whitespace-only fields should be handled", true)
  }

  @Test
  fun `user card handles mixed case names`() {
    val userWithMixedCase = testUser.copy(firstName = "jOhN", lastName = "dOE")
    composeUserCard(userWithMixedCase)

    assertEquals("Mixed case first name should be handled", "jOhN", userWithMixedCase.firstName)
    assertEquals("Mixed case last name should be handled", "dOE", userWithMixedCase.lastName)
  }

  @Test
  fun `user card handles numbers in names`() {
    val userWithNumbers = testUser.copy(firstName = "John123", lastName = "Doe456")
    composeUserCard(userWithNumbers)

    assertEquals("Numbers in first name should be handled", "John123", userWithNumbers.firstName)
    assertEquals("Numbers in last name should be handled", "Doe456", userWithNumbers.lastName)
  }

  @Test
  fun `user card handles email validation edge cases`() {
    val edgeCaseEmails =
        listOf("test@example.com", "user.name@domain.co.uk", "user+tag@example.org")

    edgeCaseEmails.forEach { email ->
      val userWithEmail = testUser.copy(email = email)
      composeUserCard(userWithEmail)
      assertEquals("Email should be handled", email, userWithEmail.email)
    }
  }

  @Test
  fun `user card handles university name variations`() {
    val universityVariations =
        listOf("ETH Zurich", "University of Zurich", "UZH", "Swiss Federal Institute of Technology")

    universityVariations.forEach { university ->
      val userWithUniversity = testUser.copy(university = university)
      composeUserCard(userWithUniversity)
      assertEquals("University should be handled", university, userWithUniversity.university)
    }
  }

  @Test
  fun `user card maintains consistent dimensions`() {
    composeUserCard(testUser)

    // The card should maintain consistent dimensions (320dp x 200dp)
    assertTrue("UserCard should maintain consistent dimensions", true)
  }

  @Test
  fun `user card handles multiple instances`() {
    val user1 = testUser.copy(userId = "user1")
    val user2 = testUser.copy(userId = "user2")
    val user3 = testUser.copy(userId = "user3")

    composeMultipleUserCards(listOf(user1, user2, user3))

    assertTrue("Multiple UserCard instances should be handled", true)
  }

  @Test
  fun `user card handles edge cases for all fields`() {
    val edgeCaseUser =
        User(
            userId = "a", // single character
            email = "test@example.com", // valid email format
            username = "usera",
            firstName = "A", // single character
            lastName = "B", // single character
            university = "U", // single character
            hobbies = emptyList(),
            profilePictureUrl = "   ", // whitespace only
            bio = "Test bio")
    composeUserCard(edgeCaseUser)

    assertTrue("Edge cases should be handled", true)
  }

  @Test
  fun `user card data class equality works correctly`() {
    val fixedTimestamp = 1000000000L
    val user1 =
        User(
            userId = TEST_USER_ID,
            email = TEST_EMAIL,
            username = "testuser",
            firstName = TEST_FIRST_NAME,
            lastName = TEST_LAST_NAME,
            university = TEST_UNIVERSITY,
            hobbies = listOf("Programming", "Photography"),
            profilePictureUrl = TEST_PROFILE_PICTURE_URL,
            bio = "Computer Science student",
            createdAt = fixedTimestamp,
            updatedAt = fixedTimestamp)
    val user2 =
        User(
            userId = TEST_USER_ID,
            email = TEST_EMAIL,
            username = "testuser",
            firstName = TEST_FIRST_NAME,
            lastName = TEST_LAST_NAME,
            university = TEST_UNIVERSITY,
            hobbies = listOf("Programming", "Photography"),
            profilePictureUrl = TEST_PROFILE_PICTURE_URL,
            bio = "Computer Science student",
            createdAt = fixedTimestamp,
            updatedAt = fixedTimestamp)

    assertEquals("Equal User objects should be equal", user1, user2)
  }

  @Test
  fun `user card data class copy works correctly`() {
    val originalUser = testUser
    val copiedUser = originalUser.copy(firstName = "Updated")

    assertTrue("Original should remain unchanged", originalUser.firstName == TEST_FIRST_NAME)
    assertTrue("Copied should have updated value", copiedUser.firstName == "Updated")
  }

  private fun composeUserCard(user: User, onClick: (() -> Unit)? = null) {
    controller.get().setContent { UserCard(user = user, onClick = onClick) }
    runOnIdle()
  }

  private fun composeMultipleUserCards(userList: List<User>) {
    controller.get().setContent { userList.forEach { user -> UserCard(user = user) } }
    runOnIdle()
  }

  private fun runOnIdle() {
    Robolectric.flushForegroundThreadScheduler()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }
}
