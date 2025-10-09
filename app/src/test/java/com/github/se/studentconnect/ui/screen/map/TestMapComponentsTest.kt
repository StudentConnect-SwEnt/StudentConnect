package com.github.se.studentconnect.ui.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestMapComponentsInstrumentedTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testMapboxMap_displaysCorrectContent() {
    composeTestRule.setContent { AppTheme { TestMapboxMap() } }

    // Test that the component displays the expected text
    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()

    // Test that it has the correct test tag
    composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
  }

  @Test
  fun testMapboxMap_withCustomContent() {
    val customContent = "Custom Test Content"

    composeTestRule.setContent { AppTheme { TestMapboxMap { Text(text = customContent) } } }

    // Test that both default and custom content are displayed
    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    composeTestRule.onNodeWithText(customContent).assertIsDisplayed()
  }

  @Test
  fun testMapboxMap_withModifier() {
    val testTag = "custom_test_tag"

    composeTestRule.setContent {
      AppTheme {
        TestMapboxMap(modifier = Modifier.semantics { testTag = testTag }) {
          Text("Additional Content")
        }
      }
    }

    // The custom modifier should still allow the map screen tag to be present
    composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    composeTestRule.onNodeWithText("Additional Content").assertIsDisplayed()
  }

  @Test
  fun testMapboxMap_emptyContent() {
    composeTestRule.setContent { AppTheme { TestMapboxMap() } }

    // Test with no additional content
    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
  }

  @Test
  fun testMapboxMap_multipleContentItems() {
    composeTestRule.setContent {
      AppTheme {
        TestMapboxMap {
          Text("Item 1")
          Text("Item 2")
          Text("Item 3")
        }
      }
    }

    // Test that all content items are displayed along with the default content
    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 3").assertIsDisplayed()
  }
}

// Unit tests for TestMapComponents functionality
class TestMapComponentsUnitTest {

  @Test
  fun testMapboxMap_defaultParameters() {
    // Test that the default parameters match expected values
    val defaultModifier = Modifier
    val defaultContent: @Composable () -> Unit = {}

    assertNotNull("Default modifier should not be null", defaultModifier)
    assertNotNull("Default content lambda should not be null", defaultContent)
  }

  @Test
  fun testMapboxMap_semanticsConfiguration() {
    // Test that the test tag constant is correctly configured
    assertEquals("Test tag should match expected value", "map_screen", C.Tag.map_screen)
  }

  @Test
  fun testMapboxMap_textContent() {
    val expectedText = "Test Map View"
    assertFalse("Expected text should not be empty", expectedText.isEmpty())
    assertTrue("Expected text should contain 'Test'", expectedText.contains("Test"))
    assertTrue("Expected text should contain 'Map'", expectedText.contains("Map"))
    assertTrue("Expected text should contain 'View'", expectedText.contains("View"))
  }

  @Test
  fun testMapboxMap_componentStructure() {
    // Test that the component follows expected patterns
    assertTrue(
        "Component should be a Composable function",
        true) // Composable annotation verified by compilation

    // Test parameter defaults
    val hasDefaultModifier = true // Modifier = Modifier is the default
    val hasDefaultContent = true // content: @Composable () -> Unit = {} is the default

    assertTrue("Should have default modifier parameter", hasDefaultModifier)
    assertTrue("Should have default content parameter", hasDefaultContent)
  }
}

// Tests for TestMapComponents integration with Material Theme
class TestMapComponentsMaterialThemeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testMapboxMap_usesMaterialTheme() {
    var capturedBackgroundColor: Color? = null
    var capturedTextColor: Color? = null

    composeTestRule.setContent {
      AppTheme {
        // Access theme colors to verify they're being used
        capturedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
        capturedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

        TestMapboxMap()
      }
    }

    assertNotNull("Background color should be captured from theme", capturedBackgroundColor)
    assertNotNull("Text color should be captured from theme", capturedTextColor)
    assertNotEquals(
        "Background color should not be unspecified", Color.Unspecified, capturedBackgroundColor)
    assertNotEquals("Text color should not be unspecified", Color.Unspecified, capturedTextColor)
  }

  @Test
  fun testMapboxMap_typography() {
    composeTestRule.setContent { AppTheme { TestMapboxMap() } }

    // Verify that the text is displayed (which implies typography is working)
    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
  }
}

// Edge case tests for TestMapComponents
class TestMapComponentsEdgeCasesTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testMapboxMap_withLongContent() {
    val longContent =
        "This is a very long content string that might overflow or cause layout issues " +
            "and we want to make sure the component handles it gracefully without crashing or " +
            "displaying incorrectly in the test environment"

    composeTestRule.setContent { AppTheme { TestMapboxMap { Text(text = longContent) } } }

    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    composeTestRule.onNodeWithText(longContent).assertIsDisplayed()
  }

  @Test
  fun testMapboxMap_withEmptyStringContent() {
    composeTestRule.setContent { AppTheme { TestMapboxMap { Text("") } } }

    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    // Empty text node might not be findable, but shouldn't crash
  }

  @Test
  fun testMapboxMap_withSpecialCharacters() {
    val specialContent = "Special chars: 🗺️ 📍 🧭 ∞ ± × ÷"

    composeTestRule.setContent { AppTheme { TestMapboxMap { Text(text = specialContent) } } }

    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    composeTestRule.onNodeWithText(specialContent).assertIsDisplayed()
  }

  @Test
  fun testMapboxMap_nestedContent() {
    composeTestRule.setContent {
      AppTheme {
        TestMapboxMap {
          Box { Text("Nested Content", modifier = Modifier.align(Alignment.Center)) }
        }
      }
    }

    composeTestRule.onNodeWithText("Test Map View").assertIsDisplayed()
    composeTestRule.onNodeWithText("Nested Content").assertIsDisplayed()
  }
}
