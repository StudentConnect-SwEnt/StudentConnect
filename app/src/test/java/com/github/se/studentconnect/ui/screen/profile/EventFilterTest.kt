package com.github.se.studentconnect.ui.screen.profile

import org.junit.Test

class EventFilterTest {

  @Test
  fun `EventFilter has Past value`() {
    assert(EventFilter.Past == EventFilter.valueOf("Past"))
  }

  @Test
  fun `EventFilter has Upcoming value`() {
    assert(EventFilter.Upcoming == EventFilter.valueOf("Upcoming"))
  }

  @Test
  fun `EventFilter values returns all filters`() {
    val values = EventFilter.values()
    assert(values.size == 2)
    assert(values.contains(EventFilter.Past))
    assert(values.contains(EventFilter.Upcoming))
  }
}
