import React, { useState, useEffect, useCallback } from 'react';
import Header from './components/Header';
import MetricsCards from './components/MetricsCards';
import CacheControls from './components/CacheControls';
import PolicyConfig from './components/PolicyConfig';
import Visualizations from './components/Visualizations';
import CacheEntriesTable from './components/CacheEntriesTable';
import ExplanationSection from './components/ExplanationSection';
import { cacheApi } from './services/api';
import { AlertCircle } from 'lucide-react';

export default function App() {
  const [metrics, setMetrics] = useState(null);
  const [entries, setEntries] = useState([]);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [selectedKey, setSelectedKey] = useState('');
  const [backendConnected, setBackendConnected] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const fetchCacheData = useCallback(async (isManual = false) => {
    if (isManual) setIsRefreshing(true);
    try {
      const [metricsData, entriesData] = await Promise.all([
        cacheApi.getMetrics(),
        cacheApi.getEntries()
      ]);
      setMetrics(metricsData);
      setEntries(entriesData);
      setBackendConnected(true);
      setErrorMessage(null);
    } catch (err) {
      console.warn('Backend connection error:', err.message);
      setBackendConnected(false);
      setErrorMessage('Backend not reachable on http://localhost:8080. Ensure the Spring Boot backend is running.');
    } finally {
      if (isManual) {
        setTimeout(() => setIsRefreshing(false), 250);
      }
    }
  }, []);

  // Initial load
  useEffect(() => {
    fetchCacheData(true);
  }, [fetchCacheData]);

  // Periodic polling every 2 seconds for live TTL and metrics
  useEffect(() => {
    const interval = setInterval(() => {
      fetchCacheData(false);
    }, 2000);
    return () => clearInterval(interval);
  }, [fetchCacheData]);

  const handleClearCache = async () => {
    if (!window.confirm('Are you sure you want to clear all cache entries?')) return;
    try {
      await cacheApi.clear();
      await fetchCacheData(true);
    } catch (err) {
      alert(`Error clearing cache: ${err.message}`);
    }
  };

  return (
    <div className="dashboard-container">
      {/* Top Header */}
      <Header
        metrics={metrics}
        backendConnected={backendConnected}
        onRefresh={() => fetchCacheData(true)}
        onClear={handleClearCache}
        isRefreshing={isRefreshing}
      />

      {/* Disconnection Warning */}
      {!backendConnected && errorMessage && (
        <div className="feedback-box feedback-error" style={{ marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <AlertCircle size={18} />
            <span>{errorMessage}</span>
          </div>
          <button
            className="btn btn-secondary"
            style={{ padding: '0.2rem 0.6rem', fontSize: '0.75rem' }}
            onClick={() => fetchCacheData(true)}
          >
            Retry Connection
          </button>
        </div>
      )}

      {/* Metric Cards */}
      <MetricsCards metrics={metrics} />

      {/* Visual Charts: Hits vs Misses and Hit Rate */}
      <Visualizations metrics={metrics} />

      {/* Cache Controls & Policy Settings Grid */}
      <div className="main-grid">
        <CacheControls
          onActionSuccess={() => fetchCacheData(false)}
          selectedKey={selectedKey}
          setSelectedKey={setSelectedKey}
        />

        <PolicyConfig
          metrics={metrics}
          onConfigChange={() => fetchCacheData(false)}
          onSampleFinished={() => fetchCacheData(false)}
          onClearCache={handleClearCache}
          onRefreshMetrics={() => fetchCacheData(true)}
        />
      </div>

      {/* Cache Entries Table */}
      <CacheEntriesTable
        entries={entries}
        onEntryAction={() => fetchCacheData(false)}
        onSelectKey={(k) => setSelectedKey(k)}
      />

      {/* Educational Explanation Section */}
      <ExplanationSection />
    </div>
  );
}
