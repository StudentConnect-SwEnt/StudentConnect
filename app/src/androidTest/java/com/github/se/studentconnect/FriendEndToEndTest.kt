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
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.activities.EventViewTestTags
import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreenTestTags
import com.github.se.studentconnect.ui.navigation.NavigationTestTags
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
class FriendEndToEndTest : FirestoreStudentConnectTest() {

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

  @NoAnonymousSignIn
  @Test
  fun friendshipFlow_throughEventAttendees() {
    val uniqueSuffix = System.currentTimeMillis()
    val shortSuffix = (uniqueSuffix % 10_000).toString()

    val eventTitle = "Friend Flow Event"
    val eventUid = createEventMadeByOtherUser(eventTitle)

    val user1Email = "friend_one_${uniqueSuffix}@example.com"
    val user2Email = "friend_two_${uniqueSuffix}@example.com"
    val user1Password = "FriendPassword123"
    val user2Password = "FriendPassword456"
    val user1Username = "alpha$shortSuffix"
    val user2Username = "bravo$shortSuffix"
    val user1FullName = "$user1Username first name $user1Username last name"
    val user2FullName = "$user2Username first name $user2Username last name"

    // 1 & 2. Both users join the same event
    joinEventAsUser(
        email = user1Email,
        password = user1Password,
        username = user1Username,
        eventTitle = eventTitle,
        eventUid = eventUid)

    joinEventAsUser(
        email = user2Email,
        password = user2Password,
        username = user2Username,
        eventTitle = eventTitle,
        eventUid = eventUid)

    // 3-5. First user opens attendees, visits second profile, sends request
    signInAndLaunchMain(email = user1Email, password = user1Password, username = user1Username)
    navigateToEventFromHome(eventTitle, eventUid)
    openAttendeesList()
    openAttendeeProfile(user2FullName)
    sendFriendRequestAndVerify()
    scenario.close()

    // 6-8. Second user accepts request and confirms friendship from friends list
    signInAndLaunchMain(email = user2Email, password = user2Password, username = user2Username)
    acceptFriendRequestFrom(user1FullName)
    navigateToProfileTab()
    openFriendsListFromProfile()
    openFriendProfileFromList(user1FullName)
    assertVisitorProfileShowsFriendship(user1FullName, user1Username)
    scenario.close()

    // 9-11. First user checks friendship from their friends list
    signInAndLaunchMain(email = user1Email, password = user1Password, username = user1Username)
    navigateToProfileTab()
    openFriendsListFromProfile()
    openFriendProfileFromList(user2FullName)
    assertVisitorProfileShowsFriendship(user2FullName, user2Username)
  }

  private fun signInAndLaunchMain(email: String, password: String, username: String) {
    runTest {
      FirebaseEmulator.auth.signOut()
      signInAs(email, password)
      createUserForCurrentUser(username)
    }
    launchActivityAndWaitForMainScreen()
  }

  private fun joinEventAsUser(
      email: String,
      password: String,
      username: String,
      eventTitle: String,
      eventUid: String
  ) {
    signInAndLaunchMain(email, password, username)
    navigateToEventFromHome(eventTitle, eventUid)
    joinOpenEvent()
    scenario.close()
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
    try {
      FirebaseEmulator.auth.createUserWithEmailAndPassword(email, password).await()
    } catch (_: Exception) {
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
    val eventRepository = EventRepositoryProvider.repository
    val eventUid = eventRepository.getNewUid()

    runTest {
      val uniqueSuffix = System.currentTimeMillis()
      val ownerEmail = "owner${uniqueSuffix}@example.com"
      val ownerPassword = "OwnerPassword123"
      signInAs(ownerEmail, ownerPassword)

      createUserForCurrentUser("owner")

      val startDate = Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)
      val endDate = Date(startDate.time + 2L * 60 * 60 * 1000)

      eventRepository.addEvent(
          Event.Public(
              uid = eventUid,
              ownerId = currentUser.uid,
              title = "$eventTitle $uniqueSuffix",
              description = "E2E event for friendship flow",
              location = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL Campus"),
              start = Timestamp(startDate),
              end = Timestamp(endDate),
              maxCapacity = 50u,
              participationFee = 0u,
              isFlash = false,
              subtitle = "Friend Flow",
              tags = listOf("campus", "friends")))
    }

    return eventUid
  }

