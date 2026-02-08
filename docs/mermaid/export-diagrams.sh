#!/bin/bash

################################################################################
# 🎨 MERMAID DIAGRAM EXPORT CHEAT SHEET
################################################################################
# Export Mermaid diagrams to PNG, SVG, and PDF formats
#
# Prerequisites:
#   npm install -g @mermaid-js/mermaid-cli
#
# Usage:
#   chmod +x export-diagrams.sh
#   ./export-diagrams.sh [command]
#
# Commands:
#   setup     - Install mermaid-cli
#   single    - Export single diagram (interactive)
#   all       - Export all .mmd files to PNG
#   svg       - Export all .mmd files to SVG
#   pdf       - Export all .mmd files to PDF
#   hq        - Export high-quality images (4K)
#   clean     - Remove all generated images
#   help      - Show this help
################################################################################

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default settings
DEFAULT_WIDTH=3000
DEFAULT_HEIGHT=2000
HQ_WIDTH=5000
HQ_HEIGHT=3500

################################################################################
# HELPER FUNCTIONS
################################################################################

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

check_mmdc() {
    if ! command -v mmdc &> /dev/null; then
        print_error "mermaid-cli (mmdc) not found!"
        print_info "Run: ./export-diagrams.sh setup"
        exit 1
    fi
}

################################################################################
# SETUP - Install mermaid-cli
################################################################################

setup() {
    print_header "Installing Mermaid CLI"

    if command -v npm &> /dev/null; then
        echo "Installing @mermaid-js/mermaid-cli globally..."
        npm install -g @mermaid-js/mermaid-cli
        print_success "Mermaid CLI installed successfully!"

        # Verify installation
        if command -v mmdc &> /dev/null; then
            print_success "mmdc command available"
            mmdc --version
        fi
    else
        print_error "npm not found! Please install Node.js first"
        echo "Visit: https://nodejs.org/"
        exit 1
    fi
}

################################################################################
# EXPORT SINGLE DIAGRAM
################################################################################

export_single() {
    check_mmdc
    print_header "Export Single Diagram"

    # List available .mmd files
    echo "Available diagrams:"
    echo ""
    ls -1 *.mmd 2>/dev/null | nl
    echo ""

    read -p "Enter diagram filename (e.g., se-flow-lvl-2.mmd): " filename

    if [ ! -f "$filename" ]; then
        print_error "File not found: $filename"
        exit 1
    fi

    echo ""
    echo "Select format:"
    echo "1) PNG"
    echo "2) SVG"
    echo "3) PDF"
    echo "4) All formats"
    read -p "Enter choice [1-4]: " format_choice

    basename="${filename%.mmd}"

    case $format_choice in
        1)
            print_info "Exporting to PNG..."
            mmdc -i "$filename" -o "${basename}.png" -w $DEFAULT_WIDTH -H $DEFAULT_HEIGHT
            print_success "Created: ${basename}.png"
            ;;
        2)
            print_info "Exporting to SVG..."
            mmdc -i "$filename" -o "${basename}.svg"
            print_success "Created: ${basename}.svg"
            ;;
        3)
            print_info "Exporting to PDF..."
            mmdc -i "$filename" -o "${basename}.pdf"
            print_success "Created: ${basename}.pdf"
            ;;
        4)
            print_info "Exporting to all formats..."
            mmdc -i "$filename" -o "${basename}.png" -w $DEFAULT_WIDTH -H $DEFAULT_HEIGHT
            mmdc -i "$filename" -o "${basename}.svg"
            mmdc -i "$filename" -o "${basename}.pdf"
            print_success "Created: ${basename}.png, ${basename}.svg, ${basename}.pdf"
            ;;
        *)
            print_error "Invalid choice"
            exit 1
            ;;
    esac
}

################################################################################
# EXPORT ALL DIAGRAMS TO PNG
################################################################################

export_all_png() {
    check_mmdc
    print_header "Exporting All Diagrams to PNG"

    count=0
    for file in *.mmd; do
        if [ -f "$file" ]; then
            basename="${file%.mmd}"
            print_info "Processing: $file"
            mmdc -i "$file" -o "${basename}.png" -w $DEFAULT_WIDTH -H $DEFAULT_HEIGHT
            print_success "Created: ${basename}.png"
            ((count++))
        fi
    done

    if [ $count -eq 0 ]; then
        print_error "No .mmd files found!"
    else
        print_success "Exported $count diagram(s) to PNG"
    fi
}

