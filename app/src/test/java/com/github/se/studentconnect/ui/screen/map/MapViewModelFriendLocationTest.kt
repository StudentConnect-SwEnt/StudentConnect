package com.github.se.studentconnect.ui.screen.map

import android.location.Location
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendLocation
import com.github.se.studentconnect.model.friends.FriendsLocationRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.map.LocationRepository
import com.github.se.studentconnect.model.map.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MapViewModelFriendLocationTest {
  private lateinit var locationRepo: LocationRepository
  private lateinit var eventRepo: EventRepository
  private lateinit var friendsRepo: FriendsRepository
  private lateinit var friendsLocationRepo: FriendsLocationRepository
  private lateinit var mockUserRepository: com.github.se.studentconnect.model.user.UserRepository
  private lateinit var viewModel: MapViewModel
  private val testDispatcher = StandardTestDispatcher()
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockAuth = mockk(relaxed = true)
    mockUser = mockk(relaxed = true)
    mockkStatic(FirebaseAuth::class)
    every { FirebaseAuth.getInstance() } returns mockAuth
    every { mockAuth.currentUser } returns mockUser
    every { mockUser.uid } returns "user123"

    locationRepo = mockk()
    eventRepo = mockk()
    friendsRepo = mockk()
    friendsLocationRepo = mockk()
    mockUserRepository = mockk(relaxed = true)
    coEvery { eventRepo.getAllVisibleEvents() } returns emptyList()
    coEvery { friendsRepo.getFriends(any()) } returns emptyList()
    every { friendsLocationRepo.observeFriendLocations(any(), any()) } returns flowOf(emptyMap())
    every { friendsLocationRepo.startListening() } just Runs
    every { friendsLocationRepo.stopListening() } just Runs
    viewModel = MapViewModel(locationRepo, eventRepo, friendsRepo, friendsLocationRepo, mockUserRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun initialState_friendLocations_isEmpty() {
    assertTrue(viewModel.uiState.value.friendLocations.isEmpty())
  }

  @Test
  fun observeFriendLocations_whenHasFriends_updatesState() = runTest {
    val friendIds = listOf("friend1")
    val locations = mapOf("friend1" to FriendLocation("friend1", 46.5191, 6.5668, 0L))
    coEvery { friendsRepo.getFriends("user123") } returns friendIds
    every { friendsLocationRepo.observeFriendLocations("user123", friendIds) } returns
        flowOf(locations)
    viewModel = MapViewModel(locationRepo, eventRepo, friendsRepo, friendsLocationRepo, mockUserRepository)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(1, viewModel.uiState.value.friendLocations.size)
  }

  @Test
  fun shareCurrentLocation_withoutPermission_doesNotShare() = runTest {
    viewModel.onEvent(MapViewEvent.SetLocationPermission(false))
    viewModel.shareCurrentLocation()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify(exactly = 0) { friendsLocationRepo.updateUserLocation(any(), any(), any()) }
  }

  @Test
  fun shareCurrentLocation_withValidLocation_updatesFirebase() = runTest {
    viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
    val loc = mockk<Location>(relaxed = true)
    every { loc.latitude } returns 46.5191
    every { loc.longitude } returns 6.5668
    coEvery { locationRepo.getCurrentLocation() } returns LocationResult.Success(loc)
    coEvery { friendsLocationRepo.updateUserLocation(any(), any(), any()) } just Runs
    viewModel.shareCurrentLocation()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify { friendsLocationRepo.updateUserLocation("user123", 46.5191, 6.5668) }
  }

  @Test
  fun shareCurrentLocation_onLocationError_doesNotUpdate() = runTest {
    viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
    coEvery { locationRepo.getCurrentLocation() } returns LocationResult.Error("Error")
    viewModel.shareCurrentLocation()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify(exactly = 0) { friendsLocationRepo.updateUserLocation(any(), any(), any()) }
  }

  @Test
  fun stopSharingLocation_withAuth_removesLocation() = runTest {
    coEvery { friendsLocationRepo.removeUserLocation(any()) } just Runs
    viewModel.stopSharingLocation()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify { friendsLocationRepo.removeUserLocation("user123") }
  }

  @Test
  fun stopSharingLocation_withoutAuth_doesNothing() = runTest {
    every { mockAuth.currentUser } returns null
    viewModel.stopSharingLocation()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify(exactly = 0) { friendsLocationRepo.removeUserLocation(any()) }
  }
}
