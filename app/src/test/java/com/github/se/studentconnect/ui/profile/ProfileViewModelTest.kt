package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
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
class ProfileViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: TestUserRepository
  private lateinit var viewModel: ProfileViewModel
  private val testUser =
      User(
          userId = "test_user",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthday = "01/01/2000",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio",
          profilePictureUrl = null)

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    viewModel = ProfileViewModel(repository, testUser.userId)
  }

  @Test
  fun `initial load populates user data`() = runTest {
    // Wait for initial load to complete
    Thread.sleep(100)

    val user = viewModel.user.value
    assertEquals(testUser, user)
    assertNull(viewModel.successMessage.value)
    assertTrue(viewModel.fieldErrors.value.isEmpty())
    assertTrue(viewModel.loadingFields.value.isEmpty())
  }

  @Test
  fun `loadUserProfile handles user not found`() = runTest {
    repository = TestUserRepository(null)
    val vm = ProfileViewModel(repository, "non_existent_user")

    // Wait for initial load to complete
    Thread.sleep(100)

    assertNull(vm.user.value)
    assertTrue(vm.fieldErrors.value.containsKey(EditingField.None))
  }

  @Test
  fun `loadUserProfile handles repository error`() = runTest {
    repository.shouldThrowOnGet = RuntimeException("Network error")
    viewModel.loadUserProfile()

    // Wait for load to complete
    Thread.sleep(100)

    assertNull(viewModel.user.value)
    assertEquals("Network error", viewModel.fieldErrors.value[EditingField.None])
  }

  @Test
  fun `startEditing sets editing field and clears its error`() = runTest {
    // Simulate an error for a field
    viewModel.updateName("", "")

    // Wait for validation to complete
    Thread.sleep(100)

    assertTrue(viewModel.fieldErrors.value.containsKey(EditingField.Name))

    viewModel.startEditing(EditingField.Name)
    assertFalse(viewModel.fieldErrors.value.containsKey(EditingField.Name))
    assertEquals(EditingField.Name, viewModel.editingField.value)
  }

  @Test
  fun `cancelEditing clears editing field`() = runTest {
    viewModel.startEditing(EditingField.Bio)
    assertEquals(EditingField.Bio, viewModel.editingField.value)

    viewModel.cancelEditing()
    assertEquals(EditingField.None, viewModel.editingField.value)
  }

  @Test
  fun `updateName validates empty names`() = runTest {
    viewModel.updateName("", "")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(ProfileConstants.ERROR_NAME_EMPTY, viewModel.fieldErrors.value[EditingField.Name])
    assertTrue(repository.updatedUsers.isEmpty())
  }

  @Test
  fun `updateName validates whitespace only names`() = runTest {
    viewModel.updateName("   ", "   ")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(ProfileConstants.ERROR_NAME_EMPTY, viewModel.fieldErrors.value[EditingField.Name])
    assertTrue(repository.updatedUsers.isEmpty())
  }

  @Test
  fun `updateName saves valid names and sets success message`() = runTest {
    viewModel.updateName("Alice", "Smith")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("Alice", updatedUser["firstName"])
    assertEquals("Smith", updatedUser["lastName"])
    assertEquals(ProfileConstants.SUCCESS_NAME_UPDATED, viewModel.successMessage.value)
    assertEquals(EditingField.None, viewModel.editingField.value)
  }

  @Test
  fun `updateName trims whitespace from names`() = runTest {
    viewModel.updateName("  Bob ", "  Johnson  ")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("Bob", updatedUser["firstName"])
    assertEquals("Johnson", updatedUser["lastName"])
  }

  @Test
  fun `updateUniversity validates empty university`() = runTest {
    viewModel.updateUniversity("")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(
        ProfileConstants.ERROR_UNIVERSITY_EMPTY,
        viewModel.fieldErrors.value[EditingField.University])
    assertTrue(repository.updatedUsers.isEmpty())
  }

  @Test
  fun `updateUniversity validates whitespace only university`() = runTest {
    viewModel.updateUniversity("   ")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(
        ProfileConstants.ERROR_UNIVERSITY_EMPTY,
        viewModel.fieldErrors.value[EditingField.University])
    assertTrue(repository.updatedUsers.isEmpty())
  }

  @Test
  fun `updateUniversity saves valid university and sets success message`() = runTest {
    viewModel.updateUniversity("ETHZ")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("ETHZ", updatedUser["university"])
    assertEquals(ProfileConstants.SUCCESS_UNIVERSITY_UPDATED, viewModel.successMessage.value)
  }

  @Test
  fun `updateCountry saves valid country and sets success message`() = runTest {
    viewModel.updateCountry("France")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("France", updatedUser["country"])
    assertEquals(ProfileConstants.SUCCESS_COUNTRY_UPDATED, viewModel.successMessage.value)
  }

  @Test
  fun `updateCountry allows clearing country`() = runTest {
    viewModel.updateCountry("")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertNull(updatedUser["country"])
    assertEquals(ProfileConstants.SUCCESS_COUNTRY_UPDATED, viewModel.successMessage.value)
  }

  @Test
  fun `updateBirthday validates invalid format`() = runTest {
    viewModel.updateBirthday("01-01-2000")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(
        ProfileConstants.ERROR_DATE_FORMAT, viewModel.fieldErrors.value[EditingField.Birthday])
    assertTrue(repository.updatedUsers.isEmpty())
  }

  @Test
  fun `updateBirthday validates invalid date with wrong separator`() = runTest {
    viewModel.updateBirthday("13.01.2020")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(
        ProfileConstants.ERROR_DATE_FORMAT, viewModel.fieldErrors.value[EditingField.Birthday])
    assertTrue(repository.updatedUsers.isEmpty())
  }

  @Test
  fun `updateBirthday validates invalid day`() = runTest {
    viewModel.updateBirthday("32/01/2020")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(
        ProfileConstants.ERROR_DATE_FORMAT, viewModel.fieldErrors.value[EditingField.Birthday])
    assertTrue(repository.updatedUsers.isEmpty())
  }

  @Test
  fun `updateBirthday validates invalid month`() = runTest {
    viewModel.updateBirthday("15/13/2020")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(
        ProfileConstants.ERROR_DATE_FORMAT, viewModel.fieldErrors.value[EditingField.Birthday])
    assertTrue(repository.updatedUsers.isEmpty())
  }

  @Test
  fun `updateBirthday validates invalid year`() = runTest {
    viewModel.updateBirthday("15/01/1800")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(
        ProfileConstants.ERROR_DATE_FORMAT, viewModel.fieldErrors.value[EditingField.Birthday])
    assertTrue(repository.updatedUsers.isEmpty())
  }

  @Test
  fun `updateBirthday accepts valid date and sets success message`() = runTest {
    viewModel.updateBirthday("15/03/1998")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("15/03/1998", updatedUser["birthday"])
    assertEquals(ProfileConstants.SUCCESS_BIRTHDAY_UPDATED, viewModel.successMessage.value)
  }

  @Test
  fun `updateBirthday allows clearing birthday`() = runTest {
    viewModel.updateBirthday("")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertNull(updatedUser["birthday"])
    assertEquals(ProfileConstants.SUCCESS_BIRTHDAY_UPDATED, viewModel.successMessage.value)
  }

  @Test
  fun `updateActivities normalizes and saves hobbies`() = runTest {
    viewModel.updateActivities("  running, hiking , coding  ")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals(listOf("running", "hiking", "coding"), updatedUser["hobbies"])
    assertEquals(ProfileConstants.SUCCESS_ACTIVITIES_UPDATED, viewModel.successMessage.value)
  }

  @Test
  fun `updateActivities handles empty activities`() = runTest {
    viewModel.updateActivities("")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertTrue((updatedUser["hobbies"] as? List<*>)?.isEmpty() == true)
    assertEquals(ProfileConstants.SUCCESS_ACTIVITIES_UPDATED, viewModel.successMessage.value)
  }

  @Test
  fun `updateActivities handles activities with extra spaces`() = runTest {
    viewModel.updateActivities("  ,  , running , , hiking  ")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals(listOf("running", "hiking"), updatedUser["hobbies"])
  }

  @Test
  fun `updateBio saves valid bio and sets success message`() = runTest {
    viewModel.updateBio("New bio content.")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("New bio content.", updatedUser["bio"])
    assertEquals(ProfileConstants.SUCCESS_BIO_UPDATED, viewModel.successMessage.value)
  }

  @Test
  fun `updateBio allows clearing bio`() = runTest {
    viewModel.updateBio("")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertNull(updatedUser["bio"])
    assertEquals(ProfileConstants.SUCCESS_BIO_UPDATED, viewModel.successMessage.value)
  }

  @Test
  fun `clearSuccessMessage resets success message`() = runTest {
    viewModel.updateName("A", "B")

    // Wait for save to complete
    Thread.sleep(100)

    assertTrue(viewModel.successMessage.value != null)

    viewModel.clearSuccessMessage()
    assertNull(viewModel.successMessage.value)
  }

  @Test
  fun `clearFieldErrors resets all field errors`() = runTest {
    viewModel.updateName("", "")

    // Wait for validation to complete
    Thread.sleep(100)

    assertFalse(viewModel.fieldErrors.value.isEmpty())

    viewModel.clearFieldErrors()
    assertTrue(viewModel.fieldErrors.value.isEmpty())
  }

  @Test
  fun `update failure surfaces field error and clears loading`() = runTest {
    repository.shouldThrowOnUpdate = RuntimeException("Update failed")
    viewModel.updateCountry("Germany")

    // Wait for update to complete
    Thread.sleep(100)

    assertTrue(viewModel.fieldErrors.value.containsKey(EditingField.Country))
    assertEquals("Update failed", viewModel.fieldErrors.value[EditingField.Country])
    assertFalse(viewModel.loadingFields.value.contains(EditingField.Country))
  }

  @Test
  fun `multiple field updates preserve independent loading states`() = runTest {
    viewModel.updateCountry("France")
    viewModel.updateBio("New bio")

    assertTrue(viewModel.loadingFields.value.contains(EditingField.Country))
    assertTrue(viewModel.loadingFields.value.contains(EditingField.Bio))

    // Wait for updates to complete
    Thread.sleep(100)

    assertFalse(viewModel.loadingFields.value.contains(EditingField.Country))
    assertFalse(viewModel.loadingFields.value.contains(EditingField.Bio))
    assertEquals("France", repository.updatedUsers[0]["country"])
    assertEquals("New bio", repository.updatedUsers[1]["bio"])
  }

  @Test
  fun `editingField displayName returns correct values`() {
    assertEquals("Name", EditingField.Name.displayName)
    assertEquals("University", EditingField.University.displayName)
    assertEquals("Country", EditingField.Country.displayName)
    assertEquals("Birthday", EditingField.Birthday.displayName)
    assertEquals("Activities", EditingField.Activities.displayName)
    assertEquals("Bio", EditingField.Bio.displayName)
    assertEquals("Profile", EditingField.None.displayName)
  }

  @Test
  fun `updateName with special characters`() = runTest {
    viewModel.updateName("José-María", "O'Connor-Smith")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("José-María", updatedUser["firstName"])
    assertEquals("O'Connor-Smith", updatedUser["lastName"])
  }

  @Test
  fun `updateUniversity with special characters`() = runTest {
    viewModel.updateUniversity("École Polytechnique Fédérale de Lausanne")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("École Polytechnique Fédérale de Lausanne", updatedUser["university"])
  }

  @Test
  fun `updateCountry with special characters`() = runTest {
    viewModel.updateCountry("Côte d'Ivoire")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("Côte d'Ivoire", updatedUser["country"])
  }

  @Test
  fun `updateBirthday with leap year`() = runTest {
    viewModel.updateBirthday("29/02/2020")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals("29/02/2020", updatedUser["birthday"])
  }

  @Test
  fun `updateBirthday with non-leap year February 29`() = runTest {
    viewModel.updateBirthday("29/02/2021")

    // Wait for validation to complete
    Thread.sleep(100)

    assertEquals(
        ProfileConstants.ERROR_DATE_FORMAT, viewModel.fieldErrors.value[EditingField.Birthday])
  }

  @Test
  fun `updateActivities with unicode characters`() = runTest {
    viewModel.updateActivities("Café, Théâtre, Cinéma")

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals(listOf("Café", "Théâtre", "Cinéma"), updatedUser["hobbies"])
  }

  @Test
  fun `updateBio with unicode characters`() = runTest {
    val unicodeBio = "Bio with unicode: αβγδε, 中文, العربية, русский"
    viewModel.updateBio(unicodeBio)

    // Wait for save to complete
    Thread.sleep(100)

    val updatedUser = repository.updatedUsers.last()
    assertEquals(unicodeBio, updatedUser["bio"])
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnGet: Throwable? = null,
      var shouldThrowOnUpdate: Throwable? = null
  ) : UserRepository {
    val updatedUsers = mutableListOf<Map<String, Any?>>()

    override suspend fun getUserById(userId: String): User? {
      shouldThrowOnGet?.let { throw it }
      return if (userId == user?.userId) user else null
    }

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
      shouldThrowOnUpdate?.let { throw it }
      if (userId == user?.userId) {
        updatedUsers.add(updates)
        user =
            user?.copy(
                firstName = updates["firstName"] as? String ?: user?.firstName ?: "",
                lastName = updates["lastName"] as? String ?: user?.lastName ?: "",
                university = updates["university"] as? String ?: user?.university ?: "",
                country = updates["country"] as? String? ?: user?.country,
                birthday = updates["birthday"] as? String? ?: user?.birthday,
                hobbies =
                    (updates["hobbies"] as? List<*>)?.filterIsInstance<String>()
                        ?: user?.hobbies
                        ?: emptyList(),
                bio = updates["bio"] as? String? ?: user?.bio,
                profilePictureUrl =
                    updates["profilePictureUrl"] as? String? ?: user?.profilePictureUrl)
      } else {
        throw NoSuchElementException("User not found")
      }
    }

    override suspend fun saveUser(user: User) = Unit

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = emptyList<User>()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        emptyList<User>() to false

    override suspend fun deleteUser(userId: String) = Unit

    override suspend fun getUsersByUniversity(university: String) = emptyList<User>()

    override suspend fun getUsersByHobby(hobby: String) = emptyList<User>()

    override suspend fun getNewUid() = "new_uid"

    override suspend fun getJoinedEvents(userId: String) = emptyList<String>()

    override suspend fun addEventToUser(eventId: String, userId: String) = Unit

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        Unit

    override suspend fun getInvitations(userId: String) =
        emptyList<com.github.se.studentconnect.ui.screen.activities.Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit
  }
}
