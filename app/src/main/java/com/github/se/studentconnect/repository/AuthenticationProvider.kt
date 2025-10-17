package com.github.se.studentconnect.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Provides authentication configuration for the app.
 *
 * Set `local = true` for local testing mode (no Firebase Auth required) Set `local = false` for
 * production mode (uses Firebase OAuth)
 */
object AuthenticationProvider {
  // Change this to false for production mode with Firebase OAuth
  var local = false

  var currentUser: String =
      if (local) {
        "user-charlie-02"
      } else {
        Firebase.auth.currentUser?.uid ?: ""
      }
}
