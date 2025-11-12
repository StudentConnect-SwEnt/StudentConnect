package com.github.se.studentconnect.resources

import androidx.annotation.StringRes

/**
 * Interface for providing string resources to ViewModels and other non-UI components.
 * This allows ViewModels to access string resources without depending on Android Context directly,
 * making them more testable and platform-agnostic.
 */
interface ResourceProvider {
  /**
   * Retrieves a string resource by its resource ID.
   *
   * @param resId The string resource ID (annotated with @StringRes for compile-time safety)
   * @return The localized string value
   */
  fun getString(@StringRes resId: Int): String

  /**
   * Retrieves a formatted string resource with arguments.
   *
   * @param resId The string resource ID (annotated with @StringRes for compile-time safety)
   * @param formatArgs The format arguments to substitute into the string
   * @return The formatted, localized string value
   */
  fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
}

/**
 * Android-specific implementation of [ResourceProvider].
 * Uses an Android [Context] to retrieve string resources from the application's `strings.xml`.
 *
 * @param context The Android Context used to access string resources
 */
class AndroidResourceProvider(private val context: android.content.Context) : ResourceProvider {
  override fun getString(@StringRes resId: Int): String = context.getString(resId)

  override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
      context.getString(resId, *formatArgs)
}

