package com.github.se.studentconnect.utils

import android.util.Log
import com.github.se.studentconnect.model.location.Location
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert

/**
 * A fake HTTP client that intercepts requests and provides predefined responses for testing
 * location search functionality.
 */
object FakeHttpClient {
  enum class FakeLocation(val queryName: String) {
    EPFL("EPFL"),
    LAUSANNE("Lausanne"),
    NOWHERE("Nowhere"),
    EVERYWHERE("Everywhere"),
    TOO_LONG("Too long"),
  }

  private const val NOMINATIM_HOST = "nominatim.openstreetmap.org"
  private const val SEARCH_PATH = "search"
  private val QUERY_PARAMETERS =
      mapOf("q" to FakeLocation.entries.map { it.queryName }.toSet(), "format" to setOf("json"))

  val FakeLocation.locationSuggestions: List<Location>
    get() =
        when (this) {
          FakeLocation.EPFL -> listOf(Location(46.5221982, 6.5661540, "Fake EPFL"))
          FakeLocation.LAUSANNE ->
              listOf(
                  Location(46.5196535, 6.6322734, "Fake Lausanne"),
              )
          FakeLocation.NOWHERE -> emptyList()
          FakeLocation.EVERYWHERE ->
              (0..50).toList<Int>().map { Location(0.0 + it, 0.0 + it, "Somewhere $it") }
          FakeLocation.TOO_LONG ->
              listOf(
                  Location(
                      0.0,
                      0.0,
                      "This is a very long location name designed to test how the application handles location names that exceed typical lengths, ensuring that text wrapping, truncation, or overflow behaviors are correctly implemented in the UI components that display location information."
                  )
              )
        }

  val FakeLocation.getRequestURL: String
    get() = "https://nominatim.openstreetmap.org/search?q=${queryName}&format=json"

  val FakeLocation.locationSuggestionsAsJson: String
    get() =
        "[" +
            locationSuggestions.joinToString(",") {
              """{
        "place_id": 0,
        "licence": "Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright",
        "osm_type": "node",
        "osm_id": 0,
        "lat": "${it.latitude}",
        "lon": "${it.longitude}",
        "class": "railway",
        "type": "station",
        "place_rank": 0,
        "importance": 0.5,
        "addresstype": "railway",
        "name": "${it.name}",
        "display_name": "${it.name}",
        "boundingbox": [
        "${it.latitude - 0.1}",
        "${it.latitude + 0.1}",
        "${it.longitude - 0.1}",
        "${it.longitude + 0.1}"
        ]
    }"""
            } +
            "]"

  private class NominatimAPIInterceptor(val checkUrl: Boolean) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
      val request = chain.request()
      val url = request.url.toString()

      Log.d("MockInterceptor", "Intercepted URL: $url")
      if (checkUrl) {
          Assert.assertTrue("Request must use HTTPS", request.url.isHttps)
          Assert.assertTrue(
              "Invalid host in $url",
              request.url.host.contains("nominatim.openstreetmap.org")
          )
          Assert.assertNotNull(request.url.queryParameter("q"))
          Assert.assertEquals("json", request.url.queryParameter("format"))
      }
      if (request.url.host == NOMINATIM_HOST &&
          request.url.pathSegments.contains(SEARCH_PATH) &&
          QUERY_PARAMETERS.all { (k, v) -> v.contains(request.url.queryParameter(k)) }) {
        val location =
            FakeLocation.entries.find { request.url.queryParameter("q") == it.queryName }!!
        Log.d("MockInterceptor", "Matched FakeLocation: ${location.queryName}")
        return Response.Builder()
            .code(200)
            .message("OK")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(
                location.locationSuggestionsAsJson.toResponseBody("application/json".toMediaType()))
            .build()
      }
      Log.d("MockInterceptor", "No match found for URL: $url")
      return Response.Builder()
          .code(404)
          .message("Not Found")
          .request(request)
          .protocol(Protocol.HTTP_1_1)
          .body("{\"error\":\"Not Found\"}".toResponseBody("application/json".toMediaType()))
          .build()
    }
  }

  fun getClient(checkUrl: Boolean = false): OkHttpClient =
      OkHttpClient.Builder().addInterceptor(NominatimAPIInterceptor(checkUrl)).build()
}