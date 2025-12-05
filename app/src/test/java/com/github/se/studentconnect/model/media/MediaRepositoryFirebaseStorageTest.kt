package com.github.se.studentconnect.model.media

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MediaRepositoryFirebaseStorageTest {

  private lateinit var mockStorage: FirebaseStorage
  private lateinit var mockStorageReference: StorageReference
  private lateinit var mockChildReference: StorageReference
  private lateinit var mockUploadTask: UploadTask
  private lateinit var mockUploadSnapshot: UploadTask.TaskSnapshot
  private lateinit var repository: MediaRepositoryFirebaseStorage

  @Before
  fun setUp() {
    mockStorage = mockk(relaxed = true)
    mockStorageReference = mockk(relaxed = true)
    mockChildReference = mockk(relaxed = true)
    mockUploadTask = mockk(relaxed = true)
    mockUploadSnapshot = mockk(relaxed = true)

    every { mockStorage.reference } returns mockStorageReference
    every { mockStorageReference.child(any()) } returns mockChildReference

    repository = MediaRepositoryFirebaseStorage(mockStorage)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `upload returns download URL instead of path`() = runBlocking {
    // Given
    val mockUri = mockk<Uri>(relaxed = true)
    val testPath = "test/path/image.jpg"
    val expectedDownloadUrl =
        "https://firebasestorage.googleapis.com/v0/b/bucket/o/test%2Fpath%2Fimage.jpg?alt=media&token=abc123"

    val downloadUri = mockk<Uri>()
    every { downloadUri.toString() } returns expectedDownloadUrl
    val mockDownloadUrlTask: Task<Uri> = Tasks.forResult(downloadUri)

    every { mockChildReference.putFile(mockUri) } returns mockUploadTask
    every { mockUploadTask.isComplete } returns true
    every { mockUploadTask.exception } returns null
    every { mockUploadTask.isCanceled } returns false
    every { mockUploadTask.result } returns mockUploadSnapshot
    every { mockChildReference.downloadUrl } returns mockDownloadUrlTask

    // When
    val result = repository.upload(mockUri, testPath)

    // Then
    assertEquals(expectedDownloadUrl, result)
    verify { mockChildReference.putFile(mockUri) }
    verify { mockChildReference.downloadUrl }
  }

  @Test
  fun `upload with null path generates automatic path with timestamp`() = runBlocking {
    // Given
    val mockUri = mockk<Uri>(relaxed = true)
    every { mockUri.lastPathSegment } returns "photo.jpg"

    val expectedDownloadUrl =
        "https://firebasestorage.googleapis.com/v0/b/bucket/o/media%2F123456789_photo.jpg?alt=media&token=xyz"
    val downloadUri = mockk<Uri>()
    every { downloadUri.toString() } returns expectedDownloadUrl
    val mockDownloadUrlTask: Task<Uri> = Tasks.forResult(downloadUri)

    every { mockStorageReference.child(any()) } returns mockChildReference
    every { mockChildReference.putFile(mockUri) } returns mockUploadTask
    every { mockUploadTask.isComplete } returns true
    every { mockUploadTask.exception } returns null
    every { mockUploadTask.isCanceled } returns false
    every { mockUploadTask.result } returns mockUploadSnapshot
    every { mockChildReference.downloadUrl } returns mockDownloadUrlTask

    // When
    val result = repository.upload(mockUri, null)

    // Then
    assertEquals(expectedDownloadUrl, result)
    verify {
      mockStorageReference.child(match { it.startsWith("media/") && it.endsWith("_photo.jpg") })
    }
  }

  @Test
  fun `delete with download URL extracts and decodes path correctly`() = runBlocking {
    // Given
    val downloadUrl =
        "https://firebasestorage.googleapis.com/v0/b/bucket/o/stories%2Fevent123%2Fuser456%2F123456789?alt=media&token=abc"
    val expectedPath = "stories/event123/user456/123456789"

    val mockDeleteTask: Task<Void> = Tasks.forResult(null)
    every { mockStorageReference.child(expectedPath) } returns mockChildReference
    every { mockChildReference.delete() } returns mockDeleteTask

    // When
    repository.delete(downloadUrl)

    // Then
    verify { mockStorageReference.child(expectedPath) }
    verify { mockChildReference.delete() }
  }

  @Test
  fun `delete with storage path uses path directly`() = runBlocking {
    // Given
    val storagePath = "stories/event123/user456/123456789"

    val mockDeleteTask: Task<Void> = Tasks.forResult(null)
    every { mockStorageReference.child(storagePath) } returns mockChildReference
    every { mockChildReference.delete() } returns mockDeleteTask

    // When
    repository.delete(storagePath)

    // Then
    verify { mockStorageReference.child(storagePath) }
    verify { mockChildReference.delete() }
  }

  @Test
  fun `delete with malformed URL falls back to using id as path`() = runBlocking {
    // Given
    val malformedUrl = "https://invalid-url-without-path-pattern"

    val mockDeleteTask: Task<Void> = Tasks.forResult(null)
    every { mockStorageReference.child(malformedUrl) } returns mockChildReference
    every { mockChildReference.delete() } returns mockDeleteTask

    // When
    repository.delete(malformedUrl)

    // Then
    verify { mockStorageReference.child(malformedUrl) }
    verify { mockChildReference.delete() }
  }

  @Test
  fun `delete with URL containing special characters decodes correctly`() = runBlocking {
    // Given
    val downloadUrl =
        "https://firebasestorage.googleapis.com/v0/b/bucket/o/path%20with%20spaces%2Ffile%2Bname?alt=media"
    val expectedPath = "path with spaces/file+name"

    val mockDeleteTask: Task<Void> = Tasks.forResult(null)
    every { mockStorageReference.child(expectedPath) } returns mockChildReference
    every { mockChildReference.delete() } returns mockDeleteTask

    // When
    repository.delete(downloadUrl)

    // Then
    verify { mockStorageReference.child(expectedPath) }
    verify { mockChildReference.delete() }
  }

  @Test
  fun `download creates temp file and returns URI`() = runBlocking {
    // Given
    val storageId = "test/path/file.jpg"
    val mockFile = mockk<java.io.File>(relaxed = true)
    val mockDownloadTask = mockk<FileDownloadTask>(relaxed = true)

    mockkStatic(java.io.File::class)
    every { java.io.File.createTempFile(any(), any()) } returns mockFile
    every { mockStorageReference.child(storageId) } returns mockChildReference
    every { mockChildReference.getFile(mockFile) } returns mockDownloadTask
    every { mockDownloadTask.isComplete } returns true
    every { mockDownloadTask.exception } returns null
    every { mockDownloadTask.isCanceled } returns false

    mockkStatic(Uri::class)
    val expectedUri = mockk<Uri>()
    every { Uri.fromFile(mockFile) } returns expectedUri

    // When
    val result = repository.download(storageId)

    // Then
    assertEquals(expectedUri, result)
    verify { mockStorageReference.child(storageId) }
    verify { mockChildReference.getFile(mockFile) }

    unmockkStatic(java.io.File::class)
    unmockkStatic(Uri::class)
  }
}
