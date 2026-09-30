import React, { useState, useEffect } from 'react';
import { Sliders, PlusCircle, Search, Trash2, CheckCircle, AlertTriangle, Info } from 'lucide-react';
import { cacheApi } from '../services/api';

export default function CacheControls({ onActionSuccess, selectedKey, setSelectedKey }) {
  const [key, setKey] = useState('');
  const [value, setValue] = useState('');
  const [ttl, setTtl] = useState('');
  const [feedback, setFeedback] = useState(null);
  const [loading, setLoading] = useState(false);

  // Sync external key selection from table if user clicks row
  useEffect(() => {
    if (selectedKey) {
      setKey(selectedKey);
      if (setSelectedKey) setSelectedKey('');
    }
  }, [selectedKey, setSelectedKey]);

  const handlePut = async (e) => {
    if (e) e.preventDefault();
    if (!key.trim()) {
      setFeedback({ type: 'error', message: 'Please provide a Key.' });
      return;
    }
    setLoading(true);
    try {
      const ttlNum = ttl.trim() ? parseInt(ttl.trim(), 10) : null;
      const res = await cacheApi.put(key.trim(), value, ttlNum);
      if (res.ok) {
        setFeedback({
          type: 'success',
          message: `PUT SUCCESS: Key '${key.trim()}' saved ${ttlNum ? `with TTL ${ttlNum}s` : '(No expiration)'}`
        });
        await onActionSuccess();
      } else {
        setFeedback({ type: 'error', message: res.data?.message || 'Failed to put entry' });
      }
    } catch (err) {
      setFeedback({ type: 'error', message: `PUT Failed: ${err.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleGet = async () => {
    if (!key.trim()) {
      setFeedback({ type: 'error', message: 'Please provide a Key to retrieve.' });
      return;
    }
    setLoading(true);
    try {
      const res = await cacheApi.get(key.trim());
      if (res.ok && res.data.hit) {
        const remaining = res.data.ttlRemainingSeconds === -1 ? 'None (∞)' : `${res.data.ttlRemainingSeconds}s`;
        setFeedback({
          type: 'success',
          message: `CACHE HIT! Key: '${key.trim()}' | Value: "${res.data.value}" | Frequency: ${res.data.accessCount} | TTL Left: ${remaining}`
        });
        if (res.data.value !== undefined && res.data.value !== null) {
          setValue(typeof res.data.value === 'object' ? JSON.stringify(res.data.value) : String(res.data.value));
        }
      } else {
        setFeedback({
          type: 'error',
          message: `CACHE MISS! Key '${key.trim()}' not found or expired.`
        });
      }
      await onActionSuccess();
    } catch (err) {
      setFeedback({ type: 'error', message: `GET Failed: ${err.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!key.trim()) {
      setFeedback({ type: 'error', message: 'Please provide a Key to delete.' });
      return;
    }
    setLoading(true);
    try {
      const res = await cacheApi.delete(key.trim());
      if (res.ok && res.data.success) {
        setFeedback({ type: 'info', message: `DELETE: Removed key '${key.trim()}' from cache.` });
      } else {
        setFeedback({ type: 'error', message: `DELETE: Key '${key.trim()}' was not in cache.` });
      }
      await onActionSuccess();
    } catch (err) {
      setFeedback({ type: 'error', message: `DELETE Failed: ${err.message}` });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="glass-card">
      <h2 className="card-title">
        <Sliders size={20} color="#6366f1" /> Cache Operation Panel
      </h2>

      <form onSubmit={handlePut}>
        <div className="form-group">
          <label className="form-label">Key (String)</label>
          <input
            type="text"
            className="form-input"
            placeholder="e.g. user:101 or product_A"
            value={key}
            onChange={(e) => setKey(e.target.value)}
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Value (Object / Text)</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Apple or { active: true }"
              value={value}
              onChange={(e) => setValue(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="form-label">TTL in Seconds (Optional)</label>
            <input
              type="number"
              min="1"
              className="form-input"
              placeholder="e.g. 30"
              value={ttl}
              onChange={(e) => setTtl(e.target.value)}
            />
          </div>
        </div>

        <div className="btn-group">
          <button type="submit" className="btn btn-primary" disabled={loading}>
            <PlusCircle size={16} /> PUT
          </button>

          <button type="button" className="btn btn-secondary" onClick={handleGet} disabled={loading}>
            <Search size={16} /> GET
          </button>

          <button type="button" className="btn btn-danger" onClick={handleDelete} disabled={loading}>
            <Trash2 size={16} /> DELETE
          </button>
        </div>
      </form>

      {feedback && (
        <div className={`feedback-box feedback-${feedback.type}`}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            {feedback.type === 'success' && <CheckCircle size={16} />}
            {feedback.type === 'error' && <AlertTriangle size={16} />}
            {feedback.type === 'info' && <Info size={16} />}
            <span>{feedback.message}</span>
          </div>
          <button
            onClick={() => setFeedback(null)}
            style={{ background: 'transparent', border: 'none', color: 'inherit', cursor: 'pointer', fontSize: '1rem' }}
          >
            ×
          </button>
        </div>
      )}
    </div>
  );
}
