package com.cache.backend.dto;

public class PutRequest {
    private Object value;
    private Long ttlSeconds;

    public PutRequest() {}

    public PutRequest(Object value, Long ttlSeconds) {
        this.value = value;
        this.ttlSeconds = ttlSeconds;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
