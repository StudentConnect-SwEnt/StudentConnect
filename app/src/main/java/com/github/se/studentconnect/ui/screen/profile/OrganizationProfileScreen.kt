package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.organization.OrganizationEvent
import com.github.se.studentconnect.model.organization.OrganizationMember
import com.github.se.studentconnect.model.organization.OrganizationProfile
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.AVATAR_BANNER_HEIGHT
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.AVATAR_BORDER_WIDTH
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.AVATAR_ICON_SIZE
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.AVATAR_SIZE
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.EVENT_CARD_HEIGHT
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.EVENT_CARD_WIDTH
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.GRID_COLUMNS
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.MEMBER_AVATAR_SIZE
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.MEMBER_ICON_SIZE
import com.github.se.studentconnect.ui.profile.OrganizationTab
import com.github.se.studentconnect.ui.utils.loadBitmapFromOrganizationEvent
import com.github.se.studentconnect.ui.utils.loadBitmapFromStringUri
import com.github.se.studentconnect.ui.utils.loadBitmapFromUser

/** Callbacks for organization profile actions. */
data class OrganizationProfileCallbacks(
    val onTabSelected: (OrganizationTab) -> Unit,
    val onFollowClick: () -> Unit,
    val onAddMemberClick: (String) -> Unit,
    val onRemoveMemberClick: (OrganizationMember) -> Unit,
    val onBackClick: () -> Unit,
    val onEventClick: (String) -> Unit = {}
)

// Constants for UI spacing and sizing
private object OrganizationProfileConstants {
  const val SCREEN_HORIZONTAL_PADDING = 24
  const val SCREEN_TOP_PADDING = 16
  const val SCREEN_BOTTOM_PADDING = 32
  const val SECTION_SPACING = 24
  const val BANNER_CORNER_RADIUS = 16
  const val EVENT_CARD_CORNER_RADIUS = 12
  const val FOLLOW_BUTTON_CORNER_RADIUS = 24
  const val HORIZONTAL_PADDING_DESCRIPTION = 16
  const val BUTTON_TOP_PADDING = 8
  const val INFO_SPACING = 16
  const val TAB_INDICATOR_HEIGHT = 2
  const val EVENT_ROW_SPACING = 16
  const val EVENT_ROW_VERTICAL_PADDING = 8
  const val EVENT_CARD_PADDING = 8
  const val EVENT_INFO_PADDING = 12
  const val EVENT_INFO_SPACING = 4
  const val LOCATION_ICON_SIZE = 20
  const val MEMBER_CARD_PADDING = 16
  const val MEMBER_CARD_SPACING = 12
  const val GRID_ITEM_SPACING = 16
  const val PRIMARY_CONTAINER_ALPHA = 0.3f
  const val PRIMARY_GRADIENT_ALPHA = 0.5f
  const val PRIMARY_GRADIENT_END_ALPHA = 0.7f
  const val WHITE_ALPHA = 0.9f

  // Typography constants for description
  const val DESCRIPTION_FONT_SIZE = 16
  const val DESCRIPTION_LINE_HEIGHT = 20
  const val DESCRIPTION_LETTER_SPACING = 0
  const val DESCRIPTION_MAX_LINES = 4
}

