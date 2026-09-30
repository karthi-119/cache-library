const API_BASE_URL = 'http://localhost:8080/api/cache';

export const cacheApi = {
  async get(key) {
    const res = await fetch(`${API_BASE_URL}/${encodeURIComponent(key)}`);
    const data = await res.json();
    return { ok: res.ok, status: res.status, data };
  },

  async put(key, value, ttlSeconds) {
    const body = {
      value,
      ttlSeconds: ttlSeconds ? parseInt(ttlSeconds, 10) : null
    };
    const res = await fetch(`${API_BASE_URL}/${encodeURIComponent(key)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const data = await res.json();
    return { ok: res.ok, status: res.status, data };
  },

  async delete(key) {
    const res = await fetch(`${API_BASE_URL}/${encodeURIComponent(key)}`, {
      method: 'DELETE'
    });
    const data = await res.json();
    return { ok: res.ok, status: res.status, data };
  },

  async clear() {
    const res = await fetch(API_BASE_URL, {
      method: 'DELETE'
    });
    const data = await res.json();
    return { ok: res.ok, status: res.status, data };
  },

  async getMetrics() {
    const res = await fetch(`${API_BASE_URL}/metrics`);
    if (!res.ok) throw new Error('Failed to fetch cache metrics');
    return await res.json();
  },

  async getEntries() {
    const res = await fetch(`${API_BASE_URL}/entries`);
    if (!res.ok) throw new Error('Failed to fetch cache entries');
    return await res.json();
  },

  async getPolicy() {
    const res = await fetch(`${API_BASE_URL}/policy`);
    if (!res.ok) throw new Error('Failed to fetch current policy');
    return await res.json();
  },

  async updatePolicy(policy, capacity) {
    const body = {};
    if (policy) body.policy = policy;
    if (capacity) body.capacity = parseInt(capacity, 10);

    const res = await fetch(`${API_BASE_URL}/policy`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const data = await res.json();
    return { ok: res.ok, status: res.status, data };
  },

  async runSamplePattern() {
    const res = await fetch(`${API_BASE_URL}/sample`, {
      method: 'POST'
    });
    if (!res.ok) throw new Error('Failed to run sample pattern');
    return await res.json();
  }
};
