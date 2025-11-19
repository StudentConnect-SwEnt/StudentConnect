package com.github.se.studentconnect.ui.screen.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.utils.HomeSearchBar
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import kotlinx.coroutines.Dispatchers

/**
 * The Search screen of the app, allowing users to search for people and events.
 *
 * @param modifier The modifier to be applied to the screen.
 * @param navController The navigation controller for navigating between screens.
 * @param viewModel The ViewModel managing the state of the search screen.
 */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: SearchViewModel = viewModel(),
) {
  val focusManager = LocalFocusManager.current
  val screenWidth =
      with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
  val screenHeight =
      with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }

  Scaffold(
      topBar = { SearchTopBar(viewModel, navController) },
      modifier =
          modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surface)
              .testTag(C.Tag.search_screen)
              .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
  ) { innerPadding ->
    Column(
        modifier = modifier.padding(innerPadding),
    ) {
      People(viewModel, screenWidth = screenWidth)
      Events(viewModel, navController, screenWidth = screenWidth, screenHeight = screenHeight)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    viewModel: SearchViewModel,
    navController: NavHostController,
) {
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) { focusRequester.requestFocus() }
  CenterAlignedTopAppBar(
      title = {
        HomeSearchBar(
            query = viewModel.state.value.query,
            onQueryChange = { viewModel.setQuery(it) },
            modifier = Modifier.focusRequester(focusRequester))
      },
      modifier = Modifier.fillMaxWidth(),
      navigationIcon = {
        IconButton(
            onClick = {
              navController.popBackStack()
              viewModel.reset()
            },
            content = {
              Icon(
                  painterResource(R.drawable.ic_placeholder),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurface,
              )
            },
            modifier = Modifier.testTag(C.Tag.back_button),
        )
      },
      //      windowInsets =
      //          WindowInsets(
      //              0.dp,
      //              LocalConfiguration.current.screenHeightDp.dp * 0.01f,
      //              LocalConfiguration.current.screenWidthDp.dp * 0.02f,
      //              LocalConfiguration.current.screenHeightDp.dp * 0.01f,
      //          ),
  )
}

@Composable
private fun People(viewModel: SearchViewModel, screenWidth: Dp) {
  if (viewModel.hasUsers())
      Column {
        Text(
            "People",
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
            modifier =
                Modifier.padding(
                        screenWidth * 0.05f,
                        0.dp,
                        0.dp,
                        0.dp,
                    )
                    .testTag(C.Tag.user_search_result_title),
        )
        LazyRow(Modifier.testTag(C.Tag.user_search_result)) {
          items(viewModel.state.value.shownUsers) { user ->
            Spacer(Modifier.size(screenWidth * 0.02f))
            UserCard(user, screenWidth = screenWidth)
          }
          item { Spacer(Modifier.size(screenWidth * 0.05f)) }
        }
      }
}

@Composable
private fun UserCard(user: User, screenWidth: Dp) {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val profileId = user.profilePictureUrl
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
            bitmap = imageBitmap!!,
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
private fun Events(
    viewModel: SearchViewModel,
    navController: NavHostController,
    screenWidth: Dp,
    screenHeight: Dp
) {

  if (viewModel.hasEvents())
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
    items(viewModel.state.value.shownEvents.size) { index ->
      val event = viewModel.state.value.shownEvents[index]
      EventCard(
          event = event,
          ownerUsername = viewModel.getUser(event.ownerId)?.username ?: "",
          participantCount = viewModel.eventParticipantCount(eventUid = event.uid),
          navController = navController,
          screenWidth = screenWidth)
      Spacer(Modifier.size(8.dp))
    }
  }
}

@Composable
private fun EventCard(
    event: Event,
    ownerUsername: String,
    participantCount: Int,
    navController: NavHostController,
    screenWidth: Dp
) {
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
  Row(
      modifier =
          Modifier.clickable(
              onClick = { navController.navigate(Route.eventView(eventUid = event.uid, true)) }),
      verticalAlignment = Alignment.CenterVertically) {
        if (imageBitmap != null) {
          Image(
              imageBitmap!!,
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