/**
 * Organization profile screen displaying organization information, events, and members.
 *
 * @param organizationId The ID of the organization to display (optional for preview/testing)
 * @param onBackClick Callback when the back button is clicked
 * @param onEventClick Callback when an event is clicked (passes event ID)
 * @param viewModel ViewModel for managing screen state
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationProfileScreen(
    organizationId: String? = null,
    onBackClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
    viewModel: OrganizationProfileViewModel = run {
      val context = LocalContext.current
      viewModel { OrganizationProfileViewModel(organizationId, context) }
    },
    modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  android.util.Log.d(
      "OrganizationProfileScreen",
      "Screen Recomposition. State: isLoading=${uiState.isLoading}, hasOrg=${uiState.organization != null}, error=${uiState.error}")

  Scaffold(modifier = modifier.fillMaxSize().testTag(C.Tag.org_profile_screen)) { paddingValues ->
    when {
      uiState.isLoading -> {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
      }
      uiState.error != null -> {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
              Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error)
            }
      }
      uiState.organization != null -> {
        OrganizationProfileContent(
            organization = uiState.organization!!,
            selectedTab = uiState.selectedTab,
            pendingInvitations = uiState.pendingInvitations,
            callbacks =
                OrganizationProfileCallbacks(
                    onTabSelected = { viewModel.selectTab(it) },
                    onFollowClick = { viewModel.onFollowButtonClick() },
                    onAddMemberClick = { role -> viewModel.showAddMemberDialog(role) },
                    onRemoveMemberClick = { member -> viewModel.removeMember(member) },
                    onBackClick = onBackClick,
                    onEventClick = onEventClick),
            modifier = Modifier.padding(paddingValues))
      }
    }

    // Unfollow confirmation dialog
    if (uiState.showUnfollowDialog && uiState.organization != null) {
      UnfollowConfirmationDialog(
          organizationName = uiState.organization!!.name,
          onConfirm = { viewModel.confirmUnfollow() },
          onDismiss = { viewModel.dismissUnfollowDialog() })
    }

    // Add member dialog
    if (uiState.showAddMemberDialog && uiState.selectedRole != null) {
      UserPickerDialog(
          users = uiState.availableUsers,
          role = uiState.selectedRole!!,
          isLoading = uiState.isLoadingUsers,
          onUserSelected = { userId -> viewModel.sendMemberInvitation(userId) },
          onDismiss = { viewModel.dismissAddMemberDialog() })
    }
  }
}

/**
 * Content of the organization profile screen.
 *
 * @param organization The organization data to display
 * @param selectedTab The currently selected tab
 * @param pendingInvitations Map of role -> userId for pending invitations
 * @param callbacks Callbacks for user interactions
 * @param modifier Modifier for the composable
 */
@Composable
private fun OrganizationProfileContent(
    organization: OrganizationProfile,
    selectedTab: OrganizationTab,
    pendingInvitations: Map<String, String>,
    callbacks: OrganizationProfileCallbacks,
    modifier: Modifier = Modifier
) {
  Column(
      modifier =
          modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(
                  start = OrganizationProfileConstants.SCREEN_HORIZONTAL_PADDING.dp,
                  end = OrganizationProfileConstants.SCREEN_HORIZONTAL_PADDING.dp,
                  top = OrganizationProfileConstants.SCREEN_TOP_PADDING.dp,
                  bottom = OrganizationProfileConstants.SCREEN_BOTTOM_PADDING.dp),
      verticalArrangement = Arrangement.spacedBy(OrganizationProfileConstants.SECTION_SPACING.dp)) {
        // Top Bar with back button
        OrganizationTopBar(
            organizationName = organization.name, onBackClick = callbacks.onBackClick)

        // Avatar Banner
        AvatarBanner(logoUrl = organization.logoUrl)

        // Organization Info Block
        OrganizationInfoBlock(organization = organization, onFollowClick = callbacks.onFollowClick)

        // About Section with Tabs
        AboutSection(selectedTab = selectedTab, onTabSelected = callbacks.onTabSelected)

        // Tab Content
        when (selectedTab) {
          OrganizationTab.EVENTS ->
              EventsTab(events = organization.events, onEventClick = callbacks.onEventClick)
          OrganizationTab.MEMBERS ->
              MembersTab(
                  members = organization.members,
                  organizationRoles = organization.roles,
                  isOwner = organization.isOwner,
                  pendingInvitations = pendingInvitations,
                  onAddMemberClick = callbacks.onAddMemberClick,
                  onRemoveMemberClick = callbacks.onRemoveMemberClick)
        }
      }
}

/**
 * Top bar with back button and organization name.
 *
 * @param organizationName The name of the organization
 * @param onBackClick Callback when the back button is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun OrganizationTopBar(
    organizationName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Box(modifier = modifier.fillMaxWidth().testTag(C.Tag.org_profile_header)) {
    IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.TopStart)) {
      Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onSurface)
    }

    Text(
        text = organizationName,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.align(Alignment.Center))
  }
}

/**
 * Avatar banner with circular profile image.
 *
 * @param logoUrl URL to the organization logo (optional)
 * @param modifier Modifier for the composable
 */
