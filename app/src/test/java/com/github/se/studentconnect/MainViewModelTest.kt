package com.github.se.studentconnect

import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private val mainDispatcher = UnconfinedTestDispatcher(testDispatcher.scheduler)

  private lateinit var userRepository: UserRepository
  private lateinit var viewModel: MainViewModel
  private lateinit var mockFirebaseAuth: FirebaseAuth
  private lateinit var mockFirebaseUser: FirebaseUser

  @Before
  fun setUp() {
    Dispatchers.setMain(mainDispatcher)
    userRepository = mockk()
    mockFirebaseAuth = mockk()
    mockFirebaseUser = mockk()

    // Mock Firebase Auth
    mockkStatic(FirebaseAuth::class)
    mockkStatic("com.google.firebase.FirebaseKt")
    every { Firebase.auth } returns mockFirebaseAuth

    viewModel = MainViewModel(userRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    AuthenticationProvider.local = false
    AuthenticationProvider.testUserId = null
    unmockkAll()
  }

  // ===================== Initial Auth State Tests =====================

  @Test
  fun `checkInitialAuthState with no Firebase user shows AUTHENTICATION state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        AuthenticationProvider.local = false
        every { mockFirebaseAuth.currentUser } returns null

        // Act
        viewModel.checkInitialAuthState()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.AUTHENTICATION, state.appState)
        assertNull(state.currentUserId)
        assertNull(state.currentUserEmail)
      }

  @Test
  fun `checkInitialAuthState with Firebase user but no profile shows ONBOARDING state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val testUserId = "test-user-id"
        val testEmail = "test@example.com"
        AuthenticationProvider.local = false
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns testEmail
        coEvery { userRepository.getUserById(testUserId) } returns null

        // Act
        viewModel.checkInitialAuthState()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.ONBOARDING, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
        coVerify { userRepository.getUserById(testUserId) }
      }

  @Test
  fun `checkInitialAuthState with Firebase user and profile shows MAIN_APP state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val testUserId = "test-user-id"
        val testEmail = "test@example.com"
        val existingUser =
            User(
                userId = testUserId,
                email = testEmail,
                username = "testuser",
                firstName = "Test",
                lastName = "User",
                university = "EPFL")
        AuthenticationProvider.local = false
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns testEmail
        coEvery { userRepository.getUserById(testUserId) } returns existingUser

        // Act
        viewModel.checkInitialAuthState()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.MAIN_APP, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
        coVerify { userRepository.getUserById(testUserId) }
      }

  @Test
  fun `checkInitialAuthState in local mode with no profile shows ONBOARDING state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val testUserId = "user-charlie-02"
        val testEmail = "test@epfl.ch"
        AuthenticationProvider.local = true
        coEvery { userRepository.getUserById(testUserId) } returns null

        // Act
        viewModel.checkInitialAuthState()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.ONBOARDING, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
        coVerify { userRepository.getUserById(testUserId) }
      }

  @Test
  fun `checkInitialAuthState in local mode with profile shows MAIN_APP state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val testUserId = "user-charlie-02"
        val testEmail = "test@epfl.ch"
        val existingUser =
            User(
                userId = testUserId,
                email = testEmail,
                username = "charlie",
                firstName = "Charlie",
                lastName = "Test",
                university = "EPFL")
        AuthenticationProvider.local = true
        coEvery { userRepository.getUserById(testUserId) } returns existingUser

        // Act
        viewModel.checkInitialAuthState()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.MAIN_APP, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
        coVerify { userRepository.getUserById(testUserId) }
      }

  // ===================== User Sign In Tests =====================

  @Test
  fun `onUserSignedIn with no profile transitions to ONBOARDING state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val testUserId = "test-user-id"
        val testEmail = "test@example.com"
        coEvery { userRepository.getUserById(testUserId) } returns null

        // Act
        viewModel.onUserSignedIn(testUserId, testEmail)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.ONBOARDING, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
        coVerify { userRepository.getUserById(testUserId) }
      }

  @Test
  fun `onUserSignedIn with existing profile transitions to MAIN_APP state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val testUserId = "test-user-id"
        val testEmail = "test@example.com"
        val existingUser =
            User(
                userId = testUserId,
                email = testEmail,
                username = "testuser",
                firstName = "Test",
                lastName = "User",
                university = "EPFL")
        coEvery { userRepository.getUserById(testUserId) } returns existingUser

        // Act
        viewModel.onUserSignedIn(testUserId, testEmail)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.MAIN_APP, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
        coVerify { userRepository.getUserById(testUserId) }
      }

  @Test
  fun `onUserSignedIn updates state with user credentials immediately`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val testUserId = "test-user-id"
        val testEmail = "test@example.com"
        coEvery { userRepository.getUserById(testUserId) } returns null

        // Act
        viewModel.onUserSignedIn(testUserId, testEmail)
        // Don't advance coroutines - check immediate state update

        // Assert - state should be updated immediately
        val state = viewModel.uiState.value
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
      }

  // ===================== User Profile Creation Tests =====================

  @Test
  fun `onUserProfileCreated transitions to MAIN_APP state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange - start in ONBOARDING state
        val testUserId = "test-user-id"
        val testEmail = "test@example.com"
        coEvery { userRepository.getUserById(testUserId) } returns null
        viewModel.onUserSignedIn(testUserId, testEmail)
        advanceUntilIdle()
        assertEquals(AppState.ONBOARDING, viewModel.uiState.value.appState)

        // Act
        viewModel.onUserProfileCreated()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.MAIN_APP, state.appState)
        // User credentials should remain unchanged
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
      }

  @Test
  fun `onUserProfileCreated preserves user credentials`() =
      runTest(testDispatcher.scheduler) {
        // Arrange - set up state with user credentials
        val testUserId = "test-user-id"
        val testEmail = "test@example.com"
        coEvery { userRepository.getUserById(testUserId) } returns null
        viewModel.onUserSignedIn(testUserId, testEmail)
        advanceUntilIdle()

        // Act
        viewModel.onUserProfileCreated()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
      }

  // ===================== State Machine Flow Tests =====================

  @Test
  fun `complete first-time user flow from LOADING to MAIN_APP`() =
      runTest(testDispatcher.scheduler) {
        // Arrange - simulate new user with no Firebase auth
        val testUserId = "new-user-id"
        val testEmail = "newuser@example.com"
        AuthenticationProvider.local = false
        every { mockFirebaseAuth.currentUser } returns null

        // Step 1: Check initial state - should go to AUTHENTICATION
        viewModel.checkInitialAuthState()
        advanceUntilIdle()
        assertEquals(AppState.AUTHENTICATION, viewModel.uiState.value.appState)

        // Step 2: User signs in - should check profile and go to ONBOARDING
        coEvery { userRepository.getUserById(testUserId) } returns null
        viewModel.onUserSignedIn(testUserId, testEmail)
        advanceUntilIdle()
        assertEquals(AppState.ONBOARDING, viewModel.uiState.value.appState)
        assertEquals(testUserId, viewModel.uiState.value.currentUserId)

        // Step 3: User completes onboarding - should go to MAIN_APP
        viewModel.onUserProfileCreated()
        assertEquals(AppState.MAIN_APP, viewModel.uiState.value.appState)
        assertEquals(testUserId, viewModel.uiState.value.currentUserId)
        assertEquals(testEmail, viewModel.uiState.value.currentUserEmail)
      }

  @Test
  fun `returning user flow skips AUTHENTICATION and ONBOARDING`() =
      runTest(testDispatcher.scheduler) {
        // Arrange - simulate returning user with Firebase auth and profile
        val testUserId = "returning-user-id"
        val testEmail = "returning@example.com"
        val existingUser =
            User(
                userId = testUserId,
                email = testEmail,
                username = "returninguser",
                firstName = "Returning",
                lastName = "User",
                university = "EPFL")
        AuthenticationProvider.local = false
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns testEmail
        coEvery { userRepository.getUserById(testUserId) } returns existingUser

        // Act - initial check should go directly to MAIN_APP
        viewModel.checkInitialAuthState()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.MAIN_APP, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
      }

  @Test
  fun `user with Firebase auth but no profile goes to ONBOARDING`() =
      runTest(testDispatcher.scheduler) {
        // Arrange - simulate user who signed in but never completed onboarding
        val testUserId = "incomplete-user-id"
        val testEmail = "incomplete@example.com"
        AuthenticationProvider.local = false
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns testEmail
        coEvery { userRepository.getUserById(testUserId) } returns null

        // Act
        viewModel.checkInitialAuthState()
        advanceUntilIdle()

        // Assert - should skip AUTHENTICATION but show ONBOARDING
        val state = viewModel.uiState.value
        assertEquals(AppState.ONBOARDING, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
      }

  // ===================== Edge Cases and Error Handling =====================

  @Test
  fun `initial state is LOADING`() {
    // Assert
    val state = viewModel.uiState.value
    assertEquals(AppState.LOADING, state.appState)
    assertNull(state.currentUserId)
    assertNull(state.currentUserEmail)
  }

  @Test
  fun `onUserSignedIn with empty userId still updates state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val testUserId = ""
        val testEmail = "test@example.com"
        coEvery { userRepository.getUserById(testUserId) } returns null

        // Act
        viewModel.onUserSignedIn(testUserId, testEmail)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppState.LOADING, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
      }

  @Test
  fun `onUserSignedIn with empty email still updates state`() =
      runTest(testDispatcher.scheduler) {
        // Arrange
        val testUserId = "test-user-id"
        val testEmail = ""
        coEvery { userRepository.getUserById(testUserId) } returns null

        // Act
        viewModel.onUserSignedIn(testUserId, testEmail)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(AppState.ONBOARDING, state.appState)
        assertEquals(testUserId, state.currentUserId)
        assertEquals(testEmail, state.currentUserEmail)
      }

  @Test
  fun `multiple calls to checkInitialAuthState with different Firebase states`() =
      runTest(testDispatcher.scheduler) {
        // First call - no Firebase user
        AuthenticationProvider.local = false
        every { mockFirebaseAuth.currentUser } returns null
        viewModel.checkInitialAuthState()
        advanceUntilIdle()
        assertEquals(AppState.AUTHENTICATION, viewModel.uiState.value.appState)

        // Second call - Firebase user appears (simulating sign-in)
        val testUserId = "test-user-id"
        val testEmail = "test@example.com"
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns testEmail
        coEvery { userRepository.getUserById(testUserId) } returns null

        viewModel.checkInitialAuthState()
        advanceUntilIdle()

        // Assert - state should update to ONBOARDING
        assertEquals(AppState.ONBOARDING, viewModel.uiState.value.appState)
        assertEquals(testUserId, viewModel.uiState.value.currentUserId)
      }

  // ===================== ViewModel Factory Tests =====================

  @Test
  fun `MainViewModelFactory creates MainViewModel instance`() {
    // Arrange
    val factory = MainViewModelFactory(userRepository)

    // Act
    val createdViewModel = factory.create(MainViewModel::class.java)

    // Assert
    assert(createdViewModel is MainViewModel)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `MainViewModelFactory throws exception for unknown ViewModel class`() {
    // Arrange
    val factory = MainViewModelFactory(userRepository)

    // Act - this should throw IllegalArgumentException
    factory.create(UnknownViewModel::class.java)
  }

  // Dummy ViewModel class for testing factory error case
  private class UnknownViewModel : androidx.lifecycle.ViewModel()
}
