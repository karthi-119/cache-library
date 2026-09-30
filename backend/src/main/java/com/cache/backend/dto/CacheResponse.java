package com.cache.backend.dto;

public record CacheResponse(
    String key,
    Object value,
    boolean hit,
    Long ttlRemainingSeconds,
    Long accessCount,
    String message
) {}
