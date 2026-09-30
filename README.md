# Custom Cache Library with Live Metrics Panel

A high-performance, full-stack in-memory caching system featuring pluggable **LRU (Least Recently Used)** and **LFU (Least Frequently Used)** eviction policies, independent per-entry **TTL (Time-To-Live)** expiration, guaranteed **thread safety**, and a modern **real-time React metrics dashboard**.

---

## 🚀 Key Features

- **Generic In-Memory Storage**: Key-value cache supporting String keys and arbitrary values.
- **LRU Eviction**: Built using an internal Doubly Linked List with $O(1)$ recency updates.
- **LFU Eviction**: Built using frequency buckets with a deterministic LRU tie-breaker for identical frequencies.
- **Dynamic Policy Switching**: Switch between LRU and LFU at runtime without losing active entries or TTL timers.
- **Per-Entry TTL Expiration**: Individual items can have optional TTLs (in seconds), with lazy expiration on access and distinct tracking from capacity evictions.
- **Thread Safety**: Concurrent access guarded with atomic synchronization and lock protection over compound operations.
- **Comprehensive Real-Time Metrics**: Total GETs, Hits, Misses, Hit Rate %, Miss Rate %, PUTs, Capacity Evictions, and TTL Expirations.
- **Interactive React Dashboard**: Dark-mode glassmorphism interface featuring metric cards, cache control panel, live capacity gauges, interactive entries table, and a one-click sample access pattern runner.

---

## 🏛️ System Architecture

```
codeathon/
├── backend/
│   ├── src/main/java/com/cache/backend/
│   │   ├── BackendApplication.java       # Spring Boot main entrypoint
│   │   ├── cache/
│   │   │   ├── CustomCache.java          # Generic Cache Interface
│   │   │   ├── ThreadSafeCustomCache.java# Thread-safe implementation
│   │   │   ├── EvictionStrategy.java     # Pluggable eviction policy interface
│   │   │   ├── LruEvictionStrategy.java  # Custom Doubly Linked List LRU
│   │   │   └── LfuEvictionStrategy.java  # Frequency bucket LFU with LRU tie-break
│   │   ├── config/
│   │   │   ├── CacheConfig.java          # Spring Bean setup (capacity, policy)
│   │   │   └── CorsConfig.java           # Cross-Origin Resource Sharing config
│   │   ├── controller/
│   │   │   └── CacheController.java      # REST API Endpoints
│   │   ├── dto/
│   │   │   ├── CacheResponse.java        # Structured GET response
│   │   │   ├── PolicyRequest.java        # Policy switch & capacity DTO
│   │   │   ├── PutRequest.java           # Cache insertion DTO
│   │   │   ├── SamplePatternResult.java  # Sequence demo results
│   │   │   └── SampleStepResult.java     # Single step log
│   │   ├── model/
│   │   │   ├── CacheEntryView.java       # DTO representing table row
│   │   │   ├── CacheMetrics.java         # Cache performance metrics
│   │   │   ├── CacheNode.java            # Doubly-linked node with TTL & frequency
│   │   │   └── EvictionPolicy.java       # Enum: LRU, LFU
│   │   └── service/
│   │       └── CacheService.java         # Business logic & demo pattern runner
│   ├── src/test/java/com/cache/backend/
│   │   └── CustomCacheTest.java          # Concurrency, LRU, LFU, TTL unit tests
│   └── pom.xml
└── frontend/
    ├── src/
    │   ├── components/
    │   │   ├── CacheControls.jsx         # Key/Value/TTL inputs & actions
    │   │   ├── CacheEntriesTable.jsx     # Live data table with status badges
    │   │   ├── Header.jsx                # Brand bar & policy/capacity badges
    │   │   ├── MetricsCards.jsx          # 8 responsive metric stat cards
    │   │   ├── PolicyConfig.jsx          # Policy selector & Sample pattern runner
    │   │   └── Visualizations.jsx        # Hit rate bar & capacity gauge
    │   ├── services/
    │   │   └── api.js                    # Fetch client for backend REST API
    │   ├── App.jsx                       # Main dashboard container
    │   └── index.css                     # Dark glassmorphism styling
    ├── index.html
    └── package.json
```

---

## ⚙️ Core Algorithms & Design Details

### 1. LRU (Least Recently Used) Eviction
- Maintains an internal Doubly Linked List with sentinel `head` and `tail` nodes.
- **Insert / Access**: Any accessed or newly inserted entry is linked immediately after `head` (most recently used).
- **Eviction**: When capacity is reached upon a new key insertion, the node preceding `tail` (`tail.prev`) is selected and unlinked in $O(1)$ time.

