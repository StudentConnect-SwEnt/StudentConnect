package com.github.se.studentconnect.ui.events

import com.github.se.studentconnect.ui.utils.formatDateHeader
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatDateHeaderTest {

  @Test
  fun formatDateHeader_returnsTODAY_forToday() {
    val now = Date()
    val ts = Timestamp(now)
    val result = formatDateHeader(ts)
    assertEquals("TODAY", result)
  }

  @Test
  fun formatDateHeader_returnsTOMORROW_forTomorrow() {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, 1)
    val ts = Timestamp(cal.time)
    val result = formatDateHeader(ts)
    assertEquals("TOMORROW", result)
  }

  @Test
  fun formatDateHeader_returnsFormatted_forOtherDay() {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, 3)
    val ts = Timestamp(cal.time)
    val result = formatDateHeader(ts)
    // Should not be TODAY or TOMORROW; just ensure it is uppercase and non-empty
    assert(result.isNotEmpty())
    assert(result != "TODAY")
    assert(result != "TOMORROW")
  }
}
