package com.github.se.studentconnect.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth

object AuthenticationProvider {
  const val local = true
  var currentUser: String =
      if (local) {
        "user-charlie-02"
      } else {
        Firebase.auth.currentUser?.uid ?: ""
      }
}
