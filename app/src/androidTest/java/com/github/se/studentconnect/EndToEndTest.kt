package com.github.se.studentconnect

import android.content.Intent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryFirestore
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.activities.EventViewTestTags
import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreenTestTags
import com.github.se.studentconnect.ui.navigation.NavigationTestTags
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.signup.BasicInfoScreenTestTags
import com.github.se.studentconnect.ui.screen.signup.SignUpViewModel
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class EndToEndTest : FirestoreStudentConnectTest(signInAnonymouslyIfPossible = false) {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var scenario: ActivityScenario<MainActivity>

  override fun createInitializedRepository(): EventRepository {
    return EventRepositoryFirestore(db = FirebaseEmulator.firestore)
  }

  @Before
  override fun setUp() {
    super.setUp()

    // Create a real test account with email and password BEFORE the activity starts
    // But DON'T create the user profile - we want to test the onboarding flow
    runTest {
      // Use a unique email per test run to avoid colliding with an existing account/profile
      val testEmail = "e2etest${System.currentTimeMillis()}@example.com"
      val testPassword = "TestPassword123"

      // Sign in with email/password (or create if doesn't exist)
      try {
        FirebaseEmulator.auth.createUserWithEmailAndPassword(testEmail, testPassword).await()
      } catch (_: Exception) {
        // Account might already exist, try signing in
        FirebaseEmulator.auth.signInWithEmailAndPassword(testEmail, testPassword).await()
      }
    }

    // NOW launch the activity AFTER the user is authenticated
    val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
    scenario = ActivityScenario.launch(intent)
  }

  @Test
  fun endToEnd_completeUserFlow() {
    composeTestRule.waitForIdle()

    // Fill in the onboarding forms
    fillOnboardingForms()

    // Wait until we're on the main screen after onboarding
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      composeTestRule
          .onAllNodesWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Step 1: Create a public event
    createPublicEvent()

    // Step 2: Verify event is visible on home page
    verifyEventOnHomePage()

    // Step 3: Open event and edit the title
    openEventAndEditTitle()

    // Step 4: Verify event appears in activities (upcoming events)
    verifyEventInActivities()

    // Step 5: Edit profile (name and country)
    editProfile()
  }

  private fun fillOnboardingForms() {
    val cal = Calendar.getInstance()
    cal.add(Calendar.YEAR, -20)
    cal.set(Calendar.DAY_OF_MONTH, 15)
    val defaultMillis = cal.timeInMillis
    scenario.onActivity { activity ->
      val vm = ViewModelProvider(activity).get(SignUpViewModel::class.java)
      vm.setBirthdate(Timestamp(Date(defaultMillis)))
    }

    composeTestRule.waitForIdle()

    val uniqueUsername = "johndoe${System.currentTimeMillis()}"

    // Fill first name
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithTag(BasicInfoScreenTestTags.FIRST_NAME_INPUT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(BasicInfoScreenTestTags.FIRST_NAME_INPUT).performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(BasicInfoScreenTestTags.FIRST_NAME_INPUT).performTextClearance()

    composeTestRule.onNodeWithTag(BasicInfoScreenTestTags.FIRST_NAME_INPUT).performTextInput("John")

    composeTestRule.waitForIdle()

    // Close keyboard
    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()

    // Fill last name
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithTag(BasicInfoScreenTestTags.LAST_NAME_INPUT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(BasicInfoScreenTestTags.LAST_NAME_INPUT).performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(BasicInfoScreenTestTags.LAST_NAME_INPUT).performTextClearance()

    composeTestRule.onNodeWithTag(BasicInfoScreenTestTags.LAST_NAME_INPUT).performTextInput("Doe")

    composeTestRule.waitForIdle()

    // Close keyboard
    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()

    // Fill username
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithTag(BasicInfoScreenTestTags.USERNAME_INPUT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(BasicInfoScreenTestTags.USERNAME_INPUT).performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(BasicInfoScreenTestTags.USERNAME_INPUT).performTextClearance()

    composeTestRule
        .onNodeWithTag(BasicInfoScreenTestTags.USERNAME_INPUT)
        .performTextInput(uniqueUsername)

    composeTestRule.waitForIdle()

    // Close keyboard
    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      try {
        composeTestRule
            .onAllNodesWithTag(BasicInfoScreenTestTags.CONTINUE_BUTTON)
            .fetchSemanticsNodes()
            .isNotEmpty() &&
            composeTestRule
                .onNodeWithTag(BasicInfoScreenTestTags.CONTINUE_BUTTON)
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.Disabled) == null
      } catch (_: Exception) {
        false
      }
    }

    // Click Continue on BasicInfo
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithTag(BasicInfoScreenTestTags.CONTINUE_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(BasicInfoScreenTestTags.CONTINUE_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Step 2: Fill Nationality screen
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithText("Where are you from", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Search for Switzerland
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithText("Search countries", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNode(hasSetTextAction() and hasText("Search countries", substring = true))
        .performClick()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNode(hasSetTextAction() and hasText("Search countries", substring = true))
        .performTextInput("Switzerland")

    composeTestRule.waitForIdle()

    // Close keyboard
    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()

    // Select Switzerland from the list
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithText("Switzerland", substring = true)
          .fetchSemanticsNodes()
          .size > 1
    }

    composeTestRule.onAllNodesWithText("Switzerland", substring = true).onLast().performClick()

    composeTestRule.waitForIdle()

    // Click Continue on Nationality
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodes(hasText("Continue") and hasClickAction())
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onAllNodes(hasText("Continue") and hasClickAction()).onFirst().performClick()

    composeTestRule.waitForIdle()

    // Step 3: Skip AddPicture screen
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithText("Skip", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNode(hasText("Skip") and hasClickAction()).performClick()

    composeTestRule.waitForIdle()

    // Step 4: Skip Description screen
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithText("Skip", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNode(hasText("Skip") and hasClickAction()).performClick()

    composeTestRule.waitForIdle()

    // Step 5: Experiences screen - select activities (chips) and Start Now
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      try {
        composeTestRule
            .onAllNodesWithTag(C.Tag.experiences_screen_container)
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: Exception) {
        false
      }
    }

    // Wait for filter list and topic grid
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule
            .onAllNodesWithTag(C.Tag.experiences_filter_list)
            .fetchSemanticsNodes()
            .isNotEmpty() &&
            composeTestRule
                .onAllNodesWithTag(C.Tag.experiences_topic_grid)
                .fetchSemanticsNodes()
                .isNotEmpty()
      } catch (_: Exception) {
        false
      }
    }

    // Select a topic from the default filter (Sports) if available
    try {
      composeTestRule
          .onNodeWithTag("${C.Tag.experiences_topic_chip_prefix}_Football", useUnmergedTree = true)
          .performScrollTo()
          .performClick()
    } catch (_: Exception) {
      // ignore if not present
    }

    composeTestRule.waitForIdle()

    // Switch to Music filter and select topics
    try {
      composeTestRule.onNodeWithTag("${C.Tag.experiences_filter_chip_prefix}_Music").performClick()
      composeTestRule.waitForIdle()
      composeTestRule
          .onNodeWithTag("${C.Tag.experiences_topic_chip_prefix}_Choir", useUnmergedTree = true)
          .performScrollTo()
          .performClick()
      composeTestRule
          .onNodeWithTag("${C.Tag.experiences_topic_chip_prefix}_Guitar", useUnmergedTree = true)
          .performScrollTo()
          .performClick()
    } catch (_: Exception) {
      // ignore if nodes not found
    }

    composeTestRule.waitForIdle()

    // Wait for the CTA button to be visible
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule.onAllNodesWithTag(C.Tag.experiences_cta).fetchSemanticsNodes().isNotEmpty()
      } catch (_: Exception) {
        false
      }
    }

    // Click Start Now CTA using the proper test tag from C.kt
    composeTestRule.onNodeWithTag(C.Tag.experiences_cta, useUnmergedTree = true).performClick()

    composeTestRule.waitForIdle()
  }

  private fun createPublicEvent() {
    composeTestRule.waitForIdle()

    // Wait for and click the center add button
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule.onAllNodesWithTag("center_add_button").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("center_add_button").performClick()
    composeTestRule.waitForIdle()

    // Wait for and click create public event option
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithTag("create_public_event_option")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag("create_public_event_option").performClick()
    composeTestRule.waitForIdle()

    // Wait for the form to appear
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
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
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
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
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      try {
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).assertIsEnabled()
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
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      composeTestRule.onAllNodesWithTag("HomePage").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify the event title appears somewhere on the home page
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
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
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
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
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
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
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      composeTestRule
          .onAllNodesWithTag(EventViewTestTags.EVENT_VIEW_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Wait for edit button to be available
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithTag(EventViewTestTags.EDIT_EVENT_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.EDIT_EVENT_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Wait for edit screen to appear
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
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
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      composeTestRule
          .onAllNodesWithTag(EventViewTestTags.EVENT_VIEW_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify the updated title is displayed
    composeTestRule.waitUntil(timeoutMillis = 20_000) {
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
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithTag(NavigationTestTags.ACTIVITIES_TAB)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).performClick()

    composeTestRule.waitForIdle()

    // 2. Wait for activities carousel to appear
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      composeTestRule
          .onAllNodesWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // 3. Verify the event is present in the carousel
    val eventTitle = "E2E Test Event Updated"

    composeTestRule.waitUntil(timeoutMillis = 30_000) {
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
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      composeTestRule
          .onAllNodesWithTag(EventViewTestTags.EVENT_VIEW_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // 6. Verify we are on the event details and Edit button appears
    // FIX 2: Use waitUntil for the button. The button's visibility depends on
    // async data fetching (checking owner ID), so it appears slightly after the screen.
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
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
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  private fun editProfile() {
    // 1. Navigate to Profile tab
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithTag(NavigationTestTags.PROFILE_TAB)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.waitForIdle()

    // 2. Wait for profile to load (checking for Edit Name button)
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      composeTestRule
          .onAllNodesWithContentDescription("Edit Name")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // --- SECTION 1: EDIT NAME ---
    composeTestRule.onNodeWithContentDescription("Edit Name").performClick()
    composeTestRule.waitForIdle()

    // Wait for edit name screen
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithText("First Name", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Edit first name
    composeTestRule.onAllNodesWithText("First Name", substring = true).onFirst().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodes(hasSetTextAction() and isFocused(), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNode(hasSetTextAction() and isFocused(), useUnmergedTree = true)
        .performTextClearance()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNode(hasSetTextAction() and isFocused(), useUnmergedTree = true)
        .performTextInput("habibi")
    composeTestRule.waitForIdle()

    // Edit last name
    composeTestRule.onAllNodesWithText("Last Name", substring = true).onFirst().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodes(hasSetTextAction() and isFocused(), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNode(hasSetTextAction() and isFocused(), useUnmergedTree = true)
        .performTextClearance()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNode(hasSetTextAction() and isFocused(), useUnmergedTree = true)
        .performTextInput("Doe")
    composeTestRule.waitForIdle()

    // Save changes (Name)
    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()
    try {
      composeTestRule.onRoot(useUnmergedTree = true).performClick()
      composeTestRule.waitForIdle()
    } catch (_: Exception) {}

    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodes(hasText("Save", substring = true), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onAllNodes(hasText("Save", substring = true), useUnmergedTree = true)
        .onFirst()
        .performClick()

    composeTestRule.waitForIdle()

    // Wait to be back on profile screen
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      composeTestRule
          .onAllNodesWithContentDescription("Edit Name")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify name was updated
    composeTestRule.waitUntil(timeoutMillis = 20_000) {
      composeTestRule
          .onAllNodesWithText("habibi Doe", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // --- SECTION 2: EDIT COUNTRY (NATIONALITY) ---

    // 1. Wait for the pencil icon next to "Country".
    // The label in ProfileSettingsScreen is "Country", so the icon description is "Edit Country".
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithContentDescription("Edit Country")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // 2. Click the pencil icon.
    // ProfileSettingsScreen IS scrollable, so performScrollTo is required here.
    composeTestRule.onNodeWithContentDescription("Edit Country").performScrollTo().performClick()

    composeTestRule.waitForIdle()

    // 3. Wait for EditNationalityScreen ("Where are you from ?")
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule
          .onAllNodesWithText("Where are you from", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // 4. Search for Tunisia (using logic from signup test)
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithText("Search countries", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNode(hasSetTextAction() and hasText("Search countries", substring = true))
        .performClick()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNode(hasSetTextAction() and hasText("Search countries", substring = true))
        .performTextInput("Tunisia")

    composeTestRule.waitForIdle()

    // 5. Close keyboard to ensure list item and Save button are visible
    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()

    // 6. Select Tunisia from the list (wait for filtering)
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithText("Tunisia", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Click on the country item (Using onLast() like in your example, assuming it's the list item)
    composeTestRule.onAllNodesWithText("Tunisia", substring = true).onLast().performClick()

    composeTestRule.waitForIdle()

    // 7. Click "Save Changes"
    // In EditNationalityScreen.kt, the button text is specifically "Save Changes"
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodes(hasText("Save Changes", substring = true) and hasClickAction())
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNode(hasText("Save Changes", substring = true) and hasClickAction())
        .performClick()

    composeTestRule.waitForIdle()

    // 8. Wait to be back on Profile
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      composeTestRule
          .onAllNodesWithContentDescription("Edit Name")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // 9. Verify Country is updated
    composeTestRule.waitUntil(timeoutMillis = 20_000) {
      composeTestRule
          .onAllNodesWithText("Tunisia", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }
}
