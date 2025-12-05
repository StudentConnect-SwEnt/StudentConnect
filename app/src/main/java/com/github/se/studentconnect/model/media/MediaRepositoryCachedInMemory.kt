package com.github.se.studentconnect.model.media

import android.net.Uri
import com.github.se.studentconnect.model.cache.TtlInMemoryCache
import java.io.File

private const val DEFAULT_TTL_MS = 5 * 60 * 1000L // 5 minutes

private class MediaInMemoryCache(ttlMs: Long) : TtlInMemoryCache<String, Uri>(ttlMs) {
  override fun isValid(key: String, value: Uri): Boolean =
      super.isValid(key, value) &&
          // Only treat file:// URIs as invalid if the backing file is gone
          value.scheme != "file" || value.path?.let { File(it).exists() } == true
}

/**
 * A media repository which wraps around another media repository. It caches images based on their
 * Uri.
 */
class MediaRepositoryCachedInMemory(private val repository: MediaRepository) : MediaRepository {
  private val cache = MediaInMemoryCache(DEFAULT_TTL_MS)

  override suspend fun upload(uri: Uri, path: String?): String =
      repository.upload(uri, path).also { id -> cache.put(id, uri) }

  override suspend fun download(id: String): Uri =
      cache.peek(id) ?: repository.download(id).also { cache.put(id, it) }

  override suspend fun delete(id: String) {
    cache.invalidate(id)
    repository.delete(id)
  }
}
