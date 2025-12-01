package com.github.se.studentconnect.ui.screen.search

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.navigation.Route

// Pastel lavender/grey color scheme
private val BackgroundColor = Color(0xFFF5F4F8)
private val SurfaceColor = Color(0xFFEAE8F0)
private val CardColor = Color(0xFFE3E1EC)
private val TextColorPrimary = Color(0xFF3C3A47)
private val TextColorSecondary = Color(0xFF6B6978)
private val PlaceholderColor = Color(0xFFB8B6C3)

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
      modifier =
          modifier
              .fillMaxSize()
              .background(BackgroundColor)
              .testTag(C.Tag.search_screen),
      containerColor = BackgroundColor,
      topBar = { SearchTopBar(viewModel, navController) },
  ) { innerPadding ->
    LazyColumn(
        modifier =
            modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(BackgroundColor),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
      item {
        if (viewModel.hasUsers()) {
          PeopleSection(viewModel = viewModel, navController = navController)
        }
      }
      item {
        if (viewModel.hasOrganizations()) {
          OrganizationsSection(viewModel = viewModel)
        }
      }
      item {
        if (viewModel.hasEvents()) {
          EventsSection(viewModel = viewModel)
        }
      }
      item { Spacer(Modifier.height(32.dp)) }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    viewModel: SearchViewModel,
    navController: NavHostController,
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(BackgroundColor)
              .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
  ) {
    Text(
        text = "Search Results",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextColorPrimary,
        modifier = Modifier.padding(bottom = 16.dp),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            tint = TextColorSecondary,
        )
      }

      TextField(
          value = viewModel.state.value.query,
          onValueChange = { viewModel.setQuery(it) },
          placeholder = {
            Text(
                text = "Search",
                color = PlaceholderColor,
            )
          },
          leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextColorSecondary,
            )
          },
          colors =
              TextFieldDefaults.colors(
                  focusedContainerColor = SurfaceColor,
                  unfocusedContainerColor = SurfaceColor,
                  disabledContainerColor = SurfaceColor,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedIndicatorColor = Color.Transparent,
                  disabledIndicatorColor = Color.Transparent,
                  focusedTextColor = TextColorPrimary,
                  unfocusedTextColor = TextColorPrimary,
              ),
          shape = RoundedCornerShape(28.dp),
          modifier = Modifier.fillMaxWidth().testTag(C.Tag.search_input_field),
          singleLine = true,
      )
    }
  }
}

@Composable
private fun PeopleSection(viewModel: SearchViewModel, navController: NavHostController) {
  Column(modifier = Modifier.padding(start = 16.dp)) {
    Text(
        text = "People",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextColorPrimary,
        modifier = Modifier.padding(bottom = 16.dp).testTag(C.Tag.user_search_result_title),
    )
    LazyRow(
        modifier = Modifier.testTag(C.Tag.user_search_result),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      items(viewModel.state.value.shownUsers) { user ->
        PersonCard(user = user, navController = navController)
      }
      item { Spacer(Modifier.width(16.dp)) }
    }
  }
}

@Composable
private fun PersonCard(user: User, navController: NavHostController) {
  Card(
      modifier =
          Modifier.width(160.dp)
              .height(200.dp)
              .clickable { navController.navigate(Route.visitorProfile(user.userId)) },
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CardColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      PlaceholderShapes()
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "${user.firstName} ${user.lastName}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "@${user.username}",
            fontSize = 14.sp,
            color = TextColorSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun OrganizationsSection(viewModel: SearchViewModel) {
  Column(modifier = Modifier.padding(start = 16.dp)) {
    Text(
        text = "Organisations",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextColorPrimary,
        modifier =
            Modifier.padding(bottom = 16.dp).testTag(C.Tag.organisation_search_result_title),
    )
    LazyRow(
        modifier = Modifier.testTag(C.Tag.organisation_search_result),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      items(viewModel.state.value.shownOrganizations) { organization ->
        OrganizationCard(organization = organization)
      }
      item { Spacer(Modifier.width(16.dp)) }
    }
  }
}

@Composable
private fun OrganizationCard(organization: Organization) {
  Card(
      modifier = Modifier.width(160.dp).height(200.dp).clickable {},
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CardColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      PlaceholderShapes()
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = organization.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun EventsSection(viewModel: SearchViewModel) {
  Column(modifier = Modifier.padding(start = 16.dp)) {
    Text(
        text = "Events",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextColorPrimary,
        modifier = Modifier.padding(bottom = 16.dp).testTag(C.Tag.event_search_result_title),
    )
    LazyRow(
        modifier = Modifier.testTag(C.Tag.event_search_result),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      items(viewModel.state.value.shownEvents) { event -> EventCard(event = event) }
      item { Spacer(Modifier.width(16.dp)) }
    }
  }
}

@Composable
private fun EventCard(event: Event) {
  Card(
      modifier = Modifier.width(200.dp).height(200.dp).clickable {},
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CardColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      PlaceholderShapes()
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = event.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = event.ownerId,
            fontSize = 14.sp,
            color = TextColorSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun PlaceholderShapes() {
  Row(
      modifier = Modifier.fillMaxWidth().height(60.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    // Triangle-like blob
    Box(
        modifier =
            Modifier.size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PlaceholderColor),
    )
    // Starburst (represented as circle with different shape)
    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(PlaceholderColor))
    // Rounded rectangle
    Box(
        modifier =
            Modifier.width(32.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PlaceholderColor),
    )
  }
}
