// Portions of this code were generated with the help of ChatGPT
package com.github.se.studentconnect.model

import androidx.annotation.VisibleForTesting

/**
 * Base class for providers of a single Repository.
 * - In production: exposes repo via `repository` (read-only).
 * - In tests: repo can be swapped via `overrideForTests`.
 */
abstract class BaseRepositoryProvider<T : Repository> : RepositoryProvider<T> {

  /**
   * Factory for the default repository. Will only be called if no test override is set and someone
   * actually needs the repository.
   */
  protected abstract fun createDefaultRepository(): T

  private val defaultRepository by lazy { createDefaultRepository() }

  // Test override (null in production)
  @Volatile private var testRepository: T? = null

  override val repository: T
    get() = testRepository ?: defaultRepository

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun overrideForTests(repository: T) {
    testRepository = repository
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun cleanOverrideForTests() {
    testRepository = null
  }
}
