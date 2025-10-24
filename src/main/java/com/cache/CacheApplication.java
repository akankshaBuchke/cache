package com.cache;

import com.cache.expiration.AccessBasedExpiration;
import com.cache.expiration.CreationBasedExpiration;
import com.cache.expiration.TimeBasedExpiration;
import com.cache.services.CacheService;
import com.cache.policies.LRUCacheEvictionPolicy;
import com.cache.store.InMemoryBackingStore;
import com.cache.writepolicy.WriteBackPolicy;
import com.cache.exceptions.CacheException;

import java.util.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CacheApplication {
  // Initialize Logger
  private static final Logger logger = Logger.getLogger(CacheApplication.class.getName());

  public static void main(String[] args) {
    SpringApplication.run(CacheApplication.class, args);
    // Initialize components.
    LRUCacheEvictionPolicy<String> evictionPolicy = new LRUCacheEvictionPolicy<>();
    InMemoryBackingStore<String, String> backingStore = new InMemoryBackingStore<>();
    WriteBackPolicy<String, String> writeBackPolicy = new WriteBackPolicy<>(backingStore);

    // Prepopulate backing store with values
    backingStore.store("key1", "value1");
    backingStore.store("key2", "value2_updated");
    backingStore.store("key3", "value3");

    // Create cache service with capacity = 2.
    CacheService<String, String> cache = new CacheService<>(2,3, evictionPolicy, backingStore, writeBackPolicy, new AccessBasedExpiration<>(5));

    try {
      // -------------------------------
      // Test Case 1: Add and Retrieve
      logger.info("---------- Test Case 1 ----------");
      cache.put("key1", "value1");
      logger.info("Get key1: " + cache.get("key1")); // Expected Output: "value1"

      // -------------------------------
      // Test Case 2: Retrieve Non-existent Key
      logger.info("---------- Test Case 2 ----------");
      try {
        logger.info("Get keyX: " + cache.get("keyX")); // Expected Output: null (or CacheException).
      } catch (CacheException e) {
        logger.warning("Exception occurred while retrieving 'keyX': " + e.getMessage());
      }

      // -------------------------------
      // Test Case 3: Update Existing Key
      logger.info("---------- Test Case 3 ----------");
      cache.put("key1", "value2");
      logger.info("Get key1: " + cache.get("key1")); // Expected Output: "value2"

      // -------------------------------
      // Test Case 4: Remove Key
      logger.info("---------- Test Case 4 ----------");
      cache.put("key1", "value1");
      cache.remove("key1");
      try {
        logger.info("Get key1: " + cache.get("key1")); // Expected Output: null (or CacheException).
      } catch (CacheException e) {
        logger.warning("Exception occurred while retrieving 'key1' after removal: " + e.getMessage());
      }

      // -------------------------------
      // Test Case 5: Evict on Capacity
      logger.info("---------- Test Case 5 ----------");
      cache.put("key1", "value1"); // Add key1
      cache.put("key2", "value2"); // Add key2
      cache.put("key3", "value3"); // Add key3 (evicts key1 due to LRU)
      try {
        logger.info("Get key1: " + cache.get("key1")); // Expected Output: null (key1 should be evicted)
      } catch (CacheException e) {
        logger.warning("Exception: Key1 evicted! " + e.getMessage());
      }

      // Verify remaining keys.
      logger.info("Get key2: " + cache.get("key2")); // Expected Output: "value2"
      logger.info("Get key3: " + cache.get("key3")); // Expected Output: "value3"




//
//      // Test Case 1: Access-Based Expiration
//      logger.info("---------- Access-Based Expiration ----------");
//      CacheService<String, String> accessBasedCache = new CacheService<>(2, 3, evictionPolicy, backingStore, writeBackPolicy, new AccessBasedExpiration<>(5));
//      accessBasedCache.put("key1", "value1");
//      Thread.sleep(6000);
//      try {
//        logger.info("Get key1: " + accessBasedCache.get("key1"));
//      } catch (Exception e) {
//        logger.warning(e.getMessage());
//      }
//
//      // Test Case 2: Creation-Based Expiration
//      logger.info("---------- Creation-Based Expiration ----------");
//      CacheService<String, String> creationBasedCache = new CacheService<>(2,3,  evictionPolicy, backingStore, writeBackPolicy, new CreationBasedExpiration<>(3));
//      creationBasedCache.put("key2", "value2");
//      Thread.sleep(4000); // Wait longer than TTL
//      try {
//        logger.info("Get key2: " + creationBasedCache.get("key2"));
//      } catch (Exception e) {
//        logger.warning(e.getMessage());
//      }

//      // Test Case 3: Fixed Time Expiration
//      logger.info("---------- Fixed Time Expiration ----------");
//      CacheService<String, String> timeBasedCache = new CacheService<>(2,3,  evictionPolicy, backingStore, writeBackPolicy, new TimeBasedExpiration<>(5));
//      timeBasedCache.put("key3", "value3");
//      Thread.sleep(7000);
//      try {
//        logger.info("Get key3: " + timeBasedCache.get("key3"));
//      } catch (Exception e) {
//        logger.warning(e.getMessage());
//      }



      // Add items to the cache
      cache.put("key1", "value1");
      cache.put("key2", "value2");

      // Simulate refresh by waiting
      Thread.sleep(7000); // Wait for 5 seconds, exceeding refresh interval
      logger.info("Get key1 after refresh: " + cache.get("key1")); // Should retrieve updated value from backing store
      logger.info("Get key2 after refresh: " + cache.get("key2")); // Should retrieve updated value from backing store

      // Cleanup
      cache.shutdownScheduler();
      // Wait for additional time to verify no refresh tasks are running
      Thread.sleep(7000);
      logger.info("Test completed. Refresh tasks should NOT execute after shutdown.");
      //logger.info("scheduler shutdown verified");


    } catch (Exception ex) {
      logger.severe("Unexpected exception occurred: " + ex.getMessage());
    }
  }
}
