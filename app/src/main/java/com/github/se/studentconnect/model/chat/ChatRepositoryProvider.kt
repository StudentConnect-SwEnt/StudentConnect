package com.github.se.studentconnect.model.chat

import com.github.se.studentconnect.model.RepositoryProvider
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Provider for ChatRepository.
 *
 * This object manages the singleton instance of the ChatRepository and allows for dependency
 * injection in tests.
 */
object ChatRepositoryProvider : RepositoryProvider<ChatRepository> {
  private var repository: ChatRepository? = null

  override val repository: ChatRepository
    get() {
      return ChatRepositoryProvider.repository
          ?: ChatRepositoryFirestore(FirebaseFirestore.getInstance()).also {
            ChatRepositoryProvider.repository = it
          }
    }

  /**
   * Sets a custom repository implementation (primarily for testing).
   *
   * @param repository The repository implementation to use.
   */
  fun setRepository(repository: ChatRepository) {
    this.repository = repository
  }

  /** Clears the current repository instance. */
  fun clearRepository() {
    repository = null
  }
}