@Composable
private fun AvatarBanner(logoUrl: String? = null, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  LaunchedEffect(logoUrl) { imageBitmap = loadBitmapFromStringUri(context, logoUrl) }

  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .height(AVATAR_BANNER_HEIGHT.dp)
              .clip(RoundedCornerShape(OrganizationProfileConstants.BANNER_CORNER_RADIUS.dp))
              .background(
                  MaterialTheme.colorScheme.primaryContainer.copy(
                      alpha = OrganizationProfileConstants.PRIMARY_CONTAINER_ALPHA))
              .testTag(C.Tag.org_profile_avatar_banner),
      contentAlignment = Alignment.Center) {
        // Circular avatar with border
        Box(
            modifier =
                Modifier.size(AVATAR_SIZE.dp)
                    .border(AVATAR_BORDER_WIDTH.dp, Color.White, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .testTag(C.Tag.org_profile_avatar),
            contentAlignment = Alignment.Center) {
              if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "Organization avatar",
                    modifier = Modifier.size(AVATAR_SIZE.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop)
              } else {
                // Person icon placeholder
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Organization avatar",
                    modifier = Modifier.size(AVATAR_ICON_SIZE.dp),
                    tint = MaterialTheme.colorScheme.primary)
              }
            }
      }
}

/**
 * Organization information block with title, description, and follow button.
 *
 * @param organization The organization data to display
 * @param onFollowClick Callback when the follow button is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun OrganizationInfoBlock(
    organization: OrganizationProfile,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier = modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(OrganizationProfileConstants.INFO_SPACING.dp)) {
        // Organization title
        Text(
            text = organization.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(C.Tag.org_profile_title))

        // Description
        Text(
            text = organization.description,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = OrganizationProfileConstants.DESCRIPTION_FONT_SIZE.sp,
                    lineHeight = OrganizationProfileConstants.DESCRIPTION_LINE_HEIGHT.sp,
                    letterSpacing = OrganizationProfileConstants.DESCRIPTION_LETTER_SPACING.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = OrganizationProfileConstants.DESCRIPTION_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(
                        horizontal = OrganizationProfileConstants.HORIZONTAL_PADDING_DESCRIPTION.dp)
                    .testTag(C.Tag.org_profile_description))

        // Follow button with icon and dynamic colors
        Button(
            onClick = onFollowClick,
            enabled = !organization.isMember, // Disable for members
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        when {
                          organization.isMember -> MaterialTheme.colorScheme.secondaryContainer
                          organization.isFollowing -> MaterialTheme.colorScheme.surfaceVariant
                          else -> MaterialTheme.colorScheme.primary
                        },
                    contentColor =
                        when {
                          organization.isMember -> MaterialTheme.colorScheme.onSecondaryContainer
                          organization.isFollowing -> MaterialTheme.colorScheme.onSurfaceVariant
                          else -> MaterialTheme.colorScheme.onPrimary
                        },
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            shape = RoundedCornerShape(OrganizationProfileConstants.FOLLOW_BUTTON_CORNER_RADIUS.dp),
            modifier =
                Modifier.padding(top = OrganizationProfileConstants.BUTTON_TOP_PADDING.dp)
                    .testTag(C.Tag.org_profile_follow_button)) {
              // Icon based on state
              val icon =
                  when {
                    organization.isMember -> Icons.Filled.Check
                    organization.isFollowing -> Icons.Filled.Check
                    else -> Icons.Filled.Add
                  }
              Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                  text =
                      when {
                        organization.isMember -> stringResource(R.string.org_profile_member)
                        organization.isFollowing -> stringResource(R.string.org_profile_following)
                        else -> stringResource(R.string.org_profile_follow)
                      },
                  style = MaterialTheme.typography.labelLarge)
            }
      }
}

/**
 * About section with tabs for Events and Members.
 *
 * @param selectedTab Currently selected tab
 * @param onTabSelected Callback when a tab is selected
 * @param modifier Modifier for the composable
 */
@Composable
private fun AboutSection(
    selectedTab: OrganizationTab,
    onTabSelected: (OrganizationTab) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth().testTag(C.Tag.org_profile_about_section)) {
    // Tab bar
    val selectedIndex = if (selectedTab == OrganizationTab.EVENTS) 0 else 1
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
          TabRowDefaults.Indicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
              color = MaterialTheme.colorScheme.primary,
              height = OrganizationProfileConstants.TAB_INDICATOR_HEIGHT.dp)
        },
        divider = {}) {
          Tab(
              selected = selectedTab == OrganizationTab.EVENTS,
              onClick = { onTabSelected(OrganizationTab.EVENTS) },
              modifier = Modifier.testTag(C.Tag.org_profile_tab_events),
              text = {
                Text(
                    text = stringResource(R.string.org_profile_tab_events),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight =
                        if (selectedTab == OrganizationTab.EVENTS) FontWeight.SemiBold
                        else FontWeight.Normal,
                    color =
                        if (selectedTab == OrganizationTab.EVENTS) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant)
              })

          Tab(
              selected = selectedTab == OrganizationTab.MEMBERS,
              onClick = { onTabSelected(OrganizationTab.MEMBERS) },
              modifier = Modifier.testTag(C.Tag.org_profile_tab_members),
              text = {
                Text(
                    text = stringResource(R.string.org_profile_tab_members),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight =
                        if (selectedTab == OrganizationTab.MEMBERS) FontWeight.SemiBold
                        else FontWeight.Normal,
                    color =
                        if (selectedTab == OrganizationTab.MEMBERS)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant)
              })
        }

    Divider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth())
  }
}

