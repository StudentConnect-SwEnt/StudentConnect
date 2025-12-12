package com.github.se.studentconnect.service

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ImageUploadWorkerTest {

  @Test
  fun `doWork fails fast when file missing`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inputData =
        Data.Builder()
            .putString(ImageUploadWorker.KEY_FILE_PATH, "/path/does/not/exist.png")
            .putString(ImageUploadWorker.KEY_STORAGE_PATH, "storage/path")
            .putString(ImageUploadWorker.KEY_COLLECTION_PATH, "collection")
            .putString(ImageUploadWorker.KEY_DOCUMENT_ID, "doc-1")
            .putString(ImageUploadWorker.KEY_FIELD_NAME, "field")
            .build()

    val params = mockk<WorkerParameters>(relaxed = true)
    every { params.inputData } returns inputData

    val worker = ImageUploadWorker(context, params)

    val result = worker.doWork()

    assertTrue(result is ListenableWorker.Result.Failure)
  }

  @Test
  fun `doWork succeeds when upload and firestore succeed`() = runTest {
    mockkStatic(FirebaseStorage::class)
    mockkStatic(FirebaseFirestore::class)
    mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    try {
      val context = ApplicationProvider.getApplicationContext<Context>()
      val tempFile =
          kotlin.io.path.createTempFile("upload_worker", ".txt").toFile().apply {
            writeText("data")
          }

      // Build input data with existing file
      val inputData =
          Data.Builder()
              .putString(ImageUploadWorker.KEY_FILE_PATH, tempFile.absolutePath)
              .putString(ImageUploadWorker.KEY_STORAGE_PATH, "storage/path")
              .putString(ImageUploadWorker.KEY_COLLECTION_PATH, "collection")
              .putString(ImageUploadWorker.KEY_DOCUMENT_ID, "doc-1")
              .putString(ImageUploadWorker.KEY_FIELD_NAME, "field")
              .build()

      val params = mockk<WorkerParameters>(relaxed = true)
      every { params.inputData } returns inputData

      // Mock Firebase Storage interactions
      val storage = mockk<FirebaseStorage>()
      val storageRef = mockk<StorageReference>(relaxed = true)
      val uploadTask = mockk<UploadTask>(relaxed = true)
      val uploadSnapshot = mockk<UploadTask.TaskSnapshot>(relaxed = true)
      val downloadTask = mockk<Task<Uri>>(relaxed = true)

      every { FirebaseStorage.getInstance() } returns storage
      every { storage.reference } returns storageRef
      every { storageRef.child(any()) } returns storageRef
      every { storageRef.putFile(any()) } returns uploadTask
      coEvery { uploadTask.await() } returns uploadSnapshot
      every { storageRef.downloadUrl } returns downloadTask
      coEvery { downloadTask.await() } returns Uri.parse("https://example.com/file.jpg")

      // Mock Firestore interactions
      val firestore = mockk<FirebaseFirestore>()
      val collection = mockk<CollectionReference>()
      val doc = mockk<DocumentReference>()
      val setTask = mockk<Task<Void>>(relaxed = true)
      every { FirebaseFirestore.getInstance() } returns firestore
      every { firestore.collection(any()) } returns collection
      every { collection.document(any()) } returns doc
      every { doc.set(any<Map<String, Any?>>(), any()) } returns setTask
      coEvery { setTask.await() } returns mockk()

      val worker = ImageUploadWorker(context, params)

      val result = worker.doWork()

      assertTrue(result is ListenableWorker.Result.Success)
      assertTrue(!tempFile.exists())
    } finally {
      unmockkAll()
    }
  }
}
