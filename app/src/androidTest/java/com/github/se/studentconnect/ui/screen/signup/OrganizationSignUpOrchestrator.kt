package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import android.net.Uri
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
import com.github.se.studentconnect.resources.C
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
class OrganizationSignUpOrchestrator {

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
  fun organizationSignUpFlow_completesSuccessfully() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    // Mock repository responses
    `when`(mockOrgRepository.getNewOrganizationId()).thenReturn("newOrgId")

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123",
          onSignUpComplete = {},
          onLogout = {},
          onBackToSelection = {},
          viewModel = viewModel)
    }

    // 1. Info Screen
    composeTestRule
        .onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT)
        .performTextInput("My Org")
    composeTestRule.onNodeWithText("Association").performClick() // Select type
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).performClick()

    // 2. Logo Screen
    composeTestRule.onNodeWithText("Skip").performClick()

    // 3. Description Screen
    composeTestRule.onNodeWithTag(C.Tag.about_input).performTextInput("Description")
    composeTestRule.onNodeWithTag(C.Tag.about_continue).performClick()

    // 4. Socials Screen
    composeTestRule.onNodeWithText("Skip").performClick()

    // 5. Profile Setup Screen
    composeTestRule.onNodeWithText("Search locations...").performClick()
    composeTestRule.onNodeWithText("EPFL").performClick()
    composeTestRule.onNodeWithText("Tech").performClick() // Select domain
    composeTestRule.onNodeWithText("20-50").performClick() // Select size
    composeTestRule.onNodeWithText("Continue").performClick()

    // 6. Team Roles Screen
    // Add a role to enable continue button
    composeTestRule.onNodeWithText("Role name").performTextInput("President")
    composeTestRule.onNodeWithText("+ Add role").performClick()

    // Click Start Now (Submit)
    composeTestRule.onNodeWithText("Start Now").performClick()

    // Verify repository interaction
    composeTestRule.waitForIdle()
    verify(mockOrgRepository).saveOrganization(any())

    // Verify "To Be Continued" screen is shown
    composeTestRule.onNodeWithText("My Org").assertIsDisplayed()
  }

  @Test
  fun organizationSignUpFlow_withLogoUpload_uploadsImage() = runTest {
    val viewModel = OrganizationSignUpViewModel()

    // Mock repository responses
    `when`(mockOrgRepository.getNewOrganizationId()).thenReturn("newOrgId")

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123",
          onSignUpComplete = {},
          onLogout = {},
          onBackToSelection = {},
          viewModel = viewModel)
    }

    // 1. Info Screen
    composeTestRule
        .onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT)
        .performTextInput("My Org")
    composeTestRule.onNodeWithText("Association").performClick()
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).performClick()

    // 2. Logo Screen - Simulate selecting an image
    viewModel.setLogoUri(Uri.parse("content://media/external/images/media/1"))

    composeTestRule.onNodeWithText("Continue").performClick()

    // 3. Description Screen
    composeTestRule.onNodeWithTag(C.Tag.about_input).performTextInput("Description")
    composeTestRule.onNodeWithTag(C.Tag.about_continue).performClick()

    // 4. Socials Screen
    composeTestRule.onNodeWithText("Skip").performClick()

    // 5. Profile Setup Screen
    composeTestRule.onNodeWithText("Search locations...").performClick()
    composeTestRule.onNodeWithText("EPFL").performClick()
    composeTestRule.onNodeWithText("Tech").performClick()
    composeTestRule.onNodeWithText("20-50").performClick()
    composeTestRule.onNodeWithText("Continue").performClick()

    // 6. Team Roles Screen
    composeTestRule.onNodeWithText("Role name").performTextInput("President")
    composeTestRule.onNodeWithText("+ Add role").performClick()
    composeTestRule.onNodeWithText("Start Now").performClick()

    // Verify upload was called
    composeTestRule.waitForIdle()
    verify(mockMediaRepository).upload(any(), any())
    verify(mockOrgRepository).saveOrganization(any())
  }

  @Test
  fun organizationSignUpFlow_navigationBack_works() = runTest {
    val viewModel = OrganizationSignUpViewModel()
    var backToSelectionCalled = false

    composeTestRule.setContent {
      OrganizationSignUpOrchestrator(
          firebaseUserId = "user123",
          onSignUpComplete = {},
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
          firebaseUserId = "user123",
          onSignUpComplete = {},
          onLogout = {},
          onBackToSelection = {},
          viewModel = viewModel)
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
