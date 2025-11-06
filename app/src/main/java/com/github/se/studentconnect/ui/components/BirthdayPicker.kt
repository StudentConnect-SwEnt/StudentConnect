package com.github.se.studentconnect.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reusable birthday picker dialog component.
 *
 * This component provides a Material 3 DatePickerDialog that can be used
 * for birthday selection across different screens (signup, profile edit, etc.)
 *
 * @param showDialog Whether the dialog should be shown
 * @param datePickerState The state of the date picker
 * @param onDismiss Callback when the dialog is dismissed
 * @param onConfirm Callback when a date is confirmed, provides the selected date in milliseconds
 * @param modifier Optional modifier for the dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayPickerDialog(
    showDialog: Boolean,
    datePickerState: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
  if (showDialog) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
          Button(
              onClick = {
                val millis = datePickerState.selectedDateMillis
                if (millis != null) {
                  onConfirm(millis)
                }
                onDismiss()
              }) {
                Text("OK")
              }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
        modifier = modifier) {
          DatePicker(state = datePickerState)
        }
  }
}

/**
 * Utility object for birthday-related date formatting.
 */
object BirthdayFormatter {
  /**
   * Standard date formatter for birthday display (dd/MM/yyyy).
   */
  val dateFormatter: SimpleDateFormat
    get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }

  /**
   * Formats a date in milliseconds to a birthday string.
   *
   * @param dateMillis The date in milliseconds
   * @return Formatted date string in dd/MM/yyyy format
   */
  fun formatDate(dateMillis: Long): String {
    return dateFormatter.format(Date(dateMillis))
  }

  /**
   * Parses a birthday string to milliseconds.
   *
   * @param dateString The date string in dd/MM/yyyy format
   * @return Date in milliseconds, or null if parsing fails
   */
  fun parseDate(dateString: String): Long? {
    return try {
      dateFormatter.parse(dateString)?.time
    } catch (e: Exception) {
      null
    }
  }
}
