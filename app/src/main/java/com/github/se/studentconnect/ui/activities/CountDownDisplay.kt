package com.github.se.studentconnect.ui.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CountDownDisplay(timeLeft: Long) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    TimeUnitBox(timeLeft = days(timeLeft))
    Separator()
    TimeUnitBox(timeLeft = hours(timeLeft))
    Separator()
    TimeUnitBox(timeLeft = mins(timeLeft))
    Separator()
    TimeUnitBox(timeLeft = secs(timeLeft))
  }
}

@Composable
private fun Separator() {
  Text(text = ":", style = TitleTextStyle())
}

@Composable
private fun TimeUnitBox(timeLeft: String) {
  Surface(
      // modifier = Modifier.padding(horizontal = 10.dp),
      shape = MaterialTheme.shapes.medium,
      color = MaterialTheme.colorScheme.primary) {
        Text(
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 3.dp),
            color = Color.White,
            text = timeLeft.padStart(2, '0'),
            style = MaterialTheme.typography.displayMedium)
      }
}

private fun days(seconds: Long): String {
  return (seconds / (24 * 3600)).toString()
}

private fun hours(seconds: Long): String {
  return ((seconds % (24 * 3600)) / 3600).toString()
}

private fun mins(seconds: Long): String {
  return ((seconds % 3600) / 60).toString()
}

private fun secs(seconds: Long): String {
  return (seconds % 60).toString()
}
