package com.github.se.studentconnect.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun homeScreen_displaysAllComponentsCorrectly() {
    composeTestRule.setContent { HomeScreen(navController = rememberNavController()) }

    // Vérifier que l'écran principal est affiché
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()

    // Vérifier que la barre de recherche est présente
    composeTestRule.onNodeWithText("Search for events...").assertIsDisplayed()

    // Vérifier que l'icône de notifications est présente
    composeTestRule.onNodeWithContentDescription("Notifications").assertIsDisplayed()
  }

  @Test
  fun notificationIcon_togglesDropdownMenu() {
    composeTestRule.setContent { HomeScreen(navController = rememberNavController()) }

    // Le menu déroulant ne doit pas être visible initialement
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.INVITATIONS_POPOVER).assertDoesNotExist()

    // Cliquer sur l'icône de notifications pour afficher le menu
    composeTestRule.onNodeWithContentDescription("Notifications").performClick()

    // Vérifier que le menu déroulant est maintenant visible
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.INVITATIONS_POPOVER).assertIsDisplayed()

    // Cliquer à nouveau (simule la fermeture, bien que le onDismiss soit plus complexe)
    // Pour un test simple, on peut vérifier l'état après une interaction
    composeTestRule.onNodeWithContentDescription("Notifications").performClick()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.INVITATIONS_POPOVER).assertDoesNotExist()
  }
}
