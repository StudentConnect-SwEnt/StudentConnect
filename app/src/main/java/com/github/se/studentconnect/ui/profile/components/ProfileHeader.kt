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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import kotlinx.coroutines.Dispatchers

/** Data class holding profile statistics */
data class ProfileStats(val friendsCount: Int, val eventsCount: Int)

/**
 * Profile header component showing user profile picture, stats, and user information.
 *
 * @param user The user whose profile is being displayed
 * @param stats Profile statistics (friends count and events count)
 * @param onFriendsClick Callback when friends count is clicked
 * @param onEventsClick Callback when events count is clicked
 * @param onEditClick Callback when edit button is clicked
 * @param onUserCardClick Callback when user card button is clicked
 * @param onOrganizationClick Callback when organization button is clicked
 * @param modifier Modifier for the composable
 */
@Composable
fun ProfileHeader(
    user: User,
    stats: ProfileStats,
    onFriendsClick: () -> Unit,
    onEventsClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onUserCardClick: (() -> Unit)? = null,
    onOrganizationClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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

  Column(modifier = modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.Start) {
    // Top Row: Profile Picture + Stats
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          // Profile Picture
          Box(
              modifier =
                  Modifier.size(100.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.secondaryContainer)
                      .border(width = 0.dp, color = Color.Transparent, shape = CircleShape),
              contentAlignment = Alignment.Center) {
                val profilePictureDescription =
                    stringResource(R.string.content_description_profile_picture)
                if (imageBitmap != null) {
                  Image(
                      bitmap = imageBitmap!!,
                      contentDescription = profilePictureDescription,
                      modifier = Modifier.size(100.dp).clip(CircleShape),
                      contentScale = ContentScale.Crop)
                } else {
                  Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription = profilePictureDescription,
                      modifier = Modifier.size(60.dp),
                      tint = MaterialTheme.colorScheme.primary)
                }
              }

          Spacer(modifier = Modifier.width(32.dp))

          // Stats Row (Friends and Events)
          Row(
              modifier = Modifier.weight(1f),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically) {
                // Friends Count
                StatItem(count = stats.friendsCount, label = "Friends", onClick = onFriendsClick)

                // Events Count
                StatItem(count = stats.eventsCount, label = "Events", onClick = onEventsClick)
              }
        }

    Spacer(modifier = Modifier.height(16.dp))

    // User Name
    Text(
        text = user.getFullName(),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSurface)

    Spacer(modifier = Modifier.height(4.dp))

    // Bio (if available)
    if (user.hasBio()) {
      Text(
          text = user.bio ?: "",
          style = MaterialTheme.typography.bodyMedium,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.fillMaxWidth())

      Spacer(modifier = Modifier.height(4.dp))
    }

    // University
    Text(
        text = user.university,
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface)

    Spacer(modifier = Modifier.height(4.dp))

    // Location (Country)
    if (user.country != null) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = user.country,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Buttons Row: Edit and User Card
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      // Edit Button
      if (onEditClick != null) {
        Button(
            onClick = onEditClick,
            modifier = Modifier.weight(1f).height(48.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(24.dp)) {
              Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit",
                  modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = "Edit", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
      }

      // User Card Button
      if (onUserCardClick != null) {
        Button(
            onClick = onUserCardClick,
            modifier = Modifier.weight(1f).height(48.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(24.dp)) {
              Icon(
                  imageVector = Icons.Default.CreditCard,
                  contentDescription = "User Card",
                  modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = "Card", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Organization Button
    if (onOrganizationClick != null) {
      Button(
          onClick = onOrganizationClick,
          modifier = Modifier.fillMaxWidth().height(48.dp),
          colors =
              ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.secondary,
                  contentColor = MaterialTheme.colorScheme.onSecondary),
          shape = RoundedCornerShape(24.dp)) {
            Icon(
                imageVector = Icons.Outlined.Groups,
                contentDescription = "Organizations",
                modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Organizations", fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
      modifier = modifier.clickable(onClick = onClick).padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface)

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface)
      }
}
