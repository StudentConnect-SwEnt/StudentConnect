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
import com.github.se.studentconnect.ui.navigation.Route

private val bitmapsBuffer = mutableMapOf<String, ImageBitmap?>()

@Composable
internal fun People(alone: Boolean, users: List<User>, navController: NavHostController) {
  Column(modifier = Modifier.testTag(SearchScreenTestTags.USERS_RESULTS)) {
    HeadText("People", SearchScreenTestTags.USERS_TITLE)
    if (alone) {
      LazyColumn(
          Modifier.padding(
                  screenWidth.value * 0.05f,
              )
              .testTag(SearchScreenTestTags.USERS_COLUMN)) {
            items(users) { user ->
              UserCardColumn(user, navController)
              ColumnSpacer()
            }
          }
    } else {
      LazyRow(Modifier.testTag(SearchScreenTestTags.USERS_ROW)) {
        items(users) { user ->
          RowSpacer()
          UserCardRow(user, navController)
        }
        item { EndRowSpacer() }
      }
    }
  }
}

@Composable
private fun UserCardRow(user: User, navController: NavHostController) {
  addToBitmapBuffer(user)
  val imageBitmap = bitmapsBuffer[user.userId]
  Box(
      modifier =
          Modifier.rowCardBoxModifier { navController.navigate(Route.visitorProfile(user.userId)) }
              .testTag(SearchScreenTestTags.USER_ROW_CARD)) {
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

          RowCardInternalSpacer()
          Text(
              text = user.firstName + " " + user.lastName,
              fontSize = MaterialTheme.typography.bodyLarge.fontSize,
              maxLines = 1,
          )
          Text(
              user.username,
              maxLines = 1,
          )
        }
      }
}

@Composable
private fun UserCardColumn(user: User, navController: NavHostController) {
  addToBitmapBuffer(user)
  val imageBitmap = bitmapsBuffer[user.userId]
  Row(
      modifier =
          Modifier.clickable(
                  onClick = { navController.navigate(Route.visitorProfile(user.userId)) })
              .testTag(SearchScreenTestTags.USER_COLUMN_CARD),
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
        ColumnCardInternalSpacer()
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
          Text(
              text = user.firstName + " " + user.lastName,
              fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
              fontSize = MaterialTheme.typography.headlineMedium.fontSize,
              maxLines = 1)
          Text(
              text = user.username,
              fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
              fontSize = MaterialTheme.typography.bodyLarge.fontSize,
              maxLines = 1)
        }
      }
}

@Composable
private fun addToBitmapBuffer(user: User) {
  bitmapsBuffer.putIfAbsent(user.userId, imageBitmap(user.profilePictureUrl))
}
