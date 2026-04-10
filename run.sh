#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_PATH="$SCRIPT_DIR/target/playtrack-app-1.0-SNAPSHOT.jar"

if [ ! -f "$JAR_PATH" ]; then
  echo "Build artifact not found. Building PlayTrack..."
  "$SCRIPT_DIR/mvnw" -DskipTests package
fi

echo "Starting PlayTrack..."
java -jar "$JAR_PATH"
