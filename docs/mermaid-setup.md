# Mermaid Diagram Setup in IntelliJ IDEA

## Supported Diagram Types

The plugin supports various Mermaid diagram types:

- Flowchart
- Sequence Diagram
- Class Diagram
- State Diagram
- Entity Relationship Diagram
- User Journey
- Gantt Chart
- Pie Chart
- Git Graph
- C4 Diagram

## Architecture Diagrams (Beta)

Architecture diagrams (`architecture-beta`) require Mermaid version 10.6.0 or higher. If your plugin doesn't support this yet, use alternative rendering methods.

## Alternative Rendering Options

### Option 1: Mermaid Live Editor

Visit [mermaid.live](https://mermaid.live) and paste your diagram code to render it online.

### Option 2: Mermaid CLI

Install and use the CLI tool:

```bash
npm install -g @mermaid-js/mermaid-cli
mmdc -i diagram.mmd -o diagram.png
```

### Option 3: Plugin Settings

Some plugins allow specifying the Mermaid version:

1. Go to **Settings** → **Languages & Frameworks** → **Mermaid** (if available)
2. Configure the Mermaid version if the option exists

## Creating Mermaid Files

1. Create a new file with `.mmd` or `.mermaid` extension
2. Start with the diagram type keyword (e.g., `sequenceDiagram`, `graph TD`)
3. Add your diagram syntax
4. The plugin will render a preview automatically

## Troubleshooting

- **Diagram not rendering**: Check if the diagram type is supported by your plugin version
- **Syntax errors**: Validate your diagram on [mermaid.live](https://mermaid.live)
- **Beta features not working**: Update your plugin or use external rendering tools

## Example Diagrams in This Project

See the `docs/mermaid/` folder for example diagrams:

- `architecture.mmd` - Architecture diagram (requires Mermaid 10.6.0+)
- `sequence.mmd` - Sequence diagram
- `test.mmd` - Test diagram

