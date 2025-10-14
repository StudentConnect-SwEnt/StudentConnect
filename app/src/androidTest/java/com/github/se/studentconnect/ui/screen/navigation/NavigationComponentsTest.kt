package com.github.se.studentconnect.ui.screen.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Screen
import com.github.se.studentconnect.ui.navigation.Tab
import com.github.se.studentconnect.ui.navigation.bottomNavigationTabs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationComponentsTest {

  @Test
  fun routes_haveCorrectConstants() {
    assertEquals("auth", Route.AUTH)
    assertEquals("home", Route.HOME)
    assertEquals("map", Route.MAP)
    assertEquals("activities", Route.ACTIVITIES)
    assertEquals("profile", Route.PROFILE)
  }

  @Test
  fun screens_haveCorrectProperties() {
    val expectedScreens =
        mapOf(
            Screen.Auth to Triple(Route.AUTH, "Authentication", false),
            Screen.Home to Triple(Route.HOME, "Home", true),
            Screen.Map to Triple(Route.MAP, "Map", true),
            Screen.Activities to Triple(Route.ACTIVITIES, "Activities", true),
            Screen.Profile to Triple(Route.PROFILE, "Profile", true))

    expectedScreens.forEach { (screen, expected) ->
      assertEquals("Route mismatch for ${screen::class.simpleName}", expected.first, screen.route)
      assertEquals("Name mismatch for ${screen::class.simpleName}", expected.second, screen.name)
      assertEquals(
          "Top level destination mismatch for ${screen::class.simpleName}",
          expected.third,
          screen.isTopLevelDestination)
    }
  }

  @Test
  fun tabs_haveCorrectProperties() {
    val expectedTabs =
        mapOf(
            Tab.Home to Triple("Home", R.drawable.ic_home, Screen.Home),
            Tab.Map to Triple("Map", R.drawable.ic_vector, Screen.Map),
            Tab.Activities to Triple("Activities", R.drawable.ic_ticket, Screen.Activities),
            Tab.Profile to Triple("Profile", R.drawable.ic_user, Screen.Profile))

    expectedTabs.forEach { (tab, expected) ->
      assertEquals(
          "Name mismatch for ${tab::class.simpleName}", expected.first, tab.destination.name)
      assertEquals("Icon mismatch for ${tab::class.simpleName}", expected.second, tab.icon)
      assertEquals(
          "Destination mismatch for ${tab::class.simpleName}", expected.third, tab.destination)
    }
  }

  @Test
  fun bottomNavigationTabs_isCorrectlyConfigured() {
    val expectedTabs = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile)

    assertEquals("Incorrect number of tabs", 4, bottomNavigationTabs.size)
    assertEquals("Incorrect tab order", expectedTabs, bottomNavigationTabs)

    expectedTabs.forEach { tab ->
      assertTrue("Missing tab: ${tab.destination.name}", bottomNavigationTabs.contains(tab))
    }
  }

  @Test
  fun navigationStructure_maintainsConsistency() {
    val topLevelScreens = listOf(Screen.Home, Screen.Map, Screen.Activities, Screen.Profile)
    val bottomNavScreens = bottomNavigationTabs.map { it.destination }

    topLevelScreens.forEach { screen ->
      assertTrue(
          "Top level screen ${screen.name} not found in bottom navigation",
          bottomNavScreens.contains(screen))
      assertTrue("Screen ${screen.name} should be top level", screen.isTopLevelDestination)
    }

    assertFalse(
        "Auth screen should not be in bottom navigation", bottomNavScreens.contains(Screen.Auth))
    assertFalse("Auth screen should not be top level", Screen.Auth.isTopLevelDestination)
  }
}
