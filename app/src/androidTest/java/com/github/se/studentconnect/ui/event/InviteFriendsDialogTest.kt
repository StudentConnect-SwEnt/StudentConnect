package com.github.se.studentconnect.ui.event

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class InviteFriendsDialogTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun buildUser(id: String, first: String = "First", last: String = "Last") =
      User(
          userId = id,
          email = "$id@example.com",
          username = "user_$id",
          firstName = first,
          lastName = last,
          university = "EPFL")

  private fun setContent(
      state: EventUiState,
      onToggleFriend: (String) -> Unit = {},
      onSendInvites: () -> Unit = {},
      onDismiss: () -> Unit = {}
  ) {
    composeTestRule.setContent {
      AppTheme {
        InviteFriendsDialog(
            state = state,
            onToggleFriend = onToggleFriend,
            onSendInvites = onSendInvites,
            onDismiss = onDismiss)
      }
    }
  }

  @Test
  fun loadingState_showsProgressIndicator() {
    val state = EventUiState(isLoadingFriends = true)

    setContent(state)

    composeTestRule
        .onNodeWithTag(InviteFriendsDialogTestTags.LOADING, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(InviteFriendsDialogTestTags.SEND_BUTTON, useUnmergedTree = true)
        .assertIsNotEnabled()
  }

  @Test
  fun emptyFriends_showsEmptyMessage() {
    val state =
        EventUiState(
            friends = emptyList(),
            invitedFriendIds = emptySet(),
            initialInvitedFriendIds = emptySet())

    setContent(state)

    composeTestRule.onNodeWithText("You have no friends to invite yet.").assertIsDisplayed()
  }

  @Test
  fun friendsList_togglesSelection_andButtonEnabledWhenSelected() {
    val friend = buildUser("friend1")
    var toggledId: String? = null
    lateinit var stateHolder: MutableState<EventUiState>

    composeTestRule.setContent {
      val state = remember {
        mutableStateOf(
            EventUiState(
                friends = listOf(friend),
                invitedFriendIds = emptySet(),
                initialInvitedFriendIds = emptySet(),
                isLoadingFriends = false))
      }
      stateHolder = state
      AppTheme {
        InviteFriendsDialog(
            state = state.value,
            onToggleFriend = { toggledId = it },
            onSendInvites = {},
            onDismiss = {})
      }
    }

    composeTestRule
        .onNodeWithTag(
            "${InviteFriendsDialogTestTags.FRIEND_CHECKBOX}_${friend.userId}",
            useUnmergedTree = true)
        .performClick()

    // Callback invoked
    assert(toggledId == friend.userId)

    // Update state to reflect selection to enable button
    composeTestRule.runOnIdle {
      stateHolder.value = stateHolder.value.copy(invitedFriendIds = setOf(friend.userId))
    }

    composeTestRule
        .onNodeWithTag(InviteFriendsDialogTestTags.SEND_BUTTON, useUnmergedTree = true)
        .assertIsEnabled()
  }

  @Test
  fun sendButton_enabledWithExistingInvitesEvenIfNoSelection() {
    val state =
        EventUiState(
            friends = emptyList(),
            invitedFriendIds = emptySet(),
            initialInvitedFriendIds = setOf("alreadyInvited"))

    setContent(state)

    composeTestRule
        .onNodeWithTag(InviteFriendsDialogTestTags.SEND_BUTTON, useUnmergedTree = true)
        .assertIsEnabled()
  }
}
