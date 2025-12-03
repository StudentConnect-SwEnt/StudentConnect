package com.github.se.studentconnect.ui.camera

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.components.EventSelectionState
import com.github.se.studentconnect.ui.screen.camera.EventSelectionConfig
import com.github.se.studentconnect.ui.screen.camera.MediaPreviewScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MediaPreviewScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var testImageUri: Uri
  private lateinit var testFile: File
  private val context: Context = ApplicationProvider.getApplicationContext()

  companion object {
    private const val TEST_IMAGE_FILENAME = "test_image.jpg"
  }

  @Before
  fun setup() {
    testFile = File(context.cacheDir, TEST_IMAGE_FILENAME)
    testFile.createNewFile()
    testImageUri = Uri.fromFile(testFile)
  }

  @After
  fun cleanup() {
    testFile.delete()
  }

  @Test
  fun mediaPreviewScreen_displaysPhotoPreview() {
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = testImageUri, isVideo = false, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("media_preview_actions").assertIsDisplayed()
  }

  @Test
  fun mediaPreviewScreen_displaysVideoPreview() {
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = testImageUri, isVideo = true, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
  }

  @Test
  fun mediaPreviewScreen_retakeButton_invokesCallback() {
    var retakeClicked = false

    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(
            mediaUri = testImageUri,
            isVideo = false,
            onAccept = {},
            onRetake = { retakeClicked = true })
      }
    }

    composeTestRule.onNodeWithTag("media_preview_retake").performClick()
    composeTestRule.runOnIdle { assert(retakeClicked) }
  }

  @Test
  fun mediaPreviewScreen_acceptButton_invokesCallback() {
    var acceptClicked = false
    val event =
        Event.Public(
            uid = "1",
            ownerId = "owner1",
            title = "Test Event",
            description = "Description",
            start = Timestamp.now(),
            isFlash = false,
            subtitle = "Subtitle")

    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(
            mediaUri = testImageUri,
            isVideo = false,
            onAccept = { acceptClicked = true },
            onRetake = {},
            eventSelectionConfig =
                EventSelectionConfig(state = EventSelectionState.Success(listOf(event))),
            initialSelectedEvent = event)
      }
    }

    composeTestRule.onNodeWithTag("media_preview_accept").performClick()
    composeTestRule.runOnIdle { assert(acceptClicked) }
  }

  @Test
  fun mediaPreviewScreen_displaysRetakeButton() {
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = testImageUri, isVideo = false, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_retake").assertIsDisplayed()
  }

  @Test
  fun mediaPreviewScreen_displaysAcceptButton() {
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = testImageUri, isVideo = false, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_accept").assertIsDisplayed()
  }

  @Test
  fun photoPreview_handlesInvalidUri() {
    val invalidUri = Uri.parse("file:///nonexistent.jpg")
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = invalidUri, isVideo = false, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("media_preview_actions").assertIsDisplayed()
  }

  @Test
  fun videoPreview_handlesInvalidUri() {
    val invalidUri = Uri.parse("file:///nonexistent.mp4")
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = invalidUri, isVideo = true, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("video_preview").assertIsDisplayed()
  }

  @Test
  fun photoPreview_displaysWithValidBitmap() {
    val bitmap =
        android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
    val tempFile = File(context.cacheDir, "valid_photo.jpg")
    tempFile.outputStream().use { out ->
      bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
    }
    val validUri = Uri.fromFile(tempFile)

    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = validUri, isVideo = false, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
    tempFile.delete()
  }

  @Test
  fun videoPreview_rendersVideoTag() {
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = testImageUri, isVideo = true, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("video_preview").assertIsDisplayed()
  }

  @Test
  fun mediaPreviewScreen_acceptButton_passesSelectedEvent() {
    val event =
        Event.Public(
            uid = "1",
            ownerId = "owner1",
            title = "Test Event",
            description = "Description",
            start = Timestamp.now(),
            isFlash = false,
            subtitle = "Subtitle")
    var acceptedEvent: Event? = null
    var acceptCalled = false

    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(
            mediaUri = testImageUri,
            isVideo = false,
            onAccept = { selectedEvent ->
              acceptedEvent = selectedEvent
              acceptCalled = true
            },
            onRetake = {},
            eventSelectionConfig =
                EventSelectionConfig(state = EventSelectionState.Success(listOf(event))),
            initialSelectedEvent = event)
      }
    }

    composeTestRule.onNodeWithTag("media_preview_accept").performClick()

    composeTestRule.runOnIdle {
      assert(acceptCalled)
      assertNotNull(acceptedEvent)
      assertEquals(event.uid, acceptedEvent!!.uid)
    }
  }
}
