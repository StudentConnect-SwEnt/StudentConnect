package com.github.se.studentconnect.ui.screen.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testSearchPageDisplayed() {
    composeTestRule.setContent { AppTheme { TestSearchScreen() } }
    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsDisplayed()
  }

  @Test
  fun testSearchUserDisplayed() {
    composeTestRule.setContent { AppTheme { TestSearchScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("user")

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsNotDisplayed()
  }

  @Test
  fun testSearchEventDisplayed() {
    composeTestRule.setContent { AppTheme { TestSearchScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("sample")

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsDisplayed()
  }

  @Test
  fun testSearchEventDisplayedWhenNotStart() {
    composeTestRule.setContent { AppTheme { TestSearchScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("event")

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsDisplayed()
  }

  @Test
  fun testSearchNothingDisplayed() {
    composeTestRule.setContent { AppTheme { TestSearchScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("nothing")

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsNotDisplayed()
  }
}
