// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.utils

import android.os.Build
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import io.mockk.InternalPlatformDsl.toArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * An object to manage the connection to Firebase Emulators for Android tests.
 *
 * This object will automatically use the emulators if they are running when the tests start.
 */
object FirebaseEmulator {
  @Volatile
  private var emulatorConfigured = false
  
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

  val HOST = if (isInAndroidEmulator()) "10.0.2.2" else "localhost"
  const val EMULATORS_PORT = 4400
  const val FIRESTORE_PORT = 8080
  const val AUTH_PORT = 9099
  const val STORAGE_PORT = 9199

  val projectID by lazy { FirebaseApp.getInstance().options.projectId }

  private val httpClient = OkHttpClient()
  private val firestoreEndpoint by lazy {
    "http://${HOST}:$FIRESTORE_PORT/emulator/v1/projects/$projectID/databases/(default)/documents"
  }

  private val authEndpoint by lazy {
    "http://${HOST}:$AUTH_PORT/emulator/v1/projects/$projectID/accounts"
  }

  private val emulatorsEndpoint = "http://$HOST:$EMULATORS_PORT/emulators"

  private fun areEmulatorsRunning(): Boolean =
      runCatching {
            val client = httpClient
            val request = Request.Builder().url(emulatorsEndpoint).build()
            client.newCall(request).execute().isSuccessful
          }
          .getOrNull() == true

  val isRunning = areEmulatorsRunning()

  init {
    configureEmulators()
  }
  
  @Synchronized
  private fun configureEmulators() {
    if (!emulatorConfigured && isRunning) {
      try {
        Firebase.auth.useEmulator(HOST, AUTH_PORT)
        Firebase.firestore.useEmulator(HOST, FIRESTORE_PORT)
        Firebase.storage.useEmulator(HOST, STORAGE_PORT)
        emulatorConfigured = true
        assert(Firebase.firestore.firestoreSettings.host.contains(HOST)) {
          "Failed to connect to Firebase Firestore Emulator."
        }
      } catch (e: IllegalStateException) {
        // Emulator already configured, log and continue
        Log.w("FirebaseEmulator", "Emulator already configured: ${e.message}")
        emulatorConfigured = true
      }
    }
  }

  val auth
    get() = Firebase.auth

  val firestore
    get() = Firebase.firestore

  val storage
    get() = Firebase.storage
  
  private val storageEndpoint by lazy {
    "http://${HOST}:$STORAGE_PORT/v0/b/${storage.app.options.storageBucket}/o"
  }

  private fun clearEmulator(endpoint: String) {
    val client = httpClient
    val request = Request.Builder().url(endpoint).delete().build()
    val response = client.newCall(request).execute()

    assert(response.isSuccessful) { "Failed to clear emulator at $endpoint" }
  }

  fun clearAuthEmulator() {
    clearEmulator(authEndpoint)
  }

  fun clearFirestoreEmulator() {
    clearEmulator(firestoreEndpoint)
  }

  fun clearStorageEmulator() {
    val endpoint = storageEndpoint
    val client = httpClient

    // List all objects
    val listRequest = Request.Builder().url(endpoint).get().build()
    val listResponse = client.newCall(listRequest).execute()
    if (!listResponse.isSuccessful) {
      println("Failed to list storage objects: ${listResponse.code}")
      return
    }

    val body = listResponse.body?.string() ?: return
    val json = org.json.JSONObject(body)
    val items = json.optJSONArray("items") ?: return

    // Delete each object one by one
    for (i in 0 until items.length()) {
      val name = items.getJSONObject(i).getString("name")
      val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
      val deleteUrl = "$endpoint/$encodedName"

      val deleteRequest = Request.Builder().url(deleteUrl).delete().build()
      val deleteResponse = client.newCall(deleteRequest).execute()
      assert(deleteResponse.isSuccessful) {
        "Failed to clear file '$name' at $deleteUrl (code: ${deleteResponse.code})"
      }
    }
  }

  /**
   * Seeds a Google user in the Firebase Auth Emulator using a fake JWT id_token.
   *
   * @param fakeIdToken A JWT-shaped string, must contain at least "sub".
   * @param email The email address to associate with the account.
   */
  fun createGoogleUser(fakeIdToken: String) {
    val url =
        "http://$HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=fake-api-key"

    // postBody must be x-www-form-urlencoded style string, wrapped in JSON
    val postBody = "id_token=$fakeIdToken&providerId=google.com"

    val requestJson =
        JSONObject().apply {
          put("postBody", postBody)
          put("requestUri", "http://localhost")
          put("returnIdpCredential", true)
          put("returnSecureToken", true)
        }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = requestJson.toString().toRequestBody(mediaType)

    val request =
        Request.Builder().url(url).post(body).addHeader("Content-Type", "application/json").build()

    val response = httpClient.newCall(request).execute()
    assert(response.isSuccessful) {
      "Failed to create user in Auth Emulator: ${response.code} ${response.message}"
    }
  }

  fun changeEmail(fakeIdToken: String, newEmail: String) {
    val response =
        httpClient
            .newCall(
                Request.Builder()
                    .url(
                        "http://$HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:update?key=fake-api-key")
                    .post(
                        """
            {
                "idToken": "$fakeIdToken",
                "email": "$newEmail",
                "returnSecureToken": true
            }
        """
                            .trimIndent()
                            .toRequestBody())
                    .build())
            .execute()
    assert(response.isSuccessful) {
      "Failed to change email in Auth Emulator: ${response.code} ${response.message}"
    }
  }

  val users: String
    get() {
      val request =
          Request.Builder()
              .url(
                  "http://$HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:query?key=fake-api-key")
              .build()

      Log.d("FirebaseEmulator", "Fetching users with request: ${request.url.toString()}")
      val response = httpClient.newCall(request).execute()
      Log.d("FirebaseEmulator", "Response received: ${response.toArray()}")
      return response.body.toString()
    }
}
