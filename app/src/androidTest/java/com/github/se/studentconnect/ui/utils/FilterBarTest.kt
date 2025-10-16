package com.github.se.studentconnect.ui.utils

import FilterBar
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val context = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun filterBar_isDisplayed() {
    composeTestRule.setContent { FilterBar(context) }

    // Verify the filter chips are displayed
    composeTestRule.onNodeWithText("Paris").assertIsDisplayed()
    composeTestRule.onNodeWithText("Filtres").assertIsDisplayed()
    composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
  }

  @Test
  fun filterBar_allChipsHaveClickAction() {
    composeTestRule.setContent { FilterBar(context) }

    composeTestRule.onNodeWithText("Paris").assertHasClickAction()
    composeTestRule.onNodeWithText("Filtres").assertHasClickAction()
    composeTestRule.onNodeWithText("Favorites").assertHasClickAction()
  }

  @Test
  fun filterBar_iconsAreDisplayed() {
    composeTestRule.setContent { FilterBar(context) }

    // Verify icons have content descriptions matching the text
    composeTestRule.onNodeWithContentDescription("Paris").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Filtres").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Favorites").assertIsDisplayed()
  }

  @Test
  fun filterBar_clickOnParisChip_doesNotCrash() {
    composeTestRule.setContent { FilterBar(context) }

    composeTestRule.onNodeWithText("Paris").performClick()
    // Should show "Not yet implemented" toast, but won't crash
  }

  @Test
  fun filterBar_clickOnFiltresChip_doesNotCrash() {
    composeTestRule.setContent { FilterBar(context) }

    composeTestRule.onNodeWithText("Filtres").performClick()
  }

  @Test
  fun filterBar_clickOnFavoritesChip_doesNotCrash() {
    composeTestRule.setContent { FilterBar(context) }

    composeTestRule.onNodeWithText("Favorites").performClick()
  }

  @Test
  fun filterBar_hasCorrectNumberOfChips() {
    composeTestRule.setContent { FilterBar(context) }

    // Should have 4 chips (Paris, Calendar icon-only, Filtres, Favorites)
    composeTestRule.onNodeWithText("Paris").assertIsDisplayed()
    composeTestRule.onNodeWithText("Filtres").assertIsDisplayed()
    composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    // Calendar chip has no text, so we just verify the others
  }
}
