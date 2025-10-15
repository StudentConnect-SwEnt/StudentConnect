// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.media

import android.net.Uri

/**
 * Minimal abstraction for uploading, downloading, and deleting media files. Optionally allows
 * specifying a custom storage path.
 */
interface MediaRepository {

  /**
   * Upload a file.
   *
   * @param uri The local Uri of the file to upload.
   * @param path Optional destination path (e.g., "users/{uid}/profile.jpg"). If null, the
   *   implementation generates a unique path.
   * @return The unique ID or URL of the uploaded file.
   */
  suspend fun upload(uri: Uri, path: String? = null): String

  /**
   * Download a file by its unique ID or path.
   *
   * @param id The unique ID or path of the file.
   * @return The local Uri of the downloaded file.
   */
  suspend fun download(id: String): Uri

  /**
   * Delete a file by its unique ID or path.
   *
   * @param id The unique ID or path of the file.
   */
  suspend fun delete(id: String)
}
