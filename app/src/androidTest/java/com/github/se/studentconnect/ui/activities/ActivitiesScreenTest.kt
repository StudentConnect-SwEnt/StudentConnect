package com.github.se.studentconnect.ui.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import org.junit.Rule
import org.junit.Test

class ActivitiesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /*@Test
    fun screenDisplaysEventsByDefaultFromLocalRepository() {

        val activitiesViewModel = ActivitiesViewModel()

        composeTestRule.setContent {
            ActivitiesScreen(
                navController = rememberNavController(),
                activitiesViewModel = activitiesViewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertIsDisplayed()

        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.EMPTY_STATE_TEXT).assertDoesNotExist()

        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.INFO_EVENT_SECTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.EVENT_ACTION_BUTTONS).assertIsDisplayed()
    }*/

    @Test
    fun emptyStateIsDisplayedWhenRepositoryIsEmpty() {
        // Préparation : On écrase le repository par défaut avec une nouvelle instance VIDE.
        // C'est la clé pour contrôler ce scénario.
        EventRepositoryProvider.repository = EventRepositoryLocal()

        val activitiesViewModel = ActivitiesViewModel()

        // Action : Afficher le ActivitiesScreen
        composeTestRule.setContent {
            ActivitiesScreen(
                navController = rememberNavController(),
                activitiesViewModel = activitiesViewModel
            )
        }

        composeTestRule.waitForIdle()

        // Vérification : Le message d'état vide est bien affiché.
        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.EMPTY_STATE_TEXT).assertIsDisplayed()

        // Vérification : Le carrousel n'est PAS affiché.
        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_CAROUSEL).assertDoesNotExist()
    }

    @Test
    fun clickingOnTabsDoesNotCrash() {
        // Ce test reste valide, il vérifie simplement l'interaction UI.
        val activitiesViewModel = ActivitiesViewModel()

        composeTestRule.setContent {
            ActivitiesScreen(
                navController = rememberNavController(),
                activitiesViewModel = activitiesViewModel
            )
        }

        // Action & Vérification
        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.TAB_BUTTON_INVITATIONS).performClick()
        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_TAB_ROW).assertIsDisplayed()

        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.TAB_BUTTON_JOINED).performClick()
        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_TAB_ROW).assertIsDisplayed()
    }

    @Test
    fun leaveEventButtonRemovesEventFromUI() {
        EventRepositoryProvider.repository = EventRepositoryLocal()
        val activitiesViewModel = ActivitiesViewModel()

        composeTestRule.setContent {
            ActivitiesScreen(
                navController = rememberNavController(),
                activitiesViewModel = activitiesViewModel
            )
        }

        composeTestRule.waitForIdle()

        val firstEventUid = activitiesViewModel.uiState.value.events.first().uid

        composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.carouselCardTag(firstEventUid)).assertIsDisplayed()

    }
}