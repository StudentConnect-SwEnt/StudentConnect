// carousel idea inspired by :
// https://proandroiddev.com/swipeable-image-carousel-with-smooth-animations-in-jetpack-compose-76eacdc89bfb

package com.github.se.studentconnect.ui.activities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivitiesScreen(
    navController: NavHostController = rememberNavController(),
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
  val mainItemWidth = screenWidth * 0.7f
  val sidePeekWidth = (screenWidth - mainItemWidth) / 2
  val pagerState = rememberPagerState { carouselItems.size }
  val countDownViewModel: CountDownViewModel = viewModel()
  val timeLeft by countDownViewModel.timeLeft.collectAsState()
  LaunchedEffect(pagerState.currentPage) {
    val currentEvent = carouselItems[pagerState.currentPage]
    countDownViewModel.startCountdown(currentEvent.start)
  }
  Scaffold { paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start) {
          HorizontalPager(
              state = pagerState,
              modifier = Modifier.fillMaxWidth(),
              pageSpacing = 10.dp,
              contentPadding = PaddingValues(horizontal = sidePeekWidth),
          ) { page ->
            CarouselCard(
                item = carouselItems[page],
                modifier =
                    Modifier.width(mainItemWidth).graphicsLayer {
                      val pageOffset =
                          (pagerState.currentPage - page + pagerState.currentPageOffsetFraction)
                              .absoluteValue

                      lerp(
                              start = 75.dp,
                              stop = 100.dp,
                              fraction = 1f - pageOffset.coerceIn(0f, 1f))
                          .also { scale -> scaleY = scale / 100.dp }
                    })
          }

          Button(
              onClick = {
                // navController.navigate("all_events_screen")
              },
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(start = 25.dp, top = 6.dp, end = 25.dp, bottom = 6.dp),
          ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                  horizontalAlignment = Alignment.Start, modifier = Modifier.wrapContentHeight()) {
                    Text(text = "Event chat", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Get The Latest News About The Event",
                        style = MaterialTheme.typography.labelSmall)
                  }
              Icon(
                  imageVector = Icons.Default.Textsms,
                  contentDescription = "Go to chat",
                  tint = MaterialTheme.colorScheme.onPrimary)
            }
          }

          Column(
              verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.Top),
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(start = 25.dp, top = 6.dp, end = 25.dp, bottom = 6.dp)) {
                Text(text = "CountDown :", style = TitleTextStyle())
                CountDownDisplay(timeLeft)
                Text(text = "Description", style = TitleTextStyle())
                Text(text = carouselItems[pagerState.currentPage].description)
              }
          Spacer(modifier = Modifier.weight(1f))
          EventActionButtons()//activitiesViewModel)
        }
  }
}

private fun date(seconds: Long): Timestamp =
    Timestamp(
        Date.from(
            LocalDateTime.now().plusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant()))

@Composable
fun CarouselCard(item: CarouselItem /*Event*/, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier.height(220.dp),
      elevation = CardDefaults.cardElevation(8.dp),
      shape = RoundedCornerShape(24.dp)) {
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
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.title, style = TitleTextStyle(), textAlign = TextAlign.Center)
                Text(
                    text = item.supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
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
              onClick = { /* TODO: Naviguer vers le site web */},
              shape = RoundedCornerShape(16.dp)) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Visit Website")
              }

          // Bouton "Share Event"
          Button(
              onClick = { /* TODO: Partager l'événement */},
              shape = RoundedCornerShape(16.dp),
          ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Event")
          }
        }

        // Bouton "Leave Event"
        OutlinedButton(
            onClick = { /* TODO: Quitter l'événement */},
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = BorderStroke(1.dp, Color.Red)) {
              Spacer(modifier = Modifier.width(8.dp))
              Text("Leave Event")
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
