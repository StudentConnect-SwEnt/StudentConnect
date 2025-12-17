package com.github.se.studentconnect.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * SnackbarHost positioned at the top of the screen.
 *
 * This composable wraps SnackbarHost in a Box with top alignment to display snackbars at the top of
 * the screen instead of the default bottom position.
 */
@Composable
fun TopSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth()) {
    SnackbarHost(
        hostState = hostState, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
  }
}
