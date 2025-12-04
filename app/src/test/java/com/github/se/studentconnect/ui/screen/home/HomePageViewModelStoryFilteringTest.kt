package com.github.se.studentconnect.ui.screen.home

import android.content.Context
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.story.MediaType
import com.github.se.studentconnect.model.story.Story
import com.github.se.studentconnect.model.story.StoryRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import com.google.firebase.Timestamp
import io.mockk.*
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomePageViewModelStoryFilteringTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModel: HomePageViewModel
  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockOrganizationRepository: OrganizationRepository
  private lateinit var mockStoryRepository: StoryRepository
  private lateinit var mockFriendsRepository: FriendsRepository
  private lateinit var mockContext: Context

  private val testUserId = "currentUser123"
  private val friendUserId1 = "friend1"
  private val friendUserId2 = "friend2"
  private val nonFriendUserId = "stranger"

  private val testLocation = Location(46.5197, 6.6323, "EPFL")

  private val testEvent =
      Event.Public(
          uid = "event1",
          title = "Test Event",
          description = "Test",
          ownerId = "owner1",
          start = Timestamp(Date(System.currentTimeMillis() + 3600000)),
          end = Timestamp(Date(System.currentTimeMillis() + 7200000)),
          location = testLocation,
          participationFee = 0u,
          isFlash = false,
          subtitle = "Test",
          tags = listOf("Test"))

  private val testUser =
      User(
          userId = testUserId,
          email = "test@test.com",
          username = "testuser",
          firstName = "Test",
          lastName = "User",
          university = "EPFL",
          createdAt = 1000L,
          updatedAt = 1000L)

  private val friendUser1 =
      testUser.copy(userId = friendUserId1, username = "friend1", email = "friend1@test.com")
  private val friendUser2 =
      testUser.copy(userId = friendUserId2, username = "friend2", email = "friend2@test.com")
  private val nonFriendUser =
      testUser.copy(userId = nonFriendUserId, username = "stranger", email = "stranger@test.com")

  @Before
  fun setUp() {
    AuthenticationProvider.testUserId = testUserId
    AuthenticationProvider.local = false

    mockEventRepository = mockk(relaxed = true)
    mockUserRepository = mockk(relaxed = true)
    mockOrganizationRepository = mockk(relaxed = true)
    mockStoryRepository = mockk(relaxed = true)
    mockFriendsRepository = mockk(relaxed = true)
    mockContext = mockk(relaxed = true)

    // Default mock behaviors
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockUserRepository.getUserById(any()) } returns null
    coEvery { mockOrganizationRepository.getAllOrganizations() } returns emptyList()
    coEvery { mockStoryRepository.getUserJoinedEvents(any()) } returns emptyList()
    coEvery { mockStoryRepository.getEventStories(any()) } returns emptyList()
    coEvery { mockFriendsRepository.getFriends(testUserId) } returns emptyList()
  }

  @After
  fun tearDown() {
    AuthenticationProvider.testUserId = null
    unmockkAll()
  }

  @Test
  fun `loadAllSubscribedEventsStories filters stories to show only owner and friends stories`() =
      runTest {
        // Given
        val ownStory =
            Story(
                storyId = "story1",
                userId = testUserId,
                eventId = "event1",
                mediaUrl = "https://example.com/story1.jpg",
                createdAt = Timestamp.now(),
                expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
                mediaType = MediaType.IMAGE)

        val friendStory =
            Story(
                storyId = "story2",
                userId = friendUserId1,
                eventId = "event1",
                mediaUrl = "https://example.com/story2.jpg",
                createdAt = Timestamp.now(),
                expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
                mediaType = MediaType.IMAGE)

        val strangerStory =
            Story(
                storyId = "story3",
                userId = nonFriendUserId,
                eventId = "event1",
                mediaUrl = "https://example.com/story3.jpg",
                createdAt = Timestamp.now(),
                expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
                mediaType = MediaType.IMAGE)

        coEvery { mockStoryRepository.getUserJoinedEvents(testUserId) } returns listOf(testEvent)
        coEvery { mockStoryRepository.getEventStories("event1") } returns
            listOf(ownStory, friendStory, strangerStory)
        coEvery { mockFriendsRepository.getFriends(testUserId) } returns listOf(friendUserId1)
        coEvery { mockUserRepository.getUserById(testUserId) } returns testUser
        coEvery { mockUserRepository.getUserById(friendUserId1) } returns friendUser1
        coEvery { mockUserRepository.getUserById(nonFriendUserId) } returns nonFriendUser

        // When
        viewModel =
            HomePageViewModel(
                eventRepository = mockEventRepository,
                userRepository = mockUserRepository,
                organizationRepository = mockOrganizationRepository,
                storyRepository = mockStoryRepository,
                friendsRepository = mockFriendsRepository,
                context = mockContext)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        val eventStories = uiState.eventStories["event1"]

        assertNotNull(eventStories)
        assertEquals(2, eventStories!!.size) // Only own story and friend's story
        assertTrue(eventStories.any { it.userId == testUserId })
        assertTrue(eventStories.any { it.userId == friendUserId1 })
        assertFalse(eventStories.any { it.userId == nonFriendUserId })
      }

  @Test
  fun `loadAllSubscribedEventsStories shows own stories when friends list is empty`() = runTest {
    // Given
    val ownStory =
        Story(
            storyId = "story1",
            userId = testUserId,
            eventId = "event1",
            mediaUrl = "https://example.com/story1.jpg",
            createdAt = Timestamp.now(),
            expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            mediaType = MediaType.IMAGE)

    coEvery { mockStoryRepository.getUserJoinedEvents(testUserId) } returns listOf(testEvent)
    coEvery { mockStoryRepository.getEventStories("event1") } returns listOf(ownStory)
    coEvery { mockFriendsRepository.getFriends(testUserId) } returns emptyList()
    coEvery { mockUserRepository.getUserById(testUserId) } returns testUser

    // When
    viewModel =
        HomePageViewModel(
            eventRepository = mockEventRepository,
            userRepository = mockUserRepository,
            organizationRepository = mockOrganizationRepository,
            storyRepository = mockStoryRepository,
            friendsRepository = mockFriendsRepository,
            context = mockContext)
    advanceUntilIdle()

    // Then
    val uiState = viewModel.uiState.value
    val eventStories = uiState.eventStories["event1"]

    assertNotNull(eventStories)
    assertEquals(1, eventStories!!.size)
    assertEquals(testUserId, eventStories[0].userId)
  }

  @Test
  fun `loadAllSubscribedEventsStories handles friends repository error gracefully`() = runTest {
    // Given
    val ownStory =
        Story(
            storyId = "story1",
            userId = testUserId,
            eventId = "event1",
            mediaUrl = "https://example.com/story1.jpg",
            createdAt = Timestamp.now(),
            expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            mediaType = MediaType.IMAGE)

    coEvery { mockStoryRepository.getUserJoinedEvents(testUserId) } returns listOf(testEvent)
    coEvery { mockStoryRepository.getEventStories("event1") } returns listOf(ownStory)
    coEvery { mockFriendsRepository.getFriends(testUserId) } throws Exception("Network error")
    coEvery { mockUserRepository.getUserById(testUserId) } returns testUser

    // When
    viewModel =
        HomePageViewModel(
            eventRepository = mockEventRepository,
            userRepository = mockUserRepository,
            organizationRepository = mockOrganizationRepository,
            storyRepository = mockStoryRepository,
            friendsRepository = mockFriendsRepository,
            context = mockContext)
    advanceUntilIdle()

    // Then - should still show own stories
    val uiState = viewModel.uiState.value
    val eventStories = uiState.eventStories["event1"]

    assertNotNull(eventStories)
    assertEquals(1, eventStories!!.size)
    assertEquals(testUserId, eventStories[0].userId)
  }

  @Test
  fun `loadAllSubscribedEventsStories returns empty when storyRepository is null`() = runTest {
    // When
    viewModel =
        HomePageViewModel(
            eventRepository = mockEventRepository,
            userRepository = mockUserRepository,
            organizationRepository = mockOrganizationRepository,
            storyRepository = null,
            friendsRepository = mockFriendsRepository,
            context = mockContext)
    advanceUntilIdle()

    // Then
    val uiState = viewModel.uiState.value
    assertTrue(uiState.subscribedEventsStories.isEmpty())
    assertTrue(uiState.eventStories.isEmpty())
  }

  @Test
  fun `loadAllSubscribedEventsStories returns empty when context is null`() = runTest {
    // When
    viewModel =
        HomePageViewModel(
            eventRepository = mockEventRepository,
            userRepository = mockUserRepository,
            organizationRepository = mockOrganizationRepository,
            storyRepository = mockStoryRepository,
            friendsRepository = mockFriendsRepository,
            context = null)
    advanceUntilIdle()

    // Then
    val uiState = viewModel.uiState.value
    assertTrue(uiState.subscribedEventsStories.isEmpty())
    assertTrue(uiState.eventStories.isEmpty())
  }

  @Test
  fun `refreshStories reloads stories without showing loading spinner`() = runTest {
    // Given
    val story =
        Story(
            storyId = "story1",
            userId = testUserId,
            eventId = "event1",
            mediaUrl = "https://example.com/story1.jpg",
            createdAt = Timestamp.now(),
            expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            mediaType = MediaType.IMAGE)

    coEvery { mockStoryRepository.getUserJoinedEvents(testUserId) } returns listOf(testEvent)
    coEvery { mockStoryRepository.getEventStories("event1") } returns listOf(story)
    coEvery { mockFriendsRepository.getFriends(testUserId) } returns emptyList()
    coEvery { mockUserRepository.getUserById(testUserId) } returns testUser

    viewModel =
        HomePageViewModel(
            eventRepository = mockEventRepository,
            userRepository = mockUserRepository,
            organizationRepository = mockOrganizationRepository,
            storyRepository = mockStoryRepository,
            friendsRepository = mockFriendsRepository,
            context = mockContext)
    advanceUntilIdle()

    // When - refresh stories
    viewModel.refreshStories()
    advanceUntilIdle()

    // Then - stories should be reloaded
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading) // Loading should be false
    assertNotNull(uiState.eventStories["event1"])
    assertEquals(1, uiState.eventStories["event1"]!!.size)
  }

  @Test
  fun `StoryWithUser data class stores correct information`() {
    // Given
    val story =
        Story(
            storyId = "story1",
            userId = testUserId,
            eventId = "event1",
            mediaUrl = "https://example.com/story1.jpg",
            createdAt = Timestamp.now(),
            expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            mediaType = MediaType.IMAGE)

    // When
    val storyWithUser =
        StoryWithUser(
            story = story,
            username = "testuser",
            userId = testUserId,
            profilePictureUrl = "https://example.com/profile.jpg")

    // Then
    assertEquals(story, storyWithUser.story)
    assertEquals("testuser", storyWithUser.username)
    assertEquals(testUserId, storyWithUser.userId)
    assertEquals("https://example.com/profile.jpg", storyWithUser.profilePictureUrl)
  }

  @Test
  fun `loadAllSubscribedEventsStories filters multiple friends correctly`() = runTest {
    // Given
    val ownStory =
        Story(
            storyId = "story1",
            userId = testUserId,
            eventId = "event1",
            mediaUrl = "https://example.com/story1.jpg",
            createdAt = Timestamp.now(),
            expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            mediaType = MediaType.IMAGE)

    val friend1Story =
        Story(
            storyId = "story2",
            userId = friendUserId1,
            eventId = "event1",
            mediaUrl = "https://example.com/story2.jpg",
            createdAt = Timestamp.now(),
            expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            mediaType = MediaType.IMAGE)

    val friend2Story =
        Story(
            storyId = "story3",
            userId = friendUserId2,
            eventId = "event1",
            mediaUrl = "https://example.com/story3.jpg",
            createdAt = Timestamp.now(),
            expiresAt = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            mediaType = MediaType.IMAGE)

    coEvery { mockStoryRepository.getUserJoinedEvents(testUserId) } returns listOf(testEvent)
    coEvery { mockStoryRepository.getEventStories("event1") } returns
        listOf(ownStory, friend1Story, friend2Story)
    coEvery { mockFriendsRepository.getFriends(testUserId) } returns
        listOf(friendUserId1, friendUserId2)
    coEvery { mockUserRepository.getUserById(testUserId) } returns testUser
    coEvery { mockUserRepository.getUserById(friendUserId1) } returns friendUser1
    coEvery { mockUserRepository.getUserById(friendUserId2) } returns friendUser2

    // When
    viewModel =
        HomePageViewModel(
            eventRepository = mockEventRepository,
            userRepository = mockUserRepository,
            organizationRepository = mockOrganizationRepository,
            storyRepository = mockStoryRepository,
            friendsRepository = mockFriendsRepository,
            context = mockContext)
    advanceUntilIdle()

    // Then
    val uiState = viewModel.uiState.value
    val eventStories = uiState.eventStories["event1"]

    assertNotNull(eventStories)
    assertEquals(3, eventStories!!.size) // Own + 2 friends
    assertTrue(eventStories.any { it.userId == testUserId })
    assertTrue(eventStories.any { it.userId == friendUserId1 })
    assertTrue(eventStories.any { it.userId == friendUserId2 })
  }
}
