#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
java -cp "$SCRIPT_DIR/bin:$SCRIPT_DIR/lib/*" com.playtrack.app.Main
