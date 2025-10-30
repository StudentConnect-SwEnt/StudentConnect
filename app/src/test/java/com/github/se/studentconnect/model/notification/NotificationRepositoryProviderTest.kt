package com.github.se.studentconnect.model.notification

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class NotificationRepositoryProviderTest {

  @Mock private lateinit var mockRepository: NotificationRepository

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    // Clear any previous repository
    NotificationRepositoryProvider.clearRepository()
  }

  @After
  fun tearDown() {
    NotificationRepositoryProvider.clearRepository()
  }

  @Test
  fun setRepository_storesRepository() {
    NotificationRepositoryProvider.setRepository(mockRepository)

    val retrieved = NotificationRepositoryProvider.repository

    assert(retrieved === mockRepository) { "Repository should be the same instance" }
  }

  @Test
  fun repository_throwsExceptionWhenNotInitialized() {
    var exceptionThrown = false

    try {
      NotificationRepositoryProvider.repository
    } catch (e: IllegalStateException) {
      exceptionThrown = true
      assert(e.message?.contains("not initialized") == true) {
        "Exception message should mention not initialized"
      }
    }

    assert(exceptionThrown) { "Should throw IllegalStateException when not initialized" }
  }

  @Test
  fun clearRepository_removesRepository() {
    NotificationRepositoryProvider.setRepository(mockRepository)
    NotificationRepositoryProvider.clearRepository()

    var exceptionThrown = false
    try {
      NotificationRepositoryProvider.repository
    } catch (e: IllegalStateException) {
      exceptionThrown = true
    }

    assert(exceptionThrown) { "Should throw exception after clearing repository" }
  }

  @Test
  fun setRepository_replacesExistingRepository() {
    val firstMock = mockRepository
    NotificationRepositoryProvider.setRepository(firstMock)

    val secondMock = org.mockito.Mockito.mock(NotificationRepository::class.java)
    NotificationRepositoryProvider.setRepository(secondMock)

    val retrieved = NotificationRepositoryProvider.repository

    assert(retrieved === secondMock) { "Repository should be updated to new instance" }
    assert(retrieved !== firstMock) { "Repository should not be the old instance" }
  }
}
