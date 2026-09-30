package com.cache.backend.dto;

public record SampleStepResult(
    int stepNumber,
    String action,
    String key,
    Object value,
    boolean isHit,
    String result
) {}
