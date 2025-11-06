package com.github.se.studentconnect.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class BirthdayFormatterTest {

  @Test
  fun `dateFormatter returns non-null SimpleDateFormat`() {
    val formatter = BirthdayFormatter.dateFormatter
    assertNotNull(formatter)
  }

  @Test
  fun `dateFormatter has correct pattern`() {
    val formatter = BirthdayFormatter.dateFormatter
    // Test by formatting a known date
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val formatted = formatter.format(calendar.time)
    assertEquals("15/01/2000", formatted)
  }

  @Test
  fun `formatDate formats milliseconds correctly`() {
    // Create a specific date: January 15, 2000
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    val formatted = BirthdayFormatter.formatDate(millis)
    assertEquals("15/01/2000", formatted)
  }

  @Test
  fun `formatDate formats different dates correctly`() {
    // Test multiple dates
    val testCases =
        listOf(
            Triple(2000, Calendar.JANUARY, 1) to "01/01/2000",
            Triple(1995, Calendar.DECEMBER, 31) to "31/12/1995",
            Triple(2024, Calendar.MARCH, 15) to "15/03/2024",
            Triple(1990, Calendar.JULY, 4) to "04/07/1990")

    testCases.forEach { (date, expected) ->
      val calendar = Calendar.getInstance(TimeZone.getDefault())
      calendar.set(date.first, date.second, date.third, 0, 0, 0)
      calendar.set(Calendar.MILLISECOND, 0)
      val millis = calendar.timeInMillis

      val formatted = BirthdayFormatter.formatDate(millis)
      assertEquals("Failed for date: $expected", expected, formatted)
    }
  }

  @Test
  fun `formatDate handles single digit days and months with leading zeros`() {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 5, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    val formatted = BirthdayFormatter.formatDate(millis)
    assertEquals("05/01/2000", formatted)
  }

  @Test
  fun `formatDate handles double digit days and months`() {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.NOVEMBER, 25, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    val formatted = BirthdayFormatter.formatDate(millis)
    assertEquals("25/11/2000", formatted)
  }

  @Test
  fun `parseDate parses valid date string correctly`() {
    val dateString = "15/01/2000"
    val millis = BirthdayFormatter.parseDate(dateString)

    assertNotNull(millis)

    // Verify by formatting back
    val formatted = BirthdayFormatter.formatDate(millis!!)
    assertEquals(dateString, formatted)
  }

  @Test
  fun `parseDate parses different valid date strings correctly`() {
    val testCases =
        listOf("01/01/2000", "31/12/1995", "15/03/2024", "04/07/1990", "29/02/2000" // Leap year
        )

    testCases.forEach { dateString ->
      val millis = BirthdayFormatter.parseDate(dateString)
      assertNotNull("Failed to parse: $dateString", millis)

      // Verify by formatting back
      val formatted = BirthdayFormatter.formatDate(millis!!)
      assertEquals("Round-trip failed for: $dateString", dateString, formatted)
    }
  }

  @Test
  fun `parseDate returns null for invalid date string`() {
    val invalidDates =
        listOf(
            "invalid",
            "32/01/2000", // Invalid day
            "01/13/2000", // Invalid month
            "15-01-2000", // Wrong separator
            "2000/01/15", // Wrong order
            "",
            "abc/def/ghij",
            "29/02/2001" // Not a leap year
            )

    invalidDates.forEach { dateString ->
      val millis = BirthdayFormatter.parseDate(dateString)
      assertNull("Should have failed to parse: $dateString", millis)
    }
  }

  @Test
  fun `parseDate returns null for empty string`() {
    val millis = BirthdayFormatter.parseDate("")
    assertNull(millis)
  }

  @Test
  fun `parseDate returns null for null-like strings`() {
    val invalidInputs = listOf("null", "undefined", "   ")

    invalidInputs.forEach { input ->
      val millis = BirthdayFormatter.parseDate(input)
      assertNull("Should have failed to parse: '$input'", millis)
    }
  }

  @Test
  fun `formatDate and parseDate are inverse operations`() {
    // Test that formatting and parsing are inverse operations
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.MARCH, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val originalMillis = calendar.timeInMillis

    val formatted = BirthdayFormatter.formatDate(originalMillis)
    val parsed = BirthdayFormatter.parseDate(formatted)

    assertNotNull(parsed)
    // Format both to compare (to ignore time components)
    val originalFormatted = BirthdayFormatter.formatDate(originalMillis)
    val parsedFormatted = BirthdayFormatter.formatDate(parsed!!)
    assertEquals(originalFormatted, parsedFormatted)
  }

  @Test
  fun `parseDate handles leap year correctly`() {
    val leapYearDate = "29/02/2000"
    val millis = BirthdayFormatter.parseDate(leapYearDate)

    assertNotNull(millis)
    val formatted = BirthdayFormatter.formatDate(millis!!)
    assertEquals(leapYearDate, formatted)
  }

  @Test
  fun `parseDate rejects invalid leap year date`() {
    val invalidLeapYearDate = "29/02/2001" // 2001 is not a leap year
    val millis = BirthdayFormatter.parseDate(invalidLeapYearDate)

    assertNull(millis)
  }

  @Test
  fun `formatDate handles epoch time`() {
    val epochMillis = 0L
    val formatted = BirthdayFormatter.formatDate(epochMillis)

    // This will depend on the timezone, but should be a valid date string
    assertNotNull(formatted)
    // Should follow the dd/MM/yyyy format
    assert(formatted.matches(Regex("\\d{2}/\\d{2}/\\d{4}")))
  }

  @Test
  fun `formatDate handles dates far in the past`() {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(1900, Calendar.JANUARY, 1, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    val formatted = BirthdayFormatter.formatDate(millis)
    assertEquals("01/01/1900", formatted)
  }

  @Test
  fun `formatDate handles dates far in the future`() {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2100, Calendar.DECEMBER, 31, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    val formatted = BirthdayFormatter.formatDate(millis)
    assertEquals("31/12/2100", formatted)
  }

  @Test
  fun `dateFormatter is not lenient`() {
    val formatter = BirthdayFormatter.dateFormatter
    assertEquals(false, formatter.isLenient)
  }

  @Test
  fun `parseDate with partially valid date returns null`() {
    // Test that partially valid dates are rejected due to isLenient = false
    val partiallyValidDates = listOf("32/01/2000", "01/13/2000", "00/01/2000", "01/00/2000")

    partiallyValidDates.forEach { dateString ->
      val millis = BirthdayFormatter.parseDate(dateString)
      assertNull("Should have rejected: $dateString", millis)
    }
  }

  @Test
  fun `multiple calls to dateFormatter return consistent results`() {
    val formatter1 = BirthdayFormatter.dateFormatter
    val formatter2 = BirthdayFormatter.dateFormatter

    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val result1 = formatter1.format(calendar.time)
    val result2 = formatter2.format(calendar.time)

    assertEquals(result1, result2)
  }
}
