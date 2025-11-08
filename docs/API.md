# GFMirror API Documentation

This document describes the public APIs and interfaces available in GFMirror for developers and contributors.

## 📚 Core APIs

### EffectManager Interface

The `EffectManager` interface provides methods for managing mirror effects.

```kotlin
interface EffectManager {
    fun getAvailableEffects(): List<MirrorEffect>
    fun getCurrentEffect(): MirrorEffect?
    fun setEffect(effect: MirrorEffect)
    fun createCustomEffect(config: EffectConfig): MirrorEffect
    fun getEffectById(id: String): MirrorEffect?
    fun addEffect(effect: MirrorEffect)
    fun removeEffect(id: String)
}
```

#### Methods

- **`getAvailableEffects()`**: Returns list of all available effects
- **`getCurrentEffect()`**: Returns currently active effect
- **`setEffect(effect)`**: Sets the active effect
- **`createCustomEffect(config)`**: Creates a new custom effect
- **`getEffectById(id)`**: Retrieves effect by ID
- **`addEffect(effect)`**: Adds a new effect to the list
- **`removeEffect(id)`**: Removes effect by ID

### MirrorEffect Data Class

Represents a mirror effect with its properties.

```kotlin
data class MirrorEffect(
    val id: String,                    // Unique identifier
    val name: String,                  // Display name
    val shaderCode: String,            // GLSL shader code
    val parameters: Map<String, Float> = emptyMap(), // Effect parameters
    val previewIcon: Int               // Icon resource ID
)
```

#### Properties

- **`id`**: Unique string identifier for the effect
- **`name`**: Human-readable name for display
- **`shaderCode`**: GLSL fragment shader code
- **`parameters`**: Map of effect parameters and their values
- **`previewIcon`**: Android drawable resource ID for icon

### LocalizationManager Interface

Manages multi-language support and localization.

```kotlin
interface LocalizationManager {
    fun getCurrentLanguage(): String
    fun setLanguage(language: String)
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg args: Any): String
    fun getSupportedLanguages(): List<SupportedLanguage>
}
```

#### Methods

- **`getCurrentLanguage()`**: Returns current language code
- **`setLanguage(language)`**: Sets the app language
- **`getString(resId)`**: Gets localized string by resource ID
- **`getString(resId, args)`**: Gets formatted localized string
- **`getSupportedLanguages()`**: Returns list of supported languages

### SupportedLanguage Enum

Represents supported languages in the app.

```kotlin
enum class SupportedLanguage(val code: String, val displayName: String) {
    SIMPLIFIED_CHINESE("zh-rCN", "Simplified Chinese"),
    TRADITIONAL_CHINESE("zh-rTW", "Traditional Chinese"),
    ENGLISH("en", "English"),
    KOREAN("ko", "Korean"),
    JAPANESE("ja", "Japanese")
}
```

## 🎨 UI Components

### MainScreen Composable

Main application screen with camera preview and effects.

```kotlin
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    configManager: ConfigManager,
    localizationManager: LocalizationManager,
    performanceMonitor: PerformanceMonitor,
    effectManager: EffectManager,
    cameraManager: CameraManager,
    imageCaptureManager: ImageCaptureManager
)
```

#### Parameters

- **`modifier`**: Compose modifier for styling
- **`configManager`**: Configuration management
- **`localizationManager`**: Language management
- **`performanceMonitor`**: Performance monitoring
- **`effectManager`**: Effect management
- **`cameraManager`**: Camera operations
- **`imageCaptureManager`**: Image capture functionality

### UnifiedCameraPreview Composable

Camera preview component with OpenGL integration.

```kotlin
@Composable
fun UnifiedCameraPreview(
    modifier: Modifier = Modifier,
    localizationManager: LocalizationManager,
    imageCaptureManager: ImageCaptureManager? = null,
    onBitmapUpdate: (Bitmap?) -> Unit,
    showSmallPreview: Boolean = false,
    isFrontCamera: Boolean = false
)
```

#### Parameters

- **`modifier`**: Compose modifier
- **`localizationManager`**: Language management
- **`imageCaptureManager`**: Image capture manager
- **`onBitmapUpdate`**: Callback for bitmap updates
- **`showSmallPreview`**: Show small preview window
- **`isFrontCamera`**: Use front camera

## 🔧 Camera APIs

### CameraManager Interface

Interface for camera operations and management.

```kotlin
interface CameraManager {
    fun startPreview()
    fun stopPreview()
    fun switchCamera()
    fun capturePhoto(callback: (Bitmap?) -> Unit)
    fun release()
}
```

#### Methods

- **`startPreview()`**: Starts camera preview
- **`stopPreview()`**: Stops camera preview
- **`switchCamera()`**: Switches between front/back camera
- **`capturePhoto(callback)`**: Captures photo with callback
- **`release()`**: Releases camera resources

### ImageCaptureManager Interface

Manages image capture functionality.

```kotlin
interface ImageCaptureManager {
    suspend fun captureImage(): Result<Bitmap>
    suspend fun saveImageToGallery(bitmap: Bitmap): Result<Uri>
    fun setImageCapture(imageCapture: ImageCapture?)
}
```

#### Methods

- **`captureImage()`**: Captures image asynchronously
- **`saveImageToGallery(bitmap)`**: Saves bitmap to gallery
- **`setImageCapture(imageCapture)`**: Sets CameraX ImageCapture

## 🎭 Effect APIs

### Creating Custom Effects

To create a custom mirror effect:

