package com.github.se.studentconnect.ui.eventcreation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BaseCreateEventViewModelTest {

  private class FakeViewModel(
      initialState: CreateEventUiState.Public,
      eventRepository: EventRepository = mockk(relaxed = true),
      mediaRepository: MediaRepository = mockk(relaxed = true),
      userRepository: UserRepository = mockk(relaxed = true),
      organizationRepository: OrganizationRepository = mockk(relaxed = true),
      friendsRepository: FriendsRepository = mockk(relaxed = true),
      notificationRepository: NotificationRepository = mockk(relaxed = true)
  ) :
      BaseCreateEventViewModel<CreateEventUiState.Public>(
          initialState,
          eventRepository,
          mediaRepository,
          userRepository,
          organizationRepository,
          friendsRepository,
          notificationRepository) {
    override fun buildEvent(uid: String, ownerId: String, bannerPath: String?): Event =
        Event.Public(
            uid = uid,
            ownerId = ownerId,
            title = uiState.value.title,
            description = uiState.value.description,
            imageUrl = bannerPath,
            location = uiState.value.location,
            start = Timestamp.now(),
            end = Timestamp.now(),
            maxCapacity = null,
            participationFee = null,
            isFlash = uiState.value.isFlash,
            subtitle = "",
            tags = emptyList(),
            website = null)

    // Expose protected methods for testing
    fun testCalculateFlashDuration(event: Event): Pair<Int, Int> = calculateFlashDuration(event)

    fun testValidateState(): Boolean = validateState()
  }

  private fun createViewModel(
      initialState: CreateEventUiState.Public = CreateEventUiState.Public()
  ): FakeViewModel = FakeViewModel(initialState)

  // ==================== Banner Resolution Tests ====================

  @Test
  fun resolveBannerForSave_offlineStagesFile() = runTest {
    val appContext = ApplicationProvider.getApplicationContext<Context>()

    val tmpDir = File(appContext.filesDir, "banner_test").apply { mkdirs() }
    val source = File(tmpDir, "banner.png").apply { writeText("banner-bytes") }
    val uri = Uri.fromFile(source)

    val vm =
        FakeViewModel(
            initialState = CreateEventUiState.Public(bannerImageUri = uri),
            eventRepository = mockk(relaxed = true),
            mediaRepository = mockk(relaxed = true),
            userRepository = mockk(relaxed = true),
            organizationRepository = mockk(relaxed = true),
            friendsRepository = mockk(relaxed = true),
            notificationRepository = mockk(relaxed = true))

    val result = vm.resolveBannerForSave(appContext, "event-1")

    val resClass = result::class.java
    val pathField = resClass.getDeclaredField("bannerPathForEvent").apply { isAccessible = true }
    val pendingField = resClass.getDeclaredField("pendingUpload").apply { isAccessible = true }

    val stagedPath = pathField.get(result) as String?
    val pendingUpload = pendingField.get(result)

    assertNotNull(stagedPath)
    assertNotNull(pendingUpload)
  }

  @Test
  fun resolveBannerForSave_returnsBannerPathWhenShouldRemove() = runTest {
    val appContext = ApplicationProvider.getApplicationContext<Context>()

    val vm =
        FakeViewModel(
            initialState =
                CreateEventUiState.Public(
                    bannerImageUri = null, bannerImagePath = null, shouldRemoveBanner = true))

    val result = vm.resolveBannerForSave(appContext, "event-1")

    val resClass = result::class.java
    val pathField = resClass.getDeclaredField("bannerPathForEvent").apply { isAccessible = true }
    val pendingField = resClass.getDeclaredField("pendingUpload").apply { isAccessible = true }

    assertNull(pathField.get(result))
    assertNull(pendingField.get(result))
  }

  @Test
  fun resolveBannerForSave_returnsExistingPathWhenNoChanges() = runTest {
    val appContext = ApplicationProvider.getApplicationContext<Context>()
    val existingPath = "https://example.com/existing-banner.jpg"

    val vm =
        FakeViewModel(
            initialState =
                CreateEventUiState.Public(
                    bannerImageUri = null,
                    bannerImagePath = existingPath,
                    shouldRemoveBanner = false))

    val result = vm.resolveBannerForSave(appContext, "event-1")

    val resClass = result::class.java
    val pathField = resClass.getDeclaredField("bannerPathForEvent").apply { isAccessible = true }
    val pendingField = resClass.getDeclaredField("pendingUpload").apply { isAccessible = true }

    assertEquals(existingPath, pathField.get(result))
    assertNull(pendingField.get(result))
  }

  // ==================== Banner Upload Job Tests ====================

  @Test
  fun bannerUploadJob_canBeConstructedViaReflection() {
    val clazz =
        Class.forName(
            "com.github.se.studentconnect.ui.eventcreation.BaseCreateEventViewModel\$BannerUploadJob")
    val ctor =
        clazz.getDeclaredConstructor(
            String::class.java, String::class.java, String::class.java, String::class.java)
    ctor.isAccessible = true

    val instance = ctor.newInstance("/tmp/file.png", "events/x/banner", "event-1", null)

    val filePath = clazz.getDeclaredField("filePath").apply { isAccessible = true }.get(instance)
    assertEquals("/tmp/file.png", filePath)
    assertNotNull(instance)
  }

  @Test
  fun bannerResolution_canBeConstructedViaReflection() {
    val clazz =
        Class.forName(
            "com.github.se.studentconnect.ui.eventcreation.BaseCreateEventViewModel\$BannerResolution")
    val uploadJobClass =
        Class.forName(
            "com.github.se.studentconnect.ui.eventcreation.BaseCreateEventViewModel\$BannerUploadJob")

    val ctor = clazz.getDeclaredConstructor(String::class.java, uploadJobClass)
    ctor.isAccessible = true

    val instance = ctor.newInstance("https://example.com/banner.jpg", null)

    val bannerPath =
        clazz.getDeclaredField("bannerPathForEvent").apply { isAccessible = true }.get(instance)
    assertEquals("https://example.com/banner.jpg", bannerPath)
  }

  // ==================== Update Methods Tests ====================

  @Test
  fun updateTitle_updatesState() {
    val vm = createViewModel()
    vm.updateTitle("New Title")
    assertEquals("New Title", vm.uiState.value.title)
  }

  @Test
  fun updateDescription_updatesState() {
    val vm = createViewModel()
    vm.updateDescription("New Description")
    assertEquals("New Description", vm.uiState.value.description)
  }

  @Test
  fun updateLocation_updatesState() {
    val vm = createViewModel()
    val location = Location(name = "Test Location", latitude = 46.5, longitude = 6.6)
    vm.updateLocation(location)
    assertEquals(location, vm.uiState.value.location)
  }

  @Test
  fun updateLocation_canSetNull() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                location = Location(name = "Test", latitude = 0.0, longitude = 0.0)))
    vm.updateLocation(null)
    assertNull(vm.uiState.value.location)
  }

  @Test
  fun updateStartDate_updatesState() {
    val vm = createViewModel()
    val date = LocalDate.of(2025, 1, 15)
    vm.updateStartDate(date)
    assertEquals(date, vm.uiState.value.startDate)
  }

  @Test
  fun updateStartTime_updatesState() {
    val vm = createViewModel()
    val time = LocalTime.of(14, 30)
    vm.updateStartTime(time)
    assertEquals(time, vm.uiState.value.startTime)
  }

  @Test
  fun updateEndDate_updatesState() {
    val vm = createViewModel()
    val date = LocalDate.of(2025, 1, 16)
    vm.updateEndDate(date)
    assertEquals(date, vm.uiState.value.endDate)
  }

  @Test
  fun updateEndTime_updatesState() {
    val vm = createViewModel()
    val time = LocalTime.of(18, 0)
    vm.updateEndTime(time)
    assertEquals(time, vm.uiState.value.endTime)
  }

  @Test
  fun updateNumberOfParticipantsString_updatesState() {
    val vm = createViewModel()
    vm.updateNumberOfParticipantsString("100")
    assertEquals("100", vm.uiState.value.numberOfParticipantsString)
  }

  @Test
  fun updateHasParticipationFee_updatesState() {
    val vm = createViewModel()
    vm.updateHasParticipationFee(true)
    assertTrue(vm.uiState.value.hasParticipationFee)
  }

  @Test
  fun updateParticipationFeeString_updatesState() {
    val vm = createViewModel()
    vm.updateParticipationFeeString("25.50")
    assertEquals("25.50", vm.uiState.value.participationFeeString)
  }

  @Test
  fun updateIsFlash_updatesState() {
    val vm = createViewModel()
    vm.updateIsFlash(true)
    assertTrue(vm.uiState.value.isFlash)
  }

  // ==================== Flash Duration Tests ====================

  @Test
  fun updateFlashDurationHours_updatesState() {
    val vm = createViewModel()
    vm.updateFlashDurationHours(3)
    assertEquals(3, vm.uiState.value.flashDurationHours)
  }

  @Test
  fun updateFlashDurationHours_clampsToMax() {
    val vm = createViewModel()
    vm.updateFlashDurationHours(10)
    assertTrue(vm.uiState.value.flashDurationHours <= 5)
  }

  @Test
  fun updateFlashDurationHours_clampsToMin() {
    val vm = createViewModel()
    vm.updateFlashDurationHours(-5)
    assertEquals(0, vm.uiState.value.flashDurationHours)
  }

  @Test
  fun updateFlashDurationMinutes_updatesState() {
    val vm = createViewModel()
    vm.updateFlashDurationMinutes(30)
    assertEquals(30, vm.uiState.value.flashDurationMinutes)
  }

  @Test
  fun updateFlashDurationMinutes_clampsToMax() {
    val vm = createViewModel()
    vm.updateFlashDurationMinutes(100)
    assertEquals(59, vm.uiState.value.flashDurationMinutes)
  }

  @Test
  fun updateFlashDurationMinutes_clampsToMin() {
    val vm = createViewModel()
    vm.updateFlashDurationMinutes(-10)
    assertEquals(0, vm.uiState.value.flashDurationMinutes)
  }

  // ==================== Banner Image URI Tests ====================

  @Test
  fun updateBannerImageUri_updatesState() {
    val vm = createViewModel()
    val uri = Uri.parse("content://test/image.jpg")
    vm.updateBannerImageUri(uri)
    assertEquals(uri, vm.uiState.value.bannerImageUri)
    assertFalse(vm.uiState.value.shouldRemoveBanner)
  }

  @Test
  fun removeBannerImage_clearsStateAndSetsRemoveFlag() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                bannerImageUri = Uri.parse("content://test/image.jpg"),
                bannerImagePath = "https://example.com/banner.jpg"))
    vm.removeBannerImage()
    assertNull(vm.uiState.value.bannerImageUri)
    assertNull(vm.uiState.value.bannerImagePath)
    assertTrue(vm.uiState.value.shouldRemoveBanner)
  }

  @Test
  fun resetFinishedSaving_resetsFlags() {
    val vm = createViewModel(CreateEventUiState.Public(finishedSaving = true, isSaving = true))
    vm.resetFinishedSaving()
    assertFalse(vm.uiState.value.finishedSaving)
    assertFalse(vm.uiState.value.isSaving)
  }

  // ==================== Validation Tests ====================

  @Test
  fun validateState_returnsFalseWhenTitleBlank() {
    val vm = createViewModel(CreateEventUiState.Public(title = ""))
    assertFalse(vm.testValidateState())
  }

  @Test
  fun validateState_returnsFalseWhenTitleWhitespaceOnly() {
    val vm = createViewModel(CreateEventUiState.Public(title = "   "))
    assertFalse(vm.testValidateState())
  }

  @Test
  fun validateState_returnsFalseWhenNormalEventWithNoDates() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                title = "Test Event", isFlash = false, startDate = null, endDate = null))
    assertFalse(vm.testValidateState())
  }

  @Test
  fun validateState_returnsFalseWhenNormalEventWithNoStartDate() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                title = "Test Event", isFlash = false, startDate = null, endDate = LocalDate.now()))
    assertFalse(vm.testValidateState())
  }

  @Test
  fun validateState_returnsFalseWhenNormalEventWithNoEndDate() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                title = "Test Event", isFlash = false, startDate = LocalDate.now(), endDate = null))
    assertFalse(vm.testValidateState())
  }

  @Test
  fun validateState_returnsFalseWhenFlashEventWithZeroDuration() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                title = "Test Event",
                isFlash = true,
                flashDurationHours = 0,
                flashDurationMinutes = 0))
    assertFalse(vm.testValidateState())
  }

  @Test
  fun validateState_returnsTrueForValidNormalEvent() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                title = "Test Event",
                isFlash = false,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1)))
    assertTrue(vm.testValidateState())
  }

  @Test
  fun validateState_returnsTrueForValidFlashEvent() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                title = "Test Event",
                isFlash = true,
                flashDurationHours = 1,
                flashDurationMinutes = 30))
    assertTrue(vm.testValidateState())
  }

  @Test
  fun validateState_returnsTrueForFlashEventWithOnlyMinutes() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                title = "Test Event",
                isFlash = true,
                flashDurationHours = 0,
                flashDurationMinutes = 15))
    assertTrue(vm.testValidateState())
  }

  @Test
  fun validateState_returnsTrueForFlashEventWithOnlyHours() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                title = "Test Event",
                isFlash = true,
                flashDurationHours = 2,
                flashDurationMinutes = 0))
    assertTrue(vm.testValidateState())
  }

  // ==================== Flash Duration Calculation Tests ====================

  @Test
  fun calculateFlashDuration_returnsCorrectDurationForFlashEvent() {
    val vm = createViewModel()
    val startTime = System.currentTimeMillis()
    val endTime = startTime + (2 * 60 * 60 * 1000) + (30 * 60 * 1000) // 2h30m

    val event =
        Event.Public(
            uid = "test",
            ownerId = "owner",
            title = "Test",
            description = "",
            imageUrl = null,
            location = null,
            start = Timestamp(java.util.Date(startTime)),
            end = Timestamp(java.util.Date(endTime)),
            maxCapacity = null,
            participationFee = null,
            isFlash = true,
            subtitle = "",
            tags = emptyList(),
            website = null)

    val (hours, minutes) = vm.testCalculateFlashDuration(event)
    assertEquals(2, hours)
    assertEquals(30, minutes)
  }

  @Test
  fun calculateFlashDuration_returnsDefaultForNonFlashEvent() {
    val vm = createViewModel()

    val event =
        Event.Public(
            uid = "test",
            ownerId = "owner",
            title = "Test",
            description = "",
            imageUrl = null,
            location = null,
            start = Timestamp.now(),
            end = null,
            maxCapacity = null,
            participationFee = null,
            isFlash = false,
            subtitle = "",
            tags = emptyList(),
            website = null)

    val (hours, minutes) = vm.testCalculateFlashDuration(event)
    assertEquals(1, hours)
    assertEquals(0, minutes)
  }

  @Test
  fun calculateFlashDuration_handlesNullEndTime() {
    val vm = createViewModel()

    val event =
        Event.Public(
            uid = "test",
            ownerId = "owner",
            title = "Test",
            description = "",
            imageUrl = null,
            location = null,
            start = Timestamp.now(),
            end = null,
            maxCapacity = null,
            participationFee = null,
            isFlash = true,
            subtitle = "",
            tags = emptyList(),
            website = null)

    val (hours, minutes) = vm.testCalculateFlashDuration(event)
    assertEquals(0, hours)
    assertEquals(0, minutes)
  }

  @Test
  fun calculateFlashDuration_handlesShortDuration() {
    val vm = createViewModel()
    val startTime = System.currentTimeMillis()
    val endTime = startTime + (15 * 60 * 1000) // 15 minutes

    val event =
        Event.Public(
            uid = "test",
            ownerId = "owner",
            title = "Test",
            description = "",
            imageUrl = null,
            location = null,
            start = Timestamp(java.util.Date(startTime)),
            end = Timestamp(java.util.Date(endTime)),
            maxCapacity = null,
            participationFee = null,
            isFlash = true,
            subtitle = "",
            tags = emptyList(),
            website = null)

    val (hours, minutes) = vm.testCalculateFlashDuration(event)
    assertEquals(0, hours)
    assertEquals(15, minutes)
  }

  // ==================== Load Event Tests ====================

  @Test
  fun loadEvent_setsEditingEventUid() {
    val vm = createViewModel()
    vm.loadEvent("event-123")

    val field = BaseCreateEventViewModel::class.java.getDeclaredField("editingEventUid")
    field.isAccessible = true
    assertEquals("event-123", field.get(vm))
  }

  // ==================== UI State Flow Tests ====================

  @Test
  fun uiState_initiallyHasDefaultValues() {
    val vm = createViewModel()
    assertEquals("", vm.uiState.value.title)
    assertEquals("", vm.uiState.value.description)
    assertNull(vm.uiState.value.location)
    assertFalse(vm.uiState.value.isFlash)
    assertFalse(vm.uiState.value.isSaving)
    assertFalse(vm.uiState.value.finishedSaving)
  }

  @Test
  fun uiState_canBeInitializedWithCustomValues() {
    val vm =
        createViewModel(
            CreateEventUiState.Public(
                title = "Custom Title",
                description = "Custom Description",
                isFlash = true,
                flashDurationHours = 2,
                flashDurationMinutes = 30))
    assertEquals("Custom Title", vm.uiState.value.title)
    assertEquals("Custom Description", vm.uiState.value.description)
    assertTrue(vm.uiState.value.isFlash)
    assertEquals(2, vm.uiState.value.flashDurationHours)
    assertEquals(30, vm.uiState.value.flashDurationMinutes)
  }

  // ==================== Banner Generation State Tests ====================

  @Test
  fun isGeneratingBanner_initiallyFalse() {
    val vm = createViewModel()
    assertFalse(vm.uiState.value.isGeneratingBanner)
  }

  @Test
  fun isGeneratingBanner_canBeSetToTrue() {
    val vm = createViewModel(CreateEventUiState.Public(isGeneratingBanner = true))
    assertTrue(vm.uiState.value.isGeneratingBanner)
  }

  @Test
  fun `saveEvent sets offline message when offline`() = runTest {
    val appContext = ApplicationProvider.getApplicationContext<Context>()
    val context = mockk<Context>(relaxed = true)
    val connectivityManager = mockk<ConnectivityManager>(relaxed = true)

    every { context.applicationContext } returns appContext
    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { connectivityManager.activeNetwork } returns null

    // Create a valid event state for validation to pass
    val initialState =
        CreateEventUiState.Public(
            title = "Test Event",
            startDate = java.time.LocalDate.now(),
            endDate = java.time.LocalDate.now().plusDays(1),
            startTime = java.time.LocalTime.now(),
            endTime = java.time.LocalTime.now().plusHours(1))

    val vm =
        FakeViewModel(
            initialState = initialState,
            eventRepository = mockk(relaxed = true),
            mediaRepository = mockk(relaxed = true),
            userRepository = mockk(relaxed = true),
            organizationRepository = mockk(relaxed = true),
            friendsRepository = mockk(relaxed = true),
            notificationRepository = mockk(relaxed = true))

    mockkStatic(FirebaseAuth::class)
    val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { FirebaseAuth.getInstance() } returns mockAuth
    every { mockAuth.currentUser } returns mockUser
    every { mockUser.uid } returns "test-user-id"

    vm.saveEvent(context)
    advanceUntilIdle()

    assertEquals(R.string.offline_changes_will_sync, vm.offlineMessageRes.value)
  }

  @Test
  fun `saveEvent clears offline message when online`() = runTest {
    val appContext = ApplicationProvider.getApplicationContext<Context>()
    val context = mockk<Context>(relaxed = true)
    val connectivityManager = mockk<ConnectivityManager>(relaxed = true)
    val network = mockk<Network>(relaxed = true)
    val capabilities = mockk<NetworkCapabilities>(relaxed = true)

    every { context.applicationContext } returns appContext
    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { connectivityManager.activeNetwork } returns network
    every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
    every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

    // Create a valid event state for validation to pass
    val initialState =
        CreateEventUiState.Public(
            title = "Test Event",
            startDate = java.time.LocalDate.now(),
            endDate = java.time.LocalDate.now().plusDays(1),
            startTime = java.time.LocalTime.now(),
            endTime = java.time.LocalTime.now().plusHours(1))

    val vm =
        FakeViewModel(
            initialState = initialState,
            eventRepository = mockk(relaxed = true),
            mediaRepository = mockk(relaxed = true),
            userRepository = mockk(relaxed = true),
            organizationRepository = mockk(relaxed = true),
            friendsRepository = mockk(relaxed = true),
            notificationRepository = mockk(relaxed = true))

    mockkStatic(FirebaseAuth::class)
    val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { FirebaseAuth.getInstance() } returns mockAuth
    every { mockAuth.currentUser } returns mockUser
    every { mockUser.uid } returns "test-user-id"

    vm.saveEvent(context)
    advanceUntilIdle()

    assertNull(vm.offlineMessageRes.value)
  }

  @Test
  fun `clearOfflineMessage clears offline message`() = runTest {
    val appContext = ApplicationProvider.getApplicationContext<Context>()
    val context = mockk<Context>(relaxed = true)
    val connectivityManager = mockk<ConnectivityManager>(relaxed = true)

    every { context.applicationContext } returns appContext
    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { connectivityManager.activeNetwork } returns null

    // Create a valid event state for validation to pass
    val initialState =
        CreateEventUiState.Public(
            title = "Test Event",
            startDate = java.time.LocalDate.now(),
            endDate = java.time.LocalDate.now().plusDays(1),
            startTime = java.time.LocalTime.now(),
            endTime = java.time.LocalTime.now().plusHours(1))

    val vm =
        FakeViewModel(
            initialState = initialState,
            eventRepository = mockk(relaxed = true),
            mediaRepository = mockk(relaxed = true),
            userRepository = mockk(relaxed = true),
            organizationRepository = mockk(relaxed = true),
            friendsRepository = mockk(relaxed = true),
            notificationRepository = mockk(relaxed = true))

    mockkStatic(FirebaseAuth::class)
    val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { FirebaseAuth.getInstance() } returns mockAuth
    every { mockAuth.currentUser } returns mockUser
    every { mockUser.uid } returns "test-user-id"

    // Set offline message by calling saveEvent offline
    vm.saveEvent(context)
    advanceUntilIdle()

    assertEquals(R.string.offline_changes_will_sync, vm.offlineMessageRes.value)

    vm.clearOfflineMessage()

    assertNull(vm.offlineMessageRes.value)
  }
}
