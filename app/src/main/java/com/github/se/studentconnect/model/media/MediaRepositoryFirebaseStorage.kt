// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.media

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlinx.coroutines.tasks.await

class MediaRepositoryFirebaseStorage(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : MediaRepository {
  override suspend fun upload(uri: Uri, path: String?): String {
    val targetPath = path ?: "media/${System.currentTimeMillis()}_${uri.lastPathSegment}"
    val ref = storage.reference.child(targetPath)

    // upload the file
    ref.putFile(uri).await()

    // Get the download URL instead of just the path
    // This is needed for displaying media in the app
    val downloadUrl = ref.downloadUrl.await()

    // Return the download URL as a string
    return downloadUrl.toString()
  }

  override suspend fun download(id: String): Uri {
    val ref = storage.reference.child(id)
    val localFile = File.createTempFile("media_", null)

    // download to local cache file
    ref.getFile(localFile).await()

    return Uri.fromFile(localFile)
  }

  override suspend fun delete(id: String) {
    // Handle both cases: storage path or download URL
    val ref =
        if (id.startsWith("http")) {
          // It's a download URL, extract the path from it
          // URL format: https://firebasestorage.googleapis.com/v0/b/[bucket]/o/[path]?[params]
          val pathMatch = Regex("/o/([^?]+)").find(id)
          if (pathMatch != null) {
            val encodedPath = pathMatch.groupValues[1]
            val decodedPath = java.net.URLDecoder.decode(encodedPath, "UTF-8")
            storage.reference.child(decodedPath)
          } else {
            // Fallback to using id as-is
            storage.reference.child(id)
          }
        } else {
          // It's a direct storage path
          storage.reference.child(id)
        }
    ref.delete().await()
  }
}
