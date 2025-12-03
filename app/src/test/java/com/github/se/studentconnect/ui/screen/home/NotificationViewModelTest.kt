package com.github.se.studentconnect.ui.screen.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  @Mock private lateinit var mockRepository: NotificationRepository

  private val testDispatcher = StandardTestDispatcher()

  private val testUserId = "test-user-123"

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)

    // Setup AuthenticationProvider with test user
    AuthenticationProvider.testUserId = testUserId

    // Setup default mock behavior
    Mockito.`when`(mockRepository.listenToNotifications(any(), any())).thenAnswer { {} }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    AuthenticationProvider.testUserId = null
  }

  @Test
  fun notificationUiState_hasCorrectDefaults() {
    val uiState = NotificationUiState()

    assert(uiState.notifications.isEmpty()) { "Default notifications should be empty" }
    assert(uiState.unreadCount == 0) { "Default unread count should be 0" }
    assert(uiState.isLoading) { "Default loading state should be true" }
  }

  @Test
  fun notificationUiState_copyWorks() {
    val notification =
        Notification.FriendRequest(
            id = "1",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John",
            timestamp = Timestamp.Companion.now(),
            isRead = false)

    val original = NotificationUiState()
    val updated =
        original.copy(notifications = listOf(notification), unreadCount = 1, isLoading = false)

    assert(updated.notifications.size == 1) { "Updated should have 1 notification" }
    assert(updated.unreadCount == 1) { "Updated should have unread count 1" }
    assert(!updated.isLoading) { "Updated should not be loading" }
    assert(original.notifications.isEmpty()) { "Original should remain unchanged" }
  }

  @Test
  fun notificationUiState_calculatesUnreadCountCorrectly() {
    val notifications =
        listOf(
            Notification.FriendRequest(
                id = "1",
                userId = "user-1",
                fromUserId = "user-2",
                fromUserName = "John",
                timestamp = Timestamp.Companion.now(),
                isRead = false),
            Notification.FriendRequest(
                id = "2",
                userId = "user-1",
                fromUserId = "user-3",
                fromUserName = "Jane",
                timestamp = Timestamp.Companion.now(),
                isRead = true),
            Notification.EventStarting(
                id = "3",
                userId = "user-1",
                eventId = "event-1",
                eventTitle = "Test Event",
                eventStart = Timestamp.Companion.now(),
                timestamp = Timestamp.Companion.now(),
                isRead = false))

    val unreadCount = notifications.count { !it.isRead }

    assert(unreadCount == 2) { "Should have 2 unread notifications" }
  }

  @Test
  fun notificationUiState_withAllRead_hasZeroUnreadCount() {
    val notifications =
        listOf(
            Notification.FriendRequest(
                id = "1",
                userId = "user-1",
                fromUserId = "user-2",
                fromUserName = "John",
                timestamp = Timestamp.Companion.now(),
                isRead = true),
            Notification.EventStarting(
                id = "2",
                userId = "user-1",
                eventId = "event-1",
                eventTitle = "Test Event",
                eventStart = Timestamp.Companion.now(),
                timestamp = Timestamp.Companion.now(),
                isRead = true))

    val unreadCount = notifications.count { !it.isRead }

    assert(unreadCount == 0) { "Should have 0 unread notifications when all are read" }
  }

  @Test
  fun notificationUiState_withMixedNotifications_tracksCorrectly() {
    val friendRequest =
        Notification.FriendRequest(
            id = "1",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John",
            timestamp = Timestamp.Companion.now(),
            isRead = false)

    val eventStarting =
        Notification.EventStarting(
            id = "2",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Basketball",
            eventStart = Timestamp.Companion.now(),
            timestamp = Timestamp.Companion.now(),
            isRead = false)

    val notifications = listOf(friendRequest, eventStarting)
    val uiState =
        NotificationUiState(notifications = notifications, unreadCount = 2, isLoading = false)

    assert(uiState.notifications.size == 2) { "Should have 2 notifications" }
    assert(uiState.notifications[0] is Notification.FriendRequest) {
      "First should be FriendRequest"
    }
    assert(uiState.notifications[1] is Notification.EventStarting) {
      "Second should be EventStarting"
    }
  }

  @Test
  fun viewModel_initialization_startsListening() = runTest {
    val notifications =
        listOf(
            Notification.FriendRequest(
                id = "1",
                userId = testUserId,
                fromUserId = "user-2",
                fromUserName = "John",
                timestamp = Timestamp.Companion.now(),
                isRead = false))

    Mockito.`when`(mockRepository.listenToNotifications(eq(testUserId), any())).thenAnswer {
      val callback = it.getArgument<(List<Notification>) -> Unit>(1)
      callback(notifications)
      return@thenAnswer {}
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    Mockito.verify(mockRepository).listenToNotifications(eq(testUserId), any())
    Assert.assertEquals(1, viewModel.uiState.value.notifications.size)
    Assert.assertEquals(1, viewModel.uiState.value.unreadCount)
    Assert.assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun viewModel_initialization_withEmptyUserId_doesNotListen() = runTest {
    AuthenticationProvider.testUserId = ""

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    Mockito.verify(mockRepository, never()).listenToNotifications(any(), any())
    Assert.assertTrue(viewModel.uiState.value.notifications.isEmpty())
    Assert.assertTrue(viewModel.uiState.value.isLoading)
  }

  @Test
  fun markAsRead_callsRepositoryWithCorrectId() = runTest {
    Mockito.`when`(mockRepository.markAsRead(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.markAsRead("notif-123")
    advanceUntilIdle()

    Mockito.verify(mockRepository).markAsRead(eq("notif-123"), any(), any())
  }

  @Test
  fun markAsRead_onFailure_logsError() = runTest {
    val testException = Exception("Mark as read failed")
    Mockito.`when`(mockRepository.markAsRead(any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(testException)
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.markAsRead("notif-123")
    advanceUntilIdle()

    Mockito.verify(mockRepository).markAsRead(eq("notif-123"), any(), any())
  }

  @Test
  fun markAllAsRead_callsRepositoryWithUserId() = runTest {
    Mockito.`when`(mockRepository.markAllAsRead(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.markAllAsRead()
    advanceUntilIdle()

    Mockito.verify(mockRepository).markAllAsRead(eq(testUserId), any(), any())
  }

  @Test
  fun markAllAsRead_withEmptyUserId_doesNotCallRepository() = runTest {
    AuthenticationProvider.testUserId = ""

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.markAllAsRead()
    advanceUntilIdle()

    Mockito.verify(mockRepository, never()).markAllAsRead(any(), any(), any())
  }

  @Test
  fun markAllAsRead_onFailure_logsError() = runTest {
    val testException = Exception("Mark all as read failed")
    Mockito.`when`(mockRepository.markAllAsRead(any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(testException)
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.markAllAsRead()
    advanceUntilIdle()

    Mockito.verify(mockRepository).markAllAsRead(eq(testUserId), any(), any())
  }

  @Test
  fun deleteNotification_callsRepositoryWithCorrectId() = runTest {
    Mockito.`when`(mockRepository.deleteNotification(any(), any(), any())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.deleteNotification("notif-456")
    advanceUntilIdle()

    Mockito.verify(mockRepository).deleteNotification(eq("notif-456"), any(), any())
  }

  @Test
  fun deleteNotification_onFailure_logsError() = runTest {
    val testException = Exception("Delete failed")
    Mockito.`when`(mockRepository.deleteNotification(any(), any(), any())).thenAnswer { invocation
      ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(testException)
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.deleteNotification("notif-456")
    advanceUntilIdle()

    Mockito.verify(mockRepository).deleteNotification(eq("notif-456"), any(), any())
  }

  @Test
  fun refresh_loadsNotificationsFromRepository() = runTest {
    val notifications =
        listOf(
            Notification.EventStarting(
                id = "2",
                userId = testUserId,
                eventId = "event-1",
                eventTitle = "Tech Meetup",
                eventStart = Timestamp.Companion.now(),
                timestamp = Timestamp.Companion.now(),
                isRead = true))

    Mockito.`when`(mockRepository.getNotifications(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Notification>) -> Unit>(1)
      onSuccess(notifications)
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.refresh()
    advanceUntilIdle()

    Mockito.verify(mockRepository).getNotifications(eq(testUserId), any(), any())
  }

  @Test
  fun refresh_withEmptyUserId_doesNotCallRepository() = runTest {
    AuthenticationProvider.testUserId = ""

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.refresh()
    advanceUntilIdle()

    Mockito.verify(mockRepository, never()).getNotifications(any(), any(), any())
  }

  @Test
  fun refresh_onSuccess_updatesUiState() = runTest {
    val notifications =
        listOf(
            Notification.FriendRequest(
                id = "1",
                userId = testUserId,
                fromUserId = "user-2",
                fromUserName = "Jane",
                timestamp = Timestamp.Companion.now(),
                isRead = false),
            Notification.FriendRequest(
                id = "2",
                userId = testUserId,
                fromUserId = "user-3",
                fromUserName = "Bob",
                timestamp = Timestamp.Companion.now(),
                isRead = true))

    Mockito.`when`(mockRepository.getNotifications(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Notification>) -> Unit>(1)
      onSuccess(notifications)
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.refresh()
    advanceUntilIdle()

    Assert.assertEquals(2, viewModel.uiState.value.notifications.size)
    Assert.assertEquals(1, viewModel.uiState.value.unreadCount)
    Assert.assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun refresh_onFailure_stopsLoading() = runTest {
    val testException = Exception("Load failed")
    Mockito.`when`(mockRepository.getNotifications(any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(testException)
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.refresh()
    advanceUntilIdle()

    Assert.assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun initialization_setsUpListener() = runTest {
    var listenerSetup = false
    Mockito.`when`(mockRepository.listenToNotifications(eq(testUserId), any())).thenAnswer {
      listenerSetup = true
      {}
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    Assert.assertTrue(listenerSetup)
    Mockito.verify(mockRepository).listenToNotifications(eq(testUserId), any())
  }

  @Test
  fun listener_updatesUiStateWithUnreadCount() = runTest {
    val captor = argumentCaptor<(List<Notification>) -> Unit>()

    Mockito.`when`(mockRepository.listenToNotifications(eq(testUserId), captor.capture()))
        .thenReturn {}

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    val notifications =
        listOf(
            Notification.FriendRequest(
                id = "1",
                userId = testUserId,
                fromUserId = "user-2",
                fromUserName = "Alice",
                timestamp = Timestamp.Companion.now(),
                isRead = false),
            Notification.FriendRequest(
                id = "2",
                userId = testUserId,
                fromUserId = "user-3",
                fromUserName = "Bob",
                timestamp = Timestamp.Companion.now(),
                isRead = false),
            Notification.EventStarting(
                id = "3",
                userId = testUserId,
                eventId = "event-1",
                eventTitle = "Workshop",
                eventStart = Timestamp.Companion.now(),
                timestamp = Timestamp.Companion.now(),
                isRead = true))

    captor.firstValue.invoke(notifications)

    Assert.assertEquals(3, viewModel.uiState.value.notifications.size)
    Assert.assertEquals(2, viewModel.uiState.value.unreadCount)
    Assert.assertFalse(viewModel.uiState.value.isLoading)
  }
}