################################################################################
# EXPORT ALL DIAGRAMS TO SVG
################################################################################

export_all_svg() {
    check_mmdc
    print_header "Exporting All Diagrams to SVG"

    count=0
    for file in *.mmd; do
        if [ -f "$file" ]; then
            basename="${file%.mmd}"
            print_info "Processing: $file"
            mmdc -i "$file" -o "${basename}.svg"
            print_success "Created: ${basename}.svg"
            ((count++))
        fi
    done

    if [ $count -eq 0 ]; then
        print_error "No .mmd files found!"
    else
        print_success "Exported $count diagram(s) to SVG"
    fi
}

################################################################################
# EXPORT ALL DIAGRAMS TO PDF
################################################################################

export_all_pdf() {
    check_mmdc
    print_header "Exporting All Diagrams to PDF"

    count=0
    for file in *.mmd; do
        if [ -f "$file" ]; then
            basename="${file%.mmd}"
            print_info "Processing: $file"
            mmdc -i "$file" -o "${basename}.pdf"
            print_success "Created: ${basename}.pdf"
            ((count++))
        fi
    done

    if [ $count -eq 0 ]; then
        print_error "No .mmd files found!"
    else
        print_success "Exported $count diagram(s) to PDF"
    fi
}

################################################################################
# EXPORT HIGH-QUALITY (4K) IMAGES
################################################################################

export_hq() {
    check_mmdc
    print_header "Exporting High-Quality (4K) Images"

    count=0
    for file in *.mmd; do
        if [ -f "$file" ]; then
            basename="${file%.mmd}"
            print_info "Processing: $file"
            mmdc -i "$file" -o "${basename}-4k.png" -w $HQ_WIDTH -H $HQ_HEIGHT
            print_success "Created: ${basename}-4k.png"
            ((count++))
        fi
    done

    if [ $count -eq 0 ]; then
        print_error "No .mmd files found!"
    else
        print_success "Exported $count high-quality diagram(s)"
    fi
}

################################################################################
# EXPORT WITH CUSTOM THEME
################################################################################

export_with_theme() {
    check_mmdc
    print_header "Export with Custom Theme"

    echo "Available themes:"
    echo "1) default"
    echo "2) dark"
    echo "3) forest"
    echo "4) neutral"
    read -p "Select theme [1-4]: " theme_choice

    case $theme_choice in
        1) theme="default" ;;
        2) theme="dark" ;;
        3) theme="forest" ;;
        4) theme="neutral" ;;
        *)
            print_error "Invalid choice"
            exit 1
            ;;
    esac

    count=0
    for file in *.mmd; do
        if [ -f "$file" ]; then
            basename="${file%.mmd}"
            print_info "Processing: $file (theme: $theme)"
            mmdc -i "$file" -o "${basename}-${theme}.png" -t $theme -w $DEFAULT_WIDTH -H $DEFAULT_HEIGHT
            print_success "Created: ${basename}-${theme}.png"
            ((count++))
        fi
    done

    print_success "Exported $count diagram(s) with $theme theme"
}

################################################################################
# EXPORT SPECIFIC DIAGRAMS
################################################################################

export_key_diagrams() {
    check_mmdc
    print_header "Exporting Key Diagrams"

    key_diagrams=(
        "se-flow-lvl-2.mmd"
        "se-sequence-detailed.mmd"
        "se-deployment-k8s.mmd"
        "se-architecture-dashboard.mmd"
        "se-job-lifecycle.mmd"
        "se-database-schema.mmd"
    )

    count=0
    for file in "${key_diagrams[@]}"; do
        if [ -f "$file" ]; then
            basename="${file%.mmd}"
            print_info "Exporting: $file"

            # PNG
            mmdc -i "$file" -o "${basename}.png" -w $DEFAULT_WIDTH -H $DEFAULT_HEIGHT

            # SVG
            mmdc -i "$file" -o "${basename}.svg"

            # PDF
            mmdc -i "$file" -o "${basename}.pdf"

            print_success "Created: ${basename}.{png,svg,pdf}"
            ((count++))
        else
            print_info "Skipping (not found): $file"
        fi
    done

    print_success "Exported $count key diagram(s) in all formats"
}

