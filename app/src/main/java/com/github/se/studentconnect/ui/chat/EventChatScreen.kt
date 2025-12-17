package com.github.se.studentconnect.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        ChatTopAppBar(
            eventTitle = uiState.event?.title,
            typingUsers = uiState.typingUsers,
            onBackClick = { navController.popBackStack() })
      },
      bottomBar = {
        ChatInputBar(
            messageText = uiState.messageText,
            onMessageTextChange = { chatViewModel.updateMessageText(it) },
            onSendClick = { chatViewModel.sendMessage() },
            isSending = uiState.isSending,
            enabled = !uiState.isLoading && uiState.currentUser != null)
      }) { paddingValues ->
        ChatContent(
            uiState = uiState,
            listState = listState,
            paddingValues = paddingValues,
            navController = navController,
            onDismissError = { chatViewModel.clearError() })
      }
}

/** Top app bar for the chat screen showing event title and typing indicators. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopAppBar(
    eventTitle: String?,
    typingUsers: Map<String, String>,
    onBackClick: () -> Unit
) {
  TopAppBar(
      title = { ChatTopAppBarTitle(eventTitle = eventTitle, typingUsers = typingUsers) },
      navigationIcon = {
        IconButton(
            onClick = onBackClick,
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
}

/** Title section of the top app bar with event name and typing indicator. */
@Composable
private fun ChatTopAppBarTitle(eventTitle: String?, typingUsers: Map<String, String>) {
  Column {
    Text(
        text = eventTitle ?: stringResource(R.string.chat_default_title),
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis)
    if (typingUsers.isNotEmpty()) {
      TypingStatusText(typingUsers = typingUsers)
    }
  }
}

/** Displays the typing status text based on the number of users typing. */
@Composable
private fun TypingStatusText(typingUsers: Map<String, String>) {
  val userNames = typingUsers.values.toList()
  val othersCount = userNames.size - 1
  val typingText =
      when (userNames.size) {
        1 -> stringResource(R.string.chat_typing_single, userNames[0])
        2 -> stringResource(R.string.chat_typing_two, userNames[0], userNames[1])
        else ->
            pluralStringResource(
                R.plurals.chat_typing_multiple, othersCount, userNames[0], othersCount)
      }
  Text(
      text = typingText,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.primary)
}

/** Main content area of the chat screen. */
@Composable
private fun ChatContent(
    uiState: ChatUiState,
    listState: LazyListState,
    paddingValues: PaddingValues,
    navController: NavHostController,
    onDismissError: () -> Unit
) {
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
                message = uiState.error,
                onDismiss = onDismissError,
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .padding(dimensionResource(R.dimen.chat_error_padding)))
          }
          uiState.messages.isEmpty() -> {
            EmptyChat(modifier = Modifier.align(Alignment.Center))
          }
          else -> {
            MessagesColumn(
                messages = uiState.messages,
                typingUsers = uiState.typingUsers,
                currentUserId = uiState.currentUser?.userId,
                listState = listState,
                navController = navController)
          }
        }
      }
}