```kotlin
val customEffect = MirrorEffect(
    id = "custom_effect",
    name = "Custom Effect",
    shaderCode = """
        precision mediump float;
        uniform sampler2D uTexture;
        varying vec2 vTextureCoord;
        
        void main() {
            // Your custom shader code here
            gl_FragColor = texture2D(uTexture, vTextureCoord);
        }
    """,
    parameters = mapOf(
        "intensity" to 1.0f,
        "radius" to 0.5f
    ),
    previewIcon = R.drawable.ic_effect_custom
)

effectManager.addEffect(customEffect)
```

### Effect Parameters

Common effect parameters:

- **`uStrength`**: Effect intensity (0.0 - 1.0)
- **`uRadius`**: Effect radius (0.0 - 1.0)
- **`uTime`**: Animation time (for animated effects)
- **`uFrequency`**: Wave frequency (for wave effects)
- **`uSpeed`**: Animation speed (for animated effects)

### Shader Uniforms

Standard uniforms available in all effects:

```glsl
uniform sampler2D uTexture;     // Input texture
uniform float uStrength;        // Effect strength
uniform float uRadius;          // Effect radius
uniform float uTime;            // Animation time
uniform float uFrequency;       // Wave frequency
uniform float uSpeed;           // Animation speed
uniform mat4 uMVPMatrix;        // Model-View-Projection matrix
```

## 🌍 Localization APIs

### Adding New Languages

To add support for a new language:

1. Create string resources file:
```
app/src/main/res/values-{language}/strings.xml
```

2. Add language to enum:
```kotlin
enum class SupportedLanguage(val code: String, val displayName: String) {
    // ... existing languages
    NEW_LANGUAGE("new", "New Language")
}
```

3. Update localization logic in `GFMirrorApplication.kt`

### String Resources

All user-facing strings should be in string resources:

```xml
<resources>
    <string name="app_name">GFMirror</string>
    <string name="effect_fisheye">Fisheye</string>
    <!-- Add more strings -->
</resources>
```

## 🔧 Configuration APIs

### ConfigManager Interface

Manages application configuration and preferences.

```kotlin
interface ConfigManager {
    fun <T> get(key: String, defaultValue: T): T
    fun <T> set(key: String, value: T)
    fun remove(key: String)
    fun clear()
}
```

#### Methods

- **`get(key, defaultValue)`**: Gets configuration value
- **`set(key, value)`**: Sets configuration value
- **`remove(key)`**: Removes configuration key
- **`clear()`**: Clears all configuration

### PerformanceMonitor Interface

Monitors application performance metrics.

```kotlin
interface PerformanceMonitor {
    fun startFrameRateMonitoring()
    fun stopFrameRateMonitoring()
    fun getCurrentFrameRate(): Float
    fun getMemoryUsage(): Float
}
```

#### Methods

- **`startFrameRateMonitoring()`**: Starts FPS monitoring
- **`stopFrameRateMonitoring()`**: Stops FPS monitoring
- **`getCurrentFrameRate()`**: Gets current FPS
- **`getMemoryUsage()`**: Gets memory usage percentage

## 🎨 Theme APIs

### Color System

```kotlin
object ColorManager {
    val Primary = Color(0xFF6200EE)
    val PrimaryVariant = Color(0xFF3700B3)
    val Secondary = Color(0xFF03DAC6)
    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFB00020)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFF000000)
    val OnBackground = Color(0xFF000000)
    val OnSurface = Color(0xFF000000)
    val OnError = Color(0xFFFFFFFF)
}
```

### Typography

```kotlin
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    // ... other text styles
)
```

## 🔒 Security Considerations

### Permissions

Required permissions:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

### Data Handling

- No sensitive data is stored
- Camera data is processed in memory only
- Images are saved to user's gallery
- No network communication

## 🧪 Testing APIs

### Unit Testing

```kotlin
@Test
fun testEffectCreation() {
    val effect = MirrorEffect(
        id = "test",
        name = "Test Effect",
        shaderCode = "void main() { gl_FragColor = vec4(1.0); }"
    )
    
    assertThat(effect.id).isEqualTo("test")
    assertThat(effect.name).isEqualTo("Test Effect")
}
```

### Integration Testing

```kotlin
@Test
fun testEffectSwitching() {
    val effectManager = DefaultEffectManager()
    val effects = effectManager.getAvailableEffects()
    
    assertThat(effects).isNotEmpty()
    
    effectManager.setEffect(effects.first())
    assertThat(effectManager.getCurrentEffect()).isNotNull()
}
```

## 📚 Examples

### Basic Effect Usage

```kotlin
// Get available effects
val effects = effectManager.getAvailableEffects()

// Set an effect
val fisheyeEffect = effects.find { it.id == "fisheye" }
fisheyeEffect?.let { effectManager.setEffect(it) }

// Get current effect
val currentEffect = effectManager.getCurrentEffect()
```

### Language Switching

```kotlin
// Get supported languages
val languages = localizationManager.getSupportedLanguages()

// Switch language
localizationManager.setLanguage("en")

// Get localized string
val appName = localizationManager.getString(R.string.app_name)
```

### Camera Operations

```kotlin
// Start camera
cameraManager.startPreview()

// Switch camera
cameraManager.switchCamera()

// Capture photo
cameraManager.capturePhoto { bitmap ->
    // Handle captured bitmap
    bitmap?.let { 
        imageCaptureManager.saveImageToGallery(it)
    }
}
```

---

For more detailed examples and advanced usage, refer to the source code and test files in the repository.
