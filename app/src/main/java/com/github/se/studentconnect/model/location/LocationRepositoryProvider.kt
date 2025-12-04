package com.github.se.studentconnect.model.location

import com.github.se.studentconnect.HttpClientProvider
import com.github.se.studentconnect.model.BaseRepositoryProvider

/** Provides a single instance of the repository in the app. */
object LocationRepositoryProvider : BaseRepositoryProvider<LocationRepository>() {
  private var cachedClient = HttpClientProvider.client
  private var cachedRepository: LocationRepository = createRepository()

  private fun createRepository() = LocationRepositoryNominatim(cachedClient)

  override fun getRepository(): LocationRepository {
    // recreate the repository if the client changed
    val currentClient = HttpClientProvider.client
    if (currentClient != cachedClient) {
      cachedClient = currentClient
      cachedRepository = createRepository()
    }

    return cachedRepository
  }
}
