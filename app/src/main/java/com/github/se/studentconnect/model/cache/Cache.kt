package com.github.se.studentconnect.model.cache

/** Interface for any type of cache. */
interface Cache<K, V> {
  /**
   * Get a value in the cache based on its key if it is present and valid.
   *
   * @param key the key
   * @return the value (null if missing or invalid)
   */
  fun peek(key: K): V?

  /**
   * Write a value to the cache.
   *
   * @param key the key
   * @param value the value
   */
  fun put(key: K, value: V)

  /**
   * Invalidate a single entry.
   *
   * @param key the key
   */
  fun invalidate(key: K)

  /** Invalidate the entire cache. */
  fun invalidateAll()
}
