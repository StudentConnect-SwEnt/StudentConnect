package com.github.se.studentconnect.model.notification

/** Singleton provider for NotificationRepository */
object NotificationRepositoryProvider {
  private var _repository: NotificationRepository? = null

  /** Gets the current repository instance */
  val repository: NotificationRepository
    get() = _repository ?: throw IllegalStateException("NotificationRepository not initialized")

  /**
   * Sets the repository instance
   *
   * @param repository The repository to use
   */
  fun setRepository(repository: NotificationRepository) {
    _repository = repository
  }

  /** Clears the repository instance (useful for testing) */
  fun clearRepository() {
    _repository = null
  }
}
