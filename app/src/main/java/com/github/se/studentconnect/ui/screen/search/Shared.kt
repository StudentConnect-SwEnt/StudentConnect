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

object SearchScreenTestTags {
  const val SEARCH_SCREEN = "search_screen"
  const val TOP_BAR = "search_screen_top_bar"
  const val SEARCH_FIELD = "search_screen_search_field"
  const val BACK_BUTTON = "search_screen_back_button"
  const val USERS_RESULTS = "search_screen_users_results"
  const val USERS_TITLE = "search_screen_users_results_title"
  const val USERS_COLUMN = "search_screen_users_results_column"
  const val USERS_ROW = "search_screen_users_results_row"
  const val USER_COLUMN_CARD = "search_screen_users_results_column_card"
  const val USER_ROW_CARD = "search_screen_users_results_row_card"
  const val EVENTS_RESULTS = "search_screen_events_results"
  const val EVENTS_TITLE = "search_screen_events_results_title"
  const val EVENT_COLUMN = "search_screen_event_results_column"
  const val EVENT_ROW = "search_screen_event_results_row"
  const val EVENT_COLUMN_CARD = "search_screen_events_results_column_card"
  const val EVENT_ROW_CARD = "search_screen_events_results_row_card"
  const val ORGANIZATIONS_RESULTS = "search_screen_organizations_results"
  const val ORGANIZATIONS_TITLE = "search_screen_organizations_results_title"
  const val ORGANIZATIONS_COLUMN = "search_screen_organizations_results_column"
  const val ORGANIZATIONS_ROW = "search_screen_organizations_results_row"
  const val ORGANIZATION_COLUMN_CARD = "search_screen_organizations_results_column_card"
  const val ORGANIZATION_ROW_CARD = "search_screen_organizations_results_row_card"
}

internal val screenWidth = mutableStateOf(0.dp)
internal val screenHeight = mutableStateOf(0.dp)

@Composable internal fun ColumnSpacer() = Spacer(Modifier.size(screenHeight.value * 0.01f))

@Composable
internal fun ColumnCardInternalSpacer() = Spacer(Modifier.size(screenWidth.value * 0.02f))

@Composable internal fun RowSpacer() = Spacer(Modifier.size(screenWidth.value * 0.02f))

@Composable internal fun EndRowSpacer() = Spacer(Modifier.size(screenWidth.value * 0.05f))

@Composable
internal fun RowCardInternalSpacer() = Spacer(Modifier.height(screenHeight.value * 0.005f))

@Composable
internal fun Modifier.rowCardBoxModifier(onClick: () -> Unit) =
    this.clickable(onClick = onClick)
        .clip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .padding(screenWidth.value * 0.03f)
        .width(screenWidth.value * 0.3f)

@Composable
internal fun HeadText(text: String, testTag: String) =
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
                .testTag(testTag),
    )

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
