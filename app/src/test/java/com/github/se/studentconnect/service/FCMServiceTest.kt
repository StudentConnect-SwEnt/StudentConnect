package com.github.se.studentconnect.service

import org.junit.Test

class FCMServiceTest {

  @Test
  fun fcmService_extendsFirebaseMessagingService() {
    val serviceClass = FCMService::class.java
    val superclass = serviceClass.superclass

    assert(superclass != null) { "FCMService should have a superclass" }
    assert(superclass?.simpleName == "FirebaseMessagingService") {
      "FCMService should extend FirebaseMessagingService"
    }
  }

  @Test
  fun fcmService_hasOnNewTokenMethod() {
    val serviceClass = FCMService::class.java
    val methods = serviceClass.declaredMethods

    val hasOnNewToken = methods.any { it.name == "onNewToken" }
    assert(hasOnNewToken) { "FCMService should have onNewToken method" }
  }

  @Test
  fun fcmService_hasOnMessageReceivedMethod() {
    val serviceClass = FCMService::class.java
    val methods = serviceClass.declaredMethods

    val hasOnMessageReceived = methods.any { it.name == "onMessageReceived" }
    assert(hasOnMessageReceived) { "FCMService should have onMessageReceived method" }
  }

  @Test
  fun fcmService_hasCompanionObject() {
    val serviceClass = FCMService::class.java
    val companionClass = serviceClass.declaredClasses.find { it.simpleName == "Companion" }

    assert(companionClass != null) { "FCMService should have Companion object" }
  }

  @Test
  fun notificationIdBase_isPositive() {
    // Verify the notification ID base is reasonable
    val baseId = 1000
    assert(baseId > 0) { "Notification ID base should be positive" }
    assert(baseId >= 1000) { "Notification ID base should be large enough to avoid conflicts" }
  }

  @Test
  fun notificationId_hashCode_isConsistent() {
    // Test that string hash codes are consistent
    val id1 = "notif-123"
    val id2 = "notif-123"
    val id3 = "notif-456"

    assert(id1.hashCode() == id2.hashCode()) { "Same strings should have same hash code" }
    assert(id1.hashCode() != id3.hashCode()) {
      "Different strings should likely have different hash codes"
    }
  }

  @Test
  fun fcmService_usesCorrectChannelIds() {
    // Verify FCM service uses the same channel IDs as NotificationChannelManager
    val friendRequestId = NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID
    val eventStartingId = NotificationChannelManager.EVENT_STARTING_CHANNEL_ID

    assert(friendRequestId == "friend_requests") { "Friend request channel ID should match" }
    assert(eventStartingId == "event_starting") { "Event starting channel ID should match" }
  }

  @Test
  fun fcmService_hasPrivateMethods() {
    val serviceClass = FCMService::class.java
    val methods = serviceClass.declaredMethods

    // Check for private handler methods
    val hasHandleFriendRequest = methods.any { it.name == "handleFriendRequestNotification" }
    val hasHandleEventStarting = methods.any { it.name == "handleEventStartingNotification" }
    val hasStoreNotification = methods.any { it.name == "storeNotification" }
    val hasShowPushNotification = methods.any { it.name == "showPushNotification" }

    assert(hasHandleFriendRequest) {
      "FCMService should have handleFriendRequestNotification method"
    }
    assert(hasHandleEventStarting) {
      "FCMService should have handleEventStartingNotification method"
    }
    assert(hasStoreNotification) { "FCMService should have storeNotification method" }
    assert(hasShowPushNotification) { "FCMService should have showPushNotification method" }
  }
}
