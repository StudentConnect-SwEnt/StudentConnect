package com.github.se.studentconnect.ui.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineDispatcher
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
