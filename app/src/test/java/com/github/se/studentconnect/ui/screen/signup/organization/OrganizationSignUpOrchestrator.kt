package com.github.se.studentconnect.ui.screen.signup.organization

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.repository.OrganizationRepository
import com.github.se.studentconnect.repository.OrganizationRepositoryProvider
import com.github.se.studentconnect.resources.C
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29], qualifiers = "w400dp-h900dp")
class OrganizationSignUpOrchestrator {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
    // Using tags defined in the screen object
    composeTestRule
        .onNodeWithTag(OrganizationInfoScreenTestTags.ORG_NAME_INPUT)
        .performTextInput("My Org")
    composeTestRule.onNodeWithText("Association").performClick() // Select type by text
    composeTestRule.onNodeWithTag(OrganizationInfoScreenTestTags.CONTINUE_BUTTON).performClick()

    // 2. Logo Screen
    composeTestRule.onNodeWithText("Skip").performClick()

    // 3. Description Screen
    // Using tags defined in C.Tag as mapped in OrganizationDescriptionScreen
    composeTestRule.onNodeWithTag(C.Tag.about_input).performTextInput("Description")
    composeTestRule.onNodeWithTag(C.Tag.about_continue).performClick()

    // 4. Socials Screen
    composeTestRule.onNodeWithText("Skip").performClick()

    // 5. Profile Setup Screen
    // Location is a dropdown. We select "EPFL" (first item) to ensure it is visible
    // in the LazyColumn without needing to scroll.
    composeTestRule.onNodeWithText("Search locations...").performClick()
    composeTestRule.onNodeWithText("EPFL").performClick()

    composeTestRule.onNodeWithText("Tech").performClick() // Select domain
    composeTestRule.onNodeWithText("20-50").performClick() // Select size

    // The button on Profile Setup screen is labeled "Continue"
    composeTestRule.onNodeWithText("Continue").performClick()

    // 6. Team Roles Screen
    // Add a role to enable continue button
    composeTestRule.onNodeWithText("Role name").performTextInput("President")
    composeTestRule.onNodeWithText("+ Add role").performClick()

    // Click Start Now (Submit) - The button on Team Roles screen is labeled "Start Now"
    composeTestRule.onNodeWithText("Start Now").performClick()

    // Verify repository interaction
    composeTestRule.waitForIdle()
    verify(mockOrgRepository).saveOrganization(any())

    // Verify "To Be Continued" screen is shown
    composeTestRule.onNodeWithText("My Org").assertIsDisplayed()
  }
}
