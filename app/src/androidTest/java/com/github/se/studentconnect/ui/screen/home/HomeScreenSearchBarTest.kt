package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.screens.HomeScreen
import com.github.se.studentconnect.viewmodel.HomePageViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenSearchBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var viewModel: HomePageViewModel

  @Before
  fun setup() {
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    viewModel = HomePageViewModel(eventRepository, userRepository)
  }

  @Test
  fun homeScreen_searchBar_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Search for events...").assertIsDisplayed()
  }

  @Test
  fun homeScreen_searchIcon_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNodeWithContentDescription("Search Icon").assertIsDisplayed()
  }

  @Test
  fun homeScreen_searchBar_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Search for events...").assertHasClickAction()
  }

  /*
    @Test
    fun homeScreen_clickSearchBar_doesNotCrash() {
      composeTestRule.setContent {
        val navController = rememberNavController()
        Scaffold { paddingValues ->
          NavHost(
              navController = navController,
              startDestination = Route.HOME,
              modifier = Modifier.padding(paddingValues),
              enterTransition = { EnterTransition.None },
              exitTransition = { ExitTransition.None }) {
                composable(Route.HOME) {
                  HomeScreen(navController = navController, viewModel = viewModel)
                }

                composable(Route.SEARCH) { SearchScreen() }
              }
        }
      }

      // Wait for screen to load
      composeTestRule.waitUntil(timeoutMillis = 3000) {
        composeTestRule
            .onAllNodes(androidx.compose.ui.test.hasText("Search for events..."))
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Click search bar - should not crash
      composeTestRule.onNodeWithText("Search for events...").performClick()
      composeTestRule.waitForIdle()
    }
  */
}