/**
 * Events tab displaying event data.
 *
 * @param events List of events to display
 * @param onEventClick Callback when an event is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun EventsTab(
    events: List<OrganizationEvent>,
    onEventClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf(OrgEventFilter.ALL) }

  val filteredEvents =
      when (selectedFilter) {
        OrgEventFilter.ALL -> events
        OrgEventFilter.UPCOMING -> events.filter { !it.hasEnded() }
        OrgEventFilter.PAST -> events.filter { it.hasEnded() }
      }

  Column(
      modifier = modifier.fillMaxWidth().testTag(C.Tag.org_profile_events_list),
      verticalArrangement =
          Arrangement.spacedBy(OrganizationProfileConstants.EVENT_ROW_SPACING.dp)) {
        // Filter chips
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag(C.Tag.org_profile_event_filters),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OrgEventFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = stringResource(filter.labelResId),
                    modifier =
                        Modifier.testTag(
                            "${C.Tag.org_profile_event_filter_prefix}_${filter.name.lowercase()}"))
              }
            }

        if (filteredEvents.isEmpty()) {
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = 32.dp)
                      .testTag(C.Tag.org_profile_events_empty),
              contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.org_profile_no_events),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
        } else {
          filteredEvents.forEachIndexed { index, event ->
            EventRow(event = event, index = index, onEventClick = onEventClick)
          }
        }
      }
}

/** Organization event filter options */
private enum class OrgEventFilter(val labelResId: Int) {
  ALL(R.string.org_profile_filter_all),
  UPCOMING(R.string.org_profile_filter_upcoming),
  PAST(R.string.org_profile_filter_past)
}

/** Filter chip for event filtering */
@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
  Button(
      onClick = onClick,
      modifier = modifier.height(32.dp),
      colors =
          ButtonDefaults.buttonColors(
              containerColor =
                  if (selected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant,
              contentColor =
                  if (selected) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurfaceVariant),
      shape = RoundedCornerShape(16.dp),
      contentPadding =
          androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 0.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontSize = 12.sp)
      }
}

/**
 * Single event row with card and text information.
 *
 * @param event Event data to display
 * @param index Index of the event in the list
 * @param onEventClick Callback when the event is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun EventRow(
    event: OrganizationEvent,
    index: Int,
    onEventClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
  val hasEnded = event.hasEnded()
  val context = LocalContext.current

  // Load event banner image
  var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  LaunchedEffect(event) { imageBitmap = loadBitmapFromOrganizationEvent(context, event) }

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .clickable { onEventClick(event.eventId) }
              .padding(vertical = OrganizationProfileConstants.EVENT_ROW_VERTICAL_PADDING.dp)
              .testTag("${C.Tag.org_profile_event_row_prefix}_$index"),
      horizontalArrangement =
          Arrangement.spacedBy(OrganizationProfileConstants.EVENT_ROW_SPACING.dp),
      verticalAlignment = Alignment.CenterVertically) {
        EventCard(event = event, imageBitmap = imageBitmap, hasEnded = hasEnded, index = index)
        EventDetails(event = event, hasEnded = hasEnded)
      }
}

/**
 * Event card displaying image or gradient background with event info.
 *
 * @param event Event data to display
 * @param imageBitmap Optional bitmap image for the event banner
 * @param hasEnded Whether the event has ended
 * @param index Index of the event for test tags
 */
