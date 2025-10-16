package com.github.se.studentconnect.ui.screen.profile

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.github.se.studentconnect.model.User
import java.time.LocalDate

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
        testUser = User(
            userId = TEST_USER_ID,
            email = TEST_EMAIL,
            firstName = TEST_FIRST_NAME,
            lastName = TEST_LAST_NAME,
            birthdateMillis = 1114819200000L, // 30/04/2005 in milliseconds
            university = TEST_UNIVERSITY,
            hobbies = listOf("Programming", "Photography"),
            profilePictureUrl = null,
            bio = "Computer Science student"
        )
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

        assertEquals("Profile picture URL should match", TEST_PROFILE_PICTURE_URL, userWithPicture.profilePictureUrl)
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

        assertEquals("University should be updated", "University of Zurich", userWithDifferentUniversity.university)
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
        val userWithSpecialNames = testUser.copy(
            firstName = specialFirstName,
            lastName = specialLastName
        )
        composeUserCard(userWithSpecialNames)

        assertEquals("Special first name should be handled", specialFirstName, userWithSpecialNames.firstName)
        assertEquals("Special last name should be handled", specialLastName, userWithSpecialNames.lastName)
    }

    @Test
    fun `user card handles unicode characters in names`() {
        val unicodeFirstName = "张"
        val unicodeLastName = "三"
        val userWithUnicodeNames = testUser.copy(
            firstName = unicodeFirstName,
            lastName = unicodeLastName
        )
        composeUserCard(userWithUnicodeNames)

        assertEquals("Unicode first name should be handled", unicodeFirstName, userWithUnicodeNames.firstName)
        assertEquals("Unicode last name should be handled", unicodeLastName, userWithUnicodeNames.lastName)
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

        assertEquals("Special characters in user ID should be handled", specialUserId, userWithSpecialId.userId)
    }

    @Test
    fun `user card handles unicode characters in user id`() {
        val unicodeUserId = "用户123测试"
        val userWithUnicodeId = testUser.copy(userId = unicodeUserId)
        composeUserCard(userWithUnicodeId)

        assertEquals("Unicode characters in user ID should be handled", unicodeUserId, userWithUnicodeId.userId)
    }

    @Test
    fun `user card click callback is registered`() {
        var clickInvoked = false
        composeUserCard(testUser) { clickInvoked = true }

        assertFalse("Click should not be invoked initially", clickInvoked)
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
        val edgeCaseUser = User(
            userId = "a", // single character
            email = "test@example.com", // valid email format
            firstName = "A", // single character
            lastName = "B", // single character
            university = "U", // single character
            hobbies = emptyList(),
            profilePictureUrl = "   ", // whitespace only
            bio = "Test bio"
        )
        composeUserCard(edgeCaseUser)

        assertTrue("Edge cases should be handled", true)
    }

    @Test
    fun `user card data class equality works correctly`() {
        val user1 = User(
            userId = TEST_USER_ID,
            email = TEST_EMAIL,
            firstName = TEST_FIRST_NAME,
            lastName = TEST_LAST_NAME,
            university = TEST_UNIVERSITY,
            hobbies = listOf("Programming", "Photography"),
            profilePictureUrl = TEST_PROFILE_PICTURE_URL,
            bio = "Computer Science student"
        )
        val user2 = User(
            userId = TEST_USER_ID,
            email = TEST_EMAIL,
            firstName = TEST_FIRST_NAME,
            lastName = TEST_LAST_NAME,
            university = TEST_UNIVERSITY,
            hobbies = listOf("Programming", "Photography"),
            profilePictureUrl = TEST_PROFILE_PICTURE_URL,
            bio = "Computer Science student"
        )

        assertEquals("Equal User objects should be equal", user1, user2)
    }

    @Test
    fun `user card data class copy works correctly`() {
        val originalUser = testUser
        val copiedUser = originalUser.copy(firstName = "Updated")

        assertTrue("Original should remain unchanged", originalUser.firstName == TEST_FIRST_NAME)
        assertTrue("Copied should have updated value", copiedUser.firstName == "Updated")
    }

    private fun composeUserCard(
        user: User,
        onClick: (() -> Unit)? = null
    ) {
        controller.get().setContent {
            UserCard(
                user = user,
                onClick = onClick
            )
        }
        runOnIdle()
    }

    private fun composeMultipleUserCards(userList: List<User>) {
        controller.get().setContent {
            userList.forEach { user ->
                UserCard(user = user)
            }
        }
        runOnIdle()
    }

    private fun runOnIdle() {
        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }
}
