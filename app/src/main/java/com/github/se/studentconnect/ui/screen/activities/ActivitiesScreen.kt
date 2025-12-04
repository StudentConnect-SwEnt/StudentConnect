// carousel idea inspired by :
// https://proandroiddev.com/swipeable-image-carousel-with-smooth-animations-in-jetpack-compose-76eacdc89bfb
package com.github.se.studentconnect.ui.screen.activities

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.activities.InvitationStatus
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.utils.loadBitmapFromEvent
import com.google.firebase.Timestamp
import java.util.*

sealed interface CarouselDisplayItem {
  val uid: String
}

data class EventCarouselItem(val event: Event) : CarouselDisplayItem {
  override val uid: String
    get() = event.uid
}

data class InvitationCarouselItem(
    val invitation: Invitation,
    val event: Event,
    val invitedBy: String
) : CarouselDisplayItem {
  override val uid: String
    get() = invitation.eventId
}

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
  const val CAROUSEL_SKELETON = "carousel_skeleton"

  fun carouselCardTag(eventUid: String) = "carousel_card_$eventUid"

  fun tab(title: String) = "tab_$title"
}
/**
 * Event tabs for the Activities screen.
 * - Upcoming: Shows upcoming events the user has joined.
 * - MyEvents: Shows events created by the user.
 * - Past: Shows past events the user has attended.
 */
enum class EventTab {
  Upcoming,
  Invitations,
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
  val carouselItems = uiState.items
  val isLoading = uiState.isLoading

  LaunchedEffect(selectedTab) {
    activitiesViewModel.refreshEvents(AuthenticationProvider.currentUser)
  }
  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val mainItemWidth = screenWidth * 0.85f
  val sidePeekWidth = (screenWidth - mainItemWidth) / 2
  val pagerState = rememberPagerState { carouselItems.size }

  LaunchedEffect(selectedTab, carouselItems.size) {
    if (pagerState.currentPage >= carouselItems.size) {
      pagerState.scrollToPage(0)
    }
  }
  Scaffold(
      modifier = Modifier.testTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN),
      topBar = {
        CenterAlignedTopAppBar(
            title = { Text("MyActivities") },
            modifier = Modifier.fillMaxWidth().testTag(ActivitiesScreenTestTags.TOP_APP_BAR),
        )
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

          if (isLoading) {
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            CarouselSkeleton(
                modifier =
                    Modifier.width(mainItemWidth)
                        .height(screenHeight * 0.65f)
                        .testTag(ActivitiesScreenTestTags.CAROUSEL_SKELETON))
          } else if (carouselItems.isEmpty()) {
            EmptyState(
                selectedTab = selectedTab, onNavigate = { navController.navigate(Route.HOME) })
          } else {
            Carousel(
                pagerState = pagerState,
                sidePeekWidth = sidePeekWidth,
                carouselItems = carouselItems,
                mainItemWidth = mainItemWidth,
                viewModel = activitiesViewModel,
                onEventClick = { eventId, isJoined ->
                  navController.navigate(Route.eventView(eventId, isJoined))
                })
          }
        }
  }
}

