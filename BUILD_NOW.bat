@echo off
echo ========================================
echo PvP Client Enhanced Build Script
echo ========================================
echo.
echo Checking for Java installation...
java -version 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo.
    echo Please install Java 25 from:
    echo https://download.oracle.com/java/25/archive/jdk-25.0.4_windows-x64_bin.exe
    echo.
    pause
    exit /b 1
)

echo.
echo Java found! Starting build...
echo.

setlocal
set GRADLE_HOME=%CD%\gradle_temp\gradle-9.5.0
set PATH=%GRADLE_HOME%\bin;%PATH%

%GRADLE_HOME%\bin\gradle.bat build

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo JAR file location: build\libs\aleksa04_1-1.4.0.jar
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD FAILED
    echo ========================================
    echo.
    echo Check the error messages above.
    echo.
)

endlocal
pause
