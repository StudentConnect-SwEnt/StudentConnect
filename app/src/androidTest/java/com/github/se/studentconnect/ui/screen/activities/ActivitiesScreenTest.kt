package com.github.se.studentconnect.ui.screen.activities

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.event.Event
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test

class ActivitiesScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Note : Pour des tests réels, vous injecteriez un ViewModel factice (mock)
  // pour contrôler l'état (isLoading, items, etc.).

  @Test
  fun activitiesScreen_displaysCorrectlyWithTabNavigation() {
    composeTestRule.setContent { ActivitiesScreen(navController = rememberNavController()) }

    // Verify screen and top bar are displayed
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.TOP_APP_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithText("MyActivities").assertIsDisplayed()

    // Verify tab row is visible
    val tabRow = composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.ACTIVITIES_TAB_ROW)
    tabRow.assertIsDisplayed()

    // Test tab navigation
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Invitations")).performClick()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Archived")).performClick()
    composeTestRule.onNodeWithTag(ActivitiesScreenTestTags.tab("Archived")).assertIsSelected()
  }

  @Test
  fun invitationCard_actionButtonsAreClickable() {
    val invitation = Invitation("invitation1", "user1", InvitationStatus.Pending)
    val event =
        Event.Public(
            "event1",
            "user2",
            "Event Title",
            "Event Subtitle",
            "Desc",
            null,
            Timestamp.now(),
            isFlash = false,
            subtitle = "sub")
    val item = InvitationCarouselItem(invitation, event, "Inviter")

    var accepted = false
    var declined = false

    composeTestRule.setContent {
      InvitationCarouselCard(
          item = item,
          onAcceptClick = { accepted = true },
          onDeclineClick = { declined = true },
          onCardClick = {})
    }

    // Cliquer sur le bouton Accepter
    composeTestRule.onNodeWithContentDescription("Accept").performClick()
    assert(accepted)

    // Cliquer sur le bouton Refuser
    composeTestRule.onNodeWithContentDescription("Decline").performClick()
    assert(declined)
  }
}
