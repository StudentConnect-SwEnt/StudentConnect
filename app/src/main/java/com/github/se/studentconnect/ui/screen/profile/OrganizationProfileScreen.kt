package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.OrganizationEvent
import com.github.se.studentconnect.model.OrganizationMember
import com.github.se.studentconnect.model.OrganizationProfile
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.AVATAR_BANNER_HEIGHT
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.AVATAR_BORDER_WIDTH
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.AVATAR_ICON_SIZE
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.AVATAR_SIZE
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.EVENT_CARD_HEIGHT
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.EVENT_CARD_WIDTH
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.GRID_COLUMNS
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.MEMBERS_GRID_HEIGHT
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.MEMBER_AVATAR_SIZE
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel.Companion.MEMBER_ICON_SIZE
import com.github.se.studentconnect.ui.profile.OrganizationTab

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
 * @param viewModel ViewModel for managing screen state
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationProfileScreen(
    organizationId: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: OrganizationProfileViewModel = run {
      val context = LocalContext.current
      viewModel { OrganizationProfileViewModel(organizationId, context) }
    },
    modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

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
            onTabSelected = { viewModel.selectTab(it) },
            onFollowClick = { viewModel.toggleFollow() },
            onBackClick = onBackClick,
            modifier = Modifier.padding(paddingValues))
      }
    }
  }
}

/**
 * Content of the organization profile screen.
 *
 * @param organization The organization data to display
 * @param selectedTab The currently selected tab
 * @param onTabSelected Callback when a tab is selected
 * @param onFollowClick Callback when the follow button is clicked
 * @param onBackClick Callback when the back button is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun OrganizationProfileContent(
    organization: OrganizationProfile,
    selectedTab: OrganizationTab,
    onTabSelected: (OrganizationTab) -> Unit,
    onFollowClick: () -> Unit,
    onBackClick: () -> Unit,
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
        OrganizationTopBar(organizationName = organization.name, onBackClick = onBackClick)

        // Avatar Banner
        AvatarBanner()

        // Organization Info Block
        OrganizationInfoBlock(organization = organization, onFollowClick = onFollowClick)

        // About Section with Tabs
        AboutSection(selectedTab = selectedTab, onTabSelected = onTabSelected)

        // Tab Content
        when (selectedTab) {
          OrganizationTab.EVENTS -> EventsTab(events = organization.events)
          OrganizationTab.MEMBERS -> MembersTab(members = organization.members)
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
 * Avatar banner with circular profile image placeholder.
 *
 * @param modifier Modifier for the composable
 */
