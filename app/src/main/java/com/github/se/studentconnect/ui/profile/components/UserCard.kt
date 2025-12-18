package com.github.se.studentconnect.ui.profile.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.ui.userqr.UserQRCode
import com.github.se.studentconnect.ui.utils.loadBitmapFromUser

// STYLING CONSTANTS
private val CARD_SHAPE = RoundedCornerShape(16.dp)
private val CARD_ELEVATION = 16.dp
private val CARD_BORDER_WIDTH = 3.dp
private val CARD_BORDER_ALPHA = 0.3f
private val GRADIENT_ALPHA = 0.5f

/**
 * Extension function that applies the standard card styling. This creates the subtle gradient
 * effect and semi-transparent border that gives the card its polished, slightly glossy appearance.
 */
@Composable
private fun Modifier.cardStyling(): Modifier =
    this.background(
            brush =
                Brush.verticalGradient(
                    colors =
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = GRADIENT_ALPHA))),
            shape = CARD_SHAPE)
        .border(
            width = CARD_BORDER_WIDTH,
            color = MaterialTheme.colorScheme.outline.copy(alpha = CARD_BORDER_ALPHA),
            shape = CARD_SHAPE)

/**
 * A wrapper component that provides consistent styling for card faces. Both the front and back of
 * the UserCard use this container to ensure they have the same surface color, shape, and styling.
 * This keeps the visual appearance consistent when flipping.
 */
@Composable
private fun CardContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Surface(
      modifier = modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.surface,
      shape = CARD_SHAPE) {
        Box(modifier = Modifier.fillMaxSize().cardStyling()) { content() }
      }
}

/**
 * A flippable card that displays user profile information on the front and a QR code on the back.
 * Tapping anywhere on the card triggers a flip animation that reveals the QR code.
 *
 * @param user The user whose information will be displayed on the card.
 * @param modifier Optional modifier for positioning and styling the card container.
 * @param onClick Optional callback invoked each time the card is tapped (in addition to flipping).
 */
@Composable
fun UserCard(user: User, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
  var isFlipped by remember { mutableStateOf(false) }

  val rotation by
      animateFloatAsState(
          targetValue = if (isFlipped) 180f else 0f,
          animationSpec = tween(durationMillis = 600),
          label = "card_rotation")

  val cardWidth = 320.dp
  val cardHeight = 200.dp

  Box(
      modifier =
          modifier
              .size(width = cardWidth, height = cardHeight)
              .background(MaterialTheme.colorScheme.background)
              .clickable(
                  indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    isFlipped = !isFlipped
                    onClick?.invoke()
                  }) {
        // Front of the card
        Card(
            modifier =
                Modifier.fillMaxSize()
                    .graphicsLayer {
                      rotationY = rotation
                      cameraDistance = 12f * density
                      // Hide the front when it's rotated more than 90 degrees
                      alpha = if (rotation > 90f) 0f else 1f
                    }
                    .shadow(elevation = CARD_ELEVATION, shape = CARD_SHAPE),
            shape = CARD_SHAPE,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
              UserCardFront(user = user)
            }

        // Back of the card
        Card(
            modifier =
                Modifier.fillMaxSize()
                    .graphicsLayer {
                      rotationY = rotation + 180f
                      cameraDistance = 12f * density
                      // Hide the back when it's rotated less than 90 degrees
                      alpha = if (rotation > 90f) 1f else 0f
                    }
                    .shadow(elevation = CARD_ELEVATION, shape = CARD_SHAPE),
            shape = CARD_SHAPE,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
              UserCardBack(user = user)
            }
      }
}

/**
 * The front face of the user card showing profile information. If the download fails, a default
 * person icon is shown instead.
 */
@Composable
private fun UserCardFront(user: User, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  LaunchedEffect(user) { imageBitmap = loadBitmapFromUser(context, user) }

  CardContainer(modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
      // App Logo (Top Right)
      Box(
          modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
          contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.studnet_logo),
                contentDescription =
                    stringResource(R.string.content_description_student_connect_logo),
                modifier = Modifier.size(56.dp, 18.dp),
                tint = MaterialTheme.colorScheme.primary)
          }

      // Main Content Row
      Row(
          modifier = Modifier.fillMaxSize().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically) {
            // Profile Picture (Left Side)
            Box(
                modifier =
                    Modifier.size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center) {
                  val profilePictureDescription =
                      stringResource(R.string.content_description_profile_picture)
                  if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = profilePictureDescription,
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop)
                  } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = profilePictureDescription,
                        modifier = Modifier.fillMaxSize().padding(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }

            Spacer(modifier = Modifier.width(14.dp))

            // User Information (Right Side)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
              // Full name on one line
              Text(
                  text = "${user.firstName} ${user.lastName}",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)

              Spacer(modifier = Modifier.height(4.dp))

              // Username
              Text(
                  text = "@${user.username}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)

              Spacer(modifier = Modifier.height(6.dp))

              // University with icon
              InfoRow(
                  icon = Icons.Outlined.School,
                  text = user.university,
                  contentDescription = stringResource(R.string.content_description_university))

              Spacer(modifier = Modifier.height(2.dp))

              // Birthday with icon (if available)
              user.birthdate?.let { birthday ->
                InfoRow(
                    icon = Icons.Outlined.Cake,
                    text = birthday,
                    contentDescription = stringResource(R.string.content_description_birthday))
              }
            }
          }
    }
  }
}

/** A row displaying an icon and text, used for user info items like university and birthday. */
@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Start) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
      }
}

/**
 * The back face of the user card showing the QR code for connecting. Displays a scannable QR code
 * that contains the user's ID. Other users can scan this code with their phone to quickly see the
 * profile of the user. The layout is centered and includes instructional text to guide users on
 * what to do.
 */
@Composable
private fun UserCardBack(user: User, modifier: Modifier = Modifier) {
  CardContainer(modifier = modifier) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Text(
              text = stringResource(R.string.text_scan_qr_code_to_connect),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(16.dp))

          // QR Code
          Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            UserQRCode(userId = user.userId)
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
              text = stringResource(R.string.text_scan_qr_to_connect),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center)
        }
  }
}
