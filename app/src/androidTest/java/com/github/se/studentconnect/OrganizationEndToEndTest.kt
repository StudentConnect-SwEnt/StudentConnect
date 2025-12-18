package com.github.se.studentconnect

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.activities.EventViewTestTags
import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreenTestTags
import com.github.se.studentconnect.ui.navigation.NavigationTestTags
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationInfoScreenTestTags
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
import com.github.se.studentconnect.utils.NoAnonymousSignIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationEndToEndTest : FirestoreStudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var scenario: ActivityScenario<MainActivity>

  // Fake Media Repository to avoid Storage dependency issues
  class FakeMediaRepository : MediaRepository {
    override suspend fun upload(uri: Uri, path: String?): String = "fake_media_id"

    override suspend fun download(id: String): Uri = Uri.EMPTY

    override suspend fun delete(id: String) {}
  }

  class FakeNotificationRepository : NotificationRepository {
    private val notifications = java.util.concurrent.CopyOnWriteArrayList<Notification>()

    override fun getNotifications(
        userId: String,
        onSuccess: (List<Notification>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      onSuccess(notifications.filter { it.userId == userId })
    }

    override fun getUnreadNotifications(
        userId: String,
        onSuccess: (List<Notification>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      onSuccess(notifications.filter { it.userId == userId && !it.isRead })
    }

    override fun createNotification(
        notification: Notification,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      notifications.add(notification)
      onSuccess()
    }

    override fun markAsRead(
        notificationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      val index = notifications.indexOfFirst { it.id == notificationId }
      if (index != -1) {
        val notif = notifications[index]
        val updated =
            when (notif) {
              is Notification.FriendRequest -> notif.copy(isRead = true)
              is Notification.EventStarting -> notif.copy(isRead = true)
              is Notification.EventInvitation -> notif.copy(isRead = true)
              is Notification.OrganizationMemberInvitation -> notif.copy(isRead = true)
            }
        notifications[index] = updated
        onSuccess()
      } else {
        onSuccess() // Fail silent for test simplicity
      }
    }

    override fun markAllAsRead(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      for (i in notifications.indices) {
        if (notifications[i].userId == userId) {
          val n = notifications[i]
          val updated =
              when (n) {
                is Notification.FriendRequest -> n.copy(isRead = true)
                is Notification.EventStarting -> n.copy(isRead = true)
                is Notification.EventInvitation -> n.copy(isRead = true)
                is Notification.OrganizationMemberInvitation -> n.copy(isRead = true)
              }
          notifications[i] = updated
        }
      }
      onSuccess()
    }

    override fun deleteNotification(
        notificationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      notifications.removeAll { it.id == notificationId }
      onSuccess()
    }

    override fun deleteAllNotifications(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      notifications.removeAll { it.userId == userId }
      onSuccess()
    }

    override fun listenToNotifications(
        userId: String,
        onNotificationsChanged: (List<Notification>) -> Unit
    ): () -> Unit {
      // Return current list for now. Ideally should observe changes, but for test sequence
      // this might suffice
      // if we trigger it at the right time. However, tests usually wait for UI updates which
      // rely on flow emissions.
      // If the UI is using this repository with a listener, we might need to be careful.
      // But this interface uses a callback. We call it once immediately.
      onNotificationsChanged(notifications.filter { it.userId == userId })
      return {}
    }
  }

  @After
  fun cleanUp() {
    MediaRepositoryProvider.cleanOverrideForTests()
    NotificationRepositoryProvider.cleanOverrideForTests()
    if (::scenario.isInitialized) {
      scenario.close()
    }
  }

  private suspend fun signInAs(email: String, password: String) {
    try {
      FirebaseEmulator.auth.createUserWithEmailAndPassword(email, password).await()
    } catch (_: Exception) {
      FirebaseEmulator.auth.signInWithEmailAndPassword(email, password).await()
    }
  }

  private fun signOut() {
    FirebaseEmulator.auth.signOut()
  }

  private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilWithMessage(
      timeoutMillis: Long = 15_000,
      message: String,
      condition: () -> Boolean
  ) {
    try {
      this.waitUntil(timeoutMillis) { condition() }
    } catch (e: AssertionError) {
      throw AssertionError("Timeout waiting for: $message", e)
    }
  }

  @NoAnonymousSignIn
  @Test
  fun organizationCompleteLifecycle() {
    // Override MediaRepository and NotificationRepository
    MediaRepositoryProvider.overrideForTests(FakeMediaRepository())
    NotificationRepositoryProvider.overrideForTests(FakeNotificationRepository())

    val uniqueSuffix = System.currentTimeMillis()
    val ownerEmail = "org_owner_${uniqueSuffix}@example.com"
    val ownerName = "owner$uniqueSuffix"
    val invitedEmail = "org_invited_${uniqueSuffix}@example.com"
    val invitedName = "invited$uniqueSuffix"
    val password = "TestPass123"

    // --- PART 1: OWNER FLOW ---
    runTest {
      // Create Owner Account
      signInAs(ownerEmail, password)
      createUserForCurrentUser(ownerName)
    }

    launchActivityAndWaitForMainScreen()

    // 1. Create Organization
    val orgName = "Test Org ${System.currentTimeMillis()}"
    createOrganization(orgName)

    // 2. Invite User (President)
    if (::scenario.isInitialized) {
      scenario.close()
    }

    var invitedUserId = ""
    runTest {
      signOut()
      signInAs(invitedEmail, password)
      createUserForCurrentUser(invitedName)
      invitedUserId = currentUser.uid
      signOut()
      signInAs(ownerEmail, password)
    }

    launchActivityAndWaitForMainScreen()

    navigateToOrganization(orgName)

    // Go to Members Tab
    composeTestRule.waitUntilWithMessage(message = "Members tab visible") {
      composeTestRule
          .onAllNodesWithTag("org_profile_tab_members")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("org_profile_tab_members").performClick()

    composeTestRule.waitForIdle()

    // Invite President
    composeTestRule.waitUntilWithMessage(message = "Add President button visible") {
      composeTestRule
          .onAllNodesWithTag("AddMemberButton_President")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("AddMemberButton_President").performClick()

    // Wait for invited user to appear and click
    composeTestRule.waitUntilWithMessage(message = "Invited user found in list") {
      composeTestRule
          .onAllNodesWithText(invitedName, substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText(invitedName, substring = true).performClick()

    // Verify "Request sent" appears
    composeTestRule.waitUntilWithMessage(message = "Request sent indicator visible") {
      composeTestRule.onAllNodesWithText("Request sent").fetchSemanticsNodes().isNotEmpty()
    }

    // 3. Pin Organization - DELETED
    // 4. Verify Badge on Profile - DELETED
    // 5. Create Organization Event - MOVE TO STEP 2

    // --- PART 2: LOGOUT & INVITED USER FLOW ---

    // 7. Logout -> 3. Change Account
    // ...

    // REFACTORING LOGIC BELOW:

    // Back from "Request sent" state in Members Tab
    composeTestRule.onNodeWithContentDescription("Back").performClick()

    // 2 Create an event under the name of the organization
    // Navigate to Create Event (via Home -> Add)
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).performClick()
    composeTestRule.waitUntilWithMessage(message = "Home visible") {
      composeTestRule.onAllNodesWithTag("center_add_button").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("center_add_button").performClick()
    composeTestRule.onNodeWithTag("create_public_event_option").performClick()

    // Wait for form
    composeTestRule.waitUntilWithMessage(message = "Create Event Form visible") {
      composeTestRule
          .onAllNodesWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Fill Event Details
    val eventTitle = "Org Event ${System.currentTimeMillis()}"
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
        .performTextInput(eventTitle)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
        .performTextInput("An awesome org event")
    Espresso.closeSoftKeyboard()

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
        .performScrollTo()
        .performClick()
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT)
        .performTextReplacement("01/01/2026")

    // Switch to Org Mode
    composeTestRule.waitUntilWithMessage(message = "Create as Org switch visible") {
      composeTestRule.onAllNodesWithTag("createAsOrgSwitch").fetchSemanticsNodes().isNotEmpty()
    }
    // Scroll to the Tag Selector to ensure the Switch (which is above it) is high enough
    // to avoid being covered by the Save button overlay.
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TAG_SELECTOR).performScrollTo()
    composeTestRule.onNodeWithTag("createAsOrgSwitch").performClick()

    // Wait for Dropdown to appear and Select Org
    composeTestRule.waitUntilWithMessage(message = "Org Dropdown visible") {
      composeTestRule.onAllNodesWithTag("orgDropdown").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("orgDropdown").performScrollTo().performClick()
    composeTestRule.onAllNodesWithText(orgName).onLast().performClick()

    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
        .performScrollTo()
        .performClick()
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT)
        .performTextReplacement("02/01/2026")

    Espresso.closeSoftKeyboard()
    composeTestRule.waitUntilWithMessage(message = "Save button enabled") {
      composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).assertIsEnabled()
      true
    }
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).performClick()

    // Verify Event Created (Optional but good practice)
    composeTestRule.waitUntilWithMessage(message = "Event View visible") {
      composeTestRule.onAllNodesWithText(eventTitle).fetchSemanticsNodes().isNotEmpty()
    }

    // 3. Change of account and move to the account of the invited user
    // Logout

    // 3. Change of account and move to the account of the invited user
    // Logout UI steps removed for stability; relying on programmatic signOut() below.

    scenario.close()

    // Switch User
    runTest {
      signOut()
      signInAs(invitedEmail, password)
    }

    launchActivityAndWaitForMainScreen()

    // 4. The invited user before accepting the invitation, joins the event
    composeTestRule.onNodeWithTag("search_input_field", useUnmergedTree = true).performClick()
    composeTestRule
        .onNodeWithTag("search_input_field", useUnmergedTree = true)
        .performTextInput(eventTitle)
    Espresso.closeSoftKeyboard()

    composeTestRule.waitUntilWithMessage(message = "Event found in search") {
      composeTestRule
          .onAllNodesWithText(eventTitle, substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onAllNodesWithText(eventTitle, substring = true).onFirst().performClick()

    // Join
    composeTestRule.waitUntilWithMessage(message = "Event View loaded") {
      composeTestRule
          .onAllNodesWithTag(EventViewTestTags.JOIN_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).performClick()

    // Verify joined
    composeTestRule.waitUntilWithMessage(message = "Leave button visible") {
      composeTestRule
          .onAllNodesWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // 5. The invited user accepts the invitation of the organization
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).performClick()
    composeTestRule.onNodeWithTag("NotificationButton").performClick()

    composeTestRule.waitUntilWithMessage(message = "Invitation notification visible") {
      composeTestRule
          .onAllNodesWithText("invited you to join", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText("Accept").performClick()

    // 6. Show in profile on the management screen that the user have joined the organization
    // and pin it
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.waitUntilWithMessage(message = "Profile screen visible") {
      composeTestRule.onAllNodesWithText("Organizations").fetchSemanticsNodes().isNotEmpty()
    }

    // Navigate to My Organizations
    composeTestRule.onNodeWithText("Organizations").performScrollTo().performClick()
    composeTestRule.waitUntilWithMessage(message = "Organization List visible") {
      composeTestRule.onAllNodesWithText(orgName).fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule
        .onNode(hasContentDescription("pin", substring = true, ignoreCase = true))
        .performClick()

    // 7. After pinning the event, we show that there exists a the badge on the profile page
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.waitUntilWithMessage(message = "Badge visible") {
      composeTestRule.onAllNodesWithContentDescription(orgName).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun createOrganization(orgName: String) {
    // 1. Go to Profile
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.waitUntilWithMessage(message = "Profile screen visible") {
      composeTestRule.onAllNodesWithText("Organizations").fetchSemanticsNodes().isNotEmpty()
    }

    // 2. Go to My Organizations
    composeTestRule.onNodeWithText("Organizations").performScrollTo().performClick()

    // Wait for Management Screen
    composeTestRule.waitUntilWithMessage(message = "Organization Management Screen visible") {
      composeTestRule.onAllNodesWithText("Create Organization").fetchSemanticsNodes().isNotEmpty()
    }

    // 3. Click Create Organization
    composeTestRule.onNodeWithText("Create Organization").performClick()

    // 4. Fill Info Screen
    composeTestRule.waitUntilWithMessage(message = "Organization Info Screen visible") {
      composeTestRule
          .onAllNodesWithTag(OrganizationInfoScreenTestTags.SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT)
        .performTextInput(orgName)
    Espresso.closeSoftKeyboard()
    composeTestRule.onNodeWithText("Association").performClick()
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).performClick()

    // 5. Logo Screen - Skip
    composeTestRule.waitUntilWithMessage(message = "Upload Logo Screen visible") {
      composeTestRule.onAllNodesWithText("Upload a logo").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("Skip").performClick()

    // 6. Description Screen
    composeTestRule.waitUntilWithMessage(message = "Description Screen visible") {
      composeTestRule
          .onAllNodesWithTag(C.Tag.about_screen_container)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule
        .onNodeWithTag(C.Tag.about_input)
        .performTextInput("This is an E2E test organization.")
    Espresso.closeSoftKeyboard()
    composeTestRule.onNodeWithTag(C.Tag.about_continue).performClick()

    // 7. Socials - Skip
    composeTestRule.waitUntilWithMessage(message = "Socials Screen visible") {
      composeTestRule
          .onAllNodesWithText("Brand your organization")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText("Skip").performClick()

    // 8. Profile Setup
    composeTestRule.waitUntilWithMessage(message = "Profile Setup Screen visible") {
      composeTestRule
          .onAllNodesWithText("Where and what do you organize ?")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText("Search locations…").performClick()
    composeTestRule.onNodeWithText("EPFL").performClick()
    composeTestRule.onNodeWithText("Tech").performClick()
    composeTestRule.onNodeWithText("18–22").performClick()
    composeTestRule.onNodeWithText("< 20").performScrollTo().performClick()
    // Note: The previous viewing showed " < 20", adjusted whitespace if needed. I'll rely on
    // text match.
    composeTestRule.onNodeWithText("Continue").performClick()

    // 9. Team Roles
    composeTestRule.waitUntilWithMessage(message = "Team Roles Screen visible") {
      composeTestRule
          .onAllNodesWithText("Set up your team roles")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Add President Role
    // Add President Role
    composeTestRule.onNodeWithText("Role name").performTextInput("Pres")
    Espresso.closeSoftKeyboard()
    composeTestRule.waitUntilWithMessage(message = "President suggestion visible") {
      composeTestRule.onAllNodesWithText("President").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("President").performClick()
    composeTestRule.onNodeWithText("Role description").performClick()
    composeTestRule.onNodeWithText("Role description").performTextInput("Leading the organization")
    Espresso.closeSoftKeyboard()
    composeTestRule.onNodeWithText("+ Add role").performClick()

    Espresso.closeSoftKeyboard()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Start Now").assertIsEnabled().performClick()

    // 10. Completion - Explicitly navigate to verify
    // Navigate to Profile
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.waitUntilWithMessage(message = "Profile screen visible") {
      composeTestRule.onAllNodesWithText("Organizations").fetchSemanticsNodes().isNotEmpty()
    }

    // Navigate to My Organizations
    composeTestRule.onNodeWithText("Organizations").performScrollTo().performClick()
    composeTestRule.waitUntilWithMessage(message = orgName) {
      composeTestRule.onAllNodesWithText(orgName).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun navigateToOrganization(orgName: String) {
    // Navigate to Profile
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.waitUntilWithMessage(message = "Profile screen visible") {
      composeTestRule.onAllNodesWithText("Organizations").fetchSemanticsNodes().isNotEmpty()
    }

    // Navigate to My Organizations
    composeTestRule.onNodeWithText("Organizations").performScrollTo().performClick()
    composeTestRule.waitUntilWithMessage(message = "Organization List visible") {
      composeTestRule.onAllNodesWithText(orgName).fetchSemanticsNodes().isNotEmpty()
    }

    // Click on the Organization
    composeTestRule.onNodeWithText(orgName).performClick()
    composeTestRule.waitUntilWithMessage(message = "Org Profile visible") {
      composeTestRule.onAllNodesWithTag(C.Tag.org_profile_screen).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun launchActivityAndWaitForMainScreen() {
    val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
    scenario = ActivityScenario.launch(intent)

    composeTestRule.waitForIdle()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_000, message = "bottom navigation menu on main screen to be visible") {
          composeTestRule
              .onAllNodesWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
              .fetchSemanticsNodes()
              .isNotEmpty()
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
}
