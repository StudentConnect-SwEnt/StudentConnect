package com.github.se.studentconnect.ui.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.AppTheme

sealed class Tab(val name: String, val icon: Int, val destination: Screen) {
  object Home : Tab("Home", R.drawable.ic_home, Screen.Home)

  object Map : Tab("Map", R.drawable.ic_vector, Screen.Map)

  object CreateEvent : Tab("Create Event", R.drawable.ic_add, Screen.EventCreation)

  object Events : Tab("Events", R.drawable.ic_ticket, Screen.Events)

  object Profile : Tab("Profile", R.drawable.ic_user, Screen.Profile)
}

private val tabs = listOf(Tab.Home, Tab.Map, Tab.CreateEvent, Tab.Events, Tab.Profile)

/**
 * A bottom navigation bar for switching between different tabs.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected A callback that is triggered when a tab is selected.
 * @param modifier The [Modifier] to be applied to this navigation bar.
 */
@Composable
fun BottomNavigationBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
  NavigationBar(
      modifier =
          modifier
              .fillMaxWidth()
              .height(64.dp)
              .clip(RoundedCornerShape(100))
              .testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
      windowInsets = WindowInsets(12.dp, 0.dp, 12.dp, 0.dp),
      content = {
        tabs.forEach { tab ->
          if (tab == Tab.CreateEvent) {
            NavigationBarItem(
                icon = {
                  Icon(
                      painterResource(tab.icon),
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                  )
                },
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.testTag(NavigationTestTags.CREATE_EVENT_TAB),
            )
          } else {
            NavigationBarItem(
                icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                label = { Text(tab.name) },
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.testTag(NavigationTestTags.getTabTestTag(tab)),
            )
          }
        }
      },
  )
}

/*@Preview
@Composable
private fun BottomNavigationPreview() {
  BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {})
}*/

class BottomNavigationTestActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background,
        ) {
          BottomNavigationBar(Tab.Home, onTabSelected = {})
        }
      }
    }
  }
}
