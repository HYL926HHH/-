#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q -DskipTests package
echo "已生成："
ls -lh target/gomoku-idea-1.0.0.jar target/gomoku-idea-1.0.0-bin.zip target/gomoku-idea-1.0.0-src.zip
