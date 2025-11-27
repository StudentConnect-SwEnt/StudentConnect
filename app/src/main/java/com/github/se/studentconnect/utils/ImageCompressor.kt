// Used AI to help write this code

package com.github.se.studentconnect.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility for compressing images before upload.
 *
 * Compresses images to a maximum width of 1920px while maintaining aspect ratio.
 */
object ImageCompressor {

  /**
   * Compresses an image from a URI to a maximum width of 1920px.
   *
   * @param context The application context
   * @param uri The URI of the image to compress
   * @return The URI of the compressed image file, or null if compression fails
   */
  suspend fun compressImage(context: Context, uri: Uri, maxWidth: Int = 1920): Uri? =
      withContext(Dispatchers.IO) {
        try {
          // Open input stream from URI
          val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null

          // Decode bitmap with options to get dimensions without loading full image
          val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
          BitmapFactory.decodeStream(inputStream, null, options)
          inputStream.close()

          val imageWidth = options.outWidth
          val imageHeight = options.outHeight

          // Calculate scale factor
          val scaleFactor =
              if (imageWidth > maxWidth) {
                imageWidth.toFloat() / maxWidth
              } else {
                1f
              }

          // Calculate new dimensions
          val newWidth = (imageWidth / scaleFactor).toInt()
          val newHeight = (imageHeight / scaleFactor).toInt()

          // Decode bitmap at reduced size
          val decodeOptions =
              BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(options, newWidth, newHeight)
              }

          val inputStream2 = context.contentResolver.openInputStream(uri) ?: return@withContext null
          val bitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
          inputStream2.close()

          if (bitmap == null) return@withContext null

          // Resize if needed
          val resizedBitmap =
              if (bitmap.width != newWidth || bitmap.height != newHeight) {
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
              } else {
                bitmap
              }

          // Save compressed image to temporary file
          val outputDir = File(context.cacheDir, "compressed_images")
          if (!outputDir.exists()) {
            outputDir.mkdirs()
          }
          val outputFile = File.createTempFile("compressed_", ".jpg", outputDir)
          val outputStream = FileOutputStream(outputFile)

          resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
          outputStream.flush()
          outputStream.close()

          // Clean up original bitmap if it was resized
          if (resizedBitmap != bitmap) {
            bitmap.recycle()
          }
          resizedBitmap.recycle()

          Uri.fromFile(outputFile)
        } catch (e: Exception) {
          null
        }
      }

  private fun calculateInSampleSize(
      options: BitmapFactory.Options,
      reqWidth: Int,
      reqHeight: Int
  ): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
      val halfHeight = height / 2
      val halfWidth = width / 2

      while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2
      }
    }

    return inSampleSize
  }
}
