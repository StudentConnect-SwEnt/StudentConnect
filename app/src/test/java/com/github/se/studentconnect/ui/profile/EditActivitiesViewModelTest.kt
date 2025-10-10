package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditActivitiesViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repo: FakeRepo
  private lateinit var vm: EditActivitiesViewModel

  private val baseUser =
      User(
          userId = "u1",
          email = "u1@example.com",
          firstName = "First",
          lastName = "Last",
          university = "EPFL",
          hobbies = listOf("Chess"),
          createdAt = 1L,
          updatedAt = 1L)

  @Before
  fun setUp() {
    repo = FakeRepo(storedUser = baseUser)
    vm = EditActivitiesViewModel(repo, baseUser.userId)
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
  }

  @Test
  fun loadsUserAndInitializesSelection() {
    assertEquals(baseUser, vm.user.value)
    assertTrue(vm.selectedActivities.value.contains("Chess"))
    assertTrue(vm.availableActivities.value.isNotEmpty())
    assertEquals(vm.availableActivities.value, vm.filteredActivities.value)
  }

  @Test
  fun updateSearchQuery_filtersActivities() {
    vm.updateSearchQuery("chi")
    assertTrue(vm.filteredActivities.value.all { it.contains("chi", ignoreCase = true) })

    vm.updateSearchQuery("")
    assertEquals(vm.availableActivities.value, vm.filteredActivities.value)
  }

  @Test
  fun toggleSelection_addsAndRemoves() {
    assertFalse(vm.isActivitySelected("Hiking"))
    vm.toggleActivitySelection("Hiking")
    assertTrue(vm.isActivitySelected("Hiking"))
    vm.toggleActivitySelection("Hiking")
    assertFalse(vm.isActivitySelected("Hiking"))
  }

  @Test
  fun saveActivities_success_updatesUserAndMessage() {
    vm.toggleActivitySelection("Hiking")
    vm.saveActivities()
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.user.value!!.hobbies.contains("Hiking"))
    assertEquals("Activities updated successfully!", vm.successMessage.value)
    assertNull(vm.errorMessage.value)
    assertEquals(vm.user.value, repo.savedUser)
  }

  @Test
  fun saveActivities_failure_setsError() {
    repo.saveException = IllegalStateException("boom")
    vm.saveActivities()
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertEquals("boom", vm.errorMessage.value)
    assertFalse(vm.isLoading.value)
  }

  @Test
  fun counters_and_clearMessages_work() {
    assertEquals(1, vm.getSelectedCount())
    vm.clearSuccessMessage()
    vm.clearErrorMessage()
    assertNull(vm.successMessage.value)
    assertNull(vm.errorMessage.value)
  }

  private class FakeRepo(var storedUser: User?) : UserRepository {
    var savedUser: User? = null
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
        savedUser = user
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
