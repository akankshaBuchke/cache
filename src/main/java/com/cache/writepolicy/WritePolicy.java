package com.cache.writepolicy;

/**
 * Interface for write policies.
 * Defines how changes to cached data are propagated to the backing store.
 */
public interface WritePolicy<K, V> {
  void handleWrite(K key, V value);
  void handleEviction(K key, V value);
}
