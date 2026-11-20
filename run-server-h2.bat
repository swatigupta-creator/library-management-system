@echo off
title Library Management System Server (H2 In-Memory Demo)
cls
echo ========================================================
echo     Library Management System (H2 In-Memory Fallback)
echo ========================================================
echo.
cd /d "%~dp0"

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

where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Starting with H2 profile...
    echo [INFO] Website URL: http://localhost:8080
    echo [INFO] Librarian Portal: http://localhost:8080/librarian.html (user: librarian / pass: admin123)
    echo.
    call mvn spring-boot:run -Dspring-boot.run.profiles=h2
    goto done
)

if exist "%IDEA_MVN%" (
    echo [INFO] Starting with H2 profile via IntelliJ Maven...
    echo [INFO] Website URL: http://localhost:8080
    echo [INFO] Librarian Portal: http://localhost:8080/librarian.html (user: librarian / pass: admin123)
    echo.
    call "%IDEA_MVN%" spring-boot:run -Dspring-boot.run.profiles=h2
    goto done
)

echo [ERROR] Maven not found.
:done
pause
