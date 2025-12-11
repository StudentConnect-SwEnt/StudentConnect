package com.github.se.studentconnect.ui.screen.visitorprofile

import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.profile.components.PinnedEventsSection
import com.github.se.studentconnect.ui.profile.components.ProfileHeader
import com.github.se.studentconnect.ui.profile.components.ProfileHeaderCallbacks
import com.github.se.studentconnect.ui.profile.components.ProfileStats
import java.util.Locale

/** Data class holding all visitor profile screen callbacks */
data class VisitorProfileCallbacks(
    val onBackClick: () -> Unit,
    val onAddFriendClick: () -> Unit,
    val onCancelFriendClick: () -> Unit = {},
    val onRemoveFriendClick: () -> Unit = {},
    val onFriendsClick: () -> Unit = {},
    val onEventsClick: () -> Unit = {},
    val onEventClick: (Event) -> Unit = {}
)

@Composable
fun VisitorProfileScreen(
    user: User,
    friendsCount: Int,
    eventsCount: Int,
    pinnedEvents: List<Event>,
    callbacks: VisitorProfileCallbacks,
    modifier: Modifier = Modifier,
    friendRequestStatus: FriendRequestStatus = FriendRequestStatus.IDLE
) {
  VisitorProfileContent(
      user = user,
      friendsCount = friendsCount,
      eventsCount = eventsCount,
      pinnedEvents = pinnedEvents,
      callbacks = callbacks,
      friendRequestStatus = friendRequestStatus,
      modifier = modifier)
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun VisitorProfileContent(
    user: User,
    friendsCount: Int,
    eventsCount: Int,
    pinnedEvents: List<Event>,
    callbacks: VisitorProfileCallbacks,
    modifier: Modifier = Modifier,
    friendRequestStatus: FriendRequestStatus = FriendRequestStatus.IDLE
) {
  val scrollState = rememberScrollState()
  val context = LocalContext.current

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Column(
        modifier =
            Modifier.fillMaxSize().verticalScroll(scrollState).semantics {
              testTag = C.Tag.visitor_profile_screen
            }) {
          // Top Bar with back button and username
          VisitorProfileTopBar(user.username, onBackClick = callbacks.onBackClick)

          // Profile Header with stats and info
          ProfileHeader(
              user = user,
              stats = ProfileStats(friendsCount = friendsCount, eventsCount = eventsCount),
              callbacks =
                  ProfileHeaderCallbacks(
                      onFriendsClick = {
                        if (friendRequestStatus == FriendRequestStatus.ALREADY_FRIENDS) {
                          callbacks.onFriendsClick()
                        } else {
                          Toast.makeText(
                                  context,
                                  context.getString(R.string.toast_add_friend_to_view_friends),
                                  Toast.LENGTH_SHORT)
                              .show()
                        }
                      },
                      onEventsClick = {
                        if (friendRequestStatus == FriendRequestStatus.ALREADY_FRIENDS) {
                          callbacks.onEventsClick()
                        } else {
                          Toast.makeText(
                                  context,
                                  context.getString(R.string.toast_add_friend_to_view_events),
                                  Toast.LENGTH_SHORT)
                              .show()
                        }
                      }),
              isVisitorMode = true,
              showUsername = false,
              friendButtonsContent = {
                FriendActionButtons(
                    friendRequestStatus = friendRequestStatus,
                    onAddFriendClick = callbacks.onAddFriendClick,
                    onCancelFriendClick = callbacks.onCancelFriendClick,
                    onRemoveFriendClick = callbacks.onRemoveFriendClick)
              },
          )

          // Pinned Events Section
          PinnedEventsSection(pinnedEvents = pinnedEvents, onEventClick = callbacks.onEventClick)
        }
  }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun VisitorProfileTopBar(username: String, onBackClick: () -> Unit) {
  Box(
      modifier =
          Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).semantics {
            testTag = C.Tag.visitor_profile_top_bar
          }) {
        IconButton(
            onClick = onBackClick,
            modifier =
                Modifier.align(Alignment.TopStart).semantics {
                  testTag = C.Tag.visitor_profile_back
                }) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = stringResource(R.string.content_description_back),
                  tint = MaterialTheme.colorScheme.onSurface)
            }

        Text(
            text = "@${username}",
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Center))
      }
}

