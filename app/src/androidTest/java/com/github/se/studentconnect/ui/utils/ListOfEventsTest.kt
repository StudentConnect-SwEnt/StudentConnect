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
}
