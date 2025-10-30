package com.github.se.studentconnect.model.media

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object MediaRepositoryProvider {
  private val _repository: MediaRepository = MediaRepositoryFirebaseStorage()

  var repository: MediaRepository = _repository
}
