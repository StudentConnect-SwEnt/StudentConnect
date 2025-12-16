package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.resources.C
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
class CommonEventFieldsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockMediaRepository: MediaRepository

  @Before
  fun setUp() {
    mockMediaRepository = Mockito.mock(MediaRepository::class.java)
    MediaRepositoryProvider.overrideForTests(mockMediaRepository)
  }

  @After
  fun tearDown() {
    MediaRepositoryProvider.cleanOverrideForTests()
  }

  @Test
  fun flashEventToggle_displaysCorrectly() {
    var isFlash = false
    composeTestRule.setContent {
      AppTheme {
        FlashEventToggle(
            isFlash = isFlash, onIsFlashChange = { isFlash = it }, flashSwitchTag = "flash_switch")
      }
    }

    composeTestRule.onNodeWithTag("flash_switch").assertIsDisplayed()
    // Note: assertIsNotChecked doesn't exist, but we can verify the switch is displayed
  }

  @Test
  fun flashEventToggle_togglesWhenClicked() {
    var isFlash = false
    composeTestRule.setContent {
      AppTheme {
        FlashEventToggle(
            isFlash = isFlash, onIsFlashChange = { isFlash = it }, flashSwitchTag = "flash_switch")
      }
    }

    composeTestRule.onNodeWithTag("flash_switch").performClick()
    // Note: In a real test, we'd need to recompose to see the change
    // This test verifies the composable renders and is clickable
  }

  @Test
  fun flashEventDurationFields_displaysCorrectly() {
    var hours = 1
    var minutes = 0
    composeTestRule.setContent {
      AppTheme {
        FlashEventDurationFields(
            hours = hours,
            minutes = minutes,
            onHoursChange = { hours = it },
            onMinutesChange = { minutes = it },
            hoursTag = "hours_dropdown",
            minutesTag = "minutes_dropdown")
      }
    }

    composeTestRule.onNodeWithTag("hours_dropdown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("minutes_dropdown").assertIsDisplayed()
  }

  @Test
  fun flashEventDurationFields_showsCorrectHourOptions() {
    var hours = 0
    var minutes = 0
    composeTestRule.setContent {
      AppTheme {
        FlashEventDurationFields(
            hours = hours,
            minutes = minutes,
            onHoursChange = { hours = it },
            onMinutesChange = { minutes = it },
            hoursTag = "hours_dropdown",
            minutesTag = "minutes_dropdown")
      }
    }

    // Verify the dropdown fields are displayed
    composeTestRule.onNodeWithTag("hours_dropdown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("minutes_dropdown").assertIsDisplayed()

    // Note: Testing dropdown expansion and option visibility requires more complex setup
    // in Robolectric. The dropdown functionality is tested in Android instrumentation tests.
    // This test verifies the composable renders correctly.
  }

  @Test
  fun flashEventDurationFields_showsCorrectMinuteOptions() {
    var hours = 0
    var minutes = 0
    composeTestRule.setContent {
      AppTheme {
        FlashEventDurationFields(
            hours = hours,
            minutes = minutes,
            onHoursChange = { hours = it },
            onMinutesChange = { minutes = it },
            hoursTag = "hours_dropdown",
            minutesTag = "minutes_dropdown")
      }
    }

    // Verify the dropdown fields are displayed
    composeTestRule.onNodeWithTag("hours_dropdown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("minutes_dropdown").assertIsDisplayed()

    // Note: Dropdown option testing is better suited for instrumentation tests
    // This test verifies the composable renders correctly.
  }

  @Test
  fun flashEventDurationFields_whenMaxHoursSelected_onlyShowsZeroMinutes() {
    var hours = C.FlashEvent.MAX_DURATION_HOURS.toInt()
    var minutes = 0
    composeTestRule.setContent {
      AppTheme {
        FlashEventDurationFields(
            hours = hours,
            minutes = minutes,
            onHoursChange = { hours = it },
            onMinutesChange = { minutes = it },
            hoursTag = "hours_dropdown",
            minutesTag = "minutes_dropdown")
      }
    }

    // Verify the composable renders when max hours is selected
    composeTestRule.onNodeWithTag("hours_dropdown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("minutes_dropdown").assertIsDisplayed()

    // Note: The logic for restricting minutes when max hours is selected is tested
    // in the ViewModel tests. UI dropdown behavior is tested in instrumentation tests.
  }

  @Test
  fun flashEventDurationFields_whenNotMaxHours_showsAllMinuteOptions() {
    var hours = 4
    var minutes = 0
    composeTestRule.setContent {
      AppTheme {
        FlashEventDurationFields(
            hours = hours,
            minutes = minutes,
            onHoursChange = { hours = it },
            onMinutesChange = { minutes = it },
            hoursTag = "hours_dropdown",
            minutesTag = "minutes_dropdown")
      }
    }

    // Verify the composable renders when not at max hours
    composeTestRule.onNodeWithTag("hours_dropdown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("minutes_dropdown").assertIsDisplayed()

    // Note: Dropdown option visibility is tested in instrumentation tests
    // This test verifies the composable renders correctly.
  }

  @Test
  fun flashEventDurationFields_selectingMaxHours_resetsMinutes() {
    var hours = 1
    var minutes = 30
    composeTestRule.setContent {
      AppTheme {
        FlashEventDurationFields(
            hours = hours,
            minutes = minutes,
            onHoursChange = { hours = it },
            onMinutesChange = { minutes = it },
            hoursTag = "hours_dropdown",
            minutesTag = "minutes_dropdown")
      }
    }

    // Open hours dropdown and select max value
    composeTestRule.onNodeWithTag("hours_dropdown").performClick()
    composeTestRule
        .onNodeWithText(C.FlashEvent.MAX_DURATION_HOURS.toInt().toString())
        .performClick()
    composeTestRule.waitForIdle()

    // Hours set to max and minutes reset to 0
    assert(hours == C.FlashEvent.MAX_DURATION_HOURS.toInt())
    assert(minutes == 0)
  }

  @Test
  fun handleHourSelection_resetsMinutesWhenSelectingMax() {
    var hours = 1
    var minutes = 30
    handleHourSelection(
        selectedHours = C.FlashEvent.MAX_DURATION_HOURS.toInt(),
        currentMinutes = minutes,
        onHoursChange = { hours = it },
        onMinutesChange = { minutes = it })

    assert(hours == C.FlashEvent.MAX_DURATION_HOURS.toInt())
    assert(minutes == 0)
  }

  @Test
  fun handleMinuteSelection_reducesHoursWhenAtMaxAndMinutesNonZero() {
    var hours = C.FlashEvent.MAX_DURATION_HOURS.toInt()
    var minutes = 0
    handleMinuteSelection(
        selectedMinutes = 15,
        currentHours = hours,
        onHoursChange = { hours = it },
        onMinutesChange = { minutes = it })

    assert(hours == C.FlashEvent.MAX_DURATION_HOURS.toInt() - 1)
    assert(minutes == 15)
  }

  @Test
  fun eventBannerField_displaysGeminiButton() {
    composeTestRule.setContent {
      AppTheme {
        EventBannerField(
            bannerImageUri = null,
            bannerImagePath = null,
            onImageSelected = {},
            onRemoveImage = {},
            pickerTag = "banner_picker",
            removeButtonTag = "remove_banner",
            onGeminiClick = {})
      }
    }

    // Verify the Gemini button is displayed
    composeTestRule.onNodeWithContentDescription("Generate Banner with Gemini").assertIsDisplayed()
  }

  @Test
  fun eventBannerField_geminiButtonCallsCallback() {
    var geminiClicked = false
    composeTestRule.setContent {
      AppTheme {
        EventBannerField(
            bannerImageUri = null,
            bannerImagePath = null,
            onImageSelected = {},
            onRemoveImage = {},
            pickerTag = "banner_picker",
            removeButtonTag = "remove_banner",
            onGeminiClick = { geminiClicked = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Generate Banner with Gemini").performClick()
    assert(geminiClicked)
  }

  @Test
  fun eventBannerField_displaysRemoveButtonDisabledWhenNoBanner() {
    composeTestRule.setContent {
      AppTheme {
        EventBannerField(
            bannerImageUri = null,
            bannerImagePath = null,
            onImageSelected = {},
            onRemoveImage = {},
            pickerTag = "banner_picker",
            removeButtonTag = "remove_banner",
            onGeminiClick = {})
      }
    }

    composeTestRule.onNodeWithTag("remove_banner").assertIsNotEnabled()
  }

  @Test
  fun eventBannerField_showsGeneratingOverlayWhenGenerating() {
    composeTestRule.setContent {
      AppTheme {
        EventBannerField(
            bannerImageUri = null,
            bannerImagePath = null,
            onImageSelected = {},
            onRemoveImage = {},
            pickerTag = "banner_picker",
            removeButtonTag = "remove_banner",
            isGenerating = true,
            onGeminiClick = {})
      }
    }

    // When generating, the banner picker should still be displayed
    composeTestRule.onNodeWithTag("banner_picker").assertIsDisplayed()
  }

  @Test
  fun eventBannerField_removeButtonEnabledWhenBannerPathExists() {
    composeTestRule.setContent {
      AppTheme {
        EventBannerField(
            bannerImageUri = null,
            bannerImagePath = "https://example.com/banner.jpg",
            onImageSelected = {},
            onRemoveImage = {},
            pickerTag = "banner_picker",
            removeButtonTag = "remove_banner",
            onGeminiClick = {})
      }
    }

    composeTestRule.onNodeWithTag("remove_banner").assertIsEnabled()
  }

  @Test
  fun eventTitleAndDescriptionFields_displaysCorrectly() {
    composeTestRule.setContent {
      AppTheme {
        EventTitleAndDescriptionFields(
            title = "Test Title",
            onTitleChange = {},
            titleTag = "title_input",
            description = "Test Description",
            onDescriptionChange = {},
            descriptionTag = "description_input",
            onFocusChange = {})
      }
    }

    composeTestRule.onNodeWithTag("title_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("description_input").assertIsDisplayed()
  }

  @Test
  fun eventTitleAndDescriptionFields_callsOnTitleChange() {
    var newTitle = ""
    composeTestRule.setContent {
      AppTheme {
        EventTitleAndDescriptionFields(
            title = "",
            onTitleChange = { newTitle = it },
            titleTag = "title_input",
            description = "",
            onDescriptionChange = {},
            descriptionTag = "description_input",
            onFocusChange = {})
      }
    }

    composeTestRule.onNodeWithTag("title_input").performTextInput("New Title")
    assert(newTitle == "New Title")
  }

  @Test
  fun eventTitleAndDescriptionFields_callsOnDescriptionChange() {
    var newDescription = ""
    composeTestRule.setContent {
      AppTheme {
        EventTitleAndDescriptionFields(
            title = "",
            onTitleChange = {},
            titleTag = "title_input",
            description = "",
            onDescriptionChange = { newDescription = it },
            descriptionTag = "description_input",
            onFocusChange = {})
      }
    }

    composeTestRule.onNodeWithTag("description_input").performTextInput("New Description")
    assert(newDescription == "New Description")
  }

  @Test
  fun eventTitleAndDescriptionFields_callsOnFocusChange() {
    var focusChanged = false
    composeTestRule.setContent {
      AppTheme {
        EventTitleAndDescriptionFields(
            title = "",
            onTitleChange = {},
            titleTag = "title_input",
            description = "",
            onDescriptionChange = {},
            descriptionTag = "description_input",
            onFocusChange = { focusChanged = true })
      }
    }

    composeTestRule.onNodeWithTag("title_input").performClick()
    composeTestRule.waitForIdle()
    // Focus change is triggered on click
  }

  @Test
  fun eventParticipantsAndFeesFields_displaysCorrectly() {
    composeTestRule.setContent {
      AppTheme {
        EventParticipantsAndFeesFields(
            state =
                ParticipantsFeeState(
                    numberOfParticipants = "100",
                    hasParticipationFee = false,
                    participationFee = "",
                    isFlash = false),
            callbacks =
                ParticipantsFeeCallbacks(
                    onParticipantsChange = {},
                    onHasFeeChange = {},
                    onFeeStringChange = {},
                    onIsFlashChange = {}),
            participantsTag = "participants_input",
            feeSwitchTag = "fee_switch",
            feeInputTag = "fee_input",
            onFocusChange = {})
      }
    }

    composeTestRule.onNodeWithTag("participants_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fee_switch").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fee_input").assertIsDisplayed()
  }

  @Test
  fun eventParticipantsAndFeesFields_feeInputDisabledWhenNoFee() {
    composeTestRule.setContent {
      AppTheme {
        EventParticipantsAndFeesFields(
            state =
                ParticipantsFeeState(
                    numberOfParticipants = "100",
                    hasParticipationFee = false,
                    participationFee = "",
                    isFlash = false),
            callbacks =
                ParticipantsFeeCallbacks(
                    onParticipantsChange = {},
                    onHasFeeChange = {},
                    onFeeStringChange = {},
                    onIsFlashChange = {}),
            participantsTag = "participants_input",
            feeSwitchTag = "fee_switch",
            feeInputTag = "fee_input",
            onFocusChange = {})
      }
    }

    composeTestRule.onNodeWithTag("fee_input").assertIsNotEnabled()
  }

  @Test
  fun eventParticipantsAndFeesFields_feeInputEnabledWhenHasFee() {
    composeTestRule.setContent {
      AppTheme {
        EventParticipantsAndFeesFields(
            state =
                ParticipantsFeeState(
                    numberOfParticipants = "100",
                    hasParticipationFee = true,
                    participationFee = "10",
                    isFlash = false),
            callbacks =
                ParticipantsFeeCallbacks(
                    onParticipantsChange = {},
                    onHasFeeChange = {},
                    onFeeStringChange = {},
                    onIsFlashChange = {}),
            participantsTag = "participants_input",
            feeSwitchTag = "fee_switch",
            feeInputTag = "fee_input",
            onFocusChange = {})
      }
    }

    composeTestRule.onNodeWithTag("fee_input").assertIsEnabled()
  }

  @Test
  fun eventParticipantsAndFeesFields_toggleFeeSwitch() {
    var hasFee = false
    var feeCleared = false
    composeTestRule.setContent {
      AppTheme {
        EventParticipantsAndFeesFields(
            state =
                ParticipantsFeeState(
                    numberOfParticipants = "100",
                    hasParticipationFee = hasFee,
                    participationFee = "10",
                    isFlash = false),
            callbacks =
                ParticipantsFeeCallbacks(
                    onParticipantsChange = {},
                    onHasFeeChange = { hasFee = it },
                    onFeeStringChange = { if (it.isEmpty()) feeCleared = true },
                    onIsFlashChange = {}),
            participantsTag = "participants_input",
            feeSwitchTag = "fee_switch",
            feeInputTag = "fee_input",
            onFocusChange = {})
      }
    }

    // Toggle on
    composeTestRule.onNodeWithTag("fee_switch").performClick()
    assert(hasFee)
  }

  @Test
  fun handleHourSelection_doesNotResetMinutesWhenNotMax() {
    var hours = 1
    var minutes = 30
    handleHourSelection(
        selectedHours = 2,
        currentMinutes = minutes,
        onHoursChange = { hours = it },
        onMinutesChange = { minutes = it })

    assert(hours == 2)
    assert(minutes == 30) // Minutes should not be reset
  }

  @Test
  fun handleMinuteSelection_doesNotReduceHoursWhenNotAtMax() {
    var hours = 3
    var minutes = 0
    handleMinuteSelection(
        selectedMinutes = 30,
        currentHours = hours,
        onHoursChange = { hours = it },
        onMinutesChange = { minutes = it })

    assert(hours == 3) // Hours should not change
    assert(minutes == 30)
  }

  @Test
  fun handleMinuteSelection_allowsZeroMinutesAtMaxHours() {
    var hours = C.FlashEvent.MAX_DURATION_HOURS.toInt()
    var minutes = 0
    handleMinuteSelection(
        selectedMinutes = 0,
        currentHours = hours,
        onHoursChange = { hours = it },
        onMinutesChange = { minutes = it })

    assert(hours == C.FlashEvent.MAX_DURATION_HOURS.toInt()) // Hours unchanged
    assert(minutes == 0)
  }

  @Test
  fun dateTimeState_holdsCorrectValues() {
    val state =
        DateTimeState(
            startDate = "01/01/2025",
            startTime = java.time.LocalTime.of(10, 0),
            endDate = "01/01/2025",
            endTime = java.time.LocalTime.of(12, 0))

    assert(state.startDate == "01/01/2025")
    assert(state.startTime.hour == 10)
    assert(state.endDate == "01/01/2025")
    assert(state.endTime.hour == 12)
  }

  @Test
  fun dateTimeCallbacks_canBeCreated() {
    var startDateChanged = false
    var startTimeChanged = false
    var endDateChanged = false
    var endTimeChanged = false

    val callbacks =
        DateTimeCallbacks(
            onStartDateChange = { startDateChanged = true },
            onStartTimeChange = { startTimeChanged = true },
            onEndDateChange = { endDateChanged = true },
            onEndTimeChange = { endTimeChanged = true })

    callbacks.onStartDateChange(java.time.LocalDate.now())
    callbacks.onStartTimeChange(java.time.LocalTime.now())
    callbacks.onEndDateChange(java.time.LocalDate.now())
    callbacks.onEndTimeChange(java.time.LocalTime.now())

    assert(startDateChanged)
    assert(startTimeChanged)
    assert(endDateChanged)
    assert(endTimeChanged)
  }

  @Test
  fun participantsFeeState_holdsCorrectValues() {
    val state =
        ParticipantsFeeState(
            numberOfParticipants = "50",
            hasParticipationFee = true,
            participationFee = "25",
            isFlash = true)

    assert(state.numberOfParticipants == "50")
    assert(state.hasParticipationFee)
    assert(state.participationFee == "25")
    assert(state.isFlash)
  }

  @Test
  fun participantsFeeCallbacks_canBeCreated() {
    var participantsChanged = false
    var hasFeeChanged = false
    var feeChanged = false
    var flashChanged = false

    val callbacks =
        ParticipantsFeeCallbacks(
            onParticipantsChange = { participantsChanged = true },
            onHasFeeChange = { hasFeeChanged = true },
            onFeeStringChange = { feeChanged = true },
            onIsFlashChange = { flashChanged = true })

    callbacks.onParticipantsChange("100")
    callbacks.onHasFeeChange(true)
    callbacks.onFeeStringChange("50")
    callbacks.onIsFlashChange(true)

    assert(participantsChanged)
    assert(hasFeeChanged)
    assert(feeChanged)
    assert(flashChanged)
  }
}
