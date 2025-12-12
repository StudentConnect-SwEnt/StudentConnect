package com.github.se.studentconnect.ui.screen.profile.edit

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.github.se.studentconnect.service.ImageUploadWorker
import com.github.se.studentconnect.ui.profile.edit.EditProfilePictureViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class EditProfilePictureScreenTest {

  @Test
  fun `stageProfilePicture copies file and returns path`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val tmpDir = Files.createTempDirectory("profile_stage_test").toFile()
    val source = File(tmpDir, "source.png").apply { writeText("hello world") }
    val uri = Uri.fromFile(source)

    val path =
        stageProfilePicture(
            context = context,
            uri = uri,
            userId = "user-1",
            contentResolver = context.contentResolver,
            filesDir = tmpDir)

    assertNotNull(path)
    val copied = File(path!!)
    assertTrue(copied.exists())
    assertEquals(source.length(), copied.length())
  }

  @Test
  fun `handleStagedProfilePicture enqueues work when staging succeeds`() = runTest {
    val tmpDir = Files.createTempDirectory("profile_stage_handle_test").toFile()
    val source = File(tmpDir, "source.jpg").apply { writeText("data") }
    val uri = Uri.fromFile(source)
    val mockContext = mockk<Context>(relaxed = true)
    val mockResolver = mockk<ContentResolver>()
    val mockViewModel = mockk<EditProfilePictureViewModel>(relaxed = true)
    val mockWorkManager = mockk<WorkManager>(relaxed = true)
    val workSlot = slot<androidx.work.OneTimeWorkRequest>()

    every { mockContext.filesDir } returns tmpDir
    every { mockContext.contentResolver } returns mockResolver
    every { mockResolver.getType(uri) } returns "image/jpeg"
    every { mockResolver.openInputStream(uri) } answers { source.inputStream() }
    every { mockWorkManager.enqueueUniqueWork(any(), any(), capture(workSlot)) } returns
        mockk(relaxed = true)

    val localUrl =
        handleStagedProfilePicture(
            context = mockContext,
            userId = "user-1",
            uri = uri,
            storagePath = "users/user-1/profile",
            existingImageUrl = "old",
            viewModel = mockViewModel,
            workManager = mockWorkManager)

    assertNotNull(localUrl)
    verify { mockViewModel.updateProfilePicture(match { it.startsWith("file://") }) }
    assertEquals(
        "users/user-1/profile",
        workSlot.captured.workSpec.input.getString(ImageUploadWorker.KEY_STORAGE_PATH))
  }

  @Test
  fun `enqueueProfilePictureUpload builds work with correct input`() {
    val mockContext = mockk<Context>(relaxed = true)
    val mockWorkManager = mockk<WorkManager>(relaxed = true)
    val workSlot = slot<androidx.work.OneTimeWorkRequest>()

    every {
      mockWorkManager.enqueueUniqueWork(
          "profile_picture_upload_user-123", ExistingWorkPolicy.REPLACE, capture(workSlot))
    } returns mockk(relaxed = true)

    enqueueProfilePictureUpload(
        context = mockContext,
        userId = "user-123",
        filePath = "/tmp/file.jpg",
        storagePath = "users/user-123/profile",
        existingImageUrl = "old/url",
        workManager = mockWorkManager)

    val input: Data = workSlot.captured.workSpec.input
    assertEquals("user-123", input.getString(ImageUploadWorker.KEY_DOCUMENT_ID))
    assertEquals("/tmp/file.jpg", input.getString(ImageUploadWorker.KEY_FILE_PATH))
    assertEquals("users/user-123/profile", input.getString(ImageUploadWorker.KEY_STORAGE_PATH))
    assertEquals("old/url", input.getString(ImageUploadWorker.KEY_EXISTING_IMAGE_URL))
    assertEquals("users", input.getString(ImageUploadWorker.KEY_COLLECTION_PATH))
    assertEquals("profilePictureUrl", input.getString(ImageUploadWorker.KEY_FIELD_NAME))
  }
}