/** Column containing the messages list and typing indicator. */
@Composable
private fun MessagesColumn(
    messages: List<ChatMessage>,
    typingUsers: Map<String, String>,
    currentUserId: String?,
    listState: LazyListState,
    navController: NavHostController
) {
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier =
            Modifier.weight(1f).fillMaxWidth().testTag(EventChatScreenTestTags.MESSAGES_LIST),
        contentPadding =
            PaddingValues(
                horizontal = dimensionResource(R.dimen.chat_messages_padding),
                vertical = dimensionResource(R.dimen.chat_messages_padding)),
        verticalArrangement =
            Arrangement.spacedBy(dimensionResource(R.dimen.chat_messages_spacing))) {
          items(items = messages, key = { it.messageId }) { message ->
            MessageItem(
                message = message,
                isCurrentUser = message.senderId == currentUserId,
                onSenderClick = { navController.navigate(Route.visitorProfile(message.senderId)) })
          }
        }

    // Typing indicator
    if (typingUsers.isNotEmpty()) {
      TypingIndicator(
          typingUserNames = typingUsers.values.toList(),
          modifier = Modifier.testTag(EventChatScreenTestTags.TYPING_INDICATOR))
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
                Modifier.widthIn(max = dimensionResource(R.dimen.chat_message_max_width))
                    .clip(
                        RoundedCornerShape(
                            topStart =
                                if (isCurrentUser)
                                    dimensionResource(R.dimen.chat_message_corner_radius_normal)
                                else dimensionResource(R.dimen.chat_message_corner_radius_small),
                            topEnd =
                                if (isCurrentUser)
                                    dimensionResource(R.dimen.chat_message_corner_radius_small)
                                else dimensionResource(R.dimen.chat_message_corner_radius_normal),
                            bottomStart =
                                dimensionResource(R.dimen.chat_message_corner_radius_normal),
                            bottomEnd =
                                dimensionResource(R.dimen.chat_message_corner_radius_normal)))
                    .background(bubbleColor)
                    .padding(
                        horizontal = dimensionResource(R.dimen.chat_message_padding_horizontal),
                        vertical = dimensionResource(R.dimen.chat_message_padding_vertical))
                    .testTag(EventChatScreenTestTags.MESSAGE_BUBBLE),
            verticalArrangement =
                Arrangement.spacedBy(dimensionResource(R.dimen.chat_message_spacing))) {
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
      shadowElevation = dimensionResource(R.dimen.chat_input_elevation)) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(R.dimen.chat_input_padding_horizontal),
                        vertical = dimensionResource(R.dimen.chat_input_padding_vertical)),
            horizontalArrangement =
                Arrangement.spacedBy(dimensionResource(R.dimen.chat_input_spacing)),
            verticalAlignment = Alignment.CenterVertically) {
              ChatTextField(
                  messageText = messageText,
                  onMessageTextChange = onMessageTextChange,
                  enabled = enabled && !isSending,
                  modifier = Modifier.weight(1f))

              SendButton(
                  onClick = onSendClick,
                  enabled = enabled && !isSending && messageText.isNotBlank(),
                  isSending = isSending,
                  hasText = messageText.isNotBlank())
            }
      }
}

/** Text field for composing chat messages. */
@Composable
private fun ChatTextField(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = messageText,
      onValueChange = onMessageTextChange,
      modifier = modifier.testTag(EventChatScreenTestTags.INPUT_FIELD),
      placeholder = {
        Text(
            text = stringResource(R.string.chat_message_placeholder),
            style = MaterialTheme.typography.bodyMedium)
      },
      textStyle = MaterialTheme.typography.bodyMedium,
      enabled = enabled,
      singleLine = false,
      maxLines = 4,
      shape = RoundedCornerShape(dimensionResource(R.dimen.chat_input_corner_radius)),
      colors =
          OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline))
}

/** Send button for the chat input bar. */
@Composable
private fun SendButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isSending: Boolean,
    hasText: Boolean
) {
  val backgroundColor =
      if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

  IconButton(
      onClick = onClick,
      enabled = enabled,
      modifier =
          Modifier.size(dimensionResource(R.dimen.chat_send_button_size))
              .clip(CircleShape)
              .background(backgroundColor)
              .testTag(EventChatScreenTestTags.SEND_BUTTON)) {
        if (isSending) {
          CircularProgressIndicator(
              modifier = Modifier.size(dimensionResource(R.dimen.chat_send_button_icon_size)),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = dimensionResource(R.dimen.chat_send_button_progress_stroke))
        } else {
          val iconTint =
              if (hasText) MaterialTheme.colorScheme.onPrimary
              else MaterialTheme.colorScheme.onSurfaceVariant
          Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = stringResource(R.string.chat_send_message),
              tint = iconTint)
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
          modifier
              .fillMaxWidth()
              .padding(dimensionResource(R.dimen.chat_empty_padding))
              .testTag(EventChatScreenTestTags.EMPTY_STATE),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chat_empty_spacing))) {
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
            modifier =
                Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.chat_error_card_padding)),
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
@Composable
private fun formatTimestamp(date: Date): String {
  val now = Date()
  val diff = now.time - date.time
  val dayInMillis = 24 * 60 * 60 * 1000

  return when {
    diff < 60000 -> stringResource(R.string.chat_timestamp_just_now)
    diff < dayInMillis -> {
      // Today - show time only
      SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    }
    diff < 2 * dayInMillis -> {
      // Yesterday
      val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
      stringResource(R.string.chat_timestamp_yesterday, timeStr)
    }
    else -> {
      // Older - show date and time
      SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
    }
  }
}
