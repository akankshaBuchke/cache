package com.cache.store;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Backing Store: Simulates a backing store using an internal map.
 */
public class InMemoryBackingStore<K, V> implements BackingStore<K, V> {
  private final ConcurrentHashMap<K, V> storage;

  public InMemoryBackingStore() {
    this.storage = new ConcurrentHashMap<>();
  }

  @Override
  public void store(K key, V value) {
    storage.put(key, value); // Save to in-memory map.
  }

  @Override
  public V retrieve(K key) {
    return storage.get(key); // Retrieve from map.
  }
}
