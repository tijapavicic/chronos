# 🎨 Mermaid Diagram Export Cheat Sheet

Complete guide for exporting Mermaid diagrams to PNG, SVG, and PDF formats.

---

## 🚀 Quick Start

### Installation

```bash
# Install mermaid-cli globally
npm install -g @mermaid-js/mermaid-cli

# OR use the provided script
chmod +x export-diagrams.sh
./export-diagrams.sh setup
```

### Verify Installation

```bash
mmdc --version
# Should output: @mermaid-js/mermaid-cli version X.X.X
```

---

## 📝 Basic Commands

### Single File Export

```bash
# Export to PNG
mmdc -i diagram.mmd -o diagram.png

# Export to SVG
mmdc -i diagram.mmd -o diagram.svg

# Export to PDF
mmdc -i diagram.mmd -o diagram.pdf
```

### Custom Dimensions

```bash
# Standard quality (3000x2000)
mmdc -i diagram.mmd -o diagram.png -w 3000 -H 2000

# High quality 4K (5000x3500)
mmdc -i diagram.mmd -o diagram.png -w 5000 -H 3500

# Ultra high quality 8K (7680x4320)
mmdc -i diagram.mmd -o diagram.png -w 7680 -H 4320
```

---

## 🎨 Themes & Styling

### Available Themes

```bash
# Default theme
mmdc -i diagram.mmd -o diagram.png -t default

# Dark theme
mmdc -i diagram.mmd -o diagram.png -t dark

# Forest theme
mmdc -i diagram.mmd -o diagram.png -t forest

# Neutral theme
mmdc -i diagram.mmd -o diagram.png -t neutral
```

### Background Colors

```bash
# White background (default)
mmdc -i diagram.mmd -o diagram.png -b white

# Transparent background
mmdc -i diagram.mmd -o diagram.png -b transparent

# Custom hex color
mmdc -i diagram.mmd -o diagram.png -b "#f0f0f0"
```

### Scale Factor

```bash
# 2x scaling for retina displays
mmdc -i diagram.mmd -o diagram.png -s 2

# 3x scaling for ultra-high DPI
mmdc -i diagram.mmd -o diagram.png -s 3
```

---

## 🔄 Batch Export

### Export All Diagrams to PNG

```bash
for file in *.mmd; do 
  mmdc -i "$file" -o "${file%.mmd}.png" -w 3000 -H 2000
done
```

### Export All Diagrams to SVG

```bash
for file in *.mmd; do 
  mmdc -i "$file" -o "${file%.mmd}.svg"
done
```

### Export All Diagrams to PDF

```bash
for file in *.mmd; do 
  mmdc -i "$file" -o "${file%.mmd}.pdf"
done
```

### Export to All Formats

```bash
for file in *.mmd; do
  base="${file%.mmd}"
  mmdc -i "$file" -o "${base}.png" -w 3000 -H 2000
  mmdc -i "$file" -o "${base}.svg"
  mmdc -i "$file" -o "${base}.pdf"
done
```

---

## 🛠️ Using the Provided Script

### Available Commands

```bash
./export-diagrams.sh setup      # Install mermaid-cli
./export-diagrams.sh single     # Export single diagram (interactive)
./export-diagrams.sh all        # Export all to PNG
./export-diagrams.sh svg        # Export all to SVG
./export-diagrams.sh pdf        # Export all to PDF
./export-diagrams.sh hq         # Export high-quality 4K
./export-diagrams.sh key        # Export key diagrams
./export-diagrams.sh theme      # Export with custom theme
./export-diagrams.sh batch      # Batch with custom options
./export-diagrams.sh clean      # Remove generated files
./export-diagrams.sh help       # Show help
```

### Examples

```bash
# Make script executable (first time only)
chmod +x export-diagrams.sh

# Export all diagrams to PNG
./export-diagrams.sh all

# Export specific diagram interactively
./export-diagrams.sh single

# Export high-quality versions
./export-diagrams.sh hq

# Clean up generated files
./export-diagrams.sh clean
```

---

## 📐 Recommended Dimensions

| Use Case | Width | Height | Command |
|----------|-------|--------|---------|
| **Web Display** | 1920 | 1080 | `mmdc -i file.mmd -o file.png -w 1920 -H 1080` |
| **Presentation** | 3000 | 2000 | `mmdc -i file.mmd -o file.png -w 3000 -H 2000` |
| **High Quality** | 5000 | 3500 | `mmdc -i file.mmd -o file.png -w 5000 -H 3500` |
| **Print/Poster** | 7680 | 4320 | `mmdc -i file.mmd -o file.png -w 7680 -H 4320` |

