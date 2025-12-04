package com.github.se.studentconnect.model.event

import com.google.firebase.Timestamp

/**
 * Represents comprehensive statistics for an event.
 *
 * @property eventId The unique identifier of the event.
 * @property totalAttendees The total number of participants registered for the event.
 * @property ageDistribution Breakdown of attendees by age group.
 * @property campusDistribution Breakdown of attendees by university/campus.
 * @property joinRateOverTime Temporal data showing registration patterns.
 * @property followerCount Number of followers for the organization hosting the event.
 * @property attendeesFollowersRate Ratio of attendees to organization followers (percentage).
 */
data class EventStatistics(
    val eventId: String,
    val totalAttendees: Int,
    val ageDistribution: List<AgeGroupData>,
    val campusDistribution: List<CampusData>,
    val joinRateOverTime: List<JoinRateData>,
    val followerCount: Int,
    val attendeesFollowersRate: Float
) {
  companion object {
    /** Returns empty statistics for initial/error states */
    fun empty(eventId: String) =
        EventStatistics(
            eventId = eventId,
            totalAttendees = 0,
            ageDistribution = emptyList(),
            campusDistribution = emptyList(),
            joinRateOverTime = emptyList(),
            followerCount = 0,
            attendeesFollowersRate = 0f)
  }
}

/**
 * Represents age group distribution data.
 *
 * @property ageRange The age range label (e.g., "18-22", "23-25").
 * @property count Number of attendees in this age group.
 * @property percentage Percentage of total attendees (0.0 to 100.0).
 */
data class AgeGroupData(val ageRange: String, val count: Int, val percentage: Float)

/**
 * Represents campus/university distribution data.
 *
 * @property campusName Name of the university or campus.
 * @property count Number of attendees from this campus.
 * @property percentage Percentage of total attendees (0.0 to 100.0).
 */
data class CampusData(val campusName: String, val count: Int, val percentage: Float)

/**
 * Represents join rate over time data point.
 *
 * @property timestamp The time point for this data.
 * @property cumulativeJoins Cumulative number of registrations up to this point.
 * @property label Display label for this time point (e.g., "Day 1", "Week 2").
 */
data class JoinRateData(val timestamp: Timestamp, val cumulativeJoins: Int, val label: String)

/** Age groups used for categorization */
object AgeGroups {
  const val UNDER_18 = "<18"
  const val AGE_18_22 = "18-22"
  const val AGE_23_25 = "23-25"
  const val AGE_26_30 = "26-30"
  const val AGE_30_PLUS = "30+"
  const val UNKNOWN = "Unknown"

  val all = listOf(UNDER_18, AGE_18_22, AGE_23_25, AGE_26_30, AGE_30_PLUS)

  /**
   * Calculates age from birthday string in DD/MM/YYYY format.
   *
   * @param birthdate Birthday string in DD/MM/YYYY format.
   * @return Age in years, or null if parsing fails.
   */
  fun calculateAge(birthdate: String?): Int? {
    if (birthdate.isNullOrBlank()) return null
    return try {
      val parts = birthdate.split("/")
      if (parts.size != 3) return null
      val day = parts[0].toIntOrNull() ?: return null
      val month = parts[1].toIntOrNull() ?: return null
      val year = parts[2].toIntOrNull() ?: return null

      val now = java.util.Calendar.getInstance()
      val currentYear = now.get(java.util.Calendar.YEAR)
      val currentMonth = now.get(java.util.Calendar.MONTH) + 1
      val currentDay = now.get(java.util.Calendar.DAY_OF_MONTH)

      var age = currentYear - year
      if (currentMonth < month || (currentMonth == month && currentDay < day)) {
        age--
      }
      age.takeIf { it >= 0 }
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Maps age to an age group.
   *
   * @param age Age in years.
   * @return The corresponding age group string.
   */
  fun getAgeGroup(age: Int?): String {
    return when {
      age == null -> UNKNOWN
      age < 18 -> UNDER_18
      age in 18..22 -> AGE_18_22
      age in 23..25 -> AGE_23_25
      age in 26..30 -> AGE_26_30
      else -> AGE_30_PLUS
    }
  }
}
