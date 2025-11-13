package com.github.se.studentconnect.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.github.se.studentconnect.BuildConfig
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class PicturePickerStyle {
  Avatar,
  Banner
}

@Composable
fun PicturePickerCard(
    modifier: Modifier = Modifier,
    style: PicturePickerStyle = PicturePickerStyle.Avatar,
    existingImagePath: String?,
    selectedImageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    placeholderText: String,
    overlayText: String,
    imageDescription: String,
    placeholderIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
  val repository = MediaRepositoryProvider.repository
  var downloadedImageUri by remember(existingImagePath, repository) { mutableStateOf<Uri?>(null) }
  val context = LocalContext.current

  LaunchedEffect(existingImagePath, repository) {
    downloadedImageUri =
        existingImagePath?.let { runCatching { repository.download(it) }.getOrNull() }
  }

  val displayUri = selectedImageUri ?: downloadedImageUri
  var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
  var shouldOpenCameraAfterPermission by remember { mutableStateOf(false) }
  var showSourceDialog by remember { mutableStateOf(false) }

  LaunchedEffect(displayUri) {
    imageBitmap =
        if (displayUri != null) {
          loadBitmapFromUri(context, displayUri, Dispatchers.IO)
        } else {
          null
        }
  }

  val pickMediaLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onImageSelected)
      }
  val takePictureLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
          pendingCameraUri?.let(onImageSelected)
        } else {
          Toast.makeText(context, "Camera closed without capturing a photo", Toast.LENGTH_SHORT)
              .show()
        }
        pendingCameraUri = null
      }
  val cameraPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (shouldOpenCameraAfterPermission) {
          if (granted) {
            launchCamera(context, takePictureLauncher) { uri -> pendingCameraUri = uri }
          } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_LONG).show()
          }
        }
        shouldOpenCameraAfterPermission = false
      }

  fun openCameraOrRequestPermission() {
    val permissionGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    if (permissionGranted) {
      launchCamera(context, takePictureLauncher) { uri -> pendingCameraUri = uri }
    } else {
      shouldOpenCameraAfterPermission = true
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  val shape =
      when (style) {
        PicturePickerStyle.Avatar -> CircleShape
        PicturePickerStyle.Banner -> RoundedCornerShape(20.dp)
      }

  val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(18f, 16f), 0f) }
  val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
  val sizeModifier =
      when (style) {
        PicturePickerStyle.Avatar -> Modifier.size(140.dp)
        PicturePickerStyle.Banner -> Modifier.fillMaxWidth().height(200.dp)
      }

  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
        modifier =
            sizeModifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .clickable { showSourceDialog = true }
                .dashedBorder(shape, dashEffect, borderColor),
        contentAlignment = Alignment.Center) {
          if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = imageDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop)
            Text(
                text = overlayText,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp))
          } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              val icon =
                  placeholderIcon
                      ?: when (style) {
                        PicturePickerStyle.Avatar -> Icons.Default.Person
                        PicturePickerStyle.Banner -> Icons.Default.Image
                      }
              Icon(
                  imageVector = icon,
                  contentDescription = imageDescription,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier =
                      when (style) {
                        PicturePickerStyle.Avatar -> Modifier.size(60.dp)
                        PicturePickerStyle.Banner -> Modifier.size(48.dp)
                      })
              Spacer(Modifier.height(16.dp))
              Text(
                  text = placeholderText,
                  style =
                      MaterialTheme.typography.bodyMedium.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant),
                  textAlign = TextAlign.Center)
            }
          }
        }
  }

  if (showSourceDialog) {
    AlertDialog(
        onDismissRequest = { showSourceDialog = false },
        title = { Text("Add a photo") },
        text = { Text("Choose how you want to add your picture.") },
        confirmButton = {
          Column(horizontalAlignment = Alignment.End) {
            TextButton(
                onClick = {
                  showSourceDialog = false
                  pickMediaLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                  Text("Choose from gallery")
                }
            TextButton(
                onClick = {
                  showSourceDialog = false
                  openCameraOrRequestPermission()
                }) {
                  Text("Take photo")
                }
          }
        },
        dismissButton = { TextButton(onClick = { showSourceDialog = false }) { Text("Cancel") } })
  }
}

private fun Modifier.dashedBorder(shape: Shape, pathEffect: PathEffect, color: Color): Modifier =
    this.then(
        Modifier.drawBehind {
          val stroke = Stroke(width = 3.dp.toPx(), pathEffect = pathEffect)
          when (val outline = shape.createOutline(size, layoutDirection, this)) {
            is Outline.Rectangle -> drawRect(color = color, size = size, style = stroke)
            is Outline.Rounded ->
                drawRoundRect(
                    color = color,
                    size = size,
                    style = stroke,
                    cornerRadius = CornerRadius(outline.roundRect.topLeftCornerRadius.x))
            is Outline.Generic -> drawPath(outline.path, color = color, style = stroke)
          }
        })

private suspend fun loadBitmapFromUri(
    context: Context,
    uri: Uri,
    dispatcher: CoroutineDispatcher
): ImageBitmap? =
    withContext(dispatcher) {
      try {
        when (uri.scheme?.lowercase()) {
          "file" -> uri.path?.let { path -> BitmapFactory.decodeFile(path)?.asImageBitmap() }
          else ->
              context.contentResolver.openInputStream(uri)?.use { stream ->
                decodeStream(stream)?.asImageBitmap()
              }
        }
      } catch (_: Exception) {
        null
      }
    }

private fun launchCamera(
    context: Context,
    takePictureLauncher: ActivityResultLauncher<Uri>,
    onUriCreated: (Uri?) -> Unit
) {
  val uri = createTempImageUri(context)
  if (uri == null) {
    Toast.makeText(context, "Unable to access camera storage", Toast.LENGTH_LONG).show()
    onUriCreated(null)
    return
  }
  onUriCreated(uri)
  takePictureLauncher.launch(uri)
}

private fun createTempImageUri(context: Context): Uri? =
    runCatching {
          val imagesDir = File(context.cacheDir, "picture_picker").apply { if (!exists()) mkdirs() }
          val imageFile = File.createTempFile("capture_", ".jpg", imagesDir)
          FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", imageFile)
        }
        .getOrNull()

private fun decodeStream(stream: InputStream): Bitmap? = BitmapFactory.decodeStream(stream)
