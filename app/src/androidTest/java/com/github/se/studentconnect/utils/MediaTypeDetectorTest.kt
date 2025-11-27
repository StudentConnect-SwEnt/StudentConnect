package com.github.se.studentconnect.utils

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaTypeDetectorInstrumentedTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = InstrumentationRegistry.getInstrumentation().targetContext
  }

  @Test
  fun detectMediaType_withImageFile_returnsImage() = runTest {
    // Arrange - create a real image file
    val testImageFile = java.io.File(context.cacheDir, "test_image.jpg")
    testImageFile.createNewFile()
    val uri = Uri.fromFile(testImageFile)

    // Act
    val result = MediaTypeDetector.detectMediaType(context, uri)

    // Assert
    assertEquals("image", result)

    // Cleanup
    testImageFile.delete()
  }

  @Test
  fun detectMediaType_withVideoFile_returnsVideo() = runTest {
    // Arrange - create a dummy video file
    val testVideoFile = java.io.File(context.cacheDir, "test_video.mp4")
    testVideoFile.createNewFile()
    val uri = Uri.fromFile(testVideoFile)

    // Act
    val result = MediaTypeDetector.detectMediaType(context, uri)

    // Assert - Android may detect MIME type or fallback to extension, both should return "video"
    assertEquals("video", result)

    // Cleanup
    testVideoFile.delete()
  }

  @Test
  fun detectMediaType_withEmptyUri_returnsImage() = runTest {
    // Arrange
    val uri = Uri.EMPTY

    // Act
    val result = MediaTypeDetector.detectMediaType(context, uri)

    // Assert - should handle exception and return default
    assertEquals("image", result)
  }

  @Test
  fun detectMediaType_withFileUriMp4Extension_usesExtensionFallback() = runTest {
    // Arrange - use file URI that won't have MIME type detected (nonexistent path)
    val uri = Uri.parse("file:///nonexistent/path/to/video.mp4")

    // Act
    val result = MediaTypeDetector.detectMediaType(context, uri)

    // Assert - should fallback to extension check
    assertEquals("video", result)
  }

  @Test
  fun detectMediaType_withFileUriMovExtension_usesExtensionFallback() = runTest {
    // Arrange
    val uri = Uri.parse("file:///path/to/video.mov")

    // Act
    val result = MediaTypeDetector.detectMediaType(context, uri)

    // Assert
    assertEquals("video", result)
  }

  @Test
  fun detectMediaType_withFileUriAviExtension_usesExtensionFallback() = runTest {
    // Arrange
    val uri = Uri.parse("file:///path/to/video.avi")

    // Act
    val result = MediaTypeDetector.detectMediaType(context, uri)

    // Assert
    assertEquals("video", result)
  }

  @Test
  fun detectMediaType_withFileUriMkvExtension_usesExtensionFallback() = runTest {
    // Arrange
    val uri = Uri.parse("file:///path/to/video.mkv")

    // Act
    val result = MediaTypeDetector.detectMediaType(context, uri)

    // Assert
    assertEquals("video", result)
  }

  @Test
  fun detectMediaType_withFileUriWebmExtension_usesExtensionFallback() = runTest {
    // Arrange
    val uri = Uri.parse("file:///path/to/video.webm")

    // Act
    val result = MediaTypeDetector.detectMediaType(context, uri)

    // Assert
    assertEquals("video", result)
  }

  @Test
  fun detectMediaType_withFileUriUnknownExtension_returnsImage() = runTest {
    // Arrange
    val uri = Uri.parse("file:///path/to/file.unknown")

    // Act
    val result = MediaTypeDetector.detectMediaType(context, uri)

    // Assert
    assertEquals("image", result)
  }

}
