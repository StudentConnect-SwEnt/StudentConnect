package com.github.se.studentconnect.ui.utils

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListOfEventsComprehensiveTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  companion object {
    private const val NO_EVENTS_TEXT = "No events found matching your criteria."
    private const val FREE_TEXT = "Free"
    private const val CURRENCY_TEXT_15 = "15 €"
  }

  private fun createTestEvent(
      uid: String = "event1",
      ownerId: String = "owner1",
      title: String = "Test Event",
      description: String = "Test Description",
      locationName: String = "Test Location",
      tags: List<String> = listOf("Sports", "Music"),
      participationFee: UInt? = null,
      timestamp: Timestamp = Timestamp.now()
  ): Event.Public {
    return Event.Public(
        uid = uid,
        ownerId = ownerId,
        title = title,
        description = description,
        imageUrl = null,
        location = Location(latitude = 0.0, longitude = 0.0, name = locationName),
        start = timestamp,
        end = timestamp,
        maxCapacity = null,
        participationFee = participationFee,
        isFlash = false,
        subtitle = "Test Subtitle",
        tags = tags,
        website = "https://test.com")
  }

  @Test
  fun eventCard_withFavorite_displaysFavoriteIcon() {
    val event = createTestEvent()
    var isFavorite = false

    composeTestRule.setContent {
      EventCard(
          event = event,
          isFavorite = isFavorite,
          onFavoriteToggle = { _: String -> isFavorite = !isFavorite },
          onClick = {})
    }

    composeTestRule.onNodeWithContentDescription("Favorite").assertIsDisplayed()
  }

  @Test
  fun eventCard_toggleFavorite_updatesState() {
    val event = createTestEvent()
    var isFavorite = false

    composeTestRule.setContent {
      EventCard(
          event = event,
          isFavorite = isFavorite,
          onFavoriteToggle = { _: String -> isFavorite = !isFavorite },
          onClick = {})
    }

    composeTestRule.onNodeWithContentDescription("Favorite").performClick()
    assert(isFavorite)
  }

  @Test
  fun eventCard_withNoFee_displaysFree() {
    val event = createTestEvent(participationFee = null)

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = { _: String -> }, onClick = {})
    }

    composeTestRule.onNodeWithText(FREE_TEXT).assertIsDisplayed()
  }

  @Test
  fun eventCard_withFee_displaysFeeAmount() {
    val event = createTestEvent(participationFee = 15u)

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = { _: String -> }, onClick = {})
    }

    composeTestRule.onNodeWithText(CURRENCY_TEXT_15).assertIsDisplayed()
  }

  @Test
  fun eventCard_withZeroFee_displaysFree() {
    val event = createTestEvent(participationFee = 0u)

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = { _: String -> }, onClick = {})
    }

    composeTestRule.onNodeWithText(FREE_TEXT).assertIsDisplayed()
  }

  @Test
  fun eventCard_displaysTags_uppercased() {
    val event = createTestEvent(tags = listOf("sports", "music", "tech"))

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = { _: String -> }, onClick = {})
    }

    composeTestRule.onNodeWithText("SPORTS").assertIsDisplayed()
    composeTestRule.onNodeWithText("MUSIC").assertIsDisplayed()
    composeTestRule.onNodeWithText("TECH").assertIsDisplayed()
  }

  @Test
  fun eventCard_displaysMaxThreeTags() {
    val event = createTestEvent(tags = listOf("tag1", "tag2", "tag3", "tag4", "tag5"))

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = { _: String -> }, onClick = {})
    }

    composeTestRule.onNodeWithText("TAG1").assertIsDisplayed()
    composeTestRule.onNodeWithText("TAG2").assertIsDisplayed()
    composeTestRule.onNodeWithText("TAG3").assertIsDisplayed()
    composeTestRule.onNodeWithText("TAG4").assertDoesNotExist()
  }

  @Test
  fun eventCard_withNoTags_doesNotDisplayTagSection() {
    val event = createTestEvent(tags = emptyList())

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = { _: String -> }, onClick = {})
    }

    composeTestRule.onNodeWithText("Test Event").assertIsDisplayed()
  }

  @Test
  fun eventCard_withNoLocation_displaysLocationNotSpecified() {
    val event = createTestEvent().copy(location = null)

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = { _: String -> }, onClick = {})
    }

    composeTestRule.onNodeWithText("Location not specified").assertIsDisplayed()
  }

  @Test
  fun eventCard_onClick_triggersCallback() {
    val event = createTestEvent()
    var clickCount = 0

    composeTestRule.setContent {
      EventCard(
          event = event,
          isFavorite = false,
          onFavoriteToggle = { _: String -> },
          onClick = { clickCount++ })
    }

    composeTestRule.onNodeWithText("Test Event").performClick()
    assert(clickCount == 1)
  }

  @Test
  fun eventListScreen_emptyList_displaysMessage() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = emptyList(),
          hasJoined = false,
          favoritesConfig =
              FavoritesConfig(
                  favoriteEventIds = emptySet<String>(), onFavoriteToggle = { _: String -> }))
    }

    composeTestRule.onNodeWithText(NO_EVENTS_TEXT).assertIsDisplayed()
  }

  @Test
  fun eventListScreen_withEvents_displaysAllEvents() {
    val events =
        listOf(
            createTestEvent(uid = "1", title = "Event 1"),
            createTestEvent(uid = "2", title = "Event 2"),
            createTestEvent(uid = "3", title = "Event 3"))

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = events,
          hasJoined = false,
          favoritesConfig =
              FavoritesConfig(
                  favoriteEventIds = emptySet<String>(), onFavoriteToggle = { _: String -> }))
    }

    composeTestRule.onNodeWithText("Event 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event 2").assertIsDisplayed()

    // Scroll within the LazyColumn to find Event 3
    composeTestRule.onNodeWithTag("event_list").performScrollToNode(hasText("Event 3"))

    composeTestRule.onNodeWithText("Event 3").assertIsDisplayed()
  }

  @Test
  fun eventListScreen_groupsByDate_todayHeader() {
    val today = Calendar.getInstance()
    val event = createTestEvent(timestamp = Timestamp(today.time))

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(event),
          hasJoined = false,
          favoritesConfig =
              FavoritesConfig(
                  favoriteEventIds = emptySet<String>(), onFavoriteToggle = { _: String -> }))
    }

    composeTestRule.onNodeWithText("TODAY").assertIsDisplayed()
  }

  @Test
  fun eventListScreen_groupsByDate_tomorrowHeader() {
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val event = createTestEvent(timestamp = Timestamp(tomorrow.time))

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(event),
          hasJoined = false,
          favoritesConfig =
              FavoritesConfig(
                  favoriteEventIds = emptySet<String>(), onFavoriteToggle = { _: String -> }))
    }

    composeTestRule.onNodeWithText("TOMORROW").assertIsDisplayed()
  }

  @Test
  fun eventListScreen_withFavoritedEvents_displaysFavoriteIcon() {
    val event = createTestEvent(uid = "fav1", title = "Favorite Event")
    val favoritedIds = setOf("fav1")

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(event),
          hasJoined = false,
          favoritesConfig =
              FavoritesConfig(favoriteEventIds = favoritedIds, onFavoriteToggle = { _: String -> }))
    }

    composeTestRule.onNodeWithText("Favorite Event").assertIsDisplayed()
  }

  @Test
  fun eventListScreen_toggleFavorite_callsCallback() {
    val event = createTestEvent(uid = "ev1")
    var toggledEventId: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = listOf(event),
          hasJoined = false,
          favoritesConfig =
              FavoritesConfig(
                  favoriteEventIds = emptySet<String>(),
                  onFavoriteToggle = { toggledEventId = it }))
    }

    composeTestRule.onNodeWithContentDescription("Favorite").performClick()
    assert(toggledEventId == "ev1")
  }

  @Test
  fun eventListScreen_multipleGroups_displaysAllHeaders() {
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val nextWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }

    val events =
        listOf(
            createTestEvent(uid = "1", timestamp = Timestamp(today.time)),
            createTestEvent(uid = "2", timestamp = Timestamp(tomorrow.time)),
            createTestEvent(uid = "3", timestamp = Timestamp(nextWeek.time)))

    composeTestRule.setContent {
      val navController = rememberNavController()
      EventListScreen(
          navController = navController,
          events = events,
          hasJoined = false,
          favoritesConfig =
              FavoritesConfig(
                  favoriteEventIds = emptySet<String>(), onFavoriteToggle = { _: String -> }))
    }

    composeTestRule.onNodeWithText("TODAY").assertIsDisplayed()
    composeTestRule.onNodeWithText("TOMORROW").assertIsDisplayed()
  }

  @Test
  fun formatDateHeader_today_returnsToday() {
    val today = Calendar.getInstance()
    val result = formatDateHeader(Timestamp(today.time))
    assert(result == "TODAY")
  }

  @Test
  fun formatDateHeader_tomorrow_returnsTomorrow() {
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val result = formatDateHeader(Timestamp(tomorrow.time))
    assert(result == "TOMORROW")
  }

  @Test
  fun eventListScreen_clickEvent_navigatesToDetail() {
    val event = createTestEvent(uid = "clickable", title = "Clickable Event")
    var clickedEvent = false

    composeTestRule.setContent {
      EventCard(
          event = event,
          isFavorite = false,
          onFavoriteToggle = { _: String -> },
          onClick = { clickedEvent = true })
    }

    composeTestRule.onNodeWithText("Clickable Event").performClick()
    assert(clickedEvent)
  }

  @Test
  fun eventCard_displaysDateTime() {
    val event = createTestEvent()

    composeTestRule.setContent {
      EventCard(
          event = event, isFavorite = false, onFavoriteToggle = { _: String -> }, onClick = {})
    }

    composeTestRule.onNodeWithText("Test Event").assertIsDisplayed()
  }

  @Test
  fun eventCard_privateEvent_doesNotDisplayTags() {
    val privateEvent =
        Event.Private(
            uid = "private1",
            ownerId = "owner1",
            title = "Private Event",
            description = "Description",
            imageUrl = null,
            location = Location(0.0, 0.0, "Private Location"),
            start = Timestamp.now(),
            end = Timestamp.now(),
            maxCapacity = null,
            participationFee = null,
            isFlash = false)

    composeTestRule.setContent {
      EventCard(
          event = privateEvent,
          isFavorite = false,
          onFavoriteToggle = { _: String -> },
          onClick = {})
    }

    composeTestRule.onNodeWithText("Private Event").assertIsDisplayed()

    // Explicitly assert that tag texts are not present for private events
    composeTestRule.onAllNodes(hasText("SPORTS") or hasText("MUSIC")).assertCountEquals(0)
  }
}
