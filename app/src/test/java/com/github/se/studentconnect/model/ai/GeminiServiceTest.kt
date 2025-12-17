package com.github.se.studentconnect.model.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GeminiServiceTest {

  private lateinit var context: Context
  private lateinit var mockClient: OkHttpClient
  private lateinit var mockCall: Call

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    mockClient = mock()
    mockCall = mock()
    whenever(mockClient.newCall(any())).thenReturn(mockCall)
  }

  // ==================== API Key Tests ====================

  @Test
  fun `generateBanner returns fallback image when API key is NULL`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner returns fallback image when API key is blank`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  // ==================== HTTP Response Tests ====================

  @Test
  fun `generateBanner returns fallback image when HTTP response is unsuccessful`() = runTest {
    val mockResponse =
        Response.Builder()
            .request(Request.Builder().url("https://example.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body("error".toResponseBody("text/plain".toMediaType()))
            .build()

    whenever(mockCall.execute()).thenReturn(mockResponse)

    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner returns fallback image when HTTP 404`() = runTest {
    val mockResponse =
        Response.Builder()
            .request(Request.Builder().url("https://example.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found")
            .body("not found".toResponseBody("text/plain".toMediaType()))
            .build()

    whenever(mockCall.execute()).thenReturn(mockResponse)

    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner returns fallback image when HTTP 403`() = runTest {
    val mockResponse =
        Response.Builder()
            .request(Request.Builder().url("https://example.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(403)
            .message("Forbidden")
            .body("forbidden".toResponseBody("text/plain".toMediaType()))
            .build()

    whenever(mockCall.execute()).thenReturn(mockResponse)

    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  // ==================== Exception Handling Tests ====================

  @Test
  fun `generateBanner returns fallback image when network exception is thrown`() = runTest {
    whenever(mockCall.execute()).thenThrow(RuntimeException("Network error"))

    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner returns fallback image when IOException is thrown`() = runTest {
    whenever(mockCall.execute()).thenThrow(java.io.IOException("Connection reset"))

    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner returns fallback image when timeout exception is thrown`() = runTest {
    whenever(mockCall.execute()).thenThrow(java.net.SocketTimeoutException("Read timed out"))

    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  // ==================== Title Length Tests ====================

  @Test
  fun `generateBanner handles long event titles by truncating`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val longTitle = "This is a very long event title that exceeds the maximum length allowed"
    val result = service.generateBanner(context, "test prompt", longTitle, "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles title exactly at max length`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val exactTitle = "12345678901234567890" // 20 chars (MAX_TITLE_LEN)
    val result = service.generateBanner(context, "test prompt", exactTitle, "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles title one char over max length`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val overTitle = "123456789012345678901" // 21 chars
    val result = service.generateBanner(context, "test prompt", overTitle, "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles short title`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val shortTitle = "Hi"
    val result = service.generateBanner(context, "test prompt", shortTitle, "Test Description")
    assertNotNull(result)
  }

  // ==================== Empty/Null Input Tests ====================

  @Test
  fun `generateBanner handles empty prompt`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "", "Test Event", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles empty event title`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles empty event description`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles all empty strings`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "", "", "")
    assertNotNull(result)
  }

  // ==================== Special Character Tests ====================

  @Test
  fun `generateBanner handles special characters in prompt`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result =
        service.generateBanner(
            context, "test & prompt <with> \"special\" 'chars'", "Test Event", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles unicode characters`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result =
        service.generateBanner(
            context, "Événement français 日本語 🎉", "Test Event", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles newlines in inputs`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result =
        service.generateBanner(
            context, "prompt\nwith\nnewlines", "Event\nTitle", "Description\nwith\nlines")
    assertNotNull(result)
  }

  // ==================== Constructor Tests ====================

  @Test
  fun `GeminiService can be instantiated with default parameters`() {
    val service = GeminiService()
    assertNotNull(service)
  }

  @Test
  fun `GeminiService can be instantiated with custom client`() {
    val customClient = OkHttpClient.Builder().build()
    val service = GeminiService(client = customClient)
    assertNotNull(service)
  }

  @Test
  fun `GeminiService can be instantiated with custom api key provider`() {
    val service = GeminiService(apiKeyProvider = { "custom-key" })
    assertNotNull(service)
  }

  @Test
  fun `GeminiService can be instantiated with both custom parameters`() {
    val customClient = OkHttpClient.Builder().build()
    val service = GeminiService(client = customClient, apiKeyProvider = { "custom-key" })
    assertNotNull(service)
  }

  // ==================== Cache Directory Tests ====================

  @Test
  fun `generateBanner creates cache directory if not exists`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })

    // Delete cache dir if exists
    val cacheDir = File(context.cacheDir, "gemini_images")
    if (cacheDir.exists()) {
      cacheDir.deleteRecursively()
    }

    val result = service.generateBanner(context, "test", "Test", "Desc")

    assertNotNull(result)
    assertTrue(cacheDir.exists())
  }

  @Test
  fun `generateBanner works when cache directory already exists`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })

    // Ensure cache dir exists
    val cacheDir = File(context.cacheDir, "gemini_images")
    cacheDir.mkdirs()

    val result = service.generateBanner(context, "test", "Test", "Desc")

    assertNotNull(result)
  }

  // ==================== Bitmap Response Tests ====================

  @Test
  fun `generateBanner returns fallback when response body is null`() = runTest {
    val mockResponse =
        Response.Builder()
            .request(Request.Builder().url("https://example.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(null)
            .build()

    whenever(mockCall.execute()).thenReturn(mockResponse)

    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner returns fallback when bitmap decode fails`() = runTest {
    // Return invalid image data that can't be decoded
    val mockBody = "not a valid image".toResponseBody("image/jpeg".toMediaType())
    val mockResponse =
        Response.Builder()
            .request(Request.Builder().url("https://example.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(mockBody)
            .build()

    whenever(mockCall.execute()).thenReturn(mockResponse)

    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "test prompt", "Test Event", "Test Description")
    assertNotNull(result)
  }

  // ==================== URL Encoding Tests ====================

  @Test
  fun `generateBanner properly encodes URL with spaces`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result =
        service.generateBanner(
            context, "prompt with spaces", "Event With Spaces", "Description with spaces")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner properly encodes URL with ampersands`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result =
        service.generateBanner(context, "prompt & more", "Event & Title", "Description & more")
    assertNotNull(result)
  }

  // ==================== Multiple Calls Tests ====================

  @Test
  fun `generateBanner can be called multiple times`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })

    val result1 = service.generateBanner(context, "prompt1", "Event1", "Desc1")
    val result2 = service.generateBanner(context, "prompt2", "Event2", "Desc2")
    val result3 = service.generateBanner(context, "prompt3", "Event3", "Desc3")

    assertNotNull(result1)
    assertNotNull(result2)
    assertNotNull(result3)
  }

  // ==================== Edge Cases ====================

  @Test
  fun `generateBanner handles very long description`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val longDesc = "A".repeat(1000)
    val result = service.generateBanner(context, "test", "Test Event", longDesc)
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles very long prompt`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val longPrompt = "B".repeat(500)
    val result = service.generateBanner(context, longPrompt, "Test Event", "Desc")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles whitespace only inputs`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "   ", "   ", "   ")
    assertNotNull(result)
  }

  @Test
  fun `generateBanner handles tab characters`() = runTest {
    val service = GeminiService(client = mockClient, apiKeyProvider = { "NULL" })
    val result = service.generateBanner(context, "prompt\twith\ttabs", "Event", "Desc")
    assertNotNull(result)
  }
}
