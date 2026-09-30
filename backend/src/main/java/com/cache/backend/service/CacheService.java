package com.cache.backend.service;

import com.cache.backend.cache.CustomCache;
import com.cache.backend.dto.CacheResponse;
import com.cache.backend.dto.SamplePatternResult;
import com.cache.backend.dto.SampleStepResult;
import com.cache.backend.model.CacheEntryView;
import com.cache.backend.model.CacheMetrics;
import com.cache.backend.model.CacheNode;
import com.cache.backend.model.EvictionPolicy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CacheService {

    private final CustomCache<String, Object> cache;

    public CacheService(CustomCache<String, Object> cache) {
        this.cache = cache;
    }

    public CacheResponse get(String key) {
        if (key == null || key.trim().isEmpty()) {
            return new CacheResponse(key, null, false, 0L, 0L, "Key cannot be empty");
        }

        CacheNode<String, Object> node = cache.getNode(key);
        if (node == null) {
            return new CacheResponse(key, null, false, 0L, 0L, "Cache Miss: Key '" + key + "' not found or expired");
        }

        return new CacheResponse(
            key,
            node.getValue(),
            true,
            node.getTtlRemainingSeconds(),
            node.getAccessCount(),
            "Cache Hit: Retrieved successfully"
        );
    }

    public void put(String key, Object value, Long ttlSeconds) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        if (ttlSeconds != null && ttlSeconds < 0) {
            throw new IllegalArgumentException("TTL cannot be negative");
        }
        cache.put(key, value, ttlSeconds);
    }

    public boolean delete(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        return cache.delete(key);
    }

    public void clear() {
        cache.clear();
    }

    public CacheMetrics getMetrics() {
        return cache.getMetrics();
    }

    public List<CacheEntryView<String, Object>> getEntries() {
        return cache.getEntries();
    }

    public EvictionPolicy getPolicy() {
        return cache.getEvictionPolicy();
    }

    public void updatePolicy(EvictionPolicy policy, Integer capacity) {
        if (policy != null) {
            cache.setEvictionPolicy(policy);
        }
        if (capacity != null && capacity > 0) {
            cache.setCapacity(capacity);
        }
    }

    public SamplePatternResult runSamplePattern() {
        List<SampleStepResult> steps = new ArrayList<>();
        int step = 1;

        // Sequence required by prompt:
        // PUT A, PUT B, PUT C, GET A, GET A, GET B, GET X, GET C, PUT D, GET A, GET D

        // Step 1: PUT A
        cache.put("A", "Alpha", 60L);
        steps.add(new SampleStepResult(step++, "PUT", "A", "Alpha", true, "Stored key 'A' with TTL 60s"));

        // Step 2: PUT B
        cache.put("B", "Beta", 60L);
        steps.add(new SampleStepResult(step++, "PUT", "B", "Beta", true, "Stored key 'B' with TTL 60s"));

        // Step 3: PUT C
        cache.put("C", "Gamma", 60L);
        steps.add(new SampleStepResult(step++, "PUT", "C", "Gamma", true, "Stored key 'C' with TTL 60s"));

        // Step 4: GET A
        Object val = cache.get("A");
        steps.add(new SampleStepResult(step++, "GET", "A", val, val != null, val != null ? "Hit: " + val : "Miss"));

        // Step 5: GET A
        val = cache.get("A");
        steps.add(new SampleStepResult(step++, "GET", "A", val, val != null, val != null ? "Hit: " + val : "Miss"));

        // Step 6: GET B
        val = cache.get("B");
        steps.add(new SampleStepResult(step++, "GET", "B", val, val != null, val != null ? "Hit: " + val : "Miss"));

        // Step 7: GET X (deliberate miss)
        val = cache.get("X");
        steps.add(new SampleStepResult(step++, "GET", "X", val, val != null, val != null ? "Hit: " + val : "Miss (Expected)"));

        // Step 8: GET C
        val = cache.get("C");
        steps.add(new SampleStepResult(step++, "GET", "C", val, val != null, val != null ? "Hit: " + val : "Miss"));

        // Step 9: PUT D
        cache.put("D", "Delta", 60L);
        steps.add(new SampleStepResult(step++, "PUT", "D", "Delta", true, "Stored key 'D' with TTL 60s"));

        // Step 10: GET A
        val = cache.get("A");
        steps.add(new SampleStepResult(step++, "GET", "A", val, val != null, val != null ? "Hit: " + val : "Miss"));

        // Step 11: GET D
        val = cache.get("D");
        steps.add(new SampleStepResult(step++, "GET", "D", val, val != null, val != null ? "Hit: " + val : "Miss"));

        return new SamplePatternResult(
            "Executed standard verification pattern (PUT A, B, C; GET A, A, B, X, C; PUT D; GET A, D)",
            steps,
            cache.getMetrics()
        );
    }
}
