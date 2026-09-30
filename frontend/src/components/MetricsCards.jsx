import React from 'react';
import { Activity, CheckCircle2, XCircle, Percent, BarChart2, Trash2, Clock, Database } from 'lucide-react';

export default function MetricsCards({ metrics }) {
  const cards = [
    {
      label: 'Total GET Requests',
      value: metrics?.totalGetRequests ?? 0,
      sub: `${metrics?.hits ?? 0} hits · ${metrics?.misses ?? 0} misses`,
      icon: Activity,
      accent: '#6366f1'
    },
    {
      label: 'Cache Hits',
      value: metrics?.hits ?? 0,
      sub: 'Keys resolved successfully',
      icon: CheckCircle2,
      accent: '#10b981'
    },
    {
      label: 'Cache Misses',
      value: metrics?.misses ?? 0,
      sub: 'Missing or expired keys',
      icon: XCircle,
      accent: '#f43f5e'
    },
    {
      label: 'Hit Rate',
      value: `${metrics?.hitRate ?? 0}%`,
      sub: 'Hit efficiency percentage',
      icon: Percent,
      accent: '#38bdf8'
    },
    {
      label: 'Miss Rate',
      value: `${metrics?.missRate ?? 0}%`,
      sub: 'Miss percentage ratio',
      icon: BarChart2,
      accent: '#fb923c'
    },
    {
      label: 'Total Evictions',
      value: metrics?.totalEvictions ?? 0,
      sub: 'Capacity limit evictions',
      icon: Trash2,
      accent: '#ec4899'
    },
    {
      label: 'TTL Expirations',
      value: metrics?.totalTtlExpirations ?? 0,
      sub: 'Time-to-live expired items',
      icon: Clock,
      accent: '#eab308'
    },
    {
      label: 'Cache Storage Size',
      value: `${metrics?.currentCacheSize ?? 0} / ${metrics?.cacheCapacity ?? 0}`,
      sub: `${metrics?.currentEvictionPolicy ?? 'LRU'} Active Policy`,
      icon: Database,
      accent: '#a855f7'
    }
  ];

  return (
    <div className="metrics-grid">
      {cards.map((card, idx) => {
        const Icon = card.icon;
        return (
          <div
            key={idx}
            className="glass-card metric-card"
            style={{ '--card-accent': card.accent }}
          >
            <div className="metric-top">
              <span className="metric-label">{card.label}</span>
              <div className="metric-icon" style={{ color: card.accent }}>
                <Icon size={16} />
              </div>
            </div>
            <div className="metric-value">{card.value}</div>
            <div className="metric-sub">{card.sub}</div>
          </div>
        );
      })}
    </div>
  );
}
