package com.github.se.studentconnect.ui.chat

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class TypingIndicatorTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun typingIndicator_notDisplayed_whenUserListIsEmpty() {
    composeTestRule.setContent { TypingIndicator(typingUserNames = emptyList()) }

    composeTestRule.onNodeWithTag("typing_indicator").assertDoesNotExist()
  }

  @Test
  fun typingIndicator_displaysSingleUser() {
    val users = listOf("Alice")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithText("Alice is typing", substring = true).assertIsDisplayed()
  }

  @Test
  fun typingIndicator_displaysTwoUsers() {
    val users = listOf("Alice", "Bob")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithText("Alice and Bob are typing", substring = true).assertIsDisplayed()
  }

  @Test
  fun typingIndicator_displaysMultipleUsers() {
    val users = listOf("Alice", "Bob", "Charlie")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Alice and 2 others are typing", substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun typingIndicator_displaysText() {
    val users = listOf("John")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator_text").assertIsDisplayed()
  }

  @Test
  fun typingIndicator_displaysAnimatedDots() {
    val users = listOf("Alice")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    // Check that all three dots are displayed
    composeTestRule.onNodeWithTag("typing_indicator_dot_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("typing_indicator_dot_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("typing_indicator_dot_2").assertIsDisplayed()
  }

  @Test
  fun typingIndicator_handlesLongUserName() {
    val users = listOf("VeryLongUserNameThatMightCauseLayoutIssues")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("VeryLongUserNameThatMightCauseLayoutIssues is typing", substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun typingIndicator_handlesSpecialCharactersInUserName() {
    val users = listOf("User@123")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithText("User@123 is typing", substring = true).assertIsDisplayed()
  }

  @Test
  fun typingIndicator_handlesFourOrMoreUsers() {
    val users = listOf("Alice", "Bob", "Charlie", "David")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Alice and 3 others are typing", substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun typingIndicator_handlesUnicodeUserNames() {
    val users = listOf("世界", "مرحبا")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithText("世界 and مرحبا are typing", substring = true).assertIsDisplayed()
  }

  @Test
  fun typingIndicator_handlesEmptyStringInUserList() {
    val users = listOf("")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithText(" is typing", substring = true).assertIsDisplayed()
  }

  @Test
  fun typingIndicator_respectsModifier() {
    val users = listOf("Alice")

    val customModifier = Modifier.testTag("custom_tag")

    composeTestRule.setContent {
      TypingIndicator(typingUserNames = users, modifier = customModifier)
    }

    composeTestRule.onNodeWithTag("custom_tag").assertIsDisplayed()
  }

  @Test
  fun typingIndicator_displayCorrectTextForOneUser() {
    val users = listOf("SingleUser")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    val expectedText = "SingleUser is typing"
    composeTestRule.onNodeWithText(expectedText, substring = true).assertIsDisplayed()
  }

  @Test
  fun typingIndicator_displayCorrectTextForTwoUsers() {
    val users = listOf("FirstUser", "SecondUser")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    val expectedText = "FirstUser and SecondUser are typing"
    composeTestRule.onNodeWithText(expectedText, substring = true).assertIsDisplayed()
  }

  @Test
  fun typingIndicator_displayCorrectTextForThreeUsers() {
    val users = listOf("FirstUser", "SecondUser", "ThirdUser")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    val expectedText = "FirstUser and 2 others are typing"
    composeTestRule.onNodeWithText(expectedText, substring = true).assertIsDisplayed()
  }

  @Test
  fun typingIndicator_displayCorrectTextForFiveUsers() {
    val users = listOf("User1", "User2", "User3", "User4", "User5")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    val expectedText = "User1 and 4 others are typing"
    composeTestRule.onNodeWithText(expectedText, substring = true).assertIsDisplayed()
  }

  @Test
  fun typingIndicator_handlesWhitespaceInUserNames() {
    val users = listOf("John Doe", "Jane Smith")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("John Doe and Jane Smith are typing", substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun typingIndicator_hasCorrectStructure() {
    val users = listOf("Alice")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    // Verify the main container exists
    composeTestRule.onNodeWithTag("typing_indicator").assertExists()

    // Verify the text element exists
    composeTestRule.onNodeWithTag("typing_indicator_text").assertExists()

    // Verify all three animated dots exist
    composeTestRule.onNodeWithTag("typing_indicator_dot_0").assertExists()
    composeTestRule.onNodeWithTag("typing_indicator_dot_1").assertExists()
    composeTestRule.onNodeWithTag("typing_indicator_dot_2").assertExists()
  }

  @Test
  fun typingIndicator_doesNotCrashWithNullOrInvalidData() {
    // This test ensures the component handles edge cases gracefully
    val users = listOf("User1", "", "User3")

    composeTestRule.setContent { TypingIndicator(typingUserNames = users) }

    composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
  }
}
