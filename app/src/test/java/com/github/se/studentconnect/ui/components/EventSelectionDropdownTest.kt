package com.github.se.studentconnect.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
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

/** Tests for [EventSelectionDropdown] composable. */
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

  @Test
  fun noEventSelected_showsPlaceholderAndEmptyState() {
    val placeholder =
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .getString(R.string.event_selection_button_label)
    composeTestRule.setContent {
      AppTheme { EventSelectionDropdown(EventSelectionState.Success(emptyList()), null, {}, {}) }
    }
    composeTestRule.onNodeWithText(placeholder).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_empty).assertIsDisplayed()
  }

  @Test
  fun eventSelected_showsEventTitle_dialogNotShownInitially() {
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            EventSelectionState.Success(emptyList()), mockEvent("1", "My Event"), {}, {})
      }
    }
    composeTestRule.onNodeWithText("My Event").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag(C.Tag.event_selection_dropdown).assertCountEquals(0)
  }

  @Test
  fun loadingState_showsLoadingIndicator() {
    composeTestRule.setContent {
      AppTheme { EventSelectionDropdown(EventSelectionState.Loading(), null, {}, {}) }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_loading).assertIsDisplayed()
  }

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

  @Test
  fun selectEvent_callsOnEventSelectedWithEvent() {
    val event = mockEvent("1", "Event 1")
    var selected: Event? = null
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            EventSelectionState.Success(listOf(event)), null, { selected = it }, {})
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_list).assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_0").performClick()
    composeTestRule.runOnIdle { assertEquals(event, selected) }
  }

  @Test
  fun deselectEvent_callsOnEventSelectedWithNull() {
    val event = mockEvent("1", "Event 1")
    var selected: Event? = event
    composeTestRule.setContent {
      AppTheme {
        EventSelectionDropdown(
            EventSelectionState.Success(listOf(event)), event, { selected = it }, {})
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_0").performClick()
    composeTestRule.runOnIdle { assertEquals(null, selected) }
  }

  @Test
  fun dialogHeader_closeButton_closesDialog() {
    composeTestRule.setContent {
      AppTheme { EventSelectionDropdown(EventSelectionState.Success(emptyList()), null, {}, {}) }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_dropdown).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_selection_close).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onAllNodesWithTag(C.Tag.event_selection_dropdown).assertCountEquals(0)
  }

  @Test
  fun multipleEvents_displaysAllCards() {
    val events = listOf(mockEvent("1", "Event 1"), mockEvent("2", "Event 2"))
    composeTestRule.setContent {
      AppTheme { EventSelectionDropdown(EventSelectionState.Success(events), null, {}, {}) }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_1").assertIsDisplayed()
  }

  @Test
  fun selectedEvent_cardShowsSelectedState() {
    val events = listOf(mockEvent("1", "Event 1"), mockEvent("2", "Event 2"))
    composeTestRule.setContent {
      AppTheme { EventSelectionDropdown(EventSelectionState.Success(events), events[0], {}, {}) }
    }
    composeTestRule.onNodeWithTag(C.Tag.event_selection_button).performClick()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("${C.Tag.event_selection_card_prefix}_1").assertIsDisplayed()
  }
}
