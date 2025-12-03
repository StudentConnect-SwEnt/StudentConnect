package com.github.se.studentconnect.ui.screen.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.github.se.studentconnect.ui.event.CountDownViewModel
import com.github.se.studentconnect.ui.event.TimeUnitBox
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CountDownDisplayTest {

  @get:Rule val composeTestRule = createComposeRule()

  // TODO : commented out because of ressource time out when ran in CI.
  //  @Test
  //  fun testCountDownDisplayShowsFullTime() {
  //    // 2 hours, 30 minutes, 45 seconds
  //    composeTestRule.setContent { CountDownDisplay(timeLeft = 9045) }
  //
  //    composeTestRule.onNodeWithText("02").assertIsDisplayed()
  //    composeTestRule.onNodeWithText("30").assertIsDisplayed()
  //    composeTestRule.onNodeWithText("45").assertIsDisplayed()
  //    composeTestRule.onAllNodesWithText(":").assertCountEquals(2)
  //  }

  @Test
  fun testTimeUnitBoxDisplaysPaddedValue() {
    composeTestRule.setContent { TimeUnitBox(timeLeft = "5") }
    composeTestRule.onNodeWithText("05").assertIsDisplayed()
  }

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: CountDownViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    viewModel = CountDownViewModel()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test fun testInitialTimeLeftIsZero() = runTest { assertEquals(0L, viewModel.timeLeft.value) }

  @Test
  fun testStartCountdownWithFutureTimestamp() = runTest {
    val futureTime = Timestamp(Date.from(Instant.now().plus(10, ChronoUnit.SECONDS)))

    viewModel.startCountdown(futureTime)
    testDispatcher.scheduler.advanceTimeBy(100)

    val timeLeft = viewModel.timeLeft.value
    assertEquals(
        true,
        timeLeft > 0L,
    )
    assertEquals(true, timeLeft <= 10L)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testStartCountdownWithPastTimestamp() = runTest {
    val pastTime = Timestamp(Date.from(Instant.now().minus(5, ChronoUnit.SECONDS)))

    viewModel.startCountdown(pastTime)
    testDispatcher.scheduler.advanceTimeBy(100)

    assertEquals(0L, viewModel.timeLeft.value)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testStartCountdownCancelsOldCountdown() = runTest {
    val firstTime = Timestamp(Date.from(Instant.now().plus(20, ChronoUnit.SECONDS)))
    viewModel.startCountdown(firstTime)
    testDispatcher.scheduler.advanceTimeBy(100)
    val firstValue = viewModel.timeLeft.value

    val secondTime = Timestamp(Date.from(Instant.now().plus(10, ChronoUnit.SECONDS)))
    viewModel.startCountdown(secondTime)
    testDispatcher.scheduler.advanceTimeBy(100)
    val secondValue = viewModel.timeLeft.value

    assertEquals(true, secondValue < firstValue)
    assertEquals(true, secondValue <= 10L)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testCountdownStopsAtZero() = runTest {
    val futureTime = Timestamp(Date.from(Instant.now().plus(1, ChronoUnit.SECONDS)))

    viewModel.startCountdown(futureTime)
    testDispatcher.scheduler.advanceTimeBy(2000)

    assertEquals(0L, viewModel.timeLeft.value)

    testDispatcher.scheduler.advanceTimeBy(1000)
    assertEquals(0L, viewModel.timeLeft.value)
  }
}
