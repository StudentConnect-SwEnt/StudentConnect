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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.theme.Dimensions

/**
 * Bottom navigation bar with a centered add button for event creation.
 *
 * This composable displays the main navigation tabs (Home, Map, Activities, Profile) with a
 * centered floating action button that opens a bottom sheet for creating events. The bottom sheet
 * provides options to create public events, private events, or create events from existing
 * templates.
 *
 * @param selectedTab The currently selected navigation tab
 * @param onTabSelected Callback invoked when a tab is selected
 * @param onCreatePublicEvent Callback invoked when the user chooses to create a public event
 * @param onCreatePrivateEvent Callback invoked when the user chooses to create a private event
 * @param onCreateFromTemplate Callback invoked when the user chooses to create from a template
 * @param modifier Modifier to be applied to the navigation bar container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onCreatePublicEvent: () -> Unit = {},
    onCreatePrivateEvent: () -> Unit = {},
    onCreateFromTemplate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
  var showBottomSheet by remember { mutableStateOf(false) }
  val bottomSheetState = rememberModalBottomSheetState()

  Box(modifier = modifier.fillMaxWidth()) {
    NavigationBar(
        modifier =
            Modifier.fillMaxWidth()
                .height(Dimensions.BottomNavHeight)
                .testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
        windowInsets =
            WindowInsets(
                Dimensions.SpacingMedium,
                Dimensions.SpacingTiny,
                Dimensions.SpacingMedium,
                Dimensions.SpacingTiny),
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
        contentDescription = stringResource(R.string.content_description_add),
        tint = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier.align(Alignment.Center)
                .width(Dimensions.BottomNavButtonWidth)
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
                onCreateFromTemplate = {
                  showBottomSheet = false
                  onCreateFromTemplate()
                })
          }
    }
  }
}

/**
 * Content for the event creation bottom sheet.
 *
 * Displays three options for creating events:
 * - Create Public Event: Visible to everyone
 * - Create Private Event: Invite only
 * - Create from Template: Use a previous event as template
 *
 * @param onCreatePublicEvent Callback invoked when public event option is selected
 * @param onCreatePrivateEvent Callback invoked when private event option is selected
 * @param onCreateFromTemplate Callback invoked when template option is selected
 * @param modifier Modifier to be applied to the content container
 */
@Composable
fun EventCreationBottomSheetContent(
    onCreatePublicEvent: () -> Unit,
    onCreatePrivateEvent: () -> Unit,
    onCreateFromTemplate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = Dimensions.SpacingLarge, vertical = Dimensions.SpacingNormal),
      verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)) {
        Text(
            text = stringResource(R.string.bottom_sheet_create_event_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier.padding(bottom = Dimensions.SpacingNormal).testTag("bottom_sheet_title"))

        // Public Event Option
        Surface(
            onClick = onCreatePublicEvent,
            modifier = Modifier.fillMaxWidth().testTag("create_public_event_option"),
            shape = RoundedCornerShape(Dimensions.CardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceVariant) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(Dimensions.SpacingNormal),
                  horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingNormal),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_web),
                        contentDescription =
                            stringResource(R.string.content_description_public_event),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimensions.IconSizeMedium))
                    Column {
                      Text(
                          text = stringResource(R.string.bottom_sheet_create_public_event),
                          style =
                              MaterialTheme.typography.titleMedium.copy(
                                  fontWeight = FontWeight.Medium),
                          color = MaterialTheme.colorScheme.onSurface)
                      Text(
                          text = stringResource(R.string.bottom_sheet_create_public_event_desc),
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
            }

        // Private Event Option
        Surface(
            onClick = onCreatePrivateEvent,
            modifier = Modifier.fillMaxWidth().testTag("create_private_event_option"),
            shape = RoundedCornerShape(Dimensions.CardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceVariant) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(Dimensions.SpacingNormal),
                  horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingNormal),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lock),
                        contentDescription =
                            stringResource(R.string.content_description_private_event),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimensions.IconSizeMedium))
                    Column {
                      Text(
                          text = stringResource(R.string.bottom_sheet_create_private_event),
                          style =
                              MaterialTheme.typography.titleMedium.copy(
                                  fontWeight = FontWeight.Medium),
                          color = MaterialTheme.colorScheme.onSurface)
                      Text(
                          text = stringResource(R.string.bottom_sheet_create_private_event_desc),
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
            }

        // Create from Template Option
        Surface(
            onClick = onCreateFromTemplate,
            modifier = Modifier.fillMaxWidth().testTag("create_from_template_option"),
            shape = RoundedCornerShape(Dimensions.CardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceVariant) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(Dimensions.SpacingNormal),
                  horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingNormal),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription =
                            stringResource(R.string.content_description_create_from_template),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimensions.IconSizeMedium))
                    Column {
                      Text(
                          text = stringResource(R.string.bottom_sheet_create_from_template),
                          style =
                              MaterialTheme.typography.titleMedium.copy(
                                  fontWeight = FontWeight.Medium),
                          color = MaterialTheme.colorScheme.onSurface)
                      Text(
                          text = stringResource(R.string.bottom_sheet_create_from_template_desc),
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
            }

        Spacer(modifier = Modifier.height(Dimensions.SpacingNormal))
      }
}