@Composable
private fun EventCard(
    event: OrganizationEvent,
    imageBitmap: ImageBitmap?,
    hasEnded: Boolean,
    index: Int
) {
  Box(
      modifier =
          Modifier.width(EVENT_CARD_WIDTH.dp)
              .height(EVENT_CARD_HEIGHT.dp)
              .clip(RoundedCornerShape(OrganizationProfileConstants.EVENT_CARD_CORNER_RADIUS.dp))
              .testTag("${C.Tag.org_profile_event_card_prefix}_$index")) {
        EventCardBackground(imageBitmap = imageBitmap, hasEnded = hasEnded)
        EventCardOverlay()
        EventCardLocationIcon()
        EventCardInfo(event = event)
        if (hasEnded) {
          EventEndedOverlay()
        }
      }
}

/** Background for event card - displays either an image or gradient. */
@Composable
private fun EventCardBackground(imageBitmap: ImageBitmap?, hasEnded: Boolean) {
  if (imageBitmap != null) {
    Image(
        bitmap = imageBitmap,
        contentDescription = "Event banner",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop)
  } else {
    val gradientColors =
        if (hasEnded) {
          listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.5f))
        } else {
          listOf(
              MaterialTheme.colorScheme.primaryContainer.copy(
                  alpha = OrganizationProfileConstants.PRIMARY_GRADIENT_ALPHA),
              MaterialTheme.colorScheme.primary)
        }
    Box(
        modifier =
            Modifier.fillMaxSize().background(Brush.verticalGradient(colors = gradientColors)))
  }
}

/** Gradient overlay for better text readability on event card. */
@Composable
private fun EventCardOverlay() {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(
                  Brush.verticalGradient(
                      colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
}

/** Location icon displayed at the top start of event card. */
@Composable
private fun BoxScope.EventCardLocationIcon() {
  Icon(
      imageVector = Icons.Default.LocationOn,
      contentDescription = "Location",
      tint = Color.White,
      modifier =
          Modifier.padding(OrganizationProfileConstants.EVENT_CARD_PADDING.dp)
              .size(OrganizationProfileConstants.LOCATION_ICON_SIZE.dp)
              .align(Alignment.TopStart))
}

/** Event information displayed at the bottom of event card. */
@Composable
private fun BoxScope.EventCardInfo(event: OrganizationEvent) {
  Column(
      modifier =
          Modifier.align(Alignment.BottomStart)
              .padding(OrganizationProfileConstants.EVENT_INFO_PADDING.dp),
      verticalArrangement =
          Arrangement.spacedBy(OrganizationProfileConstants.EVENT_INFO_SPACING.dp)) {
        Text(
            text = event.cardTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White)

        Text(
            text = event.cardDate,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = OrganizationProfileConstants.WHITE_ALPHA))
      }
}

/** "Event Ended" overlay for past events. */
@Composable
private fun EventEndedOverlay() {
  Box(
      modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
      contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.org_profile_event_ended),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center)
      }
}

