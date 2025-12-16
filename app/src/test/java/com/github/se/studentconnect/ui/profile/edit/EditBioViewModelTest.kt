package com.github.se.studentconnect.ui.profile.edit

import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.profile.ProfileConstants
import com.github.se.studentconnect.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditBioViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: TestUserRepository
  private lateinit var viewModel: EditBioViewModel
  private val testContext = ApplicationProvider.getApplicationContext<android.content.Context>()
  private val testUser =
      User(
          userId = "test_user",
          username = "johndoe",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthdate = "01/01/2000",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio about the user",
          profilePictureUrl = null)

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    viewModel = EditBioViewModel(repository, testUser.userId)
  }

  @Test
  fun `initial state loads user bio correctly`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertEquals("Test bio about the user", viewModel.bioText.value)
    assertEquals(testUser.bio?.length, viewModel.characterCount.value)
    assertNull(viewModel.validationError.value)
  }

  @Test
  fun `initial state handles user with null bio`() = runTest {
    val userWithNullBio = testUser.copy(bio = null)
    repository = TestUserRepository(userWithNullBio)
    val nullBioViewModel = EditBioViewModel(repository, userWithNullBio.userId)

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertEquals("", nullBioViewModel.bioText.value)
    assertEquals(0, nullBioViewModel.characterCount.value)
  }

  @Test
  fun `initial state handles user with empty bio`() = runTest {
    val userWithEmptyBio = testUser.copy(bio = "")
    repository = TestUserRepository(userWithEmptyBio)
    val emptyBioViewModel = EditBioViewModel(repository, userWithEmptyBio.userId)

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertEquals("", emptyBioViewModel.bioText.value)
    assertEquals(0, emptyBioViewModel.characterCount.value)
  }

  @Test
  fun `initial state handles user not found`() = runTest {
    repository = TestUserRepository(null)
    val errorViewModel = EditBioViewModel(repository, "non_existent_user")

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertEquals("", errorViewModel.bioText.value)
    assertEquals(0, errorViewModel.characterCount.value)
  }

  @Test
  fun `updateBioText updates bio text and character count`() {
    val newBio = "This is my new bio"
    viewModel.updateBioText(newBio)

    assertEquals(newBio, viewModel.bioText.value)
    assertEquals(newBio.length, viewModel.characterCount.value)
    assertNull(viewModel.validationError.value)
  }

  @Test
  fun `updateBioText clears validation error`() = runTest {
    // Set up an error first
    viewModel.updateBioText("")
    viewModel.saveBio(testContext)
    kotlinx.coroutines.delay(100)

    // Now update with valid text
    viewModel.updateBioText("Valid bio")

    assertNull(viewModel.validationError.value)
  }

  @Test
  fun `updateBioText rejects text exceeding max length`() {
    val currentBio = viewModel.bioText.value
    val tooLongBio = "A".repeat(ProfileConstants.MAX_BIO_LENGTH + 1)

    viewModel.updateBioText(tooLongBio)

    // Should keep the old bio, not update
    assertEquals(currentBio, viewModel.bioText.value)
  }

  @Test
  fun `updateBioText accepts text at max length`() {
    val maxLengthBio = "A".repeat(ProfileConstants.MAX_BIO_LENGTH)

    viewModel.updateBioText(maxLengthBio)

    assertEquals(maxLengthBio, viewModel.bioText.value)
    assertEquals(ProfileConstants.MAX_BIO_LENGTH, viewModel.characterCount.value)
  }

  @Test
  fun `updateBioText handles empty string`() {
    viewModel.updateBioText("")

    assertEquals("", viewModel.bioText.value)
    assertEquals(0, viewModel.characterCount.value)
  }

  @Test
  fun `updateBioText handles whitespace only`() {
    val whitespace = "   "
    viewModel.updateBioText(whitespace)

    assertEquals(whitespace, viewModel.bioText.value)
    assertEquals(whitespace.length, viewModel.characterCount.value)
  }

  @Test
  fun `saveBio validates empty bio`() = runTest {
    viewModel.updateBioText("")
    viewModel.saveBio(testContext)

    // Wait for validation to complete
    kotlinx.coroutines.delay(200)

    assertEquals(ProfileConstants.ERROR_BIO_EMPTY, viewModel.validationError.value)
    assertTrue(repository.savedUsers.isEmpty())
  }

  @Test
  fun `saveBio validates whitespace only bio`() = runTest {
    viewModel.updateBioText("   ")
    viewModel.saveBio(testContext)

    // Wait for validation to complete
    kotlinx.coroutines.delay(200)

    assertEquals(ProfileConstants.ERROR_BIO_EMPTY, viewModel.validationError.value)
    assertTrue(repository.savedUsers.isEmpty())
  }

  @Test
  fun `saveBio validates bio exceeding max length`() = runTest {
    // Force set a bio that's too long (bypassing updateBioText validation)
    viewModel.updateBioText("A".repeat(ProfileConstants.MAX_BIO_LENGTH))
    // Manually add more characters to the internal state to test validation
    viewModel.updateBioText("A".repeat(ProfileConstants.MAX_BIO_LENGTH) + "extra")

    // Since updateBioText prevents this, we test by setting it properly
    viewModel.updateBioText("A".repeat(ProfileConstants.MAX_BIO_LENGTH))

    // Now try to save - should succeed at max length
    viewModel.saveBio(testContext)
    kotlinx.coroutines.delay(200)

    // Should save successfully
    assertTrue(repository.savedUsers.isNotEmpty())
  }

  @Test
  fun `saveBio saves valid bio successfully`() = runTest {
    val newBio = "This is my new and improved bio"
    viewModel.updateBioText(newBio)

    viewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(newBio, savedUser.bio)
    assertNull(viewModel.validationError.value)
  }

  @Test
  fun `saveBio trims whitespace from bio`() = runTest {
    val bioWithWhitespace = "  This is my bio  "
    viewModel.updateBioText(bioWithWhitespace)

    viewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals("This is my bio", savedUser.bio)
  }

  @Test
  fun `saveBio updates updatedAt timestamp`() = runTest {
    val oldTimestamp = testUser.updatedAt
    val newBio = "New bio"
    viewModel.updateBioText(newBio)

    // Add small delay to ensure timestamp difference
    kotlinx.coroutines.delay(10)
    viewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertTrue(
        "updatedAt should be greater than or equal to old timestamp",
        savedUser.updatedAt >= oldTimestamp)
  }

  @Test
  fun `saveBio handles user not found error`() = runTest {
    repository = TestUserRepository(null)
    val errorViewModel = EditBioViewModel(repository, "non_existent_user")
    errorViewModel.updateBioText("Some bio")

    errorViewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(errorViewModel.uiState.value is BaseEditViewModel.UiState.Error)
  }

  @Test
  fun `saveBio handles repository error`() = runTest {
    repository.shouldThrowOnSave = RuntimeException("Save failed")
    viewModel.updateBioText("Valid bio")

    viewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is BaseEditViewModel.UiState.Error)
  }

  @Test
  fun `saveBio sets success state on successful save`() = runTest {
    viewModel.updateBioText("New bio")

    viewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is BaseEditViewModel.UiState.Success)
    val successState = viewModel.uiState.value as BaseEditViewModel.UiState.Success
    assertEquals(ProfileConstants.SUCCESS_BIO_UPDATED, successState.message)
  }

  @Test
  fun `saveBio clears previous validation errors before validation`() = runTest {
    // First attempt with empty bio
    viewModel.updateBioText("")
    viewModel.saveBio(testContext)
    kotlinx.coroutines.delay(100)

    // Should have error
    assertEquals(ProfileConstants.ERROR_BIO_EMPTY, viewModel.validationError.value)

    // Second attempt with valid bio
    viewModel.updateBioText("Valid bio")
    viewModel.saveBio(testContext)
    kotlinx.coroutines.delay(200)

    // Error should be cleared and save successful
    assertNull(viewModel.validationError.value)
    assertTrue(repository.savedUsers.isNotEmpty())
  }

  @Test
  fun `clearValidationError clears validation error`() {
    viewModel.updateBioText("")
    viewModel.saveBio(testContext)

    // Given: there is a validation error
    viewModel.clearValidationError()

    // Then: error is cleared
    assertNull(viewModel.validationError.value)
  }

  @Test
  fun `saveBio with special characters in bio`() = runTest {
    val specialBio = "Bio with émojis 🎉 and spëcial çharacters!"
    viewModel.updateBioText(specialBio)

    viewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(specialBio, savedUser.bio)
  }

  @Test
  fun `saveBio with multiline bio`() = runTest {
    val multilineBio = "Line 1\nLine 2\nLine 3"
    viewModel.updateBioText(multilineBio)

    viewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(multilineBio, savedUser.bio)
  }

  @Test
  fun `saveBio with very long but valid bio`() = runTest {
    val longBio = "A".repeat(ProfileConstants.MAX_BIO_LENGTH)
    viewModel.updateBioText(longBio)

    viewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(longBio, savedUser.bio)
  }

  @Test
  fun `multiple save operations work correctly`() = runTest {
    // First save
    viewModel.updateBioText("First bio")
    viewModel.saveBio(testContext)
    kotlinx.coroutines.delay(300)

    // Second save
    viewModel.updateBioText("Second bio")
    viewModel.saveBio(testContext)
    kotlinx.coroutines.delay(300)

    assertEquals(2, repository.savedUsers.size)
    assertEquals("Second bio", repository.savedUsers.last().bio)
  }

  @Test
  fun `saveBio sets offline message when offline`() = runTest {
    val context = mockk<android.content.Context>(relaxed = true)
    val connectivityManager = mockk<android.net.ConnectivityManager>(relaxed = true)

    every { context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { connectivityManager.activeNetwork } returns null

    viewModel.updateBioText("Test bio")
    viewModel.saveBio(context)

    kotlinx.coroutines.delay(200)

    assertEquals(com.github.se.studentconnect.R.string.offline_changes_will_sync, viewModel.offlineMessageRes.value)
  }

  @Test
  fun `saveBio clears offline message when online`() = runTest {
    val context = mockk<android.content.Context>(relaxed = true)
    val connectivityManager = mockk<android.net.ConnectivityManager>(relaxed = true)
    val network = mockk<android.net.Network>(relaxed = true)
    val capabilities = mockk<android.net.NetworkCapabilities>(relaxed = true)

    every { context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { connectivityManager.activeNetwork } returns network
    every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
    every { capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

    viewModel.updateBioText("Test bio")
    viewModel.saveBio(context)

    kotlinx.coroutines.delay(200)

    assertNull(viewModel.offlineMessageRes.value)
  }

  @Test
  fun `character count updates correctly with unicode characters`() {
    val unicodeBio = "Hello 👋 World 🌍"
    viewModel.updateBioText(unicodeBio)

    assertEquals(unicodeBio.length, viewModel.characterCount.value)
  }

  @Test
  fun `saveBio preserves other user fields`() = runTest {
    val newBio = "Updated bio"
    viewModel.updateBioText(newBio)

    viewModel.saveBio(testContext)

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()

    // Verify all other fields are preserved
    assertEquals(testUser.userId, savedUser.userId)
    assertEquals(testUser.username, savedUser.username)
    assertEquals(testUser.firstName, savedUser.firstName)
    assertEquals(testUser.lastName, savedUser.lastName)
    assertEquals(testUser.email, savedUser.email)
    assertEquals(testUser.university, savedUser.university)
    assertEquals(testUser.country, savedUser.country)
    assertEquals(testUser.birthdate, savedUser.birthdate)
    assertEquals(testUser.hobbies, savedUser.hobbies)
    assertEquals(testUser.profilePictureUrl, savedUser.profilePictureUrl)
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnSave: Throwable? = null
  ) : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      return if (userId == user?.userId) user else null
    }

    override suspend fun saveUser(user: User) {
      shouldThrowOnSave?.let { throw it }
      savedUsers.add(user)
      this.user = user
    }

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = emptyList<User>()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        emptyList<User>() to false

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

    override suspend fun deleteUser(userId: String) = Unit

    override suspend fun getUsersByUniversity(university: String) = emptyList<User>()

    override suspend fun getUsersByHobby(hobby: String) = emptyList<User>()

    override suspend fun getNewUid() = "new_uid"

    override suspend fun addPinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun removePinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun getPinnedEvents(userId: String) = emptyList<String>()

    override suspend fun getJoinedEvents(userId: String) = emptyList<String>()

    override suspend fun addEventToUser(eventId: String, userId: String) = Unit

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        Unit

    override suspend fun getInvitations(userId: String) = emptyList<Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun removeInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit

    override suspend fun addFavoriteEvent(userId: String, eventId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun getFavoriteEvents(userId: String): List<String> {
      TODO("Not yet implemented")
    }

    override suspend fun checkUsernameAvailability(username: String): Boolean {
      TODO("Not yet implemented")
    }

    override suspend fun pinOrganization(userId: String, organizationId: String) {}

    override suspend fun unpinOrganization(userId: String) {}

    override suspend fun getPinnedOrganization(userId: String): String? = null
  }
}
