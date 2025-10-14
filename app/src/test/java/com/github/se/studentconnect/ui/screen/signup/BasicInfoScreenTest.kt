package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

  @Test
  fun `prepopulated state starts enabled`() {
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(1_000L)

    composeScreen()

    assertEquals(listOf(true), enabledStates)
  }

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

  private fun composeScreen() {
    controller.get().setContent {
      BasicInfoScreen(
          viewModel = viewModel,
          onContinue = {},
          onBack = {},
          onContinueEnabledChanged = { enabledStates += it })
    }
    runOnIdle()
  }

  private fun runOnIdle() {
    Robolectric.flushForegroundThreadScheduler()
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
  }
}
