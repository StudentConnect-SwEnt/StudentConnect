package com.github.se.studentconnect.ui.chat

import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.chat.ChatMessage
import com.github.se.studentconnect.model.chat.ChatRepository
import com.github.se.studentconnect.model.chat.ChatRepositoryProvider
import com.github.se.studentconnect.model.chat.TypingStatus
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

  private lateinit var viewModel: ChatViewModel
  private lateinit var mockChatRepository: ChatRepository
  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockUserRepository: UserRepository
  private val testDispatcher = StandardTestDispatcher()

  private val testUserId = "user-123"
  private val testEventId = "event-456"

  private val testUser =
      User(
          userId = testUserId,
          firstName = "John",
          lastName = "Doe",
          email = "john@example.com",
          profilePictureUrl = null,
          bio = "",
          socialMediaLinks = emptyMap(),
          organizationsOwned = emptyList(),
          organizationsJoined = emptyList(),
          organizationMembershipRequests = emptyList(),
          organizationMembershipInvitations = emptyList(),
          eventsCreated = emptyList(),
          eventsJoined = emptyList(),
          eventsInvited = emptyList())

  private val testEvent =
      Event.Private(
          uid = testEventId,
          ownerId = "owner-123",
          title = "Test Event",
          description = "Test Description",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(46.5, 6.6, "EPFL"))

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    mockChatRepository = mock(ChatRepository::class.java)
    mockEventRepository = mock(EventRepository::class.java)
    mockUserRepository = mock(UserRepository::class.java)

    // Setup default mocks
    `when`(mockChatRepository.observeMessages(anyString())).thenReturn(flowOf(emptyList()))
    `when`(mockChatRepository.observeTypingUsers(anyString())).thenReturn(flowOf(emptyList()))
    `when`(mockChatRepository.getNewMessageId()).thenReturn("new-msg-id")

    // Mock AuthenticationProvider
    AuthenticationProvider.currentUser = testUserId

    // Override providers
    ChatRepositoryProvider.overrideForTests(mockChatRepository)
    EventRepositoryProvider.overrideForTests(mockEventRepository)
    UserRepositoryProvider.overrideForTests(mockUserRepository)

    viewModel = ChatViewModel(mockChatRepository, mockEventRepository, mockUserRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    AuthenticationProvider.currentUser = null
    ChatRepositoryProvider.cleanOverrideForTests()
    EventRepositoryProvider.cleanOverrideForTests()
    UserRepositoryProvider.cleanOverrideForTests()
  }

  @Test
  fun initialState_hasDefaultValues() {
    val state = viewModel.uiState.value

    assertNull(state.event)
    assertTrue(state.messages.isEmpty())
    assertNull(state.currentUser)
    assertTrue(state.typingUsers.isEmpty())
    assertEquals("", state.messageText)
    assertTrue(state.isLoading)
    assertNull(state.error)
    assertFalse(state.isSending)
  }

  @Test
  fun initializeChat_loadsEventAndUser() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.event)
    assertEquals(testEvent.uid, state.event?.uid)
    assertNotNull(state.currentUser)
    assertEquals(testUserId, state.currentUser?.userId)
    assertFalse(state.isLoading)
  }

  @Test
  fun initializeChat_handlesEventLoadFailure() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenThrow(RuntimeException("Event not found"))
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("Failed to load event"))
  }

  @Test
  fun initializeChat_handlesUserLoadFailure() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenThrow(RuntimeException("User not found"))

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("Failed to load user"))
  }

  @Test
  fun initializeChat_observesMessages() = runTest {
    val messages =
        listOf(
            ChatMessage(
                messageId = "msg-1",
                eventId = testEventId,
                senderId = "user-1",
                senderName = "Jane Doe",
                content = "Hello!"))

    `when`(mockChatRepository.observeMessages(testEventId)).thenReturn(flowOf(messages))
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.messages.size)
    assertEquals("msg-1", state.messages[0].messageId)
  }

  @Test
  fun initializeChat_observesTypingUsers() = runTest {
    val typingUsers =
        listOf(
            TypingStatus(
                userId = "user-2", userName = "Jane Doe", eventId = testEventId, isTyping = true))

    `when`(mockChatRepository.observeTypingUsers(testEventId)).thenReturn(flowOf(typingUsers))
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.typingUsers.size)
    assertEquals("user-2", state.typingUsers[0].userId)
  }

  @Test
  fun initializeChat_filtersOutCurrentUserFromTypingIndicators() = runTest {
    val typingUsers =
        listOf(
            TypingStatus(
                userId = testUserId, // Current user
                userName = "John Doe",
                eventId = testEventId,
                isTyping = true),
            TypingStatus(
                userId = "user-2", userName = "Jane Doe", eventId = testEventId, isTyping = true))

    `when`(mockChatRepository.observeTypingUsers(testEventId)).thenReturn(flowOf(typingUsers))
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should only have user-2, current user filtered out
    assertEquals(1, state.typingUsers.size)
    assertEquals("user-2", state.typingUsers[0].userId)
  }

  @Test
  fun updateMessageText_updatesState() {
    viewModel.updateMessageText("Hello World!")

    val state = viewModel.uiState.value
    assertEquals("Hello World!", state.messageText)
  }

  @Test
  fun updateMessageText_withNonBlankText_sendsTypingStatus() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    // Setup capture for typing status
    val captor = ArgumentCaptor.forClass(TypingStatus::class.java)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .updateTypingStatus(captor.capture(), any(), any())

    viewModel.updateMessageText("Hello")
    advanceUntilIdle()

    verify(mockChatRepository).updateTypingStatus(any(), any(), any())
    val capturedStatus = captor.value
    assertTrue(capturedStatus.isTyping)
    assertEquals(testUserId, capturedStatus.userId)
  }

  @Test
  fun updateMessageText_withBlankText_stopsTypingStatus() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val captor = ArgumentCaptor.forClass(TypingStatus::class.java)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .updateTypingStatus(captor.capture(), any(), any())

    viewModel.updateMessageText("")
    advanceUntilIdle()

    verify(mockChatRepository).updateTypingStatus(any(), any(), any())
    val capturedStatus = captor.value
    assertFalse(capturedStatus.isTyping)
  }

  @Test
  fun updateMessageText_debounceStopsTypingAfter2Seconds() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val captor = ArgumentCaptor.forClass(TypingStatus::class.java)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .updateTypingStatus(captor.capture(), any(), any())

    viewModel.updateMessageText("Hello")
    advanceUntilIdle()

    // Advance time by 2 seconds
    advanceTimeBy(2000)

    // Should have called updateTypingStatus twice: once for typing, once for stopping
    verify(mockChatRepository, org.mockito.Mockito.atLeast(2))
        .updateTypingStatus(any(), any(), any())
    val lastStatus = captor.allValues.last()
    assertFalse(lastStatus.isTyping)
  }

  @Test
  fun sendMessage_withValidMessage_callsRepository() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello World!")

    val captor = ArgumentCaptor.forClass(ChatMessage::class.java)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .sendMessage(captor.capture(), any(), any())

    var callbackCalled = false
    viewModel.sendMessage { callbackCalled = true }
    advanceUntilIdle()

    verify(mockChatRepository).sendMessage(any(), any(), any())
    val sentMessage = captor.value
    assertEquals("Hello World!", sentMessage.content)
    assertEquals(testUserId, sentMessage.senderId)
    assertEquals(testEventId, sentMessage.eventId)
    assertTrue(callbackCalled)
  }

  @Test
  fun sendMessage_clearsMessageTextOnSuccess() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello World!")

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .sendMessage(any(), any(), any())

    viewModel.sendMessage()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("", state.messageText)
    assertFalse(state.isSending)
  }

  @Test
  fun sendMessage_withBlankMessage_doesNotSend() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("   ")
    viewModel.sendMessage()
    advanceUntilIdle()

    verify(mockChatRepository, never()).sendMessage(any(), any(), any())
  }

  @Test
  fun sendMessage_withoutCurrentUser_doesNotSend() = runTest {
    // Don't initialize chat, so currentUser is null
    viewModel.updateMessageText("Hello")
    viewModel.sendMessage()
    advanceUntilIdle()

    verify(mockChatRepository, never()).sendMessage(any(), any(), any())
  }

  @Test
  fun sendMessage_withoutEvent_doesNotSend() = runTest {
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    // Initialize chat but event loading fails
    `when`(mockEventRepository.getEvent(testEventId)).thenThrow(RuntimeException("Event not found"))

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello")
    viewModel.sendMessage()
    advanceUntilIdle()

    verify(mockChatRepository, never()).sendMessage(any(), any(), any())
  }

  @Test
  fun sendMessage_onFailure_setsError() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello World!")

    val exception = Exception("Network error")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .`when`(mockChatRepository)
        .sendMessage(any(), any(), any())

    viewModel.sendMessage()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("Failed to send message"))
    assertFalse(state.isSending)
  }

  @Test
  fun sendMessage_trimsWhitespace() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("  Hello World!  ")

    val captor = ArgumentCaptor.forClass(ChatMessage::class.java)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .sendMessage(captor.capture(), any(), any())

    viewModel.sendMessage()
    advanceUntilIdle()

    val sentMessage = captor.value
    assertEquals("Hello World!", sentMessage.content)
  }

  @Test
  fun clearError_clearsErrorMessage() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenThrow(RuntimeException("Error"))

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    // Error should be set
    assertNotNull(viewModel.uiState.value.error)

    viewModel.clearError()

    // Error should be cleared
    assertNull(viewModel.uiState.value.error)
  }

  @Test
  fun onCleared_stopsTypingIndicator() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val captor = ArgumentCaptor.forClass(TypingStatus::class.java)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .updateTypingStatus(captor.capture(), any(), any())

    // Trigger onCleared by calling it directly (simulates ViewModel destruction)
    viewModel.onCleared()
    advanceUntilIdle()

    // Should have sent isTyping = false
    val capturedStatuses = captor.allValues
    val lastStatus = capturedStatuses.last()
    assertFalse(lastStatus.isTyping)
  }

  @Test
  fun sendMessage_stopsTypingIndicator() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello")

    val typingCaptor = ArgumentCaptor.forClass(TypingStatus::class.java)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .updateTypingStatus(typingCaptor.capture(), any(), any())

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .sendMessage(any(), any(), any())

    viewModel.sendMessage()
    advanceUntilIdle()

    // Should have stopped typing after sending
    val capturedStatuses = typingCaptor.allValues
    val lastStatus = capturedStatuses.last()
    assertFalse(lastStatus.isTyping)
  }

  @Test
  fun initializeChat_withNullCurrentUser_skipsUserLoad() = runTest {
    AuthenticationProvider.currentUser = null

    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    // Should not attempt to load user
    verify(mockUserRepository, never()).getUserById(anyString())

    val state = viewModel.uiState.value
    assertNull(state.currentUser)
  }

  @Test
  fun updateMessageText_withoutEventOrUser_doesNotSendTypingStatus() = runTest {
    // Don't initialize chat
    viewModel.updateMessageText("Hello")
    advanceUntilIdle()

    verify(mockChatRepository, never()).updateTypingStatus(any(), any(), any())
  }

  @Test
  fun sendMessage_setsIsSendingFlag() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello")

    // Don't call success or failure callbacks to keep isSending true
    doAnswer { null }.`when`(mockChatRepository).sendMessage(any(), any(), any())

    viewModel.sendMessage()

    // isSending should be true before callbacks are invoked
    assertTrue(viewModel.uiState.value.isSending)
  }
}
