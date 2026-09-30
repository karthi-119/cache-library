package com.cache.backend.controller;

import com.cache.backend.dto.CacheResponse;
import com.cache.backend.dto.PolicyRequest;
import com.cache.backend.dto.PutRequest;
import com.cache.backend.dto.SamplePatternResult;
import com.cache.backend.model.CacheEntryView;
import com.cache.backend.model.CacheMetrics;
import com.cache.backend.model.EvictionPolicy;
import com.cache.backend.service.CacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/{key}")
    public ResponseEntity<CacheResponse> get(@PathVariable String key) {
        CacheResponse response = cacheService.get(key);
        if (!response.hit()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{key}")
    public ResponseEntity<Map<String, Object>> put(@PathVariable String key, @RequestBody(required = false) PutRequest request) {
        if (key == null || key.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Key parameter cannot be empty");
            return ResponseEntity.badRequest().body(error);
        }

        Object value = request != null ? request.getValue() : null;
        Long ttlSeconds = request != null ? request.getTtlSeconds() : null;

        if (ttlSeconds != null && ttlSeconds < 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "TTL seconds cannot be negative");
            return ResponseEntity.badRequest().body(error);
        }

        cacheService.put(key, value, ttlSeconds);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("key", key);
        response.put("value", value);
        response.put("ttlSeconds", ttlSeconds);
        response.put("message", "Key '" + key + "' stored successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String key) {
        boolean deleted = cacheService.delete(key);
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("key", key);
        response.put("message", deleted ? "Key '" + key + "' removed from cache" : "Key '" + key + "' was not found");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clear() {
        cacheService.clear();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cache cleared completely");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    public ResponseEntity<CacheMetrics> getMetrics() {
        return ResponseEntity.ok(cacheService.getMetrics());
    }

    @GetMapping("/entries")
    public ResponseEntity<List<CacheEntryView<String, Object>>> getEntries() {
        return ResponseEntity.ok(cacheService.getEntries());
    }

    @GetMapping("/policy")
    public ResponseEntity<Map<String, Object>> getPolicy() {
        Map<String, Object> response = new HashMap<>();
        response.put("policy", cacheService.getPolicy());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/policy")
    public ResponseEntity<Map<String, Object>> updatePolicy(@RequestBody PolicyRequest request) {
        if (request == null || (request.getPolicy() == null && request.getCapacity() == null)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Please specify a valid policy (LRU or LFU) and/or positive capacity");
            return ResponseEntity.badRequest().body(error);
        }

        if (request.getCapacity() != null && request.getCapacity() <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Capacity must be greater than 0");
            return ResponseEntity.badRequest().body(error);
        }

        cacheService.updatePolicy(request.getPolicy(), request.getCapacity());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("policy", cacheService.getPolicy());
        response.put("metrics", cacheService.getMetrics());
        response.put("message", "Policy configuration updated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sample")
    public ResponseEntity<SamplePatternResult> runSamplePattern() {
        SamplePatternResult result = cacheService.runSamplePattern();
        return ResponseEntity.ok(result);
    }
}