---

## 🎯 Format Comparison

| Format | Best For | Pros | Cons |
|--------|----------|------|------|
| **PNG** | Presentations, Documentation | Universal support, Good quality | Large file size, Raster |
| **SVG** | Web, Scalable graphics | Vector, Small size, Scalable | Limited support in some tools |
| **PDF** | Print, Professional docs | Vector, High quality, Universal | Larger than SVG |

---

## 💡 Pro Tips

### 1. Transparent Backgrounds for Overlays

```bash
mmdc -i diagram.mmd -o diagram.png -b transparent
```

Use this for:
- Presentations with custom backgrounds
- Web pages with dynamic themes
- Overlay on images

### 2. High DPI for Retina Displays

```bash
mmdc -i diagram.mmd -o diagram.png -w 3000 -H 2000 -s 2
```

Perfect for:
- MacBook Retina displays
- 4K monitors
- Mobile devices

### 3. Batch Export with Custom Function

Add to your `.bashrc` or `.zshrc`:

```bash
export_mermaid() {
    local input="$1"
    local base="${input%.mmd}"
    mmdc -i "$input" -o "${base}.png" -w 3000 -H 2000
    mmdc -i "$input" -o "${base}.svg"
    mmdc -i "$input" -o "${base}.pdf"
    echo "Exported: ${base}.{png,svg,pdf}"
}

# Usage: export_mermaid diagram.mmd
```

### 4. Export with Timestamp

```bash
timestamp=$(date +%Y%m%d_%H%M%S)
mmdc -i diagram.mmd -o "diagram_${timestamp}.png"
```

### 5. Create Export Directory

```bash
mkdir -p exports
for file in *.mmd; do
    mmdc -i "$file" -o "exports/${file%.mmd}.png" -w 3000 -H 2000
done
```

---

## 🔧 Advanced Configuration

### Custom Config File

Create `mermaid-config.json`:

```json
{
  "theme": "default",
  "themeVariables": {
    "primaryColor": "#1168bd",
    "primaryTextColor": "#fff",
    "primaryBorderColor": "#0a4d8c",
    "lineColor": "#1a8fff",
    "secondaryColor": "#087b7b",
    "tertiaryColor": "#052E56"
  },
  "flowchart": {
    "htmlLabels": true,
    "curve": "basis"
  }
}
```

Use with:

```bash
mmdc -i diagram.mmd -o diagram.png -c mermaid-config.json
```

### Puppeteer Config

Create `.puppeteerrc.json`:

```json
{
  "args": [
    "--no-sandbox",
    "--disable-setuid-sandbox"
  ]
}
```

---

## 🐛 Troubleshooting

### Error: mmdc command not found

```bash
# Install mermaid-cli
npm install -g @mermaid-js/mermaid-cli

# Check installation
which mmdc
mmdc --version
```

### Error: Puppeteer download failed

```bash
# Install puppeteer separately
npm install -g puppeteer

# Or set download path
export PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true
```

### Error: Out of memory

```bash
# Increase Node.js memory limit
export NODE_OPTIONS="--max-old-space-size=4096"

# Then retry export
mmdc -i large-diagram.mmd -o large-diagram.png
```

### Error: Parse error in diagram

```bash
# Validate syntax at Mermaid Live Editor
# https://mermaid.live/

# Check for common issues:
# - Missing quotes in labels
# - Invalid relationship syntax
# - Malformed entity definitions
```

### Slow export performance

```bash
# Export to SVG first (faster)
mmdc -i diagram.mmd -o diagram.svg

# Then convert SVG to PNG if needed (using ImageMagick)
convert diagram.svg diagram.png

# Or use rsvg-convert
rsvg-convert -w 3000 -h 2000 diagram.svg -o diagram.png
```

---

## 📦 Export Strategies

### Strategy 1: All Formats for Key Diagrams

```bash
key_diagrams=(
    "se-flow-lvl-2.mmd"
    "se-sequence-detailed.mmd"
    "se-deployment-k8s.mmd"
)

for diagram in "${key_diagrams[@]}"; do
    base="${diagram%.mmd}"
    mmdc -i "$diagram" -o "${base}.png" -w 3000 -H 2000
    mmdc -i "$diagram" -o "${base}.svg"
    mmdc -i "$diagram" -o "${base}.pdf"
done
```

