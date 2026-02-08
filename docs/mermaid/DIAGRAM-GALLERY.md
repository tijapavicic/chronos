# 🎨 SE Platform - Architecture Visualization Gallery

> **Professional Mermaid Diagrams Collection**  
> A comprehensive visual documentation suite for the Simulation Engine Platform

---

## 📚 Table of Contents

1. [Overview](#overview)
2. [System Architecture Diagrams](#system-architecture-diagrams)
3. [Flow & Interaction Diagrams](#flow--interaction-diagrams)
4. [Infrastructure & Deployment](#infrastructure--deployment)
5. [State & Lifecycle Diagrams](#state--lifecycle-diagrams)
6. [Quick Reference](#quick-reference)
7. [Rendering Instructions](#rendering-instructions)

---

## 🎯 Overview

This gallery contains production-grade architecture diagrams for the SE Platform, featuring:

- ✨ **Professional styling** with modern color palettes
- 🎨 **Emoji icons** for instant visual recognition
- 📊 **Multi-level views** from high-level overviews to detailed component interactions
- 🔄 **Interactive flows** showing data movement and state transitions
- ☁️ **Infrastructure diagrams** with Kubernetes and cloud services
- 📈 **Executive dashboards** with metrics and observability

---

## 🏗️ System Architecture Diagrams

### 1️⃣ Level 2 - Detailed Component View

**File**: [`se-flow-lvl-2.mmd`](se-flow-lvl-2.mmd)

The most detailed view of the SE Platform architecture, showing all internal components, external systems, and data flows.

**Features**:
- 🏢 Complete SE Platform with Frontend and Backend tiers
- ⚡ External Calc Platform integration
- 🗂️ SaS Repository and database layer
- 💾 Redis caching strategy
- 📡 Event bus messaging
- 🎮 All API controllers, orchestrators, and workers

**Best For**: Technical deep-dives, developer onboarding, architecture reviews

---

### 2️⃣ Level 3 - Simplified System Flow

**File**: [`se-flow-lvl-3.mmd`](se-flow-lvl-3.mmd)

Simplified version focusing on major components and primary data flows.

**Features**:
- Clean, uncluttered view
- Focus on main user journeys
- Key integration points highlighted
- Same professional styling as Level 2

**Best For**: Executive presentations, stakeholder reviews, quick overviews

---

### 3️⃣ Frontend Detailed View (Landscape)

**File**: [`se-frontend-flow.mmd`](se-frontend-flow.mmd)

Horizontal layout focusing exclusively on the frontend layer and its immediate backend integration.

**Features**:
- 📱 User Interface component
- 🔐 Authorization & security layer
- 🔌 SA Client for backend communication
- 🎮 Backend API gateway
- Landscape orientation optimized for presentations

**Best For**: Frontend team documentation, API integration guides, security reviews

---

## 🔄 Flow & Interaction Diagrams

### 4️⃣ Detailed Sequence Diagram

**File**: [`se-sequence-detailed.mmd`](se-sequence-detailed.mmd)

Complete simulation request lifecycle with step-by-step sequence flow.

**Features**:
- 🎯 Numbered steps (30+ interactions)
- 🌈 Color-coded sections (Frontend, Backend, External, Persistence, Completion)
- ✅ Happy path flow with cache hit/miss scenarios
- ❌ Error handling and retry logic
- 🔍 Optional result query flow
- 📊 WebSocket notifications
- ⚠️ Failure scenarios with 3 retry attempts

**Key Flows Covered**:
1. User authentication and submission
2. Cache check optimization
3. Job scheduling and async processing
4. External computation via gRPC
5. Data persistence to SaS Repository
6. Result caching and user notification
7. Error handling with retries

**Best For**: Developer training, API documentation, troubleshooting, system design reviews

---

## ☁️ Infrastructure & Deployment

### 5️⃣ Executive Dashboard View

**File**: [`se-architecture-dashboard.mmd`](se-architecture-dashboard.mmd)

High-level architecture overview with operational metrics and system health indicators.

**Features**:
- 👥 User layer (10K+ active users, 500 req/min)
- 🌐 Presentation tier (Web UI, Mobile, API Gateway)
- ⚙️ Application tier (Auth, SE API, Orchestrator, Job Service)
- 🔌 Integration tier (Kafka, Redis, RabbitMQ)
- 🗄️ Data tier (PostgreSQL, MongoDB, TimescaleDB)
- 🌍 External services (Calc Engine, Payment, Notification, Monitoring)
- 📈 Observability (ELK, Prometheus, Jaeger, PagerDuty)

**Metrics Included**:
- Active pods and replicas
- Memory and CPU allocations
- Uptime percentages
- Queue and cache sizes
- Thread counts

**Best For**: Executive presentations, capacity planning, cost analysis, SLA reviews

---

### 6️⃣ Kubernetes Deployment Architecture

**File**: [`se-deployment-k8s.mmd`](se-deployment-k8s.mmd)

Production-grade Kubernetes deployment on AWS EKS with HA configuration.

**Features**:
- 🌍 Internet → CDN (CloudFlare + S3)
- ⚖️ Load Balancing (ALB + NLB)
- ☸️ Kubernetes cluster with multiple namespaces:
  - **se-platform-prod**: Frontend (3 pods), Backend (3 pods), Workers (2 pods)
  - **middleware**: Redis Cluster (3 nodes), Kafka Cluster (3 brokers)
  - **monitoring**: Prometheus, Grafana, Jaeger
- 🗄️ AWS RDS (Multi-AZ Primary + 2 Read Replicas)
- 💿 Storage (EBS, EFS)
- 🌍 External integrations (Calc Cluster, SaS API, DataDog)
- 📊 Horizontal Pod Autoscaling (HPA)
- 🔐 Cert Manager for TLS

**Infrastructure Details**:
- Multi-AZ deployment
- HA Redis with Sentinel
- Kafka cluster with 3 brokers
- PostgreSQL 15 with replication
- Ingress with Nginx
- Service mesh ready

**Best For**: DevOps documentation, infrastructure reviews, DR planning, cost optimization

---

## 🔄 State & Lifecycle Diagrams

### 7️⃣ Job Lifecycle State Machine

**File**: [`se-job-lifecycle.mmd`](se-job-lifecycle.mmd)

Complete state machine showing all possible job states and transitions.

**States**:
- 🚀 **Submitted** → Input validation
- 📋 **Queued** → Waiting for worker
- 🎯 **Scheduled** → Cache check
- 🏃 **Running** → Execution pipeline (7 sub-states)
- ✅ **Completed** → Success with caching
- ❌ **Failed** → Retry logic (max 3 attempts)
- ⛔ **PermanentFailure** → Max retries exceeded
- 🛑 **Cancelled** → User-initiated abort
- ⏸️ **Paused** → System maintenance
- 🗄️ **Archived** → Cold storage

**Features**:
- Retry strategy with exponential backoff
- Cache hit optimization
- Timeout handling
- Resource cleanup
- Notification triggers
- Data archival policies

**Notes Included**:
- Queue priority and max wait times
- Computation timeout (30s) and retry attempts
- Cache TTL (1 hour)
- Retry delays (2s, 4s, 8s)
- Archive retention (90 days in S3 Glacier)
- Pause limits (30 minutes)

**Best For**: Job scheduler implementation, SLA definition, error handling design, monitoring setup

---

## 📊 Quick Reference

### Diagram Comparison Matrix

| Diagram | Type | Complexity | Best Use Case | Key Audience |
|---------|------|------------|---------------|--------------|
| **se-flow-lvl-2.mmd** | Flowchart | ⭐⭐⭐⭐⭐ | Technical deep-dive | Engineers, Architects |
| **se-flow-lvl-3.mmd** | Flowchart | ⭐⭐⭐ | System overview | Technical managers |
| **se-frontend-flow.mmd** | Flowchart | ⭐⭐ | Frontend focus | Frontend developers |
| **se-sequence-detailed.mmd** | Sequence | ⭐⭐⭐⭐⭐ | Integration details | Developers, QA |
| **se-architecture-dashboard.mmd** | Graph | ⭐⭐⭐⭐ | Operational view | Executives, DevOps |
| **se-deployment-k8s.mmd** | Graph | ⭐⭐⭐⭐⭐ | Infrastructure | DevOps, Platform team |
| **se-job-lifecycle.mmd** | State Diagram | ⭐⭐⭐⭐ | State management | Engineers, QA |

### Color Palette Reference

| Component Type | Fill Color | Stroke Color | Usage |
|---------------|------------|--------------|-------|
| **Frontend** | `#052E56` | `#0a4d8c` | UI, Authorization, Client |
| **Backend** | `#1168bd` | `#1a8fff` | Controllers, Services, Workers |
| **Database** | `#438dd5` | `#5ca3e6` | Cache, PostgreSQL, Storage |
| **SaS Platform** | `#087b7b` | `#0a9c9c` | Repository, SaS DB |
| **External** | `#845878` | `#a56f93` | Calc Engine, 3rd party |
| **User/Person** | `#08427b` | `#0a4d8c` | Actors, end users |

### Emoji Icon Legend

| Icon | Meaning | Icon | Meaning |
|------|---------|------|---------|
| 🎯 | User/Actor | 🏢 | Platform/System |
| 📱 | User Interface | 🔐 | Security/Auth |
| 🔌 | Client/API | 🎮 | Controller |
| 🎼 | Orchestrator | ⏰ | Scheduler |
| 🏃 | Worker/Runner | 📊 | Analytics/Calc |
| 🚗 | SideCar/Proxy | 📡 | Event Bus |
| 💾 | Cache | 🗃️ | Database |
| 🖥️ | Server/Compute | 🗄️ | Storage |
| ⚡ | Fast/Performance | ☁️ | Cloud |

---

## 🛠️ Rendering Instructions

### IntelliJ IDEA

1. **Install Mermaid Plugin**:
   - Go to `Settings` → `Plugins`
   - Search for "Mermaid"
   - Install and restart IDE

2. **View Diagrams**:
   - Open any `.mmd` file
   - Click the preview pane (split view)
   - Diagrams render automatically

### VS Code

1. **Install Extension**:
   ```bash
   code --install-extension bierner.markdown-mermaid
   ```

2. **View Diagrams**:
   - Open `.mmd` file
   - Press `Cmd+Shift+V` (Mac) or `Ctrl+Shift+V` (Windows)
   - Or right-click → "Open Preview"

### GitHub / GitLab

- Diagrams render automatically in Markdown files
- Just embed with triple backticks and `mermaid` language tag

### Export to PNG/SVG

**Using Mermaid CLI**:
```bash
# Install
npm install -g @mermaid-js/mermaid-cli

# Export to PNG
mmdc -i se-flow-lvl-2.mmd -o se-flow-lvl-2.png -w 3000 -H 2000

# Export to SVG
mmdc -i se-flow-lvl-2.mmd -o se-flow-lvl-2.svg

# Export all diagrams
for file in *.mmd; do 
  mmdc -i "$file" -o "${file%.mmd}.png" -w 3000 -H 2000
done
```

**Using Online Editors**:
- [Mermaid Live Editor](https://mermaid.live/) - Copy/paste diagram code
- [Draw.io](https://app.diagrams.net/) - Import Mermaid format
- [Kroki](https://kroki.io/) - API-based rendering

### Embed in Documentation

**Markdown**:
````markdown
```mermaid
# Paste diagram code here
```
````

**HTML**:
```html
<div class="mermaid">
  # Paste diagram code here
</div>
<script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
<script>mermaid.initialize({startOnLoad:true});</script>
```

**Confluence**:
- Use "HTML Macro" or "Mermaid Macro" plugin
- Paste diagram code

**Notion**:
- Use `/embed` command
- Link to Mermaid Live Editor with diagram

---

## 📖 Documentation Standards

### Naming Conventions

- **Format**: `{domain}-{type}-{level}.mmd`
- **Examples**:
  - `se-flow-lvl-2.mmd` - SE platform flow, level 2 detail
  - `se-sequence-detailed.mmd` - SE sequence diagram, detailed
  - `se-deployment-k8s.mmd` - SE deployment on Kubernetes

### Version Control

- ✅ Commit `.mmd` source files
- ✅ Commit generated `.png` or `.svg` if needed for presentations
- ❌ Don't commit large resolution images (>5MB)
- ✅ Use Git LFS for binary image files

### Maintenance

- **Review Frequency**: Quarterly or with major architecture changes
- **Owner**: Platform Architecture Team
- **Approvers**: CTO, Lead Architect, Principal Engineers
- **Update Trigger**: Infrastructure changes, new services, major refactors

---

## 🚀 Next Steps

### Planned Enhancements

- [ ] Add C4 model diagrams (Context, Container, Component, Code)
- [ ] Create user journey maps with timeline
- [ ] Add network topology diagrams
- [ ] Create database ER diagrams
- [ ] Add security architecture diagrams
- [ ] Create cost allocation diagrams
- [ ] Add disaster recovery (DR) diagrams

### Feedback & Contributions

Have suggestions? Found an issue? Want to add a diagram?

1. Open an issue in the repo
2. Tag `@platform-architecture` team
3. Use template: `[DIAGRAM] Your suggestion`

---

## 📜 License & Attribution

**Created By**: SE Platform Architecture Team  
**Last Updated**: February 2026  
**Tool**: Mermaid.js (v10+)  
**License**: Internal use only - Proprietary

---

## 🎉 Acknowledgments

Special thanks to:
- **Mermaid.js** community for the amazing diagramming tool
- **Platform Engineering** team for infrastructure insights
- **Development teams** for technical accuracy reviews
- **Design team** for color palette recommendations

---

**🎨 Happy Diagramming!**

*For questions or support, contact: platform-architecture@company.com*

