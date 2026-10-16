#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "[*] Launching CherrygramNB build in Docker container..."
docker compose -f docker/docker-compose.yml up --build --abort-on-container-exit
