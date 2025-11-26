package com.github.se.studentconnect.ui.screen.home

import junit.framework.TestCase.assertEquals
import org.junit.Test

class HomeTabModeTest {

  @Test
  fun homeTabMode_hasThreeValues() {
    // Act
    val values = HomeTabMode.entries

    // Assert
    assertEquals(3, values.size)
  }

  @Test
  fun homeTabMode_containsForYou() {
    // Act
    val values = HomeTabMode.entries

    // Assert
    assertEquals(true, values.contains(HomeTabMode.FOR_YOU))
  }

  @Test
  fun homeTabMode_containsEvents() {
    // Act
    val values = HomeTabMode.entries

    // Assert
    assertEquals(true, values.contains(HomeTabMode.EVENTS))
  }

  @Test
  fun homeTabMode_containsDiscover() {
    // Act
    val values = HomeTabMode.entries

    // Assert
    assertEquals(true, values.contains(HomeTabMode.DISCOVER))
  }

  @Test
  fun homeTabMode_forYouHasOrdinalZero() {
    // Act & Assert
    assertEquals(0, HomeTabMode.FOR_YOU.ordinal)
  }

  @Test
  fun homeTabMode_eventsHasOrdinalOne() {
    // Act & Assert
    assertEquals(1, HomeTabMode.EVENTS.ordinal)
  }

  @Test
  fun homeTabMode_discoverHasOrdinalTwo() {
    // Act & Assert
    assertEquals(2, HomeTabMode.DISCOVER.ordinal)
  }

  @Test
  fun homeTabMode_canGetByOrdinal() {
    // Act & Assert
    assertEquals(HomeTabMode.FOR_YOU, HomeTabMode.entries[0])
    assertEquals(HomeTabMode.EVENTS, HomeTabMode.entries[1])
    assertEquals(HomeTabMode.DISCOVER, HomeTabMode.entries[2])
  }

  @Test
  fun homeTabMode_forYouToString() {
    // Act & Assert
    assertEquals("FOR_YOU", HomeTabMode.FOR_YOU.toString())
  }

  @Test
  fun homeTabMode_eventsToString() {
    // Act & Assert
    assertEquals("EVENTS", HomeTabMode.EVENTS.toString())
  }

  @Test
  fun homeTabMode_discoverToString() {
    // Act & Assert
    assertEquals("DISCOVER", HomeTabMode.DISCOVER.toString())
  }

  @Test
  fun homeTabMode_valueOf_forYou() {
    // Act & Assert
    assertEquals(HomeTabMode.FOR_YOU, HomeTabMode.valueOf("FOR_YOU"))
  }

  @Test
  fun homeTabMode_valueOf_events() {
    // Act & Assert
    assertEquals(HomeTabMode.EVENTS, HomeTabMode.valueOf("EVENTS"))
  }

  @Test
  fun homeTabMode_valueOf_discover() {
    // Act & Assert
    assertEquals(HomeTabMode.DISCOVER, HomeTabMode.valueOf("DISCOVER"))
  }

  @Test
  fun homeTabMode_entriesContainsAllValues() {
    // Act
    val entries = HomeTabMode.entries

    // Assert - verify all expected values are present
    assertEquals(true, entries.contains(HomeTabMode.FOR_YOU))
    assertEquals(true, entries.contains(HomeTabMode.EVENTS))
    assertEquals(true, entries.contains(HomeTabMode.DISCOVER))
    assertEquals(3, entries.size)
  }

  @Test
  fun homeTabMode_ordinalSequence() {
    // Act
    val entries = HomeTabMode.entries

    // Assert - verify ordinals are sequential starting from 0
    assertEquals(0, entries[0].ordinal)
    assertEquals(1, entries[1].ordinal)
    assertEquals(2, entries[2].ordinal)
  }
}
