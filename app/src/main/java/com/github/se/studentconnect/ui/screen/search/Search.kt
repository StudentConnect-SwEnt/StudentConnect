package com.github.se.studentconnect.ui.screen.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import kotlinx.coroutines.Dispatchers

// UI Constants
private object CardDimensions {
  val WIDTH = 132.dp
  val HEIGHT = 175.dp
  val CORNER_RADIUS = 12.dp
  val PADDING = 12.dp
  val ICON_SIZE = 64.dp
}

private object SectionDimensions {
  val HEIGHT = 210.dp
  val SPACING = 16.dp
  val SIDE_PADDING = 4.dp
}

private object SearchBarDimensions {
  val CORNER_RADIUS = 28.dp
  val VERTICAL_PADDING = 12.dp
  val HORIZONTAL_PADDING = 16.dp
  val ICON_SPACING = 4.dp
}

/**
 * The Search screen of the app, allowing users to search for people, organisations and events.
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
  Scaffold(
      modifier = modifier.fillMaxSize().testTag(C.Tag.search_screen),
      containerColor = MaterialTheme.colorScheme.surface,
      topBar = { SearchTopBar(viewModel, navController) },
  ) { innerPadding ->
    LazyColumn(
        modifier = modifier.padding(innerPadding).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item {
        if (viewModel.hasUsers()) {
          PeopleSection(viewModel = viewModel, navController = navController)
        }
      }
      item {
        if (viewModel.hasOrganizations()) {
          OrganizationsSection(viewModel = viewModel, navController = navController)
        }
      }
      item {
        if (viewModel.hasEvents()) {
          EventsSection(viewModel = viewModel, navController = navController)
        }
      }
      item { Spacer(Modifier.height(8.dp)) }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    viewModel: SearchViewModel,
    navController: NavHostController,
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .background(MaterialTheme.colorScheme.surface)
              .padding(vertical = SearchBarDimensions.VERTICAL_PADDING),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(SearchBarDimensions.ICON_SPACING),
  ) {
    IconButton(
        onClick = {
          navController.popBackStack()
          viewModel.reset()
        },
        modifier = Modifier.testTag(C.Tag.back_button),
    ) {
      Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    TextField(
        value = viewModel.state.value.query,
        onValueChange = { viewModel.setQuery(it) },
        placeholder = {
          Text(
              text = "Search",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        },
        leadingIcon = {
          Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
        shape = RoundedCornerShape(SearchBarDimensions.CORNER_RADIUS),
        modifier =
            Modifier.fillMaxWidth()
                .padding(end = SearchBarDimensions.HORIZONTAL_PADDING)
                .testTag(C.Tag.search_input_field),
        singleLine = true,
    )
  }
}

@Composable
private fun PeopleSection(viewModel: SearchViewModel, navController: NavHostController) {
  Column(modifier = Modifier.fillMaxWidth().height(SectionDimensions.HEIGHT)) {
    Text(
        text = "People",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            Modifier.padding(
                    start = SearchBarDimensions.HORIZONTAL_PADDING,
                    bottom = SectionDimensions.SPACING)
                .testTag(C.Tag.user_search_result_title),
    )
    LazyRow(
        modifier = Modifier.testTag(C.Tag.user_search_result),
        horizontalArrangement = Arrangement.spacedBy(SectionDimensions.SPACING),
    ) {
      item { Spacer(Modifier.width(SectionDimensions.SIDE_PADDING)) }
      items(viewModel.state.value.shownUsers) { user ->
        PersonCard(user = user, navController = navController)
      }
      item { Spacer(Modifier.width(SectionDimensions.SIDE_PADDING)) }
    }
  }
}

@Composable
private fun PersonCard(user: User, navController: NavHostController) {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val profileId = user.profilePictureUrl
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, profileId, repository) {
        value =
            profileId?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure {
                    android.util.Log.e("PersonCard", "Failed to download profile image: $id", it)
                  }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

  Card(
      modifier =
          Modifier.width(CardDimensions.WIDTH).height(CardDimensions.HEIGHT).clickable {
            navController.navigate(Route.visitorProfile(user.userId))
          },
      shape = RoundedCornerShape(CardDimensions.CORNER_RADIUS),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(CardDimensions.PADDING),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
          modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp),
          contentAlignment = Alignment.Center) {
            if (imageBitmap != null) {
              Image(
                  bitmap = imageBitmap!!,
                  contentDescription = "Profile picture",
                  modifier = Modifier.size(CardDimensions.ICON_SIZE).clip(CircleShape),
                  contentScale = ContentScale.Crop,
              )
            } else {
              Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = "Default profile picture",
                  modifier = Modifier.size(CardDimensions.ICON_SIZE),
                  tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
      Column(
          verticalArrangement = Arrangement.spacedBy(2.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          }
    }
  }
}

@Composable
private fun OrganizationsSection(viewModel: SearchViewModel, navController: NavHostController) {
  Column(modifier = Modifier.fillMaxWidth().height(SectionDimensions.HEIGHT)) {
    Text(
        text = "Organisations",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            Modifier.padding(
                    start = SearchBarDimensions.HORIZONTAL_PADDING,
                    bottom = SectionDimensions.SPACING)
                .testTag(C.Tag.organisation_search_result_title),
    )
    LazyRow(
        modifier = Modifier.testTag(C.Tag.organisation_search_result),
        horizontalArrangement = Arrangement.spacedBy(SectionDimensions.SPACING),
    ) {
      item { Spacer(Modifier.width(SectionDimensions.SIDE_PADDING)) }
      items(viewModel.state.value.shownOrganizations) { organization ->
        OrganizationCard(organization = organization, navController = navController)
      }
      item { Spacer(Modifier.width(SectionDimensions.SIDE_PADDING)) }
    }
  }
}

@Composable
private fun OrganizationCard(organization: Organization, navController: NavHostController) {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val logoId = organization.logoUrl
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, logoId, repository) {
        value =
            logoId?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure {
                    android.util.Log.e("OrganizationCard", "Failed to download logo: $id", it)
                  }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

  Card(
      modifier =
          Modifier.width(CardDimensions.WIDTH).height(CardDimensions.HEIGHT).clickable {
            navController.navigate(Route.organizationProfile(organization.id))
          },
      shape = RoundedCornerShape(CardDimensions.CORNER_RADIUS),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(CardDimensions.PADDING),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
          modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp),
          contentAlignment = Alignment.Center) {
            if (imageBitmap != null) {
              Image(
                  bitmap = imageBitmap!!,
                  contentDescription = "Organization logo",
                  modifier = Modifier.size(CardDimensions.ICON_SIZE).clip(CircleShape),
                  contentScale = ContentScale.Crop,
              )
            } else {
              Icon(
                  imageVector = Icons.Default.Groups,
                  contentDescription = "Default organization logo",
                  modifier = Modifier.size(CardDimensions.ICON_SIZE),
                  tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
      Column(
          verticalArrangement = Arrangement.spacedBy(2.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()) {
            Text(
                text = organization.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "@${organization.name.lowercase().replace(" ", "")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          }
    }
  }
}

@Composable
private fun EventsSection(viewModel: SearchViewModel, navController: NavHostController) {
  Column(modifier = Modifier.fillMaxWidth().height(SectionDimensions.HEIGHT)) {
    Text(
        text = "Events",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            Modifier.padding(
                    start = SearchBarDimensions.HORIZONTAL_PADDING,
                    bottom = SectionDimensions.SPACING)
                .testTag(C.Tag.event_search_result_title),
    )
    LazyRow(
        modifier = Modifier.testTag(C.Tag.event_search_result),
        horizontalArrangement = Arrangement.spacedBy(SectionDimensions.SPACING),
    ) {
      item { Spacer(Modifier.width(SectionDimensions.SIDE_PADDING)) }
      items(viewModel.state.value.shownEvents) { event ->
        EventCard(event = event, navController = navController)
      }
      item { Spacer(Modifier.width(SectionDimensions.SIDE_PADDING)) }
    }
  }
}

@Composable
private fun EventCard(event: Event, navController: NavHostController) {
  Card(
      modifier =
          Modifier.width(CardDimensions.WIDTH).height(CardDimensions.HEIGHT).clickable {
            navController.navigate(Route.eventView(event.uid, false))
          },
      shape = RoundedCornerShape(CardDimensions.CORNER_RADIUS),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(CardDimensions.PADDING),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
          modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp),
          contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = "Event ticket icon",
                modifier = Modifier.size(CardDimensions.ICON_SIZE),
                tint = MaterialTheme.colorScheme.primary,
            )
          }
      Column(
          verticalArrangement = Arrangement.spacedBy(2.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          }
    }
  }
}
