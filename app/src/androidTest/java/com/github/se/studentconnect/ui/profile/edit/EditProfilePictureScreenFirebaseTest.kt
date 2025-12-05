package com.github.se.studentconnect.ui.profile.edit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryFirestore
import com.github.se.studentconnect.ui.screen.profile.edit.EditProfilePictureScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.StudentConnectTest
import com.github.se.studentconnect.utils.UI_WAIT_TIMEOUT
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayDeque
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Minimal Compose test that renders [EditProfilePictureScreen] with the real Firebase-backed
 * repositories using the emulator, then clicks on the photo area (to launch the picker) — nothing
 * more, mirroring the lightweight approach of [CreatePrivateEventScreenTest].
 */
class EditProfilePictureScreenFirebaseTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var registryOwner: QueueingActivityResultRegistryOwner
  private lateinit var tempImageFile: File
  private lateinit var tempImageUri: Uri
  private lateinit var testUser: User

  private val uploadedIds = mutableListOf<String>()

  @Before
  override fun setUp() {
    super.setUp()
    userRepository = UserRepositoryFirestore(FirebaseEmulator.firestore)

    val uid = currentUser.uid
    testUser =
        User(
            userId = uid,
            email = currentUser.email ?: "$uid@studentconnect.test",
            firstName = "Edit",
            lastName = "Picture",
            university = "EPFL",
            hobbies = listOf("Testing"),
            username = "edit_picture_user")

    runTest { userRepository.saveUser(testUser) }

    tempImageFile = createTempImageFile()
    tempImageUri = Uri.fromFile(tempImageFile)

    registryOwner = QueueingActivityResultRegistryOwner()

    composeTestRule.setContent {
      CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
        AppTheme {
          EditProfilePictureScreen(
              userId = testUser.userId, userRepository = userRepository, onNavigateBack = {})
        }
      }
    }
  }

  @After
  override fun tearDown() {
    val mediaRepository = MediaRepositoryProvider.repository
    runTest {
      uploadedIds.forEach { path -> runCatching { mediaRepository.delete(path) } }
      runCatching { userRepository.deleteUser(testUser.userId) }
    }
    tempImageFile.delete()
    super.tearDown()
  }

  @Test
  fun tappingProfilePicture_launchesPicker() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    registryOwner.enqueueResult(tempImageUri)
    composeTestRule
        .onNodeWithContentDescription(ctx.getString(R.string.content_description_profile_picture))
        .assertExists()
        .performClick()
    selectGalleryOption()
  }

  @Test
  fun saveButton_becomesClickableAfterSelectingPhoto() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    // Wait for the screen to finish loading before checking for the Save button
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = UI_WAIT_TIMEOUT) {
      composeTestRule
          .onAllNodesWithText(ctx.getString(R.string.button_save))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    val saveButton = composeTestRule.onNodeWithText(ctx.getString(R.string.button_save))
    saveButton.assertExists().assertIsNotEnabled()

    registryOwner.enqueueResult(tempImageUri)
    composeTestRule
        .onNodeWithContentDescription(ctx.getString(R.string.content_description_profile_picture))
        .performClick()
    selectGalleryOption()

    composeTestRule.waitForIdle()
    saveButton.assertIsEnabled().assertHasClickAction()

    saveButton.performClick()

    composeTestRule.waitUntil(timeoutMillis = UI_WAIT_TIMEOUT) {
      val path =
          runCatching {
                runBlocking { userRepository.getUserById(testUser.userId)?.profilePictureUrl }
              }
              .getOrNull()
      !path.isNullOrBlank()
    }

    runTest {
      val updatedUser = userRepository.getUserById(testUser.userId)
      val uploadedPath = requireNotNull(updatedUser?.profilePictureUrl)
      uploadedIds += uploadedPath
    }
  }

  @Test
  fun removePhotoButton_clearsSelectionAndDisablesItself() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    val removeButton = composeTestRule.onNodeWithText(ctx.getString(R.string.button_remove_photo))
    removeButton.assertExists().assertIsNotEnabled()

    registryOwner.enqueueResult(tempImageUri)
    composeTestRule
        .onNodeWithContentDescription(ctx.getString(R.string.content_description_profile_picture))
        .performClick()
    selectGalleryOption()
    composeTestRule.waitForIdle()

    removeButton.assertIsEnabled().performClick()

    removeButton.assertIsNotEnabled()
    composeTestRule
        .onNodeWithText(ctx.getString(R.string.instruction_choose_profile_photo))
        .assertExists()
  }

  @Test
  fun takePhotoOption_enablesSaveButton() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    val saveButton = composeTestRule.onNodeWithText(ctx.getString(R.string.button_save))

    // Wait for button to appear
    composeTestRule.waitUntil(timeoutMillis = UI_WAIT_TIMEOUT) {
      composeTestRule
          .onAllNodesWithText(ctx.getString(R.string.button_save))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    saveButton.assertExists().assertIsNotEnabled()

    // Permission result + TakePicture result
    registryOwner.enqueueResult(true)
    registryOwner.enqueueResult(true)

    composeTestRule
        .onNodeWithContentDescription(ctx.getString(R.string.content_description_profile_picture))
        .performClick()
    selectTakePhotoOption()
    composeTestRule.waitForIdle()

    saveButton.assertIsEnabled()
  }

  private fun createTempImageFile(): File {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val file = File(context.cacheDir, "profile_${UUID.randomUUID()}.png")
    val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).drawColor(Color.CYAN)
    FileOutputStream(file).use { stream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) }
    bitmap.recycle()
    return file
  }

  private fun selectGalleryOption() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    composeTestRule
        .onNodeWithText(ctx.getString(R.string.button_choose_from_gallery))
        .assertExists()
        .performClick()
  }

  private fun selectTakePhotoOption() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    composeTestRule
        .onNodeWithText(ctx.getString(R.string.button_take_photo))
        .assertExists()
        .performClick()
  }
}

private class QueueingActivityResultRegistryOwner : ActivityResultRegistryOwner {
  private val registry = QueueingActivityResultRegistry()

  override val activityResultRegistry: ActivityResultRegistry
    get() = registry

  fun enqueueResult(result: Any?) {
    registry.enqueueResult(result)
  }
}

private class QueueingActivityResultRegistry : ActivityResultRegistry() {
  private val queuedResults = ArrayDeque<Any?>()

  fun enqueueResult(result: Any?) {
    queuedResults.addLast(result)
  }

  override fun <I, O> onLaunch(
      requestCode: Int,
      contract: ActivityResultContract<I, O>,
      input: I,
      options: ActivityOptionsCompat?
  ) {
    check(queuedResults.isNotEmpty()) { "No queued ActivityResult for EditProfilePictureScreen." }
    @Suppress("UNCHECKED_CAST") val result = queuedResults.removeFirst() as O
    dispatchResult(requestCode, result)
  }
}
