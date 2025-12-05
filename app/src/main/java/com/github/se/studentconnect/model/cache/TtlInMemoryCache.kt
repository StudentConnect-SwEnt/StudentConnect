package com.github.se.studentconnect.model.cache

/**
 * In-memory cache with a fixed time-to-live per entry.
 *
 * @param ttlMs how long an entry stays valid after insertion
 * @param now time source for testing
 */
open class TtlInMemoryCache<K, V>(
    private val ttlMs: Long,
    private val now: () -> Long = { System.currentTimeMillis() }
) : InMemoryCache<K, V>() {
  private val timestamps = mutableMapOf<K, Long>()

  override fun put(key: K, value: V) {
    timestamps[key] = now()
    super.put(key, value)
  }

  override fun removeCached(key: K) {
    timestamps.remove(key)
    super.removeCached(key)
  }

  override fun clearCached() {
    timestamps.clear()
    super.clearCached()
  }

  override fun isValid(key: K, value: V): Boolean {
    val storedAt = timestamps[key] ?: return false
    if (now() - storedAt >= ttlMs) return false
    return super.isValid(key, value)
  }
}
