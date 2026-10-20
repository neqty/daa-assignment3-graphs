#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
./mvnw --batch-mode verify
java -Xmx3g -jar target/assignment3-graphs-1.0.jar
"${PYTHON:-python3}" scripts/plot_results.py
