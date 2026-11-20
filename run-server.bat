@echo off
title Library Management System Server (Spring Boot + MySQL)
cls
echo ========================================================
echo     Library Management System - Spring Boot + MySQL
echo ========================================================
echo.
cd /d "%~dp0"

REM 1. Set JAVA_HOME if not already set
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Java\jdk-26" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26"
    ) else if exist "C:\Program Files\Java\latest" (
        set "JAVA_HOME=C:\Program Files\Java\latest"
    )
)

if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo [INFO] Using JAVA_HOME: %JAVA_HOME%
)

set "IDEA_MVN=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd"

REM 2. Run with Maven
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Running via System Maven...
    echo [INFO] Website URL: http://localhost:8080
    echo [INFO] Books Catalog: http://localhost:8080/books.html
    echo [INFO] Librarian Portal: http://localhost:8080/librarian.html (user: librarian / pass: admin123)
    echo.
    call mvn spring-boot:run
    goto done
)

if exist "%IDEA_MVN%" (
    echo [INFO] Running via IntelliJ IDEA Bundled Maven...
    echo [INFO] Website URL: http://localhost:8080
    echo [INFO] Books Catalog: http://localhost:8080/books.html
    echo [INFO] Librarian Portal: http://localhost:8080/librarian.html (user: librarian / pass: admin123)
    echo.
    call "%IDEA_MVN%" spring-boot:run
    goto done
)

echo [ERROR] Maven was not found in PATH or standard IntelliJ location.
echo [TIP] You can open this project folder in IntelliJ IDEA / Eclipse / VS Code and run LibraryManagementApplication.java directly.
echo.

:done
pause
