package com.cache.expiration;

import java.time.LocalDateTime;
import java.time.Duration;
import com.cache.models.CacheEntry;

/**
 * Expiration strategy based on a fixed expiration duration irrespective of usage.
 */
public class TimeBasedExpiration<K, V> implements ExpirationStrategy<K, V> {
  private final LocalDateTime cacheInitializedTime;
  private final int fixedDurationSeconds;

  public TimeBasedExpiration(int fixedDurationSeconds) {
    this.cacheInitializedTime = LocalDateTime.now(); // Timestamp when the cache was created
    this.fixedDurationSeconds = fixedDurationSeconds;
  }

  @Override
  public boolean isExpired(CacheEntry<K, V> entry) {
    LocalDateTime now = LocalDateTime.now();
    Duration durationSinceCacheInitialization = Duration.between(cacheInitializedTime, now);
    return durationSinceCacheInitialization.getSeconds() > fixedDurationSeconds;
  }
}
