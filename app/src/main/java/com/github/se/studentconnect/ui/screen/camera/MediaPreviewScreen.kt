package com.github.se.studentconnect.ui.screen.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.exifinterface.media.ExifInterface
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.components.EventSelectionDropdown
import com.github.se.studentconnect.ui.components.EventSelectionState
import java.io.IOException

private val ACTION_BUTTON_SIZE = 64.dp
private val ACTION_ICON_SIZE = 32.dp
private val ACTIONS_BOTTOM_PADDING = 32.dp
private val ACTIONS_SPACING = 48.dp
private val EVENT_SELECTOR_TOP_PADDING = 48.dp
private val EVENT_SELECTOR_HORIZONTAL_PADDING = 24.dp

/**
 * Screen that displays a preview of captured photo or video with options to accept or retake.
 *
 * @param mediaUri The URI of the captured media
 * @param isVideo Whether the media is a video (true) or photo (false)
 * @param onAccept Callback when user accepts the media with selected event
 * @param onRetake Callback when user wants to retake
 * @param eventSelectionState State for loading joined events
 * @param onLoadEvents Callback to load user's joined events
 * @param modifier Modifier for the screen
 */
@Composable
fun MediaPreviewScreen(
    mediaUri: Uri,
    isVideo: Boolean,
    onAccept: (Event?) -> Unit,
    onRetake: () -> Unit,
    eventSelectionState: EventSelectionState = EventSelectionState.Success(emptyList()),
    onLoadEvents: () -> Unit = {},
    modifier: Modifier = Modifier,
    initialSelectedEvent: Event? = null
) {
  var selectedEvent by remember { mutableStateOf<Event?>(initialSelectedEvent) }

  Box(modifier = modifier.fillMaxSize().background(Color.Black).testTag("media_preview_screen")) {
    if (isVideo) {
      VideoPreview(videoUri = mediaUri, modifier = Modifier.fillMaxSize())
    } else {
      PhotoPreview(imageUri = mediaUri, modifier = Modifier.fillMaxSize())
    }

    // Event selection dropdown at top
    EventSelectionDropdown(
        state = eventSelectionState,
        selectedEvent = selectedEvent,
        onEventSelected = { selectedEvent = it },
        onLoadEvents = onLoadEvents,
        modifier =
            Modifier.align(Alignment.TopCenter)
                .padding(
                    top = EVENT_SELECTOR_TOP_PADDING,
                    start = EVENT_SELECTOR_HORIZONTAL_PADDING,
                    end = EVENT_SELECTOR_HORIZONTAL_PADDING)
                .testTag("media_preview_event_selector"))

    // Action buttons
    Row(
        modifier =
            Modifier.align(Alignment.BottomCenter)
                .padding(bottom = ACTIONS_BOTTOM_PADDING)
                .testTag("media_preview_actions"),
        horizontalArrangement = Arrangement.spacedBy(ACTIONS_SPACING)) {
          // Retake button
          IconButton(
              onClick = onRetake,
              modifier =
                  Modifier.size(ACTION_BUTTON_SIZE)
                      .background(
                          Color.White.copy(alpha = 0.2f),
                          shape = androidx.compose.foundation.shape.CircleShape)
                      .testTag("media_preview_retake")) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.content_description_retake),
                    tint = Color.White,
                    modifier = Modifier.size(ACTION_ICON_SIZE))
              }

          // Accept button - disabled until an event is selected
          val isAcceptEnabled = selectedEvent != null
          IconButton(
              onClick = { if (isAcceptEnabled) onAccept(selectedEvent) },
              enabled = isAcceptEnabled,
              modifier =
                  Modifier.size(ACTION_BUTTON_SIZE)
                      .background(
                          if (isAcceptEnabled) Color(0xFF4CAF50).copy(alpha = 0.8f)
                          else Color.Gray.copy(alpha = 0.4f),
                          shape = androidx.compose.foundation.shape.CircleShape)
                      .testTag("media_preview_accept")) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.content_description_accept),
                    tint = if (isAcceptEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(ACTION_ICON_SIZE))
              }
        }
  }
}

@Composable
private fun PhotoPreview(imageUri: Uri, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val bitmap =
      remember(imageUri) {
        try {
          // Decode the bitmap
          val originalBitmap =
              context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
              }

          // Read EXIF orientation
          originalBitmap?.let { bmp ->
            val exif =
                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                  ExifInterface(inputStream)
                }

            val orientation =
                exif?.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    ?: ExifInterface.ORIENTATION_NORMAL

            // Rotate bitmap based on EXIF orientation
            rotateBitmap(bmp, orientation)
          }
        } catch (e: IOException) {
          Log.e("PhotoPreview", "IO error loading image", e)
          null
        } catch (e: IllegalStateException) {
          Log.e("PhotoPreview", "Illegal state loading image", e)
          null
        } catch (e: Exception) {
          Log.e("PhotoPreview", "Unexpected error loading image", e)
          null
        }
      }

  bitmap?.let {
    Image(
        bitmap = it.asImageBitmap(),
        contentDescription = stringResource(R.string.content_description_captured_photo),
        modifier = modifier.testTag("photo_preview"),
        contentScale = ContentScale.Fit)
  }
}

/** Rotates a bitmap based on EXIF orientation data. */
private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
  val matrix = Matrix()

  when (orientation) {
    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
    ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
    ExifInterface.ORIENTATION_TRANSPOSE -> {
      matrix.postRotate(90f)
      matrix.postScale(-1f, 1f)
    }
    ExifInterface.ORIENTATION_TRANSVERSE -> {
      matrix.postRotate(-90f)
      matrix.postScale(-1f, 1f)
    }
    else -> return bitmap
  }

  return try {
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    if (rotatedBitmap != bitmap) {
      bitmap.recycle()
    }
    rotatedBitmap
  } catch (e: Exception) {
    bitmap
  }
}

@Composable
private fun VideoPreview(videoUri: Uri, modifier: Modifier = Modifier) {
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

  DisposableEffect(videoUri) {
    onDispose {
      mediaPlayer?.release()
      mediaPlayer = null
    }
  }

  AndroidView(
      factory = { ctx ->
        SurfaceView(ctx).apply {
          holder.addCallback(
              object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                  try {
                    mediaPlayer =
                        MediaPlayer().apply {
                          setDataSource(ctx, videoUri)
                          setDisplay(holder)
                          isLooping = true
                          prepare()
                          start()
                        }
                  } catch (e: IOException) {
                    Log.e("VideoPreview", "IO error initializing MediaPlayer", e)
                  } catch (e: IllegalStateException) {
                    Log.e("VideoPreview", "MediaPlayer already initialized", e)
                  } catch (e: Exception) {
                    Log.e("VideoPreview", "Unexpected error initializing MediaPlayer", e)
                  }
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                  // Required by SurfaceHolder.Callback but not used
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                  mediaPlayer?.release()
                  mediaPlayer = null
                }
              })
        }
      },
      modifier = modifier.testTag("video_preview"))
}
