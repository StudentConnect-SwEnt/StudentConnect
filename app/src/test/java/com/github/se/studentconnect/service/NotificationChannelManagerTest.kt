package com.github.se.studentconnect.service

import org.junit.Test

class NotificationChannelManagerTest {

  @Test
  fun channelIds_areNotEmpty() {
    assert(NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID.isNotEmpty()) {
      "Friend request channel ID should not be empty"
    }
    assert(NotificationChannelManager.EVENT_STARTING_CHANNEL_ID.isNotEmpty()) {
      "Event starting channel ID should not be empty"
    }
  }

  @Test
  fun channelIds_areDifferent() {
    assert(
        NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID !=
            NotificationChannelManager.EVENT_STARTING_CHANNEL_ID) {
          "Channel IDs should be different"
        }
  }

  @Test
  fun channelIds_haveCorrectValues() {
    assert(NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID == "friend_requests") {
      "Friend request channel ID should be 'friend_requests'"
    }
    assert(NotificationChannelManager.EVENT_STARTING_CHANNEL_ID == "event_starting") {
      "Event starting channel ID should be 'event_starting'"
    }
  }

  @Test
  fun channelIds_followNamingConvention() {
    // Channel IDs should be lowercase with underscores
    val friendRequestId = NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID
    val eventStartingId = NotificationChannelManager.EVENT_STARTING_CHANNEL_ID

    assert(friendRequestId == friendRequestId.lowercase()) {
      "Friend request channel ID should be lowercase"
    }
    assert(eventStartingId == eventStartingId.lowercase()) {
      "Event starting channel ID should be lowercase"
    }
    assert(!friendRequestId.contains(" ")) { "Friend request channel ID should not contain spaces" }
    assert(!eventStartingId.contains(" ")) { "Event starting channel ID should not contain spaces" }
  }
}
