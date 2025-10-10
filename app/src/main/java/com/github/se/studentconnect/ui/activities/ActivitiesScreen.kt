// carousel idea inspired by :
// https://proandroiddev.com/swipeable-image-carousel-with-smooth-animations-in-jetpack-compose-76eacdc89bfb

package com.github.se.studentconnect.ui.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlin.math.absoluteValue

object ActivitiesScreenTestTags {
  const val ACTIVITIES_SCREEN = "activities_screen"
  const val TOP_APP_BAR = "top_app_bar"
  const val ACTIVITIES_TAB_ROW = "activities_tab_row"
  const val TAB_BUTTON_JOINED = "tab_button_joined"
  const val TAB_BUTTON_INVITATIONS = "tab_button_invitations"
  const val EMPTY_STATE_TEXT = "empty_state_text"
  const val ACTIVITIES_CAROUSEL = "activities_carousel"
  const val INFO_EVENT_SECTION = "info_event_section"
  const val EVENT_DESCRIPTION_TEXT = "event_description_text"
  const val EVENT_ACTION_BUTTONS = "event_action_buttons"
  const val CHAT_BUTTON = "chat_button"
  const val VISIT_WEBSITE_BUTTON = "visit_website_button"
  const val SHARE_EVENT_BUTTON = "share_event_button"
  const val LEAVE_EVENT_BUTTON = "leave_event_button"
  const val COUNTDOWN_TEXT_DAYS = "countdown_text_days"
  const val COUNTDOWN_DISPLAY_TIMER = "countdown_display"

  fun carouselCardTag(eventUid: String) = "carousel_card_$eventUid"
}

data class CarouselItem(
    val id: Int,
    val title: String,
    val supportingText: String = "",
    val description: String = "No description available",
    val start: Timestamp
)

enum class EventTab {
  JoinedEvents,
  Invitations
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    navController: NavHostController = rememberNavController(),
    activitiesViewModel: ActivitiesViewModel = viewModel(),
) {
  val uiState by activitiesViewModel.uiState.collectAsState()
  val carouselItems = uiState.events
  LaunchedEffect(Unit) { activitiesViewModel.refreshEvents(Firebase.auth.currentUser?.uid ?: "") }

  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val mainItemWidth = screenWidth * 0.75f
  val sidePeekWidth = (screenWidth - mainItemWidth) / 2
  val pagerState = rememberPagerState { carouselItems.size }
  val countDownViewModel: CountDownViewModel = viewModel()
  val timeLeft by countDownViewModel.timeLeft.collectAsState()

  LaunchedEffect(pagerState.currentPage, carouselItems) {
    if (carouselItems.isNotEmpty()) {
      val currentEvent = carouselItems[pagerState.currentPage]
      countDownViewModel.startCountdown(currentEvent.start)
    }
  }

  Scaffold(
      modifier = Modifier.testTag("activities_screen"),
      topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Activities") },
            modifier = Modifier.fillMaxWidth().testTag("top_app_bar"))
      },
  ) { paddingValues ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .testTag("activities_main_column"),
        verticalArrangement = Arrangement.spacedBy(25.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally) {
          ActivitiesTab()
          if (carouselItems.isEmpty()) {
            // Corrected Empty State Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier =
                    Modifier.fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .testTag("empty_state_column")) {
                  Text(
                      text = "You have not joined any events yet",
                      style = MaterialTheme.typography.bodyLarge,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(top = 16.dp).testTag("empty_state_text"))
                }
          } else {
            // This part remains the same
            Carousel(pagerState, sidePeekWidth, carouselItems, mainItemWidth, screenWidth)
            ChatButton()
            AnimatedContent(
                targetState = pagerState.currentPage,
                label = "ContentAnimator",
                transitionSpec = {
                  val exit = fadeOut(animationSpec = tween(durationMillis = 400))
                  val enter = fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = 400))
                  enter togetherWith exit
                }) { targetPage ->
                  InfoEvent(
                      timeLeft = timeLeft,
                      carouselItems = carouselItems,
                      currentPage = targetPage,
                      modifier = Modifier.testTag("info_event_section"))
                }
            EventActionButtons(
                carouselItems[pagerState.currentPage],
                activitiesViewModel,
                modifier = Modifier.testTag("event_action_buttons"))
          }
        }
  }
}

