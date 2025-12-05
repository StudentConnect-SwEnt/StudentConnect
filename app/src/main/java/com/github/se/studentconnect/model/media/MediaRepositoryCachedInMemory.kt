package com.github.se.studentconnect.model.media

import android.net.Uri
import com.github.se.studentconnect.model.cache.InMemoryCache
import java.io.File

private class MediaInMemoryCache : InMemoryCache<String, Uri>() {
  override fun isValid(value: Uri): Boolean =
      // Only treat file:// URIs as invalid if the backing file is gone
      value.scheme != "file" || value.path?.let { File(it).exists() } == true
}

class MediaRepositoryCachedInMemory(private val repository: MediaRepository) : MediaRepository {
  private val cache = MediaInMemoryCache()

  override suspend fun upload(uri: Uri, path: String?): String =
      repository.upload(uri, path).also { id -> cache.put(id, uri) }

  override suspend fun download(id: String): Uri =
      cache.peek(id) ?: repository.download(id).also { cache.put(id, it) }

  override suspend fun delete(id: String) {
    cache.invalidate(id)
    repository.delete(id)
  }
}
