package com.cache.backend.cache;

import com.cache.backend.model.CacheNode;
import com.cache.backend.model.EvictionPolicy;
import java.util.Collection;

public interface EvictionStrategy<K, V> {
    void onNodeAccess(CacheNode<K, V> node);
    void onNodeInsert(CacheNode<K, V> node);
    void onNodeRemove(CacheNode<K, V> node);
    CacheNode<K, V> selectEvictionCandidate();
    void clear();
    void rebuild(Collection<CacheNode<K, V>> nodes);
    EvictionPolicy getPolicyType();
}
