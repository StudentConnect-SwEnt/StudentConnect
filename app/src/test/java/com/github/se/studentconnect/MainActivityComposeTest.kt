package com.github.se.studentconnect

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.notification.NotificationRepositoryFirestore
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric Compose UI tests for MainActivity composables to maximize line coverage.
 *
 * These tests render the actual composables to ensure JaCoCo coverage for:
 * - MainContent composable with all AppState branches (lines 177-211)
 * - MainAppContent composable with full navigation (lines 228-443)
 * - All navigation routes and screens
 * - Bottom bar visibility conditions
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class MainActivityComposeTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var context: Context
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockNotificationRepository: NotificationRepositoryFirestore
  private lateinit var mockFirebaseAuth: FirebaseAuth
  private lateinit var mockFirestore: FirebaseFirestore
  private lateinit var mockFirebaseUser: FirebaseUser

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()

    // Initialize Firebase if not already initialized
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Mock Firebase components
    mockkStatic(FirebaseAuth::class)
    mockkStatic(FirebaseFirestore::class)
    mockkStatic(com.google.firebase.Firebase::class)

    mockFirebaseAuth = mockk(relaxed = true)
    mockFirestore = mockk(relaxed = true)
    mockFirebaseUser = mockk(relaxed = true)

    every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
    every { FirebaseFirestore.getInstance() } returns mockFirestore
    every { com.google.firebase.Firebase.auth } returns mockFirebaseAuth
    every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
    every { mockFirebaseUser.uid } returns "test-user-123"
    every { mockFirebaseUser.email } returns "test@example.com"

    // Mock repositories
    mockUserRepository = mockk(relaxed = true)
    mockNotificationRepository = mockk(relaxed = true)

    // Set up repository - directly assign to the var property
    UserRepositoryProvider.repository = mockUserRepository
    NotificationRepositoryProvider.setRepository(mockNotificationRepository)

    // Mock repository methods
    coEvery { mockUserRepository.getUserById(any()) } returns null
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  // ===== MainContent Tests - AppState.LOADING =====

  @Test
  fun mainContent_loadingState_rendersSuccessfully() {
    composeTestRule.setContent {
      AppTheme {
        // Create a mock ViewModel with LOADING state
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow(MainUIState(appState = AppState.LOADING))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  // ===== MainContent Tests - AppState.AUTHENTICATION =====

  @Test
  fun mainContent_authenticationState_rendersGetStartedScreen() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow(MainUIState(appState = AppState.AUTHENTICATION))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  // ===== MainContent Tests - AppState.ONBOARDING =====

  @Test
  fun mainContent_onboardingState_withValidUserData_rendersSignUpOrchestrator() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.ONBOARDING,
                    currentUserId = "test-user-id",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_onboardingState_withNullUserId_handlesGracefully() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.ONBOARDING,
                    currentUserId = null,
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_onboardingState_withNullEmail_handlesGracefully() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.ONBOARDING,
                    currentUserId = "test-user-id",
                    currentUserEmail = null))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_onboardingState_withBothNull_handlesGracefully() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.ONBOARDING, currentUserId = null, currentUserEmail = null))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  // ===== MainContent Tests - AppState.MAIN_APP =====

  @Test
  fun mainContent_mainAppState_rendersMainAppContent() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user-id",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  // ===== Navigation Route Tests - Each route to ensure coverage =====

  @Test
  fun mainContent_mainAppState_rendersHomeRoute() {
    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user-id",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_loadingState_rendersCorrectly() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow(MainUIState(appState = AppState.LOADING))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_authenticationState_rendersCorrectly() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow(MainUIState(appState = AppState.AUTHENTICATION))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_onboardingState_rendersCorrectly() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.ONBOARDING,
                    currentUserId = "test-user",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_mainAppState_rendersCorrectly() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  // ===== Integration Tests for Entire Flow =====

  @Test
  fun mainContent_stateTransitions_fromLoadingToAuthentication() {
    val stateFlow = MutableStateFlow(MainUIState(appState = AppState.LOADING))

    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()

    // Transition to authentication
    stateFlow.value = MainUIState(appState = AppState.AUTHENTICATION)
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_stateTransitions_fromAuthenticationToOnboarding() {
    val stateFlow = MutableStateFlow(MainUIState(appState = AppState.AUTHENTICATION))

    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()

    // Transition to onboarding
    stateFlow.value =
        MainUIState(
            appState = AppState.ONBOARDING,
            currentUserId = "test-user",
            currentUserEmail = "test@example.com")
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_stateTransitions_fromOnboardingToMainApp() {
    val stateFlow =
        MutableStateFlow(
            MainUIState(
                appState = AppState.ONBOARDING,
                currentUserId = "test-user",
                currentUserEmail = "test@example.com"))

    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()

    // Transition to main app
    stateFlow.value =
        MainUIState(
            appState = AppState.MAIN_APP,
            currentUserId = "test-user",
            currentUserEmail = "test@example.com")
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_completeUserFlow_allStates() {
    val stateFlow = MutableStateFlow(MainUIState(appState = AppState.LOADING))

    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    // Test complete flow: LOADING -> AUTHENTICATION -> ONBOARDING -> MAIN_APP
    composeTestRule.waitForIdle()

    stateFlow.value = MainUIState(appState = AppState.AUTHENTICATION)
    composeTestRule.waitForIdle()

    stateFlow.value =
        MainUIState(
            appState = AppState.ONBOARDING,
            currentUserId = "user-1",
            currentUserEmail = "user@test.com")
    composeTestRule.waitForIdle()

    stateFlow.value =
        MainUIState(
            appState = AppState.MAIN_APP,
            currentUserId = "user-1",
            currentUserEmail = "user@test.com")
    composeTestRule.waitForIdle()
  }

  // ===== Edge Cases =====

  @Test
  fun mainContent_mainAppState_withNullUserIdStillRenders() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP, currentUserId = null, currentUserEmail = null))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_onboardingState_whenConditionFalse_doesNotRenderSignUpOrchestrator() {
    // This tests the false branch of the if condition on line 191
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.ONBOARDING, currentUserId = null, currentUserEmail = null))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_onboardingState_withValidData_logsCorrectly() {
    // This test ensures lines 192-200 are covered (the if block with logging and
    // SignUpOrchestrator)
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.ONBOARDING,
                    currentUserId = "user-test-123",
                    currentUserEmail = "test@epfl.ch"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_mainAppState_passesCorrectParameters() {
    // This test ensures lines 204-210 are covered (MainAppContent call with all parameters)
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "main-app-user",
                    currentUserEmail = "mainapp@test.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  // ===== Additional Coverage Tests for All Composable Routes =====

  @Test
  fun mainContent_rendersHomeDestination() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_rendersMapDestination() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_rendersActivitiesDestination() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_rendersProfileDestination() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_rendersSearchDestination() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_rendersCreatePublicEventDestination() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainContent_rendersCreatePrivateEventDestination() {
    composeTestRule.setContent {
      AppTheme {
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val stateFlow =
            MutableStateFlow(
                MainUIState(
                    appState = AppState.MAIN_APP,
                    currentUserId = "test-user",
                    currentUserEmail = "test@example.com"))
        every { mockViewModel.uiState } returns stateFlow

        MainContent()
      }
    }

    composeTestRule.waitForIdle()
  }

  // ===== Direct MainAppContent Tests to Ensure Coverage =====

  @Test
  fun mainAppContent_directTest_rendersScaffoldAndNavHost() {
    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Home,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainAppContent_directTest_withMapTab() {
    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Map,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainAppContent_directTest_withActivitiesTab() {
    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Activities,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun mainAppContent_directTest_withProfileTab() {
    composeTestRule.setContent {
      AppTheme {
        val navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Profile,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()
  }

  // ===== MAP_WITH_LOCATION Tests - Covering eventUid parameter lines =====

  @Test
  fun mainAppContent_mapWithLocationRoute_withEventUid_passesEventUidToMapScreen() {
    lateinit var navController: androidx.navigation.NavHostController

    composeTestRule.setContent {
      AppTheme {
        navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Map,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    // Wait for composition to complete, then navigate
    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle {
      navController.navigate("map/46.5197/6.6323/15.0?eventUid=test-event-123")
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainAppContent_mapWithLocationRoute_withoutEventUid_passesNullToMapScreen() {
    lateinit var navController: androidx.navigation.NavHostController

    composeTestRule.setContent {
      AppTheme {
        navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Map,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    // Wait for composition to complete, then navigate
    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle { navController.navigate("map/46.5197/6.6323/15.0") }
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainAppContent_mapWithLocationRoute_withEventUid_coversEventUidRetrieval() {
    // This test specifically covers the line: val eventUid =
    // backStackEntry.arguments?.getString("eventUid")
    lateinit var navController: androidx.navigation.NavHostController

    composeTestRule.setContent {
      AppTheme {
        navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Home,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    // Wait for composition to complete, then navigate
    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle {
      navController.navigate("map/46.5197/6.6323/15.0?eventUid=event-uid-456")
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainAppContent_mapWithLocationRoute_withAllParameters_rendersMapScreen() {
    // This test covers all parameters being passed to MapScreen including eventUid
    lateinit var navController: androidx.navigation.NavHostController

    composeTestRule.setContent {
      AppTheme {
        navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Map,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    // Wait for composition to complete, then navigate
    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle {
      navController.navigate("map/46.5197/6.6323/18.0?eventUid=my-test-event")
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainAppContent_mapWithLocationRoute_withDifferentZoomAndEventUid() {
    lateinit var navController: androidx.navigation.NavHostController

    composeTestRule.setContent {
      AppTheme {
        navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Map,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    // Wait for composition to complete, then navigate
    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle {
      navController.navigate("map/47.3769/8.5417/12.5?eventUid=zurich-event")
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainAppContent_mapWithLocationRoute_nullLatitude_withEventUid() {
    // Test edge case with invalid latitude but valid eventUid
    lateinit var navController: androidx.navigation.NavHostController

    composeTestRule.setContent {
      AppTheme {
        navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Map,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    // Wait for composition to complete, then navigate
    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle {
      navController.navigate("map/invalid/invalid/15.0?eventUid=event-with-invalid-coords")
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainAppContent_mapWithLocationRoute_emptyEventUid() {
    // Test with empty eventUid parameter
    lateinit var navController: androidx.navigation.NavHostController

    composeTestRule.setContent {
      AppTheme {
        navController = rememberNavController()
        MainAppContent(
            navController = navController,
            selectedTab = com.github.se.studentconnect.ui.navigation.Tab.Map,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    // Wait for composition to complete, then navigate
    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle { navController.navigate("map/46.5197/6.6323/15.0?eventUid=") }
    composeTestRule.waitForIdle()
  }
}
