@echo off
title BBAU Lucknow - Library Management System (JavaFX Desktop)
echo ==============================================================
echo Babasaheb Bhimrao Ambedkar University, Lucknow
echo Gautam Buddha Central Library Management System (JavaFX)
echo ==============================================================
echo.
echo Launching JavaFX Desktop Application...
echo.

set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd" (
        set "MVN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd"
    )
)

call "%MVN_CMD%" spring-boot:run
pause
