package com.github.se.studentconnect.model.media

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object MediaRepositoryProvider {
  private val firebaseRepository: MediaRepository by lazy { MediaRepositoryFirebaseStorage() }

  private var _repository: MediaRepository? = null

  var repository: MediaRepository
    get() = _repository ?: firebaseRepository
    set(value) {
      _repository = value
    }
}
