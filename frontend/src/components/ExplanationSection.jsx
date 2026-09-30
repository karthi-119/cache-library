import React from 'react';
import { HelpCircle, Clock, Zap, Repeat } from 'lucide-react';

export default function ExplanationSection() {
  return (
    <div className="glass-card" style={{ marginTop: '1.5rem', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
      <h3 style={{ fontSize: '1.05rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem', color: '#f8fafc' }}>
        <HelpCircle size={18} color="#a855f7" /> How the Cache Eviction & Expiration Works
      </h3>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1rem' }}>
        {/* LRU */}
        <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '0.85rem 1rem', borderRadius: '8px', borderLeft: '3px solid #6366f1' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, color: '#a5b4fc', marginBottom: '0.25rem' }}>
            <Repeat size={15} /> LRU (Least Recently Used)
          </div>
          <p style={{ fontSize: '0.825rem', color: '#cbd5e1' }}>
            <strong>Removes least recently used entry.</strong> When capacity is reached, the entry that has not been accessed for the longest period is evicted.
          </p>
        </div>

        {/* LFU */}
        <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '0.85rem 1rem', borderRadius: '8px', borderLeft: '3px solid #06b6d4' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, color: '#67e8f9', marginBottom: '0.25rem' }}>
            <Zap size={15} /> LFU (Least Frequently Used)
          </div>
          <p style={{ fontSize: '0.825rem', color: '#cbd5e1' }}>
            <strong>Removes least frequently used entry.</strong> Tracks access frequency; evicts the item with lowest count. Ties are broken using LRU recency.
          </p>
        </div>

        {/* TTL */}
        <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '0.85rem 1rem', borderRadius: '8px', borderLeft: '3px solid #eab308' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, color: '#fde047', marginBottom: '0.25rem' }}>
            <Clock size={15} /> TTL (Time-To-Live)
          </div>
          <p style={{ fontSize: '0.825rem', color: '#cbd5e1' }}>
            <strong>Entry expires after its configured lifetime.</strong> Evaluated independently of eviction policy; expired entries register as a cache miss on retrieval.
          </p>
        </div>
      </div>
    </div>
  );
}
