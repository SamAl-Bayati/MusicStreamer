@echo off
REM run_server.bat - Runs the Server application

echo Starting Server...
java -cp ".;lib/*" Server

if %ERRORLEVEL% neq 0 (
    echo Server encountered an error.
    pause
    exit /b %ERRORLEVEL%
)

pause
