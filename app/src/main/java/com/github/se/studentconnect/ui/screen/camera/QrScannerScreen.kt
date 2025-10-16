package com.github.se.studentconnect.ui.screen.camera

import android.annotation.SuppressLint
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.camera.CameraView
import com.github.se.studentconnect.ui.camera.QrCodeAnalyzer

private val ANALYSIS_TARGET_RESOLUTION = Size(720, 720)

typealias AnalyzerProvider =
    ((String) -> Unit, (Throwable) -> Unit, RectF?) -> ImageAnalysis.Analyzer?

@SuppressLint("SuspiciousIndentation")
@Composable
fun QrScannerScreen(
    onBackClick: () -> Unit,
    onProfileDetected: (String) -> Unit,
    modifier: Modifier = Modifier.Companion,
    isActive: Boolean = true,
    cameraContent: (@Composable (ImageAnalysis.Analyzer?) -> Unit)? = null,
    analyzerProvider: AnalyzerProvider = { onDetected, onError, roi ->
      QrCodeAnalyzer(onDetected, onError)
    },
) {
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isHandlingResult by remember { mutableStateOf(false) }

  val latestProfileDetected = rememberUpdatedState(onProfileDetected)
  val latestBackClick = rememberUpdatedState(onBackClick)

  LaunchedEffect(isActive) {
    if (!isActive) {
      isHandlingResult = false
      errorMessage = null
    }
  }

  val density = LocalDensity.current
  var containerSize by remember { mutableStateOf(IntSize.Companion.Zero) }
  val focusSizePx = with(density) { 240.dp.toPx() }
  val roiRect: RectF? =
      remember(containerSize, focusSizePx) {
        if (containerSize.width == 0 || containerSize.height == 0) return@remember null
        val cx = containerSize.width / 2f
        val cy = containerSize.height / 2f
        val half = focusSizePx / 2f
        val left = ((cx - half) / containerSize.width).coerceIn(0f, 1f)
        val top = ((cy - half) / containerSize.height).coerceIn(0f, 1f)
        val right = ((cx + half) / containerSize.width).coerceIn(0f, 1f)
        val bottom = ((cy + half) / containerSize.height).coerceIn(0f, 1f)
        RectF(left, top, right, bottom)
      }

  val analyzer =
      remember(isActive, roiRect) {
        if (isActive) {
          analyzerProvider(
              { userId ->
                if (!isHandlingResult) {
                  errorMessage = null
                  isHandlingResult = true
                  latestProfileDetected.value(userId)
                }
              },
              { throwable ->
                errorMessage =
                    throwable.message ?: "Unable to process the QR code. Please try again."
              },
              roiRect)
        } else {
          null
        }
      }

  DisposableEffect(analyzer) {
    if (analyzer == null) {
      onDispose {}
    } else {
      onDispose { (analyzer as? QrCodeAnalyzer)?.close() }
    }
  }

  LaunchedEffect(analyzer, roiRect) { (analyzer as? QrCodeAnalyzer)?.setRoiNormalized(roiRect) }

  val defaultCameraContent: @Composable (ImageAnalysis.Analyzer?) -> Unit = { currentAnalyzer ->
    CameraView(
        modifier = Modifier.Companion.fillMaxSize(),
        enableImageCapture = false,
        imageAnalyzer = currentAnalyzer,
        imageAnalysisConfig = { setTargetResolution(ANALYSIS_TARGET_RESOLUTION) },
        onError = { throwable ->
          errorMessage = throwable.message ?: "Camera error. Please retry."
        },
        noPermission = {
          ScannerPermissionRequired(
              onBackClick = latestBackClick.value,
          )
        })
  }
  val cameraPreview = cameraContent ?: defaultCameraContent

  Box(
      modifier =
          modifier
              .fillMaxSize()
              .onSizeChanged { containerSize = it }
              .semantics { testTag = C.Tag.qr_scanner_screen }) {
        if (isActive && analyzer != null) {
          cameraPreview(analyzer)
        } else {
          InactiveScannerBackground()
        }

        IconButton(
            onClick = latestBackClick.value,
            modifier =
                Modifier.Companion.align(Alignment.Companion.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
                    .semantics { testTag = C.Tag.qr_scanner_back }) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  tint = Color.Companion.White)
            }

        ScannerFocusFrame(modifier = Modifier.Companion.align(Alignment.Companion.Center))

        Column(
            modifier =
                Modifier.Companion.align(Alignment.Companion.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 48.dp)
                    .semantics { testTag = C.Tag.qr_scanner_instructions },
            horizontalAlignment = Alignment.Companion.CenterHorizontally) {
              Text(
                  text = "Point the camera at a StudentConnect QR code",
                  style = MaterialTheme.typography.titleMedium,
                  textAlign = TextAlign.Companion.Center,
                  color = Color.Companion.White)
              Text(
                  text = "We will automatically open the corresponding profile",
                  style = MaterialTheme.typography.bodyMedium,
                  textAlign = TextAlign.Companion.Center,
                  color = Color.Companion.White.copy(alpha = 0.85f),
                  modifier = Modifier.Companion.padding(top = 8.dp))
            }

        errorMessage?.let { message ->
          Surface(
              modifier =
                  Modifier.Companion.align(Alignment.Companion.BottomCenter)
                      .padding(bottom = 12.dp, start = 24.dp, end = 24.dp)
                      .semantics { testTag = C.Tag.qr_scanner_error },
              color = MaterialTheme.colorScheme.errorContainer,
              tonalElevation = 2.dp,
              shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.Companion.padding(horizontal = 16.dp, vertical = 12.dp),
                    textAlign = TextAlign.Companion.Center)
              }
        }
      }
}

@Composable
private fun ScannerFocusFrame(modifier: Modifier = Modifier.Companion, size: Dp = 240.dp) {
  Box(
      modifier =
          modifier
              .size(size)
              .border(
                  width = 2.dp,
                  color = Color.Companion.White.copy(alpha = 0.9f),
                  shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
              .background(Color.Companion.Transparent)
              .semantics { testTag = C.Tag.qr_scanner_focus })
}

@Composable
private fun ScannerPermissionRequired(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
  Column(
      modifier = modifier.fillMaxSize().background(Color.Companion.Black.copy(alpha = 0.6f)),
      horizontalAlignment = Alignment.Companion.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Text(
            text = "Camera permission is required to scan QR codes.",
            color = Color.Companion.White,
            textAlign = TextAlign.Companion.Center,
            modifier =
                Modifier.Companion.padding(horizontal = 24.dp).semantics {
                  testTag = C.Tag.qr_scanner_permission
                })
        IconButton(onClick = onBackClick, modifier = Modifier.Companion.padding(top = 24.dp)) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.Companion.White)
        }
      }
}

@Composable
private fun InactiveScannerBackground(modifier: Modifier = Modifier.Companion) {
  Box(
      modifier =
          modifier.fillMaxSize().background(Color.Companion.Black.copy(alpha = 0.5f)).semantics {
            testTag = C.Tag.qr_scanner_placeholder
          },
      contentAlignment = Alignment.Companion.Center) {
        Text(
            text = "Swipe right to activate the scanner",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Companion.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Companion.Center,
            modifier = Modifier.Companion.padding(24.dp))
      }
}
