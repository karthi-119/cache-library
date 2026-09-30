package com.cache.backend.model;

public record CacheMetrics(
    long totalGetRequests,
    long hits,
    long misses,
    double hitRate,
    double missRate,
    long totalPutOperations,
    long totalEvictions,
    long totalTtlExpirations,
    int currentCacheSize,
    int cacheCapacity,
    EvictionPolicy currentEvictionPolicy
) {}
