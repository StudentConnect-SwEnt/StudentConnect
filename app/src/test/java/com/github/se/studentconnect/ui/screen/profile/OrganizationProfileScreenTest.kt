package com.github.se.studentconnect.ui.screen.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel
import com.github.se.studentconnect.ui.profile.OrganizationTab
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OrganizationProfileScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun `screen displays organization profile content`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_screen).assertIsDisplayed()
  }

  @Test
  fun `header displays organization name`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_header).assertIsDisplayed()
    // Evolve appears in both header and title, so check count
    composeTestRule.onAllNodesWithText("Evolve").assertCountEquals(2)
  }

  @Test
  fun `avatar banner is displayed`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_avatar_banner).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.org_profile_avatar).assertIsDisplayed()
  }

  @Test
  fun `organization title is displayed`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_title).assertIsDisplayed()
  }

  @Test
  fun `organization description is displayed`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_description).assertIsDisplayed()
  }

  @Test
  fun `follow button is displayed with correct initial text`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_follow_button).assertIsDisplayed()
    composeTestRule.onNodeWithText("Follow").assertIsDisplayed()
  }

  @Test
  fun `follow button toggles text on click`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_follow_button).performClick()
    composeTestRule.onNodeWithText("Following").assertIsDisplayed()
  }

  @Test
  fun `follow button toggles back on second click`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_follow_button).performClick()
    composeTestRule.onNodeWithTag(C.Tag.org_profile_follow_button).performClick()
    composeTestRule.onNodeWithText("Follow").assertIsDisplayed()
  }

  @Test
  fun `about section with tabs is displayed`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_about_section).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.org_profile_tab_events).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.org_profile_tab_members).assertIsDisplayed()
  }

  @Test
  fun `events tab is selected by default`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.org_profile_events_list).assertExists()
  }

  @Test
  fun `clicking members tab shows members grid`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.org_profile_members_grid).assertExists()
  }

  @Test
  fun `clicking events tab after members shows events list`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.org_profile_tab_events).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.org_profile_events_list).assertExists()
  }

  @Test
  fun `events list displays event rows`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("${C.Tag.org_profile_event_row_prefix}_0").assertExists()
    composeTestRule.onNodeWithTag("${C.Tag.org_profile_event_row_prefix}_1").assertExists()
  }

  @Test
  fun `event cards are displayed`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("${C.Tag.org_profile_event_card_prefix}_0").assertExists()
    composeTestRule.onNodeWithTag("${C.Tag.org_profile_event_card_prefix}_1").assertExists()
  }

  @Test
  fun `members grid displays member cards`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("${C.Tag.org_profile_member_card_prefix}_0").assertExists()
  }

  @Test
  fun `back button callback is invoked`() {
    var backClicked = false

    composeTestRule.setContent {
      AppTheme { OrganizationProfileScreen(onBackClick = { backClicked = true }) }
    }

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assertTrue(backClicked)
  }

  @Test
  fun `screen with custom viewModel uses viewModel state`() {
    val viewModel = OrganizationProfileViewModel("custom_id")
    viewModel.selectTab(OrganizationTab.MEMBERS)

    composeTestRule.setContent { AppTheme { OrganizationProfileScreen(viewModel = viewModel) } }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.org_profile_members_grid).assertExists()
  }

  @Test
  fun `screen displays event title text`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    // Hackathon EPFL appears twice (2 events)
    composeTestRule.onAllNodesWithText("Hackathon EPFL").assertCountEquals(2)
  }

  @Test
  fun `screen displays event subtitle text`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    // Tomorrow appears twice (2 events)
    composeTestRule.onAllNodesWithText("Tomorrow").assertCountEquals(2)
  }

  @Test
  fun `members tab displays member names`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeTestRule.waitForIdle()
    // LazyGrid only renders visible items, at least 4 should be visible
    val nodes = composeTestRule.onAllNodesWithText("Habibi")
    nodes.fetchSemanticsNodes().size.let { count ->
      assertTrue("Expected at least 4 member names, found $count", count >= 4)
    }
  }

  @Test
  fun `members tab displays member roles`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeTestRule.waitForIdle()
    // LazyGrid only renders visible items, at least 4 should be visible
    val nodes = composeTestRule.onAllNodesWithText("Owner")
    nodes.fetchSemanticsNodes().size.let { count ->
      assertTrue("Expected at least 4 member roles, found $count", count >= 4)
    }
  }

  @Test
  fun `tab text displays Events and Members`() {
    composeTestRule.setContent { AppTheme { OrganizationProfileScreen() } }

    composeTestRule.onNodeWithText("Events").assertIsDisplayed()
    composeTestRule.onNodeWithText("Members").assertIsDisplayed()
  }

  private fun androidx.compose.ui.test.SemanticsNodeInteractionsProvider
      .onNodeWithContentDescription(description: String) =
      onNode(androidx.compose.ui.test.hasContentDescription(description))
}
