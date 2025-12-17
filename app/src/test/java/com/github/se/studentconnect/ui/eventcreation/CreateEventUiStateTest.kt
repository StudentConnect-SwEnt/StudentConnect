package com.github.se.studentconnect.ui.eventcreation

import com.github.se.studentconnect.model.location.Location
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CreateEventUiStateTest {

  // --- Public State Tests ---

  @Test
  fun `Public state has correct default values`() {
    val state = CreateEventUiState.Public()

    assertEquals("", state.title)
    assertEquals("", state.description)
    assertNull(state.location)
    assertNull(state.startDate)
    assertEquals(LocalTime.of(0, 0), state.startTime)
    assertNull(state.endDate)
    assertEquals(LocalTime.of(0, 0), state.endTime)
    assertEquals("", state.numberOfParticipantsString)
    assertFalse(state.hasParticipationFee)
    assertEquals("", state.participationFeeString)
    assertFalse(state.isFlash)
    assertEquals(1, state.flashDurationHours)
    assertEquals(0, state.flashDurationMinutes)
    assertFalse(state.finishedSaving)
    assertFalse(state.isSaving)
    assertNull(state.bannerImageUri)
    assertNull(state.bannerImagePath)
    assertFalse(state.shouldRemoveBanner)
    assertFalse(state.isGeneratingBanner)
    assertEquals("", state.subtitle)
    assertEquals("", state.website)
    assertTrue(state.tags.isEmpty())
  }

  @Test
  fun `Public state copy works correctly`() {
    val state = CreateEventUiState.Public()
    val newState =
        state.copy(
            title = "Test Title",
            subtitle = "Test Subtitle",
            description = "Test Description",
            isGeneratingBanner = true)

    assertEquals("Test Title", newState.title)
    assertEquals("Test Subtitle", newState.subtitle)
    assertEquals("Test Description", newState.description)
    assertTrue(newState.isGeneratingBanner)
  }

  // --- Private State Tests ---

  @Test
  fun `Private state has correct default values`() {
    val state = CreateEventUiState.Private()

    assertEquals("", state.title)
    assertEquals("", state.description)
    assertNull(state.location)
    assertNull(state.startDate)
    assertEquals(LocalTime.of(0, 0), state.startTime)
    assertNull(state.endDate)
    assertEquals(LocalTime.of(0, 0), state.endTime)
    assertEquals("", state.numberOfParticipantsString)
    assertFalse(state.hasParticipationFee)
    assertEquals("", state.participationFeeString)
    assertFalse(state.isFlash)
    assertEquals(1, state.flashDurationHours)
    assertEquals(0, state.flashDurationMinutes)
    assertFalse(state.finishedSaving)
    assertFalse(state.isSaving)
    assertNull(state.bannerImageUri)
    assertNull(state.bannerImagePath)
    assertFalse(state.shouldRemoveBanner)
    assertFalse(state.isGeneratingBanner)
  }

  @Test
  fun `Private state copy works correctly`() {
    val state = CreateEventUiState.Private()
    val newState =
        state.copy(
            title = "Private Event", description = "Private Description", isGeneratingBanner = true)

    assertEquals("Private Event", newState.title)
    assertEquals("Private Description", newState.description)
    assertTrue(newState.isGeneratingBanner)
  }

  // --- copyCommon Tests ---

  @Test
  fun `copyCommon on Public state preserves type and updates fields`() {
    val state = CreateEventUiState.Public(title = "Original")
    val newState = state.copyCommon(title = "Updated Title", description = "New Description")

    assertTrue(newState is CreateEventUiState.Public)
    assertEquals("Updated Title", newState.title)
    assertEquals("New Description", newState.description)
  }

  @Test
  fun `copyCommon on Private state preserves type and updates fields`() {
    val state = CreateEventUiState.Private(title = "Original")
    val newState = state.copyCommon(title = "Updated Title", description = "New Description")

    assertTrue(newState is CreateEventUiState.Private)
    assertEquals("Updated Title", newState.title)
    assertEquals("New Description", newState.description)
  }

  @Test
  fun `copyCommon updates location correctly`() {
    val state = CreateEventUiState.Public()
    val location = Location(latitude = 46.5, longitude = 6.6, name = "EPFL")
    val newState = state.copyCommon(location = location)

    assertEquals(location, newState.location)
  }

  @Test
  fun `copyCommon updates dates and times correctly`() {
    val state = CreateEventUiState.Public()
    val startDate = LocalDate.of(2024, 12, 25)
    val endDate = LocalDate.of(2024, 12, 26)
    val startTime = LocalTime.of(10, 0)
    val endTime = LocalTime.of(18, 0)

    val newState =
        state.copyCommon(
            startDate = startDate, endDate = endDate, startTime = startTime, endTime = endTime)

    assertEquals(startDate, newState.startDate)
    assertEquals(endDate, newState.endDate)
    assertEquals(startTime, newState.startTime)
    assertEquals(endTime, newState.endTime)
  }

  @Test
  fun `copyCommon updates flash event fields correctly`() {
    val state = CreateEventUiState.Public()
    val newState =
        state.copyCommon(isFlash = true, flashDurationHours = 2, flashDurationMinutes = 30)

    assertTrue(newState.isFlash)
    assertEquals(2, newState.flashDurationHours)
    assertEquals(30, newState.flashDurationMinutes)
  }

  @Test
  fun `copyCommon updates participation fee fields correctly`() {
    val state = CreateEventUiState.Private()
    val newState =
        state.copyCommon(
            numberOfParticipantsString = "50",
            hasParticipationFee = true,
            participationFeeString = "25")

    assertEquals("50", newState.numberOfParticipantsString)
    assertTrue(newState.hasParticipationFee)
    assertEquals("25", newState.participationFeeString)
  }

  @Test
  fun `copyCommon updates saving state correctly`() {
    val state = CreateEventUiState.Public()
    val savingState = state.copyCommon(isSaving = true)
    assertTrue(savingState.isSaving)

    val finishedState = state.copyCommon(finishedSaving = true, isSaving = false)
    assertTrue(finishedState.finishedSaving)
    assertFalse(finishedState.isSaving)
  }

  @Test
  fun `copyCommon updates banner fields correctly`() {
    val state = CreateEventUiState.Public()
    val newState =
        state.copyCommon(bannerImagePath = "path/to/banner.jpg", shouldRemoveBanner = false)

    assertEquals("path/to/banner.jpg", newState.bannerImagePath)
    assertFalse(newState.shouldRemoveBanner)
  }

  @Test
  fun `copyCommon updates isGeneratingBanner correctly`() {
    val state = CreateEventUiState.Public()
    val generatingState = state.copyCommon(isGeneratingBanner = true)
    assertTrue(generatingState.isGeneratingBanner)

    val notGeneratingState = generatingState.copyCommon(isGeneratingBanner = false)
    assertFalse(notGeneratingState.isGeneratingBanner)
  }

  @Test
  fun `copyCommon preserves Public-specific fields`() {
    val state =
        CreateEventUiState.Public(
            subtitle = "My Subtitle",
            website = "https://example.com",
            tags = listOf("music", "party"))
    val newState = state.copyCommon(title = "New Title")

    assertTrue(newState is CreateEventUiState.Public)
    val publicState = newState as CreateEventUiState.Public
    assertEquals("My Subtitle", publicState.subtitle)
    assertEquals("https://example.com", publicState.website)
    assertEquals(listOf("music", "party"), publicState.tags)
  }

  @Test
  fun `copyCommon with all default parameters returns equivalent state`() {
    val state =
        CreateEventUiState.Public(title = "Test", description = "Description", isFlash = true)
    val newState = state.copyCommon()

    assertEquals(state.title, newState.title)
    assertEquals(state.description, newState.description)
    assertEquals(state.isFlash, newState.isFlash)
  }

  @Test
  fun `copyCommon handles null location`() {
    val location = Location(latitude = 46.5, longitude = 6.6, name = "EPFL")
    val state = CreateEventUiState.Public(location = location)
    val newState = state.copyCommon(location = null)

    assertNull(newState.location)
  }

  @Test
  fun `copyCommon handles null dates`() {
    val state =
        CreateEventUiState.Private(
            startDate = LocalDate.of(2024, 12, 25), endDate = LocalDate.of(2024, 12, 26))
    val newState = state.copyCommon(startDate = null, endDate = null)

    assertNull(newState.startDate)
    assertNull(newState.endDate)
  }
}
