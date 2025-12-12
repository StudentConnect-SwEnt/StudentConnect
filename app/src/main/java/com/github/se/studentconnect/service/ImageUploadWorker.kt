package com.github.se.studentconnect.service

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlinx.coroutines.tasks.await

/**
 * Generic worker to upload an image to Firebase Storage and patch a Firestore document field.
 *
 * Used for both event banners and user profile pictures.
 */
class ImageUploadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result {
    val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
    val storagePath = inputData.getString(KEY_STORAGE_PATH) ?: return Result.failure()
    val existingImageUrl = inputData.getString(KEY_EXISTING_IMAGE_URL)
    val collectionPath = inputData.getString(KEY_COLLECTION_PATH) ?: return Result.failure()
    val documentId = inputData.getString(KEY_DOCUMENT_ID) ?: return Result.failure()
    val fieldName = inputData.getString(KEY_FIELD_NAME) ?: return Result.failure()

    val file = File(filePath)
    if (!file.exists()) return Result.failure()

    return try {
      val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)
      storageRef.putFile(file.toUri()).await()
      val downloadUrl = storageRef.downloadUrl.await().toString()

      FirebaseFirestore.getInstance()
          .collection(collectionPath)
          .document(documentId)
          .set(mapOf(fieldName to downloadUrl), SetOptions.merge())
          .await()

      // Best-effort cleanup of previous image
      if (!existingImageUrl.isNullOrBlank() && !existingImageUrl.startsWith("file://")) {
        runCatching {
          val ref =
              if (existingImageUrl.startsWith("http")) {
                FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl)
              } else {
                FirebaseStorage.getInstance().reference.child(existingImageUrl)
              }
          // Skip deletion if we just uploaded to the same reference; otherwise we'd delete the new
          // image we uploaded above.
          if (ref.path != storageRef.path || ref.bucket != storageRef.bucket) {
            ref.delete().await()
          }
        }
      }

      file.delete()
      Result.success()
    } catch (e: Exception) {
      Log.w(TAG, "Image upload failed for $documentId, will retry", e)
      Result.retry()
    }
  }

  companion object {
    const val KEY_FILE_PATH = "file_path"
    const val KEY_STORAGE_PATH = "storage_path"
    const val KEY_EXISTING_IMAGE_URL = "existing_image_url"
    const val KEY_COLLECTION_PATH = "collection_path"
    const val KEY_DOCUMENT_ID = "document_id"
    const val KEY_FIELD_NAME = "field_name"
    private const val TAG = "ImageUploadWorker"
  }
}
