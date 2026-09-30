package com.cache.backend.cache;

import com.cache.backend.model.CacheNode;
import com.cache.backend.model.EvictionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class LruEvictionStrategy<K, V> implements EvictionStrategy<K, V> {

    private final CacheNode<K, V> head;
    private final CacheNode<K, V> tail;

    public LruEvictionStrategy() {
        head = new CacheNode<>(null, null, null);
        tail = new CacheNode<>(null, null, null);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void onNodeInsert(CacheNode<K, V> node) {
        addNodeToHead(node);
    }

    @Override
    public void onNodeAccess(CacheNode<K, V> node) {
        unlink(node);
        addNodeToHead(node);
    }

    @Override
    public void onNodeRemove(CacheNode<K, V> node) {
        unlink(node);
    }

    @Override
    public CacheNode<K, V> selectEvictionCandidate() {
        if (tail.prev == head) {
            return null;
        }
        return tail.prev;
    }

    @Override
    public void clear() {
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void rebuild(Collection<CacheNode<K, V>> nodes) {
        clear();
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        // Sort from oldest access to newest access
        List<CacheNode<K, V>> sorted = new ArrayList<>(nodes);
        sorted.sort(Comparator.comparingLong(CacheNode::getLastAccessedTimeNano));
        // Inserting from oldest to newest puts newest at head and oldest at tail
        for (CacheNode<K, V> node : sorted) {
            node.prev = null;
            node.next = null;
            addNodeToHead(node);
        }
    }

    @Override
    public EvictionPolicy getPolicyType() {
        return EvictionPolicy.LRU;
    }

    private void addNodeToHead(CacheNode<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void unlink(CacheNode<K, V> node) {
        if (node.prev != null && node.next != null) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            node.prev = null;
            node.next = null;
        }
    }
}
