@echo off
rem One-click launcher for AI Mental Platform frontend (Vite dev server).
rem Double-click this file, wait ~3 seconds, browser opens http://localhost:5173 automatically.
title AI Mental Platform - Frontend
cd /d "%~dp0"
if not exist "node_modules" (
  echo [INFO] node_modules not found, installing dependencies first...
  call npm.cmd install
)
echo [INFO] Starting Vite dev server, browser will open automatically...
npm.cmd run dev
rem Keep the window open if startup fails, so error messages are visible.
pause
