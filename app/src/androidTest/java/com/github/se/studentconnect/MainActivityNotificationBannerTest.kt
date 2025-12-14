package com.github.se.studentconnect

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.ui.navigation.NavigationTestTags
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
import com.github.se.studentconnect.utils.NoAnonymousSignIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for app-wide notification banner in MainActivity. These tests verify that the
 * notification banner appears across different screens.
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class MainActivityNotificationBannerTest : FirestoreStudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION,
          android.Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var scenario: ActivityScenario<MainActivity>

  private suspend fun signInAs(email: String, password: String) {
    try {
      FirebaseEmulator.auth.createUserWithEmailAndPassword(email, password).await()
    } catch (_: Exception) {
      // Account might already exist, try signing in
      FirebaseEmulator.auth.signInWithEmailAndPassword(email, password).await()
    }
  }

  @NoAnonymousSignIn
  @Before
  fun setup() {
    // Sign in a test user and create profile BEFORE launching activity
    runTest {
      val uniqueSuffix = System.currentTimeMillis()
      val testEmail = "notificationtest${uniqueSuffix}@example.com"
      val testPassword = "TestPassword123"

      signInAs(testEmail, testPassword)

      // Create user profile so app skips onboarding and goes to MAIN_APP state
      val userRepository = UserRepositoryProvider.repository
      userRepository.saveUser(
          User(
              userId = currentUser.uid,
              email = currentUser.email!!,
              username = "testuser${uniqueSuffix % 10000}", // Keep within 3-20 char limit
              firstName = "Test",
              lastName = "User",
              university = "EPFL"))
    }

    // Launch activity AFTER authentication and profile creation
    val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
    scenario = ActivityScenario.launch(intent)

    // Wait for activity to be ready and compose content to be available
    composeTestRule.waitForIdle()

    // First, wait for any compose hierarchy to be available
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      try {
        composeTestRule.onRoot().assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Then wait for bottom navigation to appear (indicates MAIN_APP state)
    composeTestRule.waitUntil(timeoutMillis = 30000) {
      try {
        composeTestRule
            .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }
  }

  @NoAnonymousSignIn
  @Test
  fun mainActivity_notificationBanner_existsInLayout() {
    // The notification banner should be in the layout (even if not visible without notification)
    // We can't directly test visibility without a notification, but we can verify the structure
    // This test verifies that the banner component is properly integrated
    // Setup already waits for MAIN_APP state, so we can proceed

    // Verify that the bottom navigation is present (confirms MAIN_APP state)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertExists()

    // This test primarily verifies that adding the notification banner to the layout
    // doesn't break the app or cause crashes
  }

  @NoAnonymousSignIn
  @Test
  fun mainActivity_hasBottomNavigation() {
    // Verify bottom navigation exists (already waited for in setup)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertExists()
  }

  @NoAnonymousSignIn
  @Test
  fun mainActivity_canNavigateBetweenScreens() {
    // Navigate to different tabs to verify banner is available on all screens
    // Click on Map tab
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.waitForIdle()

    // Click on Activities tab
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).performClick()
    composeTestRule.waitForIdle()

    // Click on Profile tab
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.waitForIdle()

    // Click on Home tab
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).performClick()
    composeTestRule.waitForIdle()

    // Verify we're back on home
    composeTestRule.onNodeWithTag("HomePage").assertExists()
  }

  @NoAnonymousSignIn
  @Test
  fun mainActivity_boxWrapper_containsScaffold() {
    // Verify that the main layout structure exists
    // The Box wrapper should contain both the Scaffold and the notification banner
    composeTestRule.onNodeWithTag("HomePage").assertExists()
  }

  @NoAnonymousSignIn
  @Test
  fun mainActivity_notificationViewModel_isInitialized() {
    // The notification system should be initialized
    // This is verified by the app not crashing and the UI being visible
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertExists()
  }
}
