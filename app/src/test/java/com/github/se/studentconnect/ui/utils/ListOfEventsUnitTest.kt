package com.github.se.studentconnect.ui.utils

import android.content.Context
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import java.io.File
import java.util.Base64
import java.util.Date
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ListOfEventsUnitTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Cette ligne est OK ici, car elle est initialisée
  // au moment de la création de l'instance de test (après Robolectric).
  private val context = ApplicationProvider.getApplicationContext<Context>()

  // 1. SUPPRIMEZ ou Videz le 'companion object'
  companion object {
    // Il n'est plus nécessaire d'avoir @BeforeClass
    @JvmStatic // Gardez JvmStatic si vous avez d'autres méthodes statiques,
    // mais supprimez le contenu de setUpClass
    fun setUpClass() {
      // SUPPRIMEZ LE CONTENU D'ICI
    }
  }

  @Before
  fun setUp() {
    // 2. AJOUTEZ l'initialisation de Firebase ICI
    // Le 'context' est maintenant disponible en toute sécurité.
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    MediaRepositoryProvider.overrideForTests(
        object : MediaRepository {
          override suspend fun upload(uri: Uri, path: String?): String = "unused"

          override suspend fun download(id: String): Uri = Uri.EMPTY

          override suspend fun delete(id: String) = Unit
        })
  }

  @Test
  fun eventCard_shows_default_image_when_download_fails() {
    // repository that throws when downloading to simulate failure
    MediaRepositoryProvider.overrideForTests(
        object : MediaRepository {
          override suspend fun upload(uri: Uri, path: String?): String = "u"

          override suspend fun download(id: String): Uri = throw RuntimeException("fail")

          override suspend fun delete(id: String) = Unit
        })

    val event =
        Event.Public(
            uid = "e1",
            ownerId = "o1",
            title = "Event Title",
            description = "desc",
            imageUrl = "some_id",
            location = null,
            start = Timestamp.now(),
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = false,
            subtitle = "sub",
            tags = emptyList(),
            website = null)

    composeTestRule.setContent {
      MaterialTheme {
        EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // when the image fails to download, the fallback image uses contentDescription = event.title
    composeTestRule.onNodeWithContentDescription(event.title).assertExists()
  }

  @Test
  fun eventCard_shows_downloaded_bitmap_when_available() {
    // create a tiny valid PNG (1x1) from base64 and write to file
    val pngBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
    val bytes = Base64.getDecoder().decode(pngBase64)
    val tmpFile = File(context.cacheDir, "test_img.png")
    tmpFile.outputStream().use { it.write(bytes) }
    assertNotNull(tmpFile)

    // repository that returns the file:// uri
    MediaRepositoryProvider.overrideForTests(
        object : MediaRepository {
          override suspend fun upload(uri: Uri, path: String?): String = "u"

          override suspend fun download(id: String): Uri = Uri.fromFile(tmpFile)

          override suspend fun delete(id: String) = Unit
        })

    val event =
        Event.Public(
            uid = "e2",
            ownerId = "o2",
            title = "Event With Image",
            description = "desc",
            imageUrl = "file_id",
            location = null,
            start = Timestamp.now(),
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = false,
            subtitle = "sub",
            tags = emptyList(),
            website = null)

    composeTestRule.setContent {
      MaterialTheme {
        EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // when bitmap is available, contentDescription is the localized string for event card picture
    val desc = context.getString(R.string.content_description_event_card_picture)
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithContentDescription(desc).fetchSemanticsNodes().size == 1
    }
    composeTestRule.onNodeWithContentDescription(desc).assertExists()
  }

  @Test
  fun eventCard_favorite_button_calls_callback() {
    var toggledId: String? = null

    val event =
        Event.Public(
            uid = "fav1",
            ownerId = "o1",
            title = "Fav Event",
            description = "desc",
            imageUrl = null,
            location = null,
            start = Timestamp.now(),
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = false,
            subtitle = "sub",
            tags = emptyList(),
            website = null)

    composeTestRule.setContent {
      MaterialTheme {
        EventCard(
            event = event,
            isFavorite = false,
            onFavoriteToggle = { id -> toggledId = id },
            onClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // Click the favorite button and ensure the callback is invoked with the event uid
    composeTestRule.onNodeWithContentDescription("Favorite").performClick()
    assert(toggledId == event.uid)
  }

  @Test
  fun eventCard_shows_live_badge_when_event_is_live() {
    // Start in the past so now >= start, no end (default 3 hours later) so now < end
    val start = Timestamp(Date(System.currentTimeMillis() - 60_000))

    val event =
        Event.Public(
            uid = "live1",
            ownerId = "o1",
            title = "Live Event",
            description = "desc",
            imageUrl = null,
            location = null,
            start = start,
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = false,
            subtitle = "sub",
            tags = emptyList(),
            website = null)

    composeTestRule.setContent {
      MaterialTheme {
        EventCard(event = event, isFavorite = false, onFavoriteToggle = {}, onClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // The LIVE badge displays the text "LIVE"
    composeTestRule.onNodeWithText(context.getString(R.string.event_label_live)).assertExists()
  }

  @Test
  fun eventCard_shows_flash_icon_when_flash_event_is_live() {
    // Start in the past so now >= start, no end (default 3 hours later) so now < end
    val start = Timestamp(Date(System.currentTimeMillis() - 60_000))

    val flashEvent =
        Event.Public(
            uid = "flash1",
            ownerId = "o1",
            title = "Flash Event",
            description = "desc",
            imageUrl = null,
            location = null,
            start = start,
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = true,
            subtitle = "sub",
            tags = emptyList(),
            website = null)

    composeTestRule.setContent {
      MaterialTheme {
        EventCard(event = flashEvent, isFavorite = false, onFavoriteToggle = {}, onClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // Flash event should show flash icon, not LIVE text
    val flashIconDesc = context.getString(R.string.content_description_flash_event)
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithContentDescription(flashIconDesc).fetchSemanticsNodes().size ==
          1
    }
    composeTestRule.onNodeWithContentDescription(flashIconDesc).assertExists()
    composeTestRule
        .onNodeWithText(context.getString(R.string.event_label_live))
        .assertDoesNotExist()
  }

  @Test
  fun eventCard_shows_live_badge_when_regular_event_is_live() {
    // Start in the past so now >= start, no end (default 3 hours later) so now < end
    val start = Timestamp(Date(System.currentTimeMillis() - 60_000))

    val regularEvent =
        Event.Public(
            uid = "regular1",
            ownerId = "o1",
            title = "Regular Event",
            description = "desc",
            imageUrl = null,
            location = null,
            start = start,
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = false,
            subtitle = "sub",
            tags = emptyList(),
            website = null)

    composeTestRule.setContent {
      MaterialTheme {
        EventCard(event = regularEvent, isFavorite = false, onFavoriteToggle = {}, onClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // Regular event should show LIVE badge, not flash icon
    composeTestRule.onNodeWithText(context.getString(R.string.event_label_live)).assertExists()
    val flashIconDesc = context.getString(R.string.content_description_flash_event)
    composeTestRule.onNodeWithContentDescription(flashIconDesc).assertDoesNotExist()
  }

  @Test
  fun eventCard_shows_no_badge_when_flash_event_is_not_live() {
    // Future event - not live
    val futureStart = Timestamp(Date(System.currentTimeMillis() + 3600_000))

    val flashEvent =
        Event.Public(
            uid = "flash2",
            ownerId = "o1",
            title = "Future Flash Event",
            description = "desc",
            imageUrl = null,
            location = null,
            start = futureStart,
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = true,
            subtitle = "sub",
            tags = emptyList(),
            website = null)

    composeTestRule.setContent {
      MaterialTheme {
        EventCard(event = flashEvent, isFavorite = false, onFavoriteToggle = {}, onClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // Future flash event should not show any badge
    val flashIconDesc = context.getString(R.string.content_description_flash_event)
    composeTestRule.onNodeWithContentDescription(flashIconDesc).assertDoesNotExist()
    composeTestRule
        .onNodeWithText(context.getString(R.string.event_label_live))
        .assertDoesNotExist()
  }

  @Test
  fun eventCard_shows_no_badge_when_regular_event_is_not_live() {
    // Future event - not live
    val futureStart = Timestamp(Date(System.currentTimeMillis() + 3600_000))

    val regularEvent =
        Event.Public(
            uid = "regular2",
            ownerId = "o1",
            title = "Future Regular Event",
            description = "desc",
            imageUrl = null,
            location = null,
            start = futureStart,
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = false,
            subtitle = "sub",
            tags = emptyList(),
            website = null)

    composeTestRule.setContent {
      MaterialTheme {
        EventCard(event = regularEvent, isFavorite = false, onFavoriteToggle = {}, onClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // Future regular event should not show any badge
    composeTestRule
        .onNodeWithText(context.getString(R.string.event_label_live))
        .assertDoesNotExist()
    val flashIconDesc = context.getString(R.string.content_description_flash_event)
    composeTestRule.onNodeWithContentDescription(flashIconDesc).assertDoesNotExist()
  }
}
