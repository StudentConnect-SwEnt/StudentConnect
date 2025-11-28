package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.profile.JoinedEventsViewModel
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JoinedEventsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockEventRepository: MockEventRepository
  private lateinit var mockUserRepository: MockUserRepository

  @Before
  fun setUp() {
    mockEventRepository = MockEventRepository()
    mockUserRepository = MockUserRepository()
  }

  @Test
  fun joinedEventsScreen_displaysTitle() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("My Events").assertIsDisplayed()
  }

  @Test
  fun joinedEventsScreen_displaysBackButton() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun joinedEventsScreen_backButtonTriggersNavigation() {
    var backCalled = false
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent {
      JoinedEventsScreen(viewModel = viewModel, onNavigateBack = { backCalled = true })
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.BACK_BUTTON).performClick()

    assert(backCalled)
  }

  @Test
  fun joinedEventsScreen_displaysSearchBar() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Search events…").assertIsDisplayed()
  }

  @Test
  fun joinedEventsScreen_displaysTabs() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Past").assertIsDisplayed()
    composeTestRule.onNodeWithText("Upcoming").assertIsDisplayed()
  }

  @Test
  fun joinedEventsScreen_pastTabIsSelectedByDefault() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.tab("Past")).assertIsSelected()
  }

  @Test
  fun joinedEventsScreen_canSwitchToUpcomingTab() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.tab("Upcoming")).performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.tab("Upcoming")).assertIsSelected()
  }

  @Test
  fun joinedEventsScreen_searchBarHasCorrectPlaceholder() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Search events…").assertExists()
  }

  @Test
  fun joinedEventsScreen_topAppBarDisplaysCorrectTitle() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("My Events").assertExists()
  }

  @Test
  fun joinedEventsScreen_hasTopAppBar() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.TOP_APP_BAR).assertExists()
  }

  @Test
  fun joinedEventsScreen_hasTabRow() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.TAB_ROW).assertExists()
  }

  @Test
  fun joinedEventsScreen_screenExists() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.JOINED_EVENTS_SCREEN).assertExists()
  }

  @Test
  fun joinedEventsScreen_backButtonHasCorrectContentDescription() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Back to Profile").assertExists()
  }

  @Test
  fun joinedEventsScreen_searchIconIsDisplayed() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Search").assertExists()
  }

  @Test
  fun joinedEventsScreen_canClickBetweenTabs() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    // Initially Past is selected
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.tab("Past")).assertIsSelected()

    // Click Upcoming
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.tab("Upcoming")).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.tab("Upcoming")).assertIsSelected()

    // Click back to Past
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.tab("Past")).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.tab("Past")).assertIsSelected()
  }

  @Test
  fun joinedEventsScreen_searchBarAcceptsInput() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    // Type text in search bar
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.SEARCH_BAR).performTextInput("party")

    composeTestRule.waitForIdle()

    // Verify the text was entered
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.SEARCH_BAR).assertTextContains("party")
  }

  @Test
  fun joinedEventsScreen_searchBarCanBeClearedAndRefilled() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    // Type text
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.SEARCH_BAR).performTextInput("first")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.SEARCH_BAR).assertTextContains("first")

    // Clear and type new text
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.SEARCH_BAR).performTextClearance()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.SEARCH_BAR).performTextInput("second")
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(JoinedEventsScreenTestTags.SEARCH_BAR)
        .assertTextContains("second")
  }

  @Test
  fun joinedEventsScreen_searchBarIsClickable() {
    val viewModel =
        JoinedEventsViewModel(
            eventRepository = mockEventRepository, userRepository = mockUserRepository)

    composeTestRule.setContent { JoinedEventsScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    // Search bar should be clickable
    composeTestRule.onNodeWithTag(JoinedEventsScreenTestTags.SEARCH_BAR).assertHasClickAction()
  }

  // Mock repositories
  private class MockUserRepository(var joinedEvents: List<String> = emptyList()) : UserRepository {

    override suspend fun getUserById(userId: String) = null

    override suspend fun getJoinedEvents(userId: String): List<String> {
      delay(50)
      return joinedEvents
    }

    override suspend fun saveUser(user: com.github.se.studentconnect.model.User) = Unit

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = emptyList<com.github.se.studentconnect.model.User>()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        emptyList<com.github.se.studentconnect.model.User>() to false

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

    override suspend fun deleteUser(userId: String) = Unit

    override suspend fun getUsersByUniversity(university: String) =
        emptyList<com.github.se.studentconnect.model.User>()

    override suspend fun getUsersByHobby(hobby: String) =
        emptyList<com.github.se.studentconnect.model.User>()

    override suspend fun getNewUid() = "new_uid"

    override suspend fun addEventToUser(eventId: String, userId: String) = Unit

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        Unit

    override suspend fun getInvitations(userId: String) =
        emptyList<com.github.se.studentconnect.ui.screen.activities.Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit

    override suspend fun addFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun getFavoriteEvents(userId: String) = emptyList<String>()

    override suspend fun checkUsernameAvailability(username: String) = true
  }

  private class MockEventRepository(var events: List<Event> = emptyList()) : EventRepository {

    override fun getNewUid() = "new_uid"

    override suspend fun getAllVisibleEvents(): List<Event> {
      delay(50)
      return events
    }

    override suspend fun getAllVisibleEventsSatisfying(predicate: (Event) -> Boolean): List<Event> {
      delay(50)
      return events.filter(predicate)
    }

    override suspend fun getEvent(eventUid: String): Event {
      delay(50)
      return events.find { it.uid == eventUid } ?: throw Exception("Event not found: $eventUid")
    }

    override suspend fun getEventParticipants(eventUid: String) =
        emptyList<com.github.se.studentconnect.model.event.EventParticipant>()

    override suspend fun addEvent(event: Event) = Unit

    override suspend fun editEvent(eventUid: String, newEvent: Event) = Unit

    override suspend fun deleteEvent(eventUid: String) = Unit

    override suspend fun addParticipantToEvent(
        eventUid: String,
        participant: com.github.se.studentconnect.model.event.EventParticipant
    ) = Unit

    override suspend fun addInvitationToEvent(
        eventUid: String,
        invitedUser: String,
        currentUserId: String
    ) = Unit

    override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) = Unit
  }
}
