package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
  Column(
      modifier = modifier.fillMaxSize().padding(16.dp).testTag("profile_screen"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
  ) {
    Text(
        text = "Profile",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
    )
  }
}
