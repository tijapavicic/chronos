#!/usr/bin/env bash
# Run the Chronos Postman collection via npm script (newman)
set -euo pipefail

# Ensure node_modules/.bin is available
if [ ! -f "package.json" ]; then
  echo "package.json not found in project root."
  exit 1
fi

# Install dependencies if node_modules is missing
if [ ! -d "node_modules" ]; then
  echo "Installing npm devDependencies (newman)..."
  npm ci
fi

# Run the collection
npm run postman
