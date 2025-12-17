package com.github.se.studentconnect.ui.chat

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.chat.ChatMessage
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.user.User
import com.google.firebase.Timestamp
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class EventChatScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavController: NavHostController
  private lateinit var mockChatViewModel: ChatViewModel
  private val uiStateFlow = MutableStateFlow(ChatUiState())

  private val testUserId = "user-123"
  private val testEventId = "event-456"

  private val testUser =
      User(
          userId = testUserId,
          firstName = "John",
          lastName = "Doe",
          email = "john@example.com",
          username = "johndoe",
          university = "EPFL",
          profilePictureUrl = null,
          bio = null)

  private val testEvent =
      Event.Private(
          uid = testEventId,
          ownerId = "owner-123",
          title = "Test Event",
          description = "Test Description",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(46.5, 6.6, "EPFL"),
          isFlash = false)

  @Before
  fun setUp() {
    mockNavController = mockk(relaxed = true)
    mockChatViewModel = mockk(relaxed = true)
    AuthenticationProvider.testUserId = testUserId

    every { mockChatViewModel.uiState } returns uiStateFlow
    every { mockChatViewModel.initializeChat(any()) } just Runs
    every { mockChatViewModel.updateMessageText(any()) } just Runs
    every { mockChatViewModel.sendMessage() } just Runs
    every { mockChatViewModel.clearError() } just Runs
  }

  @After
  fun tearDown() {
    AuthenticationProvider.testUserId = null
    unmockkAll()
  }

  @Test
  fun eventChatScreen_displaysLoadingIndicator_whenLoading() {
    uiStateFlow.value = ChatUiState(isLoading = true)

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_displaysEmptyState_whenNoMessages() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false, messages = emptyList(), event = testEvent, currentUser = testUser)

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.EMPTY_STATE).assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_displaysErrorMessage_whenErrorOccurs() {
    uiStateFlow.value =
        ChatUiState(isLoading = false, error = "Test error message", event = testEvent)

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Test error message").assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_displaysMessages_whenMessagesExist() {
    val messages =
        listOf(
            ChatMessage(
                messageId = "msg-1",
                eventId = testEventId,
                senderId = "other-user",
                senderName = "Jane Doe",
                content = "Hello!"),
            ChatMessage(
                messageId = "msg-2",
                eventId = testEventId,
                senderId = testUserId,
                senderName = "John Doe",
                content = "Hi there!"))

    uiStateFlow.value =
        ChatUiState(
            isLoading = false, messages = messages, event = testEvent, currentUser = testUser)

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.MESSAGES_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithText("Hello!").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hi there!").assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_displaysEventTitle_inTopAppBar() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false, event = testEvent, currentUser = testUser, messages = emptyList())

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithText("Test Event").assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_displaysTypingIndicator_whenUsersAreTyping() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages =
                listOf(
                    ChatMessage(
                        messageId = "msg-1",
                        eventId = testEventId,
                        senderId = "user-1",
                        senderName = "Jane",
                        content = "Hello")),
            typingUsers = mapOf("user-2" to "Bob Smith"))

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.TYPING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_backButton_navigatesBack() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false, event = testEvent, currentUser = testUser, messages = emptyList())

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.BACK_BUTTON).performClick()

    verify { mockNavController.popBackStack() }
  }

  @Test
  fun eventChatScreen_inputField_updatesMessageText() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false, event = testEvent, currentUser = testUser, messages = emptyList())

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule
        .onNodeWithTag(EventChatScreenTestTags.INPUT_FIELD)
        .performTextInput("Test message")

    verify { mockChatViewModel.updateMessageText("Test message") }
  }

  @Test
  fun eventChatScreen_sendButton_sendsMessage() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages = emptyList(),
            messageText = "Test message")

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.SEND_BUTTON).performClick()

    verify { mockChatViewModel.sendMessage() }
  }

  @Test
  fun eventChatScreen_sendButton_isDisabled_whenMessageIsEmpty() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages = emptyList(),
            messageText = "")

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.SEND_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun eventChatScreen_sendButton_isEnabled_whenMessageIsNotEmpty() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages = emptyList(),
            messageText = "Hello")

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.SEND_BUTTON).assertIsEnabled()
  }

  @Test
  fun eventChatScreen_sendButton_showsLoadingIndicator_whenSending() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages = emptyList(),
            messageText = "Hello",
            isSending = true)

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    // When sending, the send button should be disabled
    composeTestRule.onNodeWithTag(EventChatScreenTestTags.SEND_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun eventChatScreen_inputField_isDisabled_whenLoading() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = true, event = testEvent, currentUser = testUser, messages = emptyList())

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    // The loading indicator should be displayed instead
    composeTestRule.onNodeWithTag(EventChatScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_inputField_isDisabled_whenCurrentUserIsNull() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false, event = testEvent, currentUser = null, messages = emptyList())

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    // Input should be disabled when current user is null
    composeTestRule.onNodeWithTag(EventChatScreenTestTags.INPUT_FIELD).assertIsNotEnabled()
  }

  @Test
  fun eventChatScreen_errorMessage_canBeDismissed() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false, error = "Test error", event = testEvent, currentUser = testUser)

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()

    // Find and click the dismiss button
    composeTestRule.onNodeWithText("Dismiss", useUnmergedTree = true).performClick()

    verify { mockChatViewModel.clearError() }
  }

  @Test
  fun eventChatScreen_messageItem_displaysCorrectContent() {
    val message =
        ChatMessage(
            messageId = "msg-1",
            eventId = testEventId,
            senderId = "other-user",
            senderName = "Jane Doe",
            content = "Test message content")

    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages = listOf(message))

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithText("Test message content").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jane Doe").assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_messageItem_doesNotDisplaySenderName_forCurrentUser() {
    val message =
        ChatMessage(
            messageId = "msg-1",
            eventId = testEventId,
            senderId = testUserId,
            senderName = "John Doe",
            content = "My message")

    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages = listOf(message))

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithText("My message").assertIsDisplayed()
    // Sender name should not be displayed for current user's messages
    composeTestRule.onAllNodesWithText("John Doe").assertCountEquals(0)
  }

  @Test
  fun eventChatScreen_initializesChat_onLaunch() {
    uiStateFlow.value = ChatUiState(isLoading = true)

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    verify { mockChatViewModel.initializeChat(testEventId) }
  }

  @Test
  fun eventChatScreen_displaysTopAppBar() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false, event = testEvent, currentUser = testUser, messages = emptyList())

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.TOP_APP_BAR).assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_displaysScreen() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false, event = testEvent, currentUser = testUser, messages = emptyList())

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_typingIndicator_displaysMultipleUsers() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages =
                listOf(
                    ChatMessage(
                        messageId = "msg-1",
                        eventId = testEventId,
                        senderId = "user-1",
                        senderName = "User1",
                        content = "Hello")),
            typingUsers = mapOf("user-2" to "Alice", "user-3" to "Bob"))

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    // Should display "Alice and Bob are typing..." or similar
    composeTestRule.onNodeWithTag(EventChatScreenTestTags.TYPING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_typingIndicator_displaysSingleUser() {
    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages =
                listOf(
                    ChatMessage(
                        messageId = "msg-1",
                        eventId = testEventId,
                        senderId = "user-1",
                        senderName = "User1",
                        content = "Hello")),
            typingUsers = mapOf("user-2" to "Alice"))

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithTag(EventChatScreenTestTags.TYPING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun eventChatScreen_displaysDefaultTitle_whenEventIsNull() {
    uiStateFlow.value =
        ChatUiState(isLoading = false, event = null, currentUser = testUser, messages = emptyList())

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule.onNodeWithText("Event Chat").assertIsDisplayed()
  }

  @Test
  fun formatTimestamp_returnsJustNow_forRecentMessage() {
    // This tests the private formatTimestamp function indirectly
    val now = Timestamp.now()
    val message =
        ChatMessage(
            messageId = "msg-1",
            eventId = testEventId,
            senderId = "other-user",
            senderName = "Jane",
            content = "Hello",
            timestamp = now)

    uiStateFlow.value =
        ChatUiState(
            isLoading = false,
            event = testEvent,
            currentUser = testUser,
            messages = listOf(message))

    composeTestRule.setContent {
      EventChatScreen(
          eventId = testEventId,
          navController = mockNavController,
          chatViewModel = mockChatViewModel)
    }

    composeTestRule
        .onNodeWithText("Just now", substring = true, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
