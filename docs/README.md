# Architecture docs for Chronos

This folder contains combined arc42 narrative and C4 PlantUML diagrams.

Render diagrams with PlantUML (requires Graphviz dot on PATH):

```bash
# render all C4 diagrams to PNG
plantuml -tpng ../demos/c4_*.puml

# render a single diagram
plantuml -tpng ../demos/c4_diagrams/c4_classes.puml
```

If PlantUML CLI isn't available you can download `plantuml.jar` and run:

```bash
java -jar plantuml.jar ../demos/c4_*.puml
```