@Composable
private fun AvatarBanner(modifier: Modifier = Modifier) {
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
              // Person icon
              Icon(
                  imageVector = Icons.Outlined.Person,
                  contentDescription = "Organization avatar",
                  modifier = Modifier.size(AVATAR_ICON_SIZE.dp),
                  tint = MaterialTheme.colorScheme.primary)
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

        // Follow button
        Button(
            onClick = onFollowClick,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(OrganizationProfileConstants.FOLLOW_BUTTON_CORNER_RADIUS.dp),
            modifier =
                Modifier.padding(top = OrganizationProfileConstants.BUTTON_TOP_PADDING.dp)
                    .testTag(C.Tag.org_profile_follow_button)) {
              Text(
                  text =
                      if (organization.isFollowing) stringResource(R.string.org_profile_following)
                      else stringResource(R.string.org_profile_follow),
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
 * @param modifier Modifier for the composable
 */
@Composable
private fun EventsTab(events: List<OrganizationEvent>, modifier: Modifier = Modifier) {
  Column(
      modifier = modifier.fillMaxWidth().testTag(C.Tag.org_profile_events_list),
      verticalArrangement =
          Arrangement.spacedBy(OrganizationProfileConstants.EVENT_ROW_SPACING.dp)) {
        if (events.isEmpty()) {
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
          events.forEachIndexed { index, event -> EventRow(event = event, index = index) }
        }
      }
}

/**
 * Single event row with card and text information.
 *
 * @param event Event data to display
 * @param index Index of the event in the list
 * @param modifier Modifier for the composable
 */
@Composable
private fun EventRow(event: OrganizationEvent, index: Int, modifier: Modifier = Modifier) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = OrganizationProfileConstants.EVENT_ROW_VERTICAL_PADDING.dp)
              .testTag("${C.Tag.org_profile_event_row_prefix}_$index"),
      horizontalArrangement =
          Arrangement.spacedBy(OrganizationProfileConstants.EVENT_ROW_SPACING.dp),
      verticalAlignment = Alignment.CenterVertically) {
        // Event card with gradient
        Box(
            modifier =
                Modifier.width(EVENT_CARD_WIDTH.dp)
                    .height(EVENT_CARD_HEIGHT.dp)
                    .clip(
                        RoundedCornerShape(
                            OrganizationProfileConstants.EVENT_CARD_CORNER_RADIUS.dp))
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha =
                                            OrganizationProfileConstants.PRIMARY_GRADIENT_ALPHA),
                                    MaterialTheme.colorScheme.primary)))
                    .testTag("${C.Tag.org_profile_event_card_prefix}_$index")) {
              // Location icon
              Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = "Location",
                  tint = Color.White,
                  modifier =
                      Modifier.padding(OrganizationProfileConstants.EVENT_CARD_PADDING.dp)
                          .size(OrganizationProfileConstants.LOCATION_ICON_SIZE.dp)
                          .align(Alignment.TopStart))

              // Event info in card
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

        // Event details
        Column(
            verticalArrangement =
                Arrangement.spacedBy(OrganizationProfileConstants.EVENT_INFO_SPACING.dp)) {
              Text(
                  text = event.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface)

              Text(
                  text = event.subtitle,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
      }
}

/**
 * Members tab displaying member data in a grid.
 *
 * @param members List of members to display
 * @param modifier Modifier for the composable
 */
@Composable
private fun MembersTab(members: List<OrganizationMember>, modifier: Modifier = Modifier) {
  if (members.isEmpty()) {
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier =
            modifier
                .fillMaxWidth()
                .height(MEMBERS_GRID_HEIGHT.dp)
                .testTag(C.Tag.org_profile_members_grid),
        horizontalArrangement =
            Arrangement.spacedBy(OrganizationProfileConstants.GRID_ITEM_SPACING.dp),
        verticalArrangement =
            Arrangement.spacedBy(OrganizationProfileConstants.GRID_ITEM_SPACING.dp)) {
          items(members) { member -> MemberCard(member = member, index = members.indexOf(member)) }
        }
  }
}

/**
 * Single member card with avatar and information.
 *
 * @param member Member data to display
 * @param index Index of the member in the list
 * @param modifier Modifier for the composable
 */
@Composable
private fun MemberCard(member: OrganizationMember, index: Int, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier.fillMaxWidth().testTag("${C.Tag.org_profile_member_card_prefix}_$index"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(OrganizationProfileConstants.MEMBER_CARD_PADDING.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(OrganizationProfileConstants.MEMBER_CARD_SPACING.dp)) {
              // Avatar with image if available, otherwise gradient background
              if (member.avatarUrl != null) {
                val drawableId = getDrawableIdFromName(member.avatarUrl)
                if (drawableId != null) {
                  Image(
                      painter = painterResource(id = drawableId),
                      contentDescription = "Member avatar",
                      modifier = Modifier.size(MEMBER_AVATAR_SIZE.dp).clip(CircleShape),
                      contentScale = ContentScale.Crop)
                } else {
                  MemberAvatarPlaceholder()
                }
              } else {
                MemberAvatarPlaceholder()
              }

              // Member name
              Text(
                  text = member.name,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  textAlign = TextAlign.Center)

              // Member role
              Text(
                  text = member.role,
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Normal,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center)
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
 * Helper function to get drawable resource ID from name.
 *
 * @param name The name of the drawable resource
 * @return The drawable resource ID, or null if not found
 */
private fun getDrawableIdFromName(name: String): Int? {
  return when (name) {
    "avatar_12" -> R.drawable.avatar_12
    "avatar_13" -> R.drawable.avatar_13
    "avatar_23" -> R.drawable.avatar_23
    else -> null
  }
}
