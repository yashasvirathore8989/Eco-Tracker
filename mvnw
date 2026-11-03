#!/bin/sh
if command -v mvn >/dev/null 2>&1; then
  mvn "$@"
else
  echo "Maven is not installed. Please install Maven or run via IDE."
  exit 1
fi
