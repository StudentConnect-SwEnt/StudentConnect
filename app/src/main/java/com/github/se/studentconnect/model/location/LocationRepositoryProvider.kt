package com.github.se.studentconnect.model.location

import com.github.se.studentconnect.HttpClientProvider

/** Provides a single instance of the repository in the app. */
object LocationRepositoryProvider {
  private var cachedClient = HttpClientProvider.client
  private var cachedRepository: LocationRepository = LocationRepositoryNominatim(cachedClient)

  val repository: LocationRepository
    get() {
      // recreate the repository if the client changed
      val currentClient = HttpClientProvider.client
      if (currentClient != cachedClient) {
        cachedClient = currentClient
        cachedRepository = LocationRepositoryNominatim(cachedClient)
      }

      return cachedRepository
    }
}
