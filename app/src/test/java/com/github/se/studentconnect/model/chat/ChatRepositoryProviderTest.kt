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
  fun chatRepositoryProvider_overrideForTests_returnsMockedRepository() {
    val mockRepository = mock(ChatRepository::class.java)

    ChatRepositoryProvider.overrideForTests(mockRepository)
    val repository = ChatRepositoryProvider.repository

    assertNotNull(repository)
    assertSame(mockRepository, repository)
  }

  @Test
  fun chatRepositoryProvider_cleanOverrideForTests_allowsNewOverride() {
    val mockRepository1 = mock(ChatRepository::class.java)
    val mockRepository2 = mock(ChatRepository::class.java)

    ChatRepositoryProvider.overrideForTests(mockRepository1)
    val repo1 = ChatRepositoryProvider.repository
    assertSame(mockRepository1, repo1)

    ChatRepositoryProvider.cleanOverrideForTests()

    ChatRepositoryProvider.overrideForTests(mockRepository2)
    val repo2 = ChatRepositoryProvider.repository
    assertSame(mockRepository2, repo2)
  }
}
