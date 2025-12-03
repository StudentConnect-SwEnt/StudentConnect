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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventSelectionDropdownTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private fun mockEvent(uid: String, title: String) =
      Event.Public(
          uid = uid,
          ownerId = "owner",
          title = title,
          description = "desc",
          start = Timestamp.now(),
          isFlash = false,
          subtitle = "sub")

  // Covers: selectedEvent=null (lines 122, 144, 147), Success empty (line 253)
  @Test
  fun noEventSelected_showsPlaceholderAndEmptyState() {
    val placeholder =
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .getString(R.string.event_selection_button_label)
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            EventSelectionState.Success(emptyList()), null, {}, {})
      }
    }
    composeTestRule.onNodeWithText(placeholder).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_empty).assertIsDisplayed()
  }

  // Covers: selectedEvent!=null (lines 122, 144, 147)
  @Test
  fun eventSelected_showsEventTitle() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            EventSelectionState.Success(emptyList()), mockEvent("1", "My Event"), {}, {})
      }
    }
    composeTestRule.onNodeWithText("My Event").assertIsDisplayed()
  }

  // Covers: when(state) Loading (line 208)
  @Test
  fun loadingState_showsLoadingIndicator() {
    composeTestRule.setContent {
      AppTheme { EventSelectionDropdown(EventSelectionState.Loading(), null, {}, {}) }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_loading).assertIsDisplayed()
  }

  // Covers: when(state) Error (line 209), message!=null (line 241)
  @Test
  fun errorState_withMessage_showsCustomMessage() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(EventSelectionState.Error(error = "Network error"), null, {}, {})
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
  }

  // Covers: message==null (line 241)
  @Test
  fun errorState_nullMessage_showsDefaultMessage() {
    val defaultError =
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .getString(R.string.event_selection_error)
    composeTestRule.setContent {
      AppTheme { EventSelectionDropdown(EventSelectionState.Error(error = null), null, {}, {}) }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithText(defaultError).assertIsDisplayed()
  }

  // Covers: isSelected=false (lines 267, 272, 289, 305, 309, 328), events.isEmpty()=false (line 253)
  @Test
  fun selectEvent_callsOnEventSelectedWithEvent() {
    val event = mockEvent("1", "Event 1")
    var selected: Event? = null
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(EventSelectionState.Success(listOf(event)), null, { selected = it }, {})
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_list).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_0").performClick()
    composeTestRule.runOnIdle { assertEquals(event, selected) }
  }

  // Covers: isSelected=true (lines 267, 272, 289, 309, 328)
  @Test
  fun deselectEvent_callsOnEventSelectedWithNull() {
    val event = mockEvent("1", "Event 1")
    var selected: Event? = event
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(EventSelectionState.Success(listOf(event)), event, { selected = it }, {})
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_0").performClick()
    composeTestRule.runOnIdle { assertEquals(null, selected) }
  }
}

