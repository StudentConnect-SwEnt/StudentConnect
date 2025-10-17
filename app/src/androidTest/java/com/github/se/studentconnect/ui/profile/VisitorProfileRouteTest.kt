package com.github.se.studentconnect.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.profile.VisitorProfileRoute
import com.github.se.studentconnect.ui.theme.AppTheme
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VisitorProfileRouteTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var originalRepository: UserRepository

  @Before
  fun setUp() {
    originalRepository = UserRepositoryProvider.repository
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun visitorProfileRoute_showsNotFoundMessageWhenRepositoryReturnsNull() {
    val repository =
        object : EmptyUserRepository() {
          override suspend fun getUserById(userId: String): User? = null
        }
    UserRepositoryProvider.repository = repository

    composeTestRule.setContent {
      AppTheme { VisitorProfileRoute(userId = "missing", onBackClick = {}) }
    }

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(C.Tag.visitor_profile_error))
    composeTestRule.onNodeWithText("Profile not found.").assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun visitorProfileRoute_showsThrownErrorMessage() {
    val repository =
        object : EmptyUserRepository() {
          override suspend fun getUserById(userId: String): User? {
            throw IllegalStateException("boom")
          }
        }
    UserRepositoryProvider.repository = repository

    composeTestRule.setContent {
      AppTheme { VisitorProfileRoute(userId = "error", onBackClick = {}) }
    }

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(C.Tag.visitor_profile_error))
    composeTestRule.onNodeWithText("boom").assertIsDisplayed()
  }

  @After
  fun tearDown() {
    UserRepositoryProvider.repository = originalRepository
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun visitorProfileRoute_showsLoadingThenProfile() {
    val deferred = CompletableDeferred<User?>()
    val repository =
        object : EmptyUserRepository() {
          override suspend fun getUserById(userId: String): User? = deferred.await()
        }
    UserRepositoryProvider.repository = repository

    composeTestRule.setContent {
      AppTheme { VisitorProfileRoute(userId = "user-1", onBackClick = {}) }
    }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_loading).assertIsDisplayed()

    val user =
        User(
            userId = "user-1",
            email = "user1@studentconnect.ch",
            firstName = "River",
            lastName = "Stone",
            university = "EPFL",
            updatedAt = 2,
            createdAt = 1)
    deferred.complete(user)

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(C.Tag.visitor_profile_screen))
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_user_name).assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun visitorProfileRoute_retryAfterErrorLoadsProfile() {
    val repository = ToggleUserRepository()
    UserRepositoryProvider.repository = repository

    composeTestRule.setContent {
      AppTheme { VisitorProfileRoute(userId = "user-2", onBackClick = {}) }
    }

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(C.Tag.visitor_profile_error))

    repository.returnUserOnNextCall()
    composeTestRule.onNodeWithText("Try again").performClick()

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(C.Tag.visitor_profile_screen))
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_user_id).assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun visitorProfileRoute_backButtonFromErrorInvokesCallback() {
    val repository =
        object : EmptyUserRepository() {
          override suspend fun getUserById(userId: String): User? = null
        }
    UserRepositoryProvider.repository = repository

    val backInvoked = AtomicBoolean(false)

    composeTestRule.setContent {
      AppTheme { VisitorProfileRoute(userId = "user-3", onBackClick = { backInvoked.set(true) }) }
    }

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(C.Tag.visitor_profile_error))
    composeTestRule.waitForIdle()

    // Click the "Back to Home" button
    composeTestRule.onNodeWithText("Back to Home").assertIsDisplayed()
    composeTestRule.onNodeWithText("Back to Home").performClick()

    assertTrue(backInvoked.get())
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun visitorProfileRoute_backButtonFromProfileInvokesCallback() {
    val repository =
        object : EmptyUserRepository() {
          override suspend fun getUserById(userId: String): User? =
              User(
                  userId = userId,
                  email = "$userId@studentconnect.ch",
                  firstName = "Alex",
                  lastName = "Moore",
                  university = "UNIL",
                  updatedAt = 2,
                  createdAt = 1)
        }
    UserRepositoryProvider.repository = repository

    val backInvoked = AtomicBoolean(false)

    composeTestRule.setContent {
      AppTheme { VisitorProfileRoute(userId = "user-4", onBackClick = { backInvoked.set(true) }) }
    }

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(C.Tag.visitor_profile_screen))
    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_back).performClick()

    assertTrue(backInvoked.get())
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun visitorProfileRoute_showsScanAgainButtonWhenProvidedAndError() {
    val repository =
        object : EmptyUserRepository() {
          override suspend fun getUserById(userId: String): User? = null
        }
    UserRepositoryProvider.repository = repository

    val scanAgainInvoked = AtomicBoolean(false)

    composeTestRule.setContent {
      AppTheme {
        VisitorProfileRoute(
            userId = "user-5", onBackClick = {}, onScanAgain = { scanAgainInvoked.set(true) })
      }
    }

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(C.Tag.visitor_profile_error))
    composeTestRule.onNodeWithTag("scan_again_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scan_again_button").performClick()

    assertTrue(scanAgainInvoked.get())
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun visitorProfileRoute_showsTryAgainButtonWhenNoScanAgainCallbackAndError() {
    val repository =
        object : EmptyUserRepository() {
          override suspend fun getUserById(userId: String): User? = null
        }
    UserRepositoryProvider.repository = repository

    composeTestRule.setContent {
      AppTheme { VisitorProfileRoute(userId = "user-6", onBackClick = {}, onScanAgain = null) }
    }

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(C.Tag.visitor_profile_error))
    composeTestRule.onNodeWithText("Try again").assertIsDisplayed()
    composeTestRule.onNodeWithText("Scan Again").assertDoesNotExist()
  }
}

private open class EmptyUserRepository : UserRepository {
  override suspend fun leaveEvent(eventId: String, userId: String) = Unit

  override suspend fun getUserById(userId: String): User? = null

  override suspend fun getUserByEmail(email: String): User? = null

  override suspend fun getAllUsers(): List<User> = emptyList()

  override suspend fun getUsersPaginated(
      limit: Int,
      lastUserId: String?
  ): Pair<List<User>, Boolean> = emptyList<User>() to false

  override suspend fun saveUser(user: User) = Unit

  override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

  override suspend fun deleteUser(userId: String) = Unit

  override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

  override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

  override suspend fun getNewUid(): String = "uid"

  override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

  override suspend fun addEventToUser(eventId: String, userId: String) = Unit

  override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
      Unit

  override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

  override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

  override suspend fun joinEvent(eventId: String, userId: String) = Unit

  override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) = Unit

  override suspend fun declineInvitation(eventId: String, userId: String) = Unit
}

private class ToggleUserRepository : EmptyUserRepository() {
  private val user =
      User(
          userId = "user-2",
          email = "user2@studentconnect.ch",
          firstName = "Jamie",
          lastName = "Lake",
          university = "UZH",
          updatedAt = 2,
          createdAt = 1)

  private val shouldReturnUser = AtomicBoolean(false)

  fun returnUserOnNextCall() {
    shouldReturnUser.set(true)
  }

  override suspend fun getUserById(userId: String): User? {
    return if (shouldReturnUser.getAndSet(false)) user else null
  }
}
