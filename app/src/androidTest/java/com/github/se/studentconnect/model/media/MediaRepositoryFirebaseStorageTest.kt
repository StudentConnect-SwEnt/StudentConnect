// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.media

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.utils.StudentConnectTest
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for [MediaRepositoryFirebaseStorage] using the Firebase Emulator.
 *
 * These tests verify that media files can be uploaded, downloaded, and deleted correctly. The
 * Firebase Emulator must be running (enforced by [StudentConnectTest]).
 */
class MediaRepositoryFirebaseStorageTest : StudentConnectTest() {

  private val repo = MediaRepositoryProvider.repository
  private lateinit var tempFile: File
  private var uploadedId: String? = null

  @Before
  override fun setUp() {
    super.setUp()

    // Create a small text file to upload.
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    tempFile = File(context.cacheDir, "test_upload_${System.currentTimeMillis()}.txt")
    tempFile.writeText("Hello StudentConnect! This is a media repository test file.")
  }

  @After
  override fun tearDown() {
    // Clean up uploaded file if it exists
    runTest {
      uploadedId?.let {
        try {
          repo.delete(it)
        } catch (_: Exception) {
          // ignore missing/deleted file
        }
      }
    }
    tempFile.delete()
    super.tearDown()
  }

  @Test
  fun upload_and_download_should_preserve_file_content() = runTest {
    // Given a local file
    val uri = Uri.fromFile(tempFile)

    // When uploading it
    uploadedId = repo.upload(uri)
    assertNotNull("Upload ID should not be null", uploadedId)
    assertTrue("Upload ID should not be empty", uploadedId!!.isNotEmpty())

    // When downloading it
    val downloadedUri = repo.download(uploadedId!!)
    val downloadedFile = File(downloadedUri.path!!)
    assertTrue("Downloaded file should exist", downloadedFile.exists())

    // Then the file content should match
    val original = tempFile.readText()
    val downloaded = downloadedFile.readText()
    assertEquals("File contents should match after upload and download", original, downloaded)
  }

  @Test
  fun delete_should_remove_file_from_storage() = runTest {
    // Given an uploaded file
    val uri = Uri.fromFile(tempFile)
    uploadedId = repo.upload(uri)
    assertNotNull("Upload ID should not be null", uploadedId)

    // When deleting it
    repo.delete(uploadedId!!)

    // Then attempting to download it should throw
    var threw = false
    try {
      repo.download(uploadedId!!)
    } catch (e: Exception) {
      threw = true
    }
    assertTrue("Download should throw after file is deleted", threw)
  }

  @Test
  fun upload_with_custom_path_should_use_that_path() = runTest {
    // Given a custom storage path
    val uri = Uri.fromFile(tempFile)
    val customPath = "test/custom/${System.currentTimeMillis()}_file.txt"

    // When uploading
    uploadedId = repo.upload(uri, customPath)

    // Then the returned path should contain the custom path
    assertNotNull("Upload ID should not be null", uploadedId)
    assertTrue(
        "Returned path should contain custom path segment", uploadedId!!.contains("test/custom/"))

    // Clean up
    repo.delete(uploadedId!!)
  }
}
