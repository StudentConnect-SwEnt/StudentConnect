package com.github.se.studentconnect.model.location

/** Represents a repository that manages locations. */
interface LocationRepository {
  /**
   * Searches for locations that match the given query string.
   *
   * @param query The query to filter locations by.
   * @return A list of [Location] objects matching the query.
   *
   * This function is `suspend` because it may involve I/O operations such as network requests.
   */
  suspend fun search(query: String): List<Location>
}
