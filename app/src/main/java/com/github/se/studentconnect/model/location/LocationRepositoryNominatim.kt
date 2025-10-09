// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.location

import androidx.annotation.VisibleForTesting
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

/**
 * A [LocationRepository] implementation that queries the
 * [Nominatim](https://nominatim.openstreetmap.org/) geocoding API (part of OpenStreetMap) for
 * location data.
 *
 * To respect the API’s usage policy, this repository enforces a minimum delay of one second between
 * consecutive requests.
 *
 * @property client The [OkHttpClient] used to execute network requests.
 */
class LocationRepositoryNominatim(private val client: OkHttpClient) : LocationRepository {
  private val minTimeBetweenRequests: Long = 1000
  private var lastRequestTime: Long = 0
  private val mutex = Mutex()

  private suspend fun rateLimit() =
      mutex.withLock {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastRequestTime

        if (elapsedTime < minTimeBetweenRequests) {
          delay(minTimeBetweenRequests - elapsedTime)
        }

        lastRequestTime = System.currentTimeMillis()
      }

  @OptIn(kotlinx.serialization.InternalSerializationApi::class)
  @Serializable
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal data class LocationResult(
      val lat: Double,
      val lon: Double,
      @SerialName("display_name") val displayName: String,
  ) {
    fun toLocation() = Location(lat, lon, displayName)
  }

  private val json = Json { ignoreUnknownKeys = true }

  override suspend fun search(query: String): List<Location> {
    rateLimit()

    val url =
        HttpUrl.Builder()
            .scheme("https")
            .host("nominatim.openstreetmap.org")
            .addPathSegment("search")
            .addQueryParameter("format", "json")
            .addQueryParameter("q", query)
            .build()

    val request =
        Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "StudentConnect/1.0 (https://github.com/StudentConnect-SwEnt/StudentConnect)")
            .build()

    val response = client.newCall(request).await()

    val locations =
        response.use {
          if (!it.isSuccessful) throw IOException("Unexpected code $response")
          val body = it.body?.string() ?: throw IOException("Empty response body")

          val results: List<LocationResult> = json.decodeFromString<List<LocationResult>>(body)
          return@use results.map(LocationResult::toLocation)
        }

    return locations
  }
}

private suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
  enqueue(
      object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          if (cont.isActive) cont.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
          if (cont.isActive) cont.resume(response)
        }
      })

  // cancel OkHttp call if coroutine is cancelled
  cont.invokeOnCancellation { cancel() }
}
