package com.cache.expiration;

import java.time.Duration;
import java.time.LocalDateTime;
import com.cache.models.CacheEntry;

/**
 * Expiration strategy based on the last accessed timestamp.
 */
public class AccessBasedExpiration<K, V> implements ExpirationStrategy<K, V> {
  private final int ttlSeconds; // Time-to-live in seconds

  public AccessBasedExpiration(int ttlSeconds) {
    this.ttlSeconds = ttlSeconds;
  }

  @Override
  public boolean isExpired(CacheEntry<K, V> entry) {
    LocalDateTime now = LocalDateTime.now();
    Duration durationSinceLastAccessed = Duration.between(entry.getLastAccessed(), now);
    return durationSinceLastAccessed.getSeconds() > ttlSeconds;
  }
}
