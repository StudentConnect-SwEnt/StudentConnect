package com.github.se.studentconnect.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.chat.ChatMessage
import com.github.se.studentconnect.ui.navigation.Route
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Test tags for the EventChatScreen and its components. */
object EventChatScreenTestTags {
  const val SCREEN = "event_chat_screen"
  const val TOP_APP_BAR = "event_chat_top_app_bar"
  const val BACK_BUTTON = "event_chat_back_button"
  const val MESSAGES_LIST = "event_chat_messages_list"
  const val MESSAGE_ITEM = "event_chat_message_item"
  const val MESSAGE_BUBBLE = "event_chat_message_bubble"
  const val MESSAGE_TEXT = "event_chat_message_text"
  const val MESSAGE_SENDER = "event_chat_message_sender"
  const val MESSAGE_TIME = "event_chat_message_time"
  const val INPUT_FIELD = "event_chat_input_field"
  const val SEND_BUTTON = "event_chat_send_button"
  const val LOADING_INDICATOR = "event_chat_loading_indicator"
  const val EMPTY_STATE = "event_chat_empty_state"
  const val ERROR_MESSAGE = "event_chat_error_message"
  const val TYPING_INDICATOR = "event_chat_typing_indicator"
}

/**
 * Main event chat screen displaying messages, typing indicators, and message input.
 *
 * @param eventId The ID of the event to display the chat for.
 * @param navController Navigation controller for handling navigation.
 * @param chatViewModel ViewModel for managing chat state and operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventChatScreen(
    eventId: String,
    navController: NavHostController,
    chatViewModel: ChatViewModel = viewModel()
) {
  val uiState by chatViewModel.uiState.collectAsState()
  val listState = rememberLazyListState()

  // Initialize chat when screen loads
  LaunchedEffect(eventId) { chatViewModel.initializeChat(eventId) }

  // Auto-scroll to bottom when new messages arrive
  LaunchedEffect(uiState.messages.size) {
    if (uiState.messages.isNotEmpty()) {
      listState.animateScrollToItem(uiState.messages.size - 1)
    }
  }

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag(EventChatScreenTestTags.SCREEN),
      topBar = {
        TopAppBar(
            title = {
              Column {
                Text(
                    text = uiState.event?.title ?: "Event Chat",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                if (uiState.typingUsers.isNotEmpty()) {
                  val typingText =
                      when (uiState.typingUsers.size) {
                        1 -> "${uiState.typingUsers[0].userName} is typing..."
                        2 ->
                            "${uiState.typingUsers[0].userName} and ${uiState.typingUsers[1].userName} are typing..."
                        else ->
                            "${uiState.typingUsers[0].userName} and ${uiState.typingUsers.size - 1} others are typing..."
                      }
                  Text(
                      text = typingText,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.primary)
                }
              }
            },
            navigationIcon = {
              IconButton(
                  onClick = { navController.popBackStack() },
                  modifier = Modifier.testTag(EventChatScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back))
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.testTag(EventChatScreenTestTags.TOP_APP_BAR))
      },
      bottomBar = {
        ChatInputBar(
            messageText = uiState.messageText,
            onMessageTextChange = { chatViewModel.updateMessageText(it) },
            onSendClick = { chatViewModel.sendMessage() },
            isSending = uiState.isSending,
            enabled = !uiState.isLoading && uiState.currentUser != null)
      }) { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
              when {
                uiState.isLoading -> {
                  CircularProgressIndicator(
                      modifier =
                          Modifier.align(Alignment.Center)
                              .testTag(EventChatScreenTestTags.LOADING_INDICATOR))
                }
                uiState.error != null -> {
                  ErrorMessage(
                      message = uiState.error!!,
                      onDismiss = { chatViewModel.clearError() },
                      modifier = Modifier.align(Alignment.TopCenter).padding(16.dp))
                }
                uiState.messages.isEmpty() -> {
                  EmptyChat(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                  Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier =
                            Modifier.weight(1f)
                                .fillMaxWidth()
                                .testTag(EventChatScreenTestTags.MESSAGES_LIST),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                          items(items = uiState.messages, key = { it.messageId }) { message ->
                            MessageItem(
                                message = message,
                                isCurrentUser = message.senderId == uiState.currentUser?.userId,
                                onSenderClick = {
                                  navController.navigate(Route.visitorProfile(message.senderId))
                                })
                          }
                        }

                    // Typing indicator
                    if (uiState.typingUsers.isNotEmpty()) {
                      TypingIndicator(
                          typingUserNames = uiState.typingUsers.map { it.userName },
                          modifier = Modifier.testTag(EventChatScreenTestTags.TYPING_INDICATOR))
                    }
                  }
                }
              }
            }
      }
}

/**
 * Displays a single chat message bubble.
 *
 * @param message The message to display.
 * @param isCurrentUser Whether this message is from the current user.
 * @param onSenderClick Callback when the sender name is clicked.
 */
