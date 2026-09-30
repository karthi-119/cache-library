import React from 'react';
import { Table, Clock, Zap, CornerDownRight, Trash2 } from 'lucide-react';
import { cacheApi } from '../services/api';

export default function CacheEntriesTable({ entries, onEntryAction, onSelectKey }) {
  const handleDeleteRow = async (e, key) => {
    e.stopPropagation();
    try {
      await cacheApi.delete(key);
      await onEntryAction();
    } catch (err) {
      alert(`Failed to delete key: ${err.message}`);
    }
  };

  const handleSelectRow = (key) => {
    if (onSelectKey) {
      onSelectKey(key);
    }
  };

  return (
    <div className="glass-card" style={{ marginBottom: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
        <h2 style={{ fontSize: '1.15rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <Table size={20} color="#38bdf8" /> Cache Entries Table ({entries?.length || 0})
        </h2>
        <span style={{ fontSize: '0.8rem', color: '#94a3b8' }}>
          Tip: Click any row to load the key into the operation panel
        </span>
      </div>

      <div className="table-container">
        <table className="cache-table">
          <thead>
            <tr>
              <th>Key</th>
              <th>Value</th>
              <th>TTL Remaining</th>
              <th>Frequency</th>
              <th>Status</th>
              <th style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {!entries || entries.length === 0 ? (
              <tr>
                <td colSpan="6" className="table-empty">
                  No items currently stored in cache. Use the Cache Operation Panel or click "Run Sample Pattern" to add entries.
                </td>
              </tr>
            ) : (
              entries.map((entry) => {
                const isExpired = entry.status === 'EXPIRED';
                const ttl = entry.ttlRemainingSeconds;
                let ttlDisplay;

                if (ttl === -1 || ttl === null) {
                  ttlDisplay = <span style={{ color: '#94a3b8' }}>None (∞)</span>;
                } else if (ttl === 0 || isExpired) {
                  ttlDisplay = <span style={{ color: '#fb7185', fontWeight: 600 }}>Expired (0s)</span>;
                } else {
                  ttlDisplay = (
                    <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', color: '#fbbf24', fontWeight: 600 }}>
                      <Clock size={12} /> {ttl}s
                    </span>
                  );
                }

                return (
                  <tr
                    key={entry.key}
                    style={{ cursor: 'pointer' }}
                    onClick={() => handleSelectRow(entry.key)}
                  >
                    <td>
                      <span className="key-code">{entry.key}</span>
                    </td>
                    <td>
                      <span style={{ color: '#e2e8f0', maxWidth: '280px', display: 'inline-block', wordBreak: 'break-word' }}>
                        {typeof entry.value === 'object' ? JSON.stringify(entry.value) : String(entry.value)}
                      </span>
                    </td>
                    <td>{ttlDisplay}</td>
                    <td>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', fontWeight: 600, color: '#a5b4fc' }}>
                        <Zap size={13} color="#818cf8" /> {entry.frequency}
                      </span>
                    </td>
                    <td>
                      <span className={isExpired ? 'pill-expired' : 'pill-active'}>
                        {entry.status}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button
                        className="btn btn-secondary"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', marginRight: '0.4rem' }}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleSelectRow(entry.key);
                        }}
                        title="Load into controls"
                      >
                        <CornerDownRight size={12} /> Select
                      </button>
                      <button
                        className="btn btn-danger"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                        onClick={(e) => handleDeleteRow(e, entry.key)}
                        title="Delete key"
                      >
                        <Trash2 size={12} />
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
