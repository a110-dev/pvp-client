@echo off
echo ========================================
echo PvP Client - GitHub Setup Helper
echo ========================================
echo.
echo This script will help you upload the code to GitHub
echo so it can be built automatically.
echo.
echo IMPORTANT: You need to create a GitHub repository first!
echo.
echo Steps:
echo 1. Go to https://github.com/new
echo 2. Create a new repository (name it anything you want)
echo 3. Copy the repository URL (it looks like: https://github.com/username/repo.git)
echo 4. Paste it below when asked
echo.
pause

echo.
set /p REPO_URL="Enter your GitHub repository URL: "

echo.
echo Checking for Git installation...
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Git is not installed
    echo.
    echo Please install Git from: https://git-scm.com/download/win
    echo.
    pause
    exit /b 1
)

echo Git found!
echo.
echo Initializing Git repository...
git init

echo.
echo Adding files to Git...
git add .

echo.
echo Creating initial commit...
git commit -m "Add enhanced PvP client with resize handles and new modules"

echo.
echo Adding remote repository...
git remote add origin %REPO_URL%

echo.
echo Pushing to GitHub...
git branch -M main
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo SUCCESS!
    echo ========================================
    echo.
    echo Your code is now on GitHub!
    echo.
    echo Next steps:
    echo 1. Go to your repository on GitHub
    echo 2. Click the "Actions" tab
    echo 3. Click "Build PvP Client" workflow
    echo 4. Click "Run workflow" button
    echo 5. Wait for the build to complete
    echo 6. Download the JAR from the Artifacts section
    echo.
) else (
    echo.
    echo ========================================
    echo PUSH FAILED
    echo ========================================
    echo.
    echo You may need to authenticate with GitHub.
    echo Try running these commands manually:
    echo.
    echo git remote add origin %REPO_URL%
    echo git branch -M main
    echo git push -u origin main
    echo.
)

pause
