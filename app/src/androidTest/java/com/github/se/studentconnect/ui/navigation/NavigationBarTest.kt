package com.github.se.studentconnect.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bottomNavigationBar_isDisplayed() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_showsAllTabs() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_showsCenterButton() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    composeTestRule.onNodeWithTag("center_add_button").assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_homeTabIsSelectedByDefault() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsSelected()
  }

  @Test
  fun bottomNavigationBar_tabSelectionWorks() {
    composeTestRule.setContent {
      AppTheme {
        var selectedTab by remember { mutableStateOf<Tab>(Tab.Home) }
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onCenterButtonClick = {})
      }
    }

    // Click on Map tab
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()

    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsSelected()
  }

  @Test
  fun bottomNavigationBar_centerButtonClickWorks() {
    var centerButtonClicked = false

    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCenterButtonClick = { centerButtonClicked = true })
      }
    }

    composeTestRule.onNodeWithTag("center_add_button").performClick()

    assert(centerButtonClicked)
  }

  @Test
  fun bottomNavigationBar_activitiesTabSelection() {
    composeTestRule.setContent {
      AppTheme {
        var selectedTab by remember { mutableStateOf<Tab>(Tab.Home) }
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onCenterButtonClick = {})
      }
    }

    // Click on Activities tab
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).performClick()

    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsSelected()
  }

  @Test
  fun bottomNavigationBar_profileTabSelection() {
    composeTestRule.setContent {
      AppTheme {
        var selectedTab by remember { mutableStateOf<Tab>(Tab.Home) }
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onCenterButtonClick = {})
      }
    }

    // Click on Profile tab
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()

    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsSelected()
  }
}
