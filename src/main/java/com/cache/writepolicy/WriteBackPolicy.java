package com.cache.writepolicy;

import com.cache.store.BackingStore;

/**
 * Write-Back Policy: Writes to the backing store only when the cache evicts an entry.
 */
public class WriteBackPolicy<K, V> implements WritePolicy<K, V> {
  private final BackingStore<K, V> backingStore;

  public WriteBackPolicy(BackingStore<K, V> backingStore) {
    this.backingStore = backingStore;
  }

  @Override
  public void handleWrite(K key, V value) {
    // No immediate write to backing store (data is kept in cache).
  }

  @Override
  public void handleEviction(K key, V value) {
    backingStore.store(key, value); // Write to backing store only on eviction.
  }
}
