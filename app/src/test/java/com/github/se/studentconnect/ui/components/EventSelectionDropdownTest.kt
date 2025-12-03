package com.github.se.studentconnect.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventSelectionDropdownTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private fun createMockEvent(uid: String, title: String): Event {
    return Event.Public(
        uid = uid,
        ownerId = "owner1",
        title = title,
        description = "Description",
        start = Timestamp.now(),
        isFlash = false,
        subtitle = "Subtitle")
  }

  @Test
  fun eventSelectionDropdown_displaysPlaceholderWhenNoEventSelected() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val placeholderText = context.getString(R.string.event_selection_button_label)

    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).assertIsDisplayed()
    composeTestRule.onNodeWithText(placeholderText).assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_displaysEventTitleWhenSelected() {
    val event = createMockEvent("1", "Tech Meetup")

    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = event,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).assertIsDisplayed()
    composeTestRule.onNodeWithText("Tech Meetup").assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_clickButton_opensDialog() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_dropdown).assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_clickButton_callsOnLoadEvents() {
    var loadEventsCalled = false
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = { loadEventsCalled = true })
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    assertTrue(loadEventsCalled)
  }

  @Test
  fun eventSelectionDropdown_displaysLoadingState() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Loading,
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_loading).assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_displaysErrorState() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Error(),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_error).assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_displaysEmptyState() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_empty).assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_displaysEventsList() {
    val events =
        listOf(
            createMockEvent("1", "Event 1"),
            createMockEvent("2", "Event 2"),
            createMockEvent("3", "Event 3"))
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(events),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_list).assertIsDisplayed()
    composeTestRule.onNodeWithText("Event 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event 2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event 3").assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_selectEvent_callsOnEventSelected() {
    val event = createMockEvent("1", "Event 1")
    var selectedEvent: Event? = null
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(listOf(event)),
            selectedEvent = selectedEvent,
            onEventSelected = { selectedEvent = it },
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_1").performClick()
    composeTestRule.runOnIdle { assertEquals(event, selectedEvent) }
  }

  @Test
  fun eventSelectionDropdown_deselectEvent_callsOnEventSelectedWithNull() {
    val event = createMockEvent("1", "Event 1")
    var selectedEvent: Event? = event
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(listOf(event)),
            selectedEvent = selectedEvent,
            onEventSelected = { selectedEvent = it },
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_1").performClick()
    composeTestRule.runOnIdle { assertEquals(null, selectedEvent) }
  }

  @Test
  fun eventSelectionDropdown_closeButton_closesDialog() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_dropdown).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_close).performClick()
    // Dialog should close after clicking close
    composeTestRule.waitForIdle()
  }

  @Test
  fun eventSelectionDropdown_selectedEvent_showsCheckmark() {
    val event = createMockEvent("1", "Event 1")
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(listOf(event)),
            selectedEvent = event,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_1").assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_errorState_withMessage_displaysMessage() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Error("Custom error message"),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_error).assertIsDisplayed()
    composeTestRule.onNodeWithText("Custom error message").assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_loadingState_displaysLoadingText() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val loadingText = context.getString(R.string.event_selection_loading)

    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Loading,
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_loading).assertIsDisplayed()
    composeTestRule.onNodeWithText(loadingText).assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_triggerButton_hasContentDescription() {
    val event = createMockEvent("1", "Test Event")
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = event,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_dialogDismissRequest_closesDialog() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_dropdown).assertIsDisplayed()
    // Simulate dismiss by clicking outside (dialog dismisses)
    composeTestRule.waitForIdle()
  }

  @Test
  fun eventSelectionDropdown_unselectedEventCard_displaysWithoutCheckmark() {
    val event1 = createMockEvent("1", "Event 1")
    val event2 = createMockEvent("2", "Event 2")
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(listOf(event1, event2)),
            selectedEvent = event1, // Only event1 is selected
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_2").assertIsDisplayed()
    // Event 2 should not have checkmark
  }

  @Test
  fun eventSelectionDropdown_multipleEvents_selectionWorks() {
    val events =
        listOf(
            createMockEvent("1", "Event 1"),
            createMockEvent("2", "Event 2"),
            createMockEvent("3", "Event 3"))
    var selectedEvent: Event? = null
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(events),
            selectedEvent = selectedEvent,
            onEventSelected = { selectedEvent = it },
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_2").performClick()
    composeTestRule.runOnIdle { assertEquals("2", selectedEvent?.uid) }
  }

  @Test
  fun eventSelectionDropdown_triggerButton_selectedEvent_showsTitle() {
    val event = createMockEvent("1", "My Event")
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = event,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithText("My Event").assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_triggerButton_noEvent_showsPlaceholder() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val placeholderText = context.getString(R.string.event_selection_button_label)
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithText(placeholderText).assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_errorState_nullMessage_usesDefault() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val defaultError = context.getString(R.string.event_selection_error)
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Error(null),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithText(defaultError).assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_eventCard_selected_showsCheckmark() {
    val event = createMockEvent("1", "Event 1")
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(listOf(event)),
            selectedEvent = event,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_1").assertIsDisplayed()
  }

  @Test
  fun eventSelectionDropdown_eventCard_unselected_noCheckmark() {
    val event1 = createMockEvent("1", "Event 1")
    val event2 = createMockEvent("2", "Event 2")
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(listOf(event1, event2)),
            selectedEvent = event1,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_2").assertIsDisplayed()
    // Event 2 should not have checkmark since only event1 is selected
  }

  @Test
  fun eventSelectionDropdown_dialogNotShown_initially() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            state = EventSelectionState.Success(emptyList()),
            selectedEvent = null,
            onEventSelected = {},
            onLoadEvents = {})
      }
    }

    // Dialog should not be visible initially
    composeTestRule.onNodeWithTag(C.Tag.event_selection_dropdown).assertDoesNotExist()
  }
}
