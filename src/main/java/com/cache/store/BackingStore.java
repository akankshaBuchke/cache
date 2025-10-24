package com.cache.store;

/**
 * Interface to abstract backing store functionality.
 * Allows different types of backing stores to be used (e.g., in-memory, database, etc.).
 */
public interface BackingStore<K, V> {
  void store(K key, V value);
  V retrieve(K key);
}
