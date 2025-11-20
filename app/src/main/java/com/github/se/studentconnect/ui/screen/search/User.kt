package com.github.se.studentconnect.ui.screen.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.navigation.Route

@Composable
internal fun People(alone: Boolean, users: List<User>, navController: NavHostController) {
  val bitmaps = getBitmaps(users)
  Column {
    headText("People", C.Tag.user_search_result_title)
    if (alone) {
      LazyColumn(
          Modifier.padding(
              screenWidth.value * 0.05f,
          )) {
            items(users) { user ->
              UserCardColumn(user, bitmaps[user.userId], navController)
              columnSpacer()
            }
          }
    } else {
      LazyRow(Modifier.testTag(C.Tag.user_search_result)) {
        items(users) { user ->
          rowSpacer()
          UserCardRow(user, bitmaps[user.userId], navController)
        }
        item { endRowSpacer() }
      }
    }
  }
}

@Composable
private fun UserCardRow(user: User, imageBitmap: ImageBitmap?, navController: NavHostController) {
  Box(modifier = rowCardBoxModifier { navController.navigate(Route.visitorProfile(user.userId)) }) {
    Column {
      if (imageBitmap != null) {

        Image(
            bitmap = imageBitmap,
            contentDescription = "User Profile Picture",
            modifier = Modifier.clip(CircleShape).size(screenWidth.value * 0.3f),
            contentScale = ContentScale.Crop)
      } else {
        Image(
            painter = painterResource(R.drawable.ic_user),
            contentDescription = "User Profile Picture",
            modifier = Modifier.size(screenWidth.value * 0.3f))
      }

      rowCardInternalSpacer()
      Text(
          text = user.firstName + " " + user.lastName,
          fontSize = MaterialTheme.typography.bodyLarge.fontSize,
      )
      Text(user.username)
    }
  }
}

@Composable
private fun UserCardColumn(
    user: User,
    imageBitmap: ImageBitmap?,
    navController: NavHostController
) {

  Row(
      modifier =
          Modifier.clickable(
              onClick = { navController.navigate(Route.visitorProfile(user.userId)) }),
      verticalAlignment = Alignment.CenterVertically) {
        if (imageBitmap != null) {
          Image(
              imageBitmap,
              contentDescription = "Event Image",
              modifier = Modifier.size(screenWidth.value * 0.2f).clip(CircleShape),
              contentScale = ContentScale.Crop)
        } else {
          Image(
              painter = painterResource(R.drawable.ic_user),
              contentDescription = "Event Image",
              modifier = Modifier.size(screenWidth.value * 0.2f).clip(CircleShape),
          )
        }
        columnCardInternalSpacer()
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
  users.forEach { user -> temp[user.userId] = imageBitmap(user.profilePictureUrl) }
  return temp
}