################################################################################
# CLEAN GENERATED FILES
################################################################################

clean() {
    print_header "Cleaning Generated Files"

    echo "This will delete all .png, .svg, and .pdf files in the current directory."
    read -p "Are you sure? (y/N): " confirm

    if [[ $confirm =~ ^[Yy]$ ]]; then
        count=0

        for ext in png svg pdf; do
            for file in *.$ext; do
                if [ -f "$file" ]; then
                    rm "$file"
                    print_info "Deleted: $file"
                    ((count++))
                fi
            done
        done

        if [ $count -eq 0 ]; then
            print_info "No generated files found"
        else
            print_success "Deleted $count file(s)"
        fi
    else
        print_info "Cancelled"
    fi
}

################################################################################
# BATCH EXPORT WITH OPTIONS
################################################################################

batch_export() {
    check_mmdc
    print_header "Batch Export with Custom Options"

    read -p "Width (default: $DEFAULT_WIDTH): " width
    width=${width:-$DEFAULT_WIDTH}

    read -p "Height (default: $DEFAULT_HEIGHT): " height
    height=${height:-$DEFAULT_HEIGHT}

    read -p "Background color (default: white): " bgcolor
    bgcolor=${bgcolor:-white}

    echo ""
    echo "Exporting with options:"
    echo "  Width: ${width}px"
    echo "  Height: ${height}px"
    echo "  Background: $bgcolor"
    echo ""

    count=0
    for file in *.mmd; do
        if [ -f "$file" ]; then
            basename="${file%.mmd}"
            print_info "Processing: $file"
            mmdc -i "$file" -o "${basename}.png" -w $width -H $height -b $bgcolor
            print_success "Created: ${basename}.png"
            ((count++))
        fi
    done

    print_success "Exported $count diagram(s) with custom options"
}

################################################################################
# SHOW HELP
################################################################################

show_help() {
    cat << EOF
${BLUE}
╔══════════════════════════════════════════════════════════════╗
║        🎨 MERMAID DIAGRAM EXPORT CHEAT SHEET                ║
╚══════════════════════════════════════════════════════════════╝
${NC}

${GREEN}COMMANDS:${NC}
  setup         Install mermaid-cli globally
  single        Export single diagram (interactive)
  all           Export all diagrams to PNG (3000x2000)
  svg           Export all diagrams to SVG
  pdf           Export all diagrams to PDF
  hq            Export high-quality 4K images (5000x3500)
  key           Export key diagrams to all formats
  theme         Export with custom theme
  batch         Batch export with custom options
  clean         Remove all generated image files
  help          Show this help message

${GREEN}EXAMPLES:${NC}
  ${YELLOW}# Install mermaid-cli${NC}
  ./export-diagrams.sh setup

  ${YELLOW}# Export single diagram${NC}
  ./export-diagrams.sh single

  ${YELLOW}# Export all diagrams to PNG${NC}
  ./export-diagrams.sh all

  ${YELLOW}# Export all diagrams to SVG${NC}
  ./export-diagrams.sh svg

  ${YELLOW}# Export high-quality images${NC}
  ./export-diagrams.sh hq

  ${YELLOW}# Export key diagrams${NC}
  ./export-diagrams.sh key

  ${YELLOW}# Clean all generated files${NC}
  ./export-diagrams.sh clean

${GREEN}DIRECT MMDC COMMANDS:${NC}
  ${YELLOW}# Basic PNG export${NC}
  mmdc -i diagram.mmd -o diagram.png

  ${YELLOW}# Custom dimensions${NC}
  mmdc -i diagram.mmd -o diagram.png -w 4000 -H 3000

  ${YELLOW}# Export to SVG${NC}
  mmdc -i diagram.mmd -o diagram.svg

  ${YELLOW}# Export to PDF${NC}
  mmdc -i diagram.mmd -o diagram.pdf

  ${YELLOW}# With custom theme${NC}
  mmdc -i diagram.mmd -o diagram.png -t dark

  ${YELLOW}# With background color${NC}
  mmdc -i diagram.mmd -o diagram.png -b transparent

  ${YELLOW}# High quality${NC}
  mmdc -i diagram.mmd -o diagram.png -w 5000 -H 3500 -s 2

${GREEN}AVAILABLE THEMES:${NC}
  - default
  - dark
  - forest
  - neutral

${GREEN}BACKGROUND OPTIONS:${NC}
  - white (default)
  - transparent
  - #hexcolor (e.g., #f0f0f0)

${GREEN}FILES:${NC}
  Input:  *.mmd (Mermaid diagram source files)
  Output: *.png, *.svg, *.pdf

${GREEN}TIPS:${NC}
  • Use SVG for web (scalable, smaller file size)
  • Use PNG for presentations (compatibility)
  • Use PDF for print (high quality, vector)
  • Use transparent background for overlays
  • 4K exports are great for posters/large displays

${GREEN}TROUBLESHOOTING:${NC}
  • If mmdc fails: npm install -g @mermaid-js/mermaid-cli
  • If puppeteer fails: npm install -g puppeteer
  • Memory issues: export NODE_OPTIONS="--max-old-space-size=4096"

${BLUE}Documentation: https://github.com/mermaid-js/mermaid-cli${NC}

EOF
}

