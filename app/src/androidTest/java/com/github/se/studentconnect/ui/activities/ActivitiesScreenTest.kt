package com.github.se.studentconnect.ui.activities

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTouchInput
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreen
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.activities.ActivitiesViewModel
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ActivitiesScreenTest {
  /*@get:Rule val composeTestRule = createComposeRule()

  private fun createTestEvent(
      uid: String = "event1",
      title: String = "Test Event",
      subtitle: String = "Test Subtitle",
      description: String = "Test Description",
      start: Timestamp = Timestamp.now()
  ): Event.Public {
    return Event.Public(
        uid = uid,
        title = title,
        subtitle = subtitle,
        description = description,
        start = start,
        end = Timestamp.now(),
        location = Location(latitude = 0.0, longitude = 0.0, name = "Test Location"),
        website = "https://test.com",
        ownerId = "owner1",
        isFlash = false)
  }

  private fun createMockUserRepository(testEvents: List<Event>): UserRepository {
    return object : UserRepository {
      override suspend fun getJoinedEvents(userId: String): List<String> {
        return testEvents.map { it.uid }
      }

      override suspend fun leaveEvent(eventId: String, userId: String) {
        // Mock implementation
      }

      override suspend fun getUserById(userId: String): User? = null

      override suspend fun getUserByEmail(email: String): User? = null

      override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
          emptyList<User>() to false

      override suspend fun getAllUsers(): List<User> = emptyList()

      override suspend fun saveUser(user: User) {}

      override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

      override suspend fun deleteUser(userId: String) {}

      override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

      override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

      override suspend fun joinEvent(eventId: String, userId: String) {}

      override suspend fun getNewUid(): String = "test-uid"

      override suspend fun addEventToUser(eventId: String, userId: String) {}

      override suspend fun addInvitationToUser(
          eventId: String,
          userId: String,
          fromUserId: String
      ) {}

      override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

      override suspend fun acceptInvitation(eventId: String, userId: String) {}

      override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {}
    }
  }

  private fun setContent(testEvents: List<Event> = emptyList()) {
    val eventRepository = EventRepositoryLocal()
    val userRepository = createMockUserRepository(testEvents)

    runBlocking { testEvents.forEach { eventRepository.addEvent(it) } }

    val viewModel =
        ActivitiesViewModel(eventRepository = eventRepository, userRepository = userRepository)
    composeTestRule.setContent { ActivitiesScreen(activitiesViewModel = viewModel) }
  }

  @Test
  fun testMainScreenTagIsDisplayed() {
    setContent()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN).assertIsDisplayed()
  }

  @Test
  fun testTopAppBarIsDisplayed() {
    setContent()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.TOP_APP_BAR).assertIsDisplayed()
  }

  @Test
  fun testTopAppBarShowsCorrectTitle() {
    setContent()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
  }

  @Test
  fun testActivitiesTabRowIsDisplayed() {
    setContent()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_TAB_ROW).assertIsDisplayed()
  }

  @Test
  fun testJoinedEventsTabButtonIsDisplayed() {
    setContent()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.TAB_BUTTON_JOINED).assertIsDisplayed()
  }

  @Test
  fun testInvitationsTabButtonIsDisplayed() {
    setContent()
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.TAB_BUTTON_INVITATIONS)
        .assertIsDisplayed()
  }

  @Test
  fun testJoinedEventsTabButtonShowsCorrectText() {
    setContent()
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.TAB_BUTTON_JOINED)
        .assertTextContains("Joined Events")
  }

  @Test
  fun testInvitationsTabButtonShowsCorrectText() {
    setContent()
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.TAB_BUTTON_INVITATIONS)
        .assertTextContains("Invitations")
  }

  @Test
  fun testTabButtonsAreClickable() {
    setContent()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.TAB_BUTTON_JOINED).assertHasClickAction()
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.TAB_BUTTON_INVITATIONS)
        .assertHasClickAction()
  }

  @Test
  fun testEmptyStateIsDisplayedWhenNoEvents() {
    setContent(testEvents = emptyList())
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.EMPTY_STATE_TEXT).assertIsDisplayed()
  }

  @Test
  fun testEmptyStateShowsCorrectMessage() {
    setContent(testEvents = emptyList())
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.EMPTY_STATE_TEXT)
        .assertTextEquals("You have not joined any events yet")
  }

  @Test
  fun testCarouselIsNotDisplayedWhenNoEvents() {
    setContent(testEvents = emptyList())
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertDoesNotExist()
  }

  @Test
  fun testChatButtonIsNotDisplayedWhenNoEvents() {
    setContent(testEvents = emptyList())
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.CHAT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun testInfoEventSectionIsNotDisplayedWhenNoEvents() {
    setContent(testEvents = emptyList())
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.INFO_EVENT_SECTION).assertDoesNotExist()
  }

  @Test
  fun testEventActionButtonsAreNotDisplayedWhenNoEvents() {
    setContent(testEvents = emptyList())
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.EVENT_ACTION_BUTTONS)
        .assertDoesNotExist()
  }

  @Test
  fun testCarouselIsDisplayedWithEvents() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertIsDisplayed()
  }

  @Test
  fun testCarouselCardIsDisplayedForEvent() {
    val event = createTestEvent(uid = "event123")
    setContent(testEvents = listOf(event))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.carouselCardTag("event123"))
        .assertIsDisplayed()
  }

  @Test
  fun testCarouselDisplaysEventTitle() {
    val event = createTestEvent(title = "Music Festival")
    setContent(testEvents = listOf(event))
    composeTestRule.onNodeWithText("Music Festival").assertIsDisplayed()
  }

  @Test
  fun testCarouselDisplaysEventSubtitle() {
    val event = createTestEvent(subtitle = "Amazing Concert")
    setContent(testEvents = listOf(event))
    composeTestRule.onNodeWithText("Amazing Concert").assertIsDisplayed()
  }

  @Test
  fun testMultipleEventsAreDisplayedInCarousel() {
    val event1 = createTestEvent(uid = "event1", title = "Event One")
    val event2 = createTestEvent(uid = "event2", title = "Event Two")
    setContent(testEvents = listOf(event1, event2))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.carouselCardTag("event1"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).performGesture {
      swipeLeft()
    }
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.carouselCardTag("event2"))
        .assertIsDisplayed()
  }

  @Test
  fun testChatButtonIsDisplayedWithEvents() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.CHAT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testChatButtonShowsCorrectText() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.CHAT_BUTTON)
        .assertTextContains("Event chat")
  }

  @Test
  fun testChatButtonShowsSubtext() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.CHAT_BUTTON)
        .assertTextContains("Get The Latest News About The Event")
  }

  @Test
  fun testChatButtonIsClickable() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.CHAT_BUTTON).assertHasClickAction()
  }

  @Test
  fun testInfoEventSectionIsDisplayedWithEvents() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.INFO_EVENT_SECTION))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.INFO_EVENT_SECTION).assertIsDisplayed()
  }

  @Test
  fun testDescriptionTitleIsDisplayed() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasText("Description"))
    composeTestRule.onNodeWithText("Description").assertIsDisplayed()
  }

  @Test
  fun testEventDescriptionTextIsDisplayed() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.EVENT_DESCRIPTION_TEXT))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.EVENT_DESCRIPTION_TEXT)
        .assertIsDisplayed()
  }

  @Test
  fun testEventDescriptionShowsCorrectText() {
    val description = "Join us for an amazing event!"
    setContent(testEvents = listOf(createTestEvent(description = description)))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.EVENT_DESCRIPTION_TEXT))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.EVENT_DESCRIPTION_TEXT)
        .assertTextEquals(description)
  }

  @Test
  fun testCountdownIsDisplayedForFutureEvents() {
    val futureDate = Timestamp(Date(System.currentTimeMillis() + 90000000))
    setContent(testEvents = listOf(createTestEvent(start = futureDate)))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.COUNTDOWN_TEXT_DAYS))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.COUNTDOWN_TEXT_DAYS).assertIsDisplayed()
  }

  @Test
  fun testCountdownShowsDaysLeft() {
    val futureDate = Timestamp(Date(System.currentTimeMillis() + 172800000))
    setContent(testEvents = listOf(createTestEvent(start = futureDate)))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.COUNTDOWN_TEXT_DAYS))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.COUNTDOWN_TEXT_DAYS)
        .assertTextContains("days left", substring = true)
  }

  @Test
  fun testEventActionButtonsAreDisplayedWithEvents() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.EVENT_ACTION_BUTTONS))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.EVENT_ACTION_BUTTONS).assertIsDisplayed()
  }

  @Test
  fun testVisitWebsiteButtonIsDisplayed() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.VISIT_WEBSITE_BUTTON))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.VISIT_WEBSITE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testVisitWebsiteButtonShowsCorrectText() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.VISIT_WEBSITE_BUTTON))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.VISIT_WEBSITE_BUTTON)
        .assertTextContains("Visit Website")
  }

  @Test
  fun testVisitWebsiteButtonIsClickable() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.VISIT_WEBSITE_BUTTON))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.VISIT_WEBSITE_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun testShareEventButtonIsDisplayed() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.SHARE_EVENT_BUTTON))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.SHARE_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testShareEventButtonShowsCorrectText() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.SHARE_EVENT_BUTTON))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.SHARE_EVENT_BUTTON)
        .assertTextContains("Share Event")
  }

  @Test
  fun testShareEventButtonIsClickable() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.SHARE_EVENT_BUTTON))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.SHARE_EVENT_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun testLeaveEventButtonIsDisplayed() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.LEAVE_EVENT_BUTTON))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.LEAVE_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testLeaveEventButtonShowsCorrectText() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.LEAVE_EVENT_BUTTON))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.LEAVE_EVENT_BUTTON)
        .assertTextContains("Leave Event")
  }

  @Test
  fun testLeaveEventButtonIsClickable() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.LEAVE_EVENT_BUTTON))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.LEAVE_EVENT_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun testAllActionButtonsAreDisplayedTogether() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.EVENT_ACTION_BUTTONS))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.VISIT_WEBSITE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.SHARE_EVENT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.LEAVE_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testCanScrollThroughMultipleEvents() {
    val events = (1..5).map { createTestEvent(uid = "event$it", title = "Event #$it") }
    setContent(testEvents = events)

    composeTestRule.onNodeWithText("Event #1").assertIsDisplayed()

    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).performTouchInput {
      val width = this.width.toFloat()
      swipeLeft(startX = width * 0.8f, endX = width * 0.2f, durationMillis = 300)
    }

    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Event #2").assertIsDisplayed()
  }

  @Test
  fun testScreenIsScrollable() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.LEAVE_EVENT_BUTTON))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.LEAVE_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testCompleteScreenLayoutWithSingleEvent() {
    setContent(testEvents = listOf(createTestEvent()))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.TOP_APP_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_TAB_ROW).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.CHAT_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasTestTag(ActivitiesScreenTestTags.EVENT_ACTION_BUTTONS))
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.INFO_EVENT_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.EVENT_ACTION_BUTTONS).assertIsDisplayed()
  }

  @Test
  fun testEventContentIsCorrectlyDisplayed() {
    val event =
        createTestEvent(title = "Summer Party", description = "Come join us for a beach party!")
    setContent(testEvents = listOf(event))
    composeTestRule.onNodeWithText("Summer Party").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN)
        .performScrollToNode(hasText("Come join us for a beach party!"))
    composeTestRule.onNodeWithText("Come join us for a beach party!").assertIsDisplayed()
  }

  @Test
  fun testEmptyStateColumnExists() {
    setContent(testEvents = emptyList())
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.EMPTY_STATE_COLUMN).assertIsDisplayed()
  }*/
}
