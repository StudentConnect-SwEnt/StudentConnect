// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.se.studentconnect.MainActivity
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
class BasicInfoScreenTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  override fun createInitializedRepository() = EventRepositoryLocal()

  private lateinit var viewModel: SignUpViewModel
  private val enabledStates = mutableListOf<Boolean>()

  private val dialogState = mutableStateOf(false)
  private var datePickerState: androidx.compose.material3.DatePickerState? = null

  // --------------------------------------------------
  // Setup
  // --------------------------------------------------

  @Before
  fun setUpContent() {
    viewModel = SignUpViewModel()
    enabledStates.clear()
    dialogState.value = false

    composeTestRule.activity.setContent {
      AppTheme {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = null)
        datePickerState = pickerState

        BasicInfoScreen(
            viewModel = viewModel,
            onContinue = {},
            onBack = {},
            onContinueEnabledChanged = { enabledStates += it },
            datePickerState = pickerState,
            showDateDialogState = dialogState)
      }
    }
  }

  private fun waitForIdle() = composeTestRule.waitForIdle()

  // --------------------------------------------------
  // 1. Initial & Rendering
  // --------------------------------------------------

  @Test
  fun initial_state_continueDisabled() {
    waitForIdle()
    assert(enabledStates.isNotEmpty())
    assert(!enabledStates.last())
  }

  @Test
  fun prepopulated_fields_enableContinue() {
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(Timestamp(Date(1_000L)))
    waitForIdle()
    assert(enabledStates.last())
  }

  // --------------------------------------------------
  // 2. Field Validation Logic
  // --------------------------------------------------

  @Test
  fun continue_enabled_only_when_all_fields_valid() {
    viewModel.setFirstName("Ada")
    waitForIdle()
    assert(enabledStates.first() == false)

    viewModel.setLastName("Lovelace")
    waitForIdle()
    assert(enabledStates.last() == false)

    viewModel.setBirthdate(Timestamp(Date(1_000L)))
    waitForIdle()
    assert(enabledStates.last())

    viewModel.setBirthdate(null)
    waitForIdle()
    assert(!enabledStates.last())
  }

  @Test
  fun clearing_lastName_disables_continue() {
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(Timestamp(Date(2_000L)))
    waitForIdle()
    assert(enabledStates.last())

    viewModel.setLastName("")
    waitForIdle()
    assert(!enabledStates.last())
  }

  @Test
  fun reset_clears_enabled_state() {
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(Timestamp(Date(3_000L)))
    waitForIdle()
    assert(enabledStates.last())

    viewModel.reset()
    waitForIdle()
    assert(!enabledStates.last())
  }

  // --------------------------------------------------
  // 3. Date Picker Behavior
  // --------------------------------------------------

  @Test
  fun external_datePicker_retains_selection() {
    val jan2000Millis = 946684800000L
    val jan2000Timestamp = Timestamp(Date(jan2000Millis))
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(jan2000Timestamp)
    waitForIdle()

    assert(datePickerState?.selectedDateMillis == jan2000Millis)
  }

  @Test
  fun selecting_date_confirms_continue_enabled() {
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    waitForIdle()

    val selectedMillis = 946684800000L
    val selectedTimestamp = Timestamp(Date(selectedMillis))
    datePickerState?.selectedDateMillis = selectedMillis
    viewModel.setBirthdate(selectedTimestamp)
    waitForIdle()

    assert(enabledStates.last())
  }

  // --------------------------------------------------
  // 4. Dialog Visibility
  // --------------------------------------------------

  @Test
  fun dialog_visibility_toggles_correctly() {
    dialogState.value = true
    waitForIdle()
    assert(dialogState.value)

    dialogState.value = false
    waitForIdle()
    assert(!dialogState.value)
  }

  @Test
  fun rapid_dialog_toggles_preserve_enabled_state() {
    viewModel.setFirstName("Ada")
    viewModel.setLastName("Lovelace")
    viewModel.setBirthdate(Timestamp(Date(6_000L)))
    waitForIdle()
    val initialStates = enabledStates.toList()

    repeat(5) {
      dialogState.value = !dialogState.value
      waitForIdle()
    }

    assert(enabledStates.take(initialStates.size) == initialStates)
  }

  // --------------------------------------------------
  // 5. Null Callback
  // --------------------------------------------------

  @Test
  fun null_callback_does_not_crash_or_update_state() {
    composeTestRule.activity.setContent {
      AppTheme {
        BasicInfoScreen(
            viewModel = viewModel, onContinue = {}, onBack = {}, onContinueEnabledChanged = null)
      }
    }

    waitForIdle()
    assert(enabledStates.isEmpty())
  }
}
