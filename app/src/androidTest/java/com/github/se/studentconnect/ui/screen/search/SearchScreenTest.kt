package com.github.se.studentconnect.ui.screen.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private suspend fun initUserRepository() {
    UserRepositoryProvider.repository = UserRepositoryLocal()

    for (i in 1..10) {
      val uid = "user$i"
      val email = "user$i@epfl.ch"
      val firstName = "FirstName$i"
      val lastName = "LastName$i"
      val university = "EPFL"
      val createdAt = System.currentTimeMillis() - i * 100000L
      val updatedAt = System.currentTimeMillis() - i * 50000L

      UserRepositoryProvider.repository.saveUser(
          User(
              userId = uid,
              email = email,
              username = firstName.lowercase(),
              firstName = firstName,
              lastName = lastName,
              university = university,
              createdAt = createdAt,
              updatedAt = updatedAt,
          ))
    }
  }

  private suspend fun initEventRepository() {
    EventRepositoryProvider.repository = EventRepositoryLocal()

    for (i in 1..10) {
      val uid = "e$i"
      val own = i % 3
      val ownerId = "user$own"
      val title = "Sample Event $i"
      val description = "Description for event $i"
      val start = Timestamp.now()
      val isFlash = i % 2 == 0
      val subtitle = "Subtitle for event $i"
      EventRepositoryProvider.repository.addEvent(
          Event.Public(
              uid = uid,
              ownerId = ownerId,
              title = title,
              description = description,
              start = start,
              isFlash = isFlash,
              subtitle = subtitle,
          ))
    }
  }

  private suspend fun initOrganizationRepository() {
    OrganizationRepositoryProvider.repository = OrganizationRepositoryLocal()

    for (i in 1..10) {
      val id = "org$i"
      val name = "Organization $i"
      val type = OrganizationType.Association
      val createdBy = "user1"
      val description = "Description for organization $i"

      OrganizationRepositoryProvider.repository.saveOrganization(
          Organization(
              id = id,
              name = name,
              type = type,
              description = description,
              createdBy = createdBy,
          ))
    }
  }

  @Composable
  private fun TestSearchScreen() {
    SearchScreen()
  }

  @Before
  fun setup() = runTest {
    initUserRepository()
    initEventRepository()
    initOrganizationRepository()
    composeTestRule.setContent { AppTheme { TestSearchScreen() } }
    composeTestRule.waitForIdle() // wait for first composition
  }

  @Test
  fun testSearchPageDisplayed() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()

    // Wait until users/events/organisations have been loaded and UI updated
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(C.Tag.user_search_result)
          .fetchSemanticsNodes()
          .isNotEmpty() &&
          composeTestRule
              .onAllNodesWithTag(C.Tag.event_search_result)
              .fetchSemanticsNodes()
              .isNotEmpty() &&
          composeTestRule
              .onAllNodesWithTag(C.Tag.organisation_search_result)
              .fetchSemanticsNodes()
              .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result_title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsDisplayed()
  }

  @Test
  fun testSearchUserDisplayed() {
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("user")
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag(C.Tag.user_search_result).fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsNotDisplayed()
  }

  @Test
  fun testSearchEventDisplayed() {
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("sample")
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(C.Tag.event_search_result)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsDisplayed()
  }

  @Test
  fun testSearchEventDisplayedWhenNotStart() {
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("event")
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(C.Tag.event_search_result)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsDisplayed()
  }

  @Test
  fun testSearchNothingDisplayed() {
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("nothing")
    composeTestRule.waitForIdle()

    // Wait to ensure recomposition finished even if no results
    composeTestRule.waitUntil(timeoutMillis = 5_000) { true }

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsNotDisplayed()
  }

  @Test
  fun testSearchOrganisationDisplayed() {
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("organization")
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(C.Tag.organisation_search_result)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(C.Tag.search_screen).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.back_button).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.user_search_result_title).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.organisation_search_result_title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.event_search_result_title).assertIsNotDisplayed()
  }
}
