package com.github.se.studentconnect.ui.eventcreation

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.media.MediaRepository
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePublicEventViewModelTest {

  private lateinit var viewModel: CreatePublicEventViewModel
  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockMediaRepository: MediaRepository
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockEventRepository = mock(EventRepository::class.java)
    mockMediaRepository = mock(MediaRepository::class.java)
    viewModel = CreatePublicEventViewModel(mockEventRepository, mockMediaRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state has empty values`() {
    val state = viewModel.uiState.value
    assertEquals("", state.title)
    assertEquals("", state.subtitle)
    assertEquals("", state.description)
    assertNull(state.location)
    assertNull(state.startDate)
    assertNull(state.endDate)
    assertFalse(state.hasParticipationFee)
    assertFalse(state.isFlash)
    assertFalse(state.isSaving)
    assertFalse(state.finishedSaving)
    assertTrue(state.tags.isEmpty())
  }

  @Test
  fun `updateTitle updates title in state`() {
    viewModel.updateTitle("Public Event Title")
    assertEquals("Public Event Title", viewModel.uiState.value.title)
  }

  @Test
  fun `updateSubtitle updates subtitle in state`() {
    viewModel.updateSubtitle("Event Subtitle")
    assertEquals("Event Subtitle", viewModel.uiState.value.subtitle)
  }

  @Test
  fun `updateDescription updates description in state`() {
    viewModel.updateDescription("Public Event Description")
    assertEquals("Public Event Description", viewModel.uiState.value.description)
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
    viewModel.updateNumberOfParticipantsString("100")
    assertEquals("100", viewModel.uiState.value.numberOfParticipantsString)
  }

  @Test
  fun `updateHasParticipationFee updates value in state`() {
    viewModel.updateHasParticipationFee(true)
    assertTrue(viewModel.uiState.value.hasParticipationFee)
  }

  @Test
  fun `updateParticipationFeeString updates value in state`() {
    viewModel.updateParticipationFeeString("30")
    assertEquals("30", viewModel.uiState.value.participationFeeString)
  }

  @Test
  fun `updateIsFlash updates value in state`() {
    viewModel.updateIsFlash(true)
    assertTrue(viewModel.uiState.value.isFlash)
  }

  @Test
  fun `updateWebsite updates website in state`() {
    viewModel.updateWebsite("https://example.com")
    assertEquals("https://example.com", viewModel.uiState.value.website)
  }

  @Test
  fun `updateTags updates tags in state`() {
    val tags = listOf("music", "festival", "outdoor")
    viewModel.updateTags(tags)
    assertEquals(tags, viewModel.uiState.value.tags)
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
  fun `prefill sets all fields from public event`() {
    val event =
        Event.Public(
            uid = "test-uid",
            ownerId = "owner-id",
            title = "Public Test Event",
            subtitle = "Test Subtitle",
            description = "Test Description",
            start = Timestamp.now(),
            end = Timestamp.now(),
            isFlash = true,
            maxCapacity = 500u,
            participationFee = 75u,
            location = Location(46.5, 6.6, "EPFL"),
            tags = listOf("tech", "conference"),
            website = "https://test.com")

    viewModel.prefill(event)

    val state = viewModel.uiState.value
    assertEquals("Public Test Event", state.title)
    assertEquals("Test Subtitle", state.subtitle)
    assertEquals("Test Description", state.description)
    assertTrue(state.isFlash)
    assertEquals("500", state.numberOfParticipantsString)
    assertTrue(state.hasParticipationFee)
    assertEquals("75", state.participationFeeString)
    assertEquals(listOf("tech", "conference"), state.tags)
    assertEquals("https://test.com", state.website)
  }

  @Test
  fun `prefillFromTemplate clears dates but keeps other fields`() {
    val event =
        Event.Public(
            uid = "template-uid",
            ownerId = "owner-id",
            title = "Template Public Event",
            subtitle = "Template Subtitle",
            description = "Template Description",
            start = Timestamp.now(),
            end = Timestamp.now(),
            isFlash = false,
            maxCapacity = 300u,
            tags = listOf("workshop"))

    viewModel.prefillFromTemplate(event)

    val state = viewModel.uiState.value
    assertEquals("Template Public Event", state.title)
    assertEquals("Template Subtitle", state.subtitle)
    assertEquals("Template Description", state.description)
    assertNull(state.startDate)
    assertNull(state.endDate)
    assertEquals("300", state.numberOfParticipantsString)
    assertEquals(listOf("workshop"), state.tags)
  }
}
