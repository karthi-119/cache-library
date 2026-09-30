package com.cache.backend.cache;

import com.cache.backend.model.CacheEntryView;
import com.cache.backend.model.CacheMetrics;
import com.cache.backend.model.CacheNode;
import com.cache.backend.model.EvictionPolicy;
import java.util.List;

public interface CustomCache<K, V> {
    V get(K key);
    void put(K key, V value, Long ttlSeconds);
    boolean delete(K key);
    void clear();
    int size();
    int getCapacity();
    void setCapacity(int capacity);
    EvictionPolicy getEvictionPolicy();
    void setEvictionPolicy(EvictionPolicy policy);
    CacheMetrics getMetrics();
    List<CacheEntryView<K, V>> getEntries();
    CacheNode<K, V> getNode(K key);
}
