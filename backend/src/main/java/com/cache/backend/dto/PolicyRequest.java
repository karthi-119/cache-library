package com.cache.backend.dto;

import com.cache.backend.model.EvictionPolicy;

public class PolicyRequest {
    private EvictionPolicy policy;
    private Integer capacity;

    public PolicyRequest() {}

    public PolicyRequest(EvictionPolicy policy, Integer capacity) {
        this.policy = policy;
        this.capacity = capacity;
    }

    public EvictionPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(EvictionPolicy policy) {
        this.policy = policy;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
