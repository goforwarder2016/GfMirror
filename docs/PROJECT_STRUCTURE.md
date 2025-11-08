# GFMirror Project Structure

This document provides a comprehensive overview of the GFMirror project structure and organization.

## 📁 Root Directory Structure

```
Gfmirror/
├── .github/                          # GitHub configuration
│   ├── workflows/                    # CI/CD workflows
│   │   └── android.yml              # Android build and test workflow
│   └── ISSUE_TEMPLATE/              # Issue and PR templates
│       ├── bug_report.md            # Bug report template
│       ├── feature_request.md       # Feature request template
│       └── pull_request_template.md # PR template
├── app/                             # Main Android application module
│   ├── build/                       # Build outputs (generated)
│   ├── src/main/                    # Main source code
│   │   ├── java/com/gf/mirror/     # Kotlin source files
│   │   └── res/                    # Android resources
│   ├── build.gradle                # App module build configuration
│   └── proguard-rules.pro          # ProGuard rules
├── docs/                           # Project documentation
│   ├── ARCHITECTURE.md             # Architecture documentation
│   ├── API.md                      # API documentation
│   ├── TROUBLESHOOTING.md          # Troubleshooting guide
│   ├── BADGES.md                   # Project badges guide
│   └── PROJECT_STRUCTURE.md        # This file
├── gradle/                         # Gradle wrapper
├── .gitignore                      # Git ignore rules
├── README.md                       # Main project documentation
├── LICENSE                         # MIT license
├── CONTRIBUTING.md                 # Contribution guidelines
├── CHANGELOG.md                    # Version history
├── SECURITY.md                     # Security policy
├── build.gradle                    # Project build configuration
├── settings.gradle                 # Gradle settings
├── gradle.properties              # Gradle properties
├── gradlew                        # Gradle wrapper script
└── local.properties               # Local development properties
```

## 🏗️ Application Architecture

### Core Package Structure

```
com.gf.mirror/
├── core/                           # Core business logic
│   ├── camera/                     # Camera management
│   │   ├── CameraConfig.kt         # Camera configuration
│   │   ├── CameraManager.kt        # Camera interface
│   │   └── CameraXManager.kt       # CameraX implementation
│   ├── capture/                    # Image capture
│   │   └── ImageCaptureManager.kt  # Capture functionality
│   ├── common/                     # Common utilities
│   │   ├── ColorManager.kt         # Color management
│   │   ├── ConfigManager.kt        # Configuration management
│   │   ├── DefaultConfigManager.kt # Default config implementation
│   │   ├── DefaultLocalizationManager.kt # Localization implementation
│   │   ├── DefaultPerformanceMonitor.kt  # Performance monitoring
│   │   ├── ExceptionFactory.kt     # Exception handling
│   │   ├── ExecutorManager.kt      # Thread management
│   │   ├── LocalizationManager.kt  # Localization interface
│   │   ├── PerformanceMonitor.kt   # Performance interface
│   │   ├── SizeManager.kt          # Size management
│   │   └── TextStyleManager.kt     # Text style management
│   └── opengl/                     # OpenGL rendering
│       ├── DefaultEffectManager.kt # Effect management implementation
│       ├── EffectManager.kt        # Effect management interface
│       ├── MirrorEffect.kt         # Effect data class
│       ├── MirrorRenderEngine.kt   # Rendering engine
│       └── effects/                # Effect implementations
│           └── MirrorEffects.kt    # Predefined effects
├── feature/                        # Feature modules (future expansion)
│   ├── camera/                     # Camera feature module
│   ├── capture/                    # Capture feature module
│   ├── effects/                    # Effects feature module
│   ├── settings/                   # Settings feature module
│   └── share/                      # Share feature module
├── ui/                            # UI layer
│   ├── components/                # Reusable UI components
│   │   ├── CameraPreview.kt       # Camera preview component
│   │   ├── EffectPreview.kt       # Effect preview component
│   │   ├── LanguageSelector.kt    # Language selection component
│   │   ├── OpenGLCameraPreview.kt # OpenGL camera preview
│   │   ├── OpenGLMirrorPreview.kt # OpenGL mirror preview
│   │   ├── OpenGLMirrorView.kt    # OpenGL mirror view
│   │   ├── SimpleCameraPreview.kt # Simple camera preview
│   │   ├── SimpleOpenGLMirrorView.kt # Simple OpenGL mirror view
│   │   └── UnifiedCameraPreview.kt # Unified camera preview
│   ├── screens/                   # Screen composables
│   │   └── MainScreen.kt          # Main application screen
│   └── theme/                     # App theming
│       ├── Color.kt               # Color definitions
│       ├── Theme.kt               # Material theme
│       └── Type.kt                # Typography
├── GFMirrorApplication.kt         # Application class
└── MainActivity.kt                # Main activity
```

