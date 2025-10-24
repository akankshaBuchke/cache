package com.cache.writepolicy;

import com.cache.store.BackingStore;

/**
 * Write-Through Policy: Writes to the backing store immediately whenever the cache is updated.
 */
public class WriteThroughPolicy<K, V> implements WritePolicy<K, V> {
  private final BackingStore<K, V> backingStore;

  public WriteThroughPolicy(BackingStore<K, V> backingStore) {
    this.backingStore = backingStore;
  }

  @Override
  public void handleWrite(K key, V value) {
    backingStore.store(key, value); // Immediately writes to the backing store.
  }

  @Override
  public void handleEviction(K key, V value) {
    // No additional handling for eviction since data is already in the backing store.
  }
}