@Composable
private fun MessageItem(
    message: ChatMessage,
    isCurrentUser: Boolean,
    onSenderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  val alignment = if (isCurrentUser) Arrangement.End else Arrangement.Start
  val bubbleColor =
      if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer
      else MaterialTheme.colorScheme.surfaceContainer

  val textColor =
      if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer
      else MaterialTheme.colorScheme.onSurface

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .testTag("${EventChatScreenTestTags.MESSAGE_ITEM}_${message.messageId}"),
      horizontalArrangement = alignment) {
        Column(
            modifier =
                Modifier.widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isCurrentUser) 16.dp else 4.dp,
                            topEnd = if (isCurrentUser) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp))
                    .background(bubbleColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag(EventChatScreenTestTags.MESSAGE_BUBBLE),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
              // Sender name (only show for other users)
              if (!isCurrentUser) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier.clickable(onClick = onSenderClick)
                            .testTag(EventChatScreenTestTags.MESSAGE_SENDER))
              }

              // Message content
              Text(
                  text = message.content,
                  style = MaterialTheme.typography.bodyMedium,
                  color = textColor,
                  modifier = Modifier.testTag(EventChatScreenTestTags.MESSAGE_TEXT))

              // Timestamp
              Text(
                  text = formatTimestamp(message.timestamp.toDate()),
                  style = MaterialTheme.typography.labelSmall,
                  color = textColor.copy(alpha = 0.7f),
                  modifier = Modifier.testTag(EventChatScreenTestTags.MESSAGE_TIME))
            }
      }
}

/**
 * Chat input bar with text field and send button.
 *
 * @param messageText Current message text.
 * @param onMessageTextChange Callback when message text changes.
 * @param onSendClick Callback when send button is clicked.
 * @param isSending Whether a message is currently being sent.
 * @param enabled Whether the input is enabled.
 */
@Composable
private fun ChatInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean,
    enabled: Boolean
) {
  Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              OutlinedTextField(
                  value = messageText,
                  onValueChange = onMessageTextChange,
                  modifier = Modifier.weight(1f).testTag(EventChatScreenTestTags.INPUT_FIELD),
                  placeholder = {
                    Text(
                        text = stringResource(R.string.chat_message_placeholder),
                        style = MaterialTheme.typography.bodyMedium)
                  },
                  textStyle = MaterialTheme.typography.bodyMedium,
                  enabled = enabled && !isSending,
                  singleLine = false,
                  maxLines = 4,
                  shape = RoundedCornerShape(24.dp),
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          focusedBorderColor = MaterialTheme.colorScheme.primary,
                          unfocusedBorderColor = MaterialTheme.colorScheme.outline))

              // Send button
              IconButton(
                  onClick = onSendClick,
                  enabled = enabled && !isSending && messageText.isNotBlank(),
                  modifier =
                      Modifier.size(48.dp)
                          .clip(CircleShape)
                          .background(
                              if (enabled && !isSending && messageText.isNotBlank())
                                  MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.surfaceVariant)
                          .testTag(EventChatScreenTestTags.SEND_BUTTON)) {
                    if (isSending) {
                      CircularProgressIndicator(
                          modifier = Modifier.size(24.dp),
                          color = MaterialTheme.colorScheme.onPrimary,
                          strokeWidth = 2.dp)
                    } else {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.Send,
                          contentDescription = stringResource(R.string.chat_send_message),
                          tint =
                              if (messageText.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                              else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
            }
      }
}

/**
 * Empty state displayed when there are no messages.
 *
 * @param modifier Modifier to apply to the component.
 */
@Composable
private fun EmptyChat(modifier: Modifier = Modifier) {
  Column(
      modifier =
          modifier.fillMaxWidth().padding(32.dp).testTag(EventChatScreenTestTags.EMPTY_STATE),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.chat_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = stringResource(R.string.chat_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

/**
 * Error message display.
 *
 * @param message The error message to display.
 * @param onDismiss Callback when the error is dismissed.
 * @param modifier Modifier to apply to the component.
 */
@Composable
private fun ErrorMessage(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier.fillMaxWidth().testTag(EventChatScreenTestTags.ERROR_MESSAGE),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = message,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onErrorContainer,
                  modifier = Modifier.weight(1f))
              TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.button_dismiss),
                    color = MaterialTheme.colorScheme.onErrorContainer)
              }
            }
      }
}

/**
 * Formats a timestamp for display in chat messages.
 *
 * @param date The date to format.
 * @return A formatted time string (e.g., "10:30 AM" or "Yesterday, 3:45 PM").
 */
private fun formatTimestamp(date: Date): String {
  val now = Date()
  val diff = now.time - date.time
  val dayInMillis = 24 * 60 * 60 * 1000

  return when {
    diff < 60000 -> "Just now" // Less than a minute
    diff < dayInMillis -> {
      // Today - show time only
      SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    }
    diff < 2 * dayInMillis -> {
      // Yesterday
      "Yesterday, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    }
    else -> {
      // Older - show date and time
      SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
    }
  }
}
