package com.github.se.studentconnect.model.poll

import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PollRepositoryProviderTest {

  @Before
  fun setUp() {
    // Reset the singleton before each test
    PollRepositoryProvider.setRepository(PollRepositoryFirestore(mockk<FirebaseFirestore>()))
  }

  @Test
  fun repository_returnsNonNullInstance() {
    val repository = PollRepositoryProvider.repository

    assertNotNull(repository)
  }

  @Test
  fun repository_returnsSameInstanceOnMultipleCalls() {
    val repo1 = PollRepositoryProvider.repository
    val repo2 = PollRepositoryProvider.repository

    assertSame(repo1, repo2)
  }

  @Test
  fun setRepository_replacesInstance() {
    val mockRepo = mockk<PollRepository>()

    PollRepositoryProvider.setRepository(mockRepo)
    val repository = PollRepositoryProvider.repository

    assertSame(mockRepo, repository)
  }

  @Test
  fun setRepository_newInstanceReturnedAfterSet() {
    val initialRepo = PollRepositoryProvider.repository
    val mockRepo = mockk<PollRepository>()

    PollRepositoryProvider.setRepository(mockRepo)
    val newRepo = PollRepositoryProvider.repository

    assertNotSame(initialRepo, newRepo)
    assertSame(mockRepo, newRepo)
  }
}
