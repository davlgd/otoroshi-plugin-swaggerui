#!/bin/bash
set -e

echo "Building plugin..."
sbt clean compile package

echo "Restarting Docker..."
docker compose down && docker compose up -d

echo "Done! Plugin updated and Otoroshi restarted."
