// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.material3.*
import androidx.compose.material3.TimePickerDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.se.studentconnect.R
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(modifier: Modifier = Modifier, time: LocalTime, onTimeChange: (LocalTime) -> Unit) {
  var showDialog by remember { mutableStateOf(false) }

  val timePickerState =
      rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)

  LaunchedEffect(time) {
    timePickerState.hour = time.hour
    timePickerState.minute = time.minute
  }

  TextButton(onClick = { showDialog = true }, modifier = modifier) {
    val formattedHour = time.hour.toString().padStart(2, '0')
    val formattedMinutes = time.minute.toString().padStart(2, '0')
    Text(text = "$formattedHour:$formattedMinutes")
  }

  if (showDialog) {
    TimePickerDialog(
        title = { Text(stringResource(R.string.time_picker_title)) },
        onDismissRequest = { showDialog = false },
        confirmButton = {
          TextButton(
              onClick = {
                showDialog = false
                onTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
              }) {
                Text(stringResource(R.string.button_ok))
              }
        },
        dismissButton = {
          TextButton(onClick = { showDialog = false }) {
            Text(stringResource(R.string.button_cancel))
          }
        },
    ) {
      TimePicker(state = timePickerState)
    }
  }
}
