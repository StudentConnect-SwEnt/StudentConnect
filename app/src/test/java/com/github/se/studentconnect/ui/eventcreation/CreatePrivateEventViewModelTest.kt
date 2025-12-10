package com.github.se.studentconnect.ui.eventcreation

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePrivateEventViewModelTest {

  private lateinit var viewModel: CreatePrivateEventViewModel
  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockMediaRepository: com.github.se.studentconnect.model.media.MediaRepository
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockOrganizationRepository: OrganizationRepository
  private lateinit var mockFriendsRepository: FriendsRepository
  private lateinit var mockNotificationRepository: NotificationRepository
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockEventRepository = Mockito.mock(EventRepository::class.java)
    mockMediaRepository =
        Mockito.mock(com.github.se.studentconnect.model.media.MediaRepository::class.java)
    mockUserRepository = Mockito.mock(UserRepository::class.java)
    mockOrganizationRepository = Mockito.mock(OrganizationRepository::class.java)
    mockFriendsRepository = Mockito.mock(FriendsRepository::class.java)
    mockNotificationRepository = Mockito.mock(NotificationRepository::class.java)
    
    // override providers so ViewModel uses our mock repositories
    EventRepositoryProvider.overrideForTests(mockEventRepository)
    com.github.se.studentconnect.model.media.MediaRepositoryProvider.overrideForTests(
        mockMediaRepository)
    UserRepositoryProvider.overrideForTests(mockUserRepository)
    OrganizationRepositoryProvider.overrideForTests(mockOrganizationRepository)
    FriendsRepositoryProvider.overrideForTests(mockFriendsRepository)
    NotificationRepositoryProvider.overrideForTests(mockNotificationRepository)
    
    viewModel = CreatePrivateEventViewModel()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    // clean override to avoid leaking across tests
    EventRepositoryProvider.cleanOverrideForTests()
    com.github.se.studentconnect.model.media.MediaRepositoryProvider.cleanOverrideForTests()
    UserRepositoryProvider.cleanOverrideForTests()
    OrganizationRepositoryProvider.cleanOverrideForTests()
    FriendsRepositoryProvider.cleanOverrideForTests()
    NotificationRepositoryProvider.cleanOverrideForTests()
  }

  @Test
  fun `initial state has empty values`() {
    val state = viewModel.uiState.value
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertNull(state.location)
    assertNull(state.startDate)
    assertNull(state.endDate)
    assertFalse(state.hasParticipationFee)
    assertFalse(state.isFlash)
    assertFalse(state.isSaving)
    assertFalse(state.finishedSaving)
  }

  @Test
  fun `updateTitle updates title in state`() {
    viewModel.updateTitle("Test Event Title")
    assertEquals("Test Event Title", viewModel.uiState.value.title)
  }

  @Test
  fun `updateDescription updates description in state`() {
    viewModel.updateDescription("Test Description")
    assertEquals("Test Description", viewModel.uiState.value.description)
  }

  @Test
  fun `updateLocation updates location in state`() {
    val location = Location(latitude = 46.5, longitude = 6.6, name = "EPFL")
    viewModel.updateLocation(location)
    assertEquals(location, viewModel.uiState.value.location)
  }

  @Test
  fun `updateLocation with null clears location`() {
    val location = Location(latitude = 46.5, longitude = 6.6, name = "EPFL")
    viewModel.updateLocation(location)
    viewModel.updateLocation(null)
    assertNull(viewModel.uiState.value.location)
  }

  @Test
  fun `updateStartDate updates startDate in state`() {
    val date = LocalDate.of(2024, 12, 25)
    viewModel.updateStartDate(date)
    assertEquals(date, viewModel.uiState.value.startDate)
  }

  @Test
  fun `updateStartTime updates startTime in state`() {
    val time = LocalTime.of(14, 30)
    viewModel.updateStartTime(time)
    assertEquals(time, viewModel.uiState.value.startTime)
  }

  @Test
  fun `updateEndDate updates endDate in state`() {
    val date = LocalDate.of(2024, 12, 26)
    viewModel.updateEndDate(date)
    assertEquals(date, viewModel.uiState.value.endDate)
  }

  @Test
  fun `updateEndTime updates endTime in state`() {
    val time = LocalTime.of(18, 0)
    viewModel.updateEndTime(time)
    assertEquals(time, viewModel.uiState.value.endTime)
  }

  @Test
  fun `updateNumberOfParticipantsString updates value in state`() {
    viewModel.updateNumberOfParticipantsString("50")
    assertEquals("50", viewModel.uiState.value.numberOfParticipantsString)
  }

  @Test
  fun `updateHasParticipationFee updates value in state`() {
    viewModel.updateHasParticipationFee(true)
    assertTrue(viewModel.uiState.value.hasParticipationFee)
  }

  @Test
  fun `updateParticipationFeeString updates value in state`() {
    viewModel.updateParticipationFeeString("25")
    assertEquals("25", viewModel.uiState.value.participationFeeString)
  }

  @Test
  fun `updateIsFlash updates value in state`() {
    viewModel.updateIsFlash(true)
    assertTrue(viewModel.uiState.value.isFlash)
  }

  @Test
  fun `removeBannerImage clears banner and sets shouldRemoveBanner`() {
    viewModel.removeBannerImage()
    assertNull(viewModel.uiState.value.bannerImageUri)
    assertNull(viewModel.uiState.value.bannerImagePath)
    assertTrue(viewModel.uiState.value.shouldRemoveBanner)
  }

  @Test
  fun `resetFinishedSaving resets saving flags`() {
    viewModel.resetFinishedSaving()
    assertFalse(viewModel.uiState.value.finishedSaving)
    assertFalse(viewModel.uiState.value.isSaving)
  }

  @Test
  fun `prefill sets all fields from event via loadEvent`() = runTest {
    val event =
        Event.Private(
            uid = "test-uid",
            ownerId = "owner-id",
            title = "Test Event",
            description = "Test Description",
            start = Timestamp.now(),
            end = Timestamp.now(),
            isFlash = true,
            maxCapacity = 100u,
            participationFee = 50u,
            location = Location(46.5, 6.6, "EPFL"))

    // Stub repository to return the event when requested
    Mockito.`when`(mockEventRepository.getEvent("test-uid")).thenReturn(event)

    // Trigger loadEvent which will call the internal prefill in the coroutine
    viewModel.loadEvent("test-uid")

    // advance dispatched coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Test Event", state.title)
    assertEquals("Test Description", state.description)
    assertTrue(state.isFlash)
    assertEquals("100", state.numberOfParticipantsString)
    assertTrue(state.hasParticipationFee)
    assertEquals("50", state.participationFeeString)
  }

  @Test
  fun `prefillFromTemplate clears dates but keeps other fields`() {
    val event =
        Event.Private(
            uid = "test-uid",
            ownerId = "owner-id",
            title = "Template Event",
            description = "Template Description",
            start = Timestamp.now(),
            end = Timestamp.now(),
            isFlash = false,
            maxCapacity = 200u)

    viewModel.prefillFromTemplate(event)

    val state = viewModel.uiState.value
    assertEquals("Template Event", state.title)
    assertEquals("Template Description", state.description)
    assertNull(state.startDate)
    assertNull(state.endDate)
    assertEquals("200", state.numberOfParticipantsString)
  }
}
