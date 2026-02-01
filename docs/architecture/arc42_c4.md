# arc42 + C4 Architecture Documentation for Chronos

This document combines the arc42 documentation template with lightweight C4 diagrams (PlantUML) to provide both narrative and visual architecture views for the Chronos project.

> Location of C4 PlantUML files:
> - `docs/demos/c4_diagrams/c4_context.puml`
> - `docs/demos/c4_diagrams/c4_container.puml`
> - `docs/demos/c4_diagrams/c4_component.puml`
> - `docs/demos/c4_diagrams/c4_classes.puml`
> - `docs/demos/c4_diagrams/c4_simulation_*.puml`

Quick rendering note: if you have PlantUML and Graphviz installed you can render diagrams with:

```bash
cd /path/to/chronos
plantuml -tpng docs/demos/c4_diagrams/c4_*.puml
```

---

## 1. Introduction and Goals

Purpose: provide a concise architectural overview of Chronos (a small Spring Boot API for managing facilities and running simulations). This doc uses arc42 sectioning and inserts C4 diagrams where they help visualize the architecture.

Stakeholders:
- API clients (integrators)
- Developers
- DevOps / Operators
- QA / Testers

Key goals:
- Simple, maintainable service for facility CRUD and simulation pinging
- Clear separation of API, business logic, and persistence
- Observability via metrics

Constraints:
- Small codebase intended for demonstration and quick iteration
- Uses Spring Boot and JPA

---

## 2. Context and Scope (arc42 §1, §2)

System scope: Chronos provides REST endpoints under `/api/facilities` and `/simulation/ping` for managing facilities and verifying simulation availability.

Include the C4 System Context diagram to show actors and the system boundary:

- C4 Context (System scope):

![C4 Context](images/c4_context.png)

If rendered to PNG, you can embed the image here; otherwise open the PUML file in your editor or render it with PlantUML.

---

## 3. Solution Strategy (arc42 §3)

- Keep controllers thin and delegate to services
- Persist domain entities using Spring Data JPA
- Expose a small, focused API surface for facilities
- Provide metrics endpoints for monitoring

---

## 4. Building Block View — Containers (arc42 §4 / C4 Container)

This section describes the main containers/components and how they map to deployment units.

- Load Balancer / Ingress (NGINX)
- Chronos API (Spring Boot application)
- Database (H2 in-memory for demo, or RDBMS for production)
- Monitoring (Prometheus + Grafana)
- Scheduler (background job runner)

See C4 Container diagram:

![C4 Container](images/c4_container.png)

Deployment diagrams (variants):

- Beige/Vanilla Bean: `docs/demos/8_deployment_beige_vanilla_bean.puml`
- DarkSlateBlue: `docs/demos/8_deployment_darkslateblue.puml`

---

## 5. Building Block View — Components (arc42 §5 / C4 Component)

Inside the Chronos application, the main components are:
- `FacilityController` — REST endpoints
- `SimulationController` — ping endpoint
- `FacilityService` — business logic and mapping
- `FacilityRepository` — JPA repository
- `Facility` entity and `FacilityDTO`

See C4 Component diagram and Classes diagram:

![C4 Component](images/c4_component.png)

![C4 Classes (code view)](images/c4_classes.png)

---

## Simulation Subsystem (C4)

We also provide focused C4 views for the Simulation subsystem (SimulFrontend, SimulBackend, SaS DB, SaS Interface, Calculation Engine).

### Simulation Context

![Simulation Context](images/c4_simulation_context.png)

### Simulation Container

![Simulation Container](images/c4_simulation_container.png)

### Simulation Backend Components

![Simulation Component](images/c4_simulation_component.png)

---

## 6. Runtime View (arc42 §6)

Typical flows (runtime):
- Create Facility: Client → FacilityController → FacilityService → FacilityRepository → DB
- Search Facility: Client → FacilityController → FacilityService → FacilityRepository → DB

Use the appropriate sequence/activity diagrams in `docs/demos/` for more detailed flows (e.g., `4_activity.puml`).

---

## 7. Deployment View (arc42 §7)

Deployment diagrams are available in `docs/demos/` (e.g., `8_deployment_beige_vanilla_bean.puml` and the dark-slate-blue / crimson variants). They show a small cluster of app nodes behind a load balancer, persistence, and monitoring.

---

## 8. Cross-cutting Concepts (arc42 §8)

- Security: basic Spring Security config is present (see `src/main/java/.../config/*`)
- Observability: expose metrics for Prometheus (monitoring integration)
- Validation: DTOs include bean validation annotations

---

## 9. Risks and Technical Debt (arc42 §9)

- Current persistence layer assumed simple; migrating to distributed DB requires additional design
- No advanced resilience (circuit-breakers) in the demo
- Authentication/authorization may need to be hardened for production

---

## 10. How to render diagrams

Render all C4 diagrams (PNG) at once:

```bash
cd /path/to/chronos
plantuml -tpng docs/demos/c4_diagrams/c4_*.puml -o docs/architecture/images
```

If your environment lacks Graphviz or PlantUML CLI, use the `plantuml.jar` approach:

```bash
curl -L -o plantuml.jar https://github.com/plantuml/plantuml/releases/latest/download/plantuml.jar
java -jar plantuml.jar -tpng docs/demos/c4_diagrams/c4_*.puml -o docs/architecture/images
```

---

## 11. Mapping: arc42 sections ↔ C4 artifacts

- arc42 §1/§2 (Context)  → `c4_context.puml`
- arc42 §4 (Containers)  → `c4_container.puml`
- arc42 §5 (Components)  → `c4_component.puml`, `c4_classes.puml`
- arc42 §6 (Runtime)     → activity/sequence diagrams in `docs/demos/`
- arc42 §7 (Deployment)  → `8_deployment_*.puml`

---

## 12. Next steps (suggested)

- Expand the component diagram with more classes for configs and security.
- Add per-environment deployment diagrams (dev, staging, prod) and Kubernetes manifests if needed.


---

Generated by automation — if you want this broken into separate HTML pages or an AsciiDoc arc42 template, I can convert it.  
