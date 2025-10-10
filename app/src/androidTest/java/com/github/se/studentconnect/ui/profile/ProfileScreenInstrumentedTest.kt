package com.github.se.studentconnect.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenInstrumentedTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val baseUser =
      User(
          userId = "user-123",
          email = "user@example.com",
          firstName = "Alex",
          lastName = "Doe",
          university = "EPFL",
          hobbies = listOf("Climbing", "Chess"),
          bio = "Bio",
          country = "Switzerland",
          birthday = "01/01/2000",
          createdAt = 1L,
          updatedAt = 1L)

  @Test
  fun profileScreen_showsLoadingWhileUserIsNull() {
    val repo = FakeUserRepository(storedUser = null)
    val viewModel = ProfileViewModel(repo, "user-123")

    composeRule.setContent {
      ProfileScreen(currentUserId = "user-123", viewModel = viewModel, userRepository = repo)
    }

    composeRule.waitUntil(5_000) { repo.getUserRequests.get() > 0 }

    composeRule
        .onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate),
            useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysUserDetails() {
    val repo = FakeUserRepository(storedUser = baseUser)
    val viewModel = ProfileViewModel(repo, baseUser.userId)

    composeRule.setContent {
      ProfileScreen(currentUserId = baseUser.userId, viewModel = viewModel, userRepository = repo)
    }

    composeRule.waitUntil(5_000) { viewModel.user.value != null }

    composeRule.onNodeWithText("Alex Doe").assertIsDisplayed()
    composeRule.onNodeWithText("University").assertIsDisplayed()
    composeRule.onNodeWithText("EPFL").assertIsDisplayed()
    composeRule.onNodeWithContentDescription("Edit Name").assertIsDisplayed()
    composeRule.onNodeWithContentDescription("Edit Birthday").assertIsDisplayed()
  }

  private class FakeUserRepository(var storedUser: User?) : UserRepository {

    val saveUserCalls = AtomicInteger(0)
    val getUserRequests = AtomicInteger(0)

    override fun getUserById(
        userId: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      getUserRequests.incrementAndGet()
      onSuccess(storedUser)
    }

    override fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
      saveUserCalls.incrementAndGet()
      storedUser = user
      onSuccess()
    }

    override fun leaveEvent(eventId: String, userId: String) = error("Not required for tests")

    override fun getUserByEmail(
        email: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) =
        error("Not required for tests")

    override fun getUsersPaginated(
        limit: Int,
        lastUserId: String?,
        onSuccess: (List<User>, Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun updateUser(
        userId: String,
        updates: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) =
        error("Not required for tests")

    override fun getUsersByUniversity(
        university: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun getUsersByHobby(
        hobby: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun getNewUid(): String = "unused"
  }
}
