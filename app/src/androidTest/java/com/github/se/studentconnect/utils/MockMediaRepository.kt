package com.github.se.studentconnect.utils

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepository

class MockMediaRepository : MediaRepository {
  override suspend fun upload(uri: Uri, path: String?): String {
    return "mock_media_id"
  }

  override suspend fun download(id: String): Uri {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return Uri.parse("android.resource://${context.packageName}/${R.drawable.pixel}")
  }

  override suspend fun delete(id: String) {}
}
