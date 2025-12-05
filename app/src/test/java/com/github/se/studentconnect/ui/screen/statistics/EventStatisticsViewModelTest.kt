package com.github.se.studentconnect.ui.screen.statistics

import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventStatistics
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.resources.TestResourceProvider
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlin.NoSuchElementException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventStatisticsViewModelTest {

  private lateinit var eventRepository: EventRepository
  private lateinit var organizationRepository: OrganizationRepository
  private lateinit var viewModel: EventStatisticsViewModel
  private val testDispatcher = StandardTestDispatcher()
  // Resource provider reads app/src/main/res/values/strings.xml so tests use the same messages.
  private val rp = TestResourceProvider()

  private val testEventUid = "event123"
  private val testOwnerId = "org456"

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
          eventId = testEventUid,
          totalAttendees = 42,
          ageDistribution = emptyList(),
          campusDistribution = emptyList(),
          joinRateOverTime = emptyList(),
          followerCount = 3,
          attendeesFollowersRate = 66.67f)

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = mockk()
    organizationRepository = mockk()
    viewModel = EventStatisticsViewModel(eventRepository, organizationRepository, rp::getString)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `initial state is loading`() {
    assertTrue(viewModel.uiState.value.isLoading)
    assertNull(viewModel.uiState.value.statistics)
    assertNull(viewModel.uiState.value.error)
    assertEquals(0f, viewModel.uiState.value.animationProgress)
  }

  @Test
  fun `loadStatistics success updates state correctly`() = runTest {
    coEvery { eventRepository.getEvent(testEventUid) } returns testEvent
    coEvery { organizationRepository.getOrganizationById(testOwnerId) } returns testOrganization
    coEvery { eventRepository.getEventStatistics(testEventUid, 3) } returns testStatistics

    viewModel.loadStatistics(testEventUid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertNotNull(state.statistics)
    assertEquals(testStatistics, state.statistics)
    assertEquals(1f, state.animationProgress)
  }

  @Test
  fun `loadStatistics with event not found shows error`() = runTest {
    coEvery { eventRepository.getEvent(testEventUid) } throws
        NoSuchElementException("Event not found")

    viewModel.loadStatistics(testEventUid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Event not found", state.error)
    assertNull(state.statistics)
  }

  @Test
  fun `loadStatistics with exception shows error message`() = runTest {
    coEvery { eventRepository.getEvent(testEventUid) } throws RuntimeException("Network error")

    viewModel.loadStatistics(testEventUid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Network error", state.error)
    assertNull(state.statistics)
  }

  @Test
  fun `loadStatistics with exception without message shows unknown error`() = runTest {
    coEvery { eventRepository.getEvent(testEventUid) } throws RuntimeException()

    viewModel.loadStatistics(testEventUid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(rp.getString(R.string.stats_error_unknown), state.error)
  }

  @Test
  fun `loadStatistics continues with 0 followers when organization not found`() = runTest {
    coEvery { eventRepository.getEvent(testEventUid) } returns testEvent
    coEvery { organizationRepository.getOrganizationById(testOwnerId) } returns null
    coEvery { eventRepository.getEventStatistics(testEventUid, 0) } returns
        testStatistics.copy(followerCount = 0)

    viewModel.loadStatistics(testEventUid)
    advanceUntilIdle()

    coVerify { eventRepository.getEventStatistics(testEventUid, 0) }
    assertNull(viewModel.uiState.value.error)
  }

  @Test
  fun `loadStatistics continues with 0 followers when organization fetch throws`() = runTest {
    coEvery { eventRepository.getEvent(testEventUid) } returns testEvent
    coEvery { organizationRepository.getOrganizationById(testOwnerId) } throws
        RuntimeException("Org error")
    coEvery { eventRepository.getEventStatistics(testEventUid, 0) } returns
        testStatistics.copy(followerCount = 0)

    viewModel.loadStatistics(testEventUid)
    advanceUntilIdle()

    coVerify { eventRepository.getEventStatistics(testEventUid, 0) }
    assertNull(viewModel.uiState.value.error)
  }

  @Test
  fun `refresh reloads current event statistics`() = runTest {
    coEvery { eventRepository.getEvent(testEventUid) } returns testEvent
    coEvery { organizationRepository.getOrganizationById(testOwnerId) } returns testOrganization
    coEvery { eventRepository.getEventStatistics(testEventUid, 3) } returns testStatistics

    viewModel.loadStatistics(testEventUid)
    advanceUntilIdle()

    viewModel.refresh()
    advanceUntilIdle()

    coVerify(exactly = 2) { eventRepository.getEvent(testEventUid) }
    coVerify(exactly = 2) { eventRepository.getEventStatistics(testEventUid, 3) }
  }

  @Test
  fun `refresh does nothing if no event was loaded`() = runTest {
    viewModel.refresh()
    advanceUntilIdle()

    coVerify(exactly = 0) { eventRepository.getEvent(any()) }
  }

  @Test
  fun `loadStatistics with organization having empty memberUids uses zero`() = runTest {
    val orgWithEmptyMembers =
        Organization(
            id = testOwnerId,
            name = "Test Org",
            type = OrganizationType.Association,
            description = "Test",
            memberUids = emptyList(),
            createdBy = "creator")
    coEvery { eventRepository.getEvent(testEventUid) } returns testEvent
    coEvery { organizationRepository.getOrganizationById(testOwnerId) } returns orgWithEmptyMembers
    coEvery { eventRepository.getEventStatistics(testEventUid, 0) } returns
        testStatistics.copy(followerCount = 0)

    viewModel.loadStatistics(testEventUid)
    advanceUntilIdle()

    coVerify { eventRepository.getEventStatistics(testEventUid, 0) }
    assertNull(viewModel.uiState.value.error)
    assertEquals(0, viewModel.uiState.value.statistics?.followerCount)
  }

  @Test
  fun `loadStatistics with statistics fetch exception shows error`() = runTest {
    coEvery { eventRepository.getEvent(testEventUid) } returns testEvent
    coEvery { organizationRepository.getOrganizationById(testOwnerId) } returns testOrganization
    coEvery { eventRepository.getEventStatistics(testEventUid, 3) } throws
        RuntimeException("Stats fetch failed")

    viewModel.loadStatistics(testEventUid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Stats fetch failed", state.error)
    assertNull(state.statistics)
  }
}
