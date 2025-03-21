@echo off
REM build_server.bat - Compiles the Server application

echo Compiling Server...
javac -cp ".;lib/*" *.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Server compiled successfully.
pause
