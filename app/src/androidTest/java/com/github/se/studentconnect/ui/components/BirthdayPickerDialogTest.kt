package com.github.se.studentconnect.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.TimeZone

@RunWith(AndroidJUnit4::class)
class BirthdayPickerDialogTest {

  @get:Rule val composeTestRule = createComposeRule()

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_notShownWhenShowDialogIsFalse() {
    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState()
        BirthdayPickerDialog(
            showDialog = false,
            datePickerState = datePickerState,
            onDismiss = {},
            onConfirm = {})
      }
    }

    // Dialog should not be displayed
    composeTestRule.onNodeWithText("OK").assertDoesNotExist()
    composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_shownWhenShowDialogIsTrue() {
    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState()
        BirthdayPickerDialog(
            showDialog = true,
            datePickerState = datePickerState,
            onDismiss = {},
            onConfirm = {})
      }
    }

    // Dialog should be displayed
    composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_displaysOKButton() {
    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState()
        BirthdayPickerDialog(
            showDialog = true,
            datePickerState = datePickerState,
            onDismiss = {},
            onConfirm = {})
      }
    }

    composeTestRule.onNodeWithText("OK").assertExists().assertIsDisplayed()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_displaysCancelButton() {
    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState()
        BirthdayPickerDialog(
            showDialog = true,
            datePickerState = datePickerState,
            onDismiss = {},
            onConfirm = {})
      }
    }

    composeTestRule.onNodeWithText("Cancel").assertExists().assertIsDisplayed()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_cancelButtonCallsOnDismiss() {
    var dismissCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState()
        BirthdayPickerDialog(
            showDialog = true,
            datePickerState = datePickerState,
            onDismiss = { dismissCalled = true },
            onConfirm = {})
      }
    }

    composeTestRule.onNodeWithText("Cancel").performClick()

    assertTrue(dismissCalled)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_okButtonCallsOnConfirmAndOnDismiss() {
    var confirmCalled = false
    var dismissCalled = false
    var confirmedMillis: Long? = null

    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val testMillis = calendar.timeInMillis

    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = testMillis)
        BirthdayPickerDialog(
            showDialog = true,
            datePickerState = datePickerState,
            onDismiss = { dismissCalled = true },
            onConfirm = { millis ->
              confirmCalled = true
              confirmedMillis = millis
            })
      }
    }

    composeTestRule.onNodeWithText("OK").performClick()

    assertTrue(confirmCalled)
    assertTrue(dismissCalled)
    assertNotNull(confirmedMillis)
  }


  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_okButtonDoesNotCallConfirmWhenNoDateSelected() {
    var confirmCalled = false
    var dismissCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = null)
        BirthdayPickerDialog(
            showDialog = true,
            datePickerState = datePickerState,
            onDismiss = { dismissCalled = true },
            onConfirm = { confirmCalled = true })
      }
    }

    composeTestRule.onNodeWithText("OK").performClick()

    assertFalse(confirmCalled) // Should not confirm when no date selected
    assertTrue(dismissCalled) // Should still dismiss
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_handlesMultipleDismissCalls() {
    var dismissCount = 0

    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState()
        BirthdayPickerDialog(
            showDialog = true,
            datePickerState = datePickerState,
            onDismiss = { dismissCount++ },
            onConfirm = {})
      }
    }

    // Click cancel
    composeTestRule.onNodeWithText("Cancel").performClick()
    assertEquals(1, dismissCount)
  }



  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_toggleShowDialogWorksCorrectly() {
    var showDialog = true

    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState()
        BirthdayPickerDialog(
            showDialog = showDialog,
            datePickerState = datePickerState,
            onDismiss = { showDialog = false },
            onConfirm = { showDialog = false })
      }
    }

    // Dialog should be shown initially
    composeTestRule.onNodeWithText("OK").assertExists()

    // Click cancel
    composeTestRule.onNodeWithText("Cancel").performClick()

    // showDialog should be false now
    assertFalse(showDialog)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun birthdayPickerDialog_confirmButtonClosesDialog() {
    var showDialog = true

    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val testMillis = calendar.timeInMillis

    composeTestRule.setContent {
      MaterialTheme {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = testMillis)
        BirthdayPickerDialog(
            showDialog = showDialog,
            datePickerState = datePickerState,
            onDismiss = { showDialog = false },
            onConfirm = { showDialog = false })
      }
    }

    // Click OK
    composeTestRule.onNodeWithText("OK").performClick()

    // showDialog should be false now
    assertFalse(showDialog)
  }
}
