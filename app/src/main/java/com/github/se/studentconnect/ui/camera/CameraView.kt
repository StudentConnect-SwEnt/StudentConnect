// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch

private val CAPTURE_BUTTON_PADDING = 16.dp
private val CAPTURE_BUTTON_SIZE = 72.dp

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    noPermission: @Composable () -> Unit = {},
    captureButton: @Composable ((Boolean) -> Unit)? =
        null, // null gives default button, Boolean is recording state
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    enableImageCapture: Boolean = true,
    enableVideoCapture: Boolean = false,
    imageAnalyzer: ImageAnalysis.Analyzer? = null,
    imageAnalysisConfig: (ImageAnalysis.Builder.() -> Unit)? = null,
    imageCaptureConfig: (ImageCapture.Builder.() -> Unit)? = null,
    onImageCaptured: ((Uri) -> Unit)? = null,
    onVideoCaptured: ((Uri) -> Unit)? = null,
    onCameraPermissionDenied: () -> Unit = {},
    onError: (Throwable) -> Unit = {},
    requestPermissionAutomatically: Boolean = true,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()

  // Permission state
  var hasCameraPermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED)
  }

  val permissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) onCameraPermissionDenied()
        hasCameraPermission = granted
      }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission && requestPermissionAutomatically) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  // Stop if no permission yet
  if (!hasCameraPermission) {
    // Render no permission
    noPermission()
    return
  }

  val previewView = remember {
    PreviewView(context).apply {
      // Use FILL_CENTER to ensure the preview fills the screen
      scaleType = PreviewView.ScaleType.FILL_CENTER
    }
  }
  val imageCapture =
      remember(enableImageCapture, imageCaptureConfig) {
        if (enableImageCapture) {
          ImageCapture.Builder().apply { imageCaptureConfig?.invoke(this) }.build()
        } else {
          null
        }
      }
  val videoCapture =
      remember(enableVideoCapture) {
        if (enableVideoCapture) {
          val recorder =
              Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HD)).build()
          VideoCapture.withOutput(recorder)
        } else {
          null
        }
      }
  var activeRecording by remember { mutableStateOf<Recording?>(null) }
  var isRecording by remember { mutableStateOf(false) }

  val cameraProviderState = remember { mutableStateOf<ProcessCameraProvider?>(null) }
  val boundUseCases = remember { mutableStateListOf<UseCase>() }
  val analysisExecutor =
      remember(imageAnalyzer) {
        if (imageAnalyzer != null) {
          java.util.concurrent.Executors.newSingleThreadExecutor()
        } else {
          null
        }
      }
  val imageAnalysis =
      remember(imageAnalyzer, analysisExecutor, imageAnalysisConfig) {
        if (imageAnalyzer != null && analysisExecutor != null) {
          ImageAnalysis.Builder()
              .apply { imageAnalysisConfig?.invoke(this) }
              .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
              .build()
              .apply { setAnalyzer(analysisExecutor, imageAnalyzer) }
        } else {
          null
        }
      }

  DisposableEffect(imageAnalyzer) { onDispose { analysisExecutor?.shutdown() } }

  // Bind camera when Composable appears
  LaunchedEffect(cameraSelector, imageAnalysis, imageCapture, videoCapture, imageCaptureConfig) {
    val cameraProvider = context.getCameraProvider()
    cameraProviderState.value = cameraProvider
    val preview =
        Preview.Builder()
            .apply {
              // Match the aspect ratio of image capture if configured
              if (imageCaptureConfig != null) {
                setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
              }
            }
            .build()
    preview.setSurfaceProvider(previewView.surfaceProvider)

    try {
      cameraProvider.unbindAll()
      val useCases = mutableListOf<UseCase>()
      useCases += preview
      imageCapture?.let { useCases += it }
      videoCapture?.let { useCases += it }
      imageAnalysis?.let { useCases += it }

      cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, *useCases.toTypedArray())
      boundUseCases.clear()
      boundUseCases.addAll(useCases)
    } catch (e: Exception) {
      onError(e)
    }
  }

  DisposableEffect(cameraProviderState.value) {
    val providerAtStart = cameraProviderState.value
    val useCasesAtStart = boundUseCases.toList()
    onDispose {
      if (providerAtStart != null && useCasesAtStart.isNotEmpty()) {
        providerAtStart.unbind(*useCasesAtStart.toTypedArray())
        if (providerAtStart == cameraProviderState.value) {
          boundUseCases.removeAll(useCasesAtStart)
        }
      }
    }
  }

  Box(modifier = modifier) {
    // Show the preview
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

    // Capture button overlay
    if ((enableImageCapture && imageCapture != null) ||
        (enableVideoCapture && videoCapture != null)) {
      val hasCustomButton = captureButton != null
      Box(
          modifier =
              Modifier.align(Alignment.BottomCenter)
                  .padding(CAPTURE_BUTTON_PADDING)
                  .let { base ->
                    if (hasCustomButton) base
                    else base.size(CAPTURE_BUTTON_SIZE).background(Color.White, CircleShape)
                  }
                  .clickable {
                    coroutineScope.launch {
                      try {
                        if (enableVideoCapture && videoCapture != null) {
                          // Toggle video recording
                          if (isRecording) {
                            activeRecording?.stop()
                            activeRecording = null
                            isRecording = false
                          } else {
                            val file =
                                File(context.cacheDir, "video_${System.currentTimeMillis()}.mp4")
                            val outputOptions = FileOutputOptions.Builder(file).build()
                            activeRecording =
                                videoCapture.output.prepareRecording(context, outputOptions).start(
                                    ContextCompat.getMainExecutor(context)) { event ->
                                      when (event) {
                                        is VideoRecordEvent.Finalize -> {
                                          if (event.hasError()) {
                                            onError(
                                                Exception("Video recording error: ${event.error}"))
                                          } else {
                                            onVideoCaptured?.invoke(Uri.fromFile(file))
                                          }
                                        }
                                      }
                                    }
                            isRecording = true
                          }
                        } else if (enableImageCapture && imageCapture != null) {
                          val uri = capturePhoto(context, imageCapture)
                          uri?.let { onImageCaptured?.invoke(it) }
                        }
                      } catch (e: Exception) {
                        onError(e)
                      }
                    }
                  },
          contentAlignment = Alignment.Center) {
            if (captureButton != null) {
              captureButton.invoke(isRecording)
            } else {
              Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = "Capture",
                  tint = Color.Black)
            }
          }
    }
  }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { cont ->
  val future = ProcessCameraProvider.getInstance(this)
  future.addListener({ cont.resume(future.get()) }, ContextCompat.getMainExecutor(this))
}

private suspend fun capturePhoto(context: Context, imageCapture: ImageCapture): Uri? {
  val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
  val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

  return suspendCoroutine { continuation ->
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
          override fun onError(exc: ImageCaptureException) {
            continuation.resume(null)
          }

          override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            continuation.resume(Uri.fromFile(file))
          }
        })
  }
}
