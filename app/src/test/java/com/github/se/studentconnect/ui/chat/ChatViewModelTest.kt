package com.github.se.studentconnect.ui.chat

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

    viewModel = ChatViewModel(mockChatRepository, mockEventRepository, mockUserRepository)
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
}
