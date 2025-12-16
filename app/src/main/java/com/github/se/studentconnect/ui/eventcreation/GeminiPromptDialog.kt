package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.github.se.studentconnect.R

@Composable
fun GeminiPromptDialog(onDismiss: () -> Unit, onGenerate: (String) -> Unit, isLoading: Boolean) {
  var prompt by remember { mutableStateOf("") }

  AlertDialog(
      onDismissRequest = { if (!isLoading) onDismiss() },
      title = {
        Text(
            text = stringResource(R.string.gemini_dialog_title),
            style = MaterialTheme.typography.titleLarge)
      },
      text = {
        Column {
          Text(text = stringResource(R.string.gemini_dialog_description))
          Spacer(modifier = Modifier.height(16.dp))
          OutlinedTextField(
              value = prompt,
              onValueChange = { prompt = it },
              label = { Text(stringResource(R.string.gemini_dialog_prompt_label)) },
              placeholder = { Text(stringResource(R.string.gemini_dialog_prompt_placeholder)) },
              modifier = Modifier.fillMaxWidth(),
              enabled = !isLoading,
              singleLine = false,
              maxLines = 3)
          if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.gemini_generating_cool),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
          }
        }
      },
      confirmButton = {
        Button(onClick = { onGenerate(prompt) }, enabled = prompt.isNotBlank() && !isLoading) {
          Text(stringResource(R.string.gemini_dialog_button_generate))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss, enabled = !isLoading) {
          Text(stringResource(android.R.string.cancel))
        }
      },
      properties =
          DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading))
}
