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

  /** Test to verify that all components of the BottomNavigationBar are displayed correctly. */
  @Test
  fun bottomNavigationBar_displaysAllComponents() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}, onCenterButtonClick = {})
      }
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag("center_add_button").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Add").assertIsDisplayed()
  }

  /** Test to verify that the BottomNavigationBar displays the correct labels for each tab. */
  @Test
  fun bottomNavigationBar_showsCorrectLabels() {
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

  /** Test to verify that tab selection updates the selected state correctly. */
  @Test
  fun bottomNavigationBar_tabSelectionWorks() {
    composeTestRule.setContent {
      AppTheme {
        var selectedTab by remember { mutableStateOf<Tab>(Tab.Home) }
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onCenterButtonClick = {},
        )
      }
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsSelected()

    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsSelected()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsNotSelected()

    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsSelected()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsNotSelected()

    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsSelected()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsNotSelected()
  }

  /** Test to verify that clicking the center button triggers the provided callback. */
  @Test
  fun bottomNavigationBar_centerButtonClickWorks() {
    var clickCount = 0

    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCenterButtonClick = { clickCount++ },
        )
      }
    }

    composeTestRule.onNodeWithTag("center_add_button").performClick()
    assert(clickCount == 1)

    repeat(2) { composeTestRule.onNodeWithTag("center_add_button").performClick() }
    composeTestRule.runOnIdle { assert(clickCount == 3) }
  }

  /** Test to verify that the BottomNavigationBar handles default parameters correctly. */
  @Test
  fun bottomNavigationBar_handlesDefaultParameters() {
    composeTestRule.setContent {
      AppTheme { BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}) }
    }

    composeTestRule.onNodeWithTag("center_add_button").performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  /** Test to verify that a custom modifier is applied correctly to the BottomNavigationBar. */
  @Test
  fun bottomNavigationBar_supportsCustomModifier() {
    composeTestRule.setContent {
      AppTheme {
        BottomNavigationBar(
            selectedTab = Tab.Home,
            onTabSelected = {},
            onCenterButtonClick = {},
            modifier = Modifier.testTag("custom_navigation"),
        )
      }
    }

    composeTestRule.onNodeWithTag("custom_navigation").assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  /**
   * Test to verify that the BottomNavigationBar maintains selection state across recompositions.
   */
  @Test
  fun bottomNavigationBar_maintainsSelectionState() {
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
}
