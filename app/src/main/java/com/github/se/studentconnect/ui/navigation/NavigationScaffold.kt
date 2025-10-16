package com.github.se.studentconnect.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onCreatePublicEvent: () -> Unit = {},
    onCreatePrivateEvent: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
  var showBottomSheet by remember { mutableStateOf(false) }
  val bottomSheetState = rememberModalBottomSheetState()

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
                .clickable { showBottomSheet = true }
                .testTag("center_add_button"))

    // Bottom Sheet for event creation options
    if (showBottomSheet) {
      ModalBottomSheet(
          onDismissRequest = { showBottomSheet = false },
          sheetState = bottomSheetState,
          modifier = Modifier.testTag("event_creation_bottom_sheet")) {
            EventCreationBottomSheetContent(
                onCreatePublicEvent = {
                  showBottomSheet = false
                  onCreatePublicEvent()
                },
                onCreatePrivateEvent = {
                  showBottomSheet = false
                  onCreatePrivateEvent()
                },
                onDismiss = { showBottomSheet = false })
          }
    }
  }
}

@Composable
fun EventCreationBottomSheetContent(
    onCreatePublicEvent: () -> Unit,
    onCreatePrivateEvent: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Create New Event",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp).testTag("bottom_sheet_title"))

        // Public Event Option
        Surface(
            onClick = onCreatePublicEvent,
            modifier = Modifier.fillMaxWidth().testTag("create_public_event_option"),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  horizontalArrangement = Arrangement.spacedBy(16.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_web),
                        contentDescription = "Public Event",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                    Column {
                      Text(
                          text = "Create Public Event",
                          style =
                              MaterialTheme.typography.titleMedium.copy(
                                  fontWeight = FontWeight.Medium),
                          color = MaterialTheme.colorScheme.onSurface)
                      Text(
                          text = "Visible to everyone",
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
            }

        // Private Event Option
        Surface(
            onClick = onCreatePrivateEvent,
            modifier = Modifier.fillMaxWidth().testTag("create_private_event_option"),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  horizontalArrangement = Arrangement.spacedBy(16.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lock),
                        contentDescription = "Private Event",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                    Column {
                      Text(
                          text = "Create Private Event",
                          style =
                              MaterialTheme.typography.titleMedium.copy(
                                  fontWeight = FontWeight.Medium),
                          color = MaterialTheme.colorScheme.onSurface)
                      Text(
                          text = "Invite only",
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
            }

        Spacer(modifier = Modifier.height(16.dp))
      }
}
