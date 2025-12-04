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
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
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
              ProfilePicture(imageBitmap = imageBitmap)

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

        UserInformation(user = user, showUsername = showUsername, isVisitorMode = isVisitorMode)

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_xlarge)))

        // Show either visitor mode buttons or action buttons
        if (isVisitorMode && friendButtonsContent != null) {
          friendButtonsContent()
        } else {
          ActionButtons(
              actions = actions,
              onLogoutClick = callbacks.onLogoutClick,
              showDialog = showDialog)
        }
      }
}

/**
 * Profile picture component.
 *
 * @param imageBitmap The profile image bitmap, or null for placeholder
 * @param modifier Modifier for the composable
 */
@Composable
private fun ProfilePicture(imageBitmap: ImageBitmap?, modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .size(dimensionResource(R.dimen.profile_picture_size))
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.secondaryContainer)
              .border(width = 0.dp, color = Color.Transparent, shape = CircleShape),
      contentAlignment = Alignment.Center) {
        val profilePictureDescription = stringResource(R.string.content_description_profile_picture)
        if (imageBitmap != null) {
          Image(
              bitmap = imageBitmap,
              contentDescription = profilePictureDescription,
              modifier =
                  Modifier.size(dimensionResource(R.dimen.profile_picture_size)).clip(CircleShape),
              contentScale = ContentScale.Crop)
        } else {
          Icon(
              imageVector = Icons.Default.Person,
              contentDescription = profilePictureDescription,
              modifier = Modifier.size(dimensionResource(R.dimen.profile_picture_icon_size)),
              tint = MaterialTheme.colorScheme.primary)
        }
      }
}

/**
 * User information section displaying name, bio, university, and location.
 *
 * @param user The user whose information to display
 * @param showUsername Whether to show the username below the name
 * @param isVisitorMode Whether this is a visitor profile
 * @param modifier Modifier for the composable
 */
@Composable
private fun UserInformation(
    user: User,
    showUsername: Boolean = false,
    isVisitorMode: Boolean = false,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
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

    // Username (if showUsername is true)
    if (showUsername) {
      Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
      Text(
          text = "@${user.username}",
          style = MaterialTheme.typography.bodyMedium,
          fontSize = dimensionResource(R.dimen.profile_body_text_size).value.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))

    // Bio (if available)
    if (user.hasBio()) {
      Text(
          text = user.bio ?: "",
          style = MaterialTheme.typography.bodyMedium,
          fontSize = dimensionResource(R.dimen.profile_body_text_size).value.sp,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.fillMaxWidth())

      Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))
    }

    // University
    Text(
        text = user.university,
        style = MaterialTheme.typography.bodyMedium,
        fontSize = dimensionResource(R.dimen.profile_body_text_size).value.sp,
        color = MaterialTheme.colorScheme.onSurface)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_small)))

    // Location (Country)
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
}

/**
 * Action buttons section (Edit, User Card, Organizations, Logout).
 *
 * @param actions Profile action callbacks
 * @param onLogoutClick Callback for logout action
 * @param showDialog State for showing logout confirmation dialog
 * @param modifier Modifier for the composable
 */
@Composable
private fun ActionButtons(
    actions: ProfileActions,
    onLogoutClick: (() -> Unit)?,
    showDialog: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
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
<<<<<<< HEAD
    // Logout Button
    if (onLogoutClick != null) {
      Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_spacing_large)))
      Button(
          onClick = { showDialog.value = true },
          modifier =
              Modifier.fillMaxWidth().height(dimensionResource(R.dimen.profile_button_height)),
          colors =
              ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.error,
                  contentColor = MaterialTheme.colorScheme.onError),
          shape = RoundedCornerShape(dimensionResource(R.dimen.profile_button_corner_radius))) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = stringResource(R.string.content_description_logout),
                modifier = Modifier.size(dimensionResource(R.dimen.profile_button_icon_size)))
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.profile_spacing_medium)))
            Text(
                text = stringResource(R.string.button_logout),
                fontSize = dimensionResource(R.dimen.profile_button_text_size).value.sp,
                fontWeight = FontWeight.Medium)
          }
      if (showDialog.value) {
        LogoutDialog(showDialog = showDialog, logOut = onLogoutClick)
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
                    shape = RoundedCornerShape(dimensionResource(R.dimen.profile_button_corner_radius)))
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
                  Button(
                      onClick = { showDialog.value = false }, modifier = Modifier.width(buttonWidth)) {
                        Text(stringResource(R.string.button_no))
                      }
                }
          }
        }
  }
}
