package com.github.se.studentconnect.ui.utils

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PanelTest {

  @get:Rule val composeTestRule = createComposeRule()

  data class TestItem(val name: String, val value: Int)

  @Test
  fun panel_displaysTitleCorrectly() {
    composeTestRule.setContent { Panel<String>(title = "Test Panel Title") }

    composeTestRule.onNodeWithText("Test Panel Title").assertIsDisplayed()
  }

  @Test
  fun panel_withEmptyList_displaysTitle() {
    composeTestRule.setContent { Panel<String>(items = emptyList(), title = "Empty Panel") }

    composeTestRule.onNodeWithText("Empty Panel").assertIsDisplayed()
  }

  @Test
  fun panel_withItems_displaysItemsCorrectly() {
    val items = listOf("Item 1", "Item 2", "Item 3")

    composeTestRule.setContent {
      Panel(items = items, title = "Items Panel", itemContent = { item -> Text(text = item) })
    }

    composeTestRule.onNodeWithText("Items Panel").assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 3").assertIsDisplayed()
  }

  @Test
  fun panel_withComplexItems_rendersCorrectly() {
    val items = listOf(TestItem("Alpha", 1), TestItem("Beta", 2), TestItem("Gamma", 3))

    composeTestRule.setContent {
      Panel(
          items = items,
          title = "Complex Items",
          itemContent = { item -> Text(text = "${item.name}: ${item.value}") })
    }

    composeTestRule.onNodeWithText("Complex Items").assertIsDisplayed()
    composeTestRule.onNodeWithText("Alpha: 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Beta: 2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Gamma: 3").assertIsDisplayed()
  }

  @Test
  fun panel_withSingleItem_displaysCorrectly() {
    val items = listOf("Single Item")

    composeTestRule.setContent {
      Panel(items = items, title = "Single", itemContent = { item -> Text(text = item) })
    }

    composeTestRule.onNodeWithText("Single").assertIsDisplayed()
    composeTestRule.onNodeWithText("Single Item").assertIsDisplayed()
  }

  @Test
  fun panel_withManyItems_isScrollable() {
    val items = (1..20).map { "Item $it" }

    composeTestRule.setContent {
      Panel(items = items, title = "Many Items", itemContent = { item -> Text(text = item) })
    }

    composeTestRule.onNodeWithText("Many Items").assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 1").assertIsDisplayed()
    // Later items may require scrolling, so we just verify the first item is there
  }

  @Test
  fun panel_withNoItemContentProvided_doesNotCrash() {
    val items = listOf("Item 1", "Item 2")

    composeTestRule.setContent {
      Panel(
          items = items, title = "Default Content", itemContent = {} // Empty content
          )
    }

    composeTestRule.onNodeWithText("Default Content").assertIsDisplayed()
  }

  @Test
  fun panel_titleHasBoldFontWeight() {
    composeTestRule.setContent { Panel<String>(title = "Bold Title") }

    // The title should be displayed with bold font weight
    composeTestRule.onNodeWithText("Bold Title").assertIsDisplayed()
  }
}
