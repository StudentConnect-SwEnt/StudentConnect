package com.github.se.studentconnect

import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test

class MainActivityTest {

  /** Test to verify that HttpClientProvider provides a default OkHttpClient instance. */
  @Test
  fun httpClientProvider_hasDefaultClient() {
    assertNotNull(HttpClientProvider.client)
    assertTrue(HttpClientProvider.client is OkHttpClient)
  }

  /** Test to verify that HttpClientProvider allows setting a new OkHttpClient instance. */
  @Test
  fun httpClientProvider_clientIsMutable() {
    val originalClient = HttpClientProvider.client
    val newClient = OkHttpClient()

    HttpClientProvider.client = newClient
    assertEquals(newClient, HttpClientProvider.client)

    HttpClientProvider.client = originalClient
  }

  /** Test to verify that Home tab object has correct destination. */
  @Test
  fun tab_homeDestination_isCorrect() {
    assertEquals(Route.HOME, Tab.Home.destination.route)
  }

  /** Test to verify that Map tab object has correct destination. */
  @Test
  fun tab_mapDestination_isCorrect() {
    assertEquals(Route.MAP, Tab.Map.destination.route)
  }

  /** Test to verify that Activities tab object has correct destination. */
  @Test
  fun tab_activitiesDestination_isCorrect() {
    assertEquals(Route.ACTIVITIES, Tab.Activities.destination.route)
  }

  /** Test to verify that Profile tab object has correct destination. */
  @Test
  fun tab_profileDestination_isCorrect() {
    assertEquals(Route.PROFILE, Tab.Profile.destination.route)
  }

  /** Test to verify that route constants are not empty strings. */
  @Test
  fun route_constants_areNotEmpty() {
    assertFalse(Route.HOME.isEmpty())
    assertFalse(Route.MAP.isEmpty())
    assertFalse(Route.ACTIVITIES.isEmpty())
    assertFalse(Route.PROFILE.isEmpty())
  }

  /** Test to verify that HttpClientProvider client is not null. */
  @Test
  fun httpClientProvider_clientIsNotNull() {
    val client = HttpClientProvider.client
    assertNotNull("HttpClientProvider client should not be null", client)
  }

  /** Test to verify that Tab objects are not null. */
  @Test
  fun tab_values_exist() {
    assertNotNull(Tab.Home)
    assertNotNull(Tab.Map)
    assertNotNull(Tab.Activities)
    assertNotNull(Tab.Profile)
  }

  /** Test to verify that Tab objects have valid (non-empty) destination routes. */
  @Test
  fun tab_destinations_areValid() {
    assertTrue(Tab.Home.destination.route.isNotEmpty())
    assertTrue(Tab.Map.destination.route.isNotEmpty())
    assertTrue(Tab.Activities.destination.route.isNotEmpty())
    assertTrue(Tab.Profile.destination.route.isNotEmpty())
  }

  /** Test to verify that a new OkHttpClient can be created. */
  @Test
  fun httpClientProvider_canCreateNewClient() {
    val newClient = OkHttpClient.Builder().build()
    assertNotNull(newClient)
    assertTrue(newClient is OkHttpClient)
  }

  /** Test to verify that Tab icons are not zero. */
  @Test
  fun tab_icons_areNotZero() {
    assertTrue(Tab.Home.icon != 0)
    assertTrue(Tab.Map.icon != 0)
    assertTrue(Tab.Activities.icon != 0)
    assertTrue(Tab.Profile.icon != 0)
  }

  /** Test to verify that Tab objects have correct destination routes. */
  @Test
  fun tab_destinations_haveCorrectRoutes() {
    assertEquals("home", Tab.Home.destination.route)
    assertEquals("map", Tab.Map.destination.route)
    assertEquals("activities", Tab.Activities.destination.route)
    assertEquals("profile", Tab.Profile.destination.route)
  }

  /** Test to verify that Tab objects have correct destination names. */
  @Test
  fun tab_destinations_haveCorrectNames() {
    assertEquals("Home", Tab.Home.destination.name)
    assertEquals("Map", Tab.Map.destination.name)
    assertEquals("Activities", Tab.Activities.destination.name)
    assertEquals("Profile", Tab.Profile.destination.name)
  }

  /** Test to verify that Tab destinations are marked as top level destinations. */
  @Test
  fun tab_destinations_areTopLevelDestinations() {
    assertTrue(Tab.Home.destination.isTopLevelDestination)
    assertTrue(Tab.Map.destination.isTopLevelDestination)
    assertTrue(Tab.Activities.destination.isTopLevelDestination)
    assertTrue(Tab.Profile.destination.isTopLevelDestination)
  }

  /** Test to verify that route constants have expected values. */
  @Test
  fun route_constants_haveExpectedValues() {
    assertEquals("home", Route.HOME)
    assertEquals("map", Route.MAP)
    assertEquals("activities", Route.ACTIVITIES)
    assertEquals("profile", Route.PROFILE)
    assertEquals("auth", Route.AUTH)
  }

  /** Test to verify that HttpClientProvider client can be replaced and restored. */
  @Test
  fun httpClientProvider_clientCanBeReplacedAndRestored() {
    val originalClient = HttpClientProvider.client
    val testClient =
        OkHttpClient.Builder()
            .connectTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

    // Replace with test client
    HttpClientProvider.client = testClient
    assertEquals(testClient, HttpClientProvider.client)
    assertNotEquals(originalClient, HttpClientProvider.client)

    // Restore original
    HttpClientProvider.client = originalClient
    assertEquals(originalClient, HttpClientProvider.client)
  }

  /** Test to verify that Tab objects have unique routes. */
  @Test
  fun tab_allTabsHaveUniqueRoutes() {
    val routes = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile).map { it.destination.route }
    val uniqueRoutes = routes.toSet()
    assertEquals(routes.size, uniqueRoutes.size)
  }

  /** Test to verify that Tab objects have unique destination names. */
  @Test
  fun tab_allTabsHaveUniqueNames() {
    val names = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile).map { it.destination.name }
    val uniqueNames = names.toSet()
    assertEquals(names.size, uniqueNames.size)
  }

  /** Test to verify that Tab objects have unique icons. */
  @Test
  fun tab_allTabsHaveUniqueIcons() {
    val icons = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile).map { it.icon }
    val uniqueIcons = icons.toSet()
    assertEquals(icons.size, uniqueIcons.size)
  }
}