@Composable
private fun ActivitiesTab() {
  var selectedTab by remember { mutableStateOf(EventTab.JoinedEvents) }
  Surface(
      shape = RoundedCornerShape(50), modifier = Modifier.testTag("activities_tab_row")
      // color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
      ) {
        Row() {
          TabButton(
              text = "Joined Events",
              isSelected = selectedTab == EventTab.JoinedEvents,
              onClick = { selectedTab = EventTab.JoinedEvents },
              id = R.drawable.ic_ticket,
              modifier = Modifier.testTag("tab_button_joined"))
          TabButton(
              text = "Invitations",
              isSelected = selectedTab == EventTab.Invitations,
              onClick = { selectedTab = EventTab.Invitations },
              id = R.drawable.ic_invitation,
              modifier = Modifier.testTag("tab_button_invitations"))
        }
      }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    id: Int,
    modifier: Modifier = Modifier
) {
  val backgroundColor =
      if (isSelected) MaterialTheme.colorScheme.secondary
      else MaterialTheme.colorScheme.secondaryContainer
  val contentColor =
      if (isSelected) MaterialTheme.colorScheme.onSecondary
      else MaterialTheme.colorScheme.onSecondaryContainer

  Button(
      onClick = onClick,
      modifier = modifier,
      colors =
          ButtonDefaults.buttonColors(
              containerColor = backgroundColor, contentColor = contentColor),
      shape = RoundedCornerShape(50),
      elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
      contentPadding = PaddingValues(horizontal = 16.dp),
  ) {
    Icon(
        painter = painterResource(id = id),
        contentDescription = "Home",
        modifier = Modifier.size(24.dp),
        tint = contentColor)
    Spacer(modifier = Modifier.width(6.dp))
    Text(text = text, style = MaterialTheme.typography.labelSmall)
  }
}

@Composable
private fun InfoEvent(
    timeLeft: Long,
    carouselItems: List<Event>,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
  Column(
      verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.Top),
      modifier =
          modifier.fillMaxWidth().padding(start = 25.dp, top = 6.dp, end = 25.dp, bottom = 6.dp)) {
        if (timeLeft > 86400) {
          Text(
              modifier =
                  Modifier.align(Alignment.CenterHorizontally)
                      .fillMaxHeight()
                      .testTag("countdown_text_days"),
              color = MaterialTheme.colorScheme.primary,
              text = days(timeLeft) + " days left",
              style = MaterialTheme.typography.displaySmall)
        } else {
          Box(modifier = Modifier.testTag("countdown_display")) { CountDownDisplay(timeLeft) }
        }
        Text(text = "Description", style = TitleTextStyle())
        Text(
            text = carouselItems[currentPage].description,
            modifier = Modifier.testTag("event_description_text"))
      }
}

