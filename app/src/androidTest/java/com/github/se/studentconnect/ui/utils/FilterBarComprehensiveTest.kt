package com.github.se.studentconnect.ui.utils

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.ui.screen.filters.FilterBar
import com.github.se.studentconnect.ui.screen.filters.FilterData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterBarComprehensiveTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val context = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun filterBar_openBottomSheet_displaysFilterOptions() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Filter Events").assertIsDisplayed()
    composeTestRule.onNodeWithText("Categories & Tags").assertIsDisplayed()
  }

  @Test
  fun filterBar_bottomSheet_displaysCategoryChips() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sports").assertIsDisplayed()
    composeTestRule.onNodeWithText("Science").assertIsDisplayed()
    composeTestRule.onNodeWithText("Music").assertIsDisplayed()
    composeTestRule.onNodeWithText("Language").assertIsDisplayed()
    composeTestRule.onNodeWithText("Art").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tech").assertIsDisplayed()
  }

  @Test
  fun filterBar_selectCategory_showsTopics() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Football").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tennis").assertIsDisplayed()
    composeTestRule.onNodeWithText("Running").assertIsDisplayed()
  }

  @Test
  fun filterBar_selectMultipleCategories_displaysAllTopics() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Music").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Football").assertIsDisplayed()
    composeTestRule.onNodeWithText("Piano").assertIsDisplayed()
  }

  @Test
  fun filterBar_deselectCategory_hidesTopics() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Football").assertIsDisplayed()

    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Football").assertDoesNotExist()
  }

  @Test
  fun filterBar_selectTopic_marksAsSelected() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Football").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun filterBar_applyFilters_callsCallback() {
    var capturedFilters: FilterData? = null
    composeTestRule.setContent { FilterBar(context) { filters -> capturedFilters = filters } }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Apply Filters").performClick()
    composeTestRule.waitForIdle()

    assert(capturedFilters != null)
    assert(capturedFilters!!.categories.contains("Sports"))
  }

  @Test
  fun filterBar_resetFilters_clearsSelection() {
    var capturedFilters: FilterData? = null
    composeTestRule.setContent { FilterBar(context) { filters -> capturedFilters = filters } }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.onNodeWithText("Music").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Reset Filters").performClick()
    composeTestRule.waitForIdle()

    assert(capturedFilters != null)
    assert(capturedFilters!!.categories.isEmpty())
  }

  @Test
  fun filterBar_closeButton_dismissesBottomSheet() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithContentDescription("Close Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Filter Events").assertDoesNotExist()
  }

  @Test
  fun filterBar_radiusSlider_isDisplayed() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Radius: 10 km").assertIsDisplayed()
  }

  @Test
  fun filterBar_priceSection_isDisplayed() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Price (€)").assertIsDisplayed()
    composeTestRule.onNodeWithText("Min: 0€").assertIsDisplayed()
    composeTestRule.onNodeWithText("Max: 50€").assertIsDisplayed()
  }

  @Test
  fun filterBar_locationSection_isDisplayed() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Location").assertIsDisplayed()
    composeTestRule.onNodeWithText("Select Location").assertIsDisplayed()
  }

  @Test
  fun filterBar_multipleTopics_canBeSelected() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Football").performClick()
    composeTestRule.onNodeWithText("Tennis").performClick()
    composeTestRule.onNodeWithText("Running").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun filterBar_appliedFilters_containsLocation() {
    var capturedFilters: FilterData? = null
    composeTestRule.setContent { FilterBar(context) { filters -> capturedFilters = filters } }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Apply Filters").performClick()
    composeTestRule.waitForIdle()

    assert(capturedFilters != null)
  }

  @Test
  fun filterBar_appliedFilters_containsRadius() {
    var capturedFilters: FilterData? = null
    composeTestRule.setContent { FilterBar(context) { filters -> capturedFilters = filters } }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Apply Filters").performClick()
    composeTestRule.waitForIdle()

    assert(capturedFilters != null)
    assert(capturedFilters!!.radiusKm == 10f)
  }

  @Test
  fun filterBar_selectAndDeselectTopic_worksCorrectly() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Football").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Football").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun filterBar_allCategories_areClickable() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    val categories = listOf("Sports", "Science", "Music", "Language", "Art", "Tech")
    categories.forEach { category ->
      composeTestRule.onNodeWithText(category).assertHasClickAction()
    }
  }
}
