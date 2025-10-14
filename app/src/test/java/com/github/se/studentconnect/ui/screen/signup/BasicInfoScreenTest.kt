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

    assertFalse(enabledStates.lastOrNull() ?: false)

    viewModel.setFirstName("Ada")
    runOnIdle()
    assertFalse(enabledStates.lastOrNull() ?: false)

    viewModel.setLastName("Lovelace")
    runOnIdle()
    assertFalse(enabledStates.lastOrNull() ?: false)

    viewModel.setBirthdate(1_000L)
    runOnIdle()
    assertEquals(true, enabledStates.last())

    viewModel.setBirthdate(null)
    runOnIdle()
    assertEquals(false, enabledStates.last())
  }

  @Test
  fun `prepopulated state starts enabled`() {
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(1_000L)

    composeScreen()

    assertEquals(true, enabledStates.last())
  }

  @Test
  fun `clearing last name toggles button`() {
    composeScreen()

    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(2_000L)
    runOnIdle()
    assertEquals(true, enabledStates.last())

    viewModel.setLastName("")
    runOnIdle()
    assertEquals(false, enabledStates.last())
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
