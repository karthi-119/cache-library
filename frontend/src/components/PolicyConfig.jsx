import React, { useState, useEffect } from 'react';
import { Settings, Play, Check, Trash2, RefreshCw } from 'lucide-react';
import { cacheApi } from '../services/api';

export default function PolicyConfig({
  metrics,
  onConfigChange,
  onSampleFinished,
  onClearCache,
  onRefreshMetrics
}) {
  const [selectedPolicy, setSelectedPolicy] = useState('LRU');
  const [capacity, setCapacity] = useState(5);
  const [isApplying, setIsApplying] = useState(false);
  const [isRunningSample, setIsRunningSample] = useState(false);
  const [sampleLog, setSampleLog] = useState(null);

  useEffect(() => {
    if (metrics?.currentEvictionPolicy) {
      setSelectedPolicy(metrics.currentEvictionPolicy);
    }
    if (metrics?.cacheCapacity) {
      setCapacity(metrics.cacheCapacity);
    }
  }, [metrics?.currentEvictionPolicy, metrics?.cacheCapacity]);

  const handleApply = async (newPolicy = selectedPolicy, newCap = capacity) => {
    setIsApplying(true);
    try {
      await cacheApi.updatePolicy(newPolicy, parseInt(newCap, 10));
      await onConfigChange();
    } catch (err) {
      alert(`Error updating policy: ${err.message}`);
    } finally {
      setIsApplying(false);
    }
  };

  const handlePolicyChange = async (e) => {
    const newPolicy = e.target.value;
    setSelectedPolicy(newPolicy);
    await handleApply(newPolicy, capacity);
  };

  const handleRunSample = async () => {
    setIsRunningSample(true);
    try {
      const result = await cacheApi.runSamplePattern();
      setSampleLog(result);
      await onSampleFinished();
    } catch (err) {
      alert(`Error running sample pattern: ${err.message}`);
    } finally {
      setIsRunningSample(false);
    }
  };

  return (
    <div className="glass-card">
      <h2 className="card-title">
        <Settings size={20} color="#06b6d4" /> Policy & Automation Panel
      </h2>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Active Eviction Policy</label>
          <select
            className="form-select"
            value={selectedPolicy}
            onChange={handlePolicyChange}
          >
            <option value="LRU">LRU (Least Recently Used)</option>
            <option value="LFU">LFU (Least Frequently Used)</option>
          </select>
        </div>

        <div className="form-group">
          <label className="form-label">Cache Capacity Limit</label>
          <input
            type="number"
            min="1"
            max="1000"
            className="form-input"
            value={capacity}
            onChange={(e) => setCapacity(e.target.value)}
          />
        </div>
      </div>

      <div className="btn-group">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => handleApply()}
          disabled={isApplying}
        >
          <Check size={16} /> {isApplying ? 'Updating...' : 'Set Capacity'}
        </button>

        <button
          type="button"
          className="btn btn-amber"
          onClick={handleRunSample}
          disabled={isRunningSample}
        >
          <Play size={16} /> {isRunningSample ? 'Executing Sequence...' : 'Run Sample Pattern'}
        </button>

        <button
          type="button"
          className="btn btn-secondary"
          onClick={onRefreshMetrics}
        >
          <RefreshCw size={16} /> Refresh Metrics
        </button>

        <button
          type="button"
          className="btn btn-danger"
          onClick={onClearCache}
        >
          <Trash2 size={16} /> Clear Cache
        </button>
      </div>

      {sampleLog && (
        <div className="sample-pattern-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <strong style={{ fontSize: '0.85rem', color: '#cbd5e1' }}>
              Sample Pattern Executed ({sampleLog.steps?.length} operations)
            </strong>
            <button
              onClick={() => setSampleLog(null)}
              style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', fontSize: '0.8rem' }}
            >
              Dismiss
            </button>
          </div>

          <div className="sample-step-list">
            {sampleLog.steps?.map((step) => (
              <div
                key={step.stepNumber}
                className={`sample-step-item ${step.action === 'GET' ? (step.isHit ? 'is-hit' : 'is-miss') : ''}`}
              >
                <div>
                  <span style={{ fontWeight: 700, marginRight: '0.5rem', color: '#94a3b8' }}>
                    #{step.stepNumber}
                  </span>
                  <span style={{ fontWeight: 600, color: step.action === 'PUT' ? '#a855f7' : '#38bdf8', marginRight: '0.5rem' }}>
                    {step.action} [{step.key}]
                  </span>
                  <span style={{ color: '#cbd5e1' }}>{step.result}</span>
                </div>
                <span
                  style={{
                    fontSize: '0.75rem',
                    fontWeight: 700,
                    color: step.isHit ? '#34d399' : (step.action === 'GET' ? '#fb7185' : '#94a3b8')
                  }}
                >
                  {step.action === 'GET' ? (step.isHit ? 'HIT' : 'MISS') : 'STORED'}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
