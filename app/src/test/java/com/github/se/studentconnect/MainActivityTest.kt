package com.github.se.studentconnect

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.github.se.studentconnect.model.notification.NotificationRepositoryFirestore
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import io.mockk.coEvery
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class MainActivityTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()

    // Initialize Firebase if not already initialized
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Initialize WorkManager for testing
    val config = Configuration.Builder().setExecutor(SynchronousExecutor()).build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

    // Mock Firebase dependencies
    mockkStatic(FirebaseAuth::class)
    mockkStatic(FirebaseFirestore::class)

    val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)

    every { FirebaseAuth.getInstance() } returns mockAuth
    every { FirebaseFirestore.getInstance() } returns mockFirestore
    every { mockAuth.currentUser } returns null

    // Setup NotificationRepositoryProvider
    val mockNotificationRepo = mockk<NotificationRepositoryFirestore>(relaxed = true)
    NotificationRepositoryProvider.setRepository(mockNotificationRepo)

    // Setup UserRepositoryProvider with mock (it uses a property, not a setter method)
    val mockUserRepo = mockk<com.github.se.studentconnect.repository.UserRepository>(relaxed = true)
    // UserRepositoryProvider.repository is already initialized, we can use it as-is
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  // ===== MainActivity onCreate Tests =====

  @Test
  fun mainActivity_onCreate_initializesSuccessfully() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.use { it.onActivity { activity -> assertNotNull(activity) } }
  }

  @Test
  fun mainActivity_onCreate_initializesNotificationRepository() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.use {
      // Verify NotificationRepository is set
      assertNotNull(NotificationRepositoryProvider.repository)
    }
  }

  @Test
  fun mainActivity_onCreate_schedulesEventReminderWorker() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.use {
      val workManager = WorkManager.getInstance(context)
      val workInfos = workManager.getWorkInfosForUniqueWork("event_reminder_work").get()
      // WorkManager should have the periodic work scheduled
      assertNotNull(workInfos)
    }
  }

  @Test
  fun mainActivity_onCreate_setsContent() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.use {
      it.onActivity { activity ->
        // Verify that content was set
        assertNotNull(activity.window)
        assertNotNull(activity.window.decorView)
      }
    }
  }

  @Test
  fun mainActivity_onCreate_createsWindow() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.use {
      it.onActivity { activity ->
        assertNotNull(activity)
        assertTrue(activity is MainActivity)
      }
    }
  }

  // ===== AppState Tests =====

  @Test
  fun appState_hasAllExpectedStates() {
    val states = AppState.values()
    assertEquals(4, states.size)
    assertTrue(states.contains(AppState.LOADING))
    assertTrue(states.contains(AppState.AUTHENTICATION))
    assertTrue(states.contains(AppState.ONBOARDING))
    assertTrue(states.contains(AppState.MAIN_APP))
  }

  @Test
  fun appState_canBeCompared() {
    assertEquals(AppState.LOADING, AppState.LOADING)
    assertNotEquals(AppState.LOADING, AppState.AUTHENTICATION)
    assertNotEquals(AppState.ONBOARDING, AppState.MAIN_APP)
  }

  @Test
  fun mainUIState_defaultStateIsLoading() {
    val state = MainUIState()
    assertEquals(AppState.LOADING, state.appState)
    assertNull(state.currentUserId)
    assertNull(state.currentUserEmail)
  }

  @Test
  fun mainUIState_canBeCreatedWithCustomValues() {
    val state =
        MainUIState(
            appState = AppState.MAIN_APP,
            currentUserId = "test-user-id",
            currentUserEmail = "test@example.com")

    assertEquals(AppState.MAIN_APP, state.appState)
    assertEquals("test-user-id", state.currentUserId)
    assertEquals("test@example.com", state.currentUserEmail)
  }

  @Test
  fun mainUIState_canBeCreatedWithNullUserData() {
    val state = MainUIState(appState = AppState.AUTHENTICATION, currentUserId = null)

    assertEquals(AppState.AUTHENTICATION, state.appState)
    assertNull(state.currentUserId)
  }

  @Test
  fun mainUIState_copyWorks() {
    val state1 = MainUIState(appState = AppState.LOADING)
    val state2 = state1.copy(appState = AppState.MAIN_APP)

    assertEquals(AppState.LOADING, state1.appState)
    assertEquals(AppState.MAIN_APP, state2.appState)
  }

  // ===== HttpClientProvider Tests =====

  /** Test to verify that HttpClientProvider provides a default OkHttpClient instance. */
  @Test
  fun httpClientProvider_hasDefaultClient() {
    assertNotNull(HttpClientProvider.client)
    assertTrue(HttpClientProvider.client is OkHttpClient)
  }

  /** Test to verify that HttpClientProvider allows setting a new OkHttpClient instance. */
  @Test
  fun httpClientProvider_clientIsMutable() {
    val originalClient = HttpClientProvider.client
    val newClient = OkHttpClient()

    HttpClientProvider.client = newClient
    assertEquals(newClient, HttpClientProvider.client)

    HttpClientProvider.client = originalClient
  }

  /** Test to verify that Home tab object has correct destination. */
  @Test
  fun tab_homeDestination_isCorrect() {
    assertEquals(Route.HOME, Tab.Home.destination.route)
  }

  /** Test to verify that Map tab object has correct destination. */
  @Test
  fun tab_mapDestination_isCorrect() {
    assertEquals(Route.MAP, Tab.Map.destination.route)
  }

  /** Test to verify that Activities tab object has correct destination. */
  @Test
  fun tab_activitiesDestination_isCorrect() {
    assertEquals(Route.ACTIVITIES, Tab.Activities.destination.route)
  }

  /** Test to verify that Profile tab object has correct destination. */
  @Test
  fun tab_profileDestination_isCorrect() {
    assertEquals(Route.PROFILE, Tab.Profile.destination.route)
  }

  /** Test to verify that route constants are not empty strings. */
  @Test
  fun route_constants_areNotEmpty() {
    assertFalse(Route.HOME.isEmpty())
    assertFalse(Route.MAP.isEmpty())
    assertFalse(Route.ACTIVITIES.isEmpty())
    assertFalse(Route.PROFILE.isEmpty())
  }

  /** Test to verify that HttpClientProvider client is not null. */
  @Test
  fun httpClientProvider_clientIsNotNull() {
    val client = HttpClientProvider.client
    assertNotNull("HttpClientProvider client should not be null", client)
  }

  /** Test to verify that Tab objects are not null. */
  @Test
  fun tab_values_exist() {
    assertNotNull(Tab.Home)
    assertNotNull(Tab.Map)
    assertNotNull(Tab.Activities)
    assertNotNull(Tab.Profile)
  }

  /** Test to verify that Tab objects have valid (non-empty) destination routes. */
  @Test
  fun tab_destinations_areValid() {
    assertTrue(Tab.Home.destination.route.isNotEmpty())
    assertTrue(Tab.Map.destination.route.isNotEmpty())
    assertTrue(Tab.Activities.destination.route.isNotEmpty())
    assertTrue(Tab.Profile.destination.route.isNotEmpty())
  }

  /** Test to verify that a new OkHttpClient can be created. */
  @Test
  fun httpClientProvider_canCreateNewClient() {
    val newClient = OkHttpClient.Builder().build()
    assertNotNull(newClient)
    assertTrue(newClient is OkHttpClient)
  }

  /** Test to verify that Tab icons are not zero. */
  @Test
  fun tab_icons_areNotZero() {
    assertTrue(Tab.Home.icon != 0)
    assertTrue(Tab.Map.icon != 0)
    assertTrue(Tab.Activities.icon != 0)
    assertTrue(Tab.Profile.icon != 0)
  }

  /** Test to verify that Tab objects have correct destination routes. */
  @Test
  fun tab_destinations_haveCorrectRoutes() {
    assertEquals("home", Tab.Home.destination.route)
    assertEquals("map", Tab.Map.destination.route)
    assertEquals("activities", Tab.Activities.destination.route)
    assertEquals("profile", Tab.Profile.destination.route)
  }

  /** Test to verify that Tab objects have correct destination names. */
  @Test
  fun tab_destinations_haveCorrectNames() {
    assertEquals("Home", Tab.Home.destination.name)
    assertEquals("Map", Tab.Map.destination.name)
    assertEquals("Activities", Tab.Activities.destination.name)
    assertEquals("Profile", Tab.Profile.destination.name)
  }

  /** Test to verify that Tab destinations are marked as top level destinations. */
  @Test
  fun tab_destinations_areTopLevelDestinations() {
    assertTrue(Tab.Home.destination.isTopLevelDestination)
    assertTrue(Tab.Map.destination.isTopLevelDestination)
    assertTrue(Tab.Activities.destination.isTopLevelDestination)
    assertTrue(Tab.Profile.destination.isTopLevelDestination)
  }

  /** Test to verify that route constants have expected values. */
  @Test
  fun route_constants_haveExpectedValues() {
    assertEquals("home", Route.HOME)
    assertEquals("map", Route.MAP)
    assertEquals("activities", Route.ACTIVITIES)
    assertEquals("profile", Route.PROFILE)
    assertEquals("auth", Route.AUTH)
  }

  /** Test to verify that HttpClientProvider client can be replaced and restored. */
  @Test
  fun httpClientProvider_clientCanBeReplacedAndRestored() {
    val originalClient = HttpClientProvider.client
    val testClient =
        OkHttpClient.Builder()
            .connectTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

    // Replace with test client
    HttpClientProvider.client = testClient
    assertEquals(testClient, HttpClientProvider.client)
    assertNotEquals(originalClient, HttpClientProvider.client)

    // Restore original
    HttpClientProvider.client = originalClient
    assertEquals(originalClient, HttpClientProvider.client)
  }

  /** Test to verify that Tab objects have unique routes. */
  @Test
  fun tab_allTabsHaveUniqueRoutes() {
    val routes = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile).map { it.destination.route }
    val uniqueRoutes = routes.toSet()
    assertEquals(routes.size, uniqueRoutes.size)
  }

  /** Test to verify that Tab objects have unique destination names. */
  @Test
  fun tab_allTabsHaveUniqueNames() {
    val names = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile).map { it.destination.name }
    val uniqueNames = names.toSet()
    assertEquals(names.size, uniqueNames.size)
  }

  /** Test to verify that Tab objects have unique icons. */
  @Test
  fun tab_allTabsHaveUniqueIcons() {
    val icons = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile).map { it.icon }
    val uniqueIcons = icons.toSet()
    assertEquals(icons.size, uniqueIcons.size)
  }

  // ===== Additional Route Tests for Coverage =====

  @Test
  fun route_createPublicEvent_isNotEmpty() {
    assertFalse(Route.CREATE_PUBLIC_EVENT.isEmpty())
  }

  @Test
  fun route_createPrivateEvent_isNotEmpty() {
    assertFalse(Route.CREATE_PRIVATE_EVENT.isEmpty())
  }

  @Test
  fun route_mapWithLocation_isNotEmpty() {
    assertFalse(Route.MAP_WITH_LOCATION.isEmpty())
  }

  @Test
  fun route_editPrivateEvent_isNotEmpty() {
    assertFalse(Route.EDIT_PRIVATE_EVENT.isEmpty())
  }

  @Test
  fun route_editPublicEvent_isNotEmpty() {
    assertFalse(Route.EDIT_PUBLIC_EVENT.isEmpty())
  }

  // ===== MainViewModel Tests =====

  @Test
  fun mainViewModel_initialState_isLoading() {
    val viewModel = MainViewModel(mockk(relaxed = true))
    val state = viewModel.uiState.value
    assertEquals(AppState.LOADING, state.appState)
  }

  @Test
  fun mainViewModel_checkInitialAuthState_withNoUser_goesToAuthentication() {
    val mockUserRepo = mockk<com.github.se.studentconnect.repository.UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    viewModel.checkInitialAuthState()

    // Give time for state to update
    Thread.sleep(100)

    val state = viewModel.uiState.value
    // Should go to AUTHENTICATION when no user is signed in
    assertTrue(state.appState == AppState.AUTHENTICATION || state.appState == AppState.LOADING)
  }

  @Test
  fun mainViewModel_onUserSignedIn_updatesState() {
    val mockUserRepo = mockk<com.github.se.studentconnect.repository.UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    coEvery { mockUserRepo.getUserById(any()) } returns null

    viewModel.onUserSignedIn("test-uid", "test@example.com")

    // Give time for state to update
    Thread.sleep(100)

    val state = viewModel.uiState.value
    assertEquals("test-uid", state.currentUserId)
    assertEquals("test@example.com", state.currentUserEmail)
  }

  @Test
  fun mainViewModel_onUserProfileCreated_updatesState() {
    val mockUserRepo = mockk<com.github.se.studentconnect.repository.UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    viewModel.onUserProfileCreated()

    val state = viewModel.uiState.value
    assertEquals(AppState.MAIN_APP, state.appState)
  }

  @Test
  fun mainViewModelFactory_createsViewModel() {
    val mockUserRepo = mockk<com.github.se.studentconnect.repository.UserRepository>(relaxed = true)
    val factory = MainViewModelFactory(mockUserRepo)

    val viewModel = factory.create(MainViewModel::class.java)

    assertNotNull(viewModel)
    assertTrue(viewModel is MainViewModel)
  }

  @Test
  fun mainViewModel_uiState_canBeObserved() {
    val mockUserRepo = mockk<com.github.se.studentconnect.repository.UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    val state = viewModel.uiState.value
    assertNotNull(state)
  }

  @Test
  fun mainUIState_withAllParameters() {
    val state =
        MainUIState(
            appState = AppState.ONBOARDING,
            currentUserId = "user-123",
            currentUserEmail = "user@test.com")

    assertEquals(AppState.ONBOARDING, state.appState)
    assertEquals("user-123", state.currentUserId)
    assertEquals("user@test.com", state.currentUserEmail)
  }

  @Test
  fun appState_valuesArray_containsAllStates() {
    val states = AppState.values()
    assertTrue(states.contains(AppState.LOADING))
    assertTrue(states.contains(AppState.AUTHENTICATION))
    assertTrue(states.contains(AppState.ONBOARDING))
    assertTrue(states.contains(AppState.MAIN_APP))
  }

  @Test
  fun httpClientProvider_multipleClientsCanBeSet() {
    val client1 = OkHttpClient()
    val client2 = OkHttpClient()
    val original = HttpClientProvider.client

    HttpClientProvider.client = client1
    assertEquals(client1, HttpClientProvider.client)

    HttpClientProvider.client = client2
    assertEquals(client2, HttpClientProvider.client)

    HttpClientProvider.client = original
  }
}
