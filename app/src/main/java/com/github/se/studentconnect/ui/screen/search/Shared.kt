package com.github.se.studentconnect.ui.screen.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import kotlinx.coroutines.Dispatchers

internal val screenWidth = mutableStateOf(0.dp)
internal val screenHeight = mutableStateOf(0.dp)

@Composable internal fun columnSpacer() = Spacer(Modifier.size(screenHeight.value * 0.01f))

@Composable
internal fun columnCardInternalSpacer() = Spacer(Modifier.size(screenWidth.value * 0.02f))

@Composable internal fun rowSpacer() = Spacer(Modifier.size(screenWidth.value * 0.02f))

@Composable internal fun endRowSpacer() = Spacer(Modifier.size(screenWidth.value * 0.05f))

@Composable
internal fun rowCardInternalSpacer() = Spacer(Modifier.height(screenHeight.value * 0.005f))

@Composable
internal fun rowCardBoxModifier(onClick: () -> Unit) =
    Modifier.clickable(onClick = onClick)
        .clip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .padding(screenWidth.value * 0.03f)
        .width(screenWidth.value * 0.3f)

@Composable
internal fun headText(text: String, testTag: String) =
    Text(
        text,
        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
        fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
        modifier =
            Modifier.padding(
                    screenWidth.value * 0.05f,
                    screenHeight.value * 0.01f,
                    0.dp,
                    0.dp,
                )
                .testTag(testTag))

@Composable
internal fun imageBitmap(url: String?): ImageBitmap? {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, url, repository) {
        value =
            url?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure { Log.e("eventViewImage", "Failed to download event image: $id", it) }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }
  return imageBitmap
}
