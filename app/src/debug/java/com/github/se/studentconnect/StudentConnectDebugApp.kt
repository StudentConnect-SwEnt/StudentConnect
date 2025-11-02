// Portions of this code were generated with the help of ChatGPT
package com.github.se.studentconnect

import android.app.Application
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

/**
 * Debug-only application class that forces Firebase SDKs to hit the local emulator suite.
 *
 * The Android emulator exposes the host machine on 10.0.2.2, while a physical device needs the
 * host's LAN IP. We detect emulators using the build fingerprint to choose the right host.
 */
class StudentConnectDebugApp : Application() {
  private companion object {
    const val EMULATOR_LOOPBACK = "10.0.2.2"
    const val LOCALHOST = "localhost"
    const val AUTH_PORT = 9099
    const val FIRESTORE_PORT = 8080
    const val STORAGE_PORT = 9199
  }

  private fun isInAndroidEmulator(): Boolean {
    val fingerprint = Build.FINGERPRINT.lowercase()
    val model = Build.MODEL.lowercase()
    val product = Build.PRODUCT.lowercase()
    val brand = Build.BRAND.lowercase()
    val device = Build.DEVICE.lowercase()
    val manufacturer = Build.MANUFACTURER.lowercase()

    return (fingerprint.startsWith("generic") ||
        fingerprint.contains("vbox") ||
        fingerprint.contains("test-keys") ||
        model.contains("google_sdk") ||
        model.contains("emulator") ||
        model.contains("android sdk built for x86") ||
        model.contains("sdk_gphone") ||
        manufacturer.contains("genymotion") ||
        (brand.startsWith("generic") && device.startsWith("generic")) ||
        product == "google_sdk" ||
        product.contains("sdk_gphone"))
  }

  override fun onCreate() {
    super.onCreate()

    // use firebase emulator (in debug app only) if -PuseFirebaseEmulator=true is set
    if (BuildConfig.USE_FIREBASE_EMULATOR) {
      val host = if (isInAndroidEmulator()) EMULATOR_LOOPBACK else LOCALHOST

      Firebase.auth.useEmulator(host, AUTH_PORT)
      Firebase.firestore.useEmulator(host, FIRESTORE_PORT)
      Firebase.storage.useEmulator(host, STORAGE_PORT)
    }
  }
}
