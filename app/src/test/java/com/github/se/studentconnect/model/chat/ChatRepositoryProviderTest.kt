package com.github.se.studentconnect.model.chat

import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.Mockito.mock

class ChatRepositoryProviderTest {

  @After
  fun tearDown() {
    // Clean up any test overrides
    ChatRepositoryProvider.cleanOverrideForTests()
  }

  @Test
  fun chatRepositoryProvider_returnsNonNullRepository() {
    val repository = ChatRepositoryProvider.repository

    assertNotNull(repository)
  }

  @Test
  fun chatRepositoryProvider_returnsSameInstanceOnMultipleCalls() {
    val repository1 = ChatRepositoryProvider.repository
    val repository2 = ChatRepositoryProvider.repository

    assertSame(repository1, repository2)
  }

  @Test
  fun chatRepositoryProvider_overrideForTests_returnsMockedRepository() {
    val mockRepository = mock(ChatRepository::class.java)

    ChatRepositoryProvider.overrideForTests(mockRepository)
    val repository = ChatRepositoryProvider.repository

    assertSame(mockRepository, repository)
  }

  @Test
  fun chatRepositoryProvider_cleanOverrideForTests_restoresDefaultRepository() {
    val mockRepository = mock(ChatRepository::class.java)
    val originalRepository = ChatRepositoryProvider.repository

    ChatRepositoryProvider.overrideForTests(mockRepository)
    val overriddenRepository = ChatRepositoryProvider.repository
    assertSame(mockRepository, overriddenRepository)

    ChatRepositoryProvider.cleanOverrideForTests()
    val restoredRepository = ChatRepositoryProvider.repository

    // After cleaning, should return the original default repository
    assertSame(originalRepository, restoredRepository)
  }
}
