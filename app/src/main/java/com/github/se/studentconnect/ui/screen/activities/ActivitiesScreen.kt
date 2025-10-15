// carousel idea inspired by :
// https://proandroiddev.com/swipeable-image-carousel-with-smooth-animations-in-jetpack-compose-76eacdc89bfb

package com.github.se.studentconnect.ui.screen.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.repository.AuthentificationProvider
import com.github.se.studentconnect.ui.utils.Panel

/** Test tags for the Activities screen and its components. */
object ActivitiesScreenTestTags {
  const val ACTIVITIES_SCREEN = "activities_screen"
  const val TOP_APP_BAR = "top_app_bar"
  const val ACTIVITIES_TAB_ROW = "activities_tab_row"
  const val TAB_BUTTON_JOINED = "tab_button_joined"
  const val BUTTON_INVITATIONS = "tab_button_invitations"
  const val EMPTY_STATE_TEXT = "empty_state_text"
  const val ACTIVITIES_CAROUSEL = "activities_carousel"
  const val ACTIVITIES_MAIN_COLUMN = "activities_main_column"
  const val EMPTY_STATE_COLUMN = "empty_state_column"
  const val INVITATIONS_POPOVER = "invitations_popover"

  fun carouselCardTag(eventUid: String) = "carousel_card_$eventUid"

  fun tab(title: String) = "tab_$title"
}

/**
 * Event tabs for the Activities screen. - Upcoming: Shows upcoming events the user has joined. -
 * MyEvents: Shows events created by the user. - Past: Shows past events the user has attended.
 */
enum class EventTab {
  Upcoming,
  MyEvents,
  Past
}

/** Activities screen displaying joined events in a large carousel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    navController: NavHostController = rememberNavController(),
    activitiesViewModel: ActivitiesViewModel = viewModel(),
) {
  val uiState by activitiesViewModel.uiState.collectAsState()
  val selectedTab = uiState.selectedTab
  val carouselItems = uiState.events
  LaunchedEffect(Unit) { activitiesViewModel.refreshEvents(AuthentificationProvider.currentUser) }

  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val mainItemWidth = screenWidth * 0.85f
  val sidePeekWidth = (screenWidth - mainItemWidth) / 2
  val pagerState = rememberPagerState { carouselItems.size }
  var showInvitations by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN),
      topBar = {
        CenterAlignedTopAppBar(
            title = { Text("MyActivities") },
            modifier = Modifier.fillMaxWidth().testTag(ActivitiesScreenTestTags.TOP_APP_BAR),
            actions = {
              Box {
                IconButton(
                    onClick = { showInvitations = true },
                    modifier = Modifier.testTag(ActivitiesScreenTestTags.BUTTON_INVITATIONS)) {
                      Icon(
                          painter = painterResource(id = R.drawable.ic_invitation),
                          contentDescription = "invitations",
                          modifier = Modifier.size(24.dp),
                          tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                DropdownMenu(
                    expanded = showInvitations,
                    onDismissRequest = { showInvitations = false },
                    modifier =
                        Modifier.background(Color.Transparent)
                            .shadow(0.dp)
                            .testTag(ActivitiesScreenTestTags.INVITATIONS_POPOVER)) {
                      Panel<Invitation>(title = "Invitations")
                    }
              }
            })
      },
  ) { paddingValues ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .testTag(ActivitiesScreenTestTags.ACTIVITIES_MAIN_COLUMN),
        horizontalAlignment = Alignment.CenterHorizontally) {
          ActivitiesTab(
              selectedTab = selectedTab, onTabSelected = { activitiesViewModel.onTabSelected(it) })

          Spacer(modifier = Modifier.height(25.dp))

          if (carouselItems.isEmpty()) {
            EmptyState(selectedTab = selectedTab, onNavigate = { /* TODO */})
          } else {
            Carousel(
                pagerState = pagerState,
                sidePeekWidth = sidePeekWidth,
                carouselItems = carouselItems,
                mainItemWidth = mainItemWidth,
                onEventClick = { eventId -> navController.navigate("eventView/$eventId") })
          }
        }
  }
}

/** Composable for displaying a customized empty state message based on the selected tab. */
@Composable
private fun EmptyState(selectedTab: EventTab, onNavigate: () -> Unit) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier =
          Modifier.fillMaxSize()
              .padding(horizontal = 32.dp)
              .testTag(ActivitiesScreenTestTags.EMPTY_STATE_COLUMN)) {
        val title: String
        val description: String
        val buttonText: String

        when (selectedTab) {
          EventTab.Upcoming -> {
            title = "No upcoming events"
            description = "You haven't joined any events yet. Explore to find some!"
            buttonText = "Explore"
          }
          EventTab.MyEvents -> {
            title = "No events created"
            description = "Create your first event and invite your friends to join you."
            buttonText = "Create an event"
          }
          EventTab.Past -> {
            title = "No past events"
            description = "Events you have attended will appear here."
            buttonText = "Explore"
          }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(ActivitiesScreenTestTags.EMPTY_STATE_TEXT))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigate) { Text(buttonText) }
      }
}

/** Tab row for switching between event categories. */
@Composable
private fun ActivitiesTab(selectedTab: EventTab, onTabSelected: (EventTab) -> Unit) {
  val tabs =
      mapOf(
          EventTab.Upcoming to "Upcoming",
          EventTab.MyEvents to "My events",
          EventTab.Past to "Archived")
  val selectedIndex = tabs.keys.indexOf(selectedTab)

  TabRow(
      selectedTabIndex = selectedIndex,
      modifier = Modifier.fillMaxWidth().testTag(ActivitiesScreenTestTags.ACTIVITIES_TAB_ROW)) {
        tabs.forEach { (tabEnum, title) ->
          Tab(
              selected = selectedTab == tabEnum,
              onClick = { onTabSelected(tabEnum) },
              text = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
              modifier = Modifier.testTag(ActivitiesScreenTestTags.tab(title)))
        }
      }
}

@Composable
private fun Carousel(
    pagerState: PagerState,
    sidePeekWidth: Dp,
    carouselItems: List<Event>,
    mainItemWidth: Dp,
    onEventClick: (String) -> Unit
) {
  HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxWidth().testTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL),
      pageSpacing = 16.dp,
      contentPadding = PaddingValues(horizontal = sidePeekWidth)) { page ->
        val event = carouselItems[page]
        CarouselCard(
            item = event,
            modifier =
                Modifier.width(mainItemWidth)
                    .testTag(ActivitiesScreenTestTags.carouselCardTag(event.uid))
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(size = 20.dp),
                        spotColor = MaterialTheme.colorScheme.primary,
                        ambientColor = MaterialTheme.colorScheme.primary)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primary))),
            onEventClick = { onEventClick(event.uid) })
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselCard(item: Event, modifier: Modifier = Modifier, onEventClick: () -> Unit) {
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp
  Card(
      onClick = onEventClick,
      modifier = modifier.height(screenHeight * 0.65f),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start) {
              Icon(
                  imageVector = Icons.Default.Image,
                  contentDescription = "Event Image",
                  modifier =
                      Modifier.fillMaxWidth()
                          .weight(1f)
                          .clip(RoundedCornerShape(16.dp))
                          .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)),
                  tint = MaterialTheme.colorScheme.onPrimary)

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                  text = item.title,
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.onPrimary,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis)
              Text(
                  text = (item as? Event.Public)?.subtitle ?: "",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)
            }
      }
}

@Preview(showBackground = true)
@Composable
fun PreviewImageTitleCarousel() {
  MaterialTheme { ActivitiesScreen() }
}
