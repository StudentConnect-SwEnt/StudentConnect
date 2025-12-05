package com.github.se.studentconnect.model.cache

/** Base class implementing high-level cache logic. */
abstract class BaseCache<K, V> : Cache<K, V> {
  override fun peek(key: K): V? {
    val value = readCached(key) ?: return null
    return if (isValid(key, value)) value
    else {
      removeCached(key)
      null
    }
  }

  override fun invalidate(key: K) {
    removeCached(key)
  }

  override fun invalidateAll() {
    clearCached()
  }

  /**
   * Returns whether the value is valid or not.
   *
   * @param key the key
   * @param value the value
   */
  protected open fun isValid(key: K, value: V): Boolean = true

  /**
   * Get a value in the cache based on its key.
   *
   * @param key the key
   * @return the value
   */
  protected abstract fun readCached(key: K): V?

  /**
   * Remove a value from the cache based on its key.
   *
   * @param key the key
   */
  protected abstract fun removeCached(key: K)

  /** Clear the cache. */
  protected abstract fun clearCached()
}
