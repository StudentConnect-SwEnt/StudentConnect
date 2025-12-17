package com.github.se.studentconnect.ui.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationEvent
import com.github.se.studentconnect.model.user.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads a bitmap from the given URI using the specified dispatcher. Returns null if the image could
 * not be loaded. This function is suspendable and should be called from a coroutine context.
 *
 * @param context The context to access content resolver.
 * @param uri The URI of the image to load.
 * @param dispatcher The coroutine dispatcher to use for loading the image.
 * @return The loaded ImageBitmap or null if loading failed.
 */
suspend fun loadBitmapFromUri(
    context: Context,
    uri: Uri,
    dispatcher: CoroutineDispatcher
): ImageBitmap? =
    withContext(dispatcher) {
      try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
      } catch (_: Exception) {
        null
      }
    }

/**
 * Composable function to load a bitmap from a URI string. Utilizes MediaRepository to download the
 * image and then loads it as an ImageBitmap.
 *
 * @param context The context to access content resolver.
 * @param uri The URI string of the image to load.
 * @return The loaded ImageBitmap or null if loading failed.
 */
suspend fun loadBitmapFromStringUri(
    context: Context,
    uri: String?,
): ImageBitmap? {
  val repository = MediaRepositoryProvider.repository
  val imageBitmap =
      uri?.let { id ->
        runCatching { repository.download(id) }
            .onFailure { Log.e("eventViewImage", "Failed to download user image: $id", it) }
            .getOrNull()
            ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
      }
  return imageBitmap
}

/**
 * Composable function to load a bitmap from a User's profile picture URL.
 *
 * @param context The context to access content resolver.
 * @param user The User object containing the profile picture URL.
 * @return The loaded ImageBitmap or null if loading failed.
 */
suspend fun loadBitmapFromUser(context: Context, user: User): ImageBitmap? =
    loadBitmapFromStringUri(context, user.profilePictureUrl)

/**
 * Composable function to load a bitmap from an Event's image URL.
 *
 * @param context The context to access content resolver.
 * @param event The Event object containing the image URL.
 * @return The loaded ImageBitmap or null if loading failed.
 */
suspend fun loadBitmapFromEvent(context: Context, event: Event): ImageBitmap? =
    loadBitmapFromStringUri(context, event.imageUrl)

/**
 * Composable function to load a bitmap from an Organization's logo URL.
 *
 * @param context The context to access content resolver.
 * @param organization The Organization object containing the logo URL.
 * @return The loaded ImageBitmap or null if loading failed.
 */
suspend fun loadBitmapFromOrganization(context: Context, organization: Organization): ImageBitmap? =
    loadBitmapFromStringUri(context, organization.logoUrl)

/**
 * Composable function to load a bitmap from an OrganizationEvent's image URL.
 *
 * @param context The context to access content resolver.
 * @param organizationEvent The OrganizationEvent object containing the image URL.
 * @return The loaded ImageBitmap or null if loading failed.
 */
suspend fun loadBitmapFromOrganizationEvent(
    context: Context,
    organizationEvent: OrganizationEvent
) = loadBitmapFromStringUri(context, organizationEvent.imageUrl)
