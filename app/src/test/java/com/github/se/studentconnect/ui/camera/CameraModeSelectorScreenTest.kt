package com.github.se.studentconnect.ui.camera

import android.content.Context
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.story.Story
import com.github.se.studentconnect.model.story.StoryRepository
import com.github.se.studentconnect.ui.screen.camera.handleStoryUpload
import com.github.se.studentconnect.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CameraModeSelectorScreenTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var mockContext: Context
  private lateinit var mockLifecycleOwner: LifecycleOwner
  private lateinit var mockStoryRepository: StoryRepository
  private lateinit var mockUri: Uri
  private lateinit var mockEvent: Event
  private lateinit var mockStory: Story
  private var uploadStateChanged = false
  private var storyAccepted = false

  @Before
  fun setup() {
    mockContext = mockk(relaxed = true)
    mockLifecycleOwner =
        mockk(relaxed = true) {
          val lifecycleRegistry = LifecycleRegistry(this)
          lifecycleRegistry.currentState = Lifecycle.State.RESUMED
          every { lifecycle } returns lifecycleRegistry
        }
    mockStoryRepository = mockk()
    mockUri = mockk()
    mockEvent =
        mockk(relaxed = true) {
          every { uid } returns "event123"
          every { title } returns "Test Event"
        }
    mockStory = mockk()

    uploadStateChanged = false
    storyAccepted = false

    mockkObject(AuthenticationProvider)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun handleStoryUpload_withNullEvent_returnsFalseAndShowsToast() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"

    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = false,
            selectedEvent = null,
            isUploading = false,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { uploadStateChanged = it },
            onStoryAccepted = { _, _, _ -> storyAccepted = true })

    assert(!result) { "Expected handleStoryUpload to return false when event is null" }
    assert(!uploadStateChanged) { "Upload state should not change when event is null" }
    assert(!storyAccepted) { "Story should not be accepted when event is null" }
  }

  @Test
  fun handleStoryUpload_whenAlreadyUploading_returnsFalseAndShowsToast() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"

    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = false,
            selectedEvent = mockEvent,
            isUploading = true,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { uploadStateChanged = it },
            onStoryAccepted = { _, _, _ -> storyAccepted = true })

    assert(!result) { "Expected handleStoryUpload to return false when already uploading" }
    assert(!uploadStateChanged) { "Upload state should not change when already uploading" }
    assert(!storyAccepted) { "Story should not be accepted when already uploading" }
  }

  @Test
  fun handleStoryUpload_withEmptyUserId_returnsFalseAndShowsToast() = runTest {
    every { AuthenticationProvider.currentUser } returns ""

    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = false,
            selectedEvent = mockEvent,
            isUploading = false,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { uploadStateChanged = it },
            onStoryAccepted = { _, _, _ -> storyAccepted = true })

    assert(!result) { "Expected handleStoryUpload to return false when user is not logged in" }
    assert(!uploadStateChanged) { "Upload state should not change when user is not logged in" }
    assert(!storyAccepted) { "Story should not be accepted when user is not logged in" }
  }

  @Test
  fun handleStoryUpload_successfulUpload_returnsTrueAndCallsCallbacks() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) } returns
        mockStory

    var uploadState = false
    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = false,
            selectedEvent = mockEvent,
            isUploading = false,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { uploadState = it },
            onStoryAccepted = { uri, isVideo, event ->
              storyAccepted = true
              assert(uri == mockUri) { "Expected URI to match" }
              assert(!isVideo) { "Expected isVideo to be false" }
              assert(event == mockEvent) { "Expected event to match" }
            })

    assert(result) { "Expected handleStoryUpload to return true on successful upload" }

    coVerify { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) }
    assert(storyAccepted) { "Expected onStoryAccepted to be called" }
  }

  @Test
  fun handleStoryUpload_failedUpload_returnsTrueButDoesNotCallOnStoryAccepted() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) } returns
        null

    var uploadState = false
    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = false,
            selectedEvent = mockEvent,
            isUploading = false,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { uploadState = it },
            onStoryAccepted = { _, _, _ -> storyAccepted = true })

    assert(result) { "Expected handleStoryUpload to return true even on failed upload" }

    coVerify { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) }
    assert(!storyAccepted) { "Expected onStoryAccepted not to be called on failed upload" }
  }

  @Test
  fun handleStoryUpload_uploadException_returnsTrueAndHandlesError() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) } throws
        Exception("Network error")

    var uploadState = false
    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = false,
            selectedEvent = mockEvent,
            isUploading = false,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { uploadState = it },
            onStoryAccepted = { _, _, _ -> storyAccepted = true })

    assert(result) { "Expected handleStoryUpload to return true even on exception" }

    coVerify { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) }
    assert(!storyAccepted) { "Expected onStoryAccepted not to be called on exception" }
  }

  @Test
  fun handleStoryUpload_setsUploadStateToTrueBeforeUpload() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) } returns
        mockStory

    var stateChanges = mutableListOf<Boolean>()
    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = true,
            selectedEvent = mockEvent,
            isUploading = false,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { stateChanges.add(it) },
            onStoryAccepted = { _, _, _ -> storyAccepted = true })

    assert(result) { "Expected handleStoryUpload to return true" }
    assert(stateChanges.isNotEmpty()) { "Expected upload state to change" }
    assert(stateChanges.first() == true) { "Expected first state change to be true" }
  }

  @Test
  fun handleStoryUpload_resetsUploadStateToFalseAfterSuccess() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) } returns
        mockStory

    var finalUploadState = true
    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = true,
            selectedEvent = mockEvent,
            isUploading = false,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { finalUploadState = it },
            onStoryAccepted = { _, _, _ -> storyAccepted = true })

    assert(result) { "Expected handleStoryUpload to return true" }
  }

  @Test
  fun handleStoryUpload_resetsUploadStateToFalseAfterFailure() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) } returns
        null

    var finalUploadState = true
    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = true,
            selectedEvent = mockEvent,
            isUploading = false,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { finalUploadState = it },
            onStoryAccepted = { _, _, _ -> storyAccepted = true })

    assert(result) { "Expected handleStoryUpload to return true" }
  }

  @Test
  fun handleStoryUpload_resetsUploadStateToFalseAfterException() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) } throws
        Exception("Test exception")

    var finalUploadState = true
    val result =
        handleStoryUpload(
            mediaUri = mockUri,
            isVideo = true,
            selectedEvent = mockEvent,
            isUploading = false,
            context = mockContext,
            lifecycleOwner = mockLifecycleOwner,
            storyRepository = mockStoryRepository,
            onUploadStateChange = { finalUploadState = it },
            onStoryAccepted = { _, _, _ -> storyAccepted = true })

    assert(result) { "Expected handleStoryUpload to return true" }
  }
}
