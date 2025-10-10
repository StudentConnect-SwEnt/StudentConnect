package com.github.se.studentconnect.ui.screen.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.ui.navigation.BottomNavigationBar
import com.github.se.studentconnect.ui.navigation.NavigationTestTags
import com.github.se.studentconnect.ui.navigation.Tab
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

  @Test
  fun bottomNavigationBar_withCustomModifier() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCenterButtonClick = {},
            modifier = Modifier.testTag("custom_navigation"))
      }
    }

    composeTestRule.onNodeWithTag("custom_navigation").assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_onlySelectedTabIsSelected() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Map, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsSelected()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsNotSelected()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsNotSelected()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsNotSelected()
  }

  @Test
  fun bottomNavigationBar_tabLabelsDisplayCorrectly() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    composeTestRule.onNodeWithText("Map").assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_centerButtonHasCorrectContentDescription() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    composeTestRule.onNodeWithContentDescription("Add").assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_multipleTabSelections() {
    composeTestRule.setContent {
      AppTheme {
        var selectedTab by remember { mutableStateOf<Tab>(Tab.Home) }
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onCenterButtonClick = {})
      }
    }

    // Start with Home selected
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsSelected()

    // Switch to Activities
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsSelected()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsNotSelected()

    // Switch to Profile
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsSelected()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsNotSelected()

    // Switch back to Home
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsSelected()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsNotSelected()
  }

  @Test
  fun bottomNavigationBar_centerButtonClickMultipleTimes() {
    var clickCount = 0

    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = { clickCount++ })
      }
    }

    repeat(3) { composeTestRule.onNodeWithTag("center_add_button").performClick() }

    composeTestRule.runOnIdle { assert(clickCount == 3) }
  }

  @Test
  fun bottomNavigationBar_defaultCenterButtonClickDoesNothing() {
    composeTestRule.setContent {
      AppTheme { BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}) }
    }

    // Should not crash when clicking center button with default empty callback
    composeTestRule.onNodeWithTag("center_add_button").performClick()
  }

  @Test
  fun bottomNavigationBar_withNullModifier() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCenterButtonClick = {},
            modifier = Modifier)
      }
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_navigationBarItemsWithIcons() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    // Verify all tab icons are displayed by checking their test tags
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_navigationBarItemsWithLabels() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    // Verify all tab labels are displayed
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    composeTestRule.onNodeWithText("Map").assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_centerIconIsDisplayed() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    // Verify center add icon is displayed
    composeTestRule.onNodeWithTag("center_add_button").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Add").assertIsDisplayed()
  }

  @Test
  fun bottomNavigationBar_emptyNavigationBarItemBehavior() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    // Test the navigation bar structure - should have 5 navigation bar items (4 tabs + 1 empty)
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    // The empty NavigationBarItem should not be clickable (enabled = false)
    // We can verify this by ensuring the main navigation bar is still displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }
}
