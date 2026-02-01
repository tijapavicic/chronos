# C4 Model Diagrams for Simulation Platform

This folder contains C4 model diagrams using Mermaid syntax for the Simulation Platform.

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

