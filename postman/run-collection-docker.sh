#!/usr/bin/env bash
# Run the Postman collection using a newman Docker image so Node isn't required locally.
set -euo pipefail

COLLECTION=postman/Chronos.postman_collection.json
ENV=postman/Chronos.postman_environment.json
BASE_URL=${BASE_URL:-http://host.docker.internal:8080}

# Ensure collection and environment exist
if [ ! -f "$COLLECTION" ]; then
  echo "Collection file not found: $COLLECTION"
  exit 1
fi
if [ ! -f "$ENV" ]; then
  echo "Environment file not found: $ENV"
  exit 1
fi

# Pull and run newman docker image
docker run --rm -v "$PWD:/etc/newman" postman/newman:alpine \
  run /etc/newman/$COLLECTION -e /etc/newman/$ENV --env-var "baseUrl=$BASE_URL" --timeout-request 10000
