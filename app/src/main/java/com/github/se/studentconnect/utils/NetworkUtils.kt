package com.github.se.studentconnect.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utility object for checking network connectivity status.
 *
 * Provides a centralized way to check if the device has an active internet connection.
 */
object NetworkUtils {
  /**
   * Checks if the device has an active internet connection.
   *
   * @param context The application context
   * @return true if network is available and has internet capability, false otherwise
   */
  fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      ?: return false
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}
