# GFMirror Architecture

This document describes the architecture and design patterns used in GFMirror.

## 🏗️ Overall Architecture

GFMirror follows a clean architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   MainScreen    │  │   Components    │  │   Theme     │ │
│  │   (Compose)     │  │   (Compose)     │  │   (Compose) │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   ViewModels    │  │   State         │  │   Events    │ │
│  │   (Future)      │  │   Management    │  │   Handling  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Use Cases     │  │   Entities      │  │   Repos     │ │
│  │   (Future)      │  │   (Future)      │  │   (Future)  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Managers      │  │   OpenGL        │  │   Camera    │ │
│  │   (Current)     │  │   Engine        │  │   (CameraX) │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Package Structure

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
│   │   ├── ConfigManager.kt        # Configuration management
│   │   ├── LocalizationManager.kt  # Multi-language support
│   │   ├── PerformanceMonitor.kt   # Performance monitoring
│   │   └── ...                     # Other managers
│   └── opengl/                     # OpenGL rendering
│       ├── EffectManager.kt        # Effect management interface
│       ├── MirrorEffect.kt         # Effect data class
│       ├── MirrorRenderEngine.kt   # Rendering engine
│       └── effects/                # Effect implementations
│           └── MirrorEffects.kt    # Predefined effects
├── feature/                        # Feature modules (future)
├── ui/                            # UI layer
│   ├── components/                # Reusable components
│   │   ├── CameraPreview.kt       # Camera preview component
│   │   ├── OpenGLMirrorView.kt    # OpenGL rendering view
│   │   └── ...                    # Other components
│   ├── screens/                   # Screen composables
│   │   └── MainScreen.kt          # Main application screen
│   └── theme/                     # App theming
│       ├── Color.kt               # Color definitions
│       ├── Theme.kt               # Material theme
│       └── Type.kt                # Typography
├── GFMirrorApplication.kt         # Application class
└── MainActivity.kt                # Main activity
```

## 🔧 Core Components

### 1. Application Class (`GFMirrorApplication`)

- Handles application-level initialization
- Manages locale configuration for internationalization
- Provides static methods for language management

### 2. Main Activity (`MainActivity`)

- Entry point of the application
- Initializes core managers
- Handles camera permissions
- Sets up the main UI

### 3. Core Managers

#### ConfigManager
- Manages application configuration
- Handles preferences and settings
- Provides type-safe configuration access

#### LocalizationManager
- Manages multi-language support
- Handles language switching
- Provides localized string access

#### PerformanceMonitor
- Monitors application performance
- Tracks frame rates and memory usage
- Provides performance metrics

#### EffectManager
- Manages mirror effects
- Handles effect switching
- Provides effect information

### 4. Camera System

#### CameraX Integration
- Uses CameraX for camera management
- Handles camera lifecycle
- Provides camera preview and capture

#### Camera Managers
- `CameraManager`: Interface for camera operations
- `CameraXManager`: CameraX implementation
- `ImageCaptureManager`: Image capture functionality

### 5. OpenGL Rendering System

#### Rendering Engine
- `MirrorRenderEngine`: Core rendering engine
- Handles OpenGL context and rendering
- Manages shader compilation and execution

#### Effect System
- `MirrorEffect`: Data class for effects
- `MirrorEffects`: Predefined effect implementations
- GLSL shaders for visual effects

## 🎨 UI Architecture

### Jetpack Compose
- Modern declarative UI framework
- Material Design 3 theming
- Reactive state management

### Component Structure
- **MainScreen**: Main application screen
- **UnifiedCameraPreview**: Camera preview with OpenGL
- **SimpleOpenGLMirrorView**: OpenGL rendering view
- **LanguageSelector**: Multi-language support

### State Management
- Uses Compose state for UI updates
- Reactive to effect changes
- Handles user interactions

## 🌍 Internationalization

### Language Support
- 5 supported languages
- Proper Android i18n implementation
- Dynamic language switching

### Implementation
- String resources in `values-{language}/strings.xml`
- `LocalizationManager` for language management
- Activity recreation for locale changes

## 🔄 Data Flow

### Effect Switching Flow
```
User Gesture → MainScreen → EffectManager → OpenGL Engine → Shader Update → Visual Effect
```

### Language Switching Flow
```
User Selection → LocalizationManager → SharedPreferences → Activity Recreation → UI Update
```

### Camera Flow
```
CameraX → SurfaceTexture → OpenGL Texture → Shader Processing → Display
```

## 🚀 Performance Considerations

### OpenGL Optimization
- Efficient shader compilation
- Texture management
- Frame rate optimization

### Memory Management
- Proper resource cleanup
- Bitmap handling
- Camera resource management

### UI Performance
- Compose optimization
- State management efficiency
- Smooth animations

## 🔮 Future Architecture Plans

### MVVM Implementation
- Add ViewModels for better state management
- Implement proper data binding
- Add repository pattern

### Modular Architecture
- Split into feature modules
- Implement dependency injection
- Add proper testing structure

### Advanced Features
- Video recording
- Custom effects
- Social sharing
- Cloud synchronization

## 📚 Design Patterns Used

- **Singleton**: For managers and utilities
- **Factory**: For effect creation
- **Observer**: For state changes
- **Strategy**: For different rendering approaches
- **Repository**: For data access (future)

## 🧪 Testing Strategy

### Current Testing
- Manual testing on various devices
- Performance monitoring
- User experience testing

### Future Testing
- Unit tests for business logic
- Integration tests for components
- UI tests for user interactions
- Performance tests for rendering
