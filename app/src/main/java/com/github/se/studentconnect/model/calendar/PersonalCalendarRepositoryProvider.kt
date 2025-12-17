package com.github.se.studentconnect.model.calendar

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Provider object for PersonalCalendarRepository. Provides a singleton instance of the repository
 * for dependency injection.
 */
object PersonalCalendarRepositoryProvider {

  /**
   * The singleton repository instance. Uses lazy initialization to create the Firestore
   * implementation.
   */
  val repository: PersonalCalendarRepository by lazy {
    PersonalCalendarRepositoryFirestore(FirebaseFirestore.getInstance())
  }
}
