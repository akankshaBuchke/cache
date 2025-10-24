package com.cache.policies;


/**
 * Interface for cache eviction policies.
 * Ensures flexibility by allowing any eviction policy to be plugged into the cache.
 */
public interface CacheEvictionPolicy<K> {
  void recordAccess(K key);
  K getEvictionCandidate();
  void removeKey(K key);
}
