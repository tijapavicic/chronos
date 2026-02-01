# C4 PlantUML – Quick Instructions

This short guide points you to the C4 PlantUML standard library and shows quick, practical steps to include and render C4 diagrams in this repo.

Reference

- C4 standard library documentation: https://crashedmind.github.io/PlantUMLHitchhikersGuide/C4/C4Stdlib.html
- C4 model overview: https://c4model.com/
- https://plantuml.com/
- https://deepwiki.com/plantuml-stdlib/C4-PlantUML/3.3-component-diagrams
- https://github.com/tupadr3/plantuml-icon-font-sprites/tree/main/icons
- OMG!!!!!  https://crashedmind.github.io/PlantUMLHitchhikersGuide/StdlibUnderTheHood/StdlibUnderstanding.html
- https://crashedmind.github.io/PlantUMLHitchhikersGuide/StdlibUnderTheHood/StdlibUnderstanding.html
- https://crashedmind.github.io/PlantUMLHitchhikersGuide/StdlibUnderTheHood/StdlibUnderstanding.html
- 
- 
Purpose

- Use the C4-PlantUML includes to create Context / Container / Component / Deployment diagrams with PlantUML.
- Keep diagrams in `docs/demos/` and render them into images for documentation under `docs/architecture/`.

Prerequisites

- Java (JRE) installed
- Graphviz `dot` available on PATH (required by PlantUML for layout)
- PlantUML CLI (optional) or `plantuml.jar` (recommended if you don't want to install the CLI)

Quick include snippet (in a `.puml` file)

```puml
!define C4P https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master
!includeurl C4P/C4_Context.puml
!includeurl C4P/C4_Container.puml
!includeurl C4P/C4_Component.puml
```

Render commands

- Using PlantUML CLI (recommended if installed via Homebrew):

```bash
# render a single diagram to PNG
plantuml -tpng docs/demos/c4_context.puml

# render multiple diagrams
plantuml -tpng docs/demos/c4_*.puml
```

- Using the PlantUML jar (no CLI install required):

```bash
curl -L -o plantuml.jar https://github.com/plantuml/plantuml/releases/latest/download/plantuml.jar
java -jar plantuml.jar -tpng docs/demos/c4_*.puml
```

Output location

- By default PlantUML writes generated images next to the source files. Use `-o <dir>` to write to a specific output folder, e.g. `-o docs/architecture/images`.

Troubleshooting

- "Cannot find graphviz": install Graphviz (macOS/Homebrew: `brew install graphviz`) and ensure `dot` is on your PATH.
- If PlantUML fails to fetch the C4 stdlib includes (offline environment), vendor the C4 files locally and change `!includeurl` to `!include` with a local path.
- If images are not generated where you expect, run PlantUML with `-v` to get verbose output.

Best practices

- Keep one PUML per diagram and name them clearly under `docs/demos/`.
- Commit the generated images to `docs/architecture/images` if you want the docs to render on GitHub without running PlantUML server-side.
- Use a consistent palette and file naming convention for easy discoverability (examples in `docs/demos/`).

Example (Context diagram header)

```puml
@startuml
!define C4P https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master
!includeurl C4P/C4_Context.puml

Person(apiClient, "API Client")
System(chronos, "Chronos API")
Rel(apiClient, chronos, "Uses")
@enduml
```

```shell
cd /Users/copor/IdeaProjects/JavaProjects/chronos && plantuml -tsvg -v -o docs/architecture/images docs/demos/c4_diagrams/*.puml docs/demos/*.puml 2>&1 | sed -n '1,300p'

cd /Users/copor/IdeaProjects/JavaProjects/chronos && plantuml -tpng -v docs/demos/c4_diagrams_SE/c4_simulation_component.puml 2>&1 | sed -n '1,200p'
```

Happy documenting!
