@echo off
echo Stopping MySQL Server...
taskkill /F /IM mysqld.exe >nul 2>&1
echo [OK] MySQL Server stopped.

