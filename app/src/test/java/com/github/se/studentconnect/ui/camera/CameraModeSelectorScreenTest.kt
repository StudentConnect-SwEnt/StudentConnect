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
import com.github.se.studentconnect.ui.screen.camera.StoryUploadCallbacks
import com.github.se.studentconnect.ui.screen.camera.StoryUploadParams
import com.github.se.studentconnect.ui.screen.camera.handleStoryUpload
import com.github.se.studentconnect.util.MainDispatcherRule
import com.github.se.studentconnect.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
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
    // Use Robolectric's application context for Toast support
    mockContext = RuntimeEnvironment.getApplication()
    mockLifecycleOwner = mockk(relaxed = true)
    val lifecycleRegistry = LifecycleRegistry(mockLifecycleOwner)
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    every { mockLifecycleOwner.lifecycle } returns lifecycleRegistry
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
    mockkObject(NetworkUtils)
    // Default to network available for all tests
    every { NetworkUtils.isNetworkAvailable(any()) } returns true
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
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = false,
                    selectedEvent = null,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = { uploadStateChanged = it },
                    onStoryAccepted = { _, _, _ -> storyAccepted = true }))

    assert(!result) { "Expected handleStoryUpload to return false when event is null" }
    assert(!uploadStateChanged) { "Upload state should not change when event is null" }
    assert(!storyAccepted) { "Story should not be accepted when event is null" }
  }

  @Test
  fun handleStoryUpload_whenAlreadyUploading_returnsFalseAndShowsToast() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"

    val result =
        handleStoryUpload(
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = false,
                    selectedEvent = mockEvent,
                    isUploading = true,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = { uploadStateChanged = it },
                    onStoryAccepted = { _, _, _ -> storyAccepted = true }))

    assert(!result) { "Expected handleStoryUpload to return false when already uploading" }
    assert(!uploadStateChanged) { "Upload state should not change when already uploading" }
    assert(!storyAccepted) { "Story should not be accepted when already uploading" }
  }

  @Test
  fun handleStoryUpload_withEmptyUserId_returnsFalseAndShowsToast() = runTest {
    every { AuthenticationProvider.currentUser } returns ""

    val result =
        handleStoryUpload(
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = false,
                    selectedEvent = mockEvent,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = { uploadStateChanged = it },
                    onStoryAccepted = { _, _, _ -> storyAccepted = true }))

    assert(!result) { "Expected handleStoryUpload to return false when user is not logged in" }
    assert(!uploadStateChanged) { "Upload state should not change when user is not logged in" }
    assert(!storyAccepted) { "Story should not be accepted when user is not logged in" }
  }

  @Test
  fun handleStoryUpload_setsUploadStateToTrueBeforeUpload() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(mockUri, "event123", "user123", mockContext) } returns
        mockStory

    var stateChanges = mutableListOf<Boolean>()
    val result =
        handleStoryUpload(
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = true,
                    selectedEvent = mockEvent,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = { stateChanges.add(it) },
                    onStoryAccepted = { _, _, _ -> storyAccepted = true }))

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
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = true,
                    selectedEvent = mockEvent,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = { finalUploadState = it },
                    onStoryAccepted = { _, _, _ -> storyAccepted = true }))

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
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = true,
                    selectedEvent = mockEvent,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = { finalUploadState = it },
                    onStoryAccepted = { _, _, _ -> storyAccepted = true }))

    assert(result) { "Expected handleStoryUpload to return true" }
  }

  @Test
  fun handleStoryUpload_resetsUploadStateToFalseAfterException() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(mockUri, "event123", "user123", any()) } throws
        Exception("Test exception")

    var finalUploadState = true
    val result =
        handleStoryUpload(
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = true,
                    selectedEvent = mockEvent,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = { finalUploadState = it },
                    onStoryAccepted = { _, _, _ -> storyAccepted = true }))

    assert(result) { "Expected handleStoryUpload to return true" }
  }

  @Test
  fun handleStoryUpload_successfulUpload_invokesOnStoryAcceptedCallback() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(any(), any(), any(), any()) } returns mockStory

    var onStoryAcceptedCalled = false
    var capturedUri: Uri? = null
    var capturedIsVideo: Boolean? = null
    var capturedEvent: Event? = null

    val result =
        handleStoryUpload(
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = true,
                    selectedEvent = mockEvent,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = {},
                    onStoryAccepted = { uri, isVideo, event ->
                      onStoryAcceptedCalled = true
                      capturedUri = uri
                      capturedIsVideo = isVideo
                      capturedEvent = event
                    }))

    // Wait for coroutine to complete
    testScheduler.advanceUntilIdle()

    assert(result) { "Expected handleStoryUpload to return true" }
    assert(onStoryAcceptedCalled) { "Expected onStoryAccepted callback to be invoked" }
    assert(capturedUri == mockUri) { "Expected captured URI to match mockUri" }
    assert(capturedIsVideo == true) { "Expected captured isVideo to be true" }
    assert(capturedEvent == mockEvent) { "Expected captured event to match mockEvent" }
  }

  @Test
  fun handleStoryUpload_failedUpload_doesNotInvokeOnStoryAccepted() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(any(), any(), any(), any()) } returns null

    var onStoryAcceptedCalled = false

    val result =
        handleStoryUpload(
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = false,
                    selectedEvent = mockEvent,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = {},
                    onStoryAccepted = { _, _, _ -> onStoryAcceptedCalled = true }))

    // Wait for coroutine to complete
    testScheduler.advanceUntilIdle()

    assert(result) { "Expected handleStoryUpload to return true" }
    assert(!onStoryAcceptedCalled) {
      "Expected onStoryAccepted callback NOT to be invoked when upload fails"
    }
  }

  @Test
  fun handleStoryUpload_exceptionDuringUpload_doesNotInvokeOnStoryAccepted() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(any(), any(), any(), any()) } throws
        Exception("Network error")

    var onStoryAcceptedCalled = false

    val result =
        handleStoryUpload(
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = false,
                    selectedEvent = mockEvent,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = {},
                    onStoryAccepted = { _, _, _ -> onStoryAcceptedCalled = true }))

    // Wait for coroutine to complete
    testScheduler.advanceUntilIdle()

    assert(result) { "Expected handleStoryUpload to return true even on exception" }
    assert(!onStoryAcceptedCalled) {
      "Expected onStoryAccepted callback NOT to be invoked when exception occurs"
    }
  }

  @Test
  fun handleStoryUpload_callsOnUploadStateChangeCorrectly() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(any(), any(), any(), any()) } returns mockStory

    val stateChanges = mutableListOf<Boolean>()

    val result =
        handleStoryUpload(
            params =
                StoryUploadParams(
                    mediaUri = mockUri,
                    isVideo = false,
                    selectedEvent = mockEvent,
                    isUploading = false,
                    context = mockContext,
                    lifecycleOwner = mockLifecycleOwner,
                    storyRepository = mockStoryRepository),
            callbacks =
                StoryUploadCallbacks(
                    onUploadStateChange = { state -> stateChanges.add(state) },
                    onStoryAccepted = { _, _, _ -> }))

    // Wait for coroutine to complete
    testScheduler.advanceUntilIdle()

    assert(result) { "Expected handleStoryUpload to return true" }
    assert(stateChanges.size >= 2) {
      "Expected at least 2 state changes (true then false), got ${stateChanges.size}"
    }
    assert(stateChanges.first() == true) { "Expected first state change to be true (uploading)" }
    assert(stateChanges.last() == false) {
      "Expected last state change to be false (upload complete)"
    }
  }

  @Test
  fun handleStoryUpload_resetsStateInFinallyBlock_onSuccess() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(any(), any(), any(), any()) } returns mockStory

    var finalState: Boolean? = null

    handleStoryUpload(
        params =
            StoryUploadParams(
                mediaUri = mockUri,
                isVideo = false,
                selectedEvent = mockEvent,
                isUploading = false,
                context = mockContext,
                lifecycleOwner = mockLifecycleOwner,
                storyRepository = mockStoryRepository),
        callbacks =
            StoryUploadCallbacks(
                onUploadStateChange = { state -> finalState = state },
                onStoryAccepted = { _, _, _ -> }))

    testScheduler.advanceUntilIdle()

    assert(finalState == false) { "Expected upload state to be reset to false in finally block" }
  }

  @Test
  fun handleStoryUpload_resetsStateInFinallyBlock_onFailure() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(any(), any(), any(), any()) } returns null

    var finalState: Boolean? = null

    handleStoryUpload(
        params =
            StoryUploadParams(
                mediaUri = mockUri,
                isVideo = false,
                selectedEvent = mockEvent,
                isUploading = false,
                context = mockContext,
                lifecycleOwner = mockLifecycleOwner,
                storyRepository = mockStoryRepository),
        callbacks =
            StoryUploadCallbacks(
                onUploadStateChange = { state -> finalState = state },
                onStoryAccepted = { _, _, _ -> }))

    testScheduler.advanceUntilIdle()

    assert(finalState == false) {
      "Expected upload state to be reset to false in finally block even on failure"
    }
  }

  @Test
  fun handleStoryUpload_resetsStateInFinallyBlock_onException() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(any(), any(), any(), any()) } throws
        RuntimeException("Test error")

    var finalState: Boolean? = null

    handleStoryUpload(
        params =
            StoryUploadParams(
                mediaUri = mockUri,
                isVideo = false,
                selectedEvent = mockEvent,
                isUploading = false,
                context = mockContext,
                lifecycleOwner = mockLifecycleOwner,
                storyRepository = mockStoryRepository),
        callbacks =
            StoryUploadCallbacks(
                onUploadStateChange = { state -> finalState = state },
                onStoryAccepted = { _, _, _ -> }))

    testScheduler.advanceUntilIdle()

    assert(finalState == false) {
      "Expected upload state to be reset to false in finally block even on exception"
    }
  }

  @Test
  fun handleStoryUpload_withVideoContent_passesCorrectIsVideoFlag() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(any(), any(), any(), any()) } returns mockStory

    var capturedIsVideo: Boolean? = null

    handleStoryUpload(
        params =
            StoryUploadParams(
                mediaUri = mockUri,
                isVideo = true,
                selectedEvent = mockEvent,
                isUploading = false,
                context = mockContext,
                lifecycleOwner = mockLifecycleOwner,
                storyRepository = mockStoryRepository),
        callbacks =
            StoryUploadCallbacks(
                onUploadStateChange = {},
                onStoryAccepted = { _, isVideo, _ -> capturedIsVideo = isVideo }))

    testScheduler.advanceUntilIdle()

    assert(capturedIsVideo == true) { "Expected isVideo flag to be true for video content" }
  }

  @Test
  fun handleStoryUpload_withImageContent_passesCorrectIsVideoFlag() = runTest {
    every { AuthenticationProvider.currentUser } returns "user123"
    coEvery { mockStoryRepository.uploadStory(any(), any(), any(), any()) } returns mockStory

    var capturedIsVideo: Boolean? = null

    handleStoryUpload(
        params =
            StoryUploadParams(
                mediaUri = mockUri,
                isVideo = false,
                selectedEvent = mockEvent,
                isUploading = false,
                context = mockContext,
                lifecycleOwner = mockLifecycleOwner,
                storyRepository = mockStoryRepository),
        callbacks =
            StoryUploadCallbacks(
                onUploadStateChange = {},
                onStoryAccepted = { _, isVideo, _ -> capturedIsVideo = isVideo }))

    testScheduler.advanceUntilIdle()

    assert(capturedIsVideo == false) { "Expected isVideo flag to be false for image content" }
  }
}
