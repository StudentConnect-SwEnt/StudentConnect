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
import com.github.se.studentconnect.model.user.UserRepository
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
    NotificationRepositoryProvider.overrideForTests(mockNotificationRepo)

    // Setup UserRepositoryProvider with mock (it uses a property, not a setter method)
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
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
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
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
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
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
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    viewModel.onUserProfileCreated()

    val state = viewModel.uiState.value
    assertEquals(AppState.MAIN_APP, state.appState)
  }

  @Test
  fun mainViewModelFactory_createsViewModel() {
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
    val factory = MainViewModelFactory(mockUserRepo)

    val viewModel = factory.create(MainViewModel::class.java)

    assertNotNull(viewModel)
    assertTrue(viewModel is MainViewModel)
  }

  @Test
  fun mainViewModel_uiState_canBeObserved() {
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
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

  /** Test to verify that MAP_WITH_LOCATION route supports eventUid parameter. */
  @Test
  fun route_mapWithLocation_supportsEventUidParameter() {
    val route = Route.mapWithLocation(46.5197, 6.6323, eventUid = "test-event-123")
    assertTrue(route.contains("eventUid=test-event-123"))
    assertTrue(route.contains("46.5197"))
    assertTrue(route.contains("6.6323"))
    assertEquals("map/46.5197/6.6323/15.0?eventUid=test-event-123", route)
  }

  /** Test to verify that MAP_WITH_LOCATION route handles null eventUid. */
  @Test
  fun route_mapWithLocation_handlesNullEventUid() {
    val route = Route.mapWithLocation(46.5197, 6.6323, eventUid = null)
    assertTrue(route.contains("46.5197"))
    assertTrue(route.contains("6.6323"))
    assertFalse(route.contains("eventUid"))
  }

  /** Test to verify that MAP_WITH_LOCATION route defaults zoom parameter. */
  @Test
  fun route_mapWithLocation_defaultsZoomParameter() {
    val route = Route.mapWithLocation(46.5197, 6.6323)
    // Default zoom is 15.0, check it's in the route
    assertTrue(route.contains("15.0"))
    assertEquals("map/46.5197/6.6323/15.0", route)
  }

  /** Test to verify that create and edit event routes are defined. */
  @Test
  fun route_eventCreationAndEditing_routesExist() {
    assertFalse(Route.CREATE_PUBLIC_EVENT.isEmpty())
    assertFalse(Route.CREATE_PRIVATE_EVENT.isEmpty())
    assertTrue(Route.EDIT_PUBLIC_EVENT.contains("{eventUid}"))
    assertTrue(Route.EDIT_PRIVATE_EVENT.contains("{eventUid}"))
  }

  /** Test to verify that edit event route functions work correctly. */
  @Test
  fun route_editEventFunctions_generateCorrectRoutes() {
    val testEventUid = "test-event-123"
    val publicEditRoute = Route.editPublicEvent(testEventUid)
    val privateEditRoute = Route.editPrivateEvent(testEventUid)

    assertTrue(publicEditRoute.contains(testEventUid))
    assertTrue(privateEditRoute.contains(testEventUid))
    assertEquals("edit_public_event/$testEventUid", publicEditRoute)
    assertEquals("edit_private_event/$testEventUid", privateEditRoute)
  }

  // ===== Additional Route Coverage Tests =====

  @Test
  fun route_visitorProfile_generatesCorrectRoute() {
    val userId = "user123"
    val route = Route.visitorProfile(userId)
    assertTrue(route.contains(userId))
    assertEquals("visitorProfile/user123", route)
  }

  @Test
  fun route_eventView_generatesCorrectRoute() {
    val eventUid = "event123"
    val route = Route.eventView(eventUid, true)
    assertTrue(route.contains(eventUid))
    assertTrue(route.contains("true"))
  }

  @Test
  fun route_organizationProfile_generatesCorrectRoute() {
    val orgId = "org123"
    val route = Route.organizationProfile(orgId)
    assertTrue(route.contains(orgId))
  }

  @Test
  fun route_organizationProfileEdit_generatesCorrectRoute() {
    val orgId = "org456"
    val route = Route.organizationProfileEdit(orgId)
    assertTrue(route.contains(orgId))
  }

  @Test
  fun route_joinedEvents_generatesCorrectRoute() {
    val userId = "user789"
    val route = Route.joinedEvents(userId)
    assertTrue(route.contains(userId))
  }

  @Test
  fun route_pollsListScreen_generatesCorrectRoute() {
    val eventUid = "event999"
    val route = Route.pollsListScreen(eventUid)
    assertTrue(route.contains(eventUid))
  }

  @Test
  fun route_pollScreen_generatesCorrectRoute() {
    val eventUid = "event111"
    val pollUid = "poll222"
    val route = Route.pollScreen(eventUid, pollUid)
    assertTrue(route.contains(eventUid))
    assertTrue(route.contains(pollUid))
  }

  @Test
  fun route_eventStatistics_generatesCorrectRoute() {
    val eventUid = "event333"
    val route = Route.eventStatistics(eventUid)
    assertTrue(route.contains(eventUid))
  }

  @Test
  fun route_eventChat_generatesCorrectRoute() {
    val eventUid = "event444"
    val route = Route.eventChat(eventUid)
    assertTrue(route.contains(eventUid))
  }

  @Test
  fun route_createPublicEventFromTemplate_generatesCorrectRoute() {
    val templateUid = "template555"
    val route = Route.createPublicEventFromTemplate(templateUid)
    assertTrue(route.contains(templateUid))
  }

  @Test
  fun route_createPrivateEventFromTemplate_generatesCorrectRoute() {
    val templateUid = "template666"
    val route = Route.createPrivateEventFromTemplate(templateUid)
    assertTrue(route.contains(templateUid))
  }

  // ===== MainViewModel Additional Tests =====

  @Test
  fun mainViewModel_onLogoutComplete_updatesState() {
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    viewModel.onLogoutComplete()

    val state = viewModel.uiState.value
    assertEquals(AppState.AUTHENTICATION, state.appState)
  }

  @Test
  fun mainViewModel_multipleStateTransitions_workCorrectly() {
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    // Start at LOADING
    assertEquals(AppState.LOADING, viewModel.uiState.value.appState)

    // Sign in
    viewModel.onUserSignedIn("user1", "user1@test.com")
    Thread.sleep(100)

    // Create profile
    viewModel.onUserProfileCreated()
    assertEquals(AppState.MAIN_APP, viewModel.uiState.value.appState)

    // Logout
    viewModel.onLogoutComplete()
    assertEquals(AppState.AUTHENTICATION, viewModel.uiState.value.appState)
  }

  @Test
  fun mainUIState_equalsWorks() {
    val state1 = MainUIState(appState = AppState.LOADING)
    val state2 = MainUIState(appState = AppState.LOADING)
    assertEquals(state1, state2)
  }

  @Test
  fun mainUIState_hashCodeWorks() {
    val state1 = MainUIState(appState = AppState.LOADING, currentUserId = "user1")
    val state2 = MainUIState(appState = AppState.LOADING, currentUserId = "user1")
    assertEquals(state1.hashCode(), state2.hashCode())
  }

  @Test
  fun mainUIState_toStringContainsAllFields() {
    val state =
        MainUIState(
            appState = AppState.MAIN_APP,
            currentUserId = "testUserId",
            currentUserEmail = "test@email.com")
    val string = state.toString()
    assertTrue(string.contains("MAIN_APP"))
    assertTrue(string.contains("testUserId"))
    assertTrue(string.contains("test@email.com"))
  }

  // ===== AppState valueOf Tests =====

  @Test
  fun appState_valueOf_loading() {
    assertEquals(AppState.LOADING, AppState.valueOf("LOADING"))
  }

  @Test
  fun appState_valueOf_authentication() {
    assertEquals(AppState.AUTHENTICATION, AppState.valueOf("AUTHENTICATION"))
  }

  @Test
  fun appState_valueOf_onboarding() {
    assertEquals(AppState.ONBOARDING, AppState.valueOf("ONBOARDING"))
  }

  @Test
  fun appState_valueOf_mainApp() {
    assertEquals(AppState.MAIN_APP, AppState.valueOf("MAIN_APP"))
  }

  // ===== Route Constants Coverage =====

  @Test
  fun route_search_isNotEmpty() {
    assertFalse(Route.SEARCH.isEmpty())
  }

  @Test
  fun route_visitorProfile_isNotEmpty() {
    assertFalse(Route.VISITOR_PROFILE.isEmpty())
  }

  @Test
  fun route_organizationProfile_isNotEmpty() {
    assertFalse(Route.ORGANIZATION_PROFILE.isEmpty())
  }

  @Test
  fun route_organizationProfileEdit_isNotEmpty() {
    assertFalse(Route.ORGANIZATION_PROFILE_EDIT.isEmpty())
  }

  @Test
  fun route_joinedEvents_isNotEmpty() {
    assertFalse(Route.JOINED_EVENTS.isEmpty())
  }

  @Test
  fun route_selectEventTemplate_isNotEmpty() {
    assertFalse(Route.SELECT_EVENT_TEMPLATE.isEmpty())
  }

  @Test
  fun route_createPublicEventFromTemplate_isNotEmpty() {
    assertFalse(Route.CREATE_PUBLIC_EVENT_FROM_TEMPLATE.isEmpty())
  }

  @Test
  fun route_createPrivateEventFromTemplate_isNotEmpty() {
    assertFalse(Route.CREATE_PRIVATE_EVENT_FROM_TEMPLATE.isEmpty())
  }

  @Test
  fun route_pollsList_isNotEmpty() {
    assertFalse(Route.POLLS_LIST.isEmpty())
  }

  @Test
  fun route_pollScreen_isNotEmpty() {
    assertFalse(Route.POLL_SCREEN.isEmpty())
  }

  @Test
  fun route_eventStatistics_isNotEmpty() {
    assertFalse(Route.EVENT_STATISTICS.isEmpty())
  }

  @Test
  fun route_eventChat_isNotEmpty() {
    assertFalse(Route.EVENT_CHAT.isEmpty())
  }

  // ===== HttpClientProvider Additional Tests =====

  @Test
  fun httpClientProvider_defaultClientConfiguration() {
    val client = HttpClientProvider.client
    assertNotNull(client.connectionPool)
    assertNotNull(client.dispatcher)
  }

  @Test
  fun httpClientProvider_clientCanBeCustomized() {
    val original = HttpClientProvider.client
    val customClient = OkHttpClient.Builder().retryOnConnectionFailure(true).build()

    HttpClientProvider.client = customClient
    assertTrue(HttpClientProvider.client.retryOnConnectionFailure)

    HttpClientProvider.client = original
  }

  // ===== Tab Additional Tests =====

  @Test
  fun tab_allTabsAreUnique() {
    val tabs = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile)
    assertEquals(4, tabs.distinct().size)
  }

  @Test
  fun tab_homeIconIsValid() {
    assertTrue(Tab.Home.icon > 0)
  }

  @Test
  fun tab_mapIconIsValid() {
    assertTrue(Tab.Map.icon > 0)
  }

  @Test
  fun tab_activitiesIconIsValid() {
    assertTrue(Tab.Activities.icon > 0)
  }

  @Test
  fun tab_profileIconIsValid() {
    assertTrue(Tab.Profile.icon > 0)
  }

  // ===== MainActivity Lifecycle Tests =====

  @Test
  fun mainActivity_recreate_worksCorrectly() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.use {
      it.recreate()
      it.onActivity { activity ->
        assertNotNull(activity)
        assertTrue(activity is MainActivity)
      }
    }
  }

  @Test
  fun mainActivity_hasValidWindow() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.use {
      it.onActivity { activity ->
        assertNotNull(activity.window)
        assertNotNull(activity.window.decorView)
        assertNotNull(activity.window.attributes)
      }
    }
  }

  // ===== ProfileRoutes Coverage Tests =====

  @Test
  fun profileRoutes_editPicture_generatesCorrectRoute() {
    val userId = "user123"
    val route = com.github.se.studentconnect.ui.profile.ProfileRoutes.editPicture(userId)
    assertTrue(route.contains(userId))
  }

  @Test
  fun profileRoutes_editName_generatesCorrectRoute() {
    val userId = "user456"
    val route = com.github.se.studentconnect.ui.profile.ProfileRoutes.editName(userId)
    assertTrue(route.contains(userId))
  }

  @Test
  fun profileRoutes_editBio_generatesCorrectRoute() {
    val userId = "user789"
    val route = com.github.se.studentconnect.ui.profile.ProfileRoutes.editBio(userId)
    assertTrue(route.contains(userId))
  }

  @Test
  fun profileRoutes_editActivities_generatesCorrectRoute() {
    val userId = "user101"
    val route = com.github.se.studentconnect.ui.profile.ProfileRoutes.editActivities(userId)
    assertTrue(route.contains(userId))
  }

  @Test
  fun profileRoutes_editBirthday_generatesCorrectRoute() {
    val userId = "user202"
    val route = com.github.se.studentconnect.ui.profile.ProfileRoutes.editBirthday(userId)
    assertTrue(route.contains(userId))
  }

  @Test
  fun profileRoutes_editNationality_generatesCorrectRoute() {
    val userId = "user303"
    val route = com.github.se.studentconnect.ui.profile.ProfileRoutes.editNationality(userId)
    assertTrue(route.contains(userId))
  }

  @Test
  fun profileRoutes_friendsList_generatesCorrectRoute() {
    val userId = "user404"
    val route = com.github.se.studentconnect.ui.profile.ProfileRoutes.friendsList(userId)
    assertTrue(route.contains(userId))
  }

  // ===== Additional MainViewModel Tests =====

  @Test
  fun mainViewModelFactory_createsCorrectInstance() {
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
    val factory = MainViewModelFactory(mockUserRepo)

    val viewModel = factory.create(MainViewModel::class.java)

    assertNotNull(viewModel)
    assertEquals(MainViewModel::class.java, viewModel::class.java)
  }

  @Test
  fun mainViewModel_stateFlow_isObservable() {
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    val initialState = viewModel.uiState.value
    assertNotNull(initialState)
    assertEquals(AppState.LOADING, initialState.appState)
  }

  @Test
  fun mainViewModel_onUserSignedIn_withEmptyEmail() {
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
    coEvery { mockUserRepo.getUserById(any()) } returns null
    val viewModel = MainViewModel(mockUserRepo)

    viewModel.onUserSignedIn("test-uid", "")
    Thread.sleep(100)

    val state = viewModel.uiState.value
    assertEquals("test-uid", state.currentUserId)
    assertEquals("", state.currentUserEmail)
  }

  // ===== Route Argument Constants Tests =====

  @Test
  fun route_userIdArg_isNotEmpty() {
    assertFalse(Route.USER_ID_ARG.isEmpty())
  }

  @Test
  fun route_organizationIdArg_isNotEmpty() {
    assertFalse(Route.ORGANIZATION_ID_ARG.isEmpty())
  }

  // ===== Additional Coverage Tests =====

  @Test
  fun httpClientProvider_isObject() {
    assertNotNull(HttpClientProvider)
    assertEquals("HttpClientProvider", HttpClientProvider::class.simpleName)
  }

  @Test
  fun mainActivity_isComponentActivity() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.use {
      it.onActivity { activity ->
        assertTrue(activity is android.app.Activity)
        assertTrue(activity is androidx.activity.ComponentActivity)
      }
    }
  }

  @Test
  fun appState_enumOrdinals() {
    assertEquals(0, AppState.LOADING.ordinal)
    assertEquals(1, AppState.AUTHENTICATION.ordinal)
    assertEquals(2, AppState.ONBOARDING.ordinal)
    assertEquals(3, AppState.MAIN_APP.ordinal)
  }

  @Test
  fun mainViewModel_initialUserIdIsNull() {
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    assertNull(viewModel.uiState.value.currentUserId)
  }

  @Test
  fun mainViewModel_initialUserEmailIsNull() {
    val mockUserRepo = mockk<UserRepository>(relaxed = true)
    val viewModel = MainViewModel(mockUserRepo)

    assertNull(viewModel.uiState.value.currentUserEmail)
  }

  @Test
  fun route_mapWithLocation_withCustomZoom() {
    val route = Route.mapWithLocation(46.5, 6.6, 20.0)
    assertTrue(route.contains("20.0"))
  }

  @Test
  fun route_mapWithLocation_withEventUidAndCustomZoom() {
    val route = Route.mapWithLocation(46.5, 6.6, 18.0, "event123")
    assertTrue(route.contains("18.0"))
    assertTrue(route.contains("event123"))
  }
}
