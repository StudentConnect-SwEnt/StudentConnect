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
class EditCountryViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repo: FakeRepo
  private lateinit var vm: EditCountryViewModel

  private val baseUser =
      User(
          userId = "u1",
          email = "u1@example.com",
          firstName = "First",
          lastName = "Last",
          university = "EPFL",
          country = "🇨🇭 Switzerland",
          createdAt = 1L,
          updatedAt = 1L)

  @Before
  fun setUp() {
    repo = FakeRepo(storedUser = baseUser)
    vm = EditCountryViewModel(repo, baseUser.userId)
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
  }

  @Test
  fun initializesCountriesAndSelection() {
    assertEquals(baseUser, vm.user.value)
    assertTrue(vm.availableCountries.value.isNotEmpty())
    assertEquals(vm.availableCountries.value, vm.filteredCountries.value)
    assertTrue(vm.isCountrySelected("🇨🇭 Switzerland"))
  }

  @Test
  fun updateSearchQuery_filtersList() {
    vm.updateSearchQuery("United")
    assertTrue(vm.filteredCountries.value.all { it.contains("United", ignoreCase = true) })
    vm.updateSearchQuery("")
    assertEquals(vm.availableCountries.value, vm.filteredCountries.value)
  }

  @Test
  fun selectCountry_and_save_success() {
    val someCountry = vm.availableCountries.value.first()
    vm.selectCountry(someCountry)
    assertTrue(vm.isCountrySelected(someCountry))

    vm.saveCountry()
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertEquals(someCountry, vm.user.value!!.country)
    assertEquals("Country updated successfully!", vm.successMessage.value)
    assertNull(vm.errorMessage.value)
    assertEquals(vm.user.value, repo.savedUser)
  }

  @Test
  fun saveCountry_failure_setsError() {
    repo.saveException = IllegalArgumentException("nope")
    vm.saveCountry()
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    assertEquals("nope", vm.errorMessage.value)
    assertFalse(vm.isLoading.value)
  }

  @Test
  fun clearMessages_works() {
    vm.clearErrorMessage()
    vm.clearSuccessMessage()
    assertNull(vm.errorMessage.value)
    assertNull(vm.successMessage.value)
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
