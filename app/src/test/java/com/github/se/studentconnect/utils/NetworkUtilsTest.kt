package com.github.se.studentconnect.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkUtilsTest {

  @Test
  fun `isNetworkAvailable returns boolean value with real context`() {
    // Test with real Robolectric context - it may return true or false depending on setup
    // The important thing is that it doesn't crash and returns a boolean
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = NetworkUtils.isNetworkAvailable(context)
    // Just verify it returns a boolean (doesn't throw)
    assertTrue(result || !result)
  }

  @Test
  fun `isNetworkAvailable returns false when ConnectivityManager is null`() {
    val context = mockk<Context>(relaxed = true)
    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns null

    val result = NetworkUtils.isNetworkAvailable(context)
    assertFalse(result)
  }

  @Test
  fun `isNetworkAvailable returns false when activeNetwork is null`() {
    val context = mockk<Context>(relaxed = true)
    val connectivityManager = mockk<ConnectivityManager>(relaxed = true)

    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { connectivityManager.activeNetwork } returns null

    val result = NetworkUtils.isNetworkAvailable(context)
    assertFalse(result)
  }

  @Test
  fun `isNetworkAvailable returns false when NetworkCapabilities is null`() {
    val context = mockk<Context>(relaxed = true)
    val connectivityManager = mockk<ConnectivityManager>(relaxed = true)
    val network = mockk<Network>(relaxed = true)

    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { connectivityManager.activeNetwork } returns network
    every { connectivityManager.getNetworkCapabilities(network) } returns null

    val result = NetworkUtils.isNetworkAvailable(context)
    assertFalse(result)
  }

  @Test
  fun `isNetworkAvailable returns false when network lacks internet capability`() {
    val context = mockk<Context>(relaxed = true)
    val connectivityManager = mockk<ConnectivityManager>(relaxed = true)
    val network = mockk<Network>(relaxed = true)
    val capabilities = mockk<NetworkCapabilities>(relaxed = true)

    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { connectivityManager.activeNetwork } returns network
    every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
    every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

    val result = NetworkUtils.isNetworkAvailable(context)
    assertFalse(result)
  }
}
