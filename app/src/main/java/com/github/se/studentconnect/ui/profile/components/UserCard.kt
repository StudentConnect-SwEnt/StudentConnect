package com.github.se.studentconnect.ui.profile.components

// import androidx.compose.ui.tooling.preview.Preview
// import com.github.se.studentconnect.ui.theme.AppTheme
// import com.google.firebase.Timestamp
// import java.util.Date
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.ui.userqr.UserQRCode

// Common styling constants
private val CARD_SHAPE = RoundedCornerShape(16.dp)
private val CARD_ELEVATION = 16.dp
private val CARD_BORDER_WIDTH = 3.dp
private val CARD_BORDER_ALPHA = 0.3f
private val GRADIENT_ALPHA = 0.5f

// Reusable card styling modifier
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

// Reusable card container
@Composable
private fun CardContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Surface(
      modifier = modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.surface,
      shape = CARD_SHAPE) {
        Box(modifier = Modifier.fillMaxSize().cardStyling()) { content() }
      }
}

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

@Composable
private fun UserCardFront(user: User, modifier: Modifier = Modifier) {
  CardContainer(modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
      // App Logo (Top Right)
      Box(
          modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
          contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.studnet_logo),
                contentDescription =
                    stringResource(R.string.content_description_student_connect_logo),
                modifier = Modifier.size(64.dp, 22.dp),
                tint = MaterialTheme.colorScheme.primary)
          }

      // Main Content Row
      Row(
          modifier = Modifier.fillMaxSize().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically) {
            // Profile Picture (Left Side)
            Box(
                modifier =
                    Modifier.size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp))) {
                  // Profile picture placeholder (TODO: Add image loading when Coil is available)
                  Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription =
                          stringResource(R.string.content_description_profile_picture),
                      modifier = Modifier.fillMaxSize().padding(16.dp),
                      tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

            Spacer(modifier = Modifier.width(16.dp))

            // User Information (Right Side)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
              Text(
                  text = user.firstName,
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface)

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                  text = user.lastName,
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface)

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                  text = user.birthdate ?: stringResource(R.string.text_birthday_not_provided),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
    }
  }
}

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

          // QR Code (even smaller size)
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
