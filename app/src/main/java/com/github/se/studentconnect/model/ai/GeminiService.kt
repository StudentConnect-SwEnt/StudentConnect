package com.github.se.studentconnect.model.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.google.ai.client.generativeai.GenerativeModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * * Service to generate assets using a Hybrid AI approach. Uses Gemini for Prompt Engineering. Uses
 *   Pollinations.ai (Open Source) for Image Synthesis.
 *
 *   We use Pollinations.ai because:
 *     1. It is free to use.
 *     2. It provides high-quality results.
 *     3. It is open source, aligning with transparency and community-driven development.
 */
class GeminiService(
    private val client: okhttp3.OkHttpClient =
        okhttp3.OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
            .build(),
    private val apiKeyProvider: () -> String = {
      com.github.se.studentconnect.BuildConfig.GEMINI_API_KEY
    }
) {

  companion object {
    private const val TIMEOUT_DURATION = 60L
    private const val MODEL_NAME = "gemini-2.5-flash"
    private const val TAG = "GeminiService"

    private const val IMAGE_WIDTH = 1280
    private const val IMAGE_HEIGHT = 720
    private const val JPEG_QUALITY = 90

    private const val CACHE_DIR = "gemini_images"
    private const val FILE_PREFIX = "banner_"
    private const val FILE_EXT = ".jpg"

    private const val FALLBACK_COLOR_START = "#6200EE"
    private const val FALLBACK_COLOR_END = "#03DAC5"
    private const val FALLBACK_TEXT_SIZE = 80f
    private const val FALLBACK_SHADOW_RADIUS = 10f

    private const val MAX_TITLE_LEN = 20
    private const val TRUNCATED_LEN = 17
    private const val ELLIPSIS = "..."

    private const val PROMPT_TEMPLATE =
        "You are an expert image generation prompter. " +
            "Create a detailed, descriptive prompt for an AI image generator based on this event:\n" +
            "Title: %s\n" +
            "Description: %s\n" +
            "User Style Request: %s\n\n" +
            "Rules: Return ONLY the raw English prompt text. No markdown, no explanations. " +
            "Focus on visual details, lighting, and style. Keep it under 40 words."

    private const val IMAGE_GEN_URL_TEMPLATE =
        "https://image.pollinations.ai/prompt/%s?width=%d&height=%d&nologo=true&seed=%d&model=gemini"
  }

  private val apiKey: String
    get() = apiKeyProvider()

  private val textModel by lazy { GenerativeModel(modelName = MODEL_NAME, apiKey = apiKey) }

  /**
   * Generates a banner image.
   * 1. Asks Gemini to enhance the user prompt (Text-to-Text).
   * 2. Sends the enhanced prompt to an open image generation API (Text-to-Image).
   */
  suspend fun generateBanner(
      context: Context,
      prompt: String,
      eventTitle: String,
      eventDescription: String
  ): Uri? =
      withContext(Dispatchers.IO) {
        // Step 1: Enhance the prompt using Gemini
        // We take the user's simple input and turn it into a detailed, artistic prompt
        val enhancedPrompt =
            try {
              if (apiKey.isNotBlank() && apiKey != "NULL") {
                val response =
                    textModel.generateContent(
                        com.google.ai.client.generativeai.type.content {
                          text(
                              String.format(
                                  Locale.US, PROMPT_TEMPLATE, eventTitle, eventDescription, prompt))
                        })
                // Clean up the response to ensure it's just the prompt text
                response.text?.replace("\n", " ")?.trim() ?: prompt
              } else {
                Log.w(TAG, "API Key missing, using raw prompt")
                "$prompt $eventTitle event banner"
              }
            } catch (e: Exception) {
              Log.e(TAG, "Gemini Prompt enhancement failed", e)
              "$prompt $eventTitle" // Fallback to simple prompt if AI enhancement fails
            }

        Log.d(TAG, "Enhanced Prompt: $enhancedPrompt")

        val encodedPrompt = Uri.encode(enhancedPrompt)

        // Step 2: Generate the image using Pollinations.ai
        // We construct a URL with the prompt and parameters (width, height, seed)
        val imageUrl =
            String.format(
                Locale.US,
                IMAGE_GEN_URL_TEMPLATE,
                encodedPrompt,
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                System.currentTimeMillis())

        try {
          val request = okhttp3.Request.Builder().url(imageUrl).build()
          val response = client.newCall(request).execute()

          if (!response.isSuccessful) {
            Log.e(TAG, "Image Gen failed: ${response.code}")
            // If generation fails, return a generated gradient image with the title
            return@withContext generateFallbackImage(context, eventTitle)
          }

          val inputStream = response.body?.byteStream()
          val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

          if (bitmap != null) {
            return@withContext saveBitmapToCache(context, bitmap)
          } else {
            return@withContext generateFallbackImage(context, eventTitle)
          }
        } catch (e: Exception) {
          Log.e(TAG, "Exception downloading image", e)
          return@withContext generateFallbackImage(context, eventTitle)
        }
      }

  private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val imagesDir = File(context.cacheDir, CACHE_DIR)
    if (!imagesDir.exists()) imagesDir.mkdirs()

    val file = File(imagesDir, "$FILE_PREFIX${System.currentTimeMillis()}$FILE_EXT")

    try {
      val out = FileOutputStream(file)
      bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
      out.flush()
      out.close()
    } catch (e: java.io.IOException) {
      Log.e(TAG, "Error saving bitmap", e)
    }

    val authority = "${context.packageName}.provider"

    return try {
      FileProvider.getUriForFile(context, authority, file)
    } catch (_: Exception) {
      Uri.fromFile(file)
    }
  }

  private fun generateFallbackImage(context: Context, title: String): Uri {
    val bitmap = createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint =
        Paint().apply {
          shader =
              LinearGradient(
                  0f,
                  0f,
                  IMAGE_WIDTH.toFloat(),
                  IMAGE_HEIGHT.toFloat(),
                  FALLBACK_COLOR_START.toColorInt(),
                  FALLBACK_COLOR_END.toColorInt(),
                  Shader.TileMode.CLAMP)
        }
    canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), IMAGE_HEIGHT.toFloat(), paint)

    val textPaint =
        Paint().apply {
          color = Color.WHITE
          textSize = FALLBACK_TEXT_SIZE
          typeface = Typeface.DEFAULT_BOLD
          textAlign = Paint.Align.CENTER
          setShadowLayer(FALLBACK_SHADOW_RADIUS, 0f, 0f, Color.BLACK)
        }

    val xPos = (IMAGE_WIDTH / 2).toFloat()
    val yPos = (IMAGE_HEIGHT / 2 - ((textPaint.descent() + textPaint.ascent()) / 2))

    val displayTitle =
        if (title.length > MAX_TITLE_LEN) title.take(TRUNCATED_LEN) + ELLIPSIS else title
    canvas.drawText(displayTitle, xPos, yPos, textPaint)

    return saveBitmapToCache(context, bitmap)
  }
}
