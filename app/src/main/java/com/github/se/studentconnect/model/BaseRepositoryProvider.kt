// Portions of this code were generated with the help of ChatGPT
package com.github.se.studentconnect.model

import androidx.annotation.VisibleForTesting

/**
 * Base class for providers of a single Repository.
 * - In production: exposes repo via `repository` (read-only).
 * - In tests: repo can be swapped via `overrideForTests`.
 */
abstract class BaseRepositoryProvider<T : Repository> : RepositoryProvider<T> {

  /** Will only be called if no test override is set and someone actually needs the repository. */
  protected abstract fun getCurrentRepository(): T

  // Test override (null in production)
  @Volatile private var testRepository: T? = null

  final override val repository: T
    get() = testRepository ?: getCurrentRepository()

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun overrideForTests(repository: T) {
    testRepository = repository
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun cleanOverrideForTests() {
    testRepository = null
  }
}
