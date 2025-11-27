package com.github.se.studentconnect.model.story

import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.*
import org.junit.Test

class StoryTest {

  private val now = Timestamp(Date())
  private val expiresAt =
      Timestamp(now.seconds + StoryRepositoryFirestore.STORY_EXPIRATION_SECONDS, now.nanoseconds)

  @Test
  fun story_toMap_correctlyConvertsToMap() {
    val story =
        Story(
            storyId = "story123",
            userId = "user456",
            eventId = "event789",
            mediaUrl = "stories/event789/user456/1234567890",
            createdAt = now,
            expiresAt = expiresAt,
            mediaType = MediaType.IMAGE)

    val map = story.toMap()

    assertEquals("story123", map["storyId"])
    assertEquals("user456", map["userId"])
    assertEquals("event789", map["eventId"])
    assertEquals("stories/event789/user456/1234567890", map["mediaUrl"])
    assertEquals(now, map["createdAt"])
    assertEquals(expiresAt, map["expiresAt"])
    assertEquals(MediaType.IMAGE.value, map["mediaType"])
  }

  @Test
  fun story_fromMap_withValidData_createsStory() {
    val map =
        mapOf(
            "storyId" to "story123",
            "userId" to "user456",
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/1234567890",
            "createdAt" to now,
            "expiresAt" to expiresAt,
            "mediaType" to "image")

    val story = Story.fromMap(map)

    assertNotNull("Story should not be null", story)
    assertEquals("story123", story?.storyId)
    assertEquals("user456", story?.userId)
    assertEquals("event789", story?.eventId)
    assertEquals("stories/event789/user456/1234567890", story?.mediaUrl)
    assertEquals(now, story?.createdAt)
    assertEquals(expiresAt, story?.expiresAt)
    assertEquals(MediaType.IMAGE, story?.mediaType)
  }

  @Test
  fun story_fromMap_withoutMediaType_defaultsToImage() {
    val map =
        mapOf(
            "storyId" to "story123",
            "userId" to "user456",
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/1234567890",
            "createdAt" to now,
            "expiresAt" to expiresAt)
    // Missing mediaType

    val story = Story.fromMap(map)

    assertNotNull("Story should not be null", story)
    assertEquals(MediaType.IMAGE, story?.mediaType) // Should default to "image"
  }

  @Test
  fun story_fromMap_withVideoType_createsVideoStory() {
    val map =
        mapOf(
            "storyId" to "story123",
            "userId" to "user456",
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/1234567890",
            "createdAt" to now,
            "expiresAt" to expiresAt,
            "mediaType" to "video")

    val story = Story.fromMap(map)

    assertNotNull("Story should not be null", story)
    assertEquals(MediaType.VIDEO, story?.mediaType)
  }

  @Test
  fun story_fromMap_withMissingField_returnsNull() {
    val map =
        mapOf(
            "storyId" to "story123",
            "userId" to "user456",
            // Missing eventId
            "mediaUrl" to "stories/event789/user456/1234567890",
            "createdAt" to now,
            "expiresAt" to expiresAt)

    val story = Story.fromMap(map)

    assertNull("Story should be null when required field is missing", story)
  }

  @Test
  fun story_fromMap_withNullValues_returnsNull() {
    val map =
        mapOf(
            "storyId" to null,
            "userId" to "user456",
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/1234567890",
            "createdAt" to now,
            "expiresAt" to expiresAt)

    val story = Story.fromMap(map)

    assertNull("Story should be null when storyId is null", story)
  }

  @Test
  fun story_fromMap_withWrongType_returnsNull() {
    val map =
        mapOf(
            "storyId" to "story123",
            "userId" to "user456",
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/1234567890",
            "createdAt" to "not-a-timestamp", // Wrong type
            "expiresAt" to expiresAt)

    val story = Story.fromMap(map)

    assertNull("Story should be null when createdAt has wrong type", story)
  }

  @Test
  fun story_equality_worksCorrectly() {
    val story1 =
        Story(
            storyId = "story123",
            userId = "user456",
            eventId = "event789",
            mediaUrl = "stories/event789/user456/1234567890",
            createdAt = now,
            expiresAt = expiresAt,
            mediaType = MediaType.IMAGE)
    val story2 =
        Story(
            storyId = "story123",
            userId = "user456",
            eventId = "event789",
            mediaUrl = "stories/event789/user456/1234567890",
            createdAt = now,
            expiresAt = expiresAt,
            mediaType = MediaType.IMAGE)
    val story3 =
        Story(
            storyId = "story999",
            userId = "user456",
            eventId = "event789",
            mediaUrl = "stories/event789/user456/1234567890",
            createdAt = now,
            expiresAt = expiresAt,
            mediaType = MediaType.IMAGE)

    assertEquals(story1, story2)
    assertNotEquals(story1, story3)
  }

  @Test
  fun story_equality_withDifferentMediaTypes_areNotEqual() {
    val story1 =
        Story(
            storyId = "story123",
            userId = "user456",
            eventId = "event789",
            mediaUrl = "stories/event789/user456/1234567890",
            createdAt = now,
            expiresAt = expiresAt,
            mediaType = MediaType.IMAGE)
    val story2 =
        Story(
            storyId = "story123",
            userId = "user456",
            eventId = "event789",
            mediaUrl = "stories/event789/user456/1234567890",
            createdAt = now,
            expiresAt = expiresAt,
            mediaType = MediaType.VIDEO)

    assertNotEquals(story1, story2)
  }

  @Test
  fun story_fromMap_withMediaTypeAsNonString_usesDefault() {
    val map =
        mapOf(
            "storyId" to "story123",
            "userId" to "user456",
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/1234567890",
            "createdAt" to now,
            "expiresAt" to expiresAt,
            "mediaType" to 123) // Wrong type (Int instead of String)

    val story = Story.fromMap(map)

    assertNotNull("Story should not be null", story)
    assertEquals(MediaType.IMAGE, story?.mediaType) // Should default to "image"
  }

  @Test
  fun story_fromMap_withNullExpiresAt_returnsNull() {
    val map =
        mapOf(
            "storyId" to "story123",
            "userId" to "user456",
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/1234567890",
            "createdAt" to now,
            "expiresAt" to null) // Null expiresAt

    val story = Story.fromMap(map)

    assertNull("Story should be null when expiresAt is null", story)
  }

  @Test
  fun story_fromMap_withNullCreatedAt_returnsNull() {
    val map =
        mapOf(
            "storyId" to "story123",
            "userId" to "user456",
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/1234567890",
            "createdAt" to null, // Null createdAt
            "expiresAt" to expiresAt)

    val story = Story.fromMap(map)

    assertNull("Story should be null when createdAt is null", story)
  }
}
