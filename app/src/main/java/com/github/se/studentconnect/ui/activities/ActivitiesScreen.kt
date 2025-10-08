// carousel idea inspired by : https://proandroiddev.com/swipeable-image-carousel-with-smooth-animations-in-jetpack-compose-76eacdc89bfb

package com.github.se.studentconnect.ui.activities

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlin.math.absoluteValue

data class CarouselItem(
    val id: Int,
    val title: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivitiesScreen(
    navController: NavHostController = rememberNavController(),
) {
  val carouselItems = remember {
    listOf(
        CarouselItem(0, "Event 1"),
        CarouselItem(1, "Event 2"),
        CarouselItem(2, "Event 3"),
        CarouselItem(3, "Event 4"),
        CarouselItem(4, "Event 5"),
    )
  }

  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val mainItemWidth = screenWidth * 0.7f
  val sidePeekWidth = (screenWidth - mainItemWidth) / 2

  val pagerState = rememberPagerState { carouselItems.size }
  Scaffold { paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
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
        }
  }
}

@Composable
fun CarouselCard(item: CarouselItem, modifier: Modifier = Modifier) {
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
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center)
                Text(
                    text = "Supporting Text",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
              }
            }
      }
}

@Preview(showBackground = true)
@Composable
fun PreviewImageTitleCarousel() {
  MaterialTheme { ActivitiesScreen() }
}
