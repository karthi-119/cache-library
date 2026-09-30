package com.cache.backend.model;

public record CacheEntryView<K, V>(
    K key,
    V value,
    Long ttlRemainingSeconds,
    long frequency,
    String status
) {}
