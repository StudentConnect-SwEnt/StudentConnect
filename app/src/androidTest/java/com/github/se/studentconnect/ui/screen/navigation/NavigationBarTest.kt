package com.github.se.studentconnect.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BottomNavigationBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bottomNavigationBar_displaysAllTabs() {
    composeTestRule.setContent { BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}) }

    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Home)).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Map)).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Activities))
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Profile)).assertIsDisplayed()
    composeTestRule.onNodeWithTag("center_add_button").assertIsDisplayed()
  }

  @Test
  fun clickingOnTab_triggersOnTabSelectedCallback() {
    var selectedTab: Tab = Tab.Home
    composeTestRule.setContent {
      BottomNavigationBar(
          selectedTab = selectedTab, onTabSelected = { newTab -> selectedTab = newTab })
    }
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Activities)).performClick()
    assertEquals(Tab.Activities, selectedTab)
  }

  @Test
  fun eventCreationBottomSheet_displaysCorrectContent() {
    composeTestRule.setContent { BottomNavigationBar(selectedTab = Tab.Home, onTabSelected = {}) }
    composeTestRule.onNodeWithTag("center_add_button").performClick()

    composeTestRule.onNodeWithTag("bottom_sheet_title").assertTextContains("Create New Event")
    composeTestRule.onNodeWithTag("create_public_event_option").assertIsDisplayed()
    composeTestRule.onNodeWithText("Create Public Event").assertIsDisplayed()
    composeTestRule.onNodeWithText("Visible to everyone").assertIsDisplayed()
    composeTestRule.onNodeWithTag("create_private_event_option").assertIsDisplayed()
    composeTestRule.onNodeWithText("Create Private Event").assertIsDisplayed()
    composeTestRule.onNodeWithText("Invite only").assertIsDisplayed()
  }

  @Test
  fun selectingEventCreationOption_triggersCallbackAndClosesSheet() {
    // Arrange
    var createPublicCalled = false
    var createPrivateCalled = false
    composeTestRule.setContent {
      BottomNavigationBar(
          selectedTab = Tab.Home,
          onTabSelected = {},
          onCreatePublicEvent = { createPublicCalled = true },
          onCreatePrivateEvent = { createPrivateCalled = true })
    }

    // Act: Option Publique
    composeTestRule.onNodeWithTag("center_add_button").performClick()
    composeTestRule.onNodeWithTag("create_public_event_option").performClick()

    // Assert: Option Publique
    assert(createPublicCalled)
    composeTestRule.onNodeWithTag("event_creation_bottom_sheet").assertDoesNotExist()

    // Act: Option Privée
    composeTestRule.onNodeWithTag("center_add_button").performClick()
    composeTestRule.onNodeWithTag("create_private_event_option").performClick()

    // Assert: Option Privée
    assert(createPrivateCalled)
    composeTestRule.onNodeWithTag("event_creation_bottom_sheet").assertDoesNotExist()
  }
}
