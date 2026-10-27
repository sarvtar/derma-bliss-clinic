@echo off
title MySQL 8.4 Server - Derma Bliss Clinic
echo ========================================================
echo MySQL 8.4 Server is RUNNING on port 3306...
echo Data Directory: %~dp0mysql-data
echo ========================================================
echo.
echo Leave this window OPEN while working on the project.
echo To STOP MySQL: Press Ctrl+C in this window.
echo.
"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --datadir="%~dp0mysql-data" --console
pause
