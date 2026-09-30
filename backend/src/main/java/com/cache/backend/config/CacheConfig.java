package com.cache.backend.config;

import com.cache.backend.cache.CustomCache;
import com.cache.backend.cache.ThreadSafeCustomCache;
import com.cache.backend.model.EvictionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CustomCache<String, Object> customCache() {
        // Default capacity of 5 and LRU policy for easy demonstration
        return new ThreadSafeCustomCache<>(5, EvictionPolicy.LRU);
    }
}
