#!/usr/bin/env bash

# chmod +x scripts/export-env.sh && ./scripts/export-env.sh

# Load environment variables from the project's .env file into the current shell
# Usage: source scripts/export-env.sh
set -euo pipefail
# Resolve the directory of this script in a way that works when executed or sourced in bash or zsh
if [ -n "${BASH_SOURCE-}" ]; then
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
elif [ -n "${ZSH_VERSION-}" ]; then
  # zsh: ${(%):-%x} expands to the current script path when sourced
  SCRIPT_DIR="$(cd "$(dirname "${(%):-%x}")" && pwd)"
else
  SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
fi
ENV_FILE="$SCRIPT_DIR/../.env"
if [ ! -f "$ENV_FILE" ]; then
  echo "No .env file found at $ENV_FILE"
  return 1 2>/dev/null || exit 1
fi
# Export all variables defined in .env for child processes while sourcing
# This is safe for simple KEY=VAL lines. Comments and blank lines are ignored by 'source'.
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a
echo ".env loaded from $ENV_FILE (variables exported)"
# Print a few variables for quick verification
if [ -n "${vxx-}" ]; then
  printf "vxx=%s\n" "${vxx}"
else
  echo "vxx is not set in $ENV_FILE"
fi
