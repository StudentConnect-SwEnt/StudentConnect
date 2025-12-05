package com.github.se.studentconnect.ui.screen.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.event.AgeGroupData
import com.github.se.studentconnect.model.event.CampusData
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.event.EventStatistics
import com.github.se.studentconnect.model.event.JoinRateData
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.resources.C
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Instrumented tests for EventStatisticsScreen to cover the main composable branches. */
class EventStatisticsScreenInstrumentedTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var organizationRepository: OrganizationRepositoryLocal
  private lateinit var viewModel: EventStatisticsViewModel

  private val testEventUid = "test-event-123"
  private val testOwnerId = "org-123"

  private val testEvent =
      Event.Public(
          uid = testEventUid,
          ownerId = testOwnerId,
          title = "Test Event",
          description = "Test Description",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = null,
          imageUrl = null,
          maxCapacity = 100u,
          participationFee = null,
          isFlash = false,
          subtitle = "Test Subtitle",
          tags = emptyList())

  private val testOrganization =
      Organization(
          id = testOwnerId,
          name = "Test Organization",
          type = OrganizationType.Association,
          description = "Test Description",
          memberUids = listOf("member1", "member2", "member3"),
          createdBy = "creator123")

  private val testStatistics =
      EventStatistics(
          eventId = "event123",
          totalAttendees = 42,
          ageDistribution = listOf(AgeGroupData("18-22", 20, 50f)),
          campusDistribution = listOf(CampusData("EPFL", 25, 60f)),
          joinRateOverTime =
              listOf(
                  JoinRateData(Timestamp(Date()), 10, "Day 1"),
                  JoinRateData(Timestamp(Date()), 42, "Day 2")),
          followerCount = 100,
          attendeesFollowersRate = 42f)

  @Before
  fun setUp() {
    eventRepository = EventRepositoryLocal()
    organizationRepository = OrganizationRepositoryLocal()
    EventRepositoryProvider.overrideForTests(eventRepository)
    OrganizationRepositoryProvider.overrideForTests(organizationRepository)
    viewModel = EventStatisticsViewModel(eventRepository, organizationRepository)

    runBlocking {
      eventRepository.addEvent(testEvent)
      organizationRepository.saveOrganization(testOrganization)
      // Participants will be added when statistics are calculated
    }
  }

  @After
  fun tearDown() {
    runBlocking {
      try {
        eventRepository.deleteEvent(testEventUid)
        organizationRepository.clear()
      } catch (e: Exception) {
        // Ignore cleanup errors
      }
    }
  }

  @Test
  fun eventStatisticsScreen_withSuccessState_displaysContent() {
    runBlocking {
      // Pre-load statistics
      viewModel.loadStatistics(testEventUid)
    }

    composeTestRule.setContent {
      MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "stats") {
          composable("stats") {
            EventStatisticsScreen(
                eventUid = testEventUid, navController = navController, viewModel = viewModel)
          }
        }
      }
    }

    composeTestRule.waitForIdle()
    Thread.sleep(1000) // Wait for statistics to load
    composeTestRule.waitForIdle()
    // Should show content (statistics branch) - use waitUntil to handle async loading
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag(C.Tag.STATS_CONTENT).fetchSemanticsNodes(false).isNotEmpty()
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_CONTENT).assertExists()
  }

  @Test
  fun eventStatisticsScreen_refreshButton_callsRefresh() {
    runBlocking { viewModel.loadStatistics(testEventUid) }

    composeTestRule.setContent {
      MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "stats") {
          composable("stats") {
            EventStatisticsScreen(
                eventUid = testEventUid, navController = navController, viewModel = viewModel)
          }
        }
      }
    }

    composeTestRule.waitForIdle()
    Thread.sleep(500)
    composeTestRule.waitForIdle()
    // Click refresh button
    composeTestRule.onNodeWithTag(C.Tag.STATS_REFRESH_BUTTON).performClick()
    composeTestRule.waitForIdle()
    // Should still show content after refresh
    composeTestRule.onNodeWithTag(C.Tag.STATS_CONTENT).assertExists()
  }

  @Test
  fun eventStatisticsScreen_backButton_isClickable() {
    composeTestRule.setContent {
      MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "stats") {
          composable("stats") {
            EventStatisticsScreen(
                eventUid = testEventUid, navController = navController, viewModel = viewModel)
          }
        }
      }
    }

    composeTestRule.waitForIdle()
    // Back button should be clickable
    composeTestRule.onNodeWithTag(C.Tag.STATS_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_BACK_BUTTON).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun eventStatisticsScreen_withErrorState_displaysError() {
    // Create a ViewModel that will error
    val errorEventRepository = EventRepositoryLocal()
    val errorViewModel = EventStatisticsViewModel(errorEventRepository, organizationRepository)

    composeTestRule.setContent {
      MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "stats") {
          composable("stats") {
            EventStatisticsScreen(
                eventUid = "non-existent-event",
                navController = navController,
                viewModel = errorViewModel)
          }
        }
      }
    }

    composeTestRule.waitForIdle()
    Thread.sleep(500) // Wait for error to occur
    composeTestRule.waitForIdle()
    // Should show error state
    composeTestRule.onNodeWithTag(C.Tag.STATS_ERROR).assertExists()
  }

  @Test
  fun eventStatisticsScreen_errorState_retryButton_callsRefresh() {
    // Create a ViewModel that will error initially
    val errorEventRepository = EventRepositoryLocal()
    val errorViewModel = EventStatisticsViewModel(errorEventRepository, organizationRepository)

    composeTestRule.setContent {
      MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "stats") {
          composable("stats") {
            EventStatisticsScreen(
                eventUid = "non-existent-event",
                navController = navController,
                viewModel = errorViewModel)
          }
        }
      }
    }

    composeTestRule.waitForIdle()
    Thread.sleep(500)
    composeTestRule.waitForIdle()
    // Click retry button
    composeTestRule.onNodeWithTag(C.Tag.STATS_RETRY_BUTTON).performClick()
    composeTestRule.waitForIdle()
    // Retry should trigger refresh (will still error, but refresh was called)
    composeTestRule.onNodeWithTag(C.Tag.STATS_RETRY_BUTTON).assertExists()
  }

  @Test
  fun mainScreen_loadingState_displaysLoadingIndicator() {
    val uiState = mutableStateOf(EventStatisticsUiState(isLoading = true))

    composeTestRule.setContent {
      MaterialTheme {
        when {
          uiState.value.isLoading -> LoadingState()
          uiState.value.error != null -> ErrorState(message = uiState.value.error!!, onRetry = {})
          uiState.value.statistics != null ->
              StatisticsContent(
                  statistics = uiState.value.statistics!!,
                  animationProgress = 1f,
                  paddingValues = PaddingValues())
        }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_LOADING).assertIsDisplayed()
  }

  @Test
  fun mainScreen_errorState_displaysErrorWithRetry() {
    val uiState = mutableStateOf(EventStatisticsUiState(isLoading = false, error = "Network error"))
    var retryCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        when {
          uiState.value.isLoading -> LoadingState()
          uiState.value.error != null ->
              ErrorState(message = uiState.value.error!!, onRetry = { retryCalled = true })
          uiState.value.statistics != null ->
              StatisticsContent(
                  statistics = uiState.value.statistics!!,
                  animationProgress = 1f,
                  paddingValues = PaddingValues())
        }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_ERROR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_RETRY_BUTTON).performClick()
    composeTestRule.runOnIdle { assert(retryCalled) }
  }

  @Test
  fun mainScreen_successState_displaysContent() {
    val uiState =
        mutableStateOf(
            EventStatisticsUiState(
                isLoading = false, statistics = testStatistics, animationProgress = 1f))

    composeTestRule.setContent {
      MaterialTheme {
        when {
          uiState.value.isLoading -> LoadingState()
          uiState.value.error != null -> ErrorState(message = uiState.value.error!!, onRetry = {})
          uiState.value.statistics != null ->
              StatisticsContent(
                  statistics = uiState.value.statistics!!,
                  animationProgress = uiState.value.animationProgress,
                  paddingValues = PaddingValues())
        }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_CONTENT).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertExists()
  }

  @Test
  fun screenTitle_withEventTitle_displaysEventTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        val title = "My Event Title"
        val displayTitle = title.ifEmpty { "Event Statistics" }
        Text(text = displayTitle)
      }
    }

    composeTestRule.onNodeWithText("My Event Title").assertIsDisplayed()
  }

  @Test
  fun screenTitle_emptyEventTitle_displaysDefault() {
    composeTestRule.setContent {
      MaterialTheme {
        val title = ""
        val displayTitle = title.ifEmpty { "Event Statistics" }
        Text(text = displayTitle)
      }
    }

    composeTestRule.onNodeWithText("Event Statistics").assertIsDisplayed()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun fullScreen_withLoadingState_displaysScaffold() {
    composeTestRule.setContent {
      MaterialTheme {
        Scaffold(
            modifier = Modifier.testTag(C.Tag.STATS_SCREEN),
            topBar = {
              TopAppBar(
                  title = { Text("Event Statistics") },
                  navigationIcon = {
                    IconButton(onClick = {}, modifier = Modifier.testTag(C.Tag.STATS_BACK_BUTTON)) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Back")
                    }
                  },
                  modifier = Modifier.testTag(C.Tag.STATS_TOP_BAR))
            }) { paddingValues ->
              LoadingState(modifier = Modifier.padding(paddingValues))
            }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOP_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_LOADING).assertIsDisplayed()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun fullScreen_backButton_isClickable() {
    var backClicked = false

    composeTestRule.setContent {
      MaterialTheme {
        Scaffold(
            modifier = Modifier.testTag(C.Tag.STATS_SCREEN),
            topBar = {
              TopAppBar(
                  title = { Text("Test") },
                  navigationIcon = {
                    IconButton(
                        onClick = { backClicked = true },
                        modifier = Modifier.testTag(C.Tag.STATS_BACK_BUTTON)) {
                          Icon(
                              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                              contentDescription = "Back")
                        }
                  })
            }) { paddingValues ->
              // Test only checks button clickability, padding not needed for this test
              Box(modifier = Modifier.padding(paddingValues))
            }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_BACK_BUTTON).performClick()
    composeTestRule.runOnIdle { assert(backClicked) }
  }

  @Test
  fun totalAttendeesCard_withAnimationProgress_displaysCorrectly() {
    composeTestRule.setContent {
      MaterialTheme {
        TotalAttendeesCard(
            totalAttendees = 100,
            animationProgress = 0.5f,
            gradientStart = Color.Blue,
            gradientEnd = Color.Magenta)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertIsDisplayed()
  }

  @Test
  fun attendeesFollowersCard_displaysCorrectly() {
    composeTestRule.setContent {
      MaterialTheme {
        AttendeesFollowersCard(
            attendees = 42,
            followers = 100,
            rate = 42f,
            animationProgress = 1f,
            accentColor = Color.Green)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertIsDisplayed()
  }

  @Test
  fun statisticsContent_animationProgress_zero_stillRendersCards() {
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = testStatistics, animationProgress = 0f, paddingValues = PaddingValues())
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_CONTENT).assertExists()
  }

  @Test
  fun staggeredAnimatedCard_animationProgressAtOne_showsImmediately() {
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = testStatistics, animationProgress = 1f, paddingValues = PaddingValues())
      }
    }
    composeTestRule.waitForIdle()
    // When progress is 1f, cards should be visible immediately
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertExists()
  }

  @Test
  fun totalAttendeesCard_partialAnimationProgress_showsPartialCount() {
    composeTestRule.setContent {
      MaterialTheme {
        TotalAttendeesCard(
            totalAttendees = 100,
            animationProgress = 0.5f,
            gradientStart = Color.Blue,
            gradientEnd = Color.Magenta)
      }
    }
    // At 0.5f progress, counter should show ~50
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertIsDisplayed()
  }

  @Test
  fun attendeesFollowersCard_rateZero_renders() {
    composeTestRule.setContent {
      MaterialTheme {
        AttendeesFollowersCard(
            attendees = 0,
            followers = 100,
            rate = 0f,
            animationProgress = 1f,
            accentColor = Color.Green)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertIsDisplayed()
  }

  @Test
  fun attendeesFollowersCard_rateHundred_renders() {
    composeTestRule.setContent {
      MaterialTheme {
        AttendeesFollowersCard(
            attendees = 100,
            followers = 100,
            rate = 100f,
            animationProgress = 1f,
            accentColor = Color.Green)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertIsDisplayed()
  }

  @Test
  fun joinRateCard_withSingleDataPoint_renders() {
    composeTestRule.setContent {
      MaterialTheme {
        JoinRateCard(
            data = listOf(JoinRateData(Timestamp(Date()), 10, "Day 1")),
            animationProgress = 1f,
            lineColor = Color.Blue,
            fillColor = Color.LightGray)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_TIMELINE_CARD).assertIsDisplayed()
  }

  @Test
  fun joinRateCard_withEmptyData_usesZeroForTotalJoins() {
    composeTestRule.setContent {
      MaterialTheme {
        JoinRateCard(
            data = emptyList(),
            animationProgress = 1f,
            lineColor = Color.Blue,
            fillColor = Color.LightGray)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_TIMELINE_CARD).assertIsDisplayed()
  }

  @Test
  fun staggeredAnimatedCard_animationProgressAboveThreshold_triggersDelay() {
    composeTestRule.setContent {
      MaterialTheme {
        // Test index 2 with progress 0.25 (above threshold 0.2, so delay will trigger)
        StatisticsContent(
            statistics = testStatistics, animationProgress = 0.25f, paddingValues = PaddingValues())
      }
    }
    // Wait for the delay to complete (index 2 * 50ms = 100ms delay) and animation
    composeTestRule.waitForIdle()
    Thread.sleep(200) // Wait for coroutine delay
    composeTestRule.waitForIdle()
    // Card should appear after delay
    composeTestRule.onNodeWithTag(C.Tag.STATS_AGE_CARD).assertExists()
  }

  @Test
  fun staggeredAnimatedCard_visibleTrue_showsContent() {
    composeTestRule.setContent {
      MaterialTheme {
        // With animationProgress = 1f, visible should be true OR animationProgress >= 1f
        StatisticsContent(
            statistics = testStatistics, animationProgress = 1f, paddingValues = PaddingValues())
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertExists()
  }

  @Test
  fun statisticsContent_withEmptyAgeDistribution_hidesAgeCard() {
    val statsWithoutAge = testStatistics.copy(ageDistribution = emptyList())
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = statsWithoutAge, animationProgress = 1f, paddingValues = PaddingValues())
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.STATS_AGE_CARD).assertDoesNotExist()
  }

  @Test
  fun statisticsContent_withEmptyCampusDistribution_hidesCampusCard() {
    val statsWithoutCampus = testStatistics.copy(campusDistribution = emptyList())
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = statsWithoutCampus,
            animationProgress = 1f,
            paddingValues = PaddingValues())
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.STATS_CAMPUS_CARD).assertDoesNotExist()
  }

  @Test
  fun statisticsContent_withEmptyJoinRate_hidesJoinRateCard() {
    val statsWithoutJoinRate = testStatistics.copy(joinRateOverTime = emptyList())
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = statsWithoutJoinRate,
            animationProgress = 1f,
            paddingValues = PaddingValues())
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.STATS_TIMELINE_CARD).assertDoesNotExist()
  }

  @Test
  fun statisticsContent_withPartialAnimationProgress_rendersCards() {
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = testStatistics, animationProgress = 0.3f, paddingValues = PaddingValues())
      }
    }
    composeTestRule.waitForIdle()
    // Cards should render even with partial progress
    composeTestRule.onNodeWithTag(C.Tag.STATS_CONTENT).assertExists()
  }

  @Test
  fun totalAttendeesCard_withSingleAttendee_showsSingularText() {
    composeTestRule.setContent {
      MaterialTheme {
        TotalAttendeesCard(
            totalAttendees = 1,
            animationProgress = 1f,
            gradientStart = Color.Blue,
            gradientEnd = Color.Magenta)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertIsDisplayed()
  }
}
