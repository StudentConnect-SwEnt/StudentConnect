package com.github.se.studentconnect.ui.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationComponentsTest {

  @Test
  fun route_constants_areCorrect() {
    assert(Route.AUTH == "auth")
    assert(Route.HOME == "home")
    assert(Route.MAP == "map")
    assert(Route.ACTIVITIES == "activities")
    assert(Route.PROFILE == "profile")
  }

  @Test
  fun screen_auth_hasCorrectProperties() {
    assert(Screen.Auth.route == Route.AUTH)
    assert(Screen.Auth.name == "Authentication")
    assert(!Screen.Auth.isTopLevelDestination)
  }

  @Test
  fun screen_home_hasCorrectProperties() {
    assert(Screen.Home.route == Route.HOME)
    assert(Screen.Home.name == "Home")
    assert(Screen.Home.isTopLevelDestination)
  }

  @Test
  fun screen_map_hasCorrectProperties() {
    assert(Screen.Map.route == Route.MAP)
    assert(Screen.Map.name == "Map")
    assert(Screen.Map.isTopLevelDestination)
  }

  @Test
  fun screen_activities_hasCorrectProperties() {
    assert(Screen.Activities.route == Route.ACTIVITIES)
    assert(Screen.Activities.name == "Activities")
    assert(Screen.Activities.isTopLevelDestination)
  }

  @Test
  fun screen_profile_hasCorrectProperties() {
    assert(Screen.Profile.route == Route.PROFILE)
    assert(Screen.Profile.name == "Profile")
    assert(Screen.Profile.isTopLevelDestination)
  }

  @Test
  fun tab_home_hasCorrectProperties() {
    assert(Tab.Home.name == "Home")
    assert(Tab.Home.icon == R.drawable.ic_home)
    assert(Tab.Home.destination == Screen.Home)
  }

  @Test
  fun tab_map_hasCorrectProperties() {
    assert(Tab.Map.name == "Map")
    assert(Tab.Map.icon == R.drawable.ic_vector)
    assert(Tab.Map.destination == Screen.Map)
  }

  @Test
  fun tab_activities_hasCorrectProperties() {
    assert(Tab.Activities.name == "Activities")
    assert(Tab.Activities.icon == R.drawable.ic_ticket)
    assert(Tab.Activities.destination == Screen.Activities)
  }

  @Test
  fun tab_profile_hasCorrectProperties() {
    assert(Tab.Profile.name == "Profile")
    assert(Tab.Profile.icon == R.drawable.ic_user)
    assert(Tab.Profile.destination == Screen.Profile)
  }

  @Test
  fun bottomNavigationTabs_containsAllTabs() {
    assert(bottomNavigationTabs.size == 4)
    assert(bottomNavigationTabs.contains(Tab.Home))
    assert(bottomNavigationTabs.contains(Tab.Map))
    assert(bottomNavigationTabs.contains(Tab.Activities))
    assert(bottomNavigationTabs.contains(Tab.Profile))
  }

  @Test
  fun bottomNavigationTabs_hasCorrectOrder() {
    assert(bottomNavigationTabs[0] == Tab.Home)
    assert(bottomNavigationTabs[1] == Tab.Map)
    assert(bottomNavigationTabs[2] == Tab.Activities)
    assert(bottomNavigationTabs[3] == Tab.Profile)
  }

  @Test
  fun allTopLevelScreens_areInBottomNavigation() {
    val topLevelScreens = listOf(Screen.Home, Screen.Map, Screen.Activities, Screen.Profile)
    val bottomNavScreens = bottomNavigationTabs.map { it.destination }

    topLevelScreens.forEach { screen -> assert(bottomNavScreens.contains(screen)) }
  }

  @Test
  fun auth_screen_isNotInBottomNavigation() {
    val bottomNavScreens = bottomNavigationTabs.map { it.destination }
    assert(!bottomNavScreens.contains(Screen.Auth))
  }
}
