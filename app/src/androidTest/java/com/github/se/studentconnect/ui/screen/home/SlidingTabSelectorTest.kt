package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class SlidingTabSelectorTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun slidingTabSelector_displaysAllTabs() {
    // Arrange
    var selectedTab = HomeTabMode.FOR_YOU

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
    }

    // Assert
    composeTestRule.onNodeWithText("For You").assertIsDisplayed()
    composeTestRule.onNodeWithText("All Events").assertIsDisplayed()
    composeTestRule.onNodeWithText("Discover").assertIsDisplayed()
  }

  @Test
  fun slidingTabSelector_displaysTabSelector() {
    // Arrange
    var selectedTab = HomeTabMode.FOR_YOU

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
    }

    // Assert
    composeTestRule.onNodeWithTag("tab_selector").assertIsDisplayed()
  }

  @Test
  fun slidingTabSelector_displaysTabIndicator() {
    // Arrange
    var selectedTab = HomeTabMode.FOR_YOU

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
    }

    // Assert
    composeTestRule.onNodeWithTag("tab_indicator").assertIsDisplayed()
  }

  @Test
  fun slidingTabSelector_clickForYouTab_callsOnTabSelected() {
    // Arrange
    var selectedTab = HomeTabMode.EVENTS
    var callbackInvoked = false

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(
          selectedTab = selectedTab,
          onTabSelected = {
            selectedTab = it
            callbackInvoked = true
          })
    }

    composeTestRule.onNodeWithTag("tab_for_you").performClick()

    // Assert
    assertEquals(HomeTabMode.FOR_YOU, selectedTab)
    assertEquals(true, callbackInvoked)
  }

  @Test
  fun slidingTabSelector_clickEventsTab_callsOnTabSelected() {
    // Arrange
    var selectedTab = HomeTabMode.FOR_YOU
    var callbackInvoked = false

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(
          selectedTab = selectedTab,
          onTabSelected = {
            selectedTab = it
            callbackInvoked = true
          })
    }

    composeTestRule.onNodeWithTag("tab_events").performClick()

    // Assert
    assertEquals(HomeTabMode.EVENTS, selectedTab)
    assertEquals(true, callbackInvoked)
  }

  @Test
  fun slidingTabSelector_clickDiscoverTab_callsOnTabSelected() {
    // Arrange
    var selectedTab = HomeTabMode.FOR_YOU
    var callbackInvoked = false

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(
          selectedTab = selectedTab,
          onTabSelected = {
            selectedTab = it
            callbackInvoked = true
          })
    }

    composeTestRule.onNodeWithTag("tab_discover").performClick()

    // Assert
    assertEquals(HomeTabMode.DISCOVER, selectedTab)
    assertEquals(true, callbackInvoked)
  }

  @Test
  fun slidingTabSelector_switchBetweenTabs() {
    // Arrange
    var selectedTab = HomeTabMode.FOR_YOU

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
    }

    // Click Events tab
    composeTestRule.onNodeWithTag("tab_events").performClick()
    assertEquals(HomeTabMode.EVENTS, selectedTab)

    // Click Discover tab
    composeTestRule.onNodeWithTag("tab_discover").performClick()
    assertEquals(HomeTabMode.DISCOVER, selectedTab)

    // Click For You tab
    composeTestRule.onNodeWithTag("tab_for_you").performClick()
    assertEquals(HomeTabMode.FOR_YOU, selectedTab)
  }

  @Test
  fun slidingTabSelector_forYouTabDisplayed() {
    // Arrange
    var selectedTab = HomeTabMode.FOR_YOU

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
    }

    // Assert
    composeTestRule.onNodeWithTag("tab_for_you").assertIsDisplayed()
  }

  @Test
  fun slidingTabSelector_eventsTabDisplayed() {
    // Arrange
    var selectedTab = HomeTabMode.FOR_YOU

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
    }

    // Assert
    composeTestRule.onNodeWithTag("tab_events").assertIsDisplayed()
  }

  @Test
  fun slidingTabSelector_discoverTabDisplayed() {
    // Arrange
    var selectedTab = HomeTabMode.FOR_YOU

    // Act
    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
    }

    // Assert
    composeTestRule.onNodeWithTag("tab_discover").assertIsDisplayed()
  }

  @Test
  fun slidingTabSelector_initialSelectedTabIsForYou() {
    // Arrange & Act
    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = HomeTabMode.FOR_YOU, onTabSelected = {})
    }

    // Assert
    composeTestRule.onNodeWithText("For You").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_for_you").assertIsDisplayed()
  }

  @Test
  fun slidingTabSelector_selectedTabCanBeDiscover() {
    // Arrange & Act
    composeTestRule.setContent {
      SlidingTabSelector(selectedTab = HomeTabMode.DISCOVER, onTabSelected = {})
    }

    // Assert
    composeTestRule.onNodeWithText("Discover").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_discover").assertIsDisplayed()
  }
}
