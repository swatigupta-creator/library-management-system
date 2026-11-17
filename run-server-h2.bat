@echo off
title Library Management System Server (H2 Mode)
cls
echo ========================================================
echo   Starting Library Management System (Zero-Setup Mode)
echo ========================================================
echo.
cd /d "%~dp0"

set "IDEA_MVN=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd"

where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Running with H2 database...
    call mvn spring-boot:run -Dspring-boot.run.profiles=h2
    goto done
)

if exist "%IDEA_MVN%" (
    echo [INFO] Running with IntelliJ Maven and H2 database...
    call "%IDEA_MVN%" spring-boot:run -Dspring-boot.run.profiles=h2
    goto done
)

echo [ERROR] Maven not found. Please open in IntelliJ IDEA!

:done
pause
