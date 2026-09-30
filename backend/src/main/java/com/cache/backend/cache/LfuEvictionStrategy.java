package com.cache.backend.cache;

import com.cache.backend.model.CacheNode;
import com.cache.backend.model.EvictionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LfuEvictionStrategy<K, V> implements EvictionStrategy<K, V> {

    private final Map<Long, DoublyLinkedList<K, V>> freqMap = new HashMap<>();
    private long minFrequency = 1L;

    private static class DoublyLinkedList<K, V> {
        private final CacheNode<K, V> head;
        private final CacheNode<K, V> tail;
        private int size;

        DoublyLinkedList() {
            head = new CacheNode<>(null, null, null);
            tail = new CacheNode<>(null, null, null);
            head.next = tail;
            tail.prev = head;
            size = 0;
        }

        void addToHead(CacheNode<K, V> node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
            size++;
        }

        void remove(CacheNode<K, V> node) {
            if (node.prev != null && node.next != null) {
                node.prev.next = node.next;
                node.next.prev = node.prev;
                node.prev = null;
                node.next = null;
                size--;
            }
        }

        CacheNode<K, V> getTailNode() {
            return size == 0 ? null : tail.prev;
        }

        boolean isEmpty() {
            return size == 0;
        }
    }

    @Override
    public void onNodeInsert(CacheNode<K, V> node) {
        long freq = node.getAccessCount() <= 0 ? 1L : node.getAccessCount();
        node.setAccessCount(freq);
        freqMap.computeIfAbsent(freq, k -> new DoublyLinkedList<>()).addToHead(node);
        if (freq < minFrequency || freqMap.size() == 1) {
            minFrequency = freq;
        }
    }

    @Override
    public void onNodeAccess(CacheNode<K, V> node) {
        long currentFreq = node.getAccessCount();
        DoublyLinkedList<K, V> list = freqMap.get(currentFreq);
        if (list != null) {
            list.remove(node);
            if (list.isEmpty()) {
                freqMap.remove(currentFreq);
                if (minFrequency == currentFreq) {
                    minFrequency = currentFreq + 1;
                }
            }
        }
        long nextFreq = currentFreq + 1;
        node.setAccessCount(nextFreq);
        freqMap.computeIfAbsent(nextFreq, k -> new DoublyLinkedList<>()).addToHead(node);
    }

    @Override
    public void onNodeRemove(CacheNode<K, V> node) {
        long freq = node.getAccessCount();
        DoublyLinkedList<K, V> list = freqMap.get(freq);
        if (list != null) {
            list.remove(node);
            if (list.isEmpty()) {
                freqMap.remove(freq);
                if (minFrequency == freq) {
                    recalculateMinFrequency();
                }
            }
        }
    }

    @Override
    public CacheNode<K, V> selectEvictionCandidate() {
        if (freqMap.isEmpty()) {
            return null;
        }
        DoublyLinkedList<K, V> minList = freqMap.get(minFrequency);
        if (minList == null || minList.isEmpty()) {
            recalculateMinFrequency();
            minList = freqMap.get(minFrequency);
        }
        return minList != null ? minList.getTailNode() : null;
    }

    @Override
    public void clear() {
        freqMap.clear();
        minFrequency = 1L;
    }

    @Override
    public void rebuild(Collection<CacheNode<K, V>> nodes) {
        clear();
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        // Group by frequency and sort by lastAccessedTimeNano ascending
        Map<Long, List<CacheNode<K, V>>> grouped = new HashMap<>();
        for (CacheNode<K, V> node : nodes) {
            grouped.computeIfAbsent(node.getAccessCount(), k -> new ArrayList<>()).add(node);
        }

        long min = Long.MAX_VALUE;
        for (Map.Entry<Long, List<CacheNode<K, V>>> entry : grouped.entrySet()) {
            long freq = entry.getKey();
            if (freq < min) {
                min = freq;
            }
            List<CacheNode<K, V>> list = entry.getValue();
            list.sort(Comparator.comparingLong(CacheNode::getLastAccessedTimeNano));
            DoublyLinkedList<K, V> bucket = freqMap.computeIfAbsent(freq, k -> new DoublyLinkedList<>());
            for (CacheNode<K, V> node : list) {
                node.prev = null;
                node.next = null;
                bucket.addToHead(node);
            }
        }
        minFrequency = min == Long.MAX_VALUE ? 1L : min;
    }

    @Override
    public EvictionPolicy getPolicyType() {
        return EvictionPolicy.LFU;
    }

    private void recalculateMinFrequency() {
        if (freqMap.isEmpty()) {
            minFrequency = 1L;
            return;
        }
        long min = Long.MAX_VALUE;
        for (Long freq : freqMap.keySet()) {
            if (freq < min) {
                min = freq;
            }
        }
        minFrequency = min;
    }
}
