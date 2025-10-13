package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GoogleSignInConfigTest {

  @Test
  fun `default web client id is present and well formed`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val resId =
        context.resources.getIdentifier("default_web_client_id", "string", context.packageName)

    assertNotEquals("Missing default_web_client_id string resource", 0, resId)

    val clientId = context.getString(resId)
    assertTrue(
        "Google client ID should end with googleusercontent.com",
        clientId.endsWith(".googleusercontent.com"))
  }
}