  private fun navigateToEventFromHome(eventTitle: String, eventUid: String) {
    searchForEventInSearchScreen(eventTitle)
    openEventInHomeScreen(eventUid)
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

  private fun openAttendeesList() {
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_030, message = "participants info to be visible") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.PARTICIPANTS_INFO)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO).performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_031, message = "attendee list to be visible") {
          composeTestRule
              .onAllNodesWithTag(EventViewTestTags.ATTENDEE_LIST)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }

  private fun openAttendeeProfile(fullName: String) {
    val attendeeMatcher =
        hasTestTag(EventViewTestTags.ATTENDEE_LIST_ITEM) and
            hasAnyDescendant(hasText(fullName, substring = true))

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 30_030, message = "attendee $fullName to appear in list") {
          composeTestRule.onAllNodes(attendeeMatcher, useUnmergedTree = true).fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onAllNodes(attendeeMatcher, useUnmergedTree = true).onFirst().performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_032,
        message = "visitor profile to open for attendee $fullName") {
          composeTestRule
              .onAllNodesWithTag(C.Tag.visitor_profile_screen)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule
        .onNodeWithTag(C.Tag.visitor_profile_user_name)
        .assertExists()
        .assertTextEquals(fullName)
  }

  private fun sendFriendRequestAndVerify() {
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_030, message = "add friend button to be visible") {
          composeTestRule
              .onAllNodesWithTag(C.Tag.visitor_profile_add_friend)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onNodeWithTag(C.Tag.visitor_profile_add_friend).performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_001, message = "cancel friend button to appear after sending request") {
          composeTestRule
              .onAllNodesWithTag(C.Tag.visitor_profile_cancel_friend)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }

  private fun acceptFriendRequestFrom(fromUserFullName: String) {
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_033, message = "notification button to be visible") {
          composeTestRule
              .onAllNodesWithTag("NotificationButton")
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onNodeWithTag("NotificationButton").performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_034, message = "friend request notification to appear") {
          composeTestRule
              .onAllNodesWithText(
                  "$fromUserFullName sent you a friend request",
                  substring = true,
                  useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    val acceptButtonMatcher =
        SemanticsMatcher("accept notification button") {
          it.config.getOrNull(SemanticsProperties.TestTag)
              ?.startsWith(C.Tag.accept_notification_button_prefix) == true
        }

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_035, message = "accept friend request button to appear") {
          composeTestRule.onAllNodes(acceptButtonMatcher, useUnmergedTree = true).fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onAllNodes(acceptButtonMatcher, useUnmergedTree = true).onFirst().performClick()

    composeTestRule.onNodeWithTag("NotificationButton").performClick()
  }

  private fun navigateToProfileTab() {
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_031, message = "profile tab to be visible") {
          composeTestRule
              .onAllNodesWithTag(NavigationTestTags.PROFILE_TAB)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
  }

  private fun openFriendsListFromProfile() {
    val friendsStatMatcher =
        hasClickAction() and hasAnyDescendant(hasText("Friends", substring = false))

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_036, message = "friends stat to be visible on profile") {
          composeTestRule.onAllNodes(friendsStatMatcher, useUnmergedTree = true).fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.onAllNodes(friendsStatMatcher, useUnmergedTree = true).onFirst().performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_037, message = "friends list screen to appear") {
          composeTestRule
              .onAllNodesWithText("Search friends", substring = true, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }

  private fun openFriendProfileFromList(friendFullName: String) {
    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_038, message = "friend $friendFullName to appear in list") {
          composeTestRule
              .onAllNodesWithText(friendFullName, substring = true, useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule
        .onAllNodesWithText(friendFullName, substring = true, useUnmergedTree = true)
        .onFirst()
        .performClick()

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 20_003, message = "visitor profile to open from friend list") {
          composeTestRule
              .onAllNodesWithTag(C.Tag.visitor_profile_screen)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }

  private fun assertVisitorProfileShowsFriendship(fullName: String, username: String) {
    composeTestRule
        .onNodeWithTag(C.Tag.visitor_profile_user_name)
        .assertExists()
        .assertTextEquals(fullName)

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 15_032, message = "remove friend button to appear") {
          composeTestRule
              .onAllNodesWithTag(C.Tag.visitor_profile_remove_friend)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

    composeTestRule.waitUntilWithMessage(
        timeoutMillis = 10_777, message = "username @$username to be visible") {
          composeTestRule
              .onAllNodesWithText("@$username", useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
  }
}
