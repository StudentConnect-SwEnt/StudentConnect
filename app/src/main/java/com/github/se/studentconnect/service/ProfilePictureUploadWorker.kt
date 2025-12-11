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

/** Uploads a staged profile picture and patches the user document. */
class ProfilePictureUploadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result {
    val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
    val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
    val storagePath = inputData.getString(KEY_STORAGE_PATH) ?: return Result.failure()
    val existingImageUrl = inputData.getString(KEY_EXISTING_IMAGE_URL)

    val file = File(filePath)
    if (!file.exists()) return Result.failure()

    return try {
      val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)
      storageRef.putFile(file.toUri()).await()
      val downloadUrl = storageRef.downloadUrl.await().toString()

      FirebaseFirestore.getInstance()
          .collection(USERS_COLLECTION_PATH)
          .document(userId)
          .set(mapOf("profilePictureUrl" to downloadUrl), SetOptions.merge())
          .await()

      // Best-effort cleanup of previous image, avoiding the one we just uploaded.
      if (!existingImageUrl.isNullOrBlank() && !existingImageUrl.startsWith("file://")) {
        runCatching {
          val ref =
              if (existingImageUrl.startsWith("http")) {
                FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl)
              } else {
                FirebaseStorage.getInstance().reference.child(existingImageUrl)
              }
          if (ref.path != storageRef.path || ref.bucket != storageRef.bucket) {
            ref.delete().await()
          }
        }
      }

      file.delete()
      Result.success()
    } catch (e: Exception) {
      Log.w(TAG, "Profile picture upload failed for $userId, will retry", e)
      Result.retry()
    }
  }

  companion object {
    const val KEY_USER_ID = "user_id"
    const val KEY_FILE_PATH = "file_path"
    const val KEY_STORAGE_PATH = "storage_path"
    const val KEY_EXISTING_IMAGE_URL = "existing_image_url"
    const val USERS_COLLECTION_PATH = "users"
    private const val TAG = "ProfilePictureUpload"
  }
}
