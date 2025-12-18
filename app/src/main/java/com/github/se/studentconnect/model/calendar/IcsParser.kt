package com.github.se.studentconnect.model.calendar

import com.google.firebase.Timestamp
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Parser for ICS (iCalendar) files. Supports basic VEVENT parsing for importing personal calendar
 * events.
 */
object IcsParser {

  private const val BEGIN_VCALENDAR = "BEGIN:VCALENDAR"
  private const val END_VCALENDAR = "END:VCALENDAR"
  private const val BEGIN_VEVENT = "BEGIN:VEVENT"
  private const val END_VEVENT = "END:VEVENT"

  private val dateTimeFormat =
      SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
      }

  private val dateTimeFormatZ =
      SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
      }

  private val dateFormat =
      SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

  /**
   * Parses an ICS file from an InputStream.
   *
   * @param inputStream The input stream of the ICS file
   * @param userId The user ID to associate with the imported events
   * @param sourceCalendar The name of the source calendar
   * @param generateUid Function to generate unique IDs for events
   * @return List of parsed PersonalCalendarEvent objects
   */
  fun parseIcs(
      inputStream: InputStream,
      userId: String,
      sourceCalendar: String,
      generateUid: () -> String = { UUID.randomUUID().toString() }
  ): List<PersonalCalendarEvent> {
    val events = mutableListOf<PersonalCalendarEvent>()
    val reader = BufferedReader(InputStreamReader(inputStream))

    var inCalendar = false
    var inEvent = false
    var currentEvent = mutableMapOf<String, String>()
    var currentKey = ""
    var currentValue = StringBuilder()

    reader.useLines { lines ->
      lines.forEach { rawLine ->
        val line = rawLine.trimEnd().removePrefix("\uFEFF")

        when {
          line == BEGIN_VCALENDAR -> inCalendar = true
          line == END_VCALENDAR -> inCalendar = false
          line == BEGIN_VEVENT && inCalendar -> {
            inEvent = true
            currentEvent = mutableMapOf()
          }
          line == END_VEVENT && inEvent -> {
            // Save any pending multi-line value
            if (currentKey.isNotEmpty()) {
              currentEvent[currentKey] = currentValue.toString()
            }

            // Create the event
            parseEvent(currentEvent, userId, sourceCalendar, generateUid)?.let { events.add(it) }

            inEvent = false
            currentKey = ""
            currentValue = StringBuilder()
          }
          inEvent -> {
            // Handle line folding (continuation lines start with space or tab)
            if (line.startsWith(" ") || line.startsWith("\t")) {
              currentValue.append(line.substring(1))
            } else {
              // Save previous key-value pair
              if (currentKey.isNotEmpty()) {
                currentEvent[currentKey] = currentValue.toString()
              }

              // Parse new key-value pair
              val colonIndex = line.indexOf(':')
              if (colonIndex > 0) {
                currentKey = line.substring(0, colonIndex).uppercase()
                currentValue = StringBuilder(line.substring(colonIndex + 1))
              }
            }
          }
        }
      }
    }

    return events
  }

  private fun parseEvent(
      eventData: Map<String, String>,
      userId: String,
      sourceCalendar: String,
      generateUid: () -> String
  ): PersonalCalendarEvent? {
    val title = eventData["SUMMARY"] ?: return null

    val startValue = eventData.entries.find { it.key.startsWith("DTSTART") }?.value ?: return null

    val endValue = eventData.entries.find { it.key.startsWith("DTEND") }?.value

    val isAllDay = eventData.keys.any { it.contains("VALUE=DATE") && !it.contains("DATE-TIME") }

    val start = parseDateTime(startValue, isAllDay) ?: return null
    val end = endValue?.let { parseDateTime(it, isAllDay) }

    val externalUid = eventData["UID"]
    val internalUid =
        if (externalUid != null) {
          UUID.nameUUIDFromBytes((userId + externalUid).toByteArray()).toString()
        } else {
          generateUid()
        }

    return PersonalCalendarEvent(
        uid = internalUid,
        userId = userId,
        title = unescapeIcsText(title),
        description = eventData["DESCRIPTION"]?.let { unescapeIcsText(it) },
        location = eventData["LOCATION"]?.let { unescapeIcsText(it) },
        start = start,
        end = end,
        isAllDay = isAllDay,
        sourceCalendar = sourceCalendar,
        externalUid = externalUid)
  }

  private fun parseDateTime(value: String, isAllDay: Boolean): Timestamp? {
    return try {
      val trimmedValue = value.trim()
      val date =
          when {
            isAllDay || trimmedValue.length == 8 -> dateFormat.parse(trimmedValue)
            trimmedValue.endsWith("Z") -> dateTimeFormatZ.parse(trimmedValue)
            else -> dateTimeFormat.parse(trimmedValue)
          }
      date?.let { Timestamp(it.time / 1000, 0) }
    } catch (e: Exception) {
      android.util.Log.e("IcsParser", "Error parsing date: $value", e)
      null
    }
  }

  private fun unescapeIcsText(text: String): String {
    return text.replace("\\n", "\n").replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\")
  }
}
