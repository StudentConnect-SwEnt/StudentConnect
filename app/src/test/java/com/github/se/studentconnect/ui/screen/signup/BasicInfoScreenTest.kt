package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BasicInfoScreenTest {

  private lateinit var controller:
      org.robolectric.android.controller.ActivityController<ComponentActivity>
  private lateinit var viewModel: SignUpViewModel
  private val enabledStates = mutableListOf<Boolean>()

  @Before
  fun setUp() {
    viewModel = SignUpViewModel()
    enabledStates.clear()
    controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
  }

  @After
  fun tearDown() {
    controller.pause().stop().destroy()
    runOnIdle()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun `continue button enables only when all fields valid`() {
    composeScreen()

    assertFalse(enabledStates.last())

    viewModel.setFirstName("Ada")
    runOnIdle()
    assertEquals(listOf(false), enabledStates)

    viewModel.setLastName("Lovelace")
    runOnIdle()
    assertEquals(listOf(false), enabledStates)

    viewModel.setBirthdate(1_000L)
    runOnIdle()
    assertEquals(listOf(false, true), enabledStates)

    viewModel.setBirthdate(null)
    runOnIdle()
    assertEquals(listOf(false, true, false), enabledStates)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun `prepopulated state starts enabled`() {
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(1_000L)

    composeScreen()

    assertEquals(listOf(true), enabledStates)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun `clearing last name toggles button`() {
    composeScreen()

    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(2_000L)
    runOnIdle()
    assertEquals(listOf(false, true), enabledStates)

    viewModel.setLastName("")
    runOnIdle()
    assertEquals(listOf(false, true, false), enabledStates)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun `reset clears enabled state`() {
    composeScreen()

    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(3_000L)
    runOnIdle()
    assertEquals(listOf(false, true), enabledStates)

    viewModel.reset()
    runOnIdle()
    assertEquals(listOf(false, true, false), enabledStates)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun `null callback does not trigger state updates`() {
    composeScreen(onContinueEnabledChanged = null)
    assertEquals(emptyList<Boolean>(), enabledStates)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun `external date picker state retains matching selection`() {
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    val jan2000 = 946684800000L
    viewModel.setBirthdate(jan2000)

    var providedState: DatePickerState? = null
    composeScreen(
        datePickerStateFactory = {
          rememberDatePickerState(
                  initialDisplayMode = DisplayMode.Picker, initialSelectedDateMillis = jan2000)
              .also { providedState = it }
        })

    runOnIdle()
    assertEquals(jan2000, providedState?.selectedDateMillis)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun `external show dialog state keeps dialog visible`() {
    val showState = mutableStateOf(true)
    composeScreen(onContinueEnabledChanged = null, showDateDialogState = showState)
    assertEquals(true, showState.value)
    showState.value = false
    runOnIdle()
    assertEquals(false, showState.value)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  private fun composeScreen(
      onContinueEnabledChanged: ((Boolean) -> Unit)? = { enabledStates += it },
      datePickerStateFactory: (@Composable () -> DatePickerState)? = null,
      showDateDialogState: MutableState<Boolean>? = null
  ) {
    controller.get().setContent {
      val state = datePickerStateFactory?.invoke()
      BasicInfoScreen(
          viewModel = viewModel,
          onContinue = {},
          onBack = {},
          onContinueEnabledChanged = onContinueEnabledChanged,
          datePickerState = state,
          showDateDialogState = showDateDialogState)
    }
    runOnIdle()
  }

  private fun runOnIdle() {
    Robolectric.flushForegroundThreadScheduler()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }
}
