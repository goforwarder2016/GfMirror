#!/bin/bash

# GFMirror GitHub Repository Setup Script
# This script helps set up the GitHub repository with proper configuration

echo "🚀 Setting up GFMirror GitHub repository..."

# Check if git is initialized
if [ ! -d ".git" ]; then
    echo "📦 Initializing Git repository..."
    git init
fi

# Add all files to git
echo "📁 Adding files to Git..."
git add .

# Create initial commit
echo "💾 Creating initial commit..."
git commit -m "Initial commit: GFMirror Android mirror effects app

- Real-time camera preview with OpenGL ES rendering
- 13+ mirror effects (fisheye, barrel, pincushion, etc.)
- Multi-language support (5 languages)
- Modern UI with Jetpack Compose
- Material Design 3 theming
- Complete documentation and CI/CD setup"

# Create main branch
echo "🌿 Creating main branch..."
git branch -M main

# Add GitHub remote (replace with your actual repository URL)
echo "🔗 Adding GitHub remote..."
echo "Please update the remote URL with your actual GitHub repository:"
echo "git remote add origin https://github.com/YOUR_USERNAME/Gfmirror.git"
echo "git push -u origin main"

# Create development branch
echo "🌿 Creating development branch..."
git checkout -b develop

# Switch back to main
git checkout main

echo "✅ Repository setup complete!"
echo ""
echo "Next steps:"
echo "1. Create a new repository on GitHub"
echo "2. Update the remote URL: git remote add origin https://github.com/YOUR_USERNAME/Gfmirror.git"
echo "3. Push to GitHub: git push -u origin main"
echo "4. Push develop branch: git push -u origin develop"
echo "5. Set up branch protection rules on GitHub"
echo "6. Enable GitHub Actions"
echo "7. Add repository topics and description"
echo ""
echo "Repository topics to add:"
echo "- android"
echo "- kotlin"
echo "- jetpack-compose"
echo "- opengl"
echo "- camera"
echo "- mirror-effects"
echo "- mobile-app"
echo "- open-source"
