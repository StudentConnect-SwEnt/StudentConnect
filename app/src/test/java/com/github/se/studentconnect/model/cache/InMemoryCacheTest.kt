package com.github.se.studentconnect.model.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryCacheTest {

  @Test
  fun peekReturnsCachedValueWhenPresentAndValid() {
    val cache = InMemoryCache<String, Int>()

    cache.put("a", 1)

    assertEquals(1, cache.peek("a"))
  }

  @Test
  fun peekReturnsNullWhenMissing() {
    val cache = InMemoryCache<String, Int>()

    assertNull(cache.peek("absent"))
  }

  @Test
  fun peekInvalidatesStaleEntry() {
    val cache =
        object : InMemoryCache<String, Int>() {
          var valid = true

          override fun isValid(value: Int): Boolean = valid
        }

    cache.put("old", 3)
    cache.valid = false

    assertNull(cache.peek("old")) // invalid entry removed

    cache.valid = true
    assertNull(cache.peek("old")) // stays removed after invalidation
  }

  @Test
  fun invalidateAndInvalidateAllRemoveEntries() {
    val cache = InMemoryCache<String, Int>()

    cache.put("one", 1)
    cache.put("two", 2)

    cache.invalidate("one")
    assertNull(cache.peek("one"))
    assertEquals(2, cache.peek("two"))

    cache.invalidateAll()
    assertNull(cache.peek("two"))
  }
}
