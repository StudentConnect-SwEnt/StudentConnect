package com.github.se.studentconnect.ui.camera

import android.graphics.RectF
import android.net.Uri
import androidx.annotation.OptIn
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/** Analyzer that scans CameraX frames for QR codes and emits the detected user identifier. */
class QrCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit,
    private val onError: (Throwable) -> Unit,
) : ImageAnalysis.Analyzer {

  private val scanner =
      BarcodeScanning.getClient(
          BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build())

  @Volatile private var roiNormalized: RectF? = null

  fun setRoiNormalized(roi: RectF?) {
    roiNormalized = roi
  }

  @OptIn(ExperimentalGetImage::class)
  override fun analyze(imageProxy: ImageProxy) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
      imageProxy.close()
      return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    scanner
        .process(inputImage)
        .addOnSuccessListener { barcodes ->
          val filtered =
              if (roiNormalized == null) {
                barcodes
              } else {
                val rotation = imageProxy.imageInfo.rotationDegrees % 180 != 0
                val rotatedWidth = if (rotation) mediaImage.height else mediaImage.width
                val rotatedHeight = if (rotation) mediaImage.width else mediaImage.height
                val roiPx =
                    RectF(
                        roiNormalized!!.left * rotatedWidth,
                        roiNormalized!!.top * rotatedHeight,
                        roiNormalized!!.right * rotatedWidth,
                        roiNormalized!!.bottom * rotatedHeight)
                barcodes.filter { barcode ->
                  val box = barcode.boundingBox
                  if (box == null) return@filter false
                  val cx = box.exactCenterX()
                  val cy = box.exactCenterY()
                  roiPx.contains(cx, cy)
                }
              }
          filtered
              .firstNotNullOfOrNull { barcode -> extractUserId(barcode.rawValue, barcode.url?.url) }
              ?.let(onQrCodeDetected)
        }
        .addOnFailureListener(onError)
        .addOnCompleteListener { imageProxy.close() }
  }

  /** Must be called when the analyzer is no longer used to free ML Kit resources. */
  fun close() {
    scanner.close()
  }

  companion object {
    @VisibleForTesting
    internal fun extractUserId(rawValue: String?, url: String?): String? {
      val trimmedRaw = rawValue?.trim().orEmpty()

      url?.let { parseFromUrl(it) }
          ?.let {
            return it
          }

      if (trimmedRaw.isBlank()) return null

      parseFromUrl(trimmedRaw)?.let {
        return it
      }

      if (trimmedRaw.contains("userId=", ignoreCase = true)) {
        val candidate =
            trimmedRaw
                .substringAfter("userId=", missingDelimiterValue = "")
                .substringBefore("&")
                .trim()
        if (candidate.isNotEmpty()) {
          return candidate
        }
      }

      return trimmedRaw
    }

    private fun parseFromUrl(raw: String): String? {
      return runCatching {
            val uri = Uri.parse(raw)
            // Only consider URL parsing if it looks like a URL (has a scheme)
            if (uri.scheme != null) {
              uri.getQueryParameter("userId")?.takeIf { it.isNotBlank() }
                  ?: uri.lastPathSegment?.takeIf { it.isNotBlank() }
            } else {
              null
            }
          }
          .getOrNull()
    }
  }
}
