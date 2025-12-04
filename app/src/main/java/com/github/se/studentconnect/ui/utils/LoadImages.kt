package com.github.se.studentconnect.ui.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.user.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadBitmapFromUri(
    context: Context,
    uri: Uri,
    dispatcher: CoroutineDispatcher
): ImageBitmap? =
    withContext(dispatcher) {
      try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
      } catch (_: Exception) {
        null
      }
    }

@Composable
fun loadBitmapFromUriComposable(
    context: Context,
    uri: String?,
): ImageBitmap? {
  val repository = MediaRepositoryProvider.repository
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, uri, repository) {
        value =
            uri?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure { Log.e("eventViewImage", "Failed to download user image: $id", it) }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }
  return imageBitmap
}

@Composable
fun loadBitmapFromUser(context: Context, user: User): ImageBitmap? =
    loadBitmapFromUriComposable(context, user.profilePictureUrl)

@Composable
fun loadBitmapFromEvent(context: Context, event: Event): ImageBitmap? =
    loadBitmapFromUriComposable(context, event.imageUrl)
