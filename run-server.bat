@echo off
title Library Management System Server (MongoDB)
cls
echo ========================================================
echo     Library Management System - Spring Boot + MongoDB
echo ========================================================
echo.
cd /d "%~dp0"

set "IDEA_MVN=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd"

where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Using System Maven...
    call mvn spring-boot:run
    goto done
)

if exist "%IDEA_MVN%" (
    echo [INFO] Using IntelliJ IDEA Bundled Maven...
    call "%IDEA_MVN%" spring-boot:run
    goto done
)

echo [ERROR] Maven not found in PATH or standard IntelliJ location.
echo [TIP] Open IntelliJ IDEA, open this project folder, and click Run on LibraryManagementApplication.java!
echo.

:done
pause
