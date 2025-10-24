package com.cache.expiration;

import java.time.Duration;
import java.time.LocalDateTime;
import com.cache.models.CacheEntry;

/**
 * Expiration strategy based on the creation timestamp.
 */
public class CreationBasedExpiration<K, V> implements ExpirationStrategy<K, V> {
  private final int ttlSeconds; // Time-to-live in seconds

  public CreationBasedExpiration(int ttlSeconds) {
    this.ttlSeconds = ttlSeconds;
  }

  @Override
  public boolean isExpired(CacheEntry<K, V> entry) {
    LocalDateTime now = LocalDateTime.now();
    Duration durationSinceCreated = Duration.between(entry.getCreatedTime(), now);
    return durationSinceCreated.getSeconds() > ttlSeconds;
  }
}
