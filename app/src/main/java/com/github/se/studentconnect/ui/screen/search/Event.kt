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
    alone: Boolean,
    events: List<Event>
) {

  val bitmaps = getBitmaps(events)
  headText("Events", C.Tag.event_search_result_title)
  if (alone) {
    LazyColumn(
        modifier =
            Modifier.padding(
                    screenWidth.value * 0.05f,
                )
                .testTag(C.Tag.event_search_result),
    ) {
      items(events) { event ->
        EventCardColumn(
            event = event,
            ownerUsername = viewModel.getUser(event.ownerId)?.username ?: "",
            participantCount = viewModel.eventParticipantCount(eventUid = event.uid),
            navController = navController,
            bitmaps[event.uid])
        columnSpacer()
      }
    }
  } else {
    LazyRow() {
      items(events) { event ->
        rowSpacer()
        EventCardRow(
            event = event,
            ownerUsername = viewModel.getUser(event.ownerId)?.username ?: "",
            imageBitmap = bitmaps[event.uid],
            navController = navController)
      }
      item { endRowSpacer() }
    }
  }
}

@Composable
private fun EventCardRow(
    event: Event,
    ownerUsername: String,
    imageBitmap: ImageBitmap?,
    navController: NavHostController
) {
  Box(
      modifier =
          rowCardBoxModifier {
            navController.navigate(Route.eventView(eventUid = event.uid, true))
          }) {
        Column {
          if (imageBitmap != null) {

            Image(
                bitmap = imageBitmap,
                contentDescription = "Event Profile Picture",
                modifier =
                    Modifier.clip(RoundedCornerShape(screenWidth.value * 0.003f))
                        .size(screenWidth.value * 0.3f),
                contentScale = ContentScale.Crop)
          } else {
            Image(
                Icons.Default.Image,
                contentDescription = "Event Profile Picture",
                modifier =
                    Modifier.size(screenWidth.value * 0.3f)
                        .clip(RoundedCornerShape(screenWidth.value * 0.003f)))
          }

          rowCardInternalSpacer()
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
                  Modifier.size(screenWidth.value * 0.3f)
                      .clip(RoundedCornerShape(screenWidth.value * 0.03f)),
              contentScale = ContentScale.Crop)
        } else {
          Image(
              Icons.Default.Image,
              contentDescription = "Event Image",
              modifier =
                  Modifier.size(screenWidth.value * 0.3f)
                      .clip(RoundedCornerShape(screenWidth.value * 0.03f)),
              contentScale = ContentScale.Crop)
        }
        columnCardInternalSpacer()
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
  events.forEach { event -> temp[event.uid] = imageBitmap(event.imageUrl) }
  return temp
}
