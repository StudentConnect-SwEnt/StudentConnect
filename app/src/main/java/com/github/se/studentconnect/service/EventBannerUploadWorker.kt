// Portions of this code were generated with the help of ChatGPT
package com.github.se.studentconnect.service

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.se.studentconnect.model.event.EventRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlinx.coroutines.tasks.await

/** Uploads a staged event banner to Firebase Storage and patches the event document. */
class EventBannerUploadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result {
    val eventUid = inputData.getString(KEY_EVENT_UID) ?: return Result.failure()
    val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
    val storagePath = inputData.getString(KEY_STORAGE_PATH) ?: return Result.failure()
    val existingImageUrl = inputData.getString(KEY_EXISTING_IMAGE_URL)

    val file = File(filePath)
    if (!file.exists()) return Result.failure()

    return try {
      val storage = FirebaseStorage.getInstance().reference.child(storagePath)
      storage.putFile(file.toUri()).await()
      val downloadUrl = storage.downloadUrl.await().toString()

      FirebaseFirestore.getInstance()
          .collection(EventRepositoryFirestore.EVENTS_COLLECTION_PATH)
          .document(eventUid)
          .set(mapOf("imageUrl" to downloadUrl), SetOptions.merge())
          .await()

      // Best-effort cleanup of previous image
      if (!existingImageUrl.isNullOrBlank()) {
        runCatching {
          val ref =
              if (existingImageUrl.startsWith("http")) {
                FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl)
              } else {
                FirebaseStorage.getInstance().reference.child(existingImageUrl)
              }
          ref.delete().await()
        }
      }

      file.delete()
      Result.success()
    } catch (e: Exception) {
      Log.w(TAG, "Banner upload failed for $eventUid, will retry", e)
      Result.retry()
    }
  }

  companion object {
    const val KEY_EVENT_UID = "event_uid"
    const val KEY_FILE_PATH = "file_path"
    const val KEY_STORAGE_PATH = "storage_path"
    const val KEY_EXISTING_IMAGE_URL = "existing_image_url"
    private const val TAG = "EventBannerUploadWorker"
  }
}
