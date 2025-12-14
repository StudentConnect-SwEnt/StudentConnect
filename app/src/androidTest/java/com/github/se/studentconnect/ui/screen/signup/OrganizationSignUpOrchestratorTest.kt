package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationInfoScreenTestTags
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpOrchestrator
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpStep
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

@RunWith(AndroidJUnit4::class)
class OrganizationSignUpOrchestratorTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val ctx: Context = ApplicationProvider.getApplicationContext()

  private lateinit var mockOrgRepository: OrganizationRepository
  private lateinit var mockMediaRepository: MediaRepository

  @Before
  fun setUp() {
    mockOrgRepository = mock(OrganizationRepository::class.java)
    mockMediaRepository = mock(MediaRepository::class.java)

    OrganizationRepositoryProvider.overrideForTests(mockOrgRepository)
    MediaRepositoryProvider.overrideForTests(mockMediaRepository)
  }

  @Test
  fun organizationSignUpFlow_navigationBack_works() = runTest {
    val viewModel = OrganizationSignUpViewModel()
    var backToSelectionCalled = false

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123",
          onBackToSelection = { backToSelectionCalled = true },
          viewModel = viewModel)
    }

    // 1. Info Screen -> Back
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.BACK_BUTTON).performClick()
    assert(backToSelectionCalled)

    // Go forward
    composeTestRule
        .onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT)
        .performTextInput("My Org")
    composeTestRule.onNodeWithText("Association").performClick()
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).performClick()

    // 2. Logo Screen -> Back
    composeTestRule
        .onNodeWithContentDescription(ctx.getString(R.string.content_description_back))
        .performClick()
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT).assertIsDisplayed()
  }

  @Test
  fun organizationSignUpFlow_descriptionScreen_displaysAndNavigates() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onBackToSelection = {}, viewModel = viewModel)
    }

    // Navigate to Description screen
    viewModel.setOrganizationName("My Org")
    viewModel.toggleOrganizationType(OrganizationType.Association)
    viewModel.nextStep() // Logo
    viewModel.nextStep() // Description

    composeTestRule.waitForIdle()

    // Verify Description screen is displayed using its container tag
    composeTestRule.onNodeWithTag(C.Tag.about_screen_container).assertIsDisplayed()

    // Test back navigation
    composeTestRule.onNodeWithTag(C.Tag.about_back).performClick()

    composeTestRule.waitForIdle()

    // Should be back on Logo screen
    assert(viewModel.state.value.currentStep == OrganizationSignUpStep.Logo)
  }

  @Test
  fun organizationSignUpFlow_descriptionScreen_continueNavigatesToSocials() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onBackToSelection = {}, viewModel = viewModel)
    }

    // Navigate to Description screen
    viewModel.setOrganizationName("My Org")
    viewModel.toggleOrganizationType(OrganizationType.Association)
    viewModel.nextStep() // Logo
    viewModel.nextStep() // Description

    composeTestRule.waitForIdle()

    // Click continue
    composeTestRule.onNodeWithTag(C.Tag.about_continue).performClick()

    composeTestRule.waitForIdle()

    // Should be on Socials screen
    assert(viewModel.state.value.currentStep == OrganizationSignUpStep.Socials)
  }

  @Test
  fun organizationSignUpFlow_socialsScreen_displaysAndNavigates() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onBackToSelection = {}, viewModel = viewModel)
    }

    // Navigate to Socials screen
    viewModel.setOrganizationName("My Org")
    viewModel.toggleOrganizationType(OrganizationType.Association)
    viewModel.nextStep() // Logo
    viewModel.nextStep() // Description
    viewModel.nextStep() // Socials

    composeTestRule.waitForIdle()

    // Verify Socials/Brand screen is displayed - check for Skip button
    composeTestRule.onNodeWithText("Skip").assertIsDisplayed()

    // Test back navigation
    composeTestRule
        .onNodeWithContentDescription(ctx.getString(R.string.content_description_back))
        .performClick()

    composeTestRule.waitForIdle()

    // Should be back on Description screen
    assert(viewModel.state.value.currentStep == OrganizationSignUpStep.Description)
  }

  @Test
  fun organizationSignUpFlow_socialsScreen_skipClearsSocialFields() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    // Pre-set some social values
    viewModel.setWebsite("https://example.com")
    viewModel.setInstagram("@myorg")
    viewModel.setX("@myorg_x")
    viewModel.setLinkedin("myorg-linkedin")

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onBackToSelection = {}, viewModel = viewModel)
    }

    // Navigate to Socials screen
    viewModel.setOrganizationName("My Org")
    viewModel.toggleOrganizationType(OrganizationType.Association)
    viewModel.nextStep() // Logo
    viewModel.nextStep() // Description
    viewModel.nextStep() // Socials

    composeTestRule.waitForIdle()

    // Click Skip
    composeTestRule.onNodeWithText("Skip").performClick()

    composeTestRule.waitForIdle()

    // Verify social fields are cleared
    assert(viewModel.state.value.websiteUrl.isEmpty())
    assert(viewModel.state.value.instagramHandle.isEmpty())
    assert(viewModel.state.value.xHandle.isEmpty())
    assert(viewModel.state.value.linkedinUrl.isEmpty())

    // Verify we moved to ProfileSetup
    assert(viewModel.state.value.currentStep == OrganizationSignUpStep.ProfileSetup)
  }

  @Test
  fun organizationSignUpFlow_profileSetupScreen_displaysAndNavigates() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onBackToSelection = {}, viewModel = viewModel)
    }

    // Navigate to ProfileSetup screen
    viewModel.setOrganizationName("My Org")
    viewModel.toggleOrganizationType(OrganizationType.Association)
    viewModel.nextStep() // Logo
    viewModel.nextStep() // Description
    viewModel.nextStep() // Socials
    viewModel.nextStep() // ProfileSetup

    composeTestRule.waitForIdle()

    // Test back navigation
    composeTestRule
        .onNodeWithContentDescription(ctx.getString(R.string.content_description_back))
        .performClick()

    composeTestRule.waitForIdle()

    // Should be back on Socials screen
    assert(viewModel.state.value.currentStep == OrganizationSignUpStep.Socials)
  }

  @Test
  fun organizationSignUpFlow_profileSetupScreen_startNowNavigatesToTeam() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onBackToSelection = {}, viewModel = viewModel)
    }

    // Navigate to ProfileSetup screen
    viewModel.setOrganizationName("My Org")
    viewModel.toggleOrganizationType(OrganizationType.Association)
    viewModel.nextStep() // Logo
    viewModel.nextStep() // Description
    viewModel.nextStep() // Socials
    viewModel.nextStep() // ProfileSetup

    composeTestRule.waitForIdle()

    // Fill required fields and continue via viewModel
    viewModel.setLocation("Lausanne")
    viewModel.toggleDomain("Tech")
    viewModel.setEventSize("20-50")
    viewModel.nextStep() // Team

    composeTestRule.waitForIdle()

    // Should be on Team screen
    assert(viewModel.state.value.currentStep == OrganizationSignUpStep.Team)
    composeTestRule.onNodeWithText("Start Now").assertIsDisplayed()
  }

  @Test
  fun organizationSignUpFlow_teamScreen_displaysCorrectly() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    // Mock repository success
    `when`(mockOrgRepository.getNewOrganizationId()).thenReturn("newOrgId")

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onBackToSelection = {}, viewModel = viewModel)
    }

    // Navigate to Team screen
    viewModel.setOrganizationName("My Org")
    viewModel.toggleOrganizationType(OrganizationType.Association)
    viewModel.nextStep() // Logo
    viewModel.nextStep() // Description
    viewModel.nextStep() // Socials
    viewModel.nextStep() // ProfileSetup
    viewModel.setLocation("Lausanne")
    viewModel.toggleDomain("Tech")
    viewModel.setEventSize("20-50")
    viewModel.nextStep() // Team

    composeTestRule.waitForIdle()

    // Verify Team screen is displayed
    composeTestRule.onNodeWithText("Start Now").assertIsDisplayed()
    composeTestRule.onNodeWithText("Role name").assertIsDisplayed()
    composeTestRule.onNodeWithText("+ Add role").assertIsDisplayed()
  }

  @Test
  fun organizationSignUpFlow_submissionFailure_handlesError() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    // Mock repository failure
    `when`(mockOrgRepository.getNewOrganizationId()).thenReturn("newOrgId")
    `when`(mockOrgRepository.saveOrganization(any())).thenThrow(RuntimeException("Network error"))

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onBackToSelection = {}, viewModel = viewModel)
    }

    // Fast forward to Team Roles Screen
    viewModel.setOrganizationName("My Org")
    viewModel.toggleOrganizationType(OrganizationType.Association)
    viewModel.nextStep() // Logo
    viewModel.nextStep() // Description
    viewModel.nextStep() // Socials
    viewModel.nextStep() // Profile Setup
    viewModel.setLocation("Lausanne")
    viewModel.toggleDomain("Tech")
    viewModel.setEventSize("20-50")
    viewModel.nextStep() // Team

    // Add role
    composeTestRule.onNodeWithText("Role name").performTextInput("President")
    composeTestRule.onNodeWithText("+ Add role").performClick()

    // Click Submit
    composeTestRule.onNodeWithText("Start Now").performClick()

    // Verify repository interaction
    composeTestRule.waitForIdle()
    verify(mockOrgRepository).saveOrganization(any())

    // Verify we are STILL on the Team Roles screen (did not navigate away on error)
    composeTestRule.onNodeWithText("Start Now").assertIsDisplayed()
  }

  @Test
  fun organizationSignUpFlow_fullNavigation_fromInfoToTeam() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onBackToSelection = {}, viewModel = viewModel)
    }

    // Verify starting at Info screen
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT).assertIsDisplayed()

    // Fill Info and continue
    composeTestRule
        .onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT)
        .performTextInput("Test Organization")
    composeTestRule.onNodeWithText("Association").performClick()
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Now on Logo screen - skip
    viewModel.nextStep() // Description

    composeTestRule.waitForIdle()

    // Now on Description screen - continue
    viewModel.nextStep() // Socials

    composeTestRule.waitForIdle()

    // Now on Socials screen - continue
    viewModel.nextStep() // ProfileSetup

    composeTestRule.waitForIdle()

    // Now on ProfileSetup screen - fill required fields and continue
    viewModel.setLocation("Geneva")
    viewModel.toggleDomain("Tech")
    viewModel.setEventSize("10-20")
    viewModel.nextStep() // Team

    composeTestRule.waitForIdle()

    // Verify we reached Team screen
    composeTestRule.onNodeWithText("Start Now").assertIsDisplayed()
  }
}
