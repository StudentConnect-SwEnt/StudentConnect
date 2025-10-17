//package com.github.se.studentconnect
//
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.assertIsSelected
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
//import androidx.compose.ui.test.onNodeWithTag
//import androidx.compose.ui.test.performClick
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.github.se.studentconnect.resources.C
//import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class MainActivityUITest {
//
//  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
//
//  /**
//   * Test to verify that MainActivity displays the main screen container and bottom navigation bar
//   * correctly.
//   */
//  @Test
//  fun mainActivity_displaysCorrectly() {
//    composeTestRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
//    composeTestRule.onNodeWithTag(C.Tag.bottom_navigation_menu).assertIsDisplayed()
//  }
//
//  /** Test to verify that navigation to the Activities tab works correctly. */
//  @Test
//  fun mainActivity_navigationToActivitiesWorks() {
//    composeTestRule.onNodeWithTag(C.Tag.activities_tab).performClick()
//    composeTestRule.onNodeWithTag(C.Tag.activities_tab).assertIsSelected()
//  }
//
//  /** Test to verify that navigation to the Profile tab works correctly. */
//  @Test
//  fun mainActivity_navigationToProfileWorks() {
//    composeTestRule.onNodeWithTag(C.Tag.profile_tab).performClick()
//    composeTestRule.onNodeWithTag(C.Tag.profile_tab).assertIsSelected()
//  }
//
//  /** Test to verify that navigation to the center button doesn't crash the app on one click. */
//  @Test
//  fun mainActivity_centerButtonClickDoesNotCrash() {
//    composeTestRule.onNodeWithTag("center_add_button").performClick()
//    // The bottom sheet should be displayed
//    composeTestRule.onNodeWithTag("event_creation_bottom_sheet").assertIsDisplayed()
//  }
//
//  /** Test to verify that all tabs in the bottom navigation bar are displayed correctly. */
//  @Test
//  fun mainActivity_allTabsAreDisplayed() {
//    composeTestRule.onNodeWithTag(C.Tag.home_tab).assertIsDisplayed()
//    composeTestRule.onNodeWithTag(C.Tag.map_tab).assertIsDisplayed()
//    composeTestRule.onNodeWithTag(C.Tag.activities_tab).assertIsDisplayed()
//    composeTestRule.onNodeWithTag(C.Tag.profile_tab).assertIsDisplayed()
//    composeTestRule.onNodeWithTag("center_add_button").assertIsDisplayed()
//  }
//
//  /** Test to verify that the navigation host is displayed correctly in MainActivity. */
//  @Test
//  fun mainActivity_navHostIsDisplayed() {
//    composeTestRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
//  }
//
//  /** Test to verify that clicking the already selected Home tab does not change the selection. */
//  @Test
//  fun mainActivity_singleTopNavigation() {
//    composeTestRule.onNodeWithTag(C.Tag.home_tab).assertIsSelected()
//
//    composeTestRule.onNodeWithTag(C.Tag.home_tab).performClick()
//    composeTestRule.onNodeWithTag(C.Tag.home_tab).assertIsSelected()
//  }
//
//  /**
//   * Test to verify that the scaffold structure of MainActivity is correct with main container and
//   * bottom navigation.
//   */
//  @Test
//  fun mainActivity_scaffoldStructureIsCorrect() {
//    composeTestRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
//    composeTestRule.onNodeWithTag(C.Tag.bottom_navigation_menu).assertIsDisplayed()
//  }
//
//  @Test
//  fun fullNavigationJourney_throughAllTabs() {
//    // Starts on Home
//    composeTestRule.onNodeWithTag(C.Tag.home_tab).assertIsSelected()
//    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
//
//    // To Activities
//    composeTestRule.onNodeWithTag(C.Tag.activities_tab).performClick()
//    composeTestRule.onNodeWithTag(C.Tag.activities_tab).assertIsSelected()
//    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN).assertIsDisplayed()
//
//    // Back to Home
//    composeTestRule.onNodeWithTag(C.Tag.home_tab).performClick()
//    composeTestRule.onNodeWithTag(C.Tag.home_tab).assertIsSelected()
//    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
//  }
//}
