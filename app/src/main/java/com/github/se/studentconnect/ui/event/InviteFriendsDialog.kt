package com.github.se.studentconnect.ui.event

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User

object InviteFriendsDialogTestTags {
  const val DIALOG = "inviteFriendsDialog"
  const val LOADING = "inviteFriendsLoading"
  const val EMPTY = "inviteFriendsEmpty"
  const val ERROR = "inviteFriendsError"
  const val FRIEND_CHECKBOX = "inviteFriendCheckbox"
  const val SEND_BUTTON = "inviteFriendsSendButton"
}

/**
 * Dialog for inviting friends to a private event. Shows loading/error/empty states, a checkbox list
 * of friends, and a single action to update invitations.
 */
@Composable
fun InviteFriendsDialog(
    state: EventUiState,
    onToggleFriend: (String) -> Unit,
    onSendInvites: () -> Unit,
    onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
        modifier =
            Modifier.fillMaxWidth().padding(16.dp).testTag(InviteFriendsDialogTestTags.DIALOG),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface) {
          Column(
              modifier =
                  Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(R.string.event_label_invite_friends),
                          style = MaterialTheme.typography.headlineSmall)
                      IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription =
                                stringResource(R.string.content_description_close_story))
                      }
                    }

                InviteFriendsSection(
                    friends = state.friends,
                    invitedFriendIds = state.invitedFriendIds,
                    isLoadingFriends = state.isLoadingFriends,
                    friendsErrorRes = state.friendsErrorRes,
                    onToggleFriend = onToggleFriend)

                Button(
                    onClick = onSendInvites,
                    enabled =
                        !state.isInvitingFriends &&
                            !state.isLoadingFriends &&
                            (state.invitedFriendIds.isNotEmpty() ||
                                state.initialInvitedFriendIds.isNotEmpty()),
                    modifier =
                        Modifier.fillMaxWidth().testTag(InviteFriendsDialogTestTags.SEND_BUTTON)) {
                      if (state.isInvitingFriends) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(2.dp),
                            color = MaterialTheme.colorScheme.onPrimary)
                      } else {
                        Text(stringResource(R.string.event_button_update_invites))
                      }
                    }
              }
        }
  }
}

@Composable
private fun InviteFriendsSection(
    friends: List<User>,
    invitedFriendIds: Set<String>,
    isLoadingFriends: Boolean,
    @StringRes friendsErrorRes: Int?,
    onToggleFriend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier = modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                  text = stringResource(R.string.event_label_invite_friends),
                  style =
                      MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
              when {
                isLoadingFriends -> {
                  Row(
                      modifier =
                          Modifier.fillMaxWidth().testTag(InviteFriendsDialogTestTags.LOADING),
                      horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                      }
                }
                friendsErrorRes != null -> {
                  val errorText = stringResource(friendsErrorRes)
                  Text(
                      text = errorText,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.error,
                      modifier = Modifier.testTag(InviteFriendsDialogTestTags.ERROR))
                }
                friends.isEmpty() -> {
                  Text(
                      text = stringResource(R.string.event_invite_friends_empty),
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.testTag(InviteFriendsDialogTestTags.EMPTY))
                }
                else -> {
                  Text(
                      text = stringResource(R.string.event_invite_friends_help),
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    friends.forEach { friend ->
                      FriendInviteRow(
                          friend = friend,
                          isChecked = invitedFriendIds.contains(friend.userId),
                          onCheckedChange = { onToggleFriend(friend.userId) })
                    }
                  }
                }
              }
            }
      }
}

@Composable
private fun FriendInviteRow(
    friend: User,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            modifier =
                Modifier.testTag("${InviteFriendsDialogTestTags.FRIEND_CHECKBOX}_${friend.userId}"))
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = friend.getFullName(),
              style = MaterialTheme.typography.bodyLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
          Text(
              text = "@${friend.username}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
        }
      }
}
