import React from 'react';
import { TrendingUp, PieChart, Activity } from 'lucide-react';

export default function Visualizations({ metrics }) {
  const hits = metrics?.hits ?? 0;
  const misses = metrics?.misses ?? 0;
  const totalGets = metrics?.totalGetRequests ?? 0;
  const hitRate = metrics?.hitRate ?? 0;
  const missRate = metrics?.missRate ?? 0;

  const size = metrics?.currentCacheSize ?? 0;
  const capacity = metrics?.cacheCapacity ?? 1;
  const capacityPercent = capacity > 0 ? Math.min(100, Math.round((size / capacity) * 100)) : 0;

  return (
    <div className="glass-card" style={{ marginBottom: '2rem' }}>
      <h2 className="card-title">
        <TrendingUp size={20} color="#10b981" /> Live Visual Performance Charts
      </h2>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
        {/* Chart 1: Hits vs Misses Distribution Chart */}
        <div className="bar-wrapper" style={{ background: 'rgba(15, 23, 42, 0.6)', padding: '1rem', borderRadius: '10px' }}>
          <div className="bar-header">
            <span style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#f8fafc' }}>
              <PieChart size={15} color="#38bdf8" /> Hits vs Misses Chart
            </span>
            <span>
              <strong style={{ color: '#34d399' }}>{hits} Hits</strong> / <strong style={{ color: '#fb7185' }}>{misses} Misses</strong>
            </span>
          </div>

          <div className="progress-track" style={{ height: '16px', marginTop: '0.5rem' }}>
            <div
              className="progress-fill-hits"
              style={{ width: `${hitRate}%` }}
              title={`Hits: ${hits} (${hitRate}%)`}
            />
            <div
              className="progress-fill-misses"
              style={{ width: `${missRate}%` }}
              title={`Misses: ${misses} (${missRate}%)`}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: '#94a3b8', marginTop: '0.5rem' }}>
            <span style={{ color: '#34d399', fontWeight: 600 }}>● Hits ({hitRate}%)</span>
            <span style={{ color: '#fb7185', fontWeight: 600 }}>● Misses ({missRate}%)</span>
          </div>
        </div>

        {/* Chart 2: Hit Rate Efficiency Meter */}
        <div className="bar-wrapper" style={{ background: 'rgba(15, 23, 42, 0.6)', padding: '1rem', borderRadius: '10px' }}>
          <div className="bar-header">
            <span style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#f8fafc' }}>
              <Activity size={15} color="#10b981" /> Hit Rate Efficiency Meter
            </span>
            <strong style={{ fontSize: '1rem', color: hitRate >= 70 ? '#34d399' : (hitRate >= 40 ? '#fbbf24' : '#fb7185') }}>
              {hitRate}%
            </strong>
          </div>

          <div className="progress-track" style={{ height: '16px', marginTop: '0.5rem' }}>
            <div
              style={{
                width: `${hitRate}%`,
                background: hitRate >= 70
                  ? 'linear-gradient(90deg, #10b981, #34d399)'
                  : hitRate >= 40
                  ? 'linear-gradient(90deg, #f59e0b, #fbbf24)'
                  : 'linear-gradient(90deg, #ef4444, #f43f5e)',
                transition: 'width 0.4s ease'
              }}
              title={`Hit Rate: ${hitRate}%`}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: '#94a3b8', marginTop: '0.5rem' }}>
            <span>Target: &gt; 75%</span>
            <span>
              Status: {totalGets === 0 ? 'No requests yet' : (hitRate >= 70 ? 'Optimal' : (hitRate >= 40 ? 'Moderate' : 'Needs Warm-up'))}
            </span>
          </div>
        </div>

        {/* Chart 3: Cache Capacity Allocation */}
        <div className="bar-wrapper" style={{ background: 'rgba(15, 23, 42, 0.6)', padding: '1rem', borderRadius: '10px' }}>
          <div className="bar-header">
            <span style={{ fontWeight: 600, color: '#f8fafc' }}>Capacity Occupancy</span>
            <strong style={{ color: capacityPercent >= 100 ? '#f43f5e' : '#38bdf8' }}>
              {size} of {capacity} slots ({capacityPercent}%)
            </strong>
          </div>

          <div className="progress-track" style={{ height: '16px', marginTop: '0.5rem' }}>
            <div
              className="progress-fill-capacity"
              style={{
                width: `${capacityPercent}%`,
                background: capacityPercent >= 100
                  ? 'linear-gradient(90deg, #f59e0b, #ef4444)'
                  : 'linear-gradient(90deg, #6366f1, #38bdf8)'
              }}
              title={`Capacity: ${size}/${capacity}`}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: '#94a3b8', marginTop: '0.5rem' }}>
            <span>Free Slots: {Math.max(0, capacity - size)}</span>
            <span>Eviction State: {size >= capacity ? 'ACTIVE' : 'IDLE'}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
