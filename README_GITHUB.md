# PvP Client Enhanced - Build with GitHub Actions

This is the enhanced version of the PvP Client with resize handles, advanced customization, and new modules.

## 🚀 Build Using GitHub Actions (Recommended)

No Java installation required! GitHub will build it for you.

### Quick Steps:

1. **Create a GitHub Repository**
   - Go to https://github.com/new
   - Name it: `pvp-client-enhanced` (or any name you like)
   - Click "Create repository"

2. **Upload Your Code**
   - In your new repository, click "uploading an existing file"
   - Upload ALL files from this folder EXCEPT:
     - ❌ `gradle_temp/` folder (don't upload this)
   - Click "Commit changes"

3. **Build the JAR**
   - Go to the "Actions" tab
   - Click "Build PvP Client"
   - Click "Run workflow" → "Run workflow"
   - Wait ~2-3 minutes for green checkmark

4. **Download**
   - Scroll to "Artifacts" at the bottom
   - Download `aleksa04-1-4-0-enhanced.zip`
   - Extract to get your JAR file

## 🤖 Or Use the Setup Script

Double-click `SETUP_GITHUB.bat` to automate the upload process (requires Git).

## 📚 Documentation

- **GITHUB_BUILD_GUIDE.md** - Detailed GitHub Actions guide
- **ENHANCEMENTS.md** - List of all new features
- **BUILD_INSTRUCTIONS.md** - Local build instructions (if you prefer)

## ✨ New Features

- ✅ Resize handles for all HUD modules
- ✅ Custom width/height per module
- ✅ Padding, border radius, opacity controls
- ✅ 4 new modules: Hotbar, Server, Timer, XP
- ✅ Per-module color customization

## 📦 What's Included

- Complete source code with all enhancements
- GitHub Actions workflow for automatic building
- Gradle wrapper (no local Gradle needed)
- Setup scripts and documentation

## ❓ Troubleshooting

**Build fails on GitHub?**
- Check the Actions tab for error logs
- Ensure all files are uploaded (except gradle_temp)
- Make sure `.github/workflows/build.yml` exists

**Can't download artifact?**
- Artifacts are kept for 30 days
- Click on the specific workflow run, not the workflow list
- Scroll all the way to the bottom

**Git not installed?**
- Use the manual upload method via GitHub web interface
- Or install Git from: https://git-scm.com/download/win

## 🎮 After Building

Place the JAR in your Minecraft mods folder:
```
.minecraft/mods/aleksa04_1-1.4.0.jar
```

Enjoy your enhanced PvP Client!
