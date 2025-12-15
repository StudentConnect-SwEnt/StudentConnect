package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val SAVE_TIMEOUT_MS = 5_000L

/**
 * Saves a user using a sibling coroutine and returns after a short timeout to avoid hanging the UI
 * when offline.
 */
suspend fun CoroutineScope.saveUserWithTimeout(userRepository: UserRepository, updatedUser: User) {
  var saveResult: Result<Unit>? = null
  val saveJob = launch { saveResult = runCatching { userRepository.saveUser(updatedUser) } }
  withTimeoutOrNull(SAVE_TIMEOUT_MS) { saveJob.join() }
  saveResult?.exceptionOrNull()?.let { throw it }
}
