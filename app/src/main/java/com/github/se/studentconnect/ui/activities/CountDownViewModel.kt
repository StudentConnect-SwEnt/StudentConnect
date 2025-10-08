// To debug : added the countDownJob cancellation by chatGpt
package com.github.se.studentconnect.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CountDownViewModel() : ViewModel() {

  private val _timeLeft = MutableStateFlow(0L)
  val timeLeft: StateFlow<Long> = _timeLeft

  private var countdownJob: Job? = null

  fun startCountdown(eventStart: Timestamp) {
    countdownJob?.cancel()
    countdownJob =
        viewModelScope.launch {
          while (true) {
            val now = LocalDateTime.now()
            val duration =
                Duration.between(
                    now,
                    eventStart
                        .toDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime())
            if (duration.isNegative || duration.isZero) {
              _timeLeft.value = 0L
              break
            }
            _timeLeft.value = duration.seconds
            // pause for 1 second before updating again
            delay(1000L)
          }
        }
  }
}
