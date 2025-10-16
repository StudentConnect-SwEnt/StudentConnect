package com.github.se.studentconnect.ui.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrCodeAnalyzerTest {

  @Test
  fun extractUserId_returnsRawWhenNoUrl() {
    val result = QrCodeAnalyzer.extractUserId("user-42", null)
    assertEquals("user-42", result)
  }

  @Test
  fun extractUserId_prefersUrlQueryParameter() {
    val result =
        QrCodeAnalyzer.extractUserId(
            rawValue = "ignored", url = "https://studentconnect.ch/profile?userId=student-12")

    assertEquals("student-12", result)
  }

  @Test
  fun extractUserId_usesLastPathSegment() {
    val result =
        QrCodeAnalyzer.extractUserId(
            rawValue = null, url = "https://studentconnect.ch/profiles/student-31")

    assertEquals("student-31", result)
  }

  @Test
  fun extractUserId_handlesQueryLikePayload() {
    val result = QrCodeAnalyzer.extractUserId("userId=student-55&foo=bar", null)
    assertEquals("student-55", result)
  }

  @Test
  fun extractUserId_returnsNullWhenEmpty() {
    val result = QrCodeAnalyzer.extractUserId("  ", null)
    assertNull(result)
  }
}
