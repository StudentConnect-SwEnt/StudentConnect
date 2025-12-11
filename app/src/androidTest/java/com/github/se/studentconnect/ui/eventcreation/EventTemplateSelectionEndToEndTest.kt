// package com.github.se.studentconnect
//
// import android.content.Intent
// import androidx.compose.ui.test.*
// import androidx.compose.ui.test.junit4.createComposeRule
// import androidx.test.core.app.ActivityScenario
// import androidx.test.core.app.ApplicationProvider
// import androidx.test.ext.junit.runners.AndroidJUnit4
// import androidx.test.rule.GrantPermissionRule
// import com.github.se.studentconnect.model.event.Event
// import com.github.se.studentconnect.model.event.EventRepositoryProvider
// import com.github.se.studentconnect.model.location.Location
// import com.github.se.studentconnect.model.user.User
// import com.github.se.studentconnect.model.user.UserRepositoryProvider
// import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreenTestTags
// import com.github.se.studentconnect.ui.eventcreation.EventTemplateSelectionScreenTestTags
// import com.github.se.studentconnect.ui.navigation.NavigationTestTags
// import com.github.se.studentconnect.utils.FirebaseEmulator
// import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
// import com.github.se.studentconnect.utils.NoAnonymousSignIn
// import com.google.firebase.Timestamp
// import java.util.Date
// import kotlinx.coroutines.tasks.await
// import kotlinx.coroutines.test.runTest
// import org.junit.Rule
// import org.junit.Test
// import org.junit.runner.RunWith
//
// @RunWith(AndroidJUnit4::class)
// class EventTemplateSelectionEndToEndTest : FirestoreStudentConnectTest() {
//
//  @get:Rule val composeTestRule = createComposeRule()
//
//  @get:Rule
//  val permissionRule: GrantPermissionRule =
//      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)
//
//  private lateinit var scenario: ActivityScenario<MainActivity>
//
//  private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilWithMessage(
//      timeoutMillis: Long = 10_000,
//      message: String,
//      condition: () -> Boolean
//  ) {
//    try {
//      this.waitUntil(timeoutMillis) { condition() }
//    } catch (e: AssertionError) {
//      throw AssertionError("Timeout waiting for: $message", e)
//    }
//  }
//
//  @NoAnonymousSignIn
//  @Test
//  fun endToEnd_createEventFromTemplateFlow() {
//    val templateTitle = "My Template Event"
//
//    // 1. Setup: Sign in and create a past event to use as a template
//    runTest {
//      val uniqueSuffix = System.currentTimeMillis()
//      val testEmail = "template_user_$uniqueSuffix@example.com"
//      val testPassword = "Password123"
//
//      signInAs(testEmail, testPassword)
//      createUserForCurrentUser("template_user")
//
//      // Create an event owned by THIS user so it appears in "My Events"
//      createEventForCurrentUser(templateTitle)
//    }
//
//    // 2. Launch the app
//    launchActivityAndWaitForMainScreen()
//
//    // 3. Open the "Add" menu (Bottom Sheet)
//    composeTestRule.waitUntilWithMessage(
//        timeoutMillis = 10_000, message = "center add button to be visible") {
//
// composeTestRule.onAllNodesWithTag("center_add_button").fetchSemanticsNodes().isNotEmpty()
//        }
//
//    composeTestRule.onNodeWithTag("center_add_button").performClick()
//    composeTestRule.waitForIdle()
//
//    // 4. Select "Create from Template" option
//    // UPDATED: Using Text matching since the Tag might be missing/different
//    composeTestRule.waitUntilWithMessage(
//        timeoutMillis = 5_000, message = "create from template option to be visible") {
//          composeTestRule
//              .onAllNodesWithText("Create from Template")
//              .fetchSemanticsNodes()
//              .isNotEmpty()
//        }
//
//    composeTestRule.onNodeWithText("Create from Template").performClick()
//    composeTestRule.waitForIdle()
//
//    // 5. Wait for the Template Selection Screen to load
//    composeTestRule.waitUntilWithMessage(
//        timeoutMillis = 10_000, message = "template selection list to appear") {
//          composeTestRule
//              .onAllNodesWithTag(EventTemplateSelectionScreenTestTags.EVENT_LIST)
//              .fetchSemanticsNodes()
//              .isNotEmpty()
//        }
//
//    // 6. Select the template event we created
//    composeTestRule.waitUntilWithMessage(
//        timeoutMillis = 5_000, message = "template event card to appear") {
//          composeTestRule.onAllNodesWithText(templateTitle).fetchSemanticsNodes().isNotEmpty()
//        }
//
//    composeTestRule.onNodeWithText(templateTitle).performClick()
//    composeTestRule.waitForIdle()
//
//    // 7. Verify we navigated to "Create Public Event" and fields are pre-filled
//    composeTestRule.waitUntilWithMessage(
//        timeoutMillis = 10_000, message = "create public event screen to appear") {
//          composeTestRule
//              .onAllNodesWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
//              .fetchSemanticsNodes()
//              .isNotEmpty()
//        }
//
//    composeTestRule
//        .onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
//        .assertTextContains(templateTitle)
//
//    composeTestRule
//        .onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
//        .assertTextContains("Template description used for E2E test")
//  }
//
//  // --- Helpers ---
//
//  private fun launchActivityAndWaitForMainScreen() {
//    val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
//    scenario = ActivityScenario.launch(intent)
//    composeTestRule.waitForIdle()
//    composeTestRule.waitUntilWithMessage(
//        timeoutMillis = 30_000, message = "bottom navigation menu to be visible") {
//          composeTestRule
//              .onAllNodesWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
//              .fetchSemanticsNodes()
//              .isNotEmpty()
//        }
//  }
//
//  private suspend fun signInAs(email: String, password: String) {
//    try {
//      FirebaseEmulator.auth.createUserWithEmailAndPassword(email, password).await()
//    } catch (_: Exception) {
//      FirebaseEmulator.auth.signInWithEmailAndPassword(email, password).await()
//    }
//  }
//
//  private suspend fun createUserForCurrentUser(username: String) {
//    val userRepository = UserRepositoryProvider.repository
//    userRepository.saveUser(
//        User(
//            userId = currentUser.uid,
//            email = currentUser.email!!,
//            username = username,
//            firstName = "$username first",
//            lastName = "$username last",
//            university = "EPFL",
//            hobbies = listOf("Testing")))
//  }
//
//  private suspend fun createEventForCurrentUser(title: String) {
//    val eventRepository = EventRepositoryProvider.repository
//    val eventUid = eventRepository.getNewUid()
//    val startDate = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
//    val endDate = Date(startDate.time + 2 * 60 * 60 * 1000)
//
//    eventRepository.addEvent(
//        Event.Public(
//            uid = eventUid,
//            ownerId = currentUser.uid,
//            title = title,
//            description = "Template description used for E2E test",
//            location = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL"),
//            start = Timestamp(startDate),
//            end = Timestamp(endDate),
//            maxCapacity = 100u,
//            participationFee = 0u,
//            isFlash = false,
//            subtitle = "Template Subtitle",
//            tags = listOf("template", "e2e")))
//  }
// }
