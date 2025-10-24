package com.cache.expiration;

import com.cache.models.CacheEntry;

/**
 * Interface for defining expiration strategies.
 */
public interface ExpirationStrategy<K, V> {
  boolean isExpired(CacheEntry<K, V> entry);
}
