package com.gf.mirror.core.opengl.effects

import com.gf.mirror.R
import com.gf.mirror.core.opengl.MirrorEffect

/**
 * Mirror effect definitions
 */
object MirrorEffects {
    
    /**
     * Fisheye effect
     */
    val FISHEYE = MirrorEffect(
        id = "fisheye",
        name = "Fisheye",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                // Simplified fisheye effect implementation
                vec2 center = vec2(0.5, 0.5);
                vec2 coord = vTextureCoord - center;
                
                // Fisheye strength parameter - further enhance effect
                float strength = 1.2;
                
                // Calculate distance
                float distance = length(coord);
                
                // Simplified fisheye distortion
                if (distance > 0.0) {
                    // Simple linear fisheye distortion
                    float fisheyeFactor = 1.0 - strength * distance;
                    
                    // Ensure factor is not too small
                    fisheyeFactor = max(fisheyeFactor, 0.3);
                    
                    // Apply distortion
                    vec2 newCoord = center + coord * fisheyeFactor;
                    
                    // Use clamp to ensure coordinates are within valid range, but preserve image content
                    newCoord = clamp(newCoord, 0.0, 1.0);
                    gl_FragColor = texture2D(uTexture, newCoord);
                } else {
                    // Direct sampling at center point
                    gl_FragColor = texture2D(uTexture, vTextureCoord);
                }
            }
        """.trimIndent(),
        parameters = mapOf(
            "strength" to 0.8f
        ),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Horizontal stretch effect
     */
    val HORIZONTAL_STRETCH = MirrorEffect(
        id = "horizontal_stretch",
        name = "Horizontal Stretch",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 coord = vTextureCoord;
                
                // Horizontal stretch
                float strength = 0.5;
                float centerY = 0.5;
                float distanceFromCenter = abs(coord.y - centerY);
                float stretchFactor = 1.0 + strength * (1.0 - distanceFromCenter * 2.0);
                
                coord.x = (coord.x - 0.5) / stretchFactor + 0.5;
                
                // Boundary check
                if (coord.x < 0.0 || coord.x > 1.0) {
                    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
                } else {
                    gl_FragColor = texture2D(uTexture, coord);
                }
            }
        """.trimIndent(),
        parameters = mapOf(
            "strength" to 0.5f
        ),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Vertical stretch effect
     */
    val VERTICAL_STRETCH = MirrorEffect(
        id = "vertical_stretch",
        name = "Vertical Stretch",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 coord = vTextureCoord;
                
                // Vertical stretch
                float strength = 0.5;
                float centerX = 0.5;
                float distanceFromCenter = abs(coord.x - centerX);
                float stretchFactor = 1.0 + strength * (1.0 - distanceFromCenter * 2.0);
                
                coord.y = (coord.y - 0.5) / stretchFactor + 0.5;
                
                // Boundary check
                if (coord.y < 0.0 || coord.y > 1.0) {
                    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
                } else {
                    gl_FragColor = texture2D(uTexture, coord);
                }
            }
        """.trimIndent(),
        parameters = mapOf(
            "strength" to 0.5f
        ),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Kaleidoscope effect (formerly Twist)
     */
    val TWIST = MirrorEffect(
        id = "twist",
        name = "Kaleidoscope",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            uniform float uStrength;
            uniform float uRadius;
            uniform float uAspect;
            uniform float uFrequency;
            uniform float uSpeed;
            varying vec2 vTextureCoord;
            
            void main() {
                // Test: just show original texture with a slight tint
                vec4 color = texture2D(uTexture, vTextureCoord);
                color.r += sin(uTime) * 0.1; // Add slight red tint that changes over time
                gl_FragColor = color;
            }
        """.trimIndent(),
        parameters = mapOf(
            "strength" to 0.5f,
            "radius" to 0.6f,
            "frequency" to 0.5f,
            "speed" to 0.5f
        ),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Bulge effect
     */
    val BULGE = MirrorEffect(
        id = "bulge",
        name = "Bulge",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 center = vec2(0.5, 0.5);
                vec2 coord = vTextureCoord - center;
                float distance = length(coord);
                
                // Bulge effect
                float strength = 0.6;
                float factor = 1.0 - strength * distance;
                vec2 newCoord = center + coord * factor;
                
                // Boundary check
                if (newCoord.x < 0.0 || newCoord.x > 1.0 || newCoord.y < 0.0 || newCoord.y > 1.0) {
                    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
                } else {
                    gl_FragColor = texture2D(uTexture, newCoord);
                }
            }
        """.trimIndent(),
        parameters = mapOf(
            "strength" to 0.6f
        ),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Wave effect
     */
    val WAVE = MirrorEffect(
        id = "wave",
        name = "Wave",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 coord = vTextureCoord;
                
                // Enhanced multi-directional wave effect
                float amplitude1 = 0.15;   // Primary wave amplitude - much stronger
                float frequency1 = 15.0;   // Primary wave frequency - more waves
                float speed1 = 4.0;        // Primary wave speed
                
                float amplitude2 = 0.08;   // Secondary wave amplitude
                float frequency2 = 25.0;   // Secondary wave frequency - finer waves
                float speed2 = 6.0;        // Secondary wave speed
                
                // Horizontal waves
                float waveX1 = amplitude1 * sin(coord.y * frequency1 + uTime * speed1);
                float waveX2 = amplitude2 * sin(coord.y * frequency2 + uTime * speed2 + 1.57);
                
                // Vertical waves for more complex effect
                float waveY1 = amplitude1 * 0.3 * sin(coord.x * frequency1 * 0.7 + uTime * speed1 * 0.8);
                float waveY2 = amplitude2 * 0.2 * sin(coord.x * frequency2 * 0.5 + uTime * speed2 * 1.2);
                
                // Combine waves
                coord.x += waveX1 + waveX2;
                coord.y += waveY1 + waveY2;
                
                // Add diagonal wave component
                float diagonalWave = 0.05 * sin((coord.x + coord.y) * 20.0 + uTime * 3.0);
                coord.x += diagonalWave;
                coord.y += diagonalWave * 0.5;
                
                // Enhanced boundary handling with edge sampling
                vec2 clampedCoord = clamp(coord, 0.0, 1.0);
                vec4 color = texture2D(uTexture, clampedCoord);
                
                // Add wave-based color modulation for more visual impact
                float waveIntensity = sin(coord.y * frequency1 + uTime * speed1) * 0.1 + 0.9;
                color.rgb *= waveIntensity;
                
                // Add subtle chromatic aberration effect
                if (coord.x < 0.0 || coord.x > 1.0 || coord.y < 0.0 || coord.y > 1.0) {
                    // Create edge glow effect instead of black
                    float edgeDistance = min(min(coord.x, 1.0 - coord.x), min(coord.y, 1.0 - coord.y));
                    float glow = smoothstep(0.0, 0.1, edgeDistance);
                    color.rgb = mix(vec3(0.8, 0.9, 1.0), color.rgb, glow);
                }
                
                gl_FragColor = color;
            }
        """.trimIndent(),
        parameters = mapOf(
            "amplitude" to 0.1f,
            "frequency" to 10.0f,
            "speed" to 2.0f
        ),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Barrel effect
     */
    val BARREL = MirrorEffect(
        id = "barrel",
        name = "Barrel",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 center = vec2(0.5, 0.5);
                vec2 coord = vTextureCoord - center;
                float distance = length(coord);
                
                // Barrel distortion: center inward depression, enhanced effect
                float strength = 0.8;
                float factor = 1.0 + strength * distance * distance;
                vec2 newCoord = center + coord * factor;
                
                // Use gentler boundary handling to preserve more image content
                if (newCoord.x >= 0.0 && newCoord.x <= 1.0 && newCoord.y >= 0.0 && newCoord.y <= 1.0) {
                    gl_FragColor = texture2D(uTexture, newCoord);
                } else {
                    // Use edge pixels when out of bounds
                    newCoord = clamp(newCoord, 0.0, 1.0);
                    gl_FragColor = texture2D(uTexture, newCoord);
                }
            }
        """.trimIndent(),
        parameters = mapOf("strength" to 0.3f),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Pincushion effect
     */
    val PINCUSHION = MirrorEffect(
        id = "pincushion",
        name = "Pincushion",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 center = vec2(0.5, 0.5);
                vec2 coord = vTextureCoord - center;
                float distance = length(coord);
                
                // Pincushion distortion: center outward protrusion, enhanced effect
                float strength = 0.7;
                float factor = 1.0 - strength * distance;
                factor = max(factor, 0.2); // Ensure no excessive contraction
                vec2 newCoord = center + coord * factor;
                
                // Use gentler boundary handling to preserve more image content
                if (newCoord.x >= 0.0 && newCoord.x <= 1.0 && newCoord.y >= 0.0 && newCoord.y <= 1.0) {
                    gl_FragColor = texture2D(uTexture, newCoord);
                } else {
                    // Use edge pixels when out of bounds
                    newCoord = clamp(newCoord, 0.0, 1.0);
                    gl_FragColor = texture2D(uTexture, newCoord);
                }
            }
        """.trimIndent(),
        parameters = mapOf("strength" to 0.4f),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    
    /**
     * Ripple effect
     */
    val RIPPLE = MirrorEffect(
        id = "ripple",
        name = "Ripple",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 center = vec2(0.5, 0.5);
                vec2 coord = vTextureCoord - center;
                float distance = length(coord);
                
                // Ultra-enhanced ripple effect with multiple concentric systems
                float amplitude1 = 4.0;   // Primary ripple amplitude - extremely strong
                float frequency1 = 6.0;   // Primary ripple frequency
                float speed1 = 8.0;       // Primary ripple speed
                
                float amplitude2 = 2.5;   // Secondary ripple amplitude
                float frequency2 = 12.0;  // Secondary ripple frequency - finer ripples
                float speed2 = 12.0;      // Secondary ripple speed
                
                float amplitude3 = 1.5;   // Tertiary ripple amplitude
                float frequency3 = 20.0;  // Tertiary ripple frequency - very fine ripples
                float speed3 = 16.0;      // Tertiary ripple speed
                
                // Calculate three ripple systems with different phases
                float ripple1 = amplitude1 * sin(distance * frequency1 - uTime * speed1);
                float ripple2 = amplitude2 * sin(distance * frequency2 - uTime * speed2 + 2.09);
                float ripple3 = amplitude3 * sin(distance * frequency3 - uTime * speed3 + 4.18);
                
                // Combine all ripples with weighted distribution
                float combinedRipple = ripple1 * 0.5 + ripple2 * 0.3 + ripple3 * 0.2;
                
                // Add radial distortion for more dynamic effect
                float radialDistortion = 0.3 * sin(distance * 8.0 - uTime * 4.0);
                
                // Apply extreme distortion with distance-based falloff
                float distortionStrength = 1.0 - smoothstep(0.0, 0.6, distance);
                vec2 newCoord = vTextureCoord + coord * (combinedRipple + radialDistortion) * distortionStrength * 0.8 / max(distance, 0.01);
                
                // Add spiral component for more complex motion
                float angle = atan(coord.y, coord.x);
                float spiral = 0.2 * sin(angle * 3.0 + distance * 10.0 - uTime * 6.0);
                newCoord += vec2(cos(angle), sin(angle)) * spiral * distortionStrength;
                
                // Sample texture with enhanced color processing
                vec4 color = texture2D(uTexture, clamp(newCoord, 0.0, 1.0));
                
                // Add ripple-based color modulation
                float colorModulation = sin(distance * frequency1 - uTime * speed1) * 0.15 + 0.85;
                color.rgb *= colorModulation;
                
                // Add chromatic aberration effect
                float aberration = distance * 0.1;
                vec4 colorR = texture2D(uTexture, clamp(newCoord + vec2(aberration, 0.0), 0.0, 1.0));
                vec4 colorB = texture2D(uTexture, clamp(newCoord - vec2(aberration, 0.0), 0.0, 1.0));
                
                color.r = colorR.r;
                color.b = colorB.b;
                
                // Enhance contrast and saturation for dramatic effect
                color.rgb = mix(color.rgb, color.rgb * 1.3, 0.2);
                
                // Add subtle glow effect
                float glow = sin(distance * frequency1 - uTime * speed1) * 0.1 + 0.9;
                color.rgb += vec3(0.1, 0.15, 0.2) * (1.0 - glow) * 0.3;
                
                gl_FragColor = color;
            }
        """.trimIndent(),
        parameters = mapOf(
            "amplitude" to 0.1f,
            "frequency" to 15.0f,
            "speed" to 2.0f
        ),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Slim face effect
     */
    val SLIM_FACE = MirrorEffect(
        id = "slim_face",
        name = "Slim Face",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 coord = vTextureCoord;
                
                // Slim face effect: horizontal compression, enhanced effect
                float strength = 0.5;
                float centerX = 0.5;
                float distanceFromCenter = abs(coord.x - centerX);
                float squeezeFactor = 1.0 - strength * (1.0 - distanceFromCenter * 2.0);
                squeezeFactor = max(squeezeFactor, 0.3); // Ensure no excessive compression
                
                coord.x = (coord.x - centerX) * squeezeFactor + centerX;
                
                // Use gentler boundary handling to preserve more image content
                if (coord.x >= 0.0 && coord.x <= 1.0) {
                    gl_FragColor = texture2D(uTexture, coord);
                } else {
                    // Use edge pixels when out of bounds
                    coord.x = clamp(coord.x, 0.0, 1.0);
                    gl_FragColor = texture2D(uTexture, coord);
                }
            }
        """.trimIndent(),
        parameters = mapOf("strength" to 0.2f),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Stretch effect
     */
    val STRETCH = MirrorEffect(
        id = "stretch",
        name = "Stretch",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 coord = vTextureCoord;
                
                // Stretch effect: vertical stretching, enhanced effect
                float strength = 0.6;
                float centerY = 0.5;
                float distanceFromCenter = abs(coord.y - centerY);
                float stretchFactor = 1.0 + strength * (1.0 - distanceFromCenter * 2.0);
                stretchFactor = max(stretchFactor, 0.4); // Ensure no excessive stretching
                
                coord.y = (coord.y - centerY) / stretchFactor + centerY;
                
                // Use gentler boundary handling to preserve more image content
                if (coord.y >= 0.0 && coord.y <= 1.0) {
                    gl_FragColor = texture2D(uTexture, coord);
                } else {
                    // Use edge pixels when out of bounds
                    coord.y = clamp(coord.y, 0.0, 1.0);
                    gl_FragColor = texture2D(uTexture, coord);
                }
            }
        """.trimIndent(),
        parameters = mapOf("strength" to 0.3f),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Distortion effect
     */
    val DISTORT = MirrorEffect(
        id = "distort",
        name = "Distort",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 coord = vTextureCoord;
                
                // Distortion effect: random distortion, enhanced effect
                float strength = 0.2;
                float frequency = 6.0;
                
                coord.x += strength * sin(coord.y * frequency + uTime);
                coord.y += strength * cos(coord.x * frequency + uTime);
                
                // Use gentler boundary handling to preserve more image content
                if (coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0) {
                    gl_FragColor = texture2D(uTexture, coord);
                } else {
                    // Use edge pixels when out of bounds
                    coord = clamp(coord, 0.0, 1.0);
                    gl_FragColor = texture2D(uTexture, coord);
                }
            }
        """.trimIndent(),
        parameters = mapOf(
            "strength" to 0.1f,
            "frequency" to 8.0f
        ),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Mirror effect
     */
    val MIRROR = MirrorEffect(
        id = "mirror",
        name = "Mirror",
        shaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uTime;
            varying vec2 vTextureCoord;
            
            void main() {
                vec2 coord = vTextureCoord;
                
                // Mirror effect: horizontal flip
                coord.x = 1.0 - coord.x;
                gl_FragColor = texture2D(uTexture, coord);
            }
        """.trimIndent(),
        parameters = mapOf(),
        previewIcon = R.drawable.ic_effect_custom
    )
    
    /**
     * Get all available effects
     */
    fun getAllEffects(): List<MirrorEffect> {
        return listOf(
            FISHEYE,
            HORIZONTAL_STRETCH,
            VERTICAL_STRETCH,
            TWIST,
            BULGE,
            WAVE,
            BARREL,
            PINCUSHION,
            RIPPLE,
            SLIM_FACE,
            STRETCH,
            DISTORT,
            MIRROR
        )
    }
}
