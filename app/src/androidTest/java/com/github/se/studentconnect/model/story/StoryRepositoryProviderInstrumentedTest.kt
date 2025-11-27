package com.github.se.studentconnect.model.story

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoryRepositoryProviderInstrumentedTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = InstrumentationRegistry.getInstrumentation().targetContext
  }

  @Test
  fun getRepository_returnsNonNullInstance() {
    val repository = StoryRepositoryProvider.getRepository(context)

    assertNotNull("Repository should not be null", repository)
    assertTrue("Repository should be StoryRepositoryFirestore", repository is StoryRepositoryFirestore)
  }

  @Test
  fun getRepository_returnsStoryRepositoryInstance() {
    val repository = StoryRepositoryProvider.getRepository(context)

    // StoryRepositoryFirestore implements StoryRepository, so this check is always true
    // but it verifies the interface contract
    assertTrue("Repository should implement StoryRepository", repository is StoryRepository)
    assertNotNull("Repository should not be null", repository)
  }
}

