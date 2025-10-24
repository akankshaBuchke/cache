package com.cache.policies;

import java.util.LinkedHashSet;

public class LRUCacheEvictionPolicy<K> implements CacheEvictionPolicy<K> {
  private final LinkedHashSet<K> accessOrder; // Keeps keys in order of usage.

  public LRUCacheEvictionPolicy() {
    this.accessOrder = new LinkedHashSet<>();
  }

  @Override
  public void recordAccess(K key) {
    accessOrder.remove(key); // Remove if present (move to end)
    accessOrder.add(key);   // Add as most recently used.
  }

  @Override
  public K getEvictionCandidate() {
    return accessOrder.iterator().next(); // Return least recently used (first in order).
  }

  @Override
  public void removeKey(K key) {
    accessOrder.remove(key);
  }
}
