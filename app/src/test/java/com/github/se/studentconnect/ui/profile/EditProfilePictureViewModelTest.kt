package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfilePictureViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repo: FakeRepo
  private lateinit var vm: EditProfilePictureViewModel

  private val baseUser =
      User(
          userId = "u1",
          email = "u1@example.com",
          firstName = "First",
          lastName = "Last",
          university = "EPFL",
          profilePictureUrl = null,
          createdAt = 1L,
          updatedAt = 1L)

  @Before
  fun setUp() {
    repo = FakeRepo(storedUser = baseUser)
    vm = EditProfilePictureViewModel(repo, baseUser.userId)
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
  }

  // Note: uploadProfilePicture involves delay + Android context. We focus on remove flow in JVM.

  @Test
  fun removeProfilePicture_success_setsNullAndMessage() {
    repo.storedUser = baseUser.copy(profilePictureUrl = "x")
    vm = EditProfilePictureViewModel(repo, baseUser.userId)
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    vm.removeProfilePicture()
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    assertNull(vm.user.value!!.profilePictureUrl)
    assertEquals("Profile picture removed successfully!", vm.successMessage.value)
  }

  @Test
  fun removeFailure_setsError() {
    repo.saveException = IllegalStateException("err")
    vm.removeProfilePicture()
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    assertEquals("err", vm.errorMessage.value)
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
      if (e != null) onFailure(e)
      else {
        storedUser = user
        onSuccess()
      }
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
