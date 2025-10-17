package com.github.se.studentconnect.ui.screen.activities

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule

class ActivitiesScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Note : Pour des tests réels, vous injecteriez un ViewModel factice (mock)
  // pour contrôler l'état (isLoading, items, etc.).

  //  @Test
  //  fun activitiesScreen_displaysCorrectly() {
  //    composeTestRule.setContent { ActivitiesScreen(navController = rememberNavController()) }
  //
  //    // Vérifier que l'écran et la barre supérieure sont affichés
  //
  // composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN).assertIsDisplayed()
  //    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.TOP_APP_BAR).assertIsDisplayed()
  //    composeTestRule.onNodeWithText("MyActivities").assertIsDisplayed()
  //  }
  //
  //  @Test
  //  fun tabNavigation_worksAsExpected() {
  //    composeTestRule.setContent { ActivitiesScreen(navController = rememberNavController()) }
  //
  //    // Vérifier que la barre d'onglets est visible
  //    val tabRow = composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_TAB_ROW)
  //    tabRow.assertIsDisplayed()
  //
  //    // Cliquer sur l'onglet "Invitations"
  //    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Invitations")).performClick()
  //    // Dans un vrai test, on vérifierait que le contenu a changé
  //
  //    // Cliquer sur l'onglet "Archived" (Past)
  //    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Archived")).performClick()
  //    // Vérifier que cet onglet est maintenant sélectionné
  //    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Archived")).assertIsSelected()
  //  }
  //
  //  @Test
  //  fun invitationCard_actionButtonsAreClickable() {
  //    val invitation = Invitation("invitation1", "user1", InvitationStatus.Pending)
  //    val event =
  //        Event.Public(
  //            "event1",
  //            "user2",
  //            "Event Title",
  //            "Event Subtitle",
  //            "Desc",
  //            null,
  //            Timestamp.now(),
  //            isFlash = false,
  //            subtitle = "sub")
  //    val item = InvitationCarouselItem(invitation, event, "Inviter")
  //
  //    var accepted = false
  //    var declined = false
  //
  //    composeTestRule.setContent {
  //      InvitationCarouselCard(
  //          item = item,
  //          onAcceptClick = { accepted = true },
  //          onDeclineClick = { declined = true },
  //          onCardClick = {})
  //    }
  //
  //    // Cliquer sur le bouton Accepter
  //    composeTestRule.onNodeWithContentDescription("Accept").performClick()
  //    assert(accepted)
  //
  //    // Cliquer sur le bouton Refuser
  //    composeTestRule.onNodeWithContentDescription("Decline").performClick()
  //    assert(declined)
  //  }
}
