package com.github.se.studentconnect.model

/** Base interface for a provider that exposes exactly one repository. */
interface RepositoryProvider<T : Repository> {
  /** The provided repository. */
  val repository: T
}
