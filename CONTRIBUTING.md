# Contributing to GFMirror

Thank you for your interest in contributing to GFMirror! This document provides guidelines and information for contributors.

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 24+
- Kotlin 1.8+
- Git

### Setting Up Development Environment

1. Fork the repository on GitHub
2. Clone your fork locally:
```bash
git clone https://github.com/yourusername/Gfmirror.git
cd Gfmirror
```

3. Add the upstream repository:
```bash
git remote add upstream https://github.com/originalowner/Gfmirror.git
```

4. Open the project in Android Studio
5. Build the project to ensure everything works

## 📋 Development Guidelines

### Code Style

- **Language**: All code, comments, and logs must be in English
- **Kotlin Style**: Follow [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- **Naming Conventions**: Use meaningful names for variables, functions, and classes
- **Comments**: Write clear, concise comments for complex logic

### Architecture Principles

- Follow the existing MVVM architecture pattern
- Keep business logic separate from UI components
- Use dependency injection for better testability
- Maintain single responsibility principle

### Git Workflow

1. Create a feature branch from `main`:
```bash
git checkout -b feature/your-feature-name
```

2. Make your changes and commit with descriptive messages:
```bash
git commit -m "Add new mirror effect: kaleidoscope"
```

3. Push your branch:
```bash
git push origin feature/your-feature-name
```

4. Create a Pull Request on GitHub

### Commit Message Format

Use clear, descriptive commit messages:
- `feat: add new mirror effect`
- `fix: resolve camera preview issue`
- `docs: update README with new features`
- `refactor: improve OpenGL rendering performance`

## 🎯 Areas for Contribution

### High Priority
- **New Mirror Effects**: Add creative distortion effects
- **Performance Optimization**: Improve rendering performance
- **UI/UX Improvements**: Enhance user experience
- **Bug Fixes**: Fix reported issues

### Medium Priority
- **Testing**: Add unit and integration tests
- **Documentation**: Improve code documentation
- **Accessibility**: Enhance accessibility features
- **Localization**: Add support for new languages

### Low Priority
- **Code Refactoring**: Improve code structure
- **Build System**: Optimize build configuration
- **CI/CD**: Improve automation

## 🧪 Testing

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Writing Tests

- Write unit tests for business logic
- Add instrumented tests for UI components
- Test on different Android versions and screen sizes
- Ensure tests are deterministic and reliable

## 🐛 Bug Reports

When reporting bugs, please include:

1. **Device Information**: Android version, device model
2. **App Version**: Version of GFMirror
3. **Steps to Reproduce**: Clear, numbered steps
4. **Expected Behavior**: What should happen
5. **Actual Behavior**: What actually happens
6. **Screenshots/Logs**: If applicable

## 💡 Feature Requests

For feature requests, please:

1. Check existing issues to avoid duplicates
2. Provide a clear description of the feature
3. Explain the use case and benefits
4. Consider implementation complexity

## 🔍 Code Review Process

### For Contributors

- Ensure your code follows the style guidelines
- Add appropriate tests for new functionality
- Update documentation if needed
- Respond to review feedback promptly

### For Reviewers

- Be constructive and respectful
- Focus on code quality and functionality
- Test the changes locally when possible
- Approve when ready for merge

## 📚 Resources

- [Android Developer Documentation](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [OpenGL ES Documentation](https://developer.android.com/guide/topics/graphics/opengl)
- [CameraX Documentation](https://developer.android.com/training/camerax)

## 🏷️ Release Process

1. Update version numbers in `build.gradle`
2. Update `CHANGELOG.md` with new features and fixes
3. Create a release tag
4. Build and test the release APK
5. Publish to GitHub Releases

## 📞 Getting Help

- **GitHub Issues**: For bugs and feature requests
- **Discussions**: For general questions and ideas
- **Email**: For security issues (use private communication)

## 📄 License

By contributing to GFMirror, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to GFMirror! 🎉
