# Build PvP Client using GitHub Actions

This guide shows you how to build the enhanced PvP Client JAR using GitHub Actions - no need to install Java locally!

## 🚀 Quick Start

### Option 1: Use My Prepared Repository (Easiest)

1. **Create a new GitHub repository**
   - Go to https://github.com/new
   - Name it something like `pvp-client-enhanced`
   - Make it Public or Private (your choice)
   - Click "Create repository"

2. **Upload the code**
   - Go to your new repository
   - Click "uploading an existing file"
   - Drag and drop all files from: `c:\Users\User\Documents\Codex\2026-10-09\mak\extracted_source`
   - **Important:** Exclude the `gradle_temp` folder
   - Click "Commit changes"

3. **Trigger the build**
   - Go to the "Actions" tab in your repository
   - Click on "Build PvP Client" workflow
   - Click "Run workflow" → "Run workflow"
   - Wait for the build to complete (green checkmark = success)

4. **Download the JAR**
   - Scroll down to the "Artifacts" section at the bottom
   - Download `aleksa04-1-4-0-enhanced`
   - Extract the ZIP to get your JAR file

### Option 2: Command Line (Git Required)

If you have Git installed:

```bash
cd "c:\Users\User\Documents\Codex\2026-10-09\mak\extracted_source"

# Initialize git (if not already done)
git init

# Add all files (excluding gradle_temp)
git add .
git commit -m "Add enhanced PvP client"

# Create a new repository on GitHub first, then:
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git branch -M main
git push -u origin main
```

Then go to GitHub Actions and run the workflow as shown in Option 1.

## 📋 What the Build Does

The GitHub Actions workflow will:
1. ✅ Check out your code
2. ✅ Set up Java 25 automatically
3. ✅ Build the project with Gradle
4. ✅ Upload the JAR as a downloadable artifact
5. ✅ Keep the artifact available for 30 days

## 🔧 Manual Build Trigger

You can also trigger builds manually:
1. Go to Actions tab
2. Click "Build PvP Client"
3. Click "Run workflow" button
4. Select branch (usually "main")
5. Click green "Run workflow" button

## 📦 After Build

Once the build completes:
1. Scroll to the bottom of the workflow run page
2. Find "Artifacts" section
3. Download `aleksa04-1-4-0-enhanced.zip`
4. Extract to get `aleksa04_1-1.4.0.jar`
5. Place in your Minecraft mods folder

## 🎯 Troubleshooting

### Build fails
- Check the "Actions" tab to see error logs
- Make sure all source files are uploaded
- Ensure `.github/workflows/build.yml` is present

### Can't download artifact
- Artifacts are kept for 30 days
- Click on the workflow run (not the workflow list)
- Scroll all the way down to "Artifacts"

### Git not installed
- Download Git from: https://git-scm.com/download/win
- Or use Option 1 (upload via GitHub web interface)

## 📝 Files Included

The repository should contain:
- ✅ `src/` - All source code
- ✅ `build.gradle` - Build configuration
- ✅ `gradle.properties` - Gradle settings
- ✅ `settings.gradle` - Project settings
- ✅ `.github/workflows/build.yml` - GitHub Actions workflow
- ✅ `gradlew` - Gradle wrapper script
- ✅ `gradle/wrapper/gradle-wrapper.jar` - Gradle wrapper JAR
- ❌ `gradle_temp/` - Exclude this (local only)

## 💡 Tips

- **Make repository private** if you don't want to share your code
- **Enable GitHub Actions** is usually on by default
- **Build time**: ~2-3 minutes on first run, ~1 minute on subsequent runs
- **No Java needed**: GitHub provides Java 25 automatically