################################################################################
# QUICK REFERENCE CARD
################################################################################

quick_reference() {
    cat << 'EOF'
╔══════════════════════════════════════════════════════════════════════════════╗
║                     📋 MERMAID EXPORT QUICK REFERENCE                        ║
╚══════════════════════════════════════════════════════════════════════════════╝

┌─ INSTALLATION ──────────────────────────────────────────────────────────────┐
│ npm install -g @mermaid-js/mermaid-cli                                      │
│ # OR                                                                         │
│ ./export-diagrams.sh setup                                                  │
└──────────────────────────────────────────────────────────────────────────────┘

┌─ BASIC COMMANDS ────────────────────────────────────────────────────────────┐
│ mmdc -i input.mmd -o output.png              # PNG export                   │
│ mmdc -i input.mmd -o output.svg              # SVG export                   │
│ mmdc -i input.mmd -o output.pdf              # PDF export                   │
└──────────────────────────────────────────────────────────────────────────────┘

┌─ WITH OPTIONS ──────────────────────────────────────────────────────────────┐
│ mmdc -i file.mmd -o file.png -w 3000 -H 2000   # Custom size              │
│ mmdc -i file.mmd -o file.png -t dark            # Dark theme               │
│ mmdc -i file.mmd -o file.png -b transparent     # Transparent bg           │
│ mmdc -i file.mmd -o file.png -s 2               # Scale 2x                 │
└──────────────────────────────────────────────────────────────────────────────┘

┌─ BATCH EXPORT ──────────────────────────────────────────────────────────────┐
│ for f in *.mmd; do mmdc -i "$f" -o "${f%.mmd}.png"; done                   │
│ for f in *.mmd; do mmdc -i "$f" -o "${f%.mmd}.svg"; done                   │
│ for f in *.mmd; do mmdc -i "$f" -o "${f%.mmd}.pdf"; done                   │
└──────────────────────────────────────────────────────────────────────────────┘

┌─ SCRIPT SHORTCUTS ──────────────────────────────────────────────────────────┐
│ ./export-diagrams.sh all        # All to PNG                               │
│ ./export-diagrams.sh svg        # All to SVG                               │
│ ./export-diagrams.sh pdf        # All to PDF                               │
│ ./export-diagrams.sh hq         # High quality 4K                          │
│ ./export-diagrams.sh key        # Key diagrams only                        │
└──────────────────────────────────────────────────────────────────────────────┘

┌─ RECOMMENDED SIZES ─────────────────────────────────────────────────────────┐
│ Web display:        -w 1920 -H 1080   (Full HD)                            │
│ Presentation:       -w 3000 -H 2000   (Standard)                           │
│ High quality:       -w 5000 -H 3500   (4K)                                 │
│ Print/Poster:       -w 7680 -H 4320   (8K)                                 │
└──────────────────────────────────────────────────────────────────────────────┘

EOF
}

################################################################################
# MAIN SCRIPT
################################################################################

case "${1:-help}" in
    setup)
        setup
        ;;
    single)
        export_single
        ;;
    all)
        export_all_png
        ;;
    svg)
        export_all_svg
        ;;
    pdf)
        export_all_pdf
        ;;
    hq)
        export_hq
        ;;
    key)
        export_key_diagrams
        ;;
    theme)
        export_with_theme
        ;;
    batch)
        batch_export
        ;;
    clean)
        clean
        ;;
    quick|ref|reference)
        quick_reference
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac

