package com.github.se.studentconnect.ui.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers

/** Data class holding profile statistics */
data class ProfileStats(val friendsCount: Int, val eventsCount: Int)

/** Data class holding profile action callbacks */
data class ProfileActions(
    val onEditClick: (() -> Unit)? = null,
    val onUserCardClick: (() -> Unit)? = null,
    val onOrganizationClick: (() -> Unit)? = null
)

/** Data class holding all profile header callbacks */
data class ProfileHeaderCallbacks(
    val onFriendsClick: () -> Unit,
    val onEventsClick: () -> Unit,
    val onEditClick: (() -> Unit)? = null,
    val onUserCardClick: (() -> Unit)? = null,
    val onOrganizationClick: (() -> Unit)? = null,
    val onLogoutClick: (() -> Unit)? = null
)

/**
 * Profile header component showing user profile picture, stats, and user information.
 *
 * @param user The user whose profile is being displayed
 * @param stats Profile statistics (friends count and events count)
 * @param callbacks All callback functions grouped together
 * @param isVisitorMode Whether this is a visitor profile (shows friend buttons instead of
 *   edit/card)
 * @param friendButtonsContent Optional composable for friend action buttons in visitor mode
 * @param showUsername Whether to show the username below the name
 * @param userOrganizations List of organizations the user is a member of
 * @param modifier Modifier for the composable
 */
@Composable
fun ProfileHeader(
    user: User,
    stats: ProfileStats,
    callbacks: ProfileHeaderCallbacks,
    isVisitorMode: Boolean = false,
    friendButtonsContent: (@Composable () -> Unit)? = null,
    showUsername: Boolean = false,
    userOrganizations: List<Organization> = emptyList(),
    modifier: Modifier = Modifier
) {
  // Create actions from the callbacks for backward compatibility
  val actions =
      ProfileActions(
          onEditClick = callbacks.onEditClick,
          onUserCardClick = callbacks.onUserCardClick,
          onOrganizationClick = callbacks.onOrganizationClick)
  val showDialog: MutableState<Boolean> = remember { mutableStateOf(false) }
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val profileId = user.profilePictureUrl
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, profileId, repository) {
        value =
            profileId?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure {
                    android.util.Log.e("ProfileHeader", "Failed to download profile image: $id", it)
                  }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

  Column(
      modifier = modifier.fillMaxWidth().padding(dimensionResource(R.dimen.profile_header_padding)),
      horizontalAlignment = Alignment.Start) {
        // Top Row: Profile Picture + Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              ProfilePicture(
                  imageBitmap = imageBitmap, organization = userOrganizations.firstOrNull())

              Spacer(modifier = Modifier.width(dimensionResource(R.dimen.profile_spacing_xxlarge)))

              // Stats Row (Friends and Events)
              Row(
                  modifier = Modifier.weight(1f),
                  horizontalArrangement = Arrangement.SpaceEvenly,
                  verticalAlignment = Alignment.CenterVertically) {
                    StatItem(
                        count = stats.friendsCount,
                        label = stringResource(R.string.label_friends),
                        onClick = callbacks.onFriendsClick)

                    StatItem(
                        count = stats.eventsCount,
                        label = stringResource(R.string.label_events),
                        onClick = callbacks.onEventsClick)
                  }
            }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_xlarge)))

        UserInformation(
            user = user,
            showUsername = showUsername,
            isVisitorMode = isVisitorMode,
            onLogoutClick = callbacks.onLogoutClick,
            showDialog = showDialog)

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_xlarge)))

        // Show either visitor mode buttons or action buttons
        if (isVisitorMode && friendButtonsContent != null) {
          friendButtonsContent()
        } else {
          ActionButtons(actions = actions)
        }
      }
}

/**
 * Profile picture component with optional organization badge.
 *
 * @param imageBitmap The profile image bitmap, or null for placeholder
 * @param organization The user's organization to display as a badge (null if not a member)
 * @param modifier Modifier for the composable
 */
@Composable
private fun ProfilePicture(
    imageBitmap: ImageBitmap?,
    organization: Organization? = null,
    modifier: Modifier = Modifier
) {
  Box(modifier = modifier.size(dimensionResource(R.dimen.profile_picture_size))) {
    // Main profile picture
    Box(
        modifier =
            Modifier.size(dimensionResource(R.dimen.profile_picture_size))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .border(width = 0.dp, color = Color.Transparent, shape = CircleShape),
        contentAlignment = Alignment.Center) {
          val profilePictureDescription =
              stringResource(R.string.content_description_profile_picture)
          if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = profilePictureDescription,
                modifier =
                    Modifier.size(dimensionResource(R.dimen.profile_picture_size))
                        .clip(CircleShape),
                contentScale = ContentScale.Crop)
          } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = profilePictureDescription,
                modifier = Modifier.size(dimensionResource(R.dimen.profile_picture_icon_size)),
                tint = MaterialTheme.colorScheme.primary)
          }
        }

    // Organization badge overlay
    if (organization != null) {
      OrganizationBadge(
          organization = organization,
          modifier =
              Modifier.align(Alignment.TopEnd)
                  .offset(
                      x = dimensionResource(R.dimen.profile_badge_offset),
                      y = -dimensionResource(R.dimen.profile_badge_offset)))
    }
  }
}

/**
 * Organization badge overlay showing membership with organization name in curved text.
 *
 * @param organization The organization to display
 * @param modifier Modifier for the composable
 */
@Composable
private fun OrganizationBadge(organization: Organization, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val density = LocalDensity.current

  // Load organization logo if available
  val logoUrl = organization.logoUrl
  val logoBitmap by
      produceState<ImageBitmap?>(initialValue = null, logoUrl, repository) {
        value =
            logoUrl?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure {
                    android.util.Log.e("OrganizationBadge", "Failed to download org logo: $id", it)
                  }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

  val textColor = MaterialTheme.colorScheme.onSurface
  val backgroundColor = MaterialTheme.colorScheme.surface
  val textSizePx = with(density) { 8.sp.toPx() }

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    // Curved text above the badge
    val orgName = organization.name

    Box(
        modifier =
            Modifier.size(60.dp).drawBehind {
              val radius = 25.dp.toPx()
              val centerX = size.width / 2f
              val centerY = size.height / 2f + 10.dp.toPx()

              // Calculate arc for text placement
              val charCount = orgName.length
              val arcAngle = 120f // Total arc angle in degrees
              val startAngle = 240f // Start from top-left

              val paint =
                  android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    this.textSize = textSizePx
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface =
                        android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                  }

              // Draw background arc for text
              val backgroundPaint =
                  android.graphics.Paint().apply {
                    color = backgroundColor.copy(alpha = 0.85f).hashCode()
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.FILL
                  }

              val canvas = drawContext.canvas.nativeCanvas

              // Draw background arc behind all text
              val arcPath = android.graphics.Path()
              val arcRect =
                  android.graphics.RectF(
                      centerX - radius - textSizePx,
                      centerY - radius - textSizePx,
                      centerX + radius + textSizePx,
                      centerY + radius + textSizePx)
              arcPath.addArc(arcRect, startAngle, arcAngle)

              val strokePaint =
                  android.graphics.Paint().apply {
                    color = backgroundColor.copy(alpha = 0.9f).hashCode()
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = textSizePx * 1.8f
                    strokeCap = android.graphics.Paint.Cap.ROUND
                  }
              canvas.drawPath(arcPath, strokePaint)

              // Draw each character along the arc
              orgName.forEachIndexed { index, char ->
                val angle = startAngle + (index * arcAngle / (charCount - 1).coerceAtLeast(1))
                val angleRad = Math.toRadians(angle.toDouble())

                val x = centerX + (radius * cos(angleRad)).toFloat()
                val y = centerY + (radius * sin(angleRad)).toFloat()

                // Rotate and draw character
                canvas.save()
                canvas.rotate(angle + 90f, x, y)
                canvas.drawText(char.toString(), x, y + textSizePx / 3, paint)
                canvas.restore()
              }
            })

    // Badge circle with logo or star - positioned at center
    Box(
        modifier =
            Modifier.align(Alignment.Center)
                .offset(y = 10.dp)
                .size(dimensionResource(R.dimen.profile_badge_size))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    width = dimensionResource(R.dimen.profile_badge_border_width),
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape),
        contentAlignment = Alignment.Center) {
          val bitmap = logoBitmap
          if (bitmap != null) {
            // Show organization logo
            Image(
                bitmap = bitmap,
                contentDescription = organization.name,
                modifier =
                    Modifier.size(dimensionResource(R.dimen.profile_badge_size)).clip(CircleShape),
                contentScale = ContentScale.Crop)
          } else {
            // Show star icon as fallback
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = organization.name,
                modifier = Modifier.size(dimensionResource(R.dimen.profile_badge_icon_size)),
                tint = MaterialTheme.colorScheme.primary)
          }
        }
  }
}

