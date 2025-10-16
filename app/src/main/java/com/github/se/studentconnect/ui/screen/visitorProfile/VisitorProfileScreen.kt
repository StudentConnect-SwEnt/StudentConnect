package com.github.se.studentconnect.ui.screen.visitorProfile

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.AppTheme
import java.util.Locale

@Composable
fun VisitorProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onAddFriendClick: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
  VisitorProfileContent(
      user = user,
      onBackClick = onBackClick,
      onAddFriendClick = onAddFriendClick,
      modifier = modifier)
}

@VisibleForTesting(otherwise = VisibleForTesting.Companion.PRIVATE)
@Composable
internal fun VisitorProfileContent(
    user: User,
    onBackClick: () -> Unit,
    onAddFriendClick: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
  val scrollState = rememberScrollState()

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Column(
        modifier =
            Modifier.Companion.fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .semantics { testTag = C.Tag.visitor_profile_screen },
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
          VisitorProfileTopBar(user.userId, onBackClick = onBackClick)

          VisitorProfileInfoCard(user = user, onAddFriendClick = onAddFriendClick)

          VisitorProfileEventSection(title = "Pinned Events")
        }
  }
}

@VisibleForTesting(otherwise = VisibleForTesting.Companion.PRIVATE)
@Composable
internal fun VisitorProfileTopBar(userId: String, onBackClick: () -> Unit) {
  Box(
      modifier =
          Modifier.Companion.fillMaxWidth().semantics { testTag = C.Tag.visitor_profile_top_bar }) {
        IconButton(
            onClick = onBackClick,
            modifier =
                Modifier.Companion.align(Alignment.Companion.TopStart).semantics {
                  testTag = C.Tag.visitor_profile_back
                }) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  tint = MaterialTheme.colorScheme.onSurface)
            }

        Text(
            text = "@${userId}",
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Companion.Light,
                    fontStyle = FontStyle.Companion.Italic),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.Companion.align(Alignment.Companion.Center))
      }
}

@VisibleForTesting(otherwise = VisibleForTesting.Companion.PRIVATE)
@Composable
internal fun VisitorProfileInfoCard(user: User, onAddFriendClick: () -> Unit) {

  Column(
      modifier = Modifier.Companion.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Companion.CenterVertically) {
              val initials =
                  listOf(user.firstName, user.lastName)
                      .mapNotNull { it.firstOrNull()?.toString() }
                      .joinToString("")
                      .ifBlank { user.userId.take(2) }
                      .uppercase(Locale.getDefault())

              Surface(
                  modifier =
                      Modifier.Companion.size(72.dp).semantics {
                        testTag = C.Tag.visitor_profile_avatar
                      },
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                  tonalElevation = 0.dp) {
                    Box(contentAlignment = Alignment.Companion.Center) {
                      Text(
                          text = initials,
                          style =
                              MaterialTheme.typography.titleLarge.copy(
                                  fontWeight = FontWeight.Companion.Bold),
                          color = MaterialTheme.colorScheme.primary)
                    }
                  }

              Spacer(modifier = Modifier.Companion.width(16.dp))

              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = user.getFullName(),
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Companion.Bold,
                            color = MaterialTheme.colorScheme.onSurface),
                    modifier =
                        Modifier.Companion.semantics { testTag = C.Tag.visitor_profile_user_name })

                Text(
                    text = user.userId,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier =
                        Modifier.Companion.semantics { testTag = C.Tag.visitor_profile_user_id })
              }
            }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
              modifier = Modifier.Companion.padding(start = 10.dp),
              text = "Bio",
              style =
                  MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Companion.Medium,
                      color = MaterialTheme.colorScheme.primary))

          val bio = user.bio?.takeIf { it.isNotBlank() } ?: "No biography available yet."
          val bioColor =
              if (user.bio.isNullOrBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant
              } else {
                MaterialTheme.colorScheme.onSurface
              }

          Text(
              text = bio,
              style = MaterialTheme.typography.bodyMedium,
              color = bioColor,
              modifier =
                  Modifier.Companion.padding(start = 10.dp).semantics {
                    testTag = C.Tag.visitor_profile_bio
                  })

          Box(modifier = Modifier.Companion.padding(horizontal = 8.dp)) {
            Button(
                onClick = onAddFriendClick,
                modifier =
                    Modifier.Companion.fillMaxWidth().semantics {
                      testTag = C.Tag.visitor_profile_add_friend
                    },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(10.dp)) {
                  Text(
                      text = "Add Friend",
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.onPrimary)
                }
          }
        }
      }
}

@VisibleForTesting(otherwise = VisibleForTesting.Companion.PRIVATE)
@Composable
internal fun VisitorProfileEventSection(title: String) {
  Column(
      modifier =
          Modifier.Companion.fillMaxWidth().semantics {
            testTag = C.Tag.visitor_profile_pinned_section
          },
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Companion.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface))

        Surface(
            modifier = Modifier.Companion.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow) {
              Column(
                  modifier = Modifier.Companion.fillMaxWidth().padding(24.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                    Text(
                        text = "Nothing to display yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier.Companion.semantics {
                              testTag = C.Tag.visitor_profile_empty_state
                            })
                  }
            }
      }
}

@Preview(showBackground = true)
@Composable
private fun VisitorProfileScreenPreview() {
  AppTheme {
    VisitorProfileScreen(
        user =
            User(
                userId = "user-123",
                email = "sample@studentconnect.ch",
                firstName = "Alex",
                lastName = "Martin",
                university = "EPFL",
                bio = "Curious learner, exploring new connections."),
        onBackClick = {},
        onAddFriendClick = {})
  }
}
