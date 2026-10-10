@echo off
setlocal
set GRADLE_HOME=%CD%\gradle_temp\gradle-9.5.0
set PATH=%GRADLE_HOME%\bin;%PATH%
echo Using Gradle from: %GRADLE_HOME%
%GRADLE_HOME%\bin\gradle.bat build
endlocal
pause
