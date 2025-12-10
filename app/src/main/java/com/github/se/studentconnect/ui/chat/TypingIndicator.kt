package com.github.se.studentconnect.ui.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Animated typing indicator showing three pulsing dots.
 *
 * @param typingUserNames List of names of users currently typing.
 * @param modifier Modifier to apply to the indicator.
 */
@Composable
fun TypingIndicator(typingUserNames: List<String>, modifier: Modifier = Modifier) {
  if (typingUserNames.isEmpty()) return

  Column(
      modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Display typing users' names
        val typingText =
            when {
              typingUserNames.isEmpty() -> ""
              typingUserNames.size == 1 -> "${typingUserNames[0]} is typing"
              typingUserNames.size == 2 ->
                  "${typingUserNames[0]} and ${typingUserNames[1]} are typing"
              else -> "${typingUserNames[0]} and ${typingUserNames.size - 1} others are typing"
            }

        Row(
            modifier = Modifier.fillMaxWidth().testTag("typing_indicator"),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = typingText,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.testTag("typing_indicator_text"))

              // Animated dots
              Row(
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { index ->
                      AnimatedDot(
                          delayMillis = index * 150,
                          modifier = Modifier.testTag("typing_indicator_dot_$index"))
                    }
                  }
            }
      }
}

/**
 * Single animated dot for the typing indicator.
 *
 * @param delayMillis Delay before starting the animation (for staggered effect).
 * @param modifier Modifier to apply to the dot.
 */
@Composable
private fun AnimatedDot(delayMillis: Int, modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "dot_animation")

  val alpha by
      infiniteTransition.animateFloat(
          initialValue = 0.3f,
          targetValue = 1f,
          animationSpec =
              infiniteRepeatable(
                  animation =
                      keyframes {
                        durationMillis = 900
                        0.3f at 0
                        1f at 300 + delayMillis
                        0.3f at 600 + delayMillis
                      },
                  repeatMode = RepeatMode.Restart),
          label = "dot_alpha")

  Box(
      modifier =
          modifier
              .size(6.dp)
              .alpha(alpha)
              .background(color = MaterialTheme.colorScheme.onSurfaceVariant, shape = CircleShape))
}
