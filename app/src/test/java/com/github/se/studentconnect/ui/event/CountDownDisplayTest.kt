package com.github.se.studentconnect.ui.event

import org.junit.Assert
import org.junit.Test

class CountDownDisplayTest {

  @Test
  fun `days function correctly converts seconds to days`() {
    Assert.assertEquals("1", days(86400))
    Assert.assertEquals("0", days(86399))
    Assert.assertEquals("2", days(172800))
  }

  @Test
  fun `hours function correctly converts seconds to hours`() {
    Assert.assertEquals("1", hours(3600))
    Assert.assertEquals("23", hours(86399))
    Assert.assertEquals("0", hours(3599))
  }

  @Test
  fun `mins function correctly converts seconds to minutes`() {
    Assert.assertEquals("1", mins(60))
    Assert.assertEquals("59", mins(3599))
    Assert.assertEquals("0", mins(59))
  }

  @Test
  fun `secs function correctly converts seconds to seconds`() {
    Assert.assertEquals("1", secs(1))
    Assert.assertEquals("59", secs(59))
    Assert.assertEquals("0", secs(60))
  }
}
