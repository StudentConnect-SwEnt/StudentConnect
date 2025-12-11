package com.github.se.studentconnect.ui.screen.home

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.NotificationRepositoryLocal
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenSnackbarTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var notificationRepository: NotificationRepositoryLocal
  private lateinit var viewModel: HomePageViewModel
  private lateinit var notificationViewModel: NotificationViewModel
  private val context: Context = ApplicationProvider.getApplicationContext()

  private fun createEventOnDate(date: Date): Event.Public {
    return Event.Public(
        uid = "event-${date.time}",
        ownerId = "test-owner",
        title = "Test Event",
        description = "Test event description",
        location = Location(0.0, 0.0, "Test Location"),
        start = Timestamp(date),
        end = Timestamp(date),
        participationFee = 0u,
        isFlash = false,
        subtitle = "Test Subtitle",
        tags = listOf("test"),
        imageUrl = null)
  }

  private fun createDateWithOffset(daysOffset: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
    calendar.set(Calendar.HOUR_OF_DAY, 12)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
  }

  @Before
  fun setup() {
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    notificationRepository = NotificationRepositoryLocal()
    viewModel = HomePageViewModel(eventRepository, userRepository)
    notificationViewModel = NotificationViewModel(notificationRepository)
  }

  @Test
  fun snackbar_showsWhenDateHasNoEvents() {
    // Covers: selectedDate != null, previousSelectedDate != selectedDate, eventsForDate.isEmpty(),
    // showSnackbar, delay, dismiss, previousSelectedDate = selectedDate
    val emptyDate = createDateWithOffset(1)

    composeTestRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            viewModel = viewModel,
            notificationViewModel = notificationViewModel)
      }
    }

    composeTestRule.waitForIdle()
    viewModel.onDateSelected(emptyDate)
    composeTestRule.waitForIdle()

    val snackbarMessage = context.getString(R.string.text_no_events_on_date)
    composeTestRule.onNodeWithText(snackbarMessage).assertIsDisplayed()
  }

  @Test
  fun snackbar_doesNotShowWhenDateHasEvents() {
    // Covers: selectedDate != null, previousSelectedDate != selectedDate,
    // eventsForDate.isNotEmpty()
    val eventDate = createDateWithOffset(1)

    runBlocking { eventRepository.addEvent(createEventOnDate(eventDate)) }

    composeTestRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            viewModel = viewModel,
            notificationViewModel = notificationViewModel)
      }
    }

    composeTestRule.waitForIdle()
    viewModel.onDateSelected(eventDate)
    composeTestRule.waitForIdle()

    val snackbarMessage = context.getString(R.string.text_no_events_on_date)
    composeTestRule.onNodeWithText(snackbarMessage).assertDoesNotExist()
  }

  @Test
  fun snackbar_doesNotShowWhenSelectingSameDateAgain() {
    // Covers: selectedDate != null, previousSelectedDate == selectedDate (branch false)
    val emptyDate = createDateWithOffset(1)

    composeTestRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            viewModel = viewModel,
            notificationViewModel = notificationViewModel)
      }
    }

    composeTestRule.waitForIdle()

    // First selection shows snackbar
    viewModel.onDateSelected(emptyDate)
    composeTestRule.waitForIdle()

    val snackbarMessage = context.getString(R.string.text_no_events_on_date)
    composeTestRule.onNodeWithText(snackbarMessage).assertIsDisplayed()

    // Wait for dismissal (2.5 seconds + buffer)
    composeTestRule.waitForIdle()
    Thread.sleep(3000)

    // Second selection of same date should not show snackbar
    viewModel.onDateSelected(emptyDate)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(snackbarMessage).assertDoesNotExist()
  }

  @Test
  fun snackbar_doesNotShowOnInitialLoad() {
    // Covers: selectedDate == null initially, else if branch, previousSelectedDate = null
    composeTestRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            viewModel = viewModel,
            notificationViewModel = notificationViewModel)
      }
    }

    composeTestRule.waitForIdle()

    val snackbarMessage = context.getString(R.string.text_no_events_on_date)
    composeTestRule.onNodeWithText(snackbarMessage).assertDoesNotExist()
  }

  @Test
  fun snackbar_autoDismissesAfterDelay() {
    // Covers: delay(2500) and dismiss() execution
    val emptyDate = createDateWithOffset(1)

    composeTestRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            viewModel = viewModel,
            notificationViewModel = notificationViewModel)
      }
    }

    composeTestRule.waitForIdle()
    viewModel.onDateSelected(emptyDate)
    composeTestRule.waitForIdle()

    val snackbarMessage = context.getString(R.string.text_no_events_on_date)
    composeTestRule.onNodeWithText(snackbarMessage).assertIsDisplayed()

    // Wait for auto-dismiss (2.5 seconds + buffer)
    composeTestRule.waitForIdle()
    Thread.sleep(3000)

    composeTestRule.onNodeWithText(snackbarMessage).assertDoesNotExist()
  }
}
