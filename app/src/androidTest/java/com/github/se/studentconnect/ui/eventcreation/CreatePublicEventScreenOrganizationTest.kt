// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests to maximize line coverage for organization-related UI components in
 * CreatePublicEventScreen.
 *
 * These tests ensure that when a user owns organizations, the organization selection UI (switch and
 * dropdown) is properly displayed and functional.
 */
@OptIn(ExperimentalTestApi::class)
class CreatePublicEventScreenOrganizationTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var organizationRepository: OrganizationRepositoryLocal

  @Before
  fun setUpOrganizations() {
    // Set up repositories
    EventRepositoryProvider.overrideForTests(EventRepositoryLocal())
    organizationRepository = OrganizationRepositoryLocal()
    OrganizationRepositoryProvider.overrideForTests(organizationRepository)

    // Get the actual test user ID from Firebase Auth (signed in anonymously by base
    // class)
    val actualUserId = currentUser.uid

    // Add test organizations where the current user is the creator
    runBlocking {
      val org1 =
          Organization(
              id = "org_public_1",
              name = "Music Society",
              type = OrganizationType.Association,
              description = "For music lovers",
              createdBy = actualUserId,
              createdAt = Timestamp.now())

      val org2 =
          Organization(
              id = "org_public_2",
              name = "Robotics Club",
              type = OrganizationType.StudentClub,
              description = "Build robots together",
              createdBy = actualUserId,
              createdAt = Timestamp.now())

      organizationRepository.saveOrganization(org1)
      organizationRepository.saveOrganization(org2)
    }

    // Set content - the ViewModel will load organizations in its init block
    composeTestRule.setContent { AppTheme { CreatePublicEventScreen(navController = null) } }
  }

  private fun waitForTag(tag: String, timeoutMillis: Long = 10000) {
    composeTestRule.waitUntil(timeoutMillis) {
      composeTestRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  // --------------------------------------------------
  // Organization Switch Display Tests
  // --------------------------------------------------

  @Test
  fun organizationSwitch_isDisplayedWhenOrganizationsExist() {
    // Wait for organizations to load
    composeTestRule.waitForIdle()
    Thread.sleep(500) // Give time for async loading

    // Scroll to make sure it's visible
    waitForTag(CreatePublicEventScreenTestTags.SCROLL_COLUMN)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SCROLL_COLUMN).performTouchInput {
      swipeUp()
    }

    // Wait for the switch to appear
    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)

    // Verify the switch is displayed
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun organizationToggle_showsCorrectLabel() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
        .performScrollTo()

    // Verify the label text exists (from string resources)
    composeTestRule.onNodeWithText("Create as organization", substring = true).assertExists()
  }

  // --------------------------------------------------
  // Organization Switch Toggle Tests
  // --------------------------------------------------

  @Test
  fun organizationSwitch_canBeToggled() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    val switchNode =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)

    switchNode.performScrollTo()

    // Initially should be off (unchecked)
    switchNode.assertIsOff()

    // Toggle on
    switchNode.performClick()
    composeTestRule.waitForIdle()

    // Should now be on
    switchNode.assertIsOn()

    // Toggle off
    switchNode.performClick()
    composeTestRule.waitForIdle()

    // Should be off again
    switchNode.assertIsOff()
  }

  // --------------------------------------------------
  // Organization Dropdown Display Tests
  // --------------------------------------------------

  @Test
  fun organizationDropdown_isDisplayedWhenToggleIsOn() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    val switchNode =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)

    switchNode.performScrollTo()
    switchNode.performClick() // Turn on the toggle

    composeTestRule.waitForIdle()

    // Dropdown should now be visible
    waitForTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun organizationDropdown_showsSelectOrganizationLabel() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()

    waitForTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN).performScrollTo()

    // Verify the dropdown label exists
    composeTestRule.onNodeWithText("Select organization", substring = true).assertExists()
  }

  @Test
  fun organizationDropdown_displaysSelectedOrganizationName() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()
    Thread.sleep(300)

    waitForTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)

    // When toggle is enabled, first organization should be auto-selected
    // Check if organization name appears in the UI
    composeTestRule.onNodeWithText("Music Society", substring = true).assertExists()
  }

  // --------------------------------------------------
  // Organization Dropdown Expansion and Selection Tests
  // --------------------------------------------------

  @Test
  fun organizationDropdown_canBeExpanded() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()

    waitForTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
    val dropdown = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)

    dropdown.performScrollTo()
    dropdown.performClick() // Expand the dropdown

    composeTestRule.waitForIdle()

    // Both organizations should be visible in dropdown menu - use test tags
    composeTestRule.onNodeWithTag("orgDropdownItem_org_public_1").assertExists()
    composeTestRule.onNodeWithTag("orgDropdownItem_org_public_2").assertExists()
  }

  @Test
  fun organizationDropdown_canSelectDifferentOrganization() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()

    waitForTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
    val dropdown = composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)

    dropdown.performScrollTo()
    dropdown.performClick() // Expand

    composeTestRule.waitForIdle()

    // Click on the second organization
    composeTestRule.onNodeWithText("Robotics Club").performClick()

    composeTestRule.waitForIdle()

    // The dropdown should now display the selected organization
    composeTestRule.onNodeWithText("Robotics Club", useUnmergedTree = true).assertExists()
  }

  @Test
  fun organizationDropdownItem_hasCorrectTestTag() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()

    waitForTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()

    // Verify dropdown items have correct test tags
    composeTestRule.onNodeWithTag("orgDropdownItem_org_public_1").assertExists()
    composeTestRule.onNodeWithTag("orgDropdownItem_org_public_2").assertExists()
  }

  @Test
  fun organizationDropdown_closesAfterSelection() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()

    waitForTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
        .performScrollTo()
        .performClick() // Expand

    composeTestRule.waitForIdle()

    // Select an organization using its test tag (avoids matching multiple nodes)
    composeTestRule.onNodeWithTag("orgDropdownItem_org_public_1").performClick()

    composeTestRule.waitForIdle()

    // After selection, the second dropdown item should no longer exist
    composeTestRule.onNodeWithTag("orgDropdownItem_org_public_2").assertDoesNotExist()
  }

  // --------------------------------------------------
  // Combined Behavior Tests
  // --------------------------------------------------

  @Test
  fun organizationDropdown_hidesWhenToggleIsDisabled() {
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    waitForTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)
    val switchNode =
        composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH)

    switchNode.performScrollTo()
    switchNode.performClick() // Enable

    composeTestRule.waitForIdle()

    // Dropdown should be visible
    waitForTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN).assertExists()

    // Disable the toggle
    switchNode.performScrollTo()
    switchNode.performClick()

    composeTestRule.waitForIdle()

    // Dropdown should no longer be visible
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN).assertDoesNotExist()
  }
}
