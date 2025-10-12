package com.github.se.studentconnect.model.location

import com.github.se.studentconnect.utils.FakeHttpClient
import junit.framework.TestCase
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Test

class LocationRepositoryNominatimTest {
  private val realLocationRepository: LocationRepository =
      LocationRepositoryNominatim(OkHttpClient())
  private val fakeClient = FakeHttpClient.getClient()

  /*@Test
  fun searchWorks() = runBlocking {
    val locations = realLocationRepository.search("New York, New York")

    TestCase.assertEquals(1, locations.size)
    TestCase.assertNotNull(locations[0].name)
    TestCase.assertTrue(locations[0].name!!.contains("New York"))
  }*/

  @Test
  fun searchRateLimitingWorks() = runBlocking {
    repeat(5) {
      // reset the internal timer in the location repository by creating a new one
      val fakeLocationRepository = LocationRepositoryNominatim(fakeClient)

      val timeElapsed = measureTimeMillis {
        fakeLocationRepository.search("EPFL")
        fakeLocationRepository.search("EPFL")
      }

      TestCase.assertTrue(timeElapsed > 1000) // at least 1 second
    }
  }

  @Test
  fun locationResultCanBeConvertedToJsonAndBack() {
    val result = LocationRepositoryNominatim.LocationResult(-1.0, 1.0, "some location")

    val string = Json.Default.encodeToString<LocationRepositoryNominatim.LocationResult>(result)
    val resultDecoded =
        Json.Default.decodeFromString<LocationRepositoryNominatim.LocationResult>(string)

    TestCase.assertEquals(result, resultDecoded)
  }
}