## 📱 Android Resources Structure

```
app/src/main/res/
├── drawable/                      # Drawable resources
│   ├── ic_effect_*.xml           # Effect icons
│   └── ic_launcher*.xml          # App icons
├── layout/                       # Layout files (empty - using Compose)
├── mipmap-*/                     # App icons for different densities
├── values/                       # Default resources (English)
│   ├── colors.xml                # Color definitions
│   ├── strings.xml               # String resources
│   └── themes.xml                # Theme definitions
├── values-zh-rCN/                # Simplified Chinese resources
│   └── strings.xml
├── values-zh-rTW/                # Traditional Chinese resources
│   └── strings.xml
├── values-ja/                    # Japanese resources
│   └── strings.xml
├── values-ko/                    # Korean resources
│   └── strings.xml
└── xml/                          # XML configuration files
    ├── backup_rules.xml          # Backup rules
    └── data_extraction_rules.xml # Data extraction rules
```

## 🔧 Build Configuration

### Gradle Files

- **`build.gradle`** (Project): Project-level build configuration
- **`app/build.gradle`** (Module): App module build configuration
- **`settings.gradle`**: Gradle settings and module inclusion
- **`gradle.properties`**: Gradle properties and settings
- **`gradlew`**: Gradle wrapper script for Unix/Linux/macOS
- **`gradlew.bat`**: Gradle wrapper script for Windows

### Dependencies

The project uses the following key dependencies:

- **AndroidX Core**: Core Android libraries
- **Jetpack Compose**: Modern UI toolkit
- **CameraX**: Camera functionality
- **OpenGL ES**: Graphics rendering
- **Material Design**: UI components
- **Kotlin Coroutines**: Asynchronous programming

## 📚 Documentation Structure

```
docs/
├── ARCHITECTURE.md               # System architecture documentation
├── API.md                       # API reference documentation
├── TROUBLESHOOTING.md           # Troubleshooting guide
├── BADGES.md                    # Project badges guide
└── PROJECT_STRUCTURE.md         # This file
```

## 🔄 CI/CD Structure

```
.github/
├── workflows/
│   └── android.yml              # Android CI/CD workflow
└── ISSUE_TEMPLATE/
    ├── bug_report.md            # Bug report template
    ├── feature_request.md       # Feature request template
    └── pull_request_template.md # Pull request template
```

## 🎯 Key Design Patterns

### Architecture Patterns
- **MVVM**: Model-View-ViewModel pattern
- **Repository**: Data access abstraction
- **Dependency Injection**: Manual DI implementation
- **Observer**: State change notifications

### UI Patterns
- **Compose**: Declarative UI framework
- **Material Design**: Design system
- **Reactive**: State-driven UI updates
- **Component-based**: Reusable UI components

### Data Patterns
- **Singleton**: For managers and utilities
- **Factory**: For effect creation
- **Strategy**: For different rendering approaches
- **Builder**: For complex object creation

## 🚀 Development Workflow

### Branch Strategy
- **`main`**: Production-ready code
- **`develop`**: Integration branch
- **`feature/*`**: Feature development branches
- **`hotfix/*`**: Critical bug fixes

### Code Organization
- **Core Logic**: Business logic and data management
- **UI Layer**: User interface components
- **Feature Modules**: Organized by functionality
- **Common Utilities**: Shared functionality

### Testing Strategy
- **Unit Tests**: Business logic testing
- **Integration Tests**: Component integration
- **UI Tests**: User interface testing
- **Performance Tests**: Rendering performance

## 📦 Build Artifacts

### Generated Files
- **APK Files**: `app/build/outputs/apk/`
- **AAR Files**: `app/build/outputs/aar/`
- **Test Results**: `app/build/test-results/`
- **Lint Reports**: `app/build/reports/lint/`

### Temporary Files
- **Build Cache**: `.gradle/` and `build/`
- **IDE Files**: `.idea/` (excluded from Git)
- **Local Properties**: `local.properties` (excluded from Git)

## 🔒 Security Considerations

### File Permissions
- **Sensitive Files**: Excluded from version control
- **Build Artifacts**: Not committed to repository
- **Local Configuration**: Kept local only

### Code Organization
- **No Hardcoded Secrets**: All sensitive data externalized
- **Permission Management**: Minimal required permissions
- **Data Protection**: Local processing only

## 📈 Scalability Considerations

### Modular Design
- **Feature Modules**: Easy to add new features
- **Plugin Architecture**: Extensible effect system
- **Component Library**: Reusable UI components

### Performance
- **Lazy Loading**: Components loaded on demand
- **Memory Management**: Efficient resource usage
- **Rendering Optimization**: Smooth 60 FPS target

---

This structure provides a solid foundation for the GFMirror project, ensuring maintainability, scalability, and ease of development.