/**
 * User information section displaying name, bio, university, and location.
 *
 * @param user The user whose information to display
 * @param showUsername Whether to show the username below the name
 * @param isVisitorMode Whether this is a visitor profile
 * @param onLogoutClick Callback for logout action (null in visitor mode)
 * @param showDialog State for showing logout confirmation dialog
 * @param modifier Modifier for the composable
 */
@Composable
private fun UserInformation(
    user: User,
    showUsername: Boolean = false,
    isVisitorMode: Boolean = false,
    onLogoutClick: (() -> Unit)? = null,
    showDialog: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    UserNameHeader(
        user = user,
        isVisitorMode = isVisitorMode,
        onLogoutClick = onLogoutClick,
        showDialog = showDialog)

    UsernameSection(user = user, showUsername = showUsername)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))

    BioSection(user = user)

    UniversitySection(user = user)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))

    LocationSection(user = user)
  }
}

/**
 * User name header with optional logout button.
 *
 * @param user The user whose name to display
 * @param isVisitorMode Whether this is a visitor profile
 * @param onLogoutClick Callback for logout action (null in visitor mode)
 * @param showDialog State for showing logout confirmation dialog
 */
@Composable
private fun UserNameHeader(
    user: User,
    isVisitorMode: Boolean,
    onLogoutClick: (() -> Unit)?,
    showDialog: MutableState<Boolean>
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        // User Name
        Text(
            text = user.getFullName(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = dimensionResource(R.dimen.profile_name_text_size).value.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                if (isVisitorMode)
                    Modifier.testTag(
                        com.github.se.studentconnect.resources.C.Tag.visitor_profile_user_name)
                else Modifier)

        LogoutButton(
            isVisitorMode = isVisitorMode, onLogoutClick = onLogoutClick, showDialog = showDialog)
      }
}

/**
 * Logout button component.
 *
 * @param isVisitorMode Whether this is a visitor profile
 * @param onLogoutClick Callback for logout action (null in visitor mode)
 * @param showDialog State for showing logout confirmation dialog
 */
@Composable
private fun LogoutButton(
    isVisitorMode: Boolean,
    onLogoutClick: (() -> Unit)?,
    showDialog: MutableState<Boolean>
) {
  if (!isVisitorMode && onLogoutClick != null) {
    IconButton(
        onClick = { showDialog.value = true },
        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent),
        modifier = Modifier.size(dimensionResource(R.dimen.profile_button_icon_size))) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = stringResource(R.string.content_description_logout))
        }
    if (showDialog.value) {
      LogoutDialog(showDialog = showDialog, logOut = onLogoutClick)
    }
  }
}

/**
 * Username section component.
 *
 * @param user The user whose username to display
 * @param showUsername Whether to show the username
 */
