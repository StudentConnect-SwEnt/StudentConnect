package com.github.se.studentconnect.ui.screen.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.repository.UserRepositoryProvider
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
      val start = Timestamp(Timestamp.now().seconds - 2 + i, Timestamp.now().nanoseconds)
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

  @Composable
  private fun TestSearchScreen() {
    SearchScreen()
  }

  @Before
  fun setup() = runTest {
    initUserRepository()
    initEventRepository()
    composeTestRule.setContent { AppTheme { TestSearchScreen() } }
    composeTestRule.waitForIdle() // wait for first composition
  }

  @Test
  fun testSearchPageDisplayed() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.TOP_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).assertIsDisplayed()

    // Wait until users/events/organizations have been loaded and UI updated
    composeTestRule.waitUntil(timeoutMillis = 50_000) {
      composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_RESULTS).isDisplayed() &&
          composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_RESULTS).isDisplayed() &&
          composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_RESULTS).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_RESULTS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_ROW).assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(SearchScreenTestTags.USER_ROW_CARD)
        .assertAll(hasClickAction())
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_COLUMN).assertIsNotDisplayed()
    composeTestRule.onAllNodesWithTag(SearchScreenTestTags.USER_COLUMN_CARD).assertCountEquals(0)

    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_RESULTS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENT_ROW).assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(SearchScreenTestTags.EVENT_ROW_CARD)
        .assertAll(hasClickAction())
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENT_COLUMN).assertIsNotDisplayed()
    composeTestRule.onAllNodesWithTag(SearchScreenTestTags.EVENT_COLUMN_CARD).assertCountEquals(0)

    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_RESULTS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_ROW).assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(SearchScreenTestTags.ORGANIZATION_ROW_CARD)
        .assertAll(hasClickAction())
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_COLUMN).assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag(SearchScreenTestTags.ORGANIZATION_COLUMN_CARD)
        .assertCountEquals(0)
  }

  @Test
  fun testSearchUserDisplayed() {
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("first")
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 50_000) {
      composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_RESULTS).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_RESULTS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_RESULTS).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_RESULTS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_ROW).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_COLUMN).assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(SearchScreenTestTags.USER_COLUMN_CARD)
        .assertAll(hasClickAction())
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun testSearchEventDisplayed() {
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("sample")
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 50_000) {
      composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_RESULTS).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_RESULTS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_RESULTS).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_RESULTS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENT_ROW).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENT_COLUMN).assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(SearchScreenTestTags.EVENT_COLUMN_CARD)
        .assertAll(!hasText("Sample Event 0"))
        .assertAll(hasClickAction())
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun testSearchEventDisplayedWhenNotStart() {
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("event")
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 50_000) {
      composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_RESULTS).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_RESULTS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_RESULTS).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_RESULTS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENT_ROW).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENT_COLUMN).assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(SearchScreenTestTags.EVENT_COLUMN_CARD)
        .assertAll(!hasText("Sample Event 0"))
        .assertAll(hasClickAction())
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun testSearchOrganizationDisplayed() {
    composeTestRule.onNodeWithTag(C.Tag.search_input_field).performTextInput("organization")
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 50_000) {
      composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_RESULTS).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_RESULTS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_RESULTS).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_RESULTS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_ROW).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.ORGANIZATIONS_COLUMN).assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(SearchScreenTestTags.ORGANIZATION_COLUMN_CARD)
        .assertAll(hasClickAction())
        .onFirst()
        .assertIsDisplayed()
  }
  // Test to be added when organization logic is finished
  /*@Test
  fun testSearchNothingDisplayed() {
    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_FIELD).performTextInput("nothing")
    composeTestRule.waitForIdle()

    // Wait to ensure recomposition finished even if no results
    composeTestRule.waitUntil(timeoutMillis = 5_000) { true }

    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.SEARCH_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_RESULTS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.USERS_TITLE).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_RESULTS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SearchScreenTestTags.EVENTS_TITLE).assertIsNotDisplayed()
  }*/
}
