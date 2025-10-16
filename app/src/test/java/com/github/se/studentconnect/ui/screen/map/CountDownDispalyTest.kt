import com.github.se.studentconnect.ui.screen.activities.days
import com.github.se.studentconnect.ui.screen.activities.hours
import com.github.se.studentconnect.ui.screen.activities.mins
import com.github.se.studentconnect.ui.screen.activities.secs
import org.junit.Assert.assertEquals
import org.junit.Test

class CountDownDisplayTest {

  @Test
  fun `days function correctly converts seconds to days`() {
    assertEquals("1", days(86400))
    assertEquals("0", days(86399))
    assertEquals("2", days(172800))
  }

  @Test
  fun `hours function correctly converts seconds to hours`() {
    assertEquals("1", hours(3600))
    assertEquals("23", hours(86399))
    assertEquals("0", hours(3599))
  }

  @Test
  fun `mins function correctly converts seconds to minutes`() {
    assertEquals("1", mins(60))
    assertEquals("59", mins(3599))
    assertEquals("0", mins(59))
  }

  @Test
  fun `secs function correctly converts seconds to seconds`() {
    assertEquals("1", secs(1))
    assertEquals("59", secs(59))
    assertEquals("0", secs(60))
  }
}
