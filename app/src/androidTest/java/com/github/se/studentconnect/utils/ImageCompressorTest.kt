package com.github.se.studentconnect.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class ImageCompressorTest {

  private lateinit var context: Context
  private lateinit var testImageFile: File

  @Before
  fun setup() {
    context = InstrumentationRegistry.getInstrumentation().targetContext
    testImageFile = File(context.cacheDir, "test_image_${System.currentTimeMillis()}.jpg")
  }

  @Test
  fun compressImage_withValidImage_returnsCompressedUri() = runTest {
    // Create a test image (small bitmap)
    val bitmap = Bitmap.createBitmap(3000, 2000, Bitmap.Config.ARGB_8888)
    val outputStream = FileOutputStream(testImageFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()
    bitmap.recycle()

    val uri = Uri.fromFile(testImageFile)

    // Act
    val compressedUri = ImageCompressor.compressImage(context, uri)

    // Assert
    assertNotNull("Compressed URI should not be null", compressedUri)
    val compressedFile = File(compressedUri?.path ?: "")
    assertTrue("Compressed file should exist", compressedFile.exists())

    // Verify compression (should be smaller than max width)
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(compressedFile.absolutePath, options)
    assertTrue("Width should be <= 1920", options.outWidth <= 1920)

    // Cleanup
    compressedFile.delete()
    testImageFile.delete()
  }

  @Test
  fun compressImage_withSmallImage_returnsCompressedUri() = runTest {
    // Create a small test image (already under max width)
    val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
    val outputStream = FileOutputStream(testImageFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()
    bitmap.recycle()

    val uri = Uri.fromFile(testImageFile)

    // Act
    val compressedUri = ImageCompressor.compressImage(context, uri)

    // Assert
    assertNotNull("Compressed URI should not be null", compressedUri)
    val compressedFile = File(compressedUri?.path ?: "")
    assertTrue("Compressed file should exist", compressedFile.exists())

    // Cleanup
    compressedFile.delete()
    testImageFile.delete()
  }

  @Test
  fun compressImage_withInvalidUri_returnsNull() = runTest {
    // Create invalid URI
    val invalidUri = Uri.parse("invalid://uri")

    // Act
    val result = ImageCompressor.compressImage(context, invalidUri)

    // Assert
    assertNull("Should return null for invalid URI", result)
  }

  @Test
  fun compressImage_withNonExistentFile_returnsNull() = runTest {
    // Create URI for non-existent file
    val nonExistentFile = File(context.cacheDir, "non_existent_${System.currentTimeMillis()}.jpg")
    val uri = Uri.fromFile(nonExistentFile)

    // Act
    val result = ImageCompressor.compressImage(context, uri)

    // Assert
    assertNull("Should return null for non-existent file", result)
  }

  @Test
  fun compressImage_maintainsAspectRatio() = runTest {
    // Create a wide image (3000x1000 = 3:1 ratio)
    val originalWidth = 3000
    val originalHeight = 1000
    val bitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888)
    val outputStream = FileOutputStream(testImageFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()
    bitmap.recycle()

    val uri = Uri.fromFile(testImageFile)

    // Act
    val compressedUri = ImageCompressor.compressImage(context, uri)

    // Assert
    assertNotNull("Compressed URI should not be null", compressedUri)
    val compressedFile = File(compressedUri?.path ?: "")
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(compressedFile.absolutePath, options)

    val expectedHeight = (options.outWidth * originalHeight) / originalWidth
    val heightDiff = kotlin.math.abs(options.outHeight - expectedHeight)
    assertTrue("Aspect ratio should be maintained (within 5px tolerance)", heightDiff <= 5)

    // Cleanup
    compressedFile.delete()
    testImageFile.delete()
  }
}

