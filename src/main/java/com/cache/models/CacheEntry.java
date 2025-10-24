package com.cache.models;

import java.time.LocalDateTime;

public class CacheEntry<K, V> {
  private final K key;
  private V value;
  private final LocalDateTime createdTime;
  private LocalDateTime lastAccessed;

  public CacheEntry(K key, V value) {
    this.key = key;
    this.value = value;
    this.createdTime = LocalDateTime.now();
    this.lastAccessed = LocalDateTime.now();
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public void setValue(V newValue) {
    this.value = newValue;
    this.lastAccessed = LocalDateTime.now(); // Update last accessed
  }

  public LocalDateTime getCreatedTime() {
    return createdTime;
  }

  public LocalDateTime getLastAccessed() {
    return lastAccessed;
  }

  public void refreshLastAccessed() {
    this.lastAccessed = LocalDateTime.now();
  }
}
