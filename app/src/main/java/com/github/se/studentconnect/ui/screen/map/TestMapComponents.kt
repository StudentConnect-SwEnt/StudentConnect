package com.github.se.studentconnect.ui.screen.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.github.se.studentconnect.resources.C

@Composable
fun TestMapboxMap(modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
  Box(
      modifier =
          modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant).semantics {
            testTag = C.Tag.map_screen
          },
      contentAlignment = Alignment.Center) {
        Text(
            text = "Test Map View",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
      }
}
