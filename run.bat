@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "JAR_PATH=%SCRIPT_DIR%target\playtrack-app-1.0-SNAPSHOT.jar"

if not exist "%JAR_PATH%" (
    echo Build artifact not found. Building PlayTrack...
    call "%SCRIPT_DIR%mvnw.cmd" -DskipTests package
    if errorlevel 1 (
        echo Build failed.
        exit /b 1
    )
)

echo Starting PlayTrack...
java -jar "%JAR_PATH%"
