package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
class CreatePrivateEventScreenUnitTest {

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
    composeTestRule.setContent { AppTheme { CreatePrivateEventScreen(navController = null) } }
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
  fun createPrivateEventScreen_displaysAllRequiredFields() {
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SCAFFOLD).assertExists()
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT).assertExists()
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT).assertExists()
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.BANNER_PICKER).assertExists()
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT).assertExists()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)
        .assertExists()
  }

  @Test
  fun createPrivateEventScreen_saveButtonDisabledWhenTitleEmpty() {
    // Save button should be disabled when title is empty
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun createPrivateEventScreen_displaysDateTimeFieldsWhenNotFlash() {
    // By default, flash is off, so date/time fields should exist
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT).assertExists()
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT).assertExists()
  }

  @Test
  fun createPrivateEventScreen_displaysParticipantsAndFeesFields() {
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
        .assertExists()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH)
        .assertExists()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
        .assertExists()
  }

  @Test
  fun createPrivateEventScreen_topAppBarDisplayed() {
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.TOP_APP_BAR).assertExists()
  }

  @Test
  fun createPrivateEventScreen_backButtonDisplayed() {
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.BACK_BUTTON).assertExists()
  }

  @Test
  fun createPrivateEventScreen_removeBannerButtonDisplayed() {
    // Scroll to remove button which may be off-screen
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.REMOVE_BANNER_BUTTON)
        .performScrollTo()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.REMOVE_BANNER_BUTTON)
        .assertExists()
  }

  @Test
  fun createPrivateEventScreen_removeBannerButtonDisabledInitially() {
    // Scroll to remove button which may be off-screen
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.REMOVE_BANNER_BUTTON)
        .performScrollTo()
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.REMOVE_BANNER_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun createPrivateEventScreen_scrollColumnDisplayed() {
    composeTestRule.onNodeWithTag(CreatePrivateEventScreenTestTags.SCROLL_COLUMN).assertExists()
  }

  @Test
  fun createPrivateEventScreen_titleInputAcceptsText() {
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
        .performTextInput("My Private Event")

    // After entering title, the text should be present
    composeTestRule.onNodeWithText("My Private Event").assertExists()
  }

  @Test
  fun createPrivateEventScreen_participantsInputAcceptsText() {
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
        .performTextInput("50")

    composeTestRule.onNodeWithText("50").assertExists()
  }

  @Test
  fun createPrivateEventScreen_descriptionInputAcceptsText() {
    composeTestRule
        .onNodeWithTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
        .performTextInput("Event description here")

    composeTestRule.onNodeWithText("Event description here").assertExists()
  }
}
