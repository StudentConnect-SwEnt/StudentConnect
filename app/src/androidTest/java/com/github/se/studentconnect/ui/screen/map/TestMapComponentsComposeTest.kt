package com.github.se.studentconnect.ui.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.resources.C
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestMapboxMapComposeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testMapboxMap_displaysBasicComponentCorrectly() {
    composeTestRule.setContent { TestMapboxMap() }

    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(C.Tag.map_screen)
        .assertWidthIsAtLeast(1.dp)
        .assertHeightIsAtLeast(1.dp)
  }

  @Test
  fun testMapboxMap_withCustomModifier() {
    composeTestRule.setContent { TestMapboxMap(modifier = Modifier.testTag("custom_test_map")) }
    composeTestRule.onNodeWithTag("custom_test_map").assertIsDisplayed()
  }

  @Test
  fun testMapboxMap_withCustomContent() {
    val customText = "Custom Content"
    composeTestRule.setContent {
      TestMapboxMap { Text(text = customText, modifier = Modifier.testTag("custom_content")) }
    }
    composeTestRule.onNodeWithTag("custom_content").assertIsDisplayed().assertTextEquals(customText)
  }

  @Test
  fun testMapboxMap_withMultipleContent() {
    composeTestRule.setContent {
      TestMapboxMap {
        Text(text = "Content 1", modifier = Modifier.testTag("content1"))
        Text(text = "Content 2", modifier = Modifier.testTag("content2"))
      }
    }
    composeTestRule.onNodeWithTag("content1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("content2").assertIsDisplayed()
  }

  @Test
  fun testMapboxMap_contentOverlaysDefaultText() {
    composeTestRule.setContent {
      TestMapboxMap { Box(modifier = Modifier.fillMaxSize().testTag("overlay_content")) }
    }

    // Both default text and overlay should be present
    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    composeTestRule.onNodeWithTag("overlay_content").assertIsDisplayed()
  }
}
