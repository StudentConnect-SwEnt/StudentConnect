package com.github.se.studentconnect.model.event

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventStatisticsTest {

  @Test
  fun `data classes store properties correctly and empty returns defaults`() {
    // Test EventStatistics.empty()
    val emptyStats = EventStatistics.empty("event123")
    assertEquals("event123", emptyStats.eventId)
    assertEquals(0, emptyStats.totalAttendees)
    assertTrue(emptyStats.ageDistribution.isEmpty())
    assertTrue(emptyStats.campusDistribution.isEmpty())
    assertTrue(emptyStats.joinRateOverTime.isEmpty())
    assertEquals(0, emptyStats.followerCount)
    assertEquals(0f, emptyStats.attendeesFollowersRate)

    // Test EventStatistics with data
    val stats =
        EventStatistics(
            eventId = "event456",
            totalAttendees = 100,
            ageDistribution = listOf(AgeGroupData("18-22", 50, 50f)),
            campusDistribution = listOf(CampusData("EPFL", 60, 60f)),
            joinRateOverTime = listOf(JoinRateData(Timestamp.now(), 50, "Day 1")),
            followerCount = 500,
            attendeesFollowersRate = 20f)
    assertEquals(100, stats.totalAttendees)
    assertEquals(1, stats.ageDistribution.size)
    assertEquals(1, stats.campusDistribution.size)
    assertEquals(1, stats.joinRateOverTime.size)

    // Test AgeGroupData
    val ageData = AgeGroupData("18-22", 25, 50f)
    assertEquals("18-22", ageData.ageRange)
    assertEquals(25, ageData.count)
    assertEquals(50f, ageData.percentage)

    // Test CampusData
    val campusData = CampusData("EPFL", 100, 75.5f)
    assertEquals("EPFL", campusData.campusName)
    assertEquals(100, campusData.count)

    // Test JoinRateData
    val timestamp = Timestamp.now()
    val joinData = JoinRateData(timestamp, 42, "Nov 15")
    assertEquals(timestamp, joinData.timestamp)
    assertEquals(42, joinData.cumulativeJoins)
    assertEquals("Nov 15", joinData.label)
  }

  @Test
  fun `AgeGroups constants and all list are correct`() {
    assertEquals("<18", AgeGroups.UNDER_18)
    assertEquals("18-22", AgeGroups.AGE_18_22)
    assertEquals("23-25", AgeGroups.AGE_23_25)
    assertEquals("26-30", AgeGroups.AGE_26_30)
    assertEquals("30+", AgeGroups.AGE_30_PLUS)
    assertEquals("Unknown", AgeGroups.UNKNOWN)

    val allGroups = AgeGroups.all
    assertEquals(5, allGroups.size)
    assertTrue(
        allGroups.containsAll(
            listOf(
                AgeGroups.UNDER_18,
                AgeGroups.AGE_18_22,
                AgeGroups.AGE_23_25,
                AgeGroups.AGE_26_30,
                AgeGroups.AGE_30_PLUS)))
  }

  @Test
  fun `AgeGroups getAgeGroup maps all age ranges correctly`() {
    // Null → Unknown
    assertEquals(AgeGroups.UNKNOWN, AgeGroups.getAgeGroup(null))

    // Under 18
    assertEquals(AgeGroups.UNDER_18, AgeGroups.getAgeGroup(0))
    assertEquals(AgeGroups.UNDER_18, AgeGroups.getAgeGroup(17))

    // 18-22
    assertEquals(AgeGroups.AGE_18_22, AgeGroups.getAgeGroup(18))
    assertEquals(AgeGroups.AGE_18_22, AgeGroups.getAgeGroup(22))

    // 23-25
    assertEquals(AgeGroups.AGE_23_25, AgeGroups.getAgeGroup(23))
    assertEquals(AgeGroups.AGE_23_25, AgeGroups.getAgeGroup(25))

    // 26-30
    assertEquals(AgeGroups.AGE_26_30, AgeGroups.getAgeGroup(26))
    assertEquals(AgeGroups.AGE_26_30, AgeGroups.getAgeGroup(30))

    // 30+
    assertEquals(AgeGroups.AGE_30_PLUS, AgeGroups.getAgeGroup(31))
    assertEquals(AgeGroups.AGE_30_PLUS, AgeGroups.getAgeGroup(100))
  }

  @Test
  fun `AgeGroups calculateAge handles all input cases`() {
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    // Valid date - returns correct age
    val birthYear = currentYear - 25
    val age = AgeGroups.calculateAge("15/06/$birthYear")
    assertNotNull(age)
    assertTrue(age == 24 || age == 25)

    // Birthday not yet occurred (Dec 31)
    val ageDecember = AgeGroups.calculateAge("31/12/${currentYear - 20}")
    assertNotNull(ageDecember)
    assertTrue(ageDecember == 19 || ageDecember == 20)

    // Invalid inputs - all return null
    assertNull(AgeGroups.calculateAge(null))
    assertNull(AgeGroups.calculateAge(""))
    assertNull(AgeGroups.calculateAge("   "))
    assertNull(AgeGroups.calculateAge("invalid"))
    assertNull(AgeGroups.calculateAge("15-06-2000")) // Wrong separator
    assertNull(AgeGroups.calculateAge("15/06")) // Partial
    assertNull(AgeGroups.calculateAge("15")) // Single value
    assertNull(AgeGroups.calculateAge("15/ab/2000")) // Non-numeric month
    assertNull(AgeGroups.calculateAge("ab/06/2000")) // Non-numeric day
    assertNull(AgeGroups.calculateAge("15/06/abcd")) // Non-numeric year

    // Future birthdate - returns null (negative age)
    assertNull(AgeGroups.calculateAge("01/01/${currentYear + 5}"))
  }
}
