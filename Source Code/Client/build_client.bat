@echo off
REM build_client.bat - Compiles the Client application

set JAVAFX_LIB="lib\javafx-sdk-21.0.5\lib"

echo Compiling Client...
javac --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.media *.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Client compiled successfully.
pause
