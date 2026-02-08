# 🗄️ SE Platform Database Schema

## Overview

This Entity Relationship Diagram (ERD) shows the complete database schema for the SE Platform, logically grouped into functional domains.

---

## 📊 Logical Groups

### 👤 **TENANT & USER GROUP** (Multi-tenancy & Authentication)
**Color**: Blue  
**Tables**: `TENANT`, `USER`, `SESSION`, `API_KEY`

Handles multi-tenant architecture, user authentication, and API access control.

**Key Features**:
- Multi-tenant isolation
- JWT-based sessions
- API key management with scopes
- Secure password hashing

---

### 🎯 **CORE SIMULATION GROUP** (Jobs & Execution)
**Color**: Green  
**Tables**: `SCENARIO`, `SIMULATION_JOB`, `JOB_EXECUTION`, `JOB_RESULT`, `JOB_LOG`

Core simulation workflow from scenario definition to job execution and results.

**Key Features**:
- Scenario versioning with parent-child relationships
- Job state tracking (submitted → completed)
- Multiple execution attempts for retries
- Result storage with S3 paths for large outputs
- Comprehensive logging

**Job Lifecycle**:
1. User creates a `SCENARIO`
2. Submits a `SIMULATION_JOB`
3. System creates `JOB_EXECUTION` (can retry up to 3 times)
4. Produces `JOB_RESULT`
5. Logs captured in `JOB_LOG`

---

### ⚡ **EXTERNAL INTEGRATION GROUP** (Calc & SaS)
**Color**: Purple  
**Tables**: `CALC_SERVICE`, `CALC_REQUEST`, `SAS_SYNC`

Integration with external computation services and SaS (Scenario and Results) repository.

**Key Features**:
- Multiple calc service support (gRPC, HTTP)
- Request tracking with retry logic
- Health check configuration
- SaS synchronization for persistence

**Flow**:
1. Job triggers `CALC_REQUEST` to a `CALC_SERVICE`
2. Response tracked with duration and retry count
3. Results sync to SaS via `SAS_SYNC`

---

### 💾 **PERFORMANCE GROUP** (Cache)
**Color**: Cyan  
**Tables**: `CACHE_ENTRY`

Redis-backed caching layer for performance optimization.

**Key Features**:
- Hash-based cache keys
- TTL management (1 hour default)
- Hit count tracking
- Size tracking for memory management

**Strategy**: Check cache before executing expensive jobs.

---

### 📡 **EVENTS & OBSERVABILITY GROUP**
**Color**: Orange  
**Tables**: `EVENT`, `AUDIT_LOG`, `JOB_METRIC`

Event-driven architecture, auditing, and metrics collection.

**Key Features**:
- **EVENT**: Kafka-based event bus for job state changes
- **AUDIT_LOG**: Complete audit trail of user actions
- **JOB_METRIC**: Performance metrics (CPU, memory, duration)

**Event Types**:
- `JOB_SUBMITTED`
- `JOB_STARTED`
- `JOB_COMPLETED`
- `JOB_FAILED`

---

### 📧 **NOTIFICATION GROUP**
**Color**: Pink  
**Tables**: `NOTIFICATION`

Multi-channel notification system.

**Key Features**:
- Multiple types: EMAIL, SMS, PUSH, WEBHOOK
- Delivery tracking
- Read receipts
- Job completion alerts

---

## 🔗 Key Relationships

### Multi-Tenancy
```
TENANT (1) ──── (N) USER
TENANT (1) ──── (N) SIMULATION_JOB
```

### Core Workflow
```
USER (1) ──── (N) SCENARIO
SCENARIO (1) ──── (N) SIMULATION_JOB
SIMULATION_JOB (1) ──── (N) JOB_EXECUTION
SIMULATION_JOB (1) ──── (1) JOB_RESULT
```

### External Integration
```
SIMULATION_JOB (1) ──── (N) CALC_REQUEST
CALC_SERVICE (1) ──── (N) CALC_REQUEST
SIMULATION_JOB (1) ──── (N) SAS_SYNC
```

