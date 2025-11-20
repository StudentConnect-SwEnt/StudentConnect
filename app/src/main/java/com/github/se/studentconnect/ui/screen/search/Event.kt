package com.github.se.studentconnect.ui.screen.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import kotlin.collections.forEach
import kotlinx.coroutines.Dispatchers

@Composable
internal fun Events(
    viewModel: SearchViewModel,
    navController: NavHostController,
    screenWidth: Dp,
    screenHeight: Dp,
    alone: Boolean,
    events: List<Event>
) {

  val bitmaps = getBitmaps(events)
  Text(
      "Events",
      fontSize = MaterialTheme.typography.headlineSmall.fontSize,
      fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
      modifier =
          Modifier.padding(
                  screenWidth * 0.05f,
                  screenHeight * 0.01f,
                  0.dp,
                  0.dp,
              )
              .testTag(C.Tag.event_search_result_title),
  )
  if (alone) {
    LazyColumn(
        modifier =
            Modifier.padding(
                    screenWidth * 0.05f,
                    0.dp,
                    screenWidth * 0.05f,
                    0.dp,
                )
                .testTag(C.Tag.event_search_result),
    ) {
      items(events) { event ->
        EventCardColumn(
            event = event,
            ownerUsername = viewModel.getUser(event.ownerId)?.username ?: "",
            participantCount = viewModel.eventParticipantCount(eventUid = event.uid),
            navController = navController,
            screenWidth = screenWidth,
            bitmaps[event.uid])
        Spacer(Modifier.size(8.dp))
      }
    }
  } else {
    LazyRow() {
      items(events) { event ->
        Spacer(Modifier.size(screenWidth * 0.02f))
        EventCardRow(
            event = event,
            ownerUsername = viewModel.getUser(event.ownerId)?.username ?: "",
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            imageBitmap = bitmaps[event.uid])
      }
      item { Spacer(Modifier.size(screenWidth * 0.05f)) }
    }
  }
}

@Composable
private fun EventCardRow(
    event: Event,
    screenWidth: Dp,
    screenHeight: Dp,
    ownerUsername: String,
    imageBitmap: ImageBitmap?
) {
  Box(
      modifier =
          Modifier.clickable(onClick = {})
              .clip(MaterialTheme.shapes.medium)
              .background(MaterialTheme.colorScheme.secondaryContainer)
              .padding(screenWidth * 0.03f)
              .size(screenWidth * 0.3f, screenHeight * 0.2f),
  ) {
    Column {
      if (imageBitmap != null) {

        Image(
            bitmap = imageBitmap,
            contentDescription = "Event Profile Picture",
            modifier =
                Modifier.clip(RoundedCornerShape(screenWidth * 0.003f)).size(screenWidth * 0.3f),
            contentScale = ContentScale.Crop)
      } else {
        Image(
            Icons.Default.Image,
            contentDescription = "Event Profile Picture",
            modifier =
                Modifier.size(screenWidth * 0.3f).clip(RoundedCornerShape(screenWidth * 0.003f)))
      }

      Spacer(Modifier.height(8.dp))
      Text(
          text = event.title,
          fontSize = MaterialTheme.typography.bodyLarge.fontSize,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
      Text(ownerUsername)
    }
  }
}

@Composable
private fun EventCardColumn(
    event: Event,
    ownerUsername: String,
    participantCount: Int,
    navController: NavHostController,
    screenWidth: Dp,
    imageBitmap: ImageBitmap?
) {
  Row(
      modifier =
          Modifier.clickable(
              onClick = { navController.navigate(Route.eventView(eventUid = event.uid, true)) }),
      verticalAlignment = Alignment.CenterVertically) {
        if (imageBitmap != null) {
          Image(
              imageBitmap,
              contentDescription = "Event Image",
              modifier =
                  Modifier.size(screenWidth * 0.3f).clip(RoundedCornerShape(screenWidth * 0.03f)),
              contentScale = ContentScale.Crop)
        } else {
          Image(
              Icons.Default.Image,
              contentDescription = "Event Image",
              modifier =
                  Modifier.size(screenWidth * 0.3f).clip(RoundedCornerShape(screenWidth * 0.03f)),
              contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.size(10.dp))
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
          Text(
              text = event.title,
              fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
              fontSize = MaterialTheme.typography.headlineMedium.fontSize,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
          Text(
              text = ownerUsername,
              fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
              fontSize = MaterialTheme.typography.bodyLarge.fontSize,
          )
          event.location?.name?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(painterResource(R.drawable.ic_location), contentDescription = null)
              Text(
                  text = it,
                  fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
                  fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)
            }
          }
          if (event.maxCapacity != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(painterResource(R.drawable.ic_users), contentDescription = null)
              Spacer(Modifier.size(8.dp))
              Text(text = "$participantCount/${event.maxCapacity}")
            }
          } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(painterResource(R.drawable.ic_users), contentDescription = null)
              Spacer(Modifier.size(8.dp))
              Text(text = "$participantCount")
            }
          }
        }
      }
}

@Composable
private fun getBitmaps(events: List<Event>): Map<String, ImageBitmap?> {
  val temp = mutableMapOf<String, ImageBitmap?>()
  events.forEach { event -> temp[event.uid] = imageBitmapEvent(event) }
  return temp
}

@Composable
private fun imageBitmapEvent(event: Event): ImageBitmap? {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val profileId = event.imageUrl
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, profileId, repository) {
        value =
            profileId?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure {
                    android.util.Log.e("eventViewImage", "Failed to download event image: $id", it)
                  }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }
  return imageBitmap
}
