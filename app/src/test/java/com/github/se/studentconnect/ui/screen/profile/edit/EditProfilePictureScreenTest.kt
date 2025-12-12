package com.github.se.studentconnect.ui.screen.profile.edit

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.WorkManager
import com.github.se.studentconnect.service.ImageUploadWorker
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfilePictureScreenTest {

  private val appContext = ApplicationProvider.getApplicationContext<Context>()

  @Before
  fun setup() {
    mockkStatic(WorkManager::class)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `stageProfilePicture copies file and returns path`() = runTest {
    val tmpDir = File(appContext.filesDir, "test_stage").apply { mkdirs() }
    val mockContext = mockk<Context>(relaxed = true)
    val mockResolver = mockk<ContentResolver>()
    val uri = Uri.parse("content://test/image")
    val data = "hello world".toByteArray()

    every { mockContext.filesDir } returns tmpDir
    every { mockContext.contentResolver } returns mockResolver
    every { mockResolver.getType(uri) } returns "image/png"
    every { mockResolver.openInputStream(uri) } returns ByteArrayInputStream(data)

    val path = stageProfilePicture(mockContext, uri, "user-1")

    assertNotNull(path)
    val copied = File(path!!)
    assertTrue(copied.exists())
    assertEquals("image/png", mockResolver.getType(uri))
    assertEquals(data.size.toLong(), copied.length())
  }

  @Test
  fun `enqueueProfilePictureUpload builds work with correct input`() {
    val mockContext = mockk<Context>(relaxed = true)
    val mockWorkManager = mockk<WorkManager>(relaxed = true)
    val workSlot = slot<androidx.work.OneTimeWorkRequest>()

    every { WorkManager.getInstance(mockContext) } returns mockWorkManager
    every { mockWorkManager.enqueueUniqueWork(any(), any(), capture(workSlot)) } returns
        mockk(relaxed = true)

    enqueueProfilePictureUpload(
        context = mockContext,
        userId = "user-123",
        filePath = "/tmp/file.jpg",
        storagePath = "users/user-123/profile",
        existingImageUrl = "old/url")

    val input: Data = workSlot.captured.workSpec.input
    assertEquals("user-123", input.getString(ImageUploadWorker.KEY_DOCUMENT_ID))
    assertEquals("/tmp/file.jpg", input.getString(ImageUploadWorker.KEY_FILE_PATH))
    assertEquals("users/user-123/profile", input.getString(ImageUploadWorker.KEY_STORAGE_PATH))
    assertEquals("old/url", input.getString(ImageUploadWorker.KEY_EXISTING_IMAGE_URL))
    assertEquals("users", input.getString(ImageUploadWorker.KEY_COLLECTION_PATH))
    assertEquals("profilePictureUrl", input.getString(ImageUploadWorker.KEY_FIELD_NAME))
  }
}
