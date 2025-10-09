package com.github.se.studentconnect.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for navigation, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest : TestCase() {
  @get:Rule val composeTestRule = createAndroidComposeRule<BottomNavigationTestActivity>()

  @Test
  fun bottomNavigationBarTest() = run {
    step("Start Test Activity and check BottomNavigationBar") {
      composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(NavigationTestTags.HOME_TAB)
          .assertIsDisplayed()
          .assertTextContains("Home")
      composeTestRule
          .onNodeWithTag(NavigationTestTags.MAP_TAB)
          .assertIsDisplayed()
          .assertTextContains("Map")
      composeTestRule.onNodeWithTag(NavigationTestTags.CREATE_EVENT_TAB).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(NavigationTestTags.EVENTS_TAB)
          .assertIsDisplayed()
          .assertTextContains("Events")
      composeTestRule
          .onNodeWithTag(NavigationTestTags.PROFILE_TAB)
          .assertIsDisplayed()
          .assertTextContains("Profile")
    }
  }
}
