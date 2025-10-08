// carousel idea inspired by :
// https://proandroiddev.com/swipeable-image-carousel-with-smooth-animations-in-jetpack-compose-76eacdc89bfb

package com.github.se.studentconnect.ui.activities

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.math.absoluteValue


data class CarouselItem(
    val id: Int,
    val title: String,
    val supportingText: String = "",
    val description: String = "No description available",
    val start: Timestamp
)

enum class EventTab{
    JoinedEvents,
    Invitations
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun ActivitiesScreen(
    navController: NavHostController = rememberNavController(),
    activitiesViewModel: ActivitiesViewModel = viewModel(),
) {
  val carouselItems = remember {
    // getEventsAttendedByUser(Firebase.auth.currentUser?.uid)
    listOf(
        CarouselItem(
            0,
            "The Killers",
            description =
                "Get ready for an epic night at Mad Cool Festival! 🔥The iconic " +
                    "The Killers are hitting the stage with all their energy, blasting out " +
                    "their legendary hits and fresh new tracks. Huge vibes, a crowd going wild, " +
                    "and sing-along anthems all night long 🎤✨" +
                    "👉 Grab your crew, bring your energy, and dance until sunrise.",
            start = date(100)),
        CarouselItem(1, "Event 2", start = date(200)),
        CarouselItem(2, "Event 3", start = date(300)),
        CarouselItem(3, "Event 4", start = date(400)),
        CarouselItem(4, "Event 5", start = date(500)),
    )
  }

  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val mainItemWidth = screenWidth * 0.75f
  val sidePeekWidth = (screenWidth - mainItemWidth) / 2
  val pagerState = rememberPagerState { carouselItems.size }
  val countDownViewModel: CountDownViewModel = viewModel()
  val timeLeft by countDownViewModel.timeLeft.collectAsState()
  LaunchedEffect(pagerState.currentPage) {
    val currentEvent = carouselItems[pagerState.currentPage]
    countDownViewModel.startCountdown(currentEvent.start)
  }
  Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Activities")},
                modifier = Modifier.fillMaxWidth() )
        },
        //bottomBar =
  ) { paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(25.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally) {
            ActivitiesTab()
          Carousel(pagerState, sidePeekWidth, carouselItems, mainItemWidth, screenWidth)
          ChatButton()
            AnimatedContent(
                targetState = pagerState.currentPage,
                label = "ContentAnimator",
                transitionSpec = {
                    val exit = fadeOut(animationSpec = tween(durationMillis = 400))

                    val enter = fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = 400))

                    enter togetherWith exit
                }
            ) { targetPage ->
                InfoEvent(
                    timeLeft = timeLeft,
                    carouselItems = carouselItems,
                    currentPage = targetPage
                )
            }
          //Spacer(modifier = Modifier.weight(1f))
          EventActionButtons()//activitiesViewModel)
        }
  }
}

@Composable
private fun ActivitiesTab() {
    var selectedTab by remember { mutableStateOf(EventTab.JoinedEvents) }
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    ) {
        Row(
        ) {
            TabButton(
                text = "Joined Events",
                isSelected = selectedTab == EventTab.JoinedEvents,
                onClick = { selectedTab = EventTab.JoinedEvents }
            )
            TabButton(
                text = "Invitations",
                isSelected = selectedTab == EventTab.Invitations,
                onClick = { selectedTab = EventTab.Invitations }
            )
        }
    }
}

@Composable
private fun TabButton(
    text : String,
    isSelected : Boolean,
    onClick : () -> Unit,
){
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(50),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun InfoEvent(
    timeLeft: Long,
    carouselItems: List<CarouselItem>,
    currentPage: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.Top),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 25.dp, top = 6.dp, end = 25.dp, bottom = 6.dp)
    ) {
        Text(text = "CountDown :", style = TitleTextStyle())
        CountDownDisplay(timeLeft)
        Text(text = "Description", style = TitleTextStyle())
        Text(text = carouselItems[currentPage].description)
    }
}

@Composable
private fun ChatButton() {
    Button(
        onClick = {
            // navController.navigate("all_events_screen")
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 25.dp, top = 6.dp, end = 25.dp, bottom = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.Start, modifier = Modifier.wrapContentHeight()
            ) {
                Text(text = "Event chat", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Get The Latest News About The Event",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Icon(
                imageVector = Icons.Default.Textsms,
                contentDescription = "Go to chat",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun Carousel(
    pagerState: PagerState,
    sidePeekWidth: Dp,
    carouselItems: List<CarouselItem>,
    mainItemWidth: Dp,
    screenWidth: Dp
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        pageSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = sidePeekWidth),
    ) { page ->
        CarouselCard(
            item = carouselItems[page],
            modifier =
                Modifier
                    .width(mainItemWidth)
                    .graphicsLayer {
                        val pageOffset =
                            (pagerState.currentPage - page + pagerState.currentPageOffsetFraction)
                                .absoluteValue

                        val scale = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                    },
            screenHeight = LocalConfiguration.current.screenHeightDp.dp, screenWidth
        )
    }
}

private fun date(seconds: Long): Timestamp =
    Timestamp(
        Date.from(
            LocalDateTime.now().plusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant()))

@Composable
fun CarouselCard(item: CarouselItem /*Event*/, modifier: Modifier = Modifier, screenHeight: Dp, screenWidth: Dp) {
  Card(
      modifier = modifier.height( screenHeight*0.5f),
      elevation = CardDefaults.cardElevation(8.dp),
      shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                  imageVector = Icons.Default.Image,
                  contentDescription = "Image",
                  modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)))
              /* add image with imageURL
              AsyncImage(
                  model = item.imageUrl,
                  contentDescription = "Event Image",
              )*/
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                  Column {
                      Text(text = item.title, style = TitleTextStyle(), textAlign = TextAlign.Center)
                      Text(
                          text = item.supportingText,
                          style = MaterialTheme.typography.bodySmall,
                          textAlign = TextAlign.Center)
                  }
                  Button(
                      contentPadding = PaddingValues(horizontal = 10.dp),
                        onClick = {}
                  ) {
                    Text("View on Map", style = MaterialTheme.typography.labelSmall)

                  }

              }
            }
      }
}

@Composable
fun EventActionButtons(/*activitiesViewModel: ActivitiesViewModel*/) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(start = 25.dp, end = 25.dp, bottom = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
          // Bouton "Visit Website"
          Button(
              onClick = { /* TODO: Naviguer vers le site web */}) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Visit Website")
              }

          // Bouton "Share Event"
          Button(
              onClick = { /* TODO: Partager l'événement */}
          ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Event")
          }
        }

        // Bouton "Leave Event"
        OutlinedButton(
            onClick = { /* TODO: Quitter l'événement */},
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = BorderStroke(1.dp, Color.Red)) {
              Spacer(modifier = Modifier.width(8.dp))
              Text("Leave Event", style = MaterialTheme.typography.labelSmall)
            }
      }
}

@Composable
fun TitleTextStyle(): TextStyle =
    MaterialTheme.typography.titleLarge.copy(
        color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)

@Preview(showBackground = true)
@Composable
fun PreviewImageTitleCarousel() {
  MaterialTheme { ActivitiesScreen() }
}
