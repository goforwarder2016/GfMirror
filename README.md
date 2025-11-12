<div align="center">

# 📱 GFMirror

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="GFMirror Logo" width="200" height="200">

**A mobile application focused on real-time image distortion effects with 13+ fun mirror effects**

[Features](#-key-features) • [Quick Start](#-quick-start) • [Architecture](#-technical-architecture) • [Contributing](#-contributing) • [License](#-license)

</div>

---

## 📖 Project Overview

**GFMirror** is a mobile application focused on real-time image distortion effects, providing users with fun and easy-to-use mirror effects. Built with modern Android development technologies, it supports real-time camera preview, photo capture, and multi-language support.

### 🎯 Key Highlights

- ✨ **13+ Built-in Mirror Effects** - Rich visual effects from fisheye to kaleidoscope
- 🚀 **Real-time Rendering** - Smooth real-time preview powered by OpenGL ES 2.0
- 📸 **Photo Capture** - One-tap photo capture with effects applied
- 🌍 **Multi-language Support** - Supports 5 languages (English, Chinese, Japanese, Korean, Traditional Chinese)
- 🎨 **Modern UI** - Jetpack Compose + Material Design 3
- 📱 **Wide Compatibility** - Supports all Android 5.0+ devices

---

## ✨ Key Features

### 🎭 Rich Mirror Effects

The app includes **13 professional mirror effects**, each carefully tuned:

| Effect | Icon | Description |
|--------|------|-------------|
| **Fisheye** | 🐠 | Fisheye effect - Creates wide-angle lens-like distortion |
| **Barrel** | 🛢️ | Barrel distortion - Image edges bulge outward |
| **Pincushion** | 📌 | Pincushion distortion - Image edges pinch inward |
| **Whirlpool** | 🌪️ | Whirlpool effect - Creates rotating twisted visual effects |
| **Ripple** | 🌊 | Ripple effect - Simulates water ripple distortion |
| **Slim Face** | 👤 | Slim face effect - Intelligent facial slimming |
| **Stretch** | 📏 | Stretch effect - Multi-directional stretch deformation |
| **Distort** | 🔄 | Distort effect - Free distortion deformation |
| **Mirror** | 🪞 | Mirror effect - Left-right mirror flip |
| **Horizontal Stretch** | ↔️ | Horizontal stretch - Horizontal stretch deformation |
| **Vertical Stretch** | ↕️ | Vertical stretch - Vertical stretch deformation |
| **Kaleidoscope** | 🌀 | Kaleidoscope effect - 4-fold symmetric kaleidoscope with dynamic rotation |
| **Bulge** | 💥 | Bulge effect - Center bulge deformation |
| **Wave** | 🌊 | Wave effect - Wave-like distortion deformation |

### 📸 Real-time Preview & Capture

- **🎥 Real-time Camera Preview** - High-performance real-time rendering based on OpenGL ES 2.0
- **📷 Photo Capture** - One-tap capture and save photos with effects
- **👆 Gesture Navigation** - Swipe left/right to quickly switch effects
- **🔄 Camera Switching** - Seamless front/rear camera switching
- **⚡ Smooth Experience** - Target frame rate ≥30 FPS, smooth and stutter-free

### 🌍 Multi-language Support

The app fully supports 5 languages, following Android internationalization best practices:

- 🇺🇸 **English**
- 🇨🇳 **Simplified Chinese**
- 🇹🇼 **Traditional Chinese**
- 🇰🇷 **Korean**
- 🇯🇵 **Japanese**

### 🎨 Modern UI Design

- **🎯 Jetpack Compose** - Latest declarative UI framework
- **🎨 Material Design 3** - Follows Google Material Design guidelines
- **📱 Responsive Design** - Perfect adaptation to various screen sizes and resolutions
- **✨ Smooth Animations** - Smooth transitions and interactive feedback

---

## 🏗️ Technical Architecture

### Core Tech Stack

```
┌─────────────────────────────────────────────────────────┐
│                    Tech Stack                           │
├─────────────────────────────────────────────────────────┤
│  Language      │  100% Kotlin                           │
│  UI Framework  │  Jetpack Compose 1.4.3                 │
│  Camera        │  CameraX (Jetpack)                     │
│  Rendering     │  OpenGL ES 2.0 + GLSL Shaders          │
│  Architecture  │  MVVM + Repository + UseCase          │
│  Min SDK       │  Android 5.0 (API 21)                   │
│  Target SDK    │  Android 14 (API 34)                   │
│  Build Tool    │  Gradle 8.0+                           │
└─────────────────────────────────────────────────────────┘
```

### Architecture Design

The project adopts **Clean Architecture** pattern with clear layers and well-defined responsibilities:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Activities  │  │  ViewModels   │  │  Compose UI  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   UseCases   │  │   Entities   │  │  Interfaces   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Repositories │  │  CameraX     │  │  OpenGL ES    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Modular Design

```
app/src/main/java/com/gf/mirror/
├── core/                           # Core business logic
│   ├── camera/                     # Camera management module
│   │   ├── CameraConfig.kt         # Camera configuration
│   │   ├── CameraManager.kt        # Camera interface
│   │   └── CameraXManager.kt       # CameraX implementation
│   ├── opengl/                     # OpenGL rendering engine
│   │   ├── MirrorRenderEngine.kt   # Rendering engine
│   │   ├── EffectManager.kt        # Effect management
│   │   └── effects/                # Effect implementations
│   ├── capture/                    # Capture functionality
│   │   └── ImageCaptureManager.kt  # Image capture management
│   └── common/                     # Common utilities
│       ├── LocalizationManager.kt  # Localization management
│       ├── ConfigManager.kt         # Configuration management
│       └── PerformanceMonitor.kt   # Performance monitoring
├── feature/                        # Feature modules
│   ├── camera/                     # Camera feature
│   ├── effects/                    # Effects feature
│   ├── capture/                   # Capture feature
│   ├── share/                     # Share feature
│   └── settings/                  # Settings feature
└── ui/                             # UI layer
    ├── components/                 # UI components
    ├── screens/                    # Screens
    └── theme/                      # Theme
```

### Key Dependencies

```gradle
// Jetpack Compose
implementation "androidx.compose.ui:ui:$compose_version"
implementation "androidx.compose.material3:material3:1.1.2"

// CameraX
implementation "androidx.camera:camera-camera2:$camerax_version"
implementation "androidx.camera:camera-lifecycle:$camerax_version"
implementation "androidx.camera:camera-view:$camerax_version"

// Lifecycle & Navigation
implementation "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version"
implementation "androidx.navigation:navigation-compose:$navigation_version"

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
```

---

## 🚀 Quick Start

### Requirements

- **Android Studio** Hedgehog (2023.1.1) or higher
- **JDK** 8 or higher (JDK 11+ recommended)
- **Android SDK** API 21-34
- **Gradle** 8.0+
- **Android Device** or **Emulator** (Android 5.0+)

### Installation

#### 1️⃣ Clone Repository

```bash
git clone git@github.com:goforwarder2016/GfMirror.git
cd Gfmirror
```

#### 2️⃣ Open in Android Studio

1. Launch **Android Studio**
2. Select **"Open an existing project"**
3. Choose the project root directory
4. Wait for Gradle sync to complete

#### 3️⃣ Configure Project

The project will automatically sync dependencies. If you encounter issues:

```bash
# Clean build cache
./gradlew clean

# Rebuild
./gradlew build
```

#### 4️⃣ Run Application

1. Connect an **Android device** or start an **emulator**
2. Click the **"Run"** button (▶️) or use shortcut `Shift + F10` (Windows/Linux) / `Ctrl + R` (Mac)
3. Select target device
4. Wait for app installation and launch

### Permissions

The app requires the following permissions (requested on first use):

| Permission | Purpose | Required |
|------------|---------|----------|
| `CAMERA` | Camera access permission | ✅ Yes |
| `READ_EXTERNAL_STORAGE` | Read external storage | ✅ Yes |
| `WRITE_EXTERNAL_STORAGE` | Write external storage (Android 9 and below) | ⚠️ Partial |
| `READ_MEDIA_IMAGES` | Read media images (Android 13+) | ✅ Yes |
| `READ_MEDIA_VIDEO` | Read media video (Android 13+) | ✅ Yes |
| `INTERNET` | Network access (for sharing features) | ⚠️ Optional |

---

## 📱 User Guide

### Basic Operations

1. **🎬 Launch App**
   - Camera preview starts automatically after app launch
   - Default effect is **Fisheye**

2. **🔄 Switch Effects**
   - **Swipe left/right** to switch between different mirror effects
   - Current effect name is displayed in the top-left corner

3. **📸 Capture Photo**
   - Tap the **capture button** at the bottom
   - Photo is automatically saved to device gallery
   - App displays success notification

4. **⚙️ Access Settings**
   - Tap the **gear icon** in the top-right corner to open settings
   - View effect information, switch languages, etc.

### Gesture Controls

| Gesture | Function |
|---------|----------|
| **Swipe Left/Right** | Switch mirror effects |
| **Tap Capture Button** | Capture and save photo |
| **Tap Settings Icon** | Open settings interface |
| **Long Press Capture** | Record video (planned feature) |

---

## 🎯 Performance Optimization

### Rendering Performance

- **Target Frame Rate**: ≥30 FPS
- **Memory Usage**: <200MB
- **CPU Usage**: <50%
- **Battery Optimization**: Optimized background operation, reduced power consumption

### Device Compatibility

- **Device Support**: All Android 5.0+ devices
- **Screen Adaptation**: Perfect adaptation for full-screen, notch, and foldable screens
- **Resolution Support**: 720p - 4K
- **Low-end Optimization**: Smooth operation on Android Go devices

### Optimization Strategies

- ✅ OpenGL shader compilation caching
- ✅ Camera resource lifecycle management
- ✅ Memory-efficient bitmap handling
- ✅ Real-time effect switching optimization
- ✅ Rendering pipeline optimization

---

## 🔧 Development Guide

### Code Standards

The project follows strict code standards to ensure code quality and maintainability:

- **Language**: All code, logs, and comments use **English** by default
- **Quality**: Strictly prohibits demo code, must be real functional code
- **i18n**: Ensures full compliance with Android multi-language support standards
- **Reuse**: When the same functionality is called in multiple places, it must be extracted as a common method

### Quality Checks

Run the following commands for code quality checks:

```bash
# Run convention check script
./check_conventions.sh

# Check hardcoded strings
grep -r '"[A-Z][a-z][^"]*"' app/src/main/java --exclude-dir=test

# Check demo code
grep -r "TODO\|FIXME\|placeholder\|demo\|mock" app/src/main/java -i

# Check duplicate implementations
grep -r "ExecutorService\|SharedPreferences\|Bitmap\.create" app/src/main/java
```

### Development Workflow

For detailed development workflow, please refer to [DEVELOPMENT_WORKFLOW.md](DEVELOPMENT_WORKFLOW.md)

---

## 📊 Project Status

### ✅ Completed Features

- [x] Basic architecture setup (MVVM + Clean Architecture)
- [x] 13 mirror effects implementation (OpenGL ES 2.0)
- [x] Multi-language support (5 languages)
- [x] Real-time camera preview (CameraX)
- [x] Photo capture functionality
- [x] Modern UI design (Jetpack Compose)
- [x] Gesture navigation (swipe to switch effects)
- [x] Settings interface
- [x] Effect preview dialog
- [x] Language switching functionality


### 📈 Version History

See [CHANGELOG.md](CHANGELOG.md) for detailed version update history.

---

## 🤝 Contributing

We welcome all forms of contributions! Whether it's code, documentation, bug reports, or feature suggestions, all support is valuable to the project.

### How to Contribute

1. **🍴 Fork the Project**
   ```bash
   # Click the Fork button on GitHub
   ```

2. **🌿 Create Feature Branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```

3. **💻 Make Changes**
   - Follow project code standards
   - Add necessary tests
   - Update relevant documentation

4. **📝 Commit Changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```

5. **📤 Push to Branch**
   ```bash
   git push origin feature/AmazingFeature
   ```

6. **🔀 Open Pull Request**
   - Create Pull Request on GitHub
   - Describe your changes in detail
   - Wait for code review

### Types of Contributions

- 🐛 **Bug Reports** - Found an issue? Please create an Issue
- 💡 **Feature Suggestions** - Have a good idea? Welcome to propose
- 📝 **Documentation Improvements** - Help improve documentation
- 🎨 **UI/UX Improvements** - Optimize user experience
- ⚡ **Performance Optimization** - Improve app performance
- 🌍 **Translation** - Help translate to more languages

For detailed contributing guidelines, please see [CONTRIBUTING.md](CONTRIBUTING.md)

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 GFMirror Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 🙏 Acknowledgments

Thanks to all developers and users who contributed to this project!

### Special Thanks

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI framework
- [CameraX](https://developer.android.com/training/camerax) - Camera management library
- [OpenGL ES](https://www.khronos.org/opengles/) - Graphics rendering API
- All contributors and test users

---

## 📚 Related Documentation

- [🏗️ Architecture Documentation](docs/ARCHITECTURE.md)
- [📖 API Documentation](docs/API.md)
- [🌍 Internationalization Implementation](ANDROID_I18N_IMPLEMENTATION.md)
- [📁 Project Structure](PROJECT_STRUCTURE.md)
- [🐛 Troubleshooting](docs/TROUBLESHOOTING.md)
- [🔒 Security Policy](SECURITY.md)
- [📝 Code of Conduct](CODE_OF_CONDUCT.md)

---

<div align="center">

### ⭐ If this project helps you, please give it a Star!

**Made with ❤️ by GoForwarder Team**

</div>
