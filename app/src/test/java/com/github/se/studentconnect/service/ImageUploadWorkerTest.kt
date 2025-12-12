package com.github.se.studentconnect.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
}