/** Event details displayed next to the event card. */
@Composable
private fun EventDetails(event: OrganizationEvent, hasEnded: Boolean) {
  Column(
      verticalArrangement =
          Arrangement.spacedBy(OrganizationProfileConstants.EVENT_INFO_SPACING.dp)) {
        Text(
            text = event.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color =
                if (hasEnded) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurface)

        Text(
            text = event.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (hasEnded) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

/**
 * Members tab displaying role slots (filled or empty) in a 2-column grid.
 *
 * @param members List of members to display
 * @param organizationRoles List of roles defined for this organization
 * @param isOwner Whether the current user is the owner
 * @param pendingInvitations Map of role -> userId for pending invitations
 * @param onAddMemberClick Callback when add member button is clicked
 * @param onRemoveMemberClick Callback when remove member button is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun MembersTab(
    members: List<OrganizationMember>,
    organizationRoles: List<String>,
    isOwner: Boolean,
    pendingInvitations: Map<String, String>,
    onAddMemberClick: (String) -> Unit,
    onRemoveMemberClick: (OrganizationMember) -> Unit = {},
    modifier: Modifier = Modifier
) {
  // Collect all roles: organizationRoles + any roles that members have but aren't in the list
  // This ensures Owner and any other assigned roles always show up
  val allRoles = (organizationRoles + members.map { it.role }).distinct()

  if (allRoles.isEmpty() && members.isEmpty()) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
                .testTag(C.Tag.org_profile_members_empty),
        contentAlignment = Alignment.Center) {
          Text(
              text = stringResource(R.string.org_profile_no_members),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
  } else {
    // Create a map of role -> member (first member with that role)
    val membersByRole = members.groupBy { it.role }.mapValues { it.value.firstOrNull() }

    // Sort roles to show Owner first, then others
    val sortedRoles =
        allRoles.sortedBy {
          when (it) {
            "Owner" -> 0 // Owner first
            else -> 1 // Others after
          }
        }

    // Create role slots: each role gets one slot
    val roleSlots = sortedRoles.map { role -> RoleSlot(role = role, member = membersByRole[role]) }

    Column(
        modifier = modifier.fillMaxWidth().testTag(C.Tag.org_profile_members_grid),
        verticalArrangement =
            Arrangement.spacedBy(OrganizationProfileConstants.GRID_ITEM_SPACING.dp)) {
          roleSlots.chunked(GRID_COLUMNS).forEach { rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(OrganizationProfileConstants.GRID_ITEM_SPACING.dp)) {
                  rowSlots.forEach { roleSlot ->
                    Box(modifier = Modifier.weight(1f)) {
                      RoleSlotCard(
                          state =
                              RoleSlotState(
                                  role = roleSlot.role,
                                  member = roleSlot.member,
                                  index = sortedRoles.indexOf(roleSlot.role),
                                  isOwner = isOwner,
                                  hasPendingInvitation =
                                      pendingInvitations.containsKey(roleSlot.role)),
                          onAddClick = { onAddMemberClick(roleSlot.role) },
                          onRemoveClick = { roleSlot.member?.let { onRemoveMemberClick(it) } })
                    }
                  }
                  // Add spacer if last row has fewer items
                  if (rowSlots.size < GRID_COLUMNS) {
                    repeat(GRID_COLUMNS - rowSlots.size) { Spacer(modifier = Modifier.weight(1f)) }
                  }
                }
          }
        }
  }
}

/** Data class representing a role slot (filled or empty). */
private data class RoleSlot(val role: String, val member: OrganizationMember?)

/** UI state for a role slot card. */
data class RoleSlotState(
    val role: String,
    val member: OrganizationMember?,
    val index: Int,
    val isOwner: Boolean,
    val hasPendingInvitation: Boolean
)

/**
 * Role slot card showing either a filled member or empty placeholder.
 *
 * @param state The state containing role, member, and UI flags
 * @param onAddClick Callback when add button is clicked
 * @param onRemoveClick Callback when remove button is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun RoleSlotCard(
    state: RoleSlotState,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    // Role title with add button or pending indicator
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = state.role,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.testTag("RoleTitle_${state.role}"))

          if (state.isOwner && state.role != "Owner") {
            if (state.hasPendingInvitation) {
              // Show "Request sent" indicator
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  modifier = Modifier.testTag("PendingInvitation_${state.role}")) {
                    Box(
                        modifier =
                            Modifier.size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))) // Green indicator
                    Text(
                        text = "Request sent",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium)
                  }
            } else {
              // Show add button
              IconButton(
                  onClick = onAddClick,
                  modifier = Modifier.size(32.dp).testTag("AddMemberButton_${state.role}")) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add ${state.role}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                  }
            }
          }
        }

    // Show either member card or empty placeholder
    if (state.member != null) {
      MemberCard(
          member = state.member,
          index = state.index,
          isOwner = state.isOwner,
          canRemove = state.role != "Owner",
          onRemoveClick = onRemoveClick)
    } else {
      EmptyRoleCard(role = state.role)
    }
  }
}

/**
 * Empty role card placeholder when no member is assigned.
 *
 * @param role The role name
 * @param modifier Modifier for the composable
 */
@Composable
private fun EmptyRoleCard(role: String, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier.fillMaxWidth().testTag("EmptyRoleCard_$role"),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      border =
          androidx.compose.foundation.BorderStroke(
              1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(OrganizationProfileConstants.MEMBER_CARD_PADDING.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(OrganizationProfileConstants.MEMBER_CARD_SPACING.dp)) {
              // Empty avatar placeholder
              Box(
                  modifier =
                      Modifier.size(MEMBER_AVATAR_SIZE.dp)
                          .clip(CircleShape)
                          .background(MaterialTheme.colorScheme.surfaceVariant),
                  contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "No member assigned",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(MEMBER_ICON_SIZE.dp))
                  }

              // "No one selected" text
              Text(
                  text = "No one selected",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Normal,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                  textAlign = TextAlign.Center)
            }
      }
}

/**
 * Single member card with avatar and information.
 *
 * @param member Member data to display
 * @param index Index of the member in the list
 * @param isOwner Whether current user is owner
 * @param canRemove Whether this member can be removed
 * @param onRemoveClick Callback when remove button is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun MemberCard(
    member: OrganizationMember,
    index: Int,
    isOwner: Boolean = false,
    canRemove: Boolean = false,
    onRemoveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  Card(
      modifier = modifier.fillMaxWidth().testTag("${C.Tag.org_profile_member_card_prefix}_$index"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(OrganizationProfileConstants.MEMBER_CARD_PADDING.dp)
                      .padding(bottom = 8.dp), // Extra bottom padding for role text
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement =
                  Arrangement.spacedBy(OrganizationProfileConstants.MEMBER_CARD_SPACING.dp)) {
                // Avatar with image if available, otherwise gradient background
                MemberAvatar(member = member)

                // Member name
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                // Member role
                Text(
                    text = member.role,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
              }

          // Remove button (only for owner and removable members)
          if (isOwner && canRemove) {
            IconButton(
                onClick = onRemoveClick,
                modifier =
                    Modifier.align(Alignment.TopEnd)
                        .size(32.dp)
                        .testTag("RemoveMemberButton_${member.memberId}")) {
                  Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Remove ${member.name}",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(18.dp))
                }
          }
        }
      }
}

/**
 * Member avatar component that loads the image from Firebase Storage.
 *
 * @param member The member whose avatar to display
 * @param modifier Modifier for the composable
 */
@Composable
private fun MemberAvatar(member: OrganizationMember, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val avatarUrl = member.avatarUrl
  var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  LaunchedEffect(avatarUrl) { imageBitmap = loadBitmapFromStringUri(context, avatarUrl) }

  Box(
      modifier =
          modifier
              .size(MEMBER_AVATAR_SIZE.dp)
              .clip(CircleShape)
              .background(
                  Brush.verticalGradient(
                      colors =
                          listOf(
                              MaterialTheme.colorScheme.primaryContainer,
                              MaterialTheme.colorScheme.primary.copy(
                                  alpha =
                                      OrganizationProfileConstants.PRIMARY_GRADIENT_END_ALPHA)))),
      contentAlignment = Alignment.Center) {
        if (imageBitmap != null) {
          Image(
              bitmap = imageBitmap!!,
              contentDescription = "Member avatar",
              modifier = Modifier.size(MEMBER_AVATAR_SIZE.dp).clip(CircleShape),
              contentScale = ContentScale.Crop)
        } else {
          Icon(
              imageVector = Icons.Outlined.Person,
              contentDescription = "Member avatar placeholder",
              tint = Color.White,
              modifier = Modifier.size(MEMBER_ICON_SIZE.dp))
        }
      }
}

/**
 * Member avatar placeholder with gradient background.
 *
 * @param modifier Modifier for the composable
 */
@Composable
private fun MemberAvatarPlaceholder(modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .size(MEMBER_AVATAR_SIZE.dp)
              .clip(CircleShape)
              .background(
                  Brush.verticalGradient(
                      colors =
                          listOf(
                              MaterialTheme.colorScheme.primaryContainer,
                              MaterialTheme.colorScheme.primary.copy(
                                  alpha =
                                      OrganizationProfileConstants.PRIMARY_GRADIENT_END_ALPHA)))),
      contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "Member avatar placeholder",
            tint = Color.White,
            modifier = Modifier.size(MEMBER_ICON_SIZE.dp))
      }
}

