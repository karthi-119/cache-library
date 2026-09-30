package com.cache.backend.dto;

import com.cache.backend.model.CacheMetrics;
import java.util.List;

public record SamplePatternResult(
    String description,
    List<SampleStepResult> steps,
    CacheMetrics finalMetrics
) {}
