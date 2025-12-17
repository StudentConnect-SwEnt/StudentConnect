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

private val SPACER_HEIGHT_SMALL = 8.dp
private val SPACER_HEIGHT_MEDIUM = 16.dp

/**
 * Dialog that prompts the user to enter a text prompt for the Gemini generator.
 *
 * This composable shows an AlertDialog containing:
 * - a title and description read from string resources
 * - an OutlinedTextField for the prompt (max 3 lines)
 * - a loading state that displays a centered CircularProgressIndicator and a status text
 * - a confirm button that calls [onGenerate] with the entered prompt
 * - a dismiss button that calls [onDismiss]
 *
 * Spacing within the dialog is provided via the constants [SPACER_HEIGHT_SMALL] and
 * [SPACER_HEIGHT_MEDIUM] to avoid hardcoded .dp values.
 *
 * @param onDismiss Callback invoked when the dialog should be dismissed.
 * @param onGenerate Callback invoked when the user taps the generate button with the prompt String.
 * @param isLoading When true, disables dismiss and shows a loading indicator.
 */
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
          Spacer(modifier = Modifier.height(SPACER_HEIGHT_MEDIUM))
          Text(
              text = stringResource(R.string.gemini_dialog_prompt_note),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(SPACER_HEIGHT_SMALL))
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
            Spacer(modifier = Modifier.height(SPACER_HEIGHT_MEDIUM))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
            Spacer(modifier = Modifier.height(SPACER_HEIGHT_SMALL))
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
