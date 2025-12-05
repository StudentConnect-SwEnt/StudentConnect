package com.github.se.studentconnect.model.cache

/** A cache that caches things in memory. */
abstract class InMemoryCache<K, V> : BaseCache<K, V>() {
  private val map = mutableMapOf<K, V>()

  override fun put(key: K, value: V) {
    map[key] = value
  }

  override fun readCached(key: K): V? = map[key]

  override fun removeCached(key: K) {
    map.remove(key)
  }

  override fun clearCached() {
    map.clear()
  }
}
