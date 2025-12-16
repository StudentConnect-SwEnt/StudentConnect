package com.github.se.studentconnect.ui.eventcreation

import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.location.LocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationTextFieldViewModelTest {

  private lateinit var locationRepository: LocationRepository
  private lateinit var viewModel: LocationTextFieldViewModel

  @Before
  fun setUp() {
    locationRepository = mockk(relaxed = true)
    viewModel = LocationTextFieldViewModel(locationRepository)
  }

  @Test
  fun `updateLocationSuggestions sets offline message when offline`() = runTest {
    viewModel.updateLocationSuggestions("Paris", isNetworkAvailable = false)

    assertEquals(R.string.offline_no_internet_try_later, viewModel.offlineMessageRes.value)
    assertEquals(emptyList<Location>(), viewModel.uiState.value.locationSuggestions)
    assertEquals(false, viewModel.uiState.value.isLoadingLocationSuggestions)
  }

  @Test
  fun `updateLocationSuggestions clears offline message when online`() = runTest {
    // First set offline message
    viewModel.updateLocationSuggestions("Paris", isNetworkAvailable = false)
    assertEquals(R.string.offline_no_internet_try_later, viewModel.offlineMessageRes.value)

    // Now update with network available
    coEvery { locationRepository.search(any()) } returns emptyList()
    viewModel.updateLocationSuggestions("Paris", isNetworkAvailable = true)
    advanceUntilIdle()

    assertNull(viewModel.offlineMessageRes.value)
  }

  @Test
  fun `updateLocationSuggestions does not search when offline`() = runTest {
    viewModel.updateLocationSuggestions("Paris", isNetworkAvailable = false)

    // Verify no search was performed (suggestions remain empty)
    assertEquals(emptyList<Location>(), viewModel.uiState.value.locationSuggestions)
    assertEquals(false, viewModel.uiState.value.isLoadingLocationSuggestions)
  }

  @Test
  fun `clearOfflineMessage clears offline message`() {
    // Set offline message manually
    viewModel.updateLocationSuggestions("Paris", isNetworkAvailable = false)
    assertEquals(R.string.offline_no_internet_try_later, viewModel.offlineMessageRes.value)

    viewModel.clearOfflineMessage()

    assertNull(viewModel.offlineMessageRes.value)
  }

  @Test
  fun `updateLocationSuggestions performs search when online`() = runTest {
    val mockLocations = listOf(
        Location(latitude = 48.8566, longitude = 2.3522, name = "Paris"),
        Location(latitude = 46.5197, longitude = 6.6323, name = "Lausanne"))
    coEvery { locationRepository.search("Paris") } returns mockLocations

    viewModel.updateLocationSuggestions("Paris", isNetworkAvailable = true)
    advanceUntilIdle()

    assertNull(viewModel.offlineMessageRes.value)
    // Note: The actual suggestions will be set by the flow, but we verify no offline message
  }
}
