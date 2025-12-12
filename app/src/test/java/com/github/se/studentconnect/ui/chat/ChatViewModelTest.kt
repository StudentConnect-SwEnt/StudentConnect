package com.github.se.studentconnect.ui.chat

import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.chat.ChatMessage
import com.github.se.studentconnect.model.chat.ChatRepository
import com.github.se.studentconnect.model.chat.ChatRepositoryProvider
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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

  private lateinit var viewModel: ChatViewModel
  private lateinit var mockChatRepository: ChatRepository
  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockUserRepository: UserRepository
  private val testDispatcher = StandardTestDispatcher()
  private val testScheduler
    get() = testDispatcher.scheduler

  private val testUserId = "user-123"
  private val testEventId = "event-456"

  // Mock getString function that returns proper error messages for tests
  private val mockGetString: (Int) -> String = { resId ->
    when (resId) {
      R.string.error_failed_to_load_event -> "Failed to load event: %s"
      R.string.error_failed_to_load_user -> "Failed to load user: %s"
      R.string.error_failed_to_send_message -> "Failed to send message: %s"
      else -> ""
    }
  }

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
    ChatRepositoryProvider.cleanOverrideForTests()
    EventRepositoryProvider.cleanOverrideForTests()
    UserRepositoryProvider.cleanOverrideForTests()

    Dispatchers.setMain(testDispatcher)

    mockChatRepository = mock(ChatRepository::class.java)
    mockEventRepository = mock(EventRepository::class.java)
    mockUserRepository = mock(UserRepository::class.java)

    `when`(mockChatRepository.observeMessages(ArgumentMatchers.anyString()))
        .thenReturn(flowOf(emptyList()))
    `when`(mockChatRepository.observeTypingUsers(ArgumentMatchers.anyString()))
        .thenReturn(flowOf(emptyList()))
    `when`(mockChatRepository.getNewMessageId()).thenReturn("new-msg-id")

    AuthenticationProvider.testUserId = testUserId

    ChatRepositoryProvider.overrideForTests(mockChatRepository)
    EventRepositoryProvider.overrideForTests(mockEventRepository)
    UserRepositoryProvider.overrideForTests(mockUserRepository)

    viewModel = ChatViewModel()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    AuthenticationProvider.testUserId = null
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
  fun updateMessageText_updatesState() {
    viewModel.updateMessageText("Hello World!")

    val state = viewModel.uiState.value
    assertEquals("Hello World!", state.messageText)
  }

  @Test
  fun clearError_clearsErrorMessage() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenThrow(RuntimeException("Error"))

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    assertNotNull(viewModel.uiState.value.error)

    viewModel.clearError()

    assertNull(viewModel.uiState.value.error)
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
  fun initializeChat_handlesNullCurrentUser() = runTest {
    AuthenticationProvider.testUserId = null
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.currentUser)
  }

  @Test
  fun sendMessage_doesNotSendBlankMessage() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("   ")
    viewModel.sendMessage()

    val state = viewModel.uiState.value
    assertFalse(state.isSending)
  }

  @Test
  fun sendMessage_doesNotSendWithoutCurrentUser() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    AuthenticationProvider.testUserId = null

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello")
    viewModel.sendMessage()

    val state = viewModel.uiState.value
    assertFalse(state.isSending)
  }

  @Test
  fun sendMessage_doesNotSendWithoutEvent() = runTest {
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.updateMessageText("Hello")
    viewModel.sendMessage()

    val state = viewModel.uiState.value
    assertFalse(state.isSending)
  }

  @Test
  fun sendMessage_successfullySendsMessage() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .sendMessage(any(), any(), any())

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello World!")

    viewModel.sendMessage()
    advanceUntilIdle()

    assertEquals("", viewModel.uiState.value.messageText)
    assertFalse(viewModel.uiState.value.isSending)
  }

  @Test
  fun sendMessage_handlesFailure() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Network error"))
          null
        }
        .`when`(mockChatRepository)
        .sendMessage(any(), any(), any())

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello World!")
    viewModel.sendMessage()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("Failed to send message"))
    assertFalse(state.isSending)
  }

  @Test
  fun updateMessageText_withBlankText_stopsTyping() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello")
    viewModel.updateMessageText("")

    val state = viewModel.uiState.value
    assertEquals("", state.messageText)
  }

  @Test
  fun updateMessageText_withNonBlankText_startsTyping() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello World!")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Hello World!", state.messageText)
  }

  @Test
  fun initializeChat_filtersOutCurrentUserFromTypingStatus() = runTest {
    val typingUsers =
        listOf(
            com.github.se.studentconnect.model.chat.TypingStatus(
                userId = testUserId, userName = "John Doe", eventId = testEventId, isTyping = true),
            com.github.se.studentconnect.model.chat.TypingStatus(
                userId = "other-user",
                userName = "Jane Smith",
                eventId = testEventId,
                isTyping = true))

    `when`(mockChatRepository.observeTypingUsers(testEventId)).thenReturn(flowOf(typingUsers))
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.typingUsers.size)
    assertTrue(state.typingUsers.containsKey("other-user"))
  }

  @Test
  fun viewModel_initializesMultipleTimes_cancelsOldJobs() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    // Initialize again with the same event
    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    // Verify the view model handles multiple initializations without crashes
    val state = viewModel.uiState.value
    assertNotNull(state.event)
    assertEquals(testEventId, state.event?.uid)
  }

  @Test
  fun sendMessage_trimsMessageContent() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    doAnswer { invocation ->
          val message = invocation.getArgument<ChatMessage>(0)
          assertEquals("Hello", message.content)
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(mockChatRepository)
        .sendMessage(any(), any(), any())

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("  Hello  ")
    viewModel.sendMessage()
    advanceUntilIdle()
  }

  @Test
  fun updateMessageText_withoutCurrentUser_doesNotUpdateTypingStatus() = runTest {
    val tempViewModel = ChatViewModel()
    AuthenticationProvider.testUserId = null

    tempViewModel.updateMessageText("Hello")

    // Should not crash
    assertEquals("Hello", tempViewModel.uiState.value.messageText)
  }

  @Test
  fun updateMessageText_withoutEvent_doesNotUpdateTypingStatus() = runTest {
    val tempViewModel = ChatViewModel()
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    tempViewModel.updateMessageText("Hello")

    // Should not crash
    assertEquals("Hello", tempViewModel.uiState.value.messageText)
  }

  @Test
  fun onCleared_cancelsAllJobs() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    // Call onCleared through reflection since it's protected
    val onClearedMethod = viewModel.javaClass.superclass.getDeclaredMethod("onCleared")
    onClearedMethod.isAccessible = true
    onClearedMethod.invoke(viewModel)

    // The method should not crash and should clean up properly
    advanceUntilIdle()
  }

  @Test
  fun updateMessageText_afterDebounceDelay_stopsTypingStatus() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello World!")

    // Advance time by 2000ms to trigger debounce
    testScheduler.advanceTimeBy(2100)
    advanceUntilIdle()

    // The typing status should have been updated to false after debounce
    val state = viewModel.uiState.value
    assertEquals("Hello World!", state.messageText)
  }

  @Test
  fun sendMessage_updatesIsSendingState() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    // Make sendMessage hang by not calling success or failure immediately
    doAnswer { invocation ->
          // Don't call callbacks immediately
          null
        }
        .`when`(mockChatRepository)
        .sendMessage(any(), any(), any())

    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    viewModel.updateMessageText("Hello World!")

    // Start sending without waiting
    viewModel.sendMessage()

    // isSending should be true immediately after calling sendMessage
    assertTrue(viewModel.uiState.value.isSending)
  }

  @Test
  fun initializeChat_cancelsExistingJobsBeforeStartingNew() = runTest {
    `when`(mockEventRepository.getEvent(testEventId)).thenReturn(testEvent)
    `when`(mockUserRepository.getUserById(testUserId)).thenReturn(testUser)

    // Initialize first time
    viewModel.initializeChat(testEventId)
    advanceUntilIdle()

    val firstEvent = viewModel.uiState.value.event

    // Initialize second time with different event
    val newEventId = "event-789"
    val newEvent =
        Event.Private(
            uid = newEventId,
            ownerId = "owner-456",
            title = "New Test Event",
            description = "New Description",
            start = com.google.firebase.Timestamp.now(),
            end = com.google.firebase.Timestamp.now(),
            location = com.github.se.studentconnect.model.location.Location(46.5, 6.6, "EPFL"),
            isFlash = false)

    `when`(mockEventRepository.getEvent(newEventId)).thenReturn(newEvent)
    `when`(mockChatRepository.observeMessages(newEventId)).thenReturn(flowOf(emptyList()))
    `when`(mockChatRepository.observeTypingUsers(newEventId)).thenReturn(flowOf(emptyList()))

    viewModel.initializeChat(newEventId)
    advanceUntilIdle()

    // Should have the new event
    assertNotNull(viewModel.uiState.value.event)
    assertEquals(newEventId, viewModel.uiState.value.event?.uid)
  }
}
