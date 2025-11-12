package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Timestamp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class StoryViewerTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testEvent =
      Event.Public(
          uid = "test-event-1",
          title = "Test Event Story",
          subtitle = "Test subtitle",
          description = "Test description for story viewer",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner-123",
          isFlash = false,
          tags = listOf("test", "story"),
      )

  @Test
  fun storyViewer_whenVisible_displaysCorrectly() {
    composeTestRule.setContent { StoryViewer(event = testEvent, isVisible = true, onDismiss = {}) }

    // Verify story viewer is displayed
    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()

    // Verify close button is displayed
    composeTestRule.onNodeWithTag("story_close_button").assertIsDisplayed()

    // Verify event title is displayed
    composeTestRule.onNodeWithText("Test Event Story").assertIsDisplayed()

    // Verify story content image is displayed
    composeTestRule.onNodeWithContentDescription("Story content").assertIsDisplayed()

    // Verify close icon is displayed
    composeTestRule.onNodeWithContentDescription("Close story").assertIsDisplayed()
  }

  @Test
  fun storyViewer_whenNotVisible_doesNotDisplay() {
    composeTestRule.setContent { StoryViewer(event = testEvent, isVisible = false, onDismiss = {}) }

    // Verify story viewer is not displayed when isVisible is false
    composeTestRule.onNodeWithTag("story_viewer").assertIsNotDisplayed()
  }

  @Test
  fun storyViewer_closeButton_triggersOnDismiss() {
    var dismissCalled = false

    composeTestRule.setContent {
      StoryViewer(event = testEvent, isVisible = true, onDismiss = { dismissCalled = true })
    }

    // Click the close button
    composeTestRule.onNodeWithTag("story_close_button").performClick()

    // Verify onDismiss was called
    assertTrue("onDismiss should be called when close button is clicked", dismissCalled)
  }

  @Test
  fun storyViewer_backgroundClick_triggersOnDismiss() {
    var dismissCalled = false

    composeTestRule.setContent {
      StoryViewer(event = testEvent, isVisible = true, onDismiss = { dismissCalled = true })
    }

    // Click on the story viewer background
    composeTestRule.onNodeWithTag("story_viewer").performClick()

    // Verify onDismiss was called
    assertTrue("onDismiss should be called when background is clicked", dismissCalled)
  }

  @Test
  fun storyViewer_displaysEventTitleCorrectly() {
    val eventWithLongTitle =
        Event.Public(
            uid = "test-event-2",
            title = "Very Long Event Title For Testing Story Viewer Display",
            subtitle = "Test subtitle",
            description = "Test description",
            start = Timestamp.now(),
            end = Timestamp.now(),
            location = Location(latitude = 46.52, longitude = 6.57, name = "Test Location"),
            website = "https://example.com",
            ownerId = "owner-456",
            isFlash = false,
            tags = listOf("test"),
        )

    composeTestRule.setContent {
      StoryViewer(event = eventWithLongTitle, isVisible = true, onDismiss = {})
    }

    // Verify long title is displayed
    composeTestRule
        .onNodeWithText("Very Long Event Title For Testing Story Viewer Display")
        .assertIsDisplayed()
  }

  @Test
  fun storyViewer_multipleToggle_worksCorrectly() {
    var isVisible = true

    composeTestRule.setContent {
      StoryViewer(event = testEvent, isVisible = isVisible, onDismiss = { isVisible = false })
    }

    // Initially visible
    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()

    // Toggle visibility
    composeTestRule.onNodeWithTag("story_close_button").performClick()
    composeTestRule.waitForIdle()

    // After clicking, it should still be displayed (visibility is controlled by parent)
    // but onDismiss callback should have been triggered
    assertTrue("isVisible should be updated", !isVisible)
  }

  @Test
  fun storyViewer_withDifferentEvents_displaysCorrectContent() {
    val event1 =
        Event.Public(
            uid = "event-1",
            title = "First Event",
            subtitle = "First subtitle",
            description = "First description",
            start = Timestamp.now(),
            end = Timestamp.now(),
            location = Location(latitude = 46.52, longitude = 6.57, name = "Location 1"),
            website = "https://event1.com",
            ownerId = "owner-1",
            isFlash = false,
            tags = listOf("tag1"),
        )

    composeTestRule.setContent { StoryViewer(event = event1, isVisible = true, onDismiss = {}) }

    composeTestRule.onNodeWithText("First Event").assertIsDisplayed()
  }

  @Test
  fun storyViewer_closeIcon_hasCorrectContentDescription() {
    composeTestRule.setContent { StoryViewer(event = testEvent, isVisible = true, onDismiss = {}) }

    // Verify close icon has the correct content description for accessibility
    composeTestRule.onNodeWithContentDescription("Close story").assertIsDisplayed()
  }

  @Test
  fun storyViewer_withFlashEvent_displaysCorrectly() {
    val flashEvent =
        Event.Public(
            uid = "flash-event-1",
            title = "Flash Event",
            subtitle = "Limited time",
            description = "This is a flash event",
            start = Timestamp.now(),
            end = Timestamp.now(),
            location = Location(latitude = 46.52, longitude = 6.57, name = "Flash Location"),
            website = "https://flash.com",
            ownerId = "owner-flash",
            isFlash = true,
            tags = listOf("flash", "urgent"),
        )

    composeTestRule.setContent { StoryViewer(event = flashEvent, isVisible = true, onDismiss = {}) }

    // Verify flash event displays correctly
    composeTestRule.onNodeWithText("Flash Event").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
  }
}
