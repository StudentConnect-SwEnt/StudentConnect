package com.github.se.studentconnect.model.location

import com.github.se.studentconnect.HttpClientProvider
import com.github.se.studentconnect.model.BaseRepositoryProvider

/** Provides a single instance of the repository in the app. */
object LocationRepositoryProvider : BaseRepositoryProvider<LocationRepository>() {
  override fun createRepository() = LocationRepositoryNominatim(HttpClientProvider.client)
}
