# 🎨 Mermaid Architecture Diagrams - Complete Gallery

> **Professional visualization suite for the SE Simulation Platform**

---

## 🌟 Featured Diagram Gallery

**👉 [View the Complete Diagram Gallery →](DIAGRAM-GALLERY.md)**

The comprehensive documentation with all diagrams, usage guides, and rendering instructions.

---

## 📊 Quick Links to Diagrams

### 🏗️ System Architecture Diagrams

| Diagram | Description | Complexity | File |
|---------|-------------|------------|------|
| **Level 2 - Detailed** | Complete component view with all services | ⭐⭐⭐⭐⭐ | [se-flow-lvl-2.mmd](se-flow-lvl-2.mmd) |
| **Level 3 - Simplified** | High-level system overview | ⭐⭐⭐ | [se-flow-lvl-3.mmd](se-flow-lvl-3.mmd) |
| **Frontend Detail** | Frontend layer focus (landscape) | ⭐⭐ | [se-frontend-flow.mmd](se-frontend-flow.mmd) |
| **Architecture Mindmap** | Interactive mindmap view | ⭐⭐⭐ | [se-architecture-mindmap.mmd](se-architecture-mindmap.mmd) |

### 🔄 Flow & Interaction Diagrams

| Diagram | Description | Complexity | File |
|---------|-------------|------------|------|
| **Sequence Diagram** | 30-step simulation lifecycle flow | ⭐⭐⭐⭐⭐ | [se-sequence-detailed.mmd](se-sequence-detailed.mmd) |
| **State Machine** | Job lifecycle with all states | ⭐⭐⭐⭐ | [se-job-lifecycle.mmd](se-job-lifecycle.mmd) |

### ☁️ Infrastructure & Operations

| Diagram | Description | Complexity | File |
|---------|-------------|------------|------|
| **Kubernetes Deployment** | Full K8s architecture on AWS EKS | ⭐⭐⭐⭐⭐ | [se-deployment-k8s.mmd](se-deployment-k8s.mmd) |
| **Executive Dashboard** | System overview with metrics | ⭐⭐⭐⭐ | [se-architecture-dashboard.mmd](se-architecture-dashboard.mmd) |

### 📐 C4 Model Diagrams

| Level | Description | File |
|-------|-------------|------|
| **Context** | Big picture - system landscape | [c4_context.mmd](c4_context.mmd) |
| **Container** | Major applications/services | [c4_container.mmd](c4_container.mmd) |
| **Component** | Internal components detail | [c4_component.mmd](c4_component.mmd) |

---

## 🎯 Choose Your Diagram

### For Developers
- **Getting Started**: [se-flow-lvl-3.mmd](se-flow-lvl-3.mmd) - Simple overview
- **Deep Dive**: [se-flow-lvl-2.mmd](se-flow-lvl-2.mmd) - All components
- **API Integration**: [se-sequence-detailed.mmd](se-sequence-detailed.mmd) - Request flow
- **State Management**: [se-job-lifecycle.mmd](se-job-lifecycle.mmd) - Job states

### For Architects
- **System Design**: [se-architecture-mindmap.mmd](se-architecture-mindmap.mmd) - Complete view
- **C4 Models**: Context → Container → Component progression
- **Infrastructure**: [se-deployment-k8s.mmd](se-deployment-k8s.mmd) - Cloud architecture

### For DevOps/SRE
- **Deployment**: [se-deployment-k8s.mmd](se-deployment-k8s.mmd) - K8s setup
- **Observability**: [se-architecture-dashboard.mmd](se-architecture-dashboard.mmd) - Metrics layer

### For Executives
- **Business Overview**: [se-architecture-dashboard.mmd](se-architecture-dashboard.mmd) - With metrics
- **High-Level**: [se-flow-lvl-3.mmd](se-flow-lvl-3.mmd) - Simple view

---

## C4 Model Levels

### 1. Context Diagram (`c4_context.mmd`)
Shows the big picture - how the Simulation Platform fits into the overall system landscape:
- External users
- The Simulation Platform as a single system
- External systems (Calculation Engine, SaS Database)
- High-level interactions

**Purpose**: Understand who uses the system and what external systems it integrates with.

### 2. Container Diagram (`c4_container.mmd`)
Zooms into the Simulation Platform to show the major containers (applications/services):
- REST Controllers
- Services (Orchestrator, Scheduler, Job Runner)
- Clients (Calculation Client, SaS SideCar)
- Data stores (Cache, Event Bus)
- External systems

**Purpose**: Understand the major architectural building blocks and their interactions.

### 3. Component Diagram (`c4_component.mmd`)
Zooms into the Simulation Orchestrator container to show its internal components:
- Simulation Service
- Cache Manager
- Status Tracker
- Result Aggregator
- Event Publisher

**Purpose**: Understand the internal structure of a specific container.

---

## Rendering the Diagrams

### IntelliJ IDEA
1. Install the Mermaid plugin (see `mermaid-setup.md`)
2. Open any `.mmd` file
3. The preview should render automatically

### Mermaid Live Editor
1. Visit [mermaid.live](https://mermaid.live)
2. Copy and paste the diagram code
3. Export as PNG/SVG

### Command Line
```bash
npm install -g @mermaid-js/mermaid-cli
mmdc -i c4_context.mmd -o c4_context.png
mmdc -i c4_container.mmd -o c4_container.png
mmdc -i c4_component.mmd -o c4_component.png
```

## C4 Model Resources

- [C4 Model Official Site](https://c4model.com/)
- [Mermaid C4 Diagrams Documentation](https://mermaid.js.org/syntax/c4.html)

## Diagram Comparison

| Original | C4 Model |
|----------|----------|
| `c4_simulation.mmd` | Generic flowchart showing components |
| `c4_context.mmd` | System context (Level 1) |
| `c4_container.mmd` | Container view (Level 2) |
| `c4_component.mmd` | Component view (Level 3) |

The C4 diagrams provide a more structured, hierarchical view of the architecture at different zoom levels.

