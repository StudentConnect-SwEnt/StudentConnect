package com.github.se.studentconnect.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class NavigationScaffoldTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  private fun waitForTag(tag: String) {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  // --------------------------------------------------
  // 1. Bottom Navigation Bar Tests
  // --------------------------------------------------

  @Test
  fun bottomNavigationBar_isDisplayed() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun homeTab_isDisplayed() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag(NavigationTestTags.getTabTestTag(Tab.Home))
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Home)).assertIsDisplayed()
  }

  @Test
  fun mapTab_isDisplayed() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag(NavigationTestTags.getTabTestTag(Tab.Map))
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Map)).assertIsDisplayed()
  }

  @Test
  fun activitiesTab_isDisplayed() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag(NavigationTestTags.getTabTestTag(Tab.Activities))
    composeTestRule
        .onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Activities))
        .assertIsDisplayed()
  }

  @Test
  fun profileTab_isDisplayed() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag(NavigationTestTags.getTabTestTag(Tab.Profile))
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Profile)).assertIsDisplayed()
  }

  @Test
  fun centerAddButton_isDisplayed() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag("center_add_button")
    composeTestRule.onNodeWithTag("center_add_button").assertIsDisplayed()
  }

  // --------------------------------------------------
  // 2. Interaction Tests
  // --------------------------------------------------

  @Test
  fun homeTab_isClickable() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag(NavigationTestTags.getTabTestTag(Tab.Home))
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Home)).assertHasClickAction()
  }

  @Test
  fun mapTab_isClickable() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag(NavigationTestTags.getTabTestTag(Tab.Map))
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Map)).assertHasClickAction()
  }

  @Test
  fun centerAddButton_isClickable() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag("center_add_button")
    composeTestRule.onNodeWithTag("center_add_button").assertHasClickAction()
  }

  @Test
  fun centerAddButton_click_opensBottomSheet() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag("center_add_button")
    composeTestRule.onNodeWithTag("center_add_button").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodes(hasTestTag("event_creation_bottom_sheet"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag("event_creation_bottom_sheet").assertIsDisplayed()
  }

  // --------------------------------------------------
  // 3. Tab Selection Tests
  // --------------------------------------------------

  @Test
  fun tabSelection_callsOnTabSelected() {
    var selectedTab: Tab? = null

    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = { selectedTab = it },
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag(NavigationTestTags.getTabTestTag(Tab.Map))
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Map)).performClick()

    assert(selectedTab == Tab.Map)
  }

  @Test
  fun profileTab_selection_callsOnTabSelected() {
    var selectedTab: Tab? = null

    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = { selectedTab = it },
            onCreatePublicEvent = {},
            onCreatePrivateEvent = {},
            onCreateFromTemplate = {})
      }
    }

    waitForTag(NavigationTestTags.getTabTestTag(Tab.Profile))
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Profile)).performClick()

    assert(selectedTab == Tab.Profile)
  }
}
