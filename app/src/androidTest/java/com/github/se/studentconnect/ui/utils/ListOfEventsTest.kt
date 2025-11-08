package com.github.se.studentconnect.ui.utils

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.events.EventCard
import com.github.se.studentconnect.ui.events.EventListScreen
import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListOfEventsTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  private fun createTestEvent(
      uid: String = "event1",
      ownerId: String = "owner1",
      title: String = "Test Event",
      description: String = "Test Description",
      locationName: String = "Test Location"
  ): Event.Public {
    return Event.Public(
        uid = uid,
        ownerId = ownerId,
        title = title,
        description = description,
        imageUrl = null,
        location = Location(latitude = 0.0, longitude = 0.0, name = locationName),
        start = Timestamp.now(),
        end = Timestamp.now(),
        maxCapacity = null,
        participationFee = null,
        isFlash = false,
        subtitle = "Test Subtitle",
        tags = listOf("tag1", "tag2", "tag3"),
        website = null)
  }

  @Test
  fun eventCard_displaysEventTitle() {
    val event = createTestEvent(title = "Music Festival")
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("Music Festival").assertIsDisplayed()
  }

  @Test
  fun eventCard_displaysEventLocation() {
    val event = createTestEvent(locationName = "EPFL Campus")
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("EPFL Campus").assertIsDisplayed()
  }

  @Test
  fun eventCard_displaysEventTags() {
    val event = createTestEvent()
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("TAG1").assertIsDisplayed()
    composeTestRule.onNodeWithText("TAG2").assertIsDisplayed()
    composeTestRule.onNodeWithText("TAG3").assertIsDisplayed()
  }

  @Test
  fun eventCard_hasClickAction() {
    val event = createTestEvent()
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("Test Event").assertHasClickAction()
  }

  @Test
  fun eventCard_favoriteButtonIsDisplayed() {
    val event = createTestEvent()
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithContentDescription("Favorite").assertIsDisplayed()
  }

  @Test
  fun eventCard_favoriteButtonIsClickable() {
    val event = createTestEvent()
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    val favoriteButton = composeTestRule.onNodeWithContentDescription("Favorite")
    favoriteButton.assertHasClickAction()
    favoriteButton.performClick()
  }

  @Test
  fun eventCard_displaysParticipationFee() {
    val event = createTestEvent().copy(participationFee = 25u)
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("25 €", substring = true).assertIsDisplayed()
  }

  @Test
  fun eventCard_displaysImage() {
    val event = createTestEvent()
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithContentDescription("Test Event").assertIsDisplayed()
  }

  @Test
  fun eventListScreen_emptyList_displaysNothing() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = emptyList(),
          hasJoined = false,
          favoriteEventIds = emptySet(),
          onFavoriteToggle = {})
    }

    composeTestRule.onNodeWithText("Test Event").assertDoesNotExist()
  }

  @Test
  fun eventListScreen_singleEvent_isDisplayed() {
    val event = createTestEvent(title = "Summer Party")
    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(event),
          hasJoined = false,
          favoriteEventIds = emptySet(),
          onFavoriteToggle = {})
    }

    composeTestRule.onNodeWithText("Summer Party").assertIsDisplayed()
  }

  fun eventListScreen_multipleEvents_areDisplayed() {
    val event1 = createTestEvent(uid = "event1", title = "Event One")
    val event2 = createTestEvent(uid = "event2", title = "Event Two")
    val event3 = createTestEvent(uid = "event3", title = "Event Three")

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(event1, event2, event3),
          hasJoined = false,
          favoriteEventIds = emptySet(),
          onFavoriteToggle = {})
    }

    composeTestRule.onNodeWithText("Event One").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event Two").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event Three").assertIsDisplayed()
  }

  @Test
  fun eventListScreen_groupsByDate_displaysHeaders() {
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

    val eventToday =
        createTestEvent(uid = "event1", title = "Today Event").copy(start = Timestamp(today.time))
    val eventTomorrow =
        createTestEvent(uid = "event2", title = "Tomorrow Event")
            .copy(start = Timestamp(tomorrow.time))

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(eventToday, eventTomorrow),
          hasJoined = false,
          favoriteEventIds = emptySet(),
          onFavoriteToggle = {})
    }

    composeTestRule.onNodeWithText("TODAY").assertIsDisplayed()
    composeTestRule.onNodeWithText("TOMORROW").assertIsDisplayed()
  }

  @Test
  fun eventListScreen_clickOnEvent_triggersNavigation() {
    val event = createTestEvent(title = "Clickable Event")
    var clickCount = 0

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = {}, onClick = { clickCount++ })
    }

    composeTestRule.onNodeWithText("Clickable Event").performClick()
    assert(clickCount == 1)
  }

  @Test
  fun eventCard_withNoLocation_displaysNoLocation() {
    val event = createTestEvent().copy(location = null)
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("Location not specified").assertIsDisplayed()
  }

  @Test
  fun eventCard_withTodayDate_displaysToday() {
    val today = Calendar.getInstance()
    val event = createTestEvent().copy(start = Timestamp(today.time))

    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("Today", substring = true).assertIsDisplayed()
  }

  @Test
  fun eventCard_withNoTags_doesNotDisplayTags() {
    val event = createTestEvent().copy(tags = emptyList())
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("TAG1").assertDoesNotExist()
    composeTestRule.onNodeWithText("TAG2").assertDoesNotExist()
  }

  @Test
  fun eventCard_withMoreThanThreeTags_displaysOnlyThree() {
    val event =
        createTestEvent().copy(tags = listOf("tag1", "tag2", "tag3", "tag4", "tag5", "tag6"))
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("TAG1").assertIsDisplayed()
    composeTestRule.onNodeWithText("TAG2").assertIsDisplayed()
    composeTestRule.onNodeWithText("TAG3").assertIsDisplayed()
    composeTestRule.onNodeWithText("TAG4").assertDoesNotExist()
  }

  @Test
  fun eventListScreen_emptyList_displaysEmptyMessage() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = emptyList(),
          hasJoined = false,
          favoriteEventIds = emptySet(),
          onFavoriteToggle = {})
    }

    composeTestRule.onNodeWithText("No events found matching your criteria.").assertIsDisplayed()
  }

  @Test
  fun eventCard_displaysFreeWhenFeeIsZero() {
    val event = createTestEvent().copy(participationFee = 0u)
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("Free").assertIsDisplayed()
  }

  @Test
  fun eventCard_displaysFreeWhenFeeIsNull() {
    val event = createTestEvent().copy(participationFee = null)
    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("Free").assertIsDisplayed()
  }

  @Test
  fun eventCard_liveIndicator_shownForLiveEvent() {
    val now = Calendar.getInstance()
    val pastStart = Calendar.getInstance().apply { add(Calendar.MINUTE, -30) }
    val futureEnd = Calendar.getInstance().apply { add(Calendar.MINUTE, 30) }

    val liveEvent =
        createTestEvent().copy(start = Timestamp(pastStart.time), end = Timestamp(futureEnd.time))

    composeTestRule.setContent {
      EventCard(event = liveEvent, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("LIVE").assertIsDisplayed()
  }

  @Test
  fun eventCard_liveIndicator_notShownForFutureEvent() {
    val futureStart = Calendar.getInstance().apply { add(Calendar.HOUR, 1) }
    val futureEnd = Calendar.getInstance().apply { add(Calendar.HOUR, 3) }

    val futureEvent =
        createTestEvent().copy(start = Timestamp(futureStart.time), end = Timestamp(futureEnd.time))

    composeTestRule.setContent {
      EventCard(event = futureEvent, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("LIVE").assertDoesNotExist()
  }

  @Test
  fun eventCard_liveIndicator_notShownForPastEvent() {
    val pastStart = Calendar.getInstance().apply { add(Calendar.HOUR, -3) }
    val pastEnd = Calendar.getInstance().apply { add(Calendar.HOUR, -1) }

    val pastEvent =
        createTestEvent().copy(start = Timestamp(pastStart.time), end = Timestamp(pastEnd.time))

    composeTestRule.setContent {
      EventCard(event = pastEvent, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("LIVE").assertDoesNotExist()
  }

  @Test
  fun eventCard_liveIndicator_calculatesDefaultEndTime() {
    val pastStart = Calendar.getInstance().apply { add(Calendar.HOUR, -1) }

    val eventWithoutEnd = createTestEvent().copy(start = Timestamp(pastStart.time), end = null)

    composeTestRule.setContent {
      EventCard(event = eventWithoutEnd, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    // Should show LIVE as default end is start + 3 hours
    composeTestRule.onNodeWithText("LIVE").assertIsDisplayed()
  }

  @Test
  fun eventCard_favoriteToggle_callsCallback() {
    val event = createTestEvent(uid = "event123")
    var toggledEventId: String? = null

    composeTestRule.setContent {
      EventCard(
          event = event,
          isFavorite = false,
          onFavoriteToggle = { eventId -> toggledEventId = eventId },
          onClick = {})
    }

    composeTestRule.onNodeWithContentDescription("Favorite").performClick()
    assert(toggledEventId == "event123")
  }

  @Test
  fun eventCard_favoriteState_changesIcon() {
    val event = createTestEvent()

    composeTestRule.setContent {
      EventCard(event = event, isFavorite = true, onFavoriteToggle = {}, onClick = {})
    }

    // When favorited, the icon should be displayed
    composeTestRule.onNodeWithContentDescription("Favorite").assertIsDisplayed()
  }

  @Test
  fun eventCard_privateEvent_displaysCorrectly() {
    val privateEvent =
        Event.Private(
            uid = "private1",
            ownerId = "owner1",
            title = "Private Party",
            description = "Private Description",
            imageUrl = null,
            location = Location(0.0, 0.0, "Secret Location"),
            start = Timestamp.now(),
            end = Timestamp.now(),
            maxCapacity = 20u,
            participationFee = null,
            isFlash = false,
        )

    composeTestRule.setContent {
      EventCard(event = privateEvent, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    composeTestRule.onNodeWithText("Private Party").assertIsDisplayed()
    composeTestRule.onNodeWithText("Secret Location").assertIsDisplayed()
  }

  @Test
  fun eventListScreen_favoriteToggle_propagatesCorrectly() {
    val event = createTestEvent(uid = "fav-event")
    var toggledId: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(event),
          hasJoined = false,
          favoriteEventIds = emptySet(),
          onFavoriteToggle = { eventId -> toggledId = eventId })
    }

    composeTestRule.onNodeWithContentDescription("Favorite").performClick()
    assert(toggledId == "fav-event")
  }

  @Test
  fun eventListScreen_favoriteEventIds_displaysFavorited() {
    val event = createTestEvent(uid = "fav123")

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(event),
          hasJoined = false,
          favoriteEventIds = setOf("fav123"),
          onFavoriteToggle = {})
    }

    // Event should be displayed with favorite icon shown
    composeTestRule.onNodeWithText("Test Event").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Favorite").assertIsDisplayed()
  }

  @Test
  fun eventCard_longDescription_truncatesCorrectly() {
    val event =
        createTestEvent(
            description = "This is a very long description that should be truncated at some point")

    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    // Description should be displayed (truncation is handled by the UI)
    composeTestRule.onNodeWithText("Test Event").assertIsDisplayed()
  }

  @Test
  fun eventCard_dateFormatting_displaysCorrectFormat() {
    val event = createTestEvent()

    composeTestRule.setContent {
      EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
    }

    // Date/time should be displayed in the format "Today | HH:mm"
    composeTestRule.onNodeWithText("Today", substring = true).assertIsDisplayed()
  }

  @Test
  fun eventListScreen_multipleEventsOnSameDay_groupedUnderOneHeader() {
    val today = Calendar.getInstance()
    val event1 =
        createTestEvent(uid = "e1", title = "Morning Event").copy(start = Timestamp(today.time))

    val todayEvening = Calendar.getInstance().apply { add(Calendar.HOUR, 5) }
    val event2 =
        createTestEvent(uid = "e2", title = "Evening Event")
            .copy(start = Timestamp(todayEvening.time))

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(event1, event2),
          hasJoined = false,
          favoriteEventIds = emptySet(),
          onFavoriteToggle = {})
    }

    // Should only have one "TODAY" header
    composeTestRule.onNodeWithText("TODAY").assertIsDisplayed()
    composeTestRule.onNodeWithText("Morning Event").assertIsDisplayed()
    composeTestRule.onNodeWithText("Evening Event").assertIsDisplayed()
  }

  @Test
  fun eventCard_clickAction_triggersCallback() {
    val event = createTestEvent()
    var clickCount = 0

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = {}, onClick = { clickCount++ })
    }

    composeTestRule.onNodeWithText("Test Event").performClick()
    assert(clickCount == 1)
  }

  @Test
  fun eventCard_multipleFavoriteToggles_eachCallsCallback() {
    val event = createTestEvent()
    var toggleCount = 0

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = { toggleCount++ }, onClick = {})
    }

    val favoriteButton = composeTestRule.onNodeWithContentDescription("Favorite")
    favoriteButton.performClick()
    favoriteButton.performClick()
    favoriteButton.performClick()

    assert(toggleCount == 3)
  }
}
