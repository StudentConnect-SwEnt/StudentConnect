package com.github.se.studentconnect.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onCenterButtonClick: () -> Unit = {}
) {
  Box(modifier = modifier.fillMaxWidth()) {
    NavigationBar(
        modifier =
            Modifier.fillMaxWidth()
                .height(64.dp)
                .testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
        windowInsets = WindowInsets(12.dp, 0.dp, 12.dp, 0.dp),
    ) {
      // First two tabs (Home, Map)
      bottomNavigationTabs.take(2).forEach { tab ->
        NavigationBarItem(
            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
            label = { Text(tab.name) },
            selected = tab == selectedTab,
            onClick = { onTabSelected(tab) },
            modifier = Modifier.testTag(NavigationTestTags.getTabTestTag(tab)),
        )
      }

      // Empty space for center button
      NavigationBarItem(icon = {}, label = {}, selected = false, onClick = {}, enabled = false)

      // Last two tabs (Activities, Profile)
      bottomNavigationTabs.drop(2).forEach { tab ->
        NavigationBarItem(
            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
            label = { Text(tab.name) },
            selected = tab == selectedTab,
            onClick = { onTabSelected(tab) },
            modifier = Modifier.testTag(NavigationTestTags.getTabTestTag(tab)),
        )
      }
    }

    // Center add button
    Icon(
        painter = painterResource(R.drawable.ic_add),
        contentDescription = "Add",
        tint = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier.align(Alignment.Center)
                .width(64.dp)
                .clickable { onCenterButtonClick() }
                .testTag("center_add_button"))
  }
}
