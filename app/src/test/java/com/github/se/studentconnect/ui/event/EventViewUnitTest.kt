package com.github.se.studentconnect.ui.event

import android.content.Context
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.activities.EventView
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import java.io.File
import java.util.Base64
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class EventViewUnitTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val context = ApplicationProvider.getApplicationContext<Context>()

  @Before
  fun setUp() {
    // Init Firebase avec le contexte de test avant d'accéder à MediaRepositoryProvider
    val ctx = ApplicationProvider.getApplicationContext<Context>()
    if (com.google.firebase.FirebaseApp.getApps(ctx).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(ctx)
    }

    // default repository to avoid real Firebase usage
    MediaRepositoryProvider.repository =
        object : MediaRepository {
          override suspend fun upload(uri: Uri, path: String?): String = "unused"

          override suspend fun download(id: String): Uri = Uri.EMPTY

          override suspend fun delete(id: String) = Unit
        }
  }

  @Test
  fun eventView_shows_icon_when_image_download_fails() {
    // repo that errors on download
    MediaRepositoryProvider.repository =
        object : MediaRepository {
          override suspend fun upload(uri: Uri, path: String?): String = "u"

          override suspend fun download(id: String): Uri = throw RuntimeException("fail")

          override suspend fun delete(id: String) = Unit
        }

    val event =
        Event.Public(
            uid = "ev1",
            ownerId = "owner",
            title = "EV",
            description = "desc",
            imageUrl = "imgid",
            location = null,
            start = Timestamp.now(),
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = false,
            subtitle = "s",
            tags = emptyList(),
            website = null)

    val eventRepo = com.github.se.studentconnect.model.event.EventRepositoryLocal()
    kotlinx.coroutines.runBlocking { eventRepo.addEvent(event) }
    val localUserRepo = UserRepositoryLocal()

    val vm = EventViewModel(eventRepository = eventRepo, userRepository = localUserRepo)

    composeTestRule.setContent {
      MaterialTheme {
        val nav = rememberNavController()
        EventView(eventUid = event.uid, navController = nav, eventViewModel = vm, hasJoined = false)
      }
    }

    composeTestRule.waitForIdle()

    val desc = context.getString(R.string.content_description_event_image)
    composeTestRule.onNodeWithContentDescription(desc).assertExists()
  }

  @Test
  fun eventView_shows_bitmap_when_download_succeeds() {
    val pngBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
    val bytes = Base64.getDecoder().decode(pngBase64)
    val tmpFile = File(context.cacheDir, "event_test_img.png")
    tmpFile.outputStream().use { it.write(bytes) }

    MediaRepositoryProvider.repository =
        object : MediaRepository {
          override suspend fun upload(uri: Uri, path: String?): String = "u"

          override suspend fun download(id: String): Uri = Uri.fromFile(tmpFile)

          override suspend fun delete(id: String) = Unit
        }

    val event =
        Event.Public(
            uid = "ev2",
            ownerId = "owner",
            title = "EV2",
            description = "desc",
            imageUrl = "fileid",
            location = null,
            start = Timestamp.now(),
            end = null,
            maxCapacity = null,
            participationFee = 0u,
            isFlash = false,
            subtitle = "s",
            tags = emptyList(),
            website = null)

    val eventRepo = EventRepositoryLocal()
    kotlinx.coroutines.runBlocking { eventRepo.addEvent(event) }
    val localUserRepo = UserRepositoryLocal()

    val vm = EventViewModel(eventRepository = eventRepo, userRepository = localUserRepo)

    composeTestRule.setContent {
      MaterialTheme {
        val nav = rememberNavController()
        EventView(eventUid = event.uid, navController = nav, eventViewModel = vm, hasJoined = false)
      }
    }

    composeTestRule.waitForIdle()

    val desc = context.getString(R.string.content_description_event_image)
    composeTestRule.onNodeWithContentDescription(desc).assertExists()
  }
}
