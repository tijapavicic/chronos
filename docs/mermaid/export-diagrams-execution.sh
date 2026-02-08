cd docs/mermaid

# Install mermaid-cli
./export-diagrams.sh setup

# Export all diagrams to PNG
./export-diagrams.sh all

# See all options
./export-diagrams.sh help

./export-diagrams.sh all #- Export all to PNG (3000x2000)
./export-diagrams.sh hq #- High-quality 4K exports
./export-diagrams.sh key #- Export key diagrams to all formats
./export-diagrams.sh single #- Interactive single diagram export
./export-diagrams.sh clean #- Remove all generated files