@Composable
private fun UsernameSection(user: User, showUsername: Boolean) {
  if (showUsername) {
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
    Text(
        text = "@${user.username}",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = dimensionResource(R.dimen.profile_body_text_size).value.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

/**
 * Bio section component.
 *
 * @param user The user whose bio to display
 */
@Composable
private fun BioSection(user: User) {
  if (user.hasBio()) {
    Text(
        text = user.bio ?: "",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = dimensionResource(R.dimen.profile_body_text_size).value.sp,
        color = MaterialTheme.colorScheme.onSurface)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
  }
}

/**
 * University section component.
 *
 * @param user The user whose university to display
 */
@Composable
private fun UniversitySection(user: User) {
  Text(
      text = user.university,
      style = MaterialTheme.typography.bodyMedium,
      fontSize = dimensionResource(R.dimen.profile_body_text_size).value.sp,
      color = MaterialTheme.colorScheme.onSurface)
}

/**
 * Location section component.
 *
 * @param user The user whose location to display
 */
@Composable
private fun LocationSection(user: User) {
  if (user.country != null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = stringResource(R.string.content_description_location),
          modifier = Modifier.size(dimensionResource(R.dimen.profile_location_icon_size)),
          tint = MaterialTheme.colorScheme.onSurface)

      Spacer(modifier = Modifier.width(dimensionResource(R.dimen.profile_spacing_small)))

      Text(
          text = user.country,
          style = MaterialTheme.typography.bodyMedium,
          fontSize = dimensionResource(R.dimen.profile_body_text_size).value.sp,
          color = MaterialTheme.colorScheme.onSurface)
    }
  }
}

/**
 * Action buttons section (Edit, User Card, Organizations).
 *
 * @param actions Profile action callbacks
 * @param modifier Modifier for the composable
 */
@Composable
private fun ActionButtons(actions: ProfileActions, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth()) {
    // Buttons Row: Edit and User Card
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(dimensionResource(R.dimen.profile_spacing_large))) {
          // Edit Button
          if (actions.onEditClick != null) {
            Button(
                onClick = actions.onEditClick,
                modifier =
                    Modifier.weight(1f).height(dimensionResource(R.dimen.profile_button_height)),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary),
                shape =
                    RoundedCornerShape(dimensionResource(R.dimen.profile_button_corner_radius))) {
                  Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = stringResource(R.string.content_description_edit),
                      modifier = Modifier.size(dimensionResource(R.dimen.profile_button_icon_size)))
                  Spacer(
                      modifier = Modifier.width(dimensionResource(R.dimen.profile_spacing_medium)))
                  Text(
                      text = stringResource(R.string.button_edit),
                      fontSize = dimensionResource(R.dimen.profile_button_text_size).value.sp,
                      fontWeight = FontWeight.Medium)
                }
          }

          // User Card Button
          if (actions.onUserCardClick != null) {
            Button(
                onClick = actions.onUserCardClick,
                modifier =
                    Modifier.weight(1f).height(dimensionResource(R.dimen.profile_button_height)),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary),
                shape =
                    RoundedCornerShape(dimensionResource(R.dimen.profile_button_corner_radius))) {
                  Icon(
                      imageVector = Icons.Default.CreditCard,
                      contentDescription = stringResource(R.string.content_description_user_card),
                      modifier = Modifier.size(dimensionResource(R.dimen.profile_button_icon_size)))
                  Spacer(
                      modifier = Modifier.width(dimensionResource(R.dimen.profile_spacing_medium)))
                  Text(
                      text = stringResource(R.string.button_card),
                      fontSize = dimensionResource(R.dimen.profile_button_text_size).value.sp,
                      fontWeight = FontWeight.Medium)
                }
          }
        }

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_large)))

    // Organization Button
    if (actions.onOrganizationClick != null) {
      Button(
          onClick = actions.onOrganizationClick,
          modifier =
              Modifier.fillMaxWidth().height(dimensionResource(R.dimen.profile_button_height)),
          colors =
              ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.secondary,
                  contentColor = MaterialTheme.colorScheme.onSecondary),
          shape = RoundedCornerShape(dimensionResource(R.dimen.profile_button_corner_radius))) {
            Icon(
                imageVector = Icons.Outlined.Groups,
                contentDescription = stringResource(R.string.content_description_organizations),
                modifier = Modifier.size(dimensionResource(R.dimen.profile_button_icon_size)))
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.profile_spacing_medium)))
            Text(
                text = stringResource(R.string.button_organizations),
                fontSize = dimensionResource(R.dimen.profile_button_text_size).value.sp,
                fontWeight = FontWeight.Medium)
          }
    }
  }
}

/**
 * A single stat item showing count and label.
 *
 * @param count The numeric count to display
 * @param label The label for the stat
 * @param onClick Callback when the stat is clicked
 * @param modifier Modifier for the composable
 */
@Composable
private fun StatItem(
    count: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier =
          modifier
              .clickable(onClick = onClick)
              .padding(dimensionResource(R.dimen.profile_stat_padding)),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = dimensionResource(R.dimen.profile_stat_text_size).value.sp,
            color = MaterialTheme.colorScheme.onSurface)

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = dimensionResource(R.dimen.profile_body_text_size).value.sp,
            color = MaterialTheme.colorScheme.onSurface)
      }
}

/**
 * Logout confirmation dialog.
 *
 * @param showDialog State controlling dialog visibility
 * @param logOut Callback to execute logout
 */
@Composable
private fun LogoutDialog(showDialog: MutableState<Boolean>, logOut: () -> Unit) {
  val buttonWidth = dimensionResource(R.dimen.profile_dialog_button_width)
  Dialog(onDismissRequest = { showDialog.value = false }) {
    Box(
        modifier =
            Modifier.background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape =
                        RoundedCornerShape(dimensionResource(R.dimen.profile_button_corner_radius)))
                .padding(dimensionResource(R.dimen.profile_spacing_large))) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.text_logout_popup),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_medium)))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()) {
                  Button(onClick = logOut, modifier = Modifier.width(buttonWidth)) {
                    Text(stringResource(R.string.button_yes))
                  }
                  Spacer(
                      modifier = Modifier.width(dimensionResource(R.dimen.profile_spacing_large)))
                  Button(
                      onClick = { showDialog.value = false },
                      modifier = Modifier.width(buttonWidth)) {
                        Text(stringResource(R.string.button_no))
                      }
                }
          }
        }
  }
}
