package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditBioViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repo: FakeRepo
  private lateinit var vm: EditBioViewModel

  private val baseUser =
      User(
          userId = "u1",
          email = "u1@example.com",
          firstName = "First",
          lastName = "Last",
          university = "EPFL",
          bio = "Hello",
          createdAt = 1L,
          updatedAt = 1L)

  @Before
  fun setUp() {
    repo = FakeRepo(storedUser = baseUser)
    vm = EditBioViewModel(repo, baseUser.userId)
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
  }

  @Test
  fun loadsUserAndBio() {
    assertEquals(baseUser, vm.user.value)
    assertEquals("Hello", vm.bioText.value)
  }

  @Test
  fun updateBioText_and_validation() {
    vm.updateBioText("A")
    assertTrue(vm.isValid.value)
    // over 500 chars
    vm.updateBioText("x".repeat(501))
    assertTrue(!vm.isValid.value)
  }

  @Test
  fun saveBio_success_blankClears() {
    vm.updateBioText("  ")
    vm.saveBio()
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    assertNull(vm.user.value!!.bio)
    assertEquals("Bio updated successfully!", vm.successMessage.value)
  }

  @Test
  fun saveBio_tooLong_setsError() {
    vm.updateBioText("x".repeat(600))
    vm.saveBio()
    assertEquals("Bio cannot exceed 500 characters", vm.errorMessage.value)
  }

  @Test
  fun saveBio_failure_setsError() {
    repo.saveException = IllegalStateException("fail")
    vm.updateBioText("New Bio")
    vm.saveBio()
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    assertEquals("fail", vm.errorMessage.value)
  }

  @Test
  fun counters_and_clears() {
    vm.updateBioText("abcd")
    assertEquals(4, vm.getCharacterCount())
    assertEquals(496, vm.getRemainingCharacters())
    vm.clearErrorMessage()
    vm.clearSuccessMessage()
    assertNull(vm.errorMessage.value)
    assertNull(vm.successMessage.value)
  }

  private class FakeRepo(var storedUser: User?) : UserRepository {
    var saveException: Exception? = null

    override fun getUserById(
        userId: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      onSuccess(storedUser)
    }

    override fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
      val e = saveException
      if (e != null) onFailure(e) else onSuccess()
    }

    override fun leaveEvent(eventId: String, userId: String) = error("unused")

    override fun getUserByEmail(
        email: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("unused")

    override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) =
        error("unused")

    override fun getUsersPaginated(
        limit: Int,
        lastUserId: String?,
        onSuccess: (List<User>, Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("unused")

    override fun updateUser(
        userId: String,
        updates: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("unused")

    override fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) =
        error("unused")

    override fun getUsersByUniversity(
        university: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("unused")

    override fun getUsersByHobby(
        hobby: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("unused")

    override fun getNewUid(): String = "uid"
  }
}
