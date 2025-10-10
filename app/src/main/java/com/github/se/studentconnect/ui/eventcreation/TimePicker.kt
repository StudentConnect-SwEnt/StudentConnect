package com.github.se.studentconnect.ui.eventcreation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime

@Composable
fun TimePicker(modifier: Modifier = Modifier, time: LocalTime, onTimeChange: (LocalTime) -> Unit) {
  val context = LocalContext.current
  val activity = context as AppCompatActivity

  TextButton(
      modifier = modifier,
      onClick = {
        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(time.hour)
                .setMinute(time.minute)
                .setTitleText("Select time")
                .build()

        picker.addOnPositiveButtonClickListener {
          onTimeChange(LocalTime.of(picker.hour, picker.minute))
        }

        picker.show(activity.supportFragmentManager, "timePicker")
      }) {
        val formattedHour = time.hour.toString().padStart(2, '0')
        val formattedMinutes = time.minute.toString().padStart(2, '0')
        Text(text = "${formattedHour}:${formattedMinutes}")
      }
}
