package com.github.se.studentconnect

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.navigation.NavigationTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUITest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun mainActivity_displaysCorrectly() {
    composeTestRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun mainActivity_navigationToActivitiesWorks() {
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsSelected()
  }

  @Test
  fun mainActivity_navigationToProfileWorks() {
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsSelected()
  }

  @Test
  fun mainActivity_centerButtonClickDoesNotCrash() {
    composeTestRule.onNodeWithTag("center_add_button").performClick()
    composeTestRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
  }

  @Test
  fun mainActivity_centerButtonMultipleClicks() {
    repeat(3) { composeTestRule.onNodeWithTag("center_add_button").performClick() }
    composeTestRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
  }

  @Test
  fun mainActivity_allTabsAreDisplayed() {
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.ACTIVITIES_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag("center_add_button").assertIsDisplayed()
  }

  @Test
  fun mainActivity_navHostIsDisplayed() {
    composeTestRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
  }

  @Test
  fun mainActivity_singleTopNavigation() {
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsSelected()

    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOME_TAB).assertIsSelected()
  }

  @Test
  fun mainActivity_scaffoldStructureIsCorrect() {
    composeTestRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }
}
