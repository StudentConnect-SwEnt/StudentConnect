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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.utils.DialogNotImplemented

@Composable
internal fun Organizations(
    alone: Boolean,
    fakeOrgaCount: Int,
) {
  Column {
    headText("Organizations", C.Tag.user_search_result_title)
    if (alone) {
      LazyColumn(
          Modifier.padding(
              screenWidth.value * 0.05f,
          )) {
            items(fakeOrgaCount) { num ->
              OrganizationCardColumn(num)
              columnSpacer()
            }
          }
    } else {
      LazyRow(Modifier.testTag(C.Tag.user_search_result)) {
        items(fakeOrgaCount) { num ->
          rowSpacer()
          OrganizationCardRow(num)
        }
        item { endRowSpacer() }
      }
    }
  }
}

@Composable
private fun OrganizationCardRow(num: Int) {
  val context = LocalContext.current
  Box(modifier = rowCardBoxModifier { DialogNotImplemented(context) }) {
    Column {
      Image(
          Icons.Default.Image,
          contentDescription = "Organization Profile Picture Row",
          modifier = Modifier.clip(CircleShape).size(screenWidth.value * 0.3f),
          contentScale = ContentScale.FillBounds)

      rowCardInternalSpacer()
      Text(
          text = "Organization $num",
          fontSize = MaterialTheme.typography.bodyLarge.fontSize,
      )
    }
  }
}

@Composable
private fun OrganizationCardColumn(num: Int) {
  val context = LocalContext.current
  Row(
      modifier = Modifier.clickable(onClick = { DialogNotImplemented(context) }),
      verticalAlignment = Alignment.CenterVertically) {
        Image(
            Icons.Default.Image,
            contentDescription = "Organization Profile Picture Column",
            modifier = Modifier.size(screenWidth.value * 0.2f).clip(CircleShape),
        )
        columnCardInternalSpacer()

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
          Text(
              text = "Organization $num",
              fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
              fontSize = MaterialTheme.typography.headlineMedium.fontSize,
          )
          Text(
              text =
                  "Organization $num is a fake organization, made to look great when searching while the organization logic is finished in the app. Please don't try anything as it is not yet implemented.",
              fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
              fontSize = MaterialTheme.typography.bodyLarge.fontSize,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis)
        }
      }
}
