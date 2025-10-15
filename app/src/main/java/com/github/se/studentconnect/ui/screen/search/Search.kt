package com.github.se.studentconnect.ui.screen.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.navigation.Route

/**
 * The Search screen of the app, allowing users to search for people and events.
 *
 * @param modifier The modifier to be applied to the screen.
 * @param navController The navigation controller for navigating between screens.
 * @param viewModel The ViewModel managing the state of the search screen.
 */
@Preview
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: SearchViewModel = SearchViewModel(),
) {
  LaunchedEffect(Unit) { viewModel.init() }

  Scaffold(
      topBar = { SearchTopBar(viewModel, navController) },
      modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
  ) { innerPadding ->
    Column(
        modifier = modifier.padding(innerPadding),
    ) {
      People(viewModel)
      Events(viewModel)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    viewModel: SearchViewModel,
    navController: NavHostController,
) {
  CenterAlignedTopAppBar(
      title = { SBar(viewModel) },
      modifier = Modifier.fillMaxWidth(),
      navigationIcon = {
        IconButton(
            onClick = { navController.navigate(Route.HOME) },
            content = {
              Icon(
                  painterResource(R.drawable.ic_placeholder),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurface,
              )
            },
        )
      },
      windowInsets =
          WindowInsets(
              0.dp,
              LocalConfiguration.current.screenHeightDp.dp * 0.01f,
              LocalConfiguration.current.screenWidthDp.dp * 0.02f,
              LocalConfiguration.current.screenHeightDp.dp * 0.01f,
          ),
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SBar(
    viewModel: SearchViewModel,
) {
  SearchBar(
      inputField = {
        SearchBarDefaults.InputField(
            leadingIcon = {
              Icon(
                  painterResource(R.drawable.ic_search),
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = MaterialTheme.colorScheme.onSurface,
              )
            },
            query = viewModel.state.value.query,
            onQueryChange = {
              ImeAction.Search
              viewModel.setQuery(it)
            },
            placeholder = { Text("Search") },
            onSearch = {},
            expanded = false,
            onExpandedChange = {},
        )
      },
      expanded = false,
      onExpandedChange = {},
      colors =
          SearchBarColors(
              MaterialTheme.colorScheme.surfaceContainer,
              MaterialTheme.colorScheme.onSurface,
          ),
      modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 5.dp),
  ) {}
}

@Composable
private fun People(viewModel: SearchViewModel) {
  if (viewModel.hasUsers())
      Column {
        Text(
            "People",
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
        )
        Row(
            modifier =
                Modifier.scrollable(
                    state = ScrollableState { f -> f },
                    orientation = Orientation.Horizontal,
                ),
        ) {
          for (user in viewModel.state.value.shownUsers) {
            UserCard(user)
          }
        }
      }
}

@Composable
private fun UserCard(user: User) {
  Box(
      modifier =
          Modifier.clickable(onClick = {})
              .clip(MaterialTheme.shapes.medium)
              .background(MaterialTheme.colorScheme.secondaryContainer)
              .padding(16.dp),
  ) {
    Column {
      Image(
          painterResource(R.drawable.ic_user),
          contentDescription = null,
          modifier = Modifier.size(128.dp),
      )
      Spacer(Modifier.height(8.dp))
      Text(
          text = user.firstName + " " + user.lastName,
          fontSize = MaterialTheme.typography.bodyLarge.fontSize,
      )
      Text(user.userId)
    }
  }
}

@Composable
private fun Events(viewModel: SearchViewModel) {

  if (viewModel.hasEvents())
      Text(
          "Events",
          fontSize = MaterialTheme.typography.titleMedium.fontSize,
          fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
      )
  Column(
      modifier =
          Modifier.scrollable(
              state = ScrollableState { f -> f },
              orientation = Orientation.Vertical,
          )) {
        for (event in viewModel.state.value.shownEvents) {
          EventCard(event)
        }
      }
}

@Composable
private fun EventCard(event: Event) {
  Row(modifier = Modifier.clickable(onClick = {}), verticalAlignment = Alignment.CenterVertically) {
    Image(
        painterResource(R.drawable.ic_ticket),
        contentDescription = null,
        modifier = Modifier.size(128.dp),
    )
    Column(
        modifier = Modifier.size(256.dp, 128.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
      Text(
          text = event.title,
          fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
          fontSize = MaterialTheme.typography.headlineMedium.fontSize,
      )
      Text(
          text = event.ownerId,
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
          )
        }
      }
      Icon(painterResource(R.drawable.ic_users), contentDescription = null)
    }
  }
}
