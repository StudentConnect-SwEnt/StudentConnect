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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelLatestNotificationTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  @Mock private lateinit var mockRepository: NotificationRepository

  private val testDispatcher = StandardTestDispatcher()
  private val testUserId = "test-user-123"

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)
    AuthenticationProvider.testUserId = testUserId
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    AuthenticationProvider.testUserId = null
  }

  @Test
  fun latestNotification_isNull_byDefault() = runTest {
    Mockito.`when`(mockRepository.listenToNotifications(any(), any())).thenAnswer {
      val callback = it.getArgument<(List<Notification>) -> Unit>(1)
      callback(emptyList());
      {}
    }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assert(uiState.latestNotification == null) { "Latest notification should be null by default" }
  }

  @Test
  fun clearLatestNotification_setsLatestNotificationToNull() = runTest {
    val notification1 =
        Notification.FriendRequest(
            id = "notif-1",
            userId = testUserId,
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    val notification2 =
        Notification.FriendRequest(
            id = "notif-2",
            userId = testUserId,
            fromUserId = "user-3",
            fromUserName = "Jane Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    val callbackCaptor = argumentCaptor<(List<Notification>) -> Unit>()
    Mockito.`when`(mockRepository.listenToNotifications(any(), callbackCaptor.capture()))
        .thenAnswer { {} }

    val viewModel = NotificationViewModel(mockRepository)
    advanceUntilIdle()

    // Simulate initial state with existing notification, then add a new one
    // The ViewModel only sets latestNotification when there are NEW notifications AND previous ones
    // exist
    callbackCaptor.firstValue.invoke(listOf(notification1))
    advanceUntilIdle()
    callbackCaptor.firstValue.invoke(listOf(notification1, notification2))
    advanceUntilIdle()

    var uiState = viewModel.uiState.value
    assert(uiState.latestNotification != null) { "Should have latest notification" }

    // Clear latest notification
    viewModel.clearLatestNotification()
    advanceUntilIdle()

    uiState = viewModel.uiState.value
    assert(uiState.latestNotification == null) {
      "Latest notification should be null after clearing"
    }
  }
}
