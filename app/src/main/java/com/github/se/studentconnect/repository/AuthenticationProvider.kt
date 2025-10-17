package com.github.se.studentconnect.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Provides authentication configuration for the app.
 *
 * Set `local = true` for local testing mode (no Firebase Auth required) Set `local = false` for
 * production mode (uses Firebase OAuth)
 *
 * For unit tests, set `testUserId` to override the current user ID
 */
object AuthenticationProvider {
  // Change this to false for production mode with Firebase OAuth
  var local = false

  // For unit tests - when set, this overrides the current user
  var testUserId: String? = null

  val currentUser: String
    get() =
        when {
          testUserId != null -> testUserId!!
          local -> "user-charlie-02"
          else -> {
            try {
              Firebase.auth.currentUser?.uid ?: ""
            } catch (e: IllegalStateException) {
              // Firebase not initialized (likely in unit tests)
              ""
            }
          }
        }
}