### Observability
```
SIMULATION_JOB (1) ──── (N) EVENT
SIMULATION_JOB (1) ──── (N) JOB_METRIC
USER (1) ──── (N) AUDIT_LOG
```

---

## 📈 Table Statistics

| Group | Tables | Total Columns | Key Entities |
|-------|--------|---------------|--------------|
| Tenant & User | 4 | ~35 | TENANT, USER |
| Core Simulation | 5 | ~60 | SIMULATION_JOB, SCENARIO |
| External Integration | 3 | ~30 | CALC_REQUEST, SAS_SYNC |
| Performance | 1 | ~10 | CACHE_ENTRY |
| Observability | 3 | ~30 | EVENT, AUDIT_LOG, JOB_METRIC |
| Notification | 1 | ~12 | NOTIFICATION |
| **TOTAL** | **17** | **~177** | - |

---

## 🎨 Design Optimizations

### ✅ **Simplified Field Names**
- Removed verbose comments from field definitions
- Shorter, clearer names (e.g., `error_msg` instead of `error_message`)
- Consistent naming patterns

### ✅ **Logical Grouping**
- 6 functional groups instead of scattered tables
- Clear group boundaries with emoji headers
- Related tables kept together

### ✅ **Cleaner Relationships**
- All relationships at the end
- Grouped by entity
- Clear relationship labels

### ✅ **Reduced Verbosity**
- Removed redundant type comments (e.g., "BCrypt hashed", "Auto-generated")
- Kept only essential information
- Cleaner field lists

---

## 🔧 Implementation Notes

### Indexes
Recommended indexes for performance:

```sql
-- User lookup
CREATE INDEX idx_user_email ON USER(email);
CREATE INDEX idx_user_tenant ON USER(tenant_id);

-- Job queries
CREATE INDEX idx_job_user ON SIMULATION_JOB(user_id);
CREATE INDEX idx_job_status ON SIMULATION_JOB(status);
CREATE INDEX idx_job_submitted ON SIMULATION_JOB(submitted_at);

-- Cache lookup
CREATE INDEX idx_cache_key ON CACHE_ENTRY(cache_key);
CREATE INDEX idx_cache_expires ON CACHE_ENTRY(expires_at);

-- Event processing
CREATE INDEX idx_event_processed ON EVENT(processed);
CREATE INDEX idx_event_job ON EVENT(job_id);
```

### Partitioning Strategy
For high-volume tables:

```sql
-- Partition JOB_LOG by month
CREATE TABLE JOB_LOG PARTITION BY RANGE (logged_at);

-- Partition EVENT by month
CREATE TABLE EVENT PARTITION BY RANGE (created_at);

-- Partition AUDIT_LOG by month
CREATE TABLE AUDIT_LOG PARTITION BY RANGE (created_at);
```

### Data Retention Policies

| Table | Retention | Strategy |
|-------|-----------|----------|
| JOB_LOG | 90 days | Archive to S3 |
| EVENT | 30 days | Archive to S3 |
| AUDIT_LOG | 1 year | Cold storage |
| JOB_METRIC | 90 days | Aggregate to analytics DB |
| CACHE_ENTRY | 1 hour TTL | Auto-expire in Redis |

---

## 🚀 Usage

### View in IntelliJ IDEA
Open `se-database-schema.mmd` - the Mermaid plugin will render it automatically.

### Export to Image
```bash
mmdc -i se-database-schema.mmd -o se-database-schema.png -w 4000 -H 3000
```

### Generate JPA Entities
Use this schema as reference to create Spring Boot `@Entity` classes with proper relationships.

### Generate Flyway Migrations
Convert this ERD to SQL migration scripts for database versioning.

---

## 📚 Related Documentation

- [Architecture Dashboard](se-architecture-dashboard.mmd) - System overview
- [Sequence Diagram](se-sequence-detailed.mmd) - Request flow
- [State Machine](se-job-lifecycle.mmd) - Job states
- [Complete Diagram Gallery](DIAGRAM-GALLERY.md) - All diagrams

---

**Last Updated**: February 2026  
**Maintained By**: Platform Architecture Team  
**Database**: PostgreSQL 15+

