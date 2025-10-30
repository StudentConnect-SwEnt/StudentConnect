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

    // the id is the path
    return ref.path
  }

  override suspend fun download(id: String): Uri {
    val ref = storage.reference.child(id)
    val localFile = File.createTempFile("media_", null)

    // download to local cache file
    ref.getFile(localFile).await()

    return Uri.fromFile(localFile)
  }

  override suspend fun delete(id: String) {
    val ref = storage.reference.child(id)
    ref.delete().await()
  }
}