@Composable
private fun ChatButton() {
  Button(
      onClick = {
        // navController.navigate("all_events_screen")
      },
      modifier =
          Modifier.fillMaxWidth()
              .padding(start = 25.dp, top = 6.dp, end = 25.dp, bottom = 6.dp)
              .testTag("chat_button"),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
      Column(horizontalAlignment = Alignment.Start, modifier = Modifier.wrapContentHeight()) {
        Text(
            text = "Event chat",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(
            text = "Get The Latest News About The Event",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer)
      }
      Icon(
          painter = painterResource(id = R.drawable.ic_chat_bubble),
          contentDescription = "Home",
          modifier = Modifier.size(24.dp),
          tint = MaterialTheme.colorScheme.onSecondaryContainer)
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Carousel(
    pagerState: PagerState,
    sidePeekWidth: Dp,
    carouselItems: List<Event>,
    mainItemWidth: Dp,
    screenWidth: Dp
) {
  HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxWidth().testTag("activities_carousel"),
      pageSpacing = 8.dp,
      contentPadding = PaddingValues(horizontal = sidePeekWidth)) { page ->
        val event = carouselItems[page]
        CarouselCard(
            item = event,
            modifier =
                Modifier.width(mainItemWidth)
                    .testTag("carousel_card_${event.uid}") // Dynamic test tag
                    .graphicsLayer {
                      val pageOffset =
                          (pagerState.currentPage - page + pagerState.currentPageOffsetFraction)
                              .absoluteValue
                      val scale =
                          lerp(
                              start = 0.85f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                      scaleX = scale
                      scaleY = scale
                    }
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(size = 12.dp),
                        spotColor = MaterialTheme.colorScheme.primary,
                        ambientColor = MaterialTheme.colorScheme.primary)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        // MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primary))),
            screenHeight = LocalConfiguration.current.screenHeightDp.dp,
            screenWidth)
      }
}

@Composable
fun CarouselCard(item: Event, modifier: Modifier = Modifier, screenHeight: Dp, screenWidth: Dp) {
  Card(
      modifier = modifier.height(screenHeight * 0.5f),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                  imageVector = Icons.Default.Image,
                  contentDescription = "Image",
                  modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)))
              Divider(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                  color = MaterialTheme.colorScheme.onSecondaryContainer)

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.Start,
                  verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                          text = item.title,
                          style = MaterialTheme.typography.titleMedium,
                          color = MaterialTheme.colorScheme.onPrimary,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis)
                      Text(
                          text =
                              when (item) {
                                is Event.Public -> item.subtitle
                                else -> ""
                              },
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onPrimary,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis)
                    }
                    Button(
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        onClick = { /* TODO */}) {
                          Icon(
                              painter = painterResource(id = R.drawable.ic_location_pin),
                              contentDescription = "Home",
                              modifier = Modifier.size(20.dp),
                              tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                  }
            }
      }
}

@SuppressLint("UseKtx")
@Composable
fun EventActionButtons(
    currentEvent: Event,
    activitiesViewModel: ActivitiesViewModel,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Column(
      modifier = modifier.fillMaxWidth().padding(start = 25.dp, end = 25.dp, bottom = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
          // Bouton "Visit Website"
          Button(
              contentPadding = PaddingValues(horizontal = 8.dp),
              onClick = {
                if (currentEvent is Event.Public) {
                  val website = currentEvent.website
                  if (!website.isNullOrEmpty()) {
                    val fixedUrl =
                        if (!website.startsWith("http://") && !website.startsWith("https://")) {
                          "https://$website"
                        } else {
                          website
                        }
                    try {
                      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl))
                      context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                      Toast.makeText(
                              context,
                              "No application can handle this request. Please install a web browser.",
                              Toast.LENGTH_LONG)
                          .show()
                    }
                  }
                }
              },
              modifier = Modifier.testTag("visit_website_button")) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_web),
                    contentDescription = "Website",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Visit Website")
              }

          // Bouton "Share Event"
          Button(
              contentPadding = PaddingValues(horizontal = 8.dp),
              onClick = { /* TODO: Partager l'événement */},
              modifier = Modifier.testTag("share_event_button")) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = "Share",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share Event")
              }
        }

        // Bouton "Leave Event"
        OutlinedButton(
            contentPadding = PaddingValues(horizontal = 8.dp),
            onClick = { activitiesViewModel.leaveEvent(eventUid = currentEvent.uid) },
            modifier = Modifier.testTag("leave_event_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = BorderStroke(1.dp, Color.Red)) {
              Icon(
                  painter = painterResource(id = R.drawable.ic_arrow_right),
                  contentDescription = "Leave",
                  modifier = Modifier.size(20.dp),
                  tint = Color.Red)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Leave Event", style = MaterialTheme.typography.labelSmall)
            }
      }
}

@Composable
fun TitleTextStyle(): TextStyle =
    MaterialTheme.typography.titleLarge.copy(
        color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)

// This Preview needs a ViewModel, so it's better to handle it within a test environment
@Preview(showBackground = true)
@Composable
fun PreviewImageTitleCarousel() {
  MaterialTheme { ActivitiesScreen() }
}
