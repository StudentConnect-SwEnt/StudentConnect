package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.profile.EventListItemCard
import com.github.se.studentconnect.ui.theme.Dimensions

/** Test tags for the EventTemplateSelectionScreen and its components. */
object EventTemplateSelectionScreenTestTags {
  const val SCAFFOLD = "templateSelectionScaffold"
  const val TOP_APP_BAR = "templateSelectionTopAppBar"
  const val BACK_BUTTON = "templateSelectionBackButton"
  const val LOADING_INDICATOR = "templateSelectionLoading"
  const val EMPTY_STATE = "templateSelectionEmptyState"
  const val EVENT_LIST = "templateSelectionEventList"
  const val EVENT_CARD_PREFIX = "templateEventCard_"
}

/**
 * Screen for selecting an existing event as a template for creating a new event. Reuses the visual
 * design from the JoinedEventsScreen via [EventListItemCard].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTemplateSelectionScreen(
    navController: NavHostController,
    viewModel: EventTemplateSelectionViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(Unit) { viewModel.loadUserEvents() }

  Scaffold(
      modifier = Modifier.testTag(EventTemplateSelectionScreenTestTags.SCAFFOLD),
      topBar = {
        TopAppBar(
            modifier = Modifier.testTag(EventTemplateSelectionScreenTestTags.TOP_APP_BAR),
            title = { Text(text = stringResource(R.string.title_select_template)) },
            navigationIcon = {
              IconButton(
                  modifier = Modifier.testTag(EventTemplateSelectionScreenTestTags.BACK_BUTTON),
                  onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back))
                  }
            })
      }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          when {
            uiState.isLoading -> {
              CircularProgressIndicator(
                  modifier =
                      Modifier.align(Alignment.Center)
                          .testTag(EventTemplateSelectionScreenTestTags.LOADING_INDICATOR))
            }
            uiState.events.isEmpty() -> {
              Column(
                  modifier =
                      Modifier.align(Alignment.Center)
                          .padding(Dimensions.SpacingXLarge)
                          .testTag(EventTemplateSelectionScreenTestTags.EMPTY_STATE),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.IconSizeXLarge),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(Dimensions.SpacingNormal))
                    Text(
                        text = stringResource(R.string.template_no_events),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
            }
            else -> {
              LazyColumn(
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(horizontal = Dimensions.SpacingNormal)
                          .testTag(EventTemplateSelectionScreenTestTags.EVENT_LIST),
                  verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
                    item { Spacer(modifier = Modifier.height(Dimensions.SpacingSmall)) }

                    items(uiState.events) { event ->
                      EventListItemCard(
                          event = event,
                          onClick = {
                            when (event) {
                              is Event.Public ->
                                  navController.navigate(
                                      Route.createPublicEventFromTemplate(event.uid))
                              is Event.Private ->
                                  navController.navigate(
                                      Route.createPrivateEventFromTemplate(event.uid))
                            }
                          },
                          // Use Location (or desc) instead of Date for templates
                          footerText = event.location?.name ?: event.description,
                          modifier =
                              Modifier.testTag(
                                  "${EventTemplateSelectionScreenTestTags.EVENT_CARD_PREFIX}${event.uid}"),
                          actionContent = null // No pin button needed
                          )
                    }

                    item { Spacer(modifier = Modifier.height(Dimensions.SpacingSmall)) }
                  }
            }
          }
        }
      }
}
