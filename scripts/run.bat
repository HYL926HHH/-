@echo off
cd /d "%~dp0"
java -jar gomoku-idea-1.0.0.jar
if errorlevel 1 pause
