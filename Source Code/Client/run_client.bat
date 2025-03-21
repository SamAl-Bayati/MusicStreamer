@echo off
REM run_client.bat - Runs the Client application

set JAVAFX_LIB="lib\javafx-sdk-21.0.5\lib"

echo Starting Client...
java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.media Client

if %ERRORLEVEL% neq 0 (
    echo Client encountered an error.
    pause
    exit /b %ERRORLEVEL%
)

pause
