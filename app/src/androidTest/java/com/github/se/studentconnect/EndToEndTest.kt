package com.github.se.studentconnect

import android.content.Intent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryFirestore
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.activities.EventViewTestTags
import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreenTestTags
import com.github.se.studentconnect.ui.navigation.NavigationTestTags
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
import com.github.se.studentconnect.utils.NoAnonymousSignIn
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class EndToEndTest : FirestoreStudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  // Helper that wraps waitUntil and throws a more descriptive error on timeout
  private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilWithMessage(
      timeoutMillis: Long = 10_000,
      message: String,
      condition: () -> Boolean
  ) {
    try {
      this.waitUntil(timeoutMillis) { condition() }
    } catch (e: AssertionError) {
      throw AssertionError("Timeout waiting for: $message", e)
    }
  }

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var scenario: ActivityScenario<MainActivity>

  override fun createInitializedRepository(): EventRepository {
    return EventRepositoryFirestore(db = FirebaseEmulator.firestore)
  }

  @NoAnonymousSignIn
  @Test
  fun endToEnd_completeUserFlow() {
    // Create a real test account with email and password BEFORE the activity starts
    // But DON'T create the user profile - we want to test the onboarding flow
    runTest {
      // Use a unique email per test run to avoid colliding with an existing account/profile
      val testEmail = "e2etest${System.currentTimeMillis()}@example.com"
      val testPassword = "TestPassword123"
      signInAs(testEmail, testPassword)

      // Create user for the joiner
      createUserForCurrentUser("owner")
    }

    // NOW launch the activity AFTER the user is authenticated
    launchActivityAndWaitForMainScreen()

    // Step 1: Create a public event
    createPublicEvent()

    // Step 2: Verify event is visible on home page
    verifyEventOnHomePage()

    // Step 3: Open event and edit the title
    openEventAndEditTitle()

    // Step 4: Verify event appears in activities (upcoming events)
    verifyEventInActivities()
  }

  @NoAnonymousSignIn
  @Test
  fun endToEnd_completeEventJoiningFlow() {
    // Create an event made by another user
    val joinEventTitle = "Join Flow Event"
    val joinEventUid = createEventMadeByOtherUser(joinEventTitle)

    // Sign in as the joiner
    runTest {
      val uniqueSuffix = System.currentTimeMillis()
      val testEmail = "e2etest${uniqueSuffix}@example.com"
      val testPassword = "TestPassword123"
      signInAs(testEmail, testPassword)

      // Create user for the joiner
      createUserForCurrentUser("owner")
    }

    // Launch MainActivity now
    launchActivityAndWaitForMainScreen()

    // Step 1: Search for event in search screen
    searchForEventInSearchScreen(joinEventTitle)

    // Step 2: Open event in home screen
    openEventInHomeScreen(joinEventUid)

    // Step 3: Join the event and check the leave button
    joinOpenEvent()
  }

  private fun launchActivityAndWaitForMainScreen() {
    val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
    scenario = ActivityScenario.launch(intent)

    composeTestRule.waitForIdle()

    // Wait until we're on the main screen
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_001, message = "bottom navigation menu on main screen to be visible") {
          composeTestRule
              .onAllNodesWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }

  private suspend fun signInAs(email: String, password: String) {
    // Sign in with email/password (or create if doesn't exist)
    try {
      FirebaseEmulator.auth.createUserWithEmailAndPassword(email, password).await()
    } catch (_: Exception) {
      // Account might already exist, try signing in
      FirebaseEmulator.auth.signInWithEmailAndPassword(email, password).await()
    }
  }

  private suspend fun createUserForCurrentUser(username: String) {
    val userRepository = UserRepositoryProvider.repository
    userRepository.saveUser(
        User(
            userId = currentUser.uid,
            email = currentUser.email!!,
            username = username,
            firstName = "$username first name",
            lastName = "$username last name",
            university = "EPFL",
            hobbies = listOf("Music", "Running")))
  }

  private fun createEventMadeByOtherUser(eventTitle: String): String {
    val eventUid = repository.getNewUid()

    runTest {
      val uniqueSuffix = System.currentTimeMillis()
      val ownerEmail = "owner${uniqueSuffix}@example.com"
      val ownerPassword = "OwnerPassword123"
      signInAs(ownerEmail, ownerPassword)

      // Create user for the owner
      createUserForCurrentUser("owner")

      // Create an event
      val startDate = Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)
      val endDate = Date(startDate.time + 2L * 60 * 60 * 1000)

      repository.addEvent(
          Event.Public(
              uid = eventUid,
              ownerId = currentUser.uid,
              title = "$eventTitle $uniqueSuffix",
              description = "Instrumentation event used to validate join flow",
              location = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL Campus"),
              start = Timestamp(startDate),
              end = Timestamp(endDate),
              maxCapacity = 50u,
              participationFee = 0u,
              isFlash = false,
              subtitle = "Join Flow",
              tags = listOf("campus", "music")))
    }

    return eventUid
  }

  private fun searchForEventInSearchScreen(eventTitle: String) {
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_016, message = "home search bar to become visible") {
          composeTestRule
              .onAllNodesWithTag(C.Tag.search_input_field, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule
        .onAllNodesWithTag(C.Tag.search_input_field, useUnmergedTree = true)
        .onFirst()
        .performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_017, message = "search screen to open from home search bar") {
          composeTestRule.onAllNodesWithTag(C.Tag.search_screen).fetchSemanticsNodes().isNotEmpty()
        }

    val searchField =
        composeTestRule
            .onAllNodesWithTag(C.Tag.search_input_field, useUnmergedTree = true)
            .onFirst()
    searchField.performTextClearance()
    searchField.performTextInput(eventTitle)

    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_018, message = "seeded event to appear in search results") {
          composeTestRule
              .onAllNodesWithText(eventTitle, substring = true, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }

  private fun openEventInHomeScreen(eventUid: String) {
    composeTestRule.onNodeWithTag(C.Tag.back_button).performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_017, message = "home screen to be visible after leaving search") {
          composeTestRule.onAllNodesWithTag("HomePage").fetchSemanticsNodes().isNotEmpty()
        }

    val eventCardTag = "event_card_title_$eventUid"

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_018, message = "join flow event card to be visible on home screen") {
          composeTestRule
              .onAllNodesWithTag(eventCardTag, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule
        .onNodeWithTag(eventCardTag, useUnmergedTree = true)
        .performScrollTo()
        .performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_019, message = "event view to open from the home event list") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.EVENT_VIEW_SCREEN)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }

  private fun joinOpenEvent() {
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_019, message = "join button to become visible on event view") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.JOIN_BUTTON)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_020, message = "leave button to appear after joining the event") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }

  private fun createPublicEvent() {
    composeTestRule.waitForIdle()

    // Wait for and click the center add button
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_005, message = "center add button to be visible") {
          composeTestRule.onAllNodesWithTag("center_add_button").fetchSemanticsNodes().isNotEmpty()
        }

    composeTestRule.onNodeWithTag("center_add_button").performClick()
    composeTestRule.waitForIdle()

    // Wait for and click create public event option
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 10_011, message = "create public event option to be visible") {
          composeTestRule
              .onAllNodesWithTag("create_public_event_option")
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onNodeWithTag("create_public_event_option").performClick()
    composeTestRule.waitForIdle()

    // Wait for the form to appear
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 10_012, message = "create event title input to appear") {
          composeTestRule
              .onAllNodesWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    // Fill in the event details
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
        .performClick()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
        .performTextInput("E2E Test Event")

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
        .performClick()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
        .performTextInput("Test event description")

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
        .performClick()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT)
        .performTextInput("EPFL")

    // Wait for location suggestion to appear
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_006, message = "Fake EPFL suggestion to appear") {
          composeTestRule
              .onAllNodes(hasText("Fake EPFL"), useUnmergedTree = true)
              .fetchSemanticsNodes(false)
              .isNotEmpty()
        }

    // Select the suggestion
    composeTestRule.onAllNodes(hasText("Fake EPFL"), useUnmergedTree = true).onLast().performClick()

    // Hide keyboard to verify visibility of next elements
    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()

    // --- FILL START DATE (TEXT INPUT) ---
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
        .performScrollTo()
        .performClick()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
        .performTextInput("01/01/2026") // Date future

    composeTestRule.waitForIdle()

    // --- FILL END DATE (TEXT INPUT) ---
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
        .performScrollTo()
        .performClick()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
        .performTextInput("01/01/2026")

    composeTestRule.waitForIdle()

    // Ensure keyboard is closed before trying to press Save (sometimes the button can be occluded)
    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()

    // Wait for save button to be enabled
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_007, message = "save button enabled in create event") {
          try {
            composeTestRule
                .onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON)
                .assertIsEnabled()
            true
          } catch (_: AssertionError) {
            false
          }
        }

    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.waitForIdle()
  }

  private fun verifyEventOnHomePage() {
    // Wait until we're back on the home screen
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_002, message = "Home page to be visible") {
          composeTestRule.onAllNodesWithTag("HomePage").fetchSemanticsNodes().isNotEmpty()
        }

    // Verify the event title appears somewhere on the home page
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_003, message = "E2E Test Event to appear on home page") {
          composeTestRule
              .onAllNodesWithText("E2E Test Event", substring = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    // Matcher for nodes whose testTag starts with the event card title prefix
    val titleTagStartsWith =
        SemanticsMatcher("testTag starts with event_card_title_") { node ->
          node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("event_card_title_") ==
              true
        }

    // Ensure we target only the event card title (not story text)
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 10_013, message = "event card title node to be present on home page") {
          try {
            composeTestRule
                .onAllNodes(
                    hasText("E2E Test Event", substring = true) and titleTagStartsWith,
                    useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
          } catch (_: Exception) {
            false
          }
        }
  }

  private fun openEventAndEditTitle() {
    // Click on the event to open it. Prefer the event card title node identified by a testTag
    // that starts with "event_card_title_" to avoid clicking other views that also contain the
    // same text (stories, etc.). Fall back to clicking any text node if necessary.
    val titleTagStartsWith =
        SemanticsMatcher("testTag starts with event_card_title_") { node ->
          node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("event_card_title_") ==
              true
        }

    try {
      // Wait until a matching event card title is present
      composeTestRule.waitUntilWithMessage(
          timeoutMillis = 10_014, message = "matching event card title to appear on home page") {
            try {
              composeTestRule
                  .onAllNodes(
                      hasText("E2E Test Event", substring = true) and titleTagStartsWith,
                      useUnmergedTree = true)
                  .fetchSemanticsNodes()
                  .isNotEmpty()
            } catch (_: Exception) {
              false
            }
          }

      composeTestRule
          .onAllNodes(
              hasText("E2E Test Event", substring = true) and titleTagStartsWith,
              useUnmergedTree = true)
          .onFirst()
          .performClick()
    } catch (_: Exception) {
      // Fallback: click the first node that contains the event title text
      composeTestRule
          .onAllNodesWithText("E2E Test Event", substring = true, useUnmergedTree = true)
          .onFirst()
          .performClick()
    }

    composeTestRule.waitForIdle()

    // Wait for event view to load
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_004, message = "event view screen to load") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.EVENT_VIEW_SCREEN)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    // Wait for edit button to be available
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_008, message = "edit event button to appear on event view") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.EDIT_EVENT_BUTTON)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onNodeWithTag(EventViewTestTags.EDIT_EVENT_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Wait for edit screen to appear
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_009, message = "title input on edit event screen to appear") {
          composeTestRule
              .onAllNodesWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    // Clear and update the title
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
        .performClick()
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
        .performTextInput("E2E Test Event Updated")

    // Save the changes
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Wait to be back on the event view screen
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_005, message = "event view screen to be visible after save") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.EVENT_VIEW_SCREEN)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    // Verify the updated title is displayed
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_001, message = "updated event title to appear on event view") {
          composeTestRule
              .onAllNodesWithText("E2E Test Event Updated", substring = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    // Go back to home
    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).performClick()

    composeTestRule.waitForIdle()
  }

  private fun verifyEventInActivities() {
    // 1. Navigate to Activities tab
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_010, message = "activities tab to be visible") {
          composeTestRule
              .onAllNodesWithTag(NavigationTestTags.ACTIVITIES_TAB)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).performClick()

    composeTestRule.waitForIdle()

    // 2. Wait for activities carousel to appear
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_006, message = "activities carousel to appear") {
          composeTestRule
              .onAllNodesWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    // 3. Verify the event is present in the carousel
    val eventTitle = "E2E Test Event Updated"

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_007, message = "event to appear in activities carousel") {
          composeTestRule
              .onAllNodesWithText(eventTitle, substring = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    // 4. Click on the Event Card
    // FIX 1: performScrollTo() ensures the item is fully on screen before clicking
    composeTestRule
        .onAllNodesWithText(eventTitle, substring = true, useUnmergedTree = true)
        .onFirst()
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()

    // 5. Wait for Event View to open
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_008, message = "event view screen to open from activities") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.EVENT_VIEW_SCREEN)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    // 6. Verify we are on the event details and Edit button appears
    // FIX 2: Use waitUntil for the button. The button's visibility depends on
    // async data fetching (checking owner ID), so it appears slightly after the screen.
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 10_015, message = "edit event button to appear in event view") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.EDIT_EVENT_BUTTON)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onNodeWithTag(EventViewTestTags.EDIT_EVENT_BUTTON).assertExists()

    // 7. Go back to Activities
    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // 8. Verify we are back on Activities
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_011, message = "activities screen to be visible after back navigation") {
          composeTestRule
              .onAllNodesWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }
}
