package com.github.se.studentconnect.ui.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.utils.HomeSearchBar

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
  screenWidth.value =
      with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
  screenHeight.value =
      with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
  val hasUsers = viewModel.hasUsers()
  val hasEvents = viewModel.hasEvents()

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
      if (hasUsers) {
        People(
            alone = !hasEvents,
            users = viewModel.state.value.shownUsers,
            navController = navController)
      }
      if (hasEvents) {
        Events(viewModel, navController, !hasUsers, events = viewModel.state.value.shownEvents)
      }
      if ((hasEvents && hasUsers) || (!hasEvents && !hasUsers)) {
        Organizations(
            alone = (!hasEvents),
            fakeOrgaCount = 15,
        )
      }
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
  )
}
