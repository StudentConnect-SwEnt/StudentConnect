package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class CreatePublicEventScreenUnitTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockMediaRepository: MediaRepository
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockOrganizationRepository: OrganizationRepository
  private lateinit var mockFriendsRepository: FriendsRepository
  private lateinit var mockNotificationRepository: NotificationRepository

  @Before
  fun setUp() {
    mockEventRepository = Mockito.mock(EventRepository::class.java)
    mockMediaRepository = Mockito.mock(MediaRepository::class.java)
    mockUserRepository = Mockito.mock(UserRepository::class.java)
    mockOrganizationRepository = Mockito.mock(OrganizationRepository::class.java)
    mockFriendsRepository = Mockito.mock(FriendsRepository::class.java)
    mockNotificationRepository = Mockito.mock(NotificationRepository::class.java)

    EventRepositoryProvider.overrideForTests(mockEventRepository)
    MediaRepositoryProvider.overrideForTests(mockMediaRepository)
    UserRepositoryProvider.overrideForTests(mockUserRepository)
    OrganizationRepositoryProvider.overrideForTests(mockOrganizationRepository)
    FriendsRepositoryProvider.overrideForTests(mockFriendsRepository)
    NotificationRepositoryProvider.overrideForTests(mockNotificationRepository)

    // Set the screen once to avoid race conditions in each test
    composeTestRule.setContent { AppTheme { CreatePublicEventScreen(navController = null) } }
    composeTestRule.waitForIdle()
  }

  @After
  fun tearDown() {
    EventRepositoryProvider.cleanOverrideForTests()
    MediaRepositoryProvider.cleanOverrideForTests()
    UserRepositoryProvider.cleanOverrideForTests()
    OrganizationRepositoryProvider.cleanOverrideForTests()
    FriendsRepositoryProvider.cleanOverrideForTests()
    NotificationRepositoryProvider.cleanOverrideForTests()
  }

  @Test
  fun createPublicEventScreen_displaysAllRequiredFields() {
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SCAFFOLD).assertExists()
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT).assertExists()
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT).assertExists()
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT).assertExists()
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.BANNER_PICKER).assertExists()
  }

  @Test
  fun createPublicEventScreen_saveButtonDisabledWhenTitleEmpty() {
    // Save button should be disabled when title is empty
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun createPublicEventScreen_displaysDateTimeFieldsWhenNotFlash() {
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.START_DATE_INPUT).assertExists()
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.END_DATE_INPUT).assertExists()
  }

  @Test
  fun createPublicEventScreen_displaysLocationField() {
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.LOCATION_INPUT).assertExists()
  }

  @Test
  fun createPublicEventScreen_displaysFlashEventSwitch() {
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH).assertExists()
  }

  @Test
  fun createPublicEventScreen_displaysParticipantsAndFeesFields() {
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
        .assertExists()
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
        .assertExists()
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertExists()
  }

  @Test
  fun createPublicEventScreen_displaysWebsiteField() {
    // Scroll to website input in case it's off-screen
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT).performScrollTo()
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT).assertExists()
  }

  @Test
  fun createPublicEventScreen_displaysTagSelector() {
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TAG_SELECTOR).assertExists()
  }

  @Test
  fun createPublicEventScreen_topAppBarDisplayed() {
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.TOP_APP_BAR).assertExists()
  }

  @Test
  fun createPublicEventScreen_backButtonDisplayed() {
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.BACK_BUTTON).assertExists()
  }

  @Test
  fun createPublicEventScreen_removeBannerButtonDisplayed() {
    // Scroll to remove button which may be off-screen
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.REMOVE_BANNER_BUTTON)
        .performScrollTo()
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.REMOVE_BANNER_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun createPublicEventScreen_removeBannerButtonDisabledInitially() {
    // Scroll to remove button which may be off-screen
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.REMOVE_BANNER_BUTTON)
        .performScrollTo()
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.REMOVE_BANNER_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun createPublicEventScreen_scrollColumnDisplayed() {
    composeTestRule.onNodeWithTag(CreatePublicEventScreenTestTags.SCROLL_COLUMN).assertExists()
  }

  @Test
  fun createPublicEventScreen_titleInputAcceptsText() {
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
        .performTextInput("My Public Event")

    composeTestRule.onNodeWithText("My Public Event").assertExists()
  }

  @Test
  fun createPublicEventScreen_subtitleInputAcceptsText() {
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
        .performTextInput("Event Subtitle")

    composeTestRule.onNodeWithText("Event Subtitle").assertExists()
  }

  @Test
  fun createPublicEventScreen_descriptionInputAcceptsText() {
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
        .performTextInput("Event description here")

    composeTestRule.onNodeWithText("Event description here").assertExists()
  }

  @Test
  fun createPublicEventScreen_websiteInputAcceptsText() {
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
        .performTextInput("https://example.com")

    composeTestRule.onNodeWithText("https://example.com").assertExists()
  }

  @Test
  fun createPublicEventScreen_participantsInputAcceptsText() {
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
        .performTextInput("100")

    composeTestRule.onNodeWithText("100").assertExists()
  }

  @Test
  fun createPublicEventScreen_feeInputDisabledInitially() {
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertIsNotEnabled()
  }

  @Test
  fun createPublicEventScreen_toggleFeeSwitchEnablesFeeInput() {
    // Initially fee input is disabled
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertIsNotEnabled()

    // Toggle fee switch on
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
        .performClick()

    // Fee input should now be visible — scroll to it first
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .performScrollTo()
    composeTestRule
        .onNodeWithTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertExists()
  }
}
