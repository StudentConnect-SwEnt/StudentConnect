package com.github.se.studentconnect

import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test

class MainActivityTest {

  @Test
  fun httpClientProvider_hasDefaultClient() {
    assertNotNull(HttpClientProvider.client)
    assertTrue(HttpClientProvider.client is OkHttpClient)
  }

  @Test
  fun httpClientProvider_clientIsMutable() {
    val originalClient = HttpClientProvider.client
    val newClient = OkHttpClient()

    HttpClientProvider.client = newClient
    assertEquals(newClient, HttpClientProvider.client)

    HttpClientProvider.client = originalClient
  }

  @Test
  fun tab_homeDestination_isCorrect() {
    assertEquals(Route.HOME, Tab.Home.destination.route)
  }

  @Test
  fun tab_mapDestination_isCorrect() {
    assertEquals(Route.MAP, Tab.Map.destination.route)
  }

  @Test
  fun tab_activitiesDestination_isCorrect() {
    assertEquals(Route.ACTIVITIES, Tab.Activities.destination.route)
  }

  @Test
  fun tab_profileDestination_isCorrect() {
    assertEquals(Route.PROFILE, Tab.Profile.destination.route)
  }

  @Test
  fun route_constants_areNotEmpty() {
    assertFalse(Route.HOME.isEmpty())
    assertFalse(Route.MAP.isEmpty())
    assertFalse(Route.ACTIVITIES.isEmpty())
    assertFalse(Route.PROFILE.isEmpty())
  }

  @Test
  fun httpClientProvider_clientIsNotNull() {
    val client = HttpClientProvider.client
    assertNotNull("HttpClientProvider client should not be null", client)
  }

  @Test
  fun tab_values_exist() {
    assertNotNull(Tab.Home)
    assertNotNull(Tab.Map)
    assertNotNull(Tab.Activities)
    assertNotNull(Tab.Profile)
  }

  @Test
  fun tab_destinations_areValid() {
    assertTrue(Tab.Home.destination.route.isNotEmpty())
    assertTrue(Tab.Map.destination.route.isNotEmpty())
    assertTrue(Tab.Activities.destination.route.isNotEmpty())
    assertTrue(Tab.Profile.destination.route.isNotEmpty())
  }

  @Test
  fun httpClientProvider_canCreateNewClient() {
    val newClient = OkHttpClient.Builder().build()
    assertNotNull(newClient)
    assertTrue(newClient is OkHttpClient)
  }
}
