package com.github.se.studentconnect.model.media

import com.github.se.studentconnect.model.BaseRepositoryProvider

/** Provides a single instance of the repository in the app. */
object MediaRepositoryProvider : BaseRepositoryProvider<MediaRepository>() {
  override fun getRepository(): MediaRepository = MediaRepositoryFirebaseStorage()
}
