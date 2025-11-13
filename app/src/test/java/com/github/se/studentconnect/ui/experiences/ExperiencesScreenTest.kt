package com.github.se.studentconnect.ui.experiences

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.github.se.studentconnect.model.Activities
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.signup.ExperienceFilterChip
import com.github.se.studentconnect.ui.screen.signup.ExperiencesContent
import com.github.se.studentconnect.ui.screen.signup.ExperiencesScreen
import com.github.se.studentconnect.ui.screen.signup.PrimaryCtaButton
import com.github.se.studentconnect.ui.screen.signup.TopicChip
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ExperiencesScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun experiencesScreenRendersDefaultStateAndTriggersCallbacks() {
    var backClicks = 0
    var startClicks = 0

    composeRule.setContent {
      AppTheme {
        var selectedFilter by remember { mutableStateOf(Activities.filterOptions.first()) }
        var selectedTopics by remember { mutableStateOf(setOf<String>()) }
        ExperiencesScreen(
            selectedFilter = selectedFilter,
            selectedTopics = selectedTopics,
            onFilterSelected = { selectedFilter = it },
            onTopicToggle = { topic ->
              selectedTopics =
                  selectedTopics
                      .toMutableSet()
                      .also { topics ->
                        if (!topics.add(topic)) {
                          topics.remove(topic)
                        }
                      }
                      .toSet()
            },
            onBackClick = { backClicks++ },
            onStartClick = { startClicks++ })
      }
    }

    composeRule.onNodeWithTag(C.Tag.experiences_screen_container).assertIsDisplayed()
    composeRule
        .onNodeWithTag(C.Tag.experiences_title)
        .assertIsDisplayed()
        .assertTextEquals("For an experience beyond Expectations")
    composeRule
        .onNodeWithTag("${C.Tag.experiences_filter_chip_prefix}_Sports", useUnmergedTree = true)
        .assertExists()
        .assertHasClickAction()

    // Sports topics should be visible by default
    composeRule.onNodeWithText("Bowling").assertExists()

    composeRule.onNodeWithContentDescription("Back").performClick()
    composeRule.runOnIdle { Assert.assertEquals(1, backClicks) }

    composeRule.onNodeWithTag(C.Tag.experiences_cta).performClick()
    composeRule.runOnIdle { Assert.assertEquals(1, startClicks) }

    // CTA text verification
    composeRule.onNodeWithText("Start Now").assertExists()
  }

  @Test
  fun topBarDisplaysSubtitleAndFilterList() {
    composeRule.setContent {
      AppTheme {
        var selectedFilter by remember { mutableStateOf(Activities.filterOptions.first()) }
        ExperiencesScreen(
            selectedFilter = selectedFilter,
            selectedTopics = emptySet(),
            onFilterSelected = { selectedFilter = it },
            onTopicToggle = {},
            onBackClick = {},
            onStartClick = {})
      }
    }

    composeRule.onNodeWithTag(C.Tag.experiences_top_bar).assertIsDisplayed()
    composeRule
        .onNodeWithTag(C.Tag.experiences_subtitle)
        .assertIsDisplayed()
        .assertTextEquals("Discover what excites you")

    // Ensure filter list contains the first few chips after scrolling
    val filters = listOf("Sports", "Science", "Music", "Language", "Art", "Tech")
    filters.forEach { filter ->
      composeRule
          .onNodeWithTag(C.Tag.experiences_filter_list)
          .performScrollToNode(hasTestTag("${C.Tag.experiences_filter_chip_prefix}_$filter"))
      composeRule
          .onNodeWithTag("${C.Tag.experiences_filter_chip_prefix}_$filter", useUnmergedTree = true)
          .assertExists()
    }
  }

  @Test
  fun selectingDifferentFilterUpdatesTopics() {
    composeRule.setContent {
      AppTheme {
        var selectedFilter by remember { mutableStateOf(Activities.filterOptions.first()) }
        ExperiencesScreen(
            selectedFilter = selectedFilter,
            selectedTopics = emptySet(),
            onFilterSelected = { selectedFilter = it },
            onTopicToggle = {},
            onBackClick = {},
            onStartClick = {})
      }
    }

    composeRule
        .onNodeWithTag(C.Tag.experiences_filter_list)
        .performScrollToNode(hasTestTag("${C.Tag.experiences_filter_chip_prefix}_Science"))
    composeRule
        .onNodeWithTag("${C.Tag.experiences_filter_chip_prefix}_Science", useUnmergedTree = true)
        .performClick()

    composeRule.onNodeWithText("Astronomy").assertExists()
    composeRule.onNodeWithText("Basketball").assertDoesNotExist()

    composeRule
        .onNodeWithTag(C.Tag.experiences_filter_list)
        .performScrollToNode(hasTestTag("${C.Tag.experiences_filter_chip_prefix}_Tech"))
    composeRule
        .onNodeWithTag("${C.Tag.experiences_filter_chip_prefix}_Tech", useUnmergedTree = true)
        .performClick()

    composeRule.onNodeWithText("AI").assertExists()
    composeRule.onNodeWithText("ML").assertExists()
  }

  @Test
  fun experiencesContentShowsPlaceholderWhenNoTopics() {
    composeRule.setContent {
      AppTheme {
        ExperiencesContent(
            selectedFilter = "Unknown",
            selectedTopics = emptySet(),
            onFilterSelected = {},
            onTopicToggle = {},
            onBackClick = {},
            onStartClick = {},
            isSaving = false,
            errorMessage = null,
        )
      }
    }

    composeRule.onNodeWithTag(C.Tag.experiences_topic_grid).assertIsDisplayed()
    composeRule.onNodeWithText("No topics yet").assertExists()
  }

  @Test
  fun filterChipClickPropagatesSelection() {
    var latestFilter = ""

    composeRule.setContent {
      AppTheme {
        val selected = remember { mutableStateOf("Science") }
        ExperiencesContent(
            selectedFilter = selected.value,
            selectedTopics = emptySet(),
            onFilterSelected = {
              selected.value = it
              latestFilter = it
            },
            onTopicToggle = {},
            onBackClick = {},
            onStartClick = {},
            isSaving = false,
            errorMessage = null,
        )
      }
    }

    composeRule
        .onNodeWithTag(C.Tag.experiences_filter_list)
        .performScrollToNode(hasTestTag("${C.Tag.experiences_filter_chip_prefix}_Music"))
    composeRule
        .onNodeWithTag("${C.Tag.experiences_filter_chip_prefix}_Music", useUnmergedTree = true)
        .performClick()
    composeRule.runOnIdle { Assert.assertEquals("Music", latestFilter) }

    // Ensure topic chips for the newly selected filter are rendered
    composeRule.onNodeWithText("Choir").assertExists()

    // Verify a topic chip test tag exists for the rendered topic
    composeRule
        .onNodeWithTag("${C.Tag.experiences_topic_chip_prefix}_Choir", useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun experienceFilterChipClickInvokesCallback() {
    var clicks = 0
    composeRule.setContent {
      AppTheme {
        ExperienceFilterChip(label = "ChipTest", selected = false, onClick = { clicks++ })
      }
    }

    composeRule
        .onNodeWithTag("${C.Tag.experiences_filter_chip_prefix}_ChipTest", useUnmergedTree = true)
        .performClick()

    composeRule.runOnIdle { Assert.assertEquals(1, clicks) }
  }

  @Test
  fun topicChipExposesSemanticsTag() {
    composeRule.setContent {
      AppTheme { TopicChip(label = "Sample", selected = false, onClick = {}) }
    }

    composeRule
        .onNodeWithTag("${C.Tag.experiences_topic_chip_prefix}_Sample", useUnmergedTree = true)
        .assertExists()
    composeRule.onNodeWithText("Sample").assertIsDisplayed()
  }

  @Test
  fun primaryCtaButtonInvokesAction() {
    var invoked = false
    composeRule.setContent {
      AppTheme { PrimaryCtaButton(text = "Call To Action", onClick = { invoked = true }) }
    }

    composeRule.onNodeWithText("Call To Action").performClick()
    composeRule.runOnIdle { Assert.assertTrue(invoked) }
  }

  @Test
  fun topicChipSelectionUpdatesState() {
    val selectedTopics = mutableStateOf(setOf<String>())

    composeRule.setContent {
      AppTheme {
        ExperiencesScreen(
            selectedFilter = Activities.filterOptions.first(),
            selectedTopics = selectedTopics.value,
            onFilterSelected = {},
            onTopicToggle = { topic ->
              selectedTopics.value =
                  selectedTopics.value
                      .toMutableSet()
                      .also { topics ->
                        if (!topics.add(topic)) {
                          topics.remove(topic)
                        }
                      }
                      .toSet()
            },
            onBackClick = {},
            onStartClick = {})
      }
    }

    val chipTag = "${C.Tag.experiences_topic_chip_prefix}_Bowling"
    composeRule.onNodeWithTag(chipTag, useUnmergedTree = true).performClick()

    composeRule.runOnIdle { Assert.assertTrue(selectedTopics.value.contains("Bowling")) }
  }

  @Test
  fun topicChipSelectionTogglesOffWhenClickedAgain() {
    val selectedTopics = mutableStateOf(setOf<String>())

    composeRule.setContent {
      AppTheme {
        ExperiencesScreen(
            selectedFilter = Activities.filterOptions.first(),
            selectedTopics = selectedTopics.value,
            onFilterSelected = {},
            onTopicToggle = { topic ->
              selectedTopics.value =
                  selectedTopics.value
                      .toMutableSet()
                      .also { topics ->
                        if (!topics.add(topic)) {
                          topics.remove(topic)
                        }
                      }
                      .toSet()
            },
            onBackClick = {},
            onStartClick = {})
      }
    }

    val chipTag = "${C.Tag.experiences_topic_chip_prefix}_Bowling"
    composeRule.onNodeWithTag(chipTag, useUnmergedTree = true).performClick()
    composeRule.onNodeWithTag(chipTag, useUnmergedTree = true).performClick()

    composeRule.runOnIdle { Assert.assertFalse(selectedTopics.value.contains("Bowling")) }
  }

  @Test
  fun primaryCtaDisabledWhileSaving() {
    composeRule.setContent {
      AppTheme {
        ExperiencesScreen(
            selectedFilter = Activities.filterOptions.first(),
            selectedTopics = emptySet(),
            onFilterSelected = {},
            onTopicToggle = {},
            onBackClick = {},
            onStartClick = {},
            isSaving = true)
      }
    }

    composeRule.onNodeWithTag(C.Tag.experiences_cta).assertIsNotEnabled()
  }

  @Test
  fun primaryCtaEnabledWhenNotSaving() {
    composeRule.setContent {
      AppTheme {
        ExperiencesScreen(
            selectedFilter = Activities.filterOptions.first(),
            selectedTopics = emptySet(),
            onFilterSelected = {},
            onTopicToggle = {},
            onBackClick = {},
            onStartClick = {},
            isSaving = false)
      }
    }

    composeRule.onNodeWithTag(C.Tag.experiences_cta).assertIsEnabled()
  }

  @Test
  fun experiencesScreenHonorsModifier() {
    composeRule.setContent {
      AppTheme {
        ExperiencesScreen(
            selectedFilter = Activities.filterOptions.first(),
            selectedTopics = emptySet(),
            onFilterSelected = {},
            onTopicToggle = {},
            onBackClick = {},
            onStartClick = {},
            modifier = Modifier.testTag("experiences_root"))
      }
    }

    composeRule.onNodeWithTag("experiences_root", useUnmergedTree = true).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.experiences_screen_container).assertIsDisplayed()
  }

  @Test
  fun primaryCtaButtonTogglesEnabledState() {
    var clicks = 0
    lateinit var enableCta: () -> Unit

    composeRule.setContent {
      AppTheme {
        var enabled by remember { mutableStateOf(false) }
        enableCta = { enabled = true }
        PrimaryCtaButton(
            text = "Toggle CTA", onClick = { clicks++ }, enabled = enabled, modifier = Modifier)
      }
    }

    val node = composeRule.onNodeWithTag(C.Tag.experiences_cta)
    node.assertIsNotEnabled()

    composeRule.runOnIdle { enableCta() }

    node.assertIsEnabled()
    node.performClick()

    composeRule.runOnIdle { Assert.assertEquals(1, clicks) }
  }
}
