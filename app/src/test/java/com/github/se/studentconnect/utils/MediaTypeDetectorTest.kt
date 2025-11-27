package com.github.se.studentconnect.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediaTypeDetectorTest {

  @Mock private lateinit var mockContext: Context
  @Mock private lateinit var mockContentResolver: ContentResolver

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
  }

  @Test
  fun detectMediaType_withImageMimeType_returnsImage() = runTest {
    // Arrange - mock MIME type detection
    val uri = Uri.parse("content://media/images/123")
    whenever(mockContentResolver.getType(uri)).thenReturn("image/jpeg")

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("image", result)
  }

  @Test
  fun detectMediaType_withVideoMimeType_returnsVideo() = runTest {
    // Arrange - mock MIME type detection
    val uri = Uri.parse("content://media/videos/123")
    whenever(mockContentResolver.getType(uri)).thenReturn("video/mp4")

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("video", result)
  }

  @Test
  fun detectMediaType_withUnknownMimeType_returnsImage() = runTest {
    // Arrange - mock MIME type that doesn't start with image/ or video/
    val uri = Uri.parse("content://media/files/123")
    whenever(mockContentResolver.getType(uri)).thenReturn("application/pdf")

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("image", result)
  }

  @Test
  fun detectMediaType_withNullMimeTypeAndNullPath_returnsImage() = runTest {
    // Arrange - URI with null path
    val uri = Uri.EMPTY
    whenever(mockContentResolver.getType(uri)).thenReturn(null)

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("image", result)
  }

  @Test
  fun detectMediaType_withExceptionAndNullPath_returnsImage() = runTest {
    // Arrange - exception thrown and null path
    val uri = Uri.EMPTY
    whenever(mockContentResolver.getType(uri)).thenThrow(RuntimeException("Error"))

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("image", result)
  }

  @Test
  fun detectMediaType_withExceptionAndValidPath_usesExtension() = runTest {
    // Arrange - exception thrown but valid path
    val uri = Uri.parse("file:///path/to/video.mp4")
    whenever(mockContentResolver.getType(uri)).thenThrow(RuntimeException("Error"))

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("video", result)
  }

  @Test
  fun detectMediaType_withExceptionAndUnknownExtension_returnsImage() = runTest {
    // Arrange - exception thrown with unknown extension
    val uri = Uri.parse("file:///path/to/file.unknown")
    whenever(mockContentResolver.getType(uri)).thenThrow(RuntimeException("Error"))

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("image", result)
  }

  @Test
  fun detectMediaType_withNullMimeTypeAndImageExtension_returnsImage() = runTest {
    // Arrange - null MIME type but image extension
    val uri = Uri.parse("file:///path/to/image.jpg")
    whenever(mockContentResolver.getType(uri)).thenReturn(null)

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("image", result)
  }

  @Test
  fun detectMediaType_withNullMimeTypeAndVideoExtension_returnsVideo() = runTest {
    // Arrange - null MIME type but video extension
    val uri = Uri.parse("file:///path/to/video.mp4")
    whenever(mockContentResolver.getType(uri)).thenReturn(null)

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("video", result)
  }

  @Test
  fun detectMediaType_withNullMimeTypeAndUnknownExtension_returnsImage() = runTest {
    // Arrange - null MIME type and unknown extension
    val uri = Uri.parse("file:///path/to/file.unknown")
    whenever(mockContentResolver.getType(uri)).thenReturn(null)

    // Act
    val result = MediaTypeDetector.detectMediaType(mockContext, uri)

    // Assert
    assertEquals("image", result)
  }
}