/**
 * Confirmation dialog for unfollowing an organization.
 *
 * @param organizationName The name of the organization to unfollow
 * @param onConfirm Callback when user confirms unfollowing
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
private fun UnfollowConfirmationDialog(
    organizationName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = stringResource(R.string.org_profile_unfollow_title)) },
      text = {
        Text(text = stringResource(R.string.org_profile_unfollow_message, organizationName))
      },
      confirmButton = {
        TextButton(
            onClick = onConfirm,
            colors =
                ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
              Text(stringResource(R.string.org_profile_unfollow_confirm))
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text(stringResource(R.string.org_profile_unfollow_cancel))
        }
      })
}

/**
 * Dialog for selecting a user to invite to the organization.
 *
 * @param users List of available users to invite
 * @param role The role to assign to the selected user
 * @param isLoading Whether the user list is loading
 * @param onUserSelected Callback when a user is selected
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun UserPickerDialog(
    users: List<com.github.se.studentconnect.model.user.User>,
    role: String,
    isLoading: Boolean,
    onUserSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }

  // Filter users based on search query
  val filteredUsers =
      users.filter { user ->
        val query = searchQuery.lowercase()
        user.getFullName().lowercase().contains(query) ||
            user.username.lowercase().contains(query) ||
            user.email.lowercase().contains(query)
      }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Invite $role") },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Search field
          OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              modifier = Modifier.fillMaxWidth().testTag("UserSearchField"),
              placeholder = { Text("Search by name or username") },
              singleLine = true)

          Spacer(modifier = Modifier.height(16.dp))

          // User grid
          Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            if (isLoading) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("UserPickerLoading"))
              }
            } else if (filteredUsers.isEmpty()) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text =
                        if (searchQuery.isEmpty()) "No users available to invite"
                        else "No users found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("NoUsersFound"))
              }
            } else {
              LazyVerticalGrid(
                  columns = GridCells.Fixed(GRID_COLUMNS),
                  horizontalArrangement =
                      Arrangement.spacedBy(OrganizationProfileConstants.GRID_ITEM_SPACING.dp),
                  verticalArrangement =
                      Arrangement.spacedBy(OrganizationProfileConstants.GRID_ITEM_SPACING.dp),
                  modifier = Modifier.testTag("UserPickerList")) {
                    items(filteredUsers) { user ->
                      UserPickerCard(
                          user = user,
                          onClick = {
                            onUserSelected(user.userId)
                            onDismiss()
                          })
                    }
                  }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = onDismiss, modifier = Modifier.testTag("CancelButton")) {
          Text("Cancel")
        }
      })
}

/**
 * Single user card in the picker dialog (similar to MemberCard).
 *
 * @param user The user to display
 * @param onClick Callback when the user is clicked
 */
