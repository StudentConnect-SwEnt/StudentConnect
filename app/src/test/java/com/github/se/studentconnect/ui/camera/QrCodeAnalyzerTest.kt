package com.github.se.studentconnect.ui.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrCodeAnalyzerTest {

  // ===== Basic extractUserId tests =====
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

  // ===== Additional edge case tests =====
  @Test
  fun extractUserId_returnsNullWhenBothNull() {
    val result = QrCodeAnalyzer.extractUserId(null, null)
    assertNull(result)
  }

  @Test
  fun extractUserId_returnsNullWhenBothEmpty() {
    val result = QrCodeAnalyzer.extractUserId("", "")
    assertNull(result)
  }

  @Test
  fun extractUserId_handlesUrlWithSchemeButNoUserIdOrPath() {
    val result = QrCodeAnalyzer.extractUserId("fallback", "https://studentconnect.ch")
    assertEquals("fallback", result)
  }

  @Test
  fun extractUserId_handlesUrlWithSchemeButEmptyPath() {
    val result = QrCodeAnalyzer.extractUserId("fallback", "https://studentconnect.ch/")
    assertEquals("fallback", result)
  }

  @Test
  fun extractUserId_handlesUrlWithBlankUserId() {
    val result = QrCodeAnalyzer.extractUserId("fallback", "https://studentconnect.ch?userId=  ")
    assertEquals("fallback", result)
  }

  @Test
  fun extractUserId_handlesUrlWithEmptyUserId() {
    val result = QrCodeAnalyzer.extractUserId("fallback", "https://studentconnect.ch?userId=")
    assertEquals("fallback", result)
  }

  @Test
  fun extractUserId_handlesUrlWithBlankPathSegment() {
    val result = QrCodeAnalyzer.extractUserId("fallback", "https://studentconnect.ch/  ")
    assertEquals("fallback", result)
  }

  @Test
  fun extractUserId_handlesRawUrlInRawValue() {
    val result =
        QrCodeAnalyzer.extractUserId("https://studentconnect.ch/profile?userId=student-99", null)
    assertEquals("student-99", result)
  }

  @Test
  fun extractUserId_handlesRawUrlWithPathInRawValue() {
    val result = QrCodeAnalyzer.extractUserId("https://studentconnect.ch/profiles/student-77", null)
    assertEquals("student-77", result)
  }

  @Test
  fun extractUserId_urlParameterTakesPriorityOverRawUrl() {
    val result =
        QrCodeAnalyzer.extractUserId(
            rawValue = "https://studentconnect.ch/profile?userId=student-raw",
            url = "https://studentconnect.ch/profile?userId=student-url")
    assertEquals("student-url", result)
  }

  @Test
  fun extractUserId_handlesQueryLikePayloadCaseInsensitive() {
    // The code detects "userId=" case-insensitively but extracts case-sensitively
    // So mixed case falls back to raw value
    val result = QrCodeAnalyzer.extractUserId("UsErId=student-case", null)
    assertEquals("UsErId=student-case", result)
  }

  @Test
  fun extractUserId_handlesQueryLikePayloadUpperCase() {
    // The code detects "userId=" case-insensitively but extracts case-sensitively
    // So uppercase falls back to raw value
    val result = QrCodeAnalyzer.extractUserId("USERID=student-upper", null)
    assertEquals("USERID=student-upper", result)
  }

  @Test
  fun extractUserId_handlesQueryLikePayloadExactCase() {
    // When exact case matches, it extracts correctly
    val result = QrCodeAnalyzer.extractUserId("userId=student-exact", null)
    assertEquals("student-exact", result)
  }

  @Test
  fun extractUserId_handlesQueryLikePayloadWithMultipleParams() {
    val result = QrCodeAnalyzer.extractUserId("foo=bar&userId=student-mid&baz=qux", null)
    assertEquals("student-mid", result)
  }

  @Test
  fun extractUserId_handlesQueryLikePayloadWithEmptyValue() {
    val result = QrCodeAnalyzer.extractUserId("userId=&foo=bar", null)
    assertEquals("userId=&foo=bar", result) // Falls back to raw since userId is empty
  }

  @Test
  fun extractUserId_handlesQueryLikePayloadWithBlankValue() {
    val result = QrCodeAnalyzer.extractUserId("userId=   &foo=bar", null)
    assertEquals("userId=   &foo=bar", result) // Falls back to raw since userId is blank
  }

  @Test
  fun extractUserId_trimsWhitespaceFromRawValue() {
    val result = QrCodeAnalyzer.extractUserId("  user-with-spaces  ", null)
    assertEquals("user-with-spaces", result)
  }

  @Test
  fun extractUserId_handlesUrlWithoutScheme() {
    val result = QrCodeAnalyzer.extractUserId("fallback", "studentconnect.ch/profile?userId=test")
    assertEquals("fallback", result)
  }

  @Test
  fun extractUserId_handlesUrlWithComplexPath() {
    val result = QrCodeAnalyzer.extractUserId(null, "https://studentconnect.ch/a/b/c/student-deep")
    assertEquals("student-deep", result)
  }

  @Test
  fun extractUserId_handlesUrlWithQueryAndFragment() {
    val result =
        QrCodeAnalyzer.extractUserId(
            null, "https://studentconnect.ch/profile?userId=student-123#section")
    assertEquals("student-123", result)
  }

  @Test
  fun extractUserId_handlesUrlWithMultipleQueryParams() {
    val result =
        QrCodeAnalyzer.extractUserId(
            null, "https://studentconnect.ch/profile?foo=bar&userId=student-multi&baz=qux")
    assertEquals("student-multi", result)
  }

  @Test
  fun extractUserId_handlesUrlWithPort() {
    val result =
        QrCodeAnalyzer.extractUserId(
            null, "https://studentconnect.ch:8080/profile?userId=student-port")
    assertEquals("student-port", result)
  }

  @Test
  fun extractUserId_handlesFileUrl() {
    val result = QrCodeAnalyzer.extractUserId("fallback", "file:///path/to/file")
    assertEquals("file", result) // last path segment
  }

  @Test
  fun extractUserId_handlesDataUrl() {
    val result = QrCodeAnalyzer.extractUserId("fallback", "data:text/plain,student-data")
    assertEquals("fallback", result) // data URLs don't have standard paths
  }

  @Test
  fun extractUserId_handlesNullRawWithValidUrl() {
    val result =
        QrCodeAnalyzer.extractUserId(
            null, "https://studentconnect.ch/profile?userId=student-only-url")
    assertEquals("student-only-url", result)
  }

  @Test
  fun extractUserId_handlesSpecialCharactersInUserId() {
    val result =
        QrCodeAnalyzer.extractUserId(
            null, "https://studentconnect.ch/profile?userId=user-123_ABC.xyz")
    assertEquals("user-123_ABC.xyz", result)
  }

  @Test
  fun extractUserId_handlesEncodedUserId() {
    // Android Uri automatically decodes URL-encoded strings
    val result =
        QrCodeAnalyzer.extractUserId(null, "https://studentconnect.ch/profile?userId=user%20space")
    assertEquals("user space", result)
  }
}
