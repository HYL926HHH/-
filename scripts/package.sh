#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q -DskipTests package
mkdir -p dist
cp -f target/gomoku-idea-1.0.0.jar dist/gomoku-idea-1.0.0.jar
cp -f target/gomoku-idea-1.0.0-bin.zip dist/gomoku-run.zip
cp -f target/gomoku-idea-1.0.0-src.zip dist/gomoku-code.zip
echo "已生成："
ls -lh target/gomoku-idea-1.0.0.jar target/gomoku-idea-1.0.0-bin.zip target/gomoku-idea-1.0.0-src.zip
ls -lh dist/gomoku-run.zip dist/gomoku-code.zip
