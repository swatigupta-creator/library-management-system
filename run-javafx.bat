@echo off
title BBAU Lucknow - Library Management System (JavaFX Desktop)
echo ==============================================================
echo Babasaheb Bhimrao Ambedkar University, Lucknow
echo Gautam Buddha Central Library Management System (JavaFX)
echo ==============================================================
echo.
echo Launching JavaFX Desktop Application...
echo.
call mvn clean compile spring-boot:run
pause
