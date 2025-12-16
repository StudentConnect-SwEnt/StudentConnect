// this code was implemented with the help of gemini

package com.github.se.studentconnect

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class VisitorProfileE2ETest : FirestoreStudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var scenario: ActivityScenario<MainActivity>

  private suspend fun signInAs(email: String, password: String) {
    try {
      FirebaseEmulator.auth.createUserWithEmailAndPassword(email, password).await()
    } catch (_: Exception) {
      FirebaseEmulator.auth.signInWithEmailAndPassword(email, password).await()
    }
  }

  @Test
  fun search_user_opens_visitor_profile_and_displays_details() {
    val unique = System.currentTimeMillis()
    // keep username length within 3..20 by using a short numeric suffix
    val shortSuffix = (unique % 10000).toString()
    val testEmail = "e2e_search${unique}@example.com"
    val testPassword = "TestPassword123"

    // Seed data and sign in
    runTest {
      // Create and sign in as the visitor account, then save its profile. This ensures
      // Firestore rules allow writing the user document for the signed-in uid (same
      // approach as in EndToEndTest).
      val visitorEmail = "visitor${unique}@example.com"
      val visitorPassword = "VisitorPassword123"

      // Sign in as visitor and create their profile
      signInAs(visitorEmail, visitorPassword)

      val visitorUser =
          User(
              userId = currentUser.uid,
              email = visitorEmail,
              // username must be between 3 and 20 chars
              username = "ritan$shortSuffix",
              firstName = "Rita",
              lastName = "Naimi",
              university = "EPFL",
              bio = "This is Rita's bio for testing")

      UserRepositoryProvider.repository.saveUser(visitorUser)

      // Now sign in as the test user who will perform the search
      signInAs(testEmail, testPassword)

      // Create a profile for the test user as well so the app will skip the sign-up/onboarding
      // screen
      UserRepositoryProvider.repository.saveUser(
          User(
              userId = currentUser.uid,
              email = currentUser.email!!,
              // also ensure test username respects length limits
              username = "searcher$shortSuffix",
              firstName = "Search",
              lastName = "User",
              university = "EPFL"))
    }

    // Launch activity
    val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
    scenario = ActivityScenario.launch(intent)
    composeTestRule.waitForIdle()

    // Wait for main bottom nav to appear
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      try {
        composeTestRule
            .onAllNodesWithTag(
                com.github.se.studentconnect.ui.navigation.NavigationTestTags
                    .BOTTOM_NAVIGATION_MENU)
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: Exception) {
        false
      }
    }

    // Click on search input from home
    composeTestRule
        .onAllNodesWithTag(C.Tag.search_input_field, useUnmergedTree = true)
        .onFirst()
        .performClick()

    // Wait for search screen
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule.onAllNodesWithTag(C.Tag.search_screen).fetchSemanticsNodes().isNotEmpty()
    }

    // Type the first name to search
    val searchField =
        composeTestRule
            .onAllNodesWithTag(C.Tag.search_input_field, useUnmergedTree = true)
            .onFirst()
    searchField.performTextClearance()
    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()

    // Attempt horizontal swipes until the expected full name appears
    val expectedFullName = "Rita Naimi"
    val maxSwipes = 20
    var found = false
    repeat(maxSwipes) {
      try {
        if (composeTestRule
            .onAllNodesWithText(expectedFullName, substring = false)
            .fetchSemanticsNodes()
            .isNotEmpty()) {
          found = true
          return@repeat
        }
      } catch (_: Exception) {}

      // Swipe left on the root to move the horizontal list
      try {
        composeTestRule.onRoot().performTouchInput { swipeLeft() }
      } catch (_: Exception) {
        // fallback: small pause if swipe fails
        Thread.sleep(100)
      }

      composeTestRule.waitForIdle()
    }

    // If not found after swiping, the subsequent waitUntil will fail the test with a clear message
    if (!found) {
      composeTestRule.waitUntil(timeoutMillis = 15_000) {
        try {
          composeTestRule
              .onAllNodesWithText(expectedFullName, substring = false)
              .fetchSemanticsNodes()
              .isNotEmpty()
        } catch (_: Exception) {
          false
        }
      }
    }

    // Click the user card (first occurrence)
    composeTestRule.onAllNodesWithText(expectedFullName, substring = false).onFirst().performClick()

    // Wait for visitor profile screen
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithTag(C.Tag.visitor_profile_screen)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.waitForIdle()

    // Assertions: name, username, bio and add friend button
    composeTestRule
        .onNodeWithTag(C.Tag.visitor_profile_user_name)
        .assertExists()
        .assertTextEquals(expectedFullName)

    // username tag should show @ritan with unique suffix
    composeTestRule.onNodeWithText("@ritan$shortSuffix", useUnmergedTree = true).assertExists()

    composeTestRule
        .onNodeWithTag(C.Tag.visitor_profile_bio)
        .performScrollTo()
        .assertExists()
        .assertTextContains("This is Rita's bio for testing")

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).assertExists()
  }
}
