package com.github.se.studentconnect.ui.utils

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterBarComprehensiveTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

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

    composeTestRule.onNodeWithText("Piano").performScrollTo()
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

  @Test
  fun filterBar_favoritesButton_togglesState() {
    var capturedFilters: FilterData? = null
    composeTestRule.setContent { FilterBar(context) { filters -> capturedFilters = filters } }

    // Click Favorites button
    composeTestRule.onNodeWithText("Favorites").performClick()
    composeTestRule.waitForIdle()

    // Verify callback was called with showOnlyFavorites = true
    assert(capturedFilters != null)
    assert(capturedFilters!!.showOnlyFavorites)

    // Click again to toggle off
    composeTestRule.onNodeWithText("Favorites").performClick()
    composeTestRule.waitForIdle()

    // Verify callback was called with showOnlyFavorites = false
    assert(capturedFilters!!.showOnlyFavorites == false)
  }

  @Test
  fun filterBar_favoritesWithOtherFilters_appliesBoth() {
    var capturedFilters: FilterData? = null
    composeTestRule.setContent { FilterBar(context) { filters -> capturedFilters = filters } }

    // Open filters and select category
    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Apply Filters").performClick()
    composeTestRule.waitForIdle()

    // Now enable favorites
    composeTestRule.onNodeWithText("Favorites").performClick()
    composeTestRule.waitForIdle()

    // Verify both filters are applied
    assert(capturedFilters != null)
    assert(capturedFilters!!.showOnlyFavorites)
    assert(capturedFilters!!.categories.contains("Sports"))
  }

  @Test
  fun filterBar_priceRange_isDisplayed() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    // Scroll to the price section and verify it exists
    composeTestRule.onNodeWithText("Price (€)").performScrollTo()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Price (€)").assertIsDisplayed()

    // Verify min and max labels exist (they may not be fully visible)
    composeTestRule.onNodeWithText("Min: 0€", substring = true).assertExists()
    composeTestRule.onNodeWithText("Max: 50€", substring = true).assertExists()
  }

  @Test
  fun filterBar_applyFilters_includesPriceRange() {
    var capturedFilters: FilterData? = null
    composeTestRule.setContent { FilterBar(context) { filters -> capturedFilters = filters } }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Apply Filters").performClick()
    composeTestRule.waitForIdle()

    assert(capturedFilters != null)
    assert(capturedFilters!!.priceRange.start == 0f)
    assert(capturedFilters!!.priceRange.endInclusive == 50f)
  }

  @Test
  fun filterBar_resetFilters_resetsAllFilters() {
    var capturedFilters: FilterData? = null
    composeTestRule.setContent { FilterBar(context) { filters -> capturedFilters = filters } }

    // Open filters and select category
    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()

    // Reset filters
    composeTestRule.onNodeWithText("Reset Filters").performClick()
    composeTestRule.waitForIdle()

    // Verify all filters are reset
    assert(capturedFilters != null)
    assert(capturedFilters!!.categories.isEmpty())
    assert(capturedFilters!!.location == null)
    assert(capturedFilters!!.radiusKm == 10f)
    assert(capturedFilters!!.priceRange.start == 0f)
    assert(capturedFilters!!.priceRange.endInclusive == 50f)
  }

  @Test
  fun filterBar_deselectCategory_removesAllTopics() {
    var capturedFilters: FilterData? = null
    composeTestRule.setContent { FilterBar(context) { filters -> capturedFilters = filters } }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()

    // Select category and a topic
    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Football").performClick()
    composeTestRule.waitForIdle()

    // Deselect the category
    composeTestRule.onNodeWithText("Sports").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Apply Filters").performClick()
    composeTestRule.waitForIdle()

    // Verify neither category nor topic are in filters
    assert(capturedFilters != null)
    assert(!capturedFilters!!.categories.contains("Sports"))
    assert(!capturedFilters!!.categories.contains("Football"))
  }

  @Test
  fun filterBar_calendarButton_isDisplayed() {
    composeTestRule.setContent { FilterBar(context) {} }

    composeTestRule.onNode(hasTestTag("calendar_button")).assertExists()
  }

  @Test
  fun filterBar_calendarButton_isClickable() {
    var calendarClicked = false
    composeTestRule.setContent { FilterBar(context, onCalendarClick = { calendarClicked = true }) }

    composeTestRule.onNode(hasTestTag("calendar_button")).performClick()
    composeTestRule.waitForIdle()

    assert(calendarClicked)
  }
}
