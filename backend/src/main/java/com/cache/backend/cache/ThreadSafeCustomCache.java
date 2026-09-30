package com.cache.backend.cache;

import com.cache.backend.model.CacheEntryView;
import com.cache.backend.model.CacheMetrics;
import com.cache.backend.model.CacheNode;
import com.cache.backend.model.EvictionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeCustomCache<K, V> implements CustomCache<K, V> {

    private final Map<K, CacheNode<K, V>> storage;
    private final ReentrantLock lock;
    private int capacity;
    private EvictionPolicy currentPolicy;
    private EvictionStrategy<K, V> evictionStrategy;

    // Metrics counters
    private final AtomicLong totalHits = new AtomicLong(0);
    private final AtomicLong totalMisses = new AtomicLong(0);
    private final AtomicLong totalPutOperations = new AtomicLong(0);
    private final AtomicLong totalEvictions = new AtomicLong(0);
    private final AtomicLong totalTtlExpirations = new AtomicLong(0);

    public ThreadSafeCustomCache(int initialCapacity, EvictionPolicy initialPolicy) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = initialCapacity;
        this.currentPolicy = initialPolicy != null ? initialPolicy : EvictionPolicy.LRU;
        this.storage = new HashMap<>();
        this.lock = new ReentrantLock(true); // Fair lock
        this.evictionStrategy = createStrategy(this.currentPolicy);
    }

    private EvictionStrategy<K, V> createStrategy(EvictionPolicy policy) {
        if (policy == EvictionPolicy.LFU) {
            return new LfuEvictionStrategy<>();
        }
        return new LruEvictionStrategy<>();
    }

    @Override
    public V get(K key) {
        lock.lock();
        try {
            CacheNode<K, V> node = storage.get(key);
            if (node == null) {
                totalMisses.incrementAndGet();
                return null;
            }

            if (node.isExpired()) {
                // Lazy expiration
                evictionStrategy.onNodeRemove(node);
                storage.remove(key);
                totalTtlExpirations.incrementAndGet();
                totalMisses.incrementAndGet();
                return null;
            }

            // Cache hit
            totalHits.incrementAndGet();
            evictionStrategy.onNodeAccess(node);
            return node.getValue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public CacheNode<K, V> getNode(K key) {
        lock.lock();
        try {
            CacheNode<K, V> node = storage.get(key);
            if (node == null) {
                totalMisses.incrementAndGet();
                return null;
            }

            if (node.isExpired()) {
                evictionStrategy.onNodeRemove(node);
                storage.remove(key);
                totalTtlExpirations.incrementAndGet();
                totalMisses.incrementAndGet();
                return null;
            }

            totalHits.incrementAndGet();
            evictionStrategy.onNodeAccess(node);
            return node;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(K key, V value, Long ttlSeconds) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        lock.lock();
        try {
            totalPutOperations.incrementAndGet();

            CacheNode<K, V> existingNode = storage.get(key);
            if (existingNode != null) {
                // Update existing node
                existingNode.updateValue(value, ttlSeconds);
                evictionStrategy.onNodeAccess(existingNode);
                return;
            }

            // Before evicting, see if any existing item has expired and can be evicted for TTL
            cleanExpiredNodesWithoutCountingAsMiss();

            // If still at or over capacity, trigger capacity eviction
            if (storage.size() >= capacity) {
                CacheNode<K, V> candidate = evictionStrategy.selectEvictionCandidate();
                if (candidate != null) {
                    evictionStrategy.onNodeRemove(candidate);
                    storage.remove(candidate.getKey());
                    totalEvictions.incrementAndGet();
                }
            }

            // Insert new node
            CacheNode<K, V> newNode = new CacheNode<>(key, value, ttlSeconds);
            storage.put(key, newNode);
            evictionStrategy.onNodeInsert(newNode);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean delete(K key) {
        lock.lock();
        try {
            CacheNode<K, V> node = storage.remove(key);
            if (node != null) {
                evictionStrategy.onNodeRemove(node);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            storage.clear();
            evictionStrategy.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return storage.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getCapacity() {
        lock.lock();
        try {
            return capacity;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setCapacity(int newCapacity) {
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        lock.lock();
        try {
            this.capacity = newCapacity;
            // If current size exceeds new capacity, evict excess elements
            while (storage.size() > this.capacity) {
                CacheNode<K, V> candidate = evictionStrategy.selectEvictionCandidate();
                if (candidate != null) {
                    evictionStrategy.onNodeRemove(candidate);
                    storage.remove(candidate.getKey());
                    totalEvictions.incrementAndGet();
                } else {
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public EvictionPolicy getEvictionPolicy() {
        lock.lock();
        try {
            return currentPolicy;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setEvictionPolicy(EvictionPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Policy cannot be null");
        }
        lock.lock();
        try {
            if (this.currentPolicy == policy) {
                return;
            }
            this.currentPolicy = policy;
            this.evictionStrategy = createStrategy(policy);
            // Rebuild strategy without losing entries or TTL
            this.evictionStrategy.rebuild(storage.values());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public CacheMetrics getMetrics() {
        lock.lock();
        try {
            long hits = totalHits.get();
            long misses = totalMisses.get();
            long totalGets = hits + misses;
            double hitRate = totalGets == 0 ? 0.0 : ((double) hits / totalGets) * 100.0;
            double missRate = totalGets == 0 ? 0.0 : ((double) misses / totalGets) * 100.0;

            // Round to 2 decimal places for clean UI presentation
            hitRate = Math.round(hitRate * 100.0) / 100.0;
            missRate = Math.round(missRate * 100.0) / 100.0;

            return new CacheMetrics(
                totalGets,
                hits,
                misses,
                hitRate,
                missRate,
                totalPutOperations.get(),
                totalEvictions.get(),
                totalTtlExpirations.get(),
                storage.size(),
                capacity,
                currentPolicy
            );
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<CacheEntryView<K, V>> getEntries() {
        lock.lock();
        try {
            List<CacheEntryView<K, V>> result = new ArrayList<>();
            for (CacheNode<K, V> node : storage.values()) {
                boolean expired = node.isExpired();
                long ttlRemaining = node.getTtlRemainingSeconds();
                result.add(new CacheEntryView<>(
                    node.getKey(),
                    node.getValue(),
                    ttlRemaining,
                    node.getAccessCount(),
                    expired ? "EXPIRED" : "ACTIVE"
                ));
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    private void cleanExpiredNodesWithoutCountingAsMiss() {
        List<CacheNode<K, V>> expiredNodes = new ArrayList<>();
        for (CacheNode<K, V> node : storage.values()) {
            if (node.isExpired()) {
                expiredNodes.add(node);
            }
        }
        for (CacheNode<K, V> node : expiredNodes) {
            evictionStrategy.onNodeRemove(node);
            storage.remove(node.getKey());
            totalTtlExpirations.incrementAndGet();
        }
    }
}
