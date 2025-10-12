package com.github.se.studentconnect.ui.screen.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.MainActivity
import com.github.se.studentconnect.ui.navigation.NavigationTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun mainActivity_displaysNavigationBarWithAllTabs() {
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    val expectedTabs = listOf("Home", "Map", "Activities", "Profile")
    val tabTags =
        listOf(
            NavigationTestTags.HOME_TAB,
            NavigationTestTags.MAP_TAB,
            NavigationTestTags.ACTIVITIES_TAB,
            NavigationTestTags.PROFILE_TAB)

    expectedTabs.zip(tabTags).forEach { (expectedText, tag) ->
      composeTestRule.onNodeWithTag(tag).assertIsDisplayed().assertTextContains(expectedText)
    }

    composeTestRule.onNodeWithTag("center_add_button").assertIsDisplayed()
  }
}
