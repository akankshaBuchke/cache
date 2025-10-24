package com.cache.services;

import com.cache.exceptions.CacheException;
import com.cache.models.CacheEntry;
import com.cache.policies.CacheEvictionPolicy;
import com.cache.store.BackingStore;
import com.cache.writepolicy.WritePolicy;
import com.cache.expiration.ExpirationStrategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CacheService<K, V> {
  private static final Logger logger = Logger.getLogger(CacheService.class.getName());

  private final ConcurrentHashMap<K, CacheEntry<K, V>> cache; // Thread-safe map for cache storage.
  private final int capacity; // Maximum number of items in cache.
  private final CacheEvictionPolicy<K> evictionPolicy; // Configurable eviction policy.
  private final WritePolicy<K, V> writePolicy; // Configurable write policy.
  private final BackingStore<K, V> backingStore; // Configurable backing store.
  private final ExpirationStrategy<K, V> expirationStrategy; // Configurable expiration strategy
  private final ScheduledExecutorService refreshScheduler; // Scheduler for refresh logic
  private final int refreshInterval; // Refresh interval in seconds
  private ScheduledFuture<?> scheduledRefreshTask; // Future object for scheduled refresh task

  /**
   * Constructor for CacheService
   */
  public CacheService(int capacity,
      int refreshInterval,
      CacheEvictionPolicy<K> evictionPolicy,
      BackingStore<K, V> backingStore,
      WritePolicy<K, V> writePolicy,
      ExpirationStrategy<K, V> expirationStrategy) {
    // Validate refresh interval
    if (refreshInterval < 0) {
      throw new IllegalArgumentException("Refresh interval must be >= 0.");
    }

    this.refreshScheduler = Executors.newScheduledThreadPool(1); // Single-threaded scheduler
    this.refreshInterval = refreshInterval;
    this.cache = new ConcurrentHashMap<>();
    this.capacity = capacity;
    this.evictionPolicy = evictionPolicy;
    this.backingStore = backingStore;
    this.writePolicy = writePolicy;
    this.expirationStrategy = expirationStrategy;

    // Schedule refresh only if refresh interval is > 0
    if (refreshInterval > 0) {
      scheduleRefresh();
    } else {
      logger.warning("Refresh interval is set to 0. Refresh mechanism is disabled.");
    }
  }

  public void put(K key, V value) {
    if (cache.size() >= capacity) {
      K evictionCandidate = evictionPolicy.getEvictionCandidate();
      CacheEntry<K, V> evictedEntry = cache.remove(evictionCandidate);
      if (evictedEntry != null) {
        writePolicy.handleEviction(evictedEntry.getKey(), evictedEntry.getValue()); // Handle eviction via write policy.
        evictionPolicy.removeKey(evictionCandidate);
        logger.info("Evicted key: " + evictionCandidate);
      }
    }

    CacheEntry<K, V> entry = new CacheEntry<>(key, value); // Create cache entry
    cache.put(key, entry);
    evictionPolicy.recordAccess(key); // Track access for eviction policy
    writePolicy.handleWrite(key, value); // Handle write based on write policy
    logger.info("Cached key: " + key + " (value: " + value + ")");
  }

  public V get(K key) throws CacheException {
    CacheEntry<K, V> entry = cache.get(key);

    if (entry == null) {
      // Item not found in cache; try backing store
      logger.info("Cache miss! Key '" + key + "' not found in cache, retrieving from backing store...");
      V value = backingStore.retrieve(key);
      if (value == null) {
        throw new CacheException("Key '" + key + "' not found in cache or backing store.");
      }
      put(key, value); // Add retrieved value to cache
      return value;
    }

    // Expiration logic: Use the expiration strategy dynamically
    if (expirationStrategy.isExpired(entry)) {
      cache.remove(key);
      evictionPolicy.removeKey(key);
      logger.warning("Expired key '" + key + "' was removed!");
      throw new CacheException("Key '" + key + "' has expired and was removed!");
    }

    evictionPolicy.recordAccess(key); // Update LRU policy tracking
    entry.refreshLastAccessed(); // Update access time
    return entry.getValue();
  }

  public void remove(K key) {
    CacheEntry<K, V> removedEntry = cache.remove(key);
    evictionPolicy.removeKey(key);
    if (removedEntry != null) {
      logger.info("Removed key: " + key);
    }
  }

  /**
   * Schedule periodic refresh of cache entries from the backing store.
   */
  private void scheduleRefresh() {
    logger.info("Initializing refresh task with interval: " + refreshInterval + " seconds...");
    refreshScheduler.scheduleAtFixedRate(() -> {
      logger.info("Refreshing cache entries from backing store...");
      for (K key : cache.keySet()) {
        try {
          // Refresh the value for each key from the backing store
          V refreshedValue = backingStore.retrieve(key);
          if (refreshedValue != null) {
            CacheEntry<K, V> entry = cache.get(key);
            if (entry != null) {
              entry.setValue(refreshedValue); // Update value in the cache
              logger.info("Refreshed key '" + key + "' with updated value.");
            }
          }
        } catch (Exception e) {
          logger.severe("Failed to refresh key '" + key + "': " + e.getMessage());
        }
      }
    }, refreshInterval, refreshInterval, TimeUnit.SECONDS);
  }

  /**
   * Shutdown the refresh scheduler gracefully.
   */
  public void shutdownScheduler() {
    refreshScheduler.shutdown();
    logger.info("Refresh scheduler shut down.");
  }

  /**
   * Shutdown the refresh scheduler gracefully.
   */
//  public void shutdownScheduler() {
//    logger.info("Attempting to shut down the refresh scheduler...");
//    refreshScheduler.shutdown(); // Gracefully shutdown (stop accepting tasks but allow running tasks to finish)
//
//    try {
//      // Wait for running tasks to finish
//      if (!refreshScheduler.awaitTermination(5, TimeUnit.SECONDS)) { // Wait max 5 seconds
//        logger.warning("Forcing shutdown of refresh scheduler as tasks did not terminate in time.");
//        refreshScheduler.shutdownNow(); // Force immediate termination
//      }
//    } catch (InterruptedException e) {
//      logger.severe("Error while waiting for refresh scheduler shutdown: " + e.getMessage());
//      refreshScheduler.shutdownNow(); // Force immediate shutdown to prevent any tasks running
//    }
//
//    logger.info("Refresh scheduler shut down successfully.");
//  }

//  public void shutdownScheduler() {
//    if (scheduledRefreshTask != null) {
//      logger.info("Canceling scheduled refresh task...");
//      scheduledRefreshTask.cancel(true); // Cancel refresh task explicitly
//      logger.info("Successfully canceled scheduled refresh.");
//    }
//    refreshScheduler.shutdown(); // Gracefully shut down the scheduler
//    try {
//      if (!refreshScheduler.awaitTermination(5, TimeUnit.SECONDS)) { // Wait for max 5 seconds for termination
//        logger.warning("Forcing termination of refresh scheduler.");
//        refreshScheduler.shutdownNow(); // Force immediate termination
//      }
//      logger.info("Refresh scheduler shut down successfully.");
//    } catch (InterruptedException e) {
//      logger.severe("Failed to shut down refresh scheduler: " + e.getMessage());
//      refreshScheduler.shutdownNow(); // Ensure forceful termination
//    }
//  }


}