### Strategy 2: SVG for All, PNG for Presentations

```bash
# Export all to SVG (fast, small)
for file in *.mmd; do
    mmdc -i "$file" -o "${file%.mmd}.svg"
done

# Export only presentation diagrams to PNG
presentation_diagrams=(
    "se-architecture-dashboard.mmd"
    "se-flow-lvl-3.mmd"
)

for diagram in "${presentation_diagrams[@]}"; do
    mmdc -i "$diagram" -o "${diagram%.mmd}.png" -w 3000 -H 2000
done
```

### Strategy 3: Version Control Friendly

```bash
# Create exports directory (gitignored)
mkdir -p exports

# Export all diagrams to exports folder
for file in *.mmd; do
    base="${file%.mmd}"
    mmdc -i "$file" -o "exports/${base}.png" -w 3000 -H 2000
done

# Add to .gitignore
echo "exports/" >> .gitignore
```

---

## 🔗 Integration Examples

### In CI/CD (GitHub Actions)

```yaml
name: Export Diagrams

on: [push]

jobs:
  export:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Setup Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '18'
      
      - name: Install mermaid-cli
        run: npm install -g @mermaid-js/mermaid-cli
      
      - name: Export diagrams
        run: |
          cd docs/mermaid
          for file in *.mmd; do
            mmdc -i "$file" -o "${file%.mmd}.png" -w 3000 -H 2000
          done
      
      - name: Upload artifacts
        uses: actions/upload-artifact@v2
        with:
          name: diagrams
          path: docs/mermaid/*.png
```

### In Makefile

```makefile
.PHONY: diagrams diagrams-clean

MERMAID_FILES := $(wildcard docs/mermaid/*.mmd)
PNG_FILES := $(MERMAID_FILES:.mmd=.png)

diagrams: $(PNG_FILES)

%.png: %.mmd
	mmdc -i $< -o $@ -w 3000 -H 2000

diagrams-clean:
	rm -f docs/mermaid/*.png docs/mermaid/*.svg docs/mermaid/*.pdf
```

### In NPM Scripts

```json
{
  "scripts": {
    "diagrams:png": "for file in docs/mermaid/*.mmd; do mmdc -i \"$file\" -o \"${file%.mmd}.png\" -w 3000 -H 2000; done",
    "diagrams:svg": "for file in docs/mermaid/*.mmd; do mmdc -i \"$file\" -o \"${file%.mmd}.svg\"; done",
    "diagrams:clean": "rm -f docs/mermaid/*.{png,svg,pdf}"
  }
}
```

---

## 📚 Quick Reference Card

```
╔══════════════════════════════════════════════════════════════╗
║              MERMAID EXPORT QUICK REFERENCE                  ║
╚══════════════════════════════════════════════════════════════╝

INSTALLATION
  npm install -g @mermaid-js/mermaid-cli

BASIC EXPORT
  mmdc -i input.mmd -o output.png         # PNG
  mmdc -i input.mmd -o output.svg         # SVG
  mmdc -i input.mmd -o output.pdf         # PDF

WITH OPTIONS
  -w <width>         Width in pixels
  -H <height>        Height in pixels
  -t <theme>         Theme (default, dark, forest, neutral)
  -b <color>         Background color
  -s <scale>         Scale factor (1, 2, 3)
  -c <config>        Config file path

BATCH EXPORT
  for f in *.mmd; do mmdc -i "$f" -o "${f%.mmd}.png"; done

RECOMMENDED SIZES
  Web:          1920 x 1080
  Presentation: 3000 x 2000
  High Quality: 5000 x 3500
  Print:        7680 x 4320

SCRIPT SHORTCUTS
  ./export-diagrams.sh all    # All to PNG
  ./export-diagrams.sh svg    # All to SVG
  ./export-diagrams.sh hq     # 4K quality
```

---

## 📖 Additional Resources

- **Mermaid Documentation**: https://mermaid.js.org/
- **Mermaid CLI GitHub**: https://github.com/mermaid-js/mermaid-cli
- **Mermaid Live Editor**: https://mermaid.live/
- **Mermaid Themes**: https://mermaid.js.org/config/theming.html

---

**Last Updated**: February 2026  
**Maintained By**: Platform Architecture Team

