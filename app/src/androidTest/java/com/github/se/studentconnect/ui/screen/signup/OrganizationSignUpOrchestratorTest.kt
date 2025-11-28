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
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.repository.OrganizationRepository
import com.github.se.studentconnect.repository.OrganizationRepositoryProvider
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationInfoScreenTestTags
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpOrchestrator
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

    OrganizationRepositoryProvider.repository = mockOrgRepository
    MediaRepositoryProvider.repository = mockMediaRepository
  }

  @Test
  fun organizationSignUpFlow_navigationBack_works() = runTest {
    val viewModel = OrganizationSignUpViewModel()
    var backToSelectionCalled = false

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123",
          onLogout = {},
          onBackToSelection = { backToSelectionCalled = true },
          viewModel = viewModel)
    }

    // 1. Info Screen -> Back
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.BACK_BUTTON).performClick()
    assert(backToSelectionCalled)

    // Reset and go forward
    backToSelectionCalled = false
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
  fun organizationSignUpFlow_submissionFailure_handlesError() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    // Mock repository failure
    `when`(mockOrgRepository.getNewOrganizationId()).thenReturn("newOrgId")
    `when`(mockOrgRepository.saveOrganization(any())).thenThrow(RuntimeException("Network error"))

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123", onLogout = {}, onBackToSelection = {}, viewModel = viewModel)
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

    // Verify we are STILL on the Team Roles screen (did not proceed to "To Be Continued")
    composeTestRule.onNodeWithText("Start Now").assertIsDisplayed()
  }
}