@Composable
private fun UserPickerCard(
    user: com.github.se.studentconnect.model.user.User,
    onClick: () -> Unit
) {
  Card(
      modifier = Modifier.fillMaxWidth().testTag("UserPickerItem_${user.userId}"),
      onClick = onClick,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(OrganizationProfileConstants.MEMBER_CARD_PADDING.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(OrganizationProfileConstants.MEMBER_CARD_SPACING.dp)) {
              // Avatar with image if available, otherwise gradient background
              UserPickerAvatar(user = user)

              // User name
              Text(
                  text = user.getFullName(),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  textAlign = TextAlign.Center,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis)

              // Username
              Text(
                  text = "@${user.username}",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Normal,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center)
            }
      }
}

/**
 * User avatar component for picker dialog that loads the image from Firebase Storage.
 *
 * @param user The user whose avatar to display
 * @param modifier Modifier for the composable
 */
@Composable
private fun UserPickerAvatar(
    user: com.github.se.studentconnect.model.user.User,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  LaunchedEffect(user) { imageBitmap = loadBitmapFromUser(context, user) }

  Box(
      modifier =
          modifier
              .size(MEMBER_AVATAR_SIZE.dp)
              .clip(CircleShape)
              .background(
                  Brush.verticalGradient(
                      colors =
                          listOf(
                              MaterialTheme.colorScheme.primaryContainer,
                              MaterialTheme.colorScheme.primary.copy(
                                  alpha =
                                      OrganizationProfileConstants.PRIMARY_GRADIENT_END_ALPHA)))),
      contentAlignment = Alignment.Center) {
        if (imageBitmap != null) {
          Image(
              bitmap = imageBitmap!!,
              contentDescription = "User avatar",
              modifier = Modifier.size(MEMBER_AVATAR_SIZE.dp).clip(CircleShape),
              contentScale = ContentScale.Crop)
        } else {
          Icon(
              imageVector = Icons.Outlined.Person,
              contentDescription = "User avatar placeholder",
              tint = Color.White,
              modifier = Modifier.size(MEMBER_ICON_SIZE.dp))
        }
      }
}

/**
 * User avatar placeholder with gradient background (for picker dialog).
 *
 * @param modifier Modifier for the composable
 */
@Composable
private fun UserAvatarPlaceholder(modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .size(MEMBER_AVATAR_SIZE.dp)
              .clip(CircleShape)
              .background(
                  Brush.verticalGradient(
                      colors =
                          listOf(
                              MaterialTheme.colorScheme.primaryContainer,
                              MaterialTheme.colorScheme.primary.copy(
                                  alpha =
                                      OrganizationProfileConstants.PRIMARY_GRADIENT_END_ALPHA)))),
      contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "User avatar placeholder",
            tint = Color.White,
            modifier = Modifier.size(MEMBER_ICON_SIZE.dp))
      }
}
