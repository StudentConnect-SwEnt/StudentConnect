// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Timestamp
import java.util.*
import org.junit.Assert.*
import org.junit.Test

class EventTest {

  private val now = Timestamp(Date())
  private val later = Timestamp(Date(System.currentTimeMillis() + 3600_000))

  @Test
  fun privateEvent_fieldsAreSetCorrectly() {
    val event =
        Event.Private(
            uid = "private1",
            ownerId = "owner1",
            title = "Private Title",
            description = "Private Description",
            imageUrl = "https://example.com/image.png",
            location = Location(10.0, 20.0, "Private Place"),
            start = now,
            end = later,
            maxCapacity = 50u,
            participationFee = 10u,
            isFlash = true)

    assertEquals("private1", event.uid)
    assertEquals("owner1", event.ownerId)
    assertEquals("Private Title", event.title)
    assertEquals("Private Description", event.description)
    assertEquals("https://example.com/image.png", event.imageUrl)
    assertEquals(Location(10.0, 20.0, "Private Place"), event.location)
    assertEquals(now, event.start)
    assertEquals(later, event.end)
    assertEquals(50u, event.maxCapacity!!)
    assertEquals(10u, event.participationFee!!)
    assertTrue(event.isFlash)
  }

  @Test
  fun privateEvent_toMapContainsAllFieldsAndTypePrivate() {
    val event =
        Event.Private(
            uid = "private2",
            ownerId = "owner2",
            title = "Private Title 2",
            description = "Private Desc",
            imageUrl = null,
            location = null,
            start = now,
            end = null,
            maxCapacity = null,
            participationFee = null,
            isFlash = false)

    val map = event.toMap()
    assertEquals("private2", map["uid"])
    assertEquals("owner2", map["ownerId"])
    assertEquals("Private Title 2", map["title"])
    assertEquals("Private Desc", map["description"])
    assertNull(map["imageUrl"])
    assertNull(map["location"])
    assertEquals(now, map["start"])
    assertNull(map["end"])
    assertNull(map["maxCapacity"])
    assertNull(map["participationFee"])
    assertEquals(false, map["isFlash"])
    assertEquals("private", map["type"])
  }

  @Test
  fun publicEvent_fieldsAreSetCorrectly() {
    val event =
        Event.Public(
            uid = "public1",
            ownerId = "owner3",
            title = "Public Title",
            description = "Public Description",
            imageUrl = "https://example.com/public.png",
            location = Location(30.0, 40.0, "Public Place"),
            start = now,
            end = later,
            maxCapacity = 100u,
            participationFee = 20u,
            isFlash = false,
            tags = listOf("tag1", "tag2"),
            website = "https://event.com")

    assertEquals("public1", event.uid)
    assertEquals("owner3", event.ownerId)
    assertEquals("Public Title", event.title)
    assertEquals("Public Description", event.description)
    assertEquals("https://example.com/public.png", event.imageUrl)
    assertEquals(Location(30.0, 40.0, "Public Place"), event.location)
    assertEquals(now, event.start)
    assertEquals(later, event.end)
    assertEquals(100u, event.maxCapacity!!)
    assertEquals(20u, event.participationFee!!)
    assertFalse(event.isFlash)
    assertEquals(listOf("tag1", "tag2"), event.tags)
    assertEquals("https://event.com", event.website)
  }

  @Test
  fun publicEvent_toMapContainsAllFieldsAndTypePublic() {
    val event =
        Event.Public(
            uid = "public2",
            ownerId = "owner4",
            title = "Public Title 2",
            description = "Public Desc",
            imageUrl = null,
            location = null,
            start = now,
            end = null,
            maxCapacity = null,
            participationFee = null,
            isFlash = true,
            tags = listOf("tagA", "tagB"),
            website = null)

    val map = event.toMap()
    assertEquals("public2", map["uid"])
    assertEquals("owner4", map["ownerId"])
    assertEquals("Public Title 2", map["title"])
    assertEquals("Public Desc", map["description"])
    assertNull(map["imageUrl"])
    assertNull(map["location"])
    assertEquals(now, map["start"])
    assertNull(map["end"])
    assertNull(map["maxCapacity"])
    assertNull(map["participationFee"])
    assertEquals(true, map["isFlash"])
    assertEquals("public", map["type"])
    assertEquals(listOf("tagA", "tagB"), map["tags"])
    assertNull(map["website"])
  }
}