/** Composable for the loading skeleton */
@Composable
private fun CarouselSkeleton(modifier: Modifier = Modifier) {
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp
  Card(
      modifier = modifier.height(screenHeight * 0.65f),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start) {
              // Image placeholder
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .weight(1f)
                          .clip(RoundedCornerShape(16.dp))
                          .background(Color.Gray.copy(alpha = 0.5f)))
              Spacer(modifier = Modifier.height(16.dp))

              Box(
                  modifier =
                      Modifier.fillMaxWidth(0.8f)
                          .height(30.dp)
                          .clip(RoundedCornerShape(8.dp))
                          .background(Color.Gray.copy(alpha = 0.5f)))
              Spacer(modifier = Modifier.height(8.dp))

              Box(
                  modifier =
                      Modifier.fillMaxWidth(0.5f)
                          .height(20.dp)
                          .clip(RoundedCornerShape(8.dp))
                          .background(Color.Gray.copy(alpha = 0.5f)))
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
            title = stringResource(R.string.text_no_upcoming_events)
            description = stringResource(R.string.text_empty_upcoming_description)
            buttonText = stringResource(R.string.button_explore)
          }
          EventTab.Invitations -> {
            title = stringResource(R.string.text_no_new_invitations)
            description = stringResource(R.string.text_empty_invitations_description)
            buttonText = stringResource(R.string.button_explore)
          }
          EventTab.Past -> {
            title = stringResource(R.string.text_no_past_events)
            description = stringResource(R.string.text_empty_past_description)
            buttonText = stringResource(R.string.button_explore)
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
  val upcomingTitle = stringResource(R.string.tab_upcoming)
  val invitationsTitle = stringResource(R.string.tab_invitations)
  val archivedTitle = stringResource(R.string.tab_archived)
  val tabs =
      mapOf(
          EventTab.Upcoming to upcomingTitle,
          EventTab.Invitations to invitationsTitle,
          EventTab.Past to archivedTitle)
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
    carouselItems: List<CarouselDisplayItem>,
    mainItemWidth: Dp,
    viewModel: ActivitiesViewModel,
    onEventClick: (String, Boolean) -> Unit
) {
  HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxWidth().testTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL),
      pageSpacing = 16.dp,
      contentPadding = PaddingValues(horizontal = sidePeekWidth)) { page ->
        val item = carouselItems.getOrNull(page) ?: return@HorizontalPager
        val cardModifier =
            Modifier.width(mainItemWidth)
                .testTag(ActivitiesScreenTestTags.carouselCardTag(item.uid))
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
                                    MaterialTheme.colorScheme.primary)))

        when (item) {
          is EventCarouselItem -> {
            val isOwner = item.event.ownerId == AuthenticationProvider.currentUser
            CarouselCard(
                item = item.event,
                isOwner = isOwner,
                modifier = cardModifier,
                onEventClick = { onEventClick(item.event.uid, true) })
          }
          is InvitationCarouselItem -> {
            InvitationCarouselCard(
                item = item,
                modifier = cardModifier,
                onCardClick = { onEventClick(item.event.uid, false) },
                onAcceptClick = { viewModel.acceptInvitation(item.invitation) },
                onDeclineClick = { viewModel.declineInvitation(item.invitation) })
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselCard(
    item: Event,
    isOwner: Boolean,
    modifier: Modifier = Modifier,
    onEventClick: () -> Unit
) {
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp

  val now = Timestamp.now()
  val endTime =
      item.end
          ?: run {
            val cal = Calendar.getInstance()
            cal.time = item.start.toDate()
            cal.add(Calendar.HOUR_OF_DAY, 3)
            Timestamp(cal.time)
          }
  val isLive = now >= item.start && now < endTime
  val isPrivate = item is Event.Private
  val context = LocalContext.current
  val imageBitmap = loadBitmapFromEvent(context, item)

  Card(
      onClick = onEventClick,
      modifier = modifier.height(screenHeight * 0.65f),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      shape = RoundedCornerShape(20.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
          Column(
              modifier = Modifier.fillMaxSize().padding(24.dp),
              verticalArrangement = Arrangement.Bottom,
              horizontalAlignment = Alignment.Start) {
                if (imageBitmap != null) {
                  Image(
                      imageBitmap,
                      contentDescription = "Event Image",
                      modifier =
                          Modifier.fillMaxWidth()
                              .weight(1f)
                              .clip(RoundedCornerShape(16.dp))
                              .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)),
                      contentScale = ContentScale.Crop)
                } else {
                  Icon(
                      imageVector = Icons.Default.Image,
                      contentDescription = "Event Image",
                      modifier =
                          Modifier.fillMaxWidth()
                              .weight(1f)
                              .clip(RoundedCornerShape(16.dp))
                              .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)),
                      tint = MaterialTheme.colorScheme.onPrimary)
                }

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

          // Event type icon (Public/Private) and LIVE badge
          Row(
              modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isLive) {
                  Row(
                      modifier =
                          Modifier.background(Color.Red.copy(alpha = 0.9f), shape = CircleShape)
                              .padding(horizontal = 10.dp, vertical = 5.dp),
                      verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Circle,
                            contentDescription = "Live Icon",
                            tint = Color.White,
                            modifier = Modifier.size(8.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold)
                      }
                }

                // Public/Private icon
                Icon(
                    painter =
                        painterResource(if (isPrivate) R.drawable.ic_lock else R.drawable.ic_web),
                    contentDescription = if (isPrivate) "Private Event" else "Public Event",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier =
                        Modifier.size(28.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                shape = CircleShape)
                            .padding(6.dp))
              }

          if (isOwner) {
            Icon(
                painter = painterResource(R.drawable.ic_crown),
                contentDescription = "Owner",
                tint = Color.Yellow,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(32.dp))
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationCarouselCard(
    item: InvitationCarouselItem,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit
) {
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp
  var acceptState by remember { mutableStateOf(false) }
  var declineState by remember { mutableStateOf(false) }

  val isDeclined = item.invitation.status == InvitationStatus.Declined || declineState
  val cardAlpha = if (isDeclined) 0.7f else 1.0f
  val context = LocalContext.current
  val imageBitmap = loadBitmapFromEvent(context, item.event)

  Card(
      onClick = onCardClick,
      modifier = modifier.height(screenHeight * 0.65f).graphicsLayer(alpha = cardAlpha),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      shape = RoundedCornerShape(20.dp),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
          modifier = Modifier.fillMaxSize().padding(24.dp),
          verticalArrangement = Arrangement.Bottom,
          horizontalAlignment = Alignment.Start) {
            if (imageBitmap != null) {
              Image(
                  imageBitmap,
                  contentDescription = "Event Image",
                  modifier =
                      Modifier.fillMaxWidth()
                          .weight(1f)
                          .clip(RoundedCornerShape(16.dp))
                          .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)),
                  contentScale = ContentScale.Crop)
            } else {
              Icon(
                  imageVector = Icons.Default.Image,
                  contentDescription = "Event Image",
                  modifier =
                      Modifier.fillMaxWidth()
                          .weight(1f)
                          .clip(RoundedCornerShape(16.dp))
                          .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)),
                  tint = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(
                      text = item.event.title,
                      style = MaterialTheme.typography.headlineSmall,
                      color = MaterialTheme.colorScheme.onPrimary,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f, fill = false))
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                          acceptState = !acceptState
                          declineState = false
                          onAcceptClick()
                        }) {
                          Icon(
                              imageVector = Icons.Default.Check,
                              contentDescription = "Accept",
                              tint =
                                  if (acceptState) Color.Green.copy(alpha = 0.8f)
                                  else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                        }
                    IconButton(
                        onClick = {
                          declineState = !declineState
                          acceptState = false
                          onDeclineClick()
                        }) {
                          Icon(
                              imageVector = Icons.Default.Close,
                              contentDescription = "Decline",
                              tint =
                                  if (isDeclined) Color.Red.copy(alpha = 0.9f)
                                  else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                        }
                  }
                }
            Text(
                text = (item.event as? Event.Public)?.subtitle ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Invited by: ${item.invitedBy}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
          }
    }
  }
}
