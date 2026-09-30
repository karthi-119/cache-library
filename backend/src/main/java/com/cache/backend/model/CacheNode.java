package com.cache.backend.model;

public class CacheNode<K, V> {
    private final K key;
    private V value;
    private long accessCount;
    private long lastAccessedTimeNano;
    private long expirationTimeMs;

    // Pointers for doubly linked list usage in eviction strategies
    public CacheNode<K, V> prev;
    public CacheNode<K, V> next;

    public CacheNode(K key, V value, Long ttlSeconds) {
        this.key = key;
        this.value = value;
        this.accessCount = 1;
        this.lastAccessedTimeNano = System.nanoTime();
        setTtl(ttlSeconds);
    }

    public void setTtl(Long ttlSeconds) {
        if (ttlSeconds != null && ttlSeconds > 0) {
            this.expirationTimeMs = System.currentTimeMillis() + (ttlSeconds * 1000L);
        } else {
            this.expirationTimeMs = -1L;
        }
    }

    public boolean isExpired() {
        return expirationTimeMs != -1L && System.currentTimeMillis() >= expirationTimeMs;
    }

    public long getTtlRemainingSeconds() {
        if (expirationTimeMs == -1L) {
            return -1L;
        }
        long diff = expirationTimeMs - System.currentTimeMillis();
        return diff > 0 ? (diff + 999L) / 1000L : 0L;
    }

    public void recordAccess() {
        this.accessCount++;
        this.lastAccessedTimeNano = System.nanoTime();
    }

    public void updateValue(V value, Long ttlSeconds) {
        this.value = value;
        this.lastAccessedTimeNano = System.nanoTime();
        this.accessCount++;
        setTtl(ttlSeconds);
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(long accessCount) {
        this.accessCount = accessCount;
    }

    public long getLastAccessedTimeNano() {
        return lastAccessedTimeNano;
    }

    public void setLastAccessedTimeNano(long lastAccessedTimeNano) {
        this.lastAccessedTimeNano = lastAccessedTimeNano;
    }

    public long getExpirationTimeMs() {
        return expirationTimeMs;
    }
}