/**
 * Composable that displays friend action buttons based on friend request status.
 *
 * @param friendRequestStatus The current friend request status
 * @param onAddFriendClick Callback when add friend button is clicked
 * @param onCancelFriendClick Callback when cancel request button is clicked
 * @param onRemoveFriendClick Callback when remove friend button is clicked
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun FriendActionButtons(
    friendRequestStatus: FriendRequestStatus,
    onAddFriendClick: () -> Unit,
    onCancelFriendClick: () -> Unit,
    onRemoveFriendClick: () -> Unit
) {
  var showRemoveFriendDialog by remember { mutableStateOf(false) }

  when (friendRequestStatus) {
    FriendRequestStatus.SENT,
    FriendRequestStatus.ALREADY_SENT -> {
      // Show Cancel and Request Sent buttons
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onCancelFriendClick,
            modifier =
                Modifier.weight(1f).height(48.dp).semantics {
                  testTag = C.Tag.visitor_profile_cancel_friend
                },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(24.dp)) {
              Text(
                  text = stringResource(R.string.button_cancel),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Medium)
            }

        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier.weight(1f).height(48.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(24.dp)) {
              Text(
                  text = stringResource(R.string.text_request_sent),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Medium)
            }
      }
    }
    FriendRequestStatus.ALREADY_FRIENDS -> {
      // Show Remove Friend button
      Button(
          onClick = { showRemoveFriendDialog = true },
          modifier =
              Modifier.fillMaxWidth().height(48.dp).semantics {
                testTag = C.Tag.visitor_profile_remove_friend
              },
          colors =
              ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceVariant,
                  contentColor = MaterialTheme.colorScheme.onSurface),
          shape = RoundedCornerShape(24.dp)) {
            Text(
                text = stringResource(R.string.text_remove_friend),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium)
          }
    }
    else -> {
      // Show Add Friend, Sending, or Try Again button
      val buttonText =
          when (friendRequestStatus) {
            FriendRequestStatus.SENDING -> stringResource(R.string.text_sending)
            FriendRequestStatus.ERROR -> stringResource(R.string.text_try_again)
            else -> stringResource(R.string.button_add_friend)
          }

      val buttonEnabled =
          friendRequestStatus == FriendRequestStatus.IDLE ||
              friendRequestStatus == FriendRequestStatus.ERROR

      Button(
          onClick = onAddFriendClick,
          enabled = buttonEnabled,
          modifier =
              Modifier.fillMaxWidth().height(48.dp).semantics {
                testTag = C.Tag.visitor_profile_add_friend
              },
          colors =
              ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary),
          shape = RoundedCornerShape(24.dp)) {
            Text(text = buttonText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
          }
    }
  }

  // Remove friend confirmation dialog
  if (showRemoveFriendDialog) {
    AlertDialog(
        onDismissRequest = { showRemoveFriendDialog = false },
        title = { Text(text = stringResource(R.string.dialog_remove_friend_title)) },
        text = { Text(text = stringResource(R.string.dialog_remove_friend_message)) },
        confirmButton = {
          TextButton(
              onClick = {
                showRemoveFriendDialog = false
                onRemoveFriendClick()
              },
              modifier = Modifier.semantics { testTag = C.Tag.visitor_profile_dialog_confirm }) {
                Text(text = stringResource(R.string.button_yes))
              }
        },
        dismissButton = {
          TextButton(
              onClick = { showRemoveFriendDialog = false },
              modifier = Modifier.semantics { testTag = C.Tag.visitor_profile_dialog_dismiss }) {
                Text(text = stringResource(R.string.button_no))
              }
        })
  }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun VisitorProfileInfoCard(
    user: User,
    onAddFriendClick: () -> Unit,
    onCancelFriendClick: () -> Unit = {},
    onRemoveFriendClick: () -> Unit = {},
    friendRequestStatus: FriendRequestStatus = FriendRequestStatus.IDLE
) {
  var showRemoveFriendDialog by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically) {
          val initials =
              listOf(user.firstName, user.lastName)
                  .mapNotNull { it.firstOrNull()?.toString() }
                  .joinToString("")
                  .ifBlank { user.username.take(2) }
                  .uppercase(Locale.getDefault())

          Surface(
              modifier = Modifier.size(72.dp).semantics { testTag = C.Tag.visitor_profile_avatar },
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
              tonalElevation = 0.dp) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                      text = initials,
                      style =
                          MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.primary)
                }
              }

          Spacer(modifier = Modifier.width(16.dp))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = user.getFullName(),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.semantics { testTag = C.Tag.visitor_profile_user_name })

            Text(
                text = user.username,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.semantics { testTag = C.Tag.visitor_profile_user_id })
          }
        }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
          modifier = Modifier.padding(start = 10.dp),
          text = stringResource(id = com.github.se.studentconnect.R.string.title_bio),
          style =
              MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary))

      val bio =
          user.bio?.takeIf { it.isNotBlank() }
              ?: stringResource(id = com.github.se.studentconnect.R.string.no_bio_available)
      val bioColor =
          if (user.bio.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant
          else MaterialTheme.colorScheme.onSurface

      Text(
          text = bio,
          style = MaterialTheme.typography.bodyMedium,
          color = bioColor,
          modifier =
              Modifier.padding(start = 10.dp).semantics { testTag = C.Tag.visitor_profile_bio })

      Box(modifier = Modifier.padding(horizontal = 8.dp)) {
        when (friendRequestStatus) {
          FriendRequestStatus.SENT,
          FriendRequestStatus.ALREADY_SENT -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                  onClick = onCancelFriendClick,
                  modifier =
                      Modifier.weight(1f).semantics {
                        testTag = C.Tag.visitor_profile_cancel_friend
                      },
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.surfaceVariant,
                          contentColor = MaterialTheme.colorScheme.onSurface),
                  shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text =
                            stringResource(
                                id = com.github.se.studentconnect.R.string.button_cancel),
                        style = MaterialTheme.typography.labelLarge)
                  }

              Button(
                  onClick = {},
                  enabled = false,
                  modifier = Modifier.weight(1f),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.primary,
                          contentColor = MaterialTheme.colorScheme.onPrimary),
                  shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text =
                            stringResource(
                                id = com.github.se.studentconnect.R.string.text_request_sent),
                        style = MaterialTheme.typography.labelLarge)
                  }
            }
          }
          FriendRequestStatus.ALREADY_FRIENDS -> {
            // Show a prominent remove button when already friends
            Button(
                onClick = { showRemoveFriendDialog = true },
                modifier =
                    Modifier.fillMaxWidth().semantics {
                      testTag = C.Tag.visitor_profile_remove_friend
                    },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(10.dp)) {
                  Text(
                      text =
                          stringResource(
                              id = com.github.se.studentconnect.R.string.text_remove_friend),
                      style = MaterialTheme.typography.labelLarge)
                }
          }
          else -> {
            val buttonText =
                when (friendRequestStatus) {
                  FriendRequestStatus.SENDING ->
                      stringResource(id = com.github.se.studentconnect.R.string.text_sending)
                  FriendRequestStatus.ERROR ->
                      stringResource(id = com.github.se.studentconnect.R.string.text_try_again)
                  else ->
                      stringResource(id = com.github.se.studentconnect.R.string.button_add_friend)
                }

            val buttonEnabled =
                friendRequestStatus == FriendRequestStatus.IDLE ||
                    friendRequestStatus == FriendRequestStatus.ERROR

            Button(
                onClick = onAddFriendClick,
                enabled = buttonEnabled,
                modifier =
                    Modifier.fillMaxWidth().semantics {
                      testTag = C.Tag.visitor_profile_add_friend
                    },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(10.dp)) {
                  Text(
                      text = buttonText,
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.onPrimary)
                }
          }
        }
      }
    }

    if (showRemoveFriendDialog) {
      AlertDialog(
          onDismissRequest = { showRemoveFriendDialog = false },
          title = {
            Text(
                text =
                    stringResource(
                        id = com.github.se.studentconnect.R.string.dialog_remove_friend_title))
          },
          text = {
            Text(
                text =
                    stringResource(
                        id = com.github.se.studentconnect.R.string.dialog_remove_friend_message))
          },
          confirmButton = {
            TextButton(
                onClick = {
                  showRemoveFriendDialog = false
                  onRemoveFriendClick()
                },
                modifier = Modifier.semantics { testTag = C.Tag.visitor_profile_dialog_confirm }) {
                  Text(text = stringResource(id = com.github.se.studentconnect.R.string.button_yes))
                }
          },
          dismissButton = {
            TextButton(
                onClick = { showRemoveFriendDialog = false },
                modifier = Modifier.semantics { testTag = C.Tag.visitor_profile_dialog_dismiss }) {
                  Text(text = stringResource(id = com.github.se.studentconnect.R.string.button_no))
                }
          })
    }
  }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun VisitorProfileEventSection(title: String) {
  Column(
      modifier =
          Modifier.fillMaxWidth().semantics { testTag = C.Tag.visitor_profile_pinned_section },
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow) {
              Column(
                  modifier = Modifier.fillMaxWidth().padding(24.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text =
                            stringResource(
                                id =
                                    com.github.se.studentconnect.R.string
                                        .text_nothing_to_display_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier.semantics { testTag = C.Tag.visitor_profile_empty_state })
                  }
            }
      }
}
