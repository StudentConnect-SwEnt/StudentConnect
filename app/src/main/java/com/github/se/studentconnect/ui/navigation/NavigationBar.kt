package com.github.se.studentconnect.ui.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.*

sealed class Tab(val name: String, val icon: ImageVector, val destination: Screen) {
    object Home : Tab("Home", Icons.Outlined.Home, Screen.Home)
    object Map : Tab("Map", Icons.Outlined.NearMe, Screen.Map)
    object CreateEvent : Tab("Create Event", Icons.Outlined.AddBox, Screen.EventCreation)

    object Events : Tab("Events", Icons.Outlined.ConfirmationNumber, Screen.Events)
    object Profile : Tab("Profile", Icons.Outlined.AccountCircle, Screen.Profile)
}

private val tabs = listOf(
    Tab.Home,
    Tab.Map,
    Tab.CreateEvent,
    Tab.Events,
    Tab.Profile
)

/**
 * A bottom navigation bar for switching between different tabs.
 *
 * @param selectedTab The currently selected tab.
 * @param onTabSelected A callback that is triggered when a tab is selected.
 * @param modifier The [Modifier] to be applied to this navigation bar.
 * */
@Composable
fun BottomNavigationBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
){
    NavigationBar(
        modifier =
            modifier.fillMaxWidth().height(64.dp)
                .clip(RoundedCornerShape(100))
                .testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
        windowInsets = WindowInsets(12.dp, 0.dp, 12.dp, 0.dp),
        content = {
            tabs.forEach { tab ->
                if (tab == Tab.CreateEvent) {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Outlined.AddBox,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = Purple40
                            )
                        },
                        selected = tab == selectedTab,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.testTag(NavigationTestTags.CREATE_EVENT_TAB)
                    )
                }else{
                    NavigationBarItem(
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = null,
                                )
                               },
                        label = { Text(tab.name) },
                        selected = tab == selectedTab,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.testTag(NavigationTestTags.getTabTestTag(tab))
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun BottomNavigationPreview(){
    BottomNavigationBar(
        selectedTab = Tab.Home,
        onTabSelected = { }
    )
}

class BottomNavigationTestActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
                    color = MaterialTheme.colorScheme.background) {
                    BottomNavigationBar(
                        Tab.Home,
                        onTabSelected = { }
                    )
                }
            }
        }
    }
}
