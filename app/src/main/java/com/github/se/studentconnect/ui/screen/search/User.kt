package com.github.se.studentconnect.ui.screen.search

import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import kotlinx.coroutines.Dispatchers

@Composable
internal fun People(screenWidth: Dp, screenHeight: Dp, alone: Boolean, users: List<User>) {
  val bitmaps = getBitmaps(users)
  Column {
    Text(
        "People",
        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
        fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
        modifier =
            Modifier.padding(
                    screenWidth * 0.05f,
                    screenHeight * 0.01f,
                    0.dp,
                    0.dp,
                )
                .testTag(C.Tag.user_search_result_title),
    )
    if (alone) {
      LazyColumn(
          Modifier.padding(
              screenWidth * 0.05f,
          )) {
            items(users) { user ->
              UserCardColumn(user, screenWidth, bitmaps[user.userId])
              Spacer(Modifier.size(screenWidth * 0.02f))
            }
          }
    } else {
      LazyRow(Modifier.testTag(C.Tag.user_search_result)) {
        items(users) { user ->
          Spacer(Modifier.size(screenWidth * 0.02f))
          UserCardRow(user, screenWidth = screenWidth, bitmaps[user.userId])
        }
        item { Spacer(Modifier.size(screenWidth * 0.05f)) }
      }
    }
  }
}

@Composable
private fun UserCardRow(user: User, screenWidth: Dp, imageBitmap: ImageBitmap?) {
  Box(
      modifier =
          Modifier.clickable(onClick = {})
              .clip(MaterialTheme.shapes.medium)
              .background(MaterialTheme.colorScheme.secondaryContainer)
              .padding(16.dp),
  ) {
    Column {
      if (imageBitmap != null) {

        Image(
            bitmap = imageBitmap,
            contentDescription = "User Profile Picture",
            modifier = Modifier.clip(CircleShape).size(screenWidth * 0.3f),
            contentScale = ContentScale.Crop)
      } else {
        Image(
            painter = painterResource(R.drawable.ic_user),
            contentDescription = "User Profile Picture",
            modifier = Modifier.size(screenWidth * 0.3f))
      }

      Spacer(Modifier.height(8.dp))
      Text(
          text = user.firstName + " " + user.lastName,
          fontSize = MaterialTheme.typography.bodyLarge.fontSize,
      )
      Text(user.username)
    }
  }
}

@Composable
private fun UserCardColumn(user: User, screenWidth: Dp, imageBitmap: ImageBitmap?) {

  Row(modifier = Modifier.clickable(onClick = {}), verticalAlignment = Alignment.CenterVertically) {
    if (imageBitmap != null) {
      Image(
          imageBitmap,
          contentDescription = "Event Image",
          modifier = Modifier.size(screenWidth * 0.2f).clip(CircleShape),
          contentScale = ContentScale.Crop)
    } else {
      Image(
          painter = painterResource(R.drawable.ic_user),
          contentDescription = "Event Image",
          modifier = Modifier.size(screenWidth * 0.2f).clip(CircleShape),
      )
    }
    Spacer(Modifier.size(10.dp))
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
      Text(
          text = user.firstName + " " + user.lastName,
          fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
          fontSize = MaterialTheme.typography.headlineMedium.fontSize,
      )
      Text(
          text = user.username,
          fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
          fontSize = MaterialTheme.typography.bodyLarge.fontSize,
      )
    }
  }
}

@Composable
private fun getBitmaps(users: List<User>): Map<String, ImageBitmap?> {
  val temp = mutableMapOf<String, ImageBitmap?>()
  users.forEach { user -> temp[user.userId] = imageBitmapUser(user) }
  return temp
}

@Composable
private fun imageBitmapUser(user: User): ImageBitmap? {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val profileId = user.profilePictureUrl
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, profileId, repository) {
        value =
            profileId?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure { Log.e("eventViewImage", "Failed to download event image: $id", it) }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }
  return imageBitmap
}
