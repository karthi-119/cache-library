package com.cache.backend;

import com.cache.backend.cache.CustomCache;
import com.cache.backend.cache.ThreadSafeCustomCache;
import com.cache.backend.model.CacheMetrics;
import com.cache.backend.model.EvictionPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CustomCacheTest {

    @Test
    @DisplayName("Basic GET, PUT, DELETE, and CLEAR operations")
    void testBasicOperations() {
        CustomCache<String, Object> cache = new ThreadSafeCustomCache<>(5, EvictionPolicy.LRU);

        cache.put("user1", "Alice", null);
        cache.put("user2", "Bob", 30L);

        assertEquals("Alice", cache.get("user1"));
        assertEquals("Bob", cache.get("user2"));
        assertNull(cache.get("nonexistent"));

        assertTrue(cache.delete("user1"));
        assertNull(cache.get("user1"));
        assertEquals(1, cache.size());

        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("LRU Eviction removes the least recently used entry")
    void testLruEviction() {
        CustomCache<String, Object> cache = new ThreadSafeCustomCache<>(3, EvictionPolicy.LRU);

        cache.put("A", "Apple", null);
        cache.put("B", "Banana", null);
        cache.put("C", "Cherry", null);

        // Access A, making B the least recently used
        assertEquals("Apple", cache.get("A"));

        // Insert D, which should evict B
        cache.put("D", "Date", null);

        assertNotNull(cache.get("A"), "A should still be in cache");
        assertNull(cache.get("B"), "B should have been evicted by LRU");
        assertNotNull(cache.get("C"), "C should still be in cache");
        assertNotNull(cache.get("D"), "D should be in cache");

        CacheMetrics metrics = cache.getMetrics();
        assertEquals(1, metrics.totalEvictions(), "Should have recorded 1 capacity eviction");
    }

    @Test
    @DisplayName("LFU Eviction removes the least frequently used entry")
    void testLfuEviction() {
        CustomCache<String, Object> cache = new ThreadSafeCustomCache<>(3, EvictionPolicy.LFU);

        cache.put("A", "Apple", null);
        cache.put("B", "Banana", null);
        cache.put("C", "Cherry", null);

        // Access A multiple times (higher frequency)
        cache.get("A");
        cache.get("A");

        // Access B once
        cache.get("B");

        // C has the lowest frequency (1 from initial put)
        // Inserting D should evict C
        cache.put("D", "Date", null);

        assertNotNull(cache.get("A"), "A should be present with high frequency");
        assertNotNull(cache.get("B"), "B should be present");
        assertNull(cache.get("C"), "C should be evicted as least frequently used");
        assertNotNull(cache.get("D"), "D should be present");
    }

    @Test
    @DisplayName("LFU Eviction tie-breaker uses LRU among same-frequency entries")
    void testLfuTieBreaker() throws InterruptedException {
        CustomCache<String, Object> cache = new ThreadSafeCustomCache<>(3, EvictionPolicy.LFU);

        cache.put("A", "Apple", null);
        Thread.sleep(10);
        cache.put("B", "Banana", null);
        Thread.sleep(10);
        cache.put("C", "Cherry", null);

        // All A, B, C have frequency 1.
        // Access B so B has freq 2.
        cache.get("B");

        // A and C both have freq 1, but A is older / least recently used.
        // Inserting D should evict A due to the tie-breaker.
        cache.put("D", "Date", null);

        assertNull(cache.get("A"), "A should be evicted as tie-breaker LRU among freq 1");
        assertNotNull(cache.get("B"), "B has higher frequency");
        assertNotNull(cache.get("C"), "C is newer than A with freq 1");
        assertNotNull(cache.get("D"), "D is newly inserted");
    }

    @Test
    @DisplayName("TTL lazy expiration behaves as cache miss and increments TTL expiration counter")
    void testTtlExpiration() throws InterruptedException {
        CustomCache<String, Object> cache = new ThreadSafeCustomCache<>(5, EvictionPolicy.LRU);

        // Entry with 1-second TTL
        cache.put("tempKey", "TempValue", 1L);
        assertEquals("TempValue", cache.get("tempKey"), "Should hit immediately");

        // Sleep for 1.2 seconds to allow TTL to expire
        Thread.sleep(1200);

        assertNull(cache.get("tempKey"), "Expired key should behave as a cache miss");

        CacheMetrics metrics = cache.getMetrics();
        assertEquals(1, metrics.totalTtlExpirations(), "Should record 1 TTL expiration");
        assertEquals(0, metrics.totalEvictions(), "Capacity evictions should remain 0");
        assertTrue(metrics.misses() >= 1, "Miss count should be at least 1");
    }

    @Test
    @DisplayName("Switching eviction policy does not destroy existing entries or TTL info")
    void testPolicySwitching() {
        CustomCache<String, Object> cache = new ThreadSafeCustomCache<>(3, EvictionPolicy.LRU);

        cache.put("K1", "V1", 60L);
        cache.put("K2", "V2", 120L);

        assertEquals(EvictionPolicy.LRU, cache.getEvictionPolicy());

        // Switch policy to LFU
        cache.setEvictionPolicy(EvictionPolicy.LFU);
        assertEquals(EvictionPolicy.LFU, cache.getEvictionPolicy());

        // Verify entries and TTL are preserved
        assertEquals("V1", cache.get("K1"));
        assertEquals("V2", cache.get("K2"));
        assertEquals(2, cache.size());
    }

    @Test
    @DisplayName("Thread safety under concurrent multi-threaded access")
    void testConcurrentAccess() throws InterruptedException {
        int threads = 10;
        int operationsPerThread = 50;
        int capacity = 5;

        CustomCache<String, Object> cache = new ThreadSafeCustomCache<>(capacity, EvictionPolicy.LRU);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "key-" + (j % 10);
                        cache.put(key, "thread-" + threadId + "-val-" + j, 10L);
                        cache.get(key);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "All concurrent tasks should complete within timeout");
        assertTrue(cache.size() <= capacity, "Cache size must not exceed capacity under concurrency");
        CacheMetrics metrics = cache.getMetrics();
        assertTrue(metrics.totalPutOperations() > 0);
        assertTrue(metrics.totalGetRequests() > 0);
    }
}
