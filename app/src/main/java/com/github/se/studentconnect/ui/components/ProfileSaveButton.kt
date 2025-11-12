package com.github.se.studentconnect.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Standardized Save button component for all edit profile pages.
 *
 * Provides consistent styling, loading state, and behavior across all profile edit screens.
 *
 * @param onClick Callback when the button is clicked
 * @param isLoading Whether the button is in a loading state
 * @param enabled Whether the button is enabled (defaults to true when not loading)
 * @param text The text to display on the button (defaults to "Save")
 * @param modifier Modifier for the button (padding can be applied here)
 */
@Composable
fun ProfileSaveButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean = !isLoading,
    text: String = "Save",
    modifier: Modifier = Modifier
) {
  Button(
      onClick = onClick,
      modifier = modifier.fillMaxWidth().height(56.dp),
      enabled = enabled && !isLoading) {
        if (isLoading) {
          CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              color = MaterialTheme.colorScheme.onPrimary)
        } else {
          Text(
              text = text,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold)
        }
      }
}