### 2. LFU (Least Frequently Used) Eviction
- Entries maintain an `accessCount` (frequency).
- Maintained via a frequency bucket mapping: each frequency $F$ has its own Doubly Linked List of nodes.
- **Access**: When an entry is accessed, its frequency increments from $F$ to $F + 1$. The node is removed from list $F$ and prepended to the head of list $F + 1$. If bucket $F$ becomes empty and was the current minimum frequency, `minFrequency` is incremented.
- **Tie-Breaker**: When multiple entries share the lowest frequency, the tail node of the `minFrequency` bucket is evicted. Because nodes in each bucket are ordered by access recency, the tie-breaker is **strictly and deterministically Least Recently Used**.

### 3. Per-Entry TTL (Time-To-Live)
- Each node stores an `expirationTimeMs = currentTime + (ttlSeconds * 1000)`.
- If no TTL is specified, `expirationTimeMs = -1` (infinite lifetime).
- **Lazy Expiration on GET**: When `get(key)` is called, the cache evaluates `System.currentTimeMillis() >= expirationTimeMs`. If expired, the entry is atomically evicted, `totalTtlExpirations` and `misses` are incremented, and `null` is returned.
- **Independence**: Capacity evictions count only towards `totalEvictions`, while expiration counts towards `totalTtlExpirations`.
- **Policy Switching Resilience**: Switching between LRU and LFU rebuilds the eviction list without resetting node expiration timestamps or frequencies.

### 4. Thread-Safety Approach
- Compound operations (checking capacity, selecting eviction candidates, updating bucket lists, removing keys, and recording metrics) are synchronized using fair `ReentrantLock` primitives.
- High-throughput metrics counters use `AtomicLong` to guarantee atomic increments under concurrent access without race conditions or torn reads.

---

## 📡 REST API Reference

| Method | Endpoint | Description | Sample Body |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cache/{key}` | Retrieve entry by key | None |
| `PUT` | `/api/cache/{key}` | Store or update entry | `{"value": "Apple", "ttlSeconds": 30}` |
| `DELETE` | `/api/cache/{key}` | Remove specific key | None |
| `DELETE` | `/api/cache` | Clear entire cache | None |
| `GET` | `/api/cache/metrics` | Get current performance metrics | None |
| `GET` | `/api/cache/entries` | List all active / expired entries | None |
| `GET` | `/api/cache/policy` | Get current eviction policy | None |
| `PUT` | `/api/cache/policy` | Change policy & capacity | `{"policy": "LFU", "capacity": 5}` |
| `POST` | `/api/cache/sample` | Run verification test pattern | None |

---

## 🧪 Sample Verification Pattern

The endpoint `POST /api/cache/sample` executes the sequence:
1. `PUT A`
2. `PUT B`
3. `PUT C`
4. `GET A` (Hit)
5. `GET A` (Hit)
6. `GET B` (Hit)
7. `GET X` (Miss - expected)
8. `GET C` (Hit)
9. `PUT D` (Triggers capacity eviction if full)
10. `GET A` (Hit)
11. `GET D` (Hit)

---

## 🛠️ How to Run Locally

### Prerequisites
- **Java**: JDK 17+ (Java 25 supported)
- **Node.js**: v18+ with `npm`

### 1. Run Backend (Spring Boot)

```bash
cd backend
# Build and run
.\mvnw.cmd spring-boot:run

# Or run tests
.\mvnw.cmd test
```
The backend starts at `http://localhost:8080`.

### 2. Run Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```
The dashboard will be available at `http://localhost:5173`.

---

## 📝 Example cURL Commands

```bash
# Store key with 30s TTL
curl -X PUT http://localhost:8080/api/cache/item1 \
  -H "Content-Type: application/json" \
  -d '{"value": "Gaming Laptop", "ttlSeconds": 30}'

# Retrieve key
curl http://localhost:8080/api/cache/item1

# View performance metrics
curl http://localhost:8080/api/cache/metrics

# Switch policy to LFU with capacity 10
curl -X PUT http://localhost:8080/api/cache/policy \
  -H "Content-Type: application/json" \
  -d '{"policy": "LFU", "capacity": 10}'

# Execute Sample Demonstration Sequence
curl -X POST http://localhost:8080/api/cache/sample
```
