import React from 'react';
import { Layers, Database, Cpu, RefreshCw, Trash2, Wifi, WifiOff } from 'lucide-react';

export default function Header({
  metrics,
  backendConnected,
  onRefresh,
  onClear,
  isRefreshing
}) {
  const policy = metrics?.currentEvictionPolicy || 'LRU';
  const capacity = metrics?.cacheCapacity || 0;
  const currentSize = metrics?.currentCacheSize || 0;

  return (
    <header className="dashboard-header">
      <div className="brand-wrapper">
        <div className="brand-icon">
          <Layers size={26} color="#ffffff" />
        </div>
        <div className="brand-titles">
          <h1>Custom Cache Library</h1>
          <p>Real-Time In-Memory Cache with LRU / LFU Eviction & Live Metrics Panel</p>
        </div>
      </div>

      <div className="header-badges">
        {/* System Status Indicator */}
        <span
          className="badge"
          style={{
            background: backendConnected ? 'rgba(16, 185, 129, 0.15)' : 'rgba(244, 63, 94, 0.15)',
            color: backendConnected ? '#34d399' : '#fb7185',
            borderColor: backendConnected ? 'rgba(16, 185, 129, 0.3)' : 'rgba(244, 63, 94, 0.3)'
          }}
        >
          {backendConnected ? <Wifi size={14} /> : <WifiOff size={14} />}
          Backend: <strong>{backendConnected ? 'Connected' : 'Disconnected'}</strong>
        </span>

        {/* Current Eviction Policy */}
        <span className="badge badge-policy" title="Active eviction policy">
          <Cpu size={14} /> Policy: <strong>{policy}</strong>
        </span>

        {/* Cache Capacity */}
        <span className="badge badge-capacity" title="Maximum capacity">
          <Database size={14} /> Capacity: <strong>{capacity}</strong>
        </span>

        {/* Current Cache Size */}
        <span className="badge badge-size" title="Current number of stored items">
          <span
            style={{
              width: 8,
              height: 8,
              borderRadius: '50%',
              background: currentSize >= capacity && capacity > 0 ? '#f43f5e' : '#10b981'
            }}
          />
          Size: <strong>{currentSize} / {capacity}</strong>
        </span>

        {/* Action Buttons */}
        <button
          onClick={onRefresh}
          className="btn btn-secondary"
          style={{ padding: '0.45rem 0.85rem', fontSize: '0.8rem' }}
          disabled={isRefreshing}
          title="Refresh metrics and entries"
        >
          <RefreshCw size={14} className={isRefreshing ? 'animate-spin' : ''} />
          {isRefreshing ? 'Syncing...' : 'Refresh Metrics'}
        </button>

        <button
          onClick={onClear}
          className="btn btn-danger"
          style={{ padding: '0.45rem 0.85rem', fontSize: '0.8rem' }}
          title="Clear all entries in cache"
        >
          <Trash2 size={14} />
          Clear Cache
        </button>
      </div>
    </header>
  );
}
