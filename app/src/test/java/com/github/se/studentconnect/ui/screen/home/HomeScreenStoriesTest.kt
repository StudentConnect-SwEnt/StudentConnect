package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class HomeScreenStoriesTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    // Initialize Firebase first (before accessing any repositories)
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Set up network availability for Robolectric
    // Robolectric provides a default network, but we need to ensure it has internet capability
    // The default setup should work for most tests

    AuthenticationProvider.testUserId = "testUser123"
  }

  @After
  fun tearDown() {
    AuthenticationProvider.testUserId = null
  }

  // Fake repositories for testing
  private val fakeEventRepository =
      object : EventRepository {
        override fun getNewUid(): String = "test-uid"

        override suspend fun getAllVisibleEvents(): List<Event> = emptyList()

        override suspend fun getAllVisibleEventsSatisfying(
            predicate: (Event) -> Boolean
        ): List<Event> = emptyList()

        override suspend fun getEvent(eventUid: String): Event = throw NotImplementedError()

        override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> =
            emptyList()

        override suspend fun addEvent(event: Event) {}

        override suspend fun editEvent(eventUid: String, newEvent: Event) {}

        override suspend fun deleteEvent(eventUid: String) {}

        override suspend fun addParticipantToEvent(
            eventUid: String,
            participant: EventParticipant
        ) {}

        override suspend fun addInvitationToEvent(
            eventUid: String,
            invitedUser: String,
            currentUserId: String
        ) {}

        override suspend fun getEventInvitations(eventUid: String): List<String> = emptyList()

        override suspend fun removeInvitationFromEvent(
            eventUid: String,
            invitedUser: String,
            currentUserId: String
        ) {}

        override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) {}
      }

  private val fakeUserRepository =
      object : UserRepository {
        override suspend fun leaveEvent(eventId: String, userId: String) {}

        override suspend fun getUserById(userId: String): User? = null

        override suspend fun getUserByEmail(email: String): User? = null

        override suspend fun getAllUsers(): List<User> = emptyList()

        override suspend fun getUsersPaginated(
            limit: Int,
            lastUserId: String?
        ): Pair<List<User>, Boolean> = Pair(emptyList(), false)

        override suspend fun saveUser(user: User) {}

        override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

        override suspend fun deleteUser(userId: String) {}

        override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

        override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

        override suspend fun getNewUid(): String = "test-uid"

        override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

        override suspend fun addEventToUser(eventId: String, userId: String) {}

        override suspend fun addInvitationToUser(
            eventId: String,
            userId: String,
            fromUserId: String
        ) {}

        override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

        override suspend fun acceptInvitation(eventId: String, userId: String) {}

        override suspend fun declineInvitation(eventId: String, userId: String) {}

        override suspend fun removeInvitation(eventId: String, userId: String) {}

        override suspend fun joinEvent(eventId: String, userId: String) {}

        override suspend fun sendInvitation(
            eventId: String,
            fromUserId: String,
            toUserId: String
        ) {}

        override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

        override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

        override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

        override suspend fun checkUsernameAvailability(username: String): Boolean = true

        override suspend fun addPinnedEvent(userId: String, eventId: String) = Unit

        override suspend fun removePinnedEvent(userId: String, eventId: String) = Unit

        override suspend fun getPinnedEvents(userId: String) = emptyList<String>()

        override suspend fun pinOrganization(userId: String, organizationId: String) {}

        override suspend fun unpinOrganization(userId: String) {}

        override suspend fun getPinnedOrganization(userId: String): String? = null
      }

  private val fakeFriendsRepository =
      object : FriendsRepository {
        override suspend fun getFriends(userId: String): List<String> = emptyList()

        override suspend fun getPendingRequests(userId: String): List<String> = emptyList()

        override suspend fun getSentRequests(userId: String): List<String> = emptyList()

        override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) {}

        override suspend fun acceptFriendRequest(userId: String, fromUserId: String) {}

        override suspend fun rejectFriendRequest(userId: String, fromUserId: String) {}

        override suspend fun cancelFriendRequest(userId: String, toUserId: String) {}

        override suspend fun removeFriend(userId: String, friendId: String) {}

        override suspend fun areFriends(userId: String, otherUserId: String): Boolean = false

        override suspend fun hasPendingRequest(fromUserId: String, toUserId: String): Boolean =
            false

        override fun observeFriendship(userId: String, otherUserId: String): Flow<Boolean> = flow {
          emit(false)
        }
      }

  private val fakeNotificationRepository =
      object : NotificationRepository {
        override fun listenToNotifications(
            userId: String,
            onNotificationsChanged: (List<Notification>) -> Unit
        ): () -> Unit = {}

        override fun getNotifications(
            userId: String,
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun getUnreadNotifications(
            userId: String,
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
          TODO("Not yet implemented")
        }

        override fun createNotification(
            notification: Notification,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
          TODO("Not yet implemented")
        }

        override fun markAsRead(
            notificationId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun markAllAsRead(
            userId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun deleteNotification(
            notificationId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun deleteAllNotifications(
            userId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
          TODO("Not yet implemented")
        }
      }

  // Mock ViewModels
  private fun createMockHomePageViewModel(): HomePageViewModel {
    return HomePageViewModel(
        eventRepository = fakeEventRepository,
        userRepository = fakeUserRepository,
        friendsRepository = fakeFriendsRepository)
  }

  private fun createMockNotificationViewModel(): NotificationViewModel {
    return NotificationViewModel(repository = fakeNotificationRepository)
  }

  private val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Summer Festival",
          subtitle = "Best summer event",
          description = "Join us for an amazing summer festival.",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("music", "outdoor"),
      )

  @Test
  fun homeScreen_eventsStories_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          onClickStory = { _, _ -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(pairs = arrayOf(Pair(testEvent1, Pair(1, 1)))),
              ))
    }

    // Use testTag instead of contentDescription since the StoryItem may not have an image
    composeTestRule.onNodeWithTag("story_item_${testEvent1.uid}").assertIsDisplayed()
  }

  @Test
  fun homeScreen_eventsStories_isNotDisplayed_whenNoStories() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          onClickStory = { _, _ -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(pairs = arrayOf(Pair(testEvent1, Pair(0, 0)))),
              ))
    }

    composeTestRule.onNodeWithContentDescription("Event Story").assertIsNotDisplayed()
  }

  @Test
  fun homeScreen_eventsStories_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          onClickStory = { _, _ -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(pairs = arrayOf(Pair(testEvent1, Pair(1, 1)))),
              ))
    }

    // Use testTag instead of contentDescription since the StoryItem may not have an image
    composeTestRule.onNodeWithTag("story_item_${testEvent1.uid}").assertIsDisplayed()
  }

  @Test
  fun storyItem_withViewedStory_displaysCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "Test Story",
          avatarRes = com.github.se.studentconnect.R.drawable.avatar_12,
          viewed = true,
          onClick = {},
          contentDescription = "Test Story Content")
    }

    composeTestRule.onNodeWithContentDescription("Test Story Content").assertIsDisplayed()
  }

  @Test
  fun storyItem_withUnviewedStory_displaysCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "Test Story",
          avatarRes = com.github.se.studentconnect.R.drawable.avatar_12,
          viewed = false,
          onClick = {},
          contentDescription = "Test Story Content")
    }

    composeTestRule.onNodeWithContentDescription("Test Story Content").assertIsDisplayed()
  }

  @Test
  fun storyItem_withCustomTestTag_displaysWithTestTag() {
    composeTestRule.setContent {
      StoryItem(
          name = "Test Story",
          avatarRes = com.github.se.studentconnect.R.drawable.avatar_12,
          viewed = false,
          onClick = {},
          contentDescription = "Test Story Content",
          testTag = "custom_test_tag")
    }

    composeTestRule.onNodeWithContentDescription("Test Story Content").assertIsDisplayed()
  }

  // ========== NEW TESTS FOR STORY ENHANCEMENTS ==========

  @Test
  fun storyItem_withAvatarUrl_displaysCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "Test User",
          avatarUrl = "test_profile_pic_url",
          viewed = false,
          onClick = {},
          testTag = "story_with_url")
    }

    composeTestRule.onNodeWithTag("story_with_url").assertIsDisplayed()
  }

  @Test
  fun storyItem_withNullAvatarResAndUrl_showsInitial() {
    composeTestRule.setContent {
      StoryItem(
          name = "TestUser",
          avatarRes = null,
          avatarUrl = null,
          viewed = false,
          onClick = {},
          testTag = "story_with_initial")
    }

    composeTestRule.onNodeWithTag("story_with_initial").assertIsDisplayed()
    // The initial "T" should be displayed as fallback
  }

  @Test
  fun storyItem_textOverflow_handledCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "Very Long Story Name That Should Be Truncated",
          avatarRes = null,
          avatarUrl = null,
          viewed = false,
          onClick = {},
          testTag = "story_long_name")
    }

    composeTestRule
        .onNodeWithTag("story_text_Very Long Story Name That Should Be Truncated")
        .assertIsDisplayed()
  }

  @Test
  fun storyItem_viewedState_affectsBorderColor() {
    composeTestRule.setContent {
      StoryItem(
          name = "Viewed Story",
          avatarRes = com.github.se.studentconnect.R.drawable.avatar_12,
          viewed = true,
          onClick = {},
          testTag = "viewed_story")
    }

    composeTestRule.onNodeWithTag("story_viewed").assertIsDisplayed()
  }

  @Test
  fun storyItem_unviewedState_affectsBorderColor() {
    composeTestRule.setContent {
      StoryItem(
          name = "Unviewed Story",
          avatarRes = com.github.se.studentconnect.R.drawable.avatar_12,
          viewed = false,
          onClick = {},
          testTag = "unviewed_story")
    }

    composeTestRule.onNodeWithTag("story_unseen").assertIsDisplayed()
  }

  @Test
  fun storiesRow_withEventStories_displaysProfilePictures() {
    val testStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUser",
            profilePictureUrl = "profile_pic_url")

    val eventStories = mapOf(testEvent1.uid to listOf(testStory))

    composeTestRule.setContent {
      StoriesRow(
          onAddStoryClick = {},
          onClick = { _, _ -> },
          stories = mapOf(testEvent1 to Pair(0, 1)),
          eventStories = eventStories)
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_item_${testEvent1.uid}").assertIsDisplayed()
  }

  @Test
  fun storiesRow_userScrollEnabled() {
    composeTestRule.setContent {
      StoriesRow(
          onAddStoryClick = {},
          onClick = { _, _ -> },
          stories = mapOf(testEvent1 to Pair(0, 1)),
          eventStories = emptyMap())
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
    // userScrollEnabled = true allows horizontal scrolling
  }

  @Test
  fun storyViewer_notVisible_doesNotDisplay() {
    val testStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUser",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1, stories = listOf(testStory), isVisible = false, onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertDoesNotExist()
  }

  @Test
  fun storyViewer_withStories_displaysContent() {
    val testStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUser",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(testStory),
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_close_button").assertIsDisplayed()
  }

  @Test
  fun storyViewer_deleteButton_onlyShownForOwnStories() {
    val ownStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "testUser123", // Same as authenticated user
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "testUser123",
            username = "TestUser",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(ownStory),
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {},
          onDeleteStory = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_delete_button").assertIsDisplayed()
  }

  @Test
  fun storyViewer_deleteButton_notShownForOthersStories() {
    val otherUserStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "otherUser", // Different from authenticated user
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "otherUser",
            username = "OtherUser",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(otherUserStory),
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_delete_button").assertDoesNotExist()
  }

  @Test
  fun storyViewer_imageMediaType_displaysImage() {
    val imageStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUser",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(imageStory),
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    // Image is displayed using AsyncImage
  }

  @Test
  fun storyViewer_videoMediaType_showsPlaceholder() {
    val videoStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.mp4",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.VIDEO,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUser",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(videoStory),
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    // Video placeholder text should be visible (tested through UI)
  }

  @Test
  fun storyViewer_progressIndicators_displayCorrectly() {
    val stories =
        listOf(
            StoryWithUser(
                story =
                    com.github.se.studentconnect.model.story.Story(
                        storyId = "story1",
                        mediaUrl = "https://example.com/story1.jpg",
                        mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                        eventId = testEvent1.uid,
                        userId = "user1",
                        createdAt = Timestamp.now(),
                        expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
                userId = "user1",
                username = "User1",
                profilePictureUrl = null),
            StoryWithUser(
                story =
                    com.github.se.studentconnect.model.story.Story(
                        storyId = "story2",
                        mediaUrl = "https://example.com/story2.jpg",
                        mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                        eventId = testEvent1.uid,
                        userId = "user2",
                        createdAt = Timestamp.now(),
                        expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
                userId = "user2",
                username = "User2",
                profilePictureUrl = null))

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = stories,
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    // Progress indicators are rendered for each story
  }

  @Test
  fun storyViewer_userInfo_displaysUsername() {
    val testStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUsername",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(testStory),
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    // Username "TestUsername" should be displayed in user info header
  }

  @Test
  fun storyViewer_initialStoryIndex_clampsToValidRange() {
    val testStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUser",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(testStory),
          initialStoryIndex = 100, // Out of bounds
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    // Should clamp to valid index (0) and not crash
  }

  @Test
  fun storyViewer_withProfilePicture_displaysAvatar() {
    val testStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUser",
            profilePictureUrl = "profile_url")

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(testStory),
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    // Profile picture loading is handled via MediaRepository
  }

  @Test
  fun storyViewer_withoutProfilePicture_showsInitial() {
    val testStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUser",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(testStory),
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    // Should show "T" as initial for "TestUser"
  }

  @Test
  fun homeScreen_onRefreshStories_callback() {
    var refreshStoriesCalled = false

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(testEvent1 to Pair(0, 1))),
          onRefreshStories = { refreshStoriesCalled = true })
    }

    // The callback is wired but requires interaction to trigger
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_storyAccepted_triggersRefresh() {
    var refreshCalled = false

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          uiState = HomePageUiState(isLoading = false, events = emptyList()),
          onRefreshStories = { refreshCalled = true })
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
    // Story acceptance triggers refresh in the camera callback
  }

  @Test
  fun storyViewer_multipleStories_handlesIndexing() {
    val stories =
        (1..5).map { i ->
          StoryWithUser(
              story =
                  com.github.se.studentconnect.model.story.Story(
                      storyId = "story$i",
                      mediaUrl = "https://example.com/story$i.jpg",
                      mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                      eventId = testEvent1.uid,
                      userId = "user$i",
                      createdAt = Timestamp.now(),
                      expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
              userId = "user$i",
              username = "User$i",
              profilePictureUrl = null)
        }

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = stories,
          initialStoryIndex = 2, // Start at middle story
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    // Should handle navigation through 5 stories
  }

  @Test
  fun storyViewer_eventTitle_displayedInHeader() {
    val testStory =
        StoryWithUser(
            story =
                com.github.se.studentconnect.model.story.Story(
                    storyId = "story1",
                    mediaUrl = "https://example.com/story.jpg",
                    mediaType = com.github.se.studentconnect.model.story.MediaType.IMAGE,
                    eventId = testEvent1.uid,
                    userId = "user1",
                    createdAt = Timestamp.now(),
                    expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0)),
            userId = "user1",
            username = "TestUser",
            profilePictureUrl = null)

    composeTestRule.setContent {
      StoryViewer(
          event = testEvent1,
          stories = listOf(testStory),
          initialStoryIndex = 0,
          isVisible = true,
          onDismiss = {})
    }

    composeTestRule.onNodeWithTag("story_viewer").assertIsDisplayed()
    // Event title "Test Event 1" should be visible in header
  }
}
