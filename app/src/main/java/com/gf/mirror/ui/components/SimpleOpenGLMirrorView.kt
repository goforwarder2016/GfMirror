package com.gf.mirror.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import com.gf.mirror.core.opengl.MirrorEffect
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Simplified OpenGL mirror view
 * Uses 2D texture instead of OES texture, more simple and reliable
 */
class SimpleOpenGLMirrorView(context: Context) : GLSurfaceView(context) {
    
    private var _currentEffect: MirrorEffect? = null
    private var needsShaderRecompile = false
    
    // Expose current effect for external access
    val currentEffect: MirrorEffect? get() = _currentEffect
    
    // 2D texture related
    private var textureId: Int = 0
    private var currentBitmap: Bitmap? = null
    private var lastBitmapWidth: Int = 0
    private var lastBitmapHeight: Int = 0
    
    // MVP matrix
    private val mvpMatrix = FloatArray(16)
    
    // Vertex data
    private val vertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f,  // Bottom left
         1.0f, -1.0f, 0.0f,  // Bottom right
        -1.0f,  1.0f, 0.0f,  // Top left
         1.0f,  1.0f, 0.0f   // Top right
    )
    
    // Texture coordinates - restore OpenGL standard coordinates
    private val textureCoords = floatArrayOf(
        0.0f, 1.0f,  // Bottom left
        1.0f, 1.0f,  // Bottom right
        0.0f, 0.0f,  // Top left
        1.0f, 0.0f   // Top right
    )
    
    // Vertex and texture coordinate buffers
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureBuffer: FloatBuffer
    
    // Shader related
    private var shaderProgram = 0
    private var positionHandle = 0
    private var textureCoordHandle = 0
    private var mvpMatrixHandle = 0
    private var textureHandle = 0
    private var timeHandle = 0
    
    // Unified uniform interface
    private var uStrengthHandle = 0
    private var uRadiusHandle = 0
    private var uAspectHandle = 0
    private var uTimeHandle = 0
    private var uFrequencyHandle = 0
    private var uSpeedHandle = 0
    
    // Kaleidoscope specific uniforms
    private var uResolutionHandle = 0
    private var uCenterHandle = 0
    private var uRotationHandle = 0
    
    // Effect parameters
    private var currentStrength = 0.5f
    private var currentRadius = 0.5f
    private var currentAspect = 1.0f
    private var currentTime = 0.0f
    private var currentFrequency = 0.5f
    
    // Surface dimensions for kaleidoscope
    private var surfaceWidth = 0
    private var surfaceHeight = 0
    private var currentSpeed = 0.5f
    
    init {
        try {
            // Set OpenGL ES 2.0
            setEGLContextClientVersion(2)
            
            // Initialize buffers
            initBuffers()
            
            // Initialize MVP matrix to identity matrix
            Matrix.setIdentityM(mvpMatrix, 0)
            
            // Set renderer
            setRenderer(SimpleOpenGLRenderer())
            
            // Set render mode to continuous rendering
            renderMode = RENDERMODE_CONTINUOUSLY
            
        } catch (e: Exception) {
            Log.e("SimpleOpenGLMirrorView", "Failed to initialize OpenGL", e)
        }
    }
    
    private fun initBuffers() {
        // Create vertex buffer
        val vertexByteBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        vertexByteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = vertexByteBuffer.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
        
        // Create texture coordinate buffer
        val textureByteBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
        textureByteBuffer.order(ByteOrder.nativeOrder())
        textureBuffer = textureByteBuffer.asFloatBuffer()
        textureBuffer.put(textureCoords)
        textureBuffer.position(0)
    }
    
    fun setEffect(effect: MirrorEffect) {
        // Only recompile if effect actually changed
        if (_currentEffect?.id != effect.id) {
            _currentEffect = effect
            needsShaderRecompile = true
            println("SimpleOpenGLMirrorView: Effect changed to ${effect.name}, will recompile shader")
        }
    }
    
    /**
     * Set effect parameters
     */
    fun setEffectParameters(
        strength: Float = 0.5f,
        radius: Float = 0.5f,
        frequency: Float = 0.5f,
        speed: Float = 0.5f
    ) {
        currentStrength = strength
        currentRadius = radius
        currentFrequency = frequency
        currentSpeed = speed
        requestRender()
    }
    
    /**
     * Get current effect parameters
     */
    fun getEffectParameters(): Map<String, Float> {
        return mapOf(
            "strength" to currentStrength,
            "radius" to currentRadius,
            "frequency" to currentFrequency,
            "speed" to currentSpeed
        )
    }
    
    fun updateBitmap(bitmap: Bitmap?) {
        currentBitmap = bitmap
        println("SimpleOpenGLMirrorView: Bitmap updated, size: ${bitmap?.width}x${bitmap?.height}")
        // Force a redraw when bitmap is updated
        requestRender()
    }
    
    /**
     * Capture the currently rendered effect image
     */
    fun captureEffectBitmap(): Bitmap? {
        return try {
            // Get current view dimensions
            val width = width
            val height = height
            
            if (width <= 0 || height <= 0) {
                println("SimpleOpenGLMirrorView: Invalid view size: ${width}x${height}")
                return null
            }
            
            println("SimpleOpenGLMirrorView: Starting capture, size: ${width}x${height}")
            println("SimpleOpenGLMirrorView: Current effect: ${_currentEffect?.name}")
            
            // Check if there is a current bitmap
            if (currentBitmap == null) {
                println("SimpleOpenGLMirrorView: No current bitmap available")
                return null
            }
            
            // Use glReadPixels to read effect data from OpenGL framebuffer
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val latch = java.util.concurrent.CountDownLatch(1)
            
            // Execute screenshot in OpenGL rendering thread
            queueEvent {
                try {
                    val pixelBuffer = IntArray(width * height)
                    val byteBuffer = ByteBuffer.allocateDirect(width * height * 4)
                    byteBuffer.order(ByteOrder.nativeOrder())
                    
                    // Ensure rendering is complete
                    GLES20.glFinish()
                    
                    // Read pixel data from OpenGL (OpenGL uses RGBA format)
                    GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer)
                    
                    // Convert RGBA to ARGB format (Android Bitmap uses ARGB format)
                    byteBuffer.rewind()
                    val rgbaBytes = ByteArray(width * height * 4)
                    byteBuffer.get(rgbaBytes)
                    
                    // Convert each pixel from RGBA to ARGB
                    for (i in 0 until width * height) {
                        val r = rgbaBytes[i * 4].toInt() and 0xFF
                        val g = rgbaBytes[i * 4 + 1].toInt() and 0xFF
                        val b = rgbaBytes[i * 4 + 2].toInt() and 0xFF
                        val a = rgbaBytes[i * 4 + 3].toInt() and 0xFF
                        
                        // Create ARGB format pixel value
                        pixelBuffer[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                    }
                    
                    // Create Bitmap
                    bitmap.setPixels(pixelBuffer, 0, width, 0, 0, width, height)
                    
                    println("SimpleOpenGLMirrorView: Successfully captured effect bitmap in OpenGL thread: ${bitmap.width}x${bitmap.height}")
                    
                } catch (e: Exception) {
                    println("SimpleOpenGLMirrorView: Failed to capture effect bitmap in OpenGL thread: ${e.message}")
                    e.printStackTrace()
                } finally {
                    latch.countDown()
                }
            }
            
            // Wait for OpenGL operations to complete
            latch.await(1000, java.util.concurrent.TimeUnit.MILLISECONDS)
            
            // Vertical flip to correct OpenGL coordinate system
            val finalBitmap = flipBitmapVertically(bitmap)
            
            println("SimpleOpenGLMirrorView: Successfully captured effect bitmap: ${finalBitmap.width}x${finalBitmap.height}")
            
            finalBitmap
        } catch (e: Exception) {
            println("SimpleOpenGLMirrorView: Failed to capture effect bitmap: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Check if bitmap is all black
     */
    private fun isBitmapBlack(bitmap: Bitmap): Boolean {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // Check if the first 100 pixels are all black or near black
        for (i in 0 until minOf(100, pixels.size)) {
            val pixel = pixels[i]
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF
            
            // If not black or near black, return false
            if (red > 10 || green > 10 || blue > 10) {
                return false
            }
        }
        return true
    }
    
    /**
     * Determine if effect needs flipping
     */
    private fun shouldFlipForEffect(effectId: String?): Boolean {
        // First try without flipping to see the effect
        return false
    }
    
    /**
     * Vertically flip Bitmap
     */
    private fun flipBitmapVertically(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val flippedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = android.graphics.Canvas(flippedBitmap)
        // Use Matrix for vertical flip
        val matrix = android.graphics.Matrix()
        matrix.postScale(1f, -1f, width / 2f, height / 2f)
        canvas.drawBitmap(bitmap, matrix, null)
        
        return flippedBitmap
    }
    
    /**
     * Horizontally flip Bitmap
     */
    private fun flipBitmapHorizontally(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val flippedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = android.graphics.Canvas(flippedBitmap)
        canvas.scale(-1f, 1f, width / 2f, height / 2f)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        
        return flippedBitmap
    }
    
    /**
     * Rotate Bitmap 180 degrees
     */
    private fun rotateBitmap180(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val rotatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = android.graphics.Canvas(rotatedBitmap)
        // Use Matrix for 180 degree rotation
        val matrix = android.graphics.Matrix()
        matrix.postRotate(180f, width / 2f, height / 2f)
        canvas.drawBitmap(bitmap, matrix, null)
        
        return rotatedBitmap
    }
    
    private inner class SimpleOpenGLRenderer : GLSurfaceView.Renderer {
        
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            println("SimpleOpenGLMirrorView: Surface created")
            
            // Set background color to black
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            
            // Enable depth testing
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            
            // Create 2D texture
            create2DTexture()
            
            // Initialize shaders
            initializeShaders()
        }
        
        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            println("SimpleOpenGLMirrorView: Surface changed: ${width}x${height}")
            
            // Set viewport
            GLES20.glViewport(0, 0, width, height)
            
            // Calculate and set aspect ratio
            currentAspect = width.toFloat() / height.toFloat()
            
            // Store surface dimensions for kaleidoscope uniforms
            surfaceWidth = width
            surfaceHeight = height
        }
        
        override fun onDrawFrame(gl: GL10?) {
            try {
                // Clear color and depth buffers
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                
                // Check if shaders need to be recompiled
                if (needsShaderRecompile || shaderProgram == 0) {
                    println("SimpleOpenGLMirrorView: Recompiling shaders for effect: ${_currentEffect?.name ?: "none"}")
                    if (shaderProgram != 0) {
                        GLES20.glDeleteProgram(shaderProgram)
                        shaderProgram = 0
                    }
                    initializeShaders()
                    needsShaderRecompile = false
                    println("SimpleOpenGLMirrorView: Shaders recompiled successfully")
                }
                
                // Use shader program for rendering
                if (shaderProgram != 0) {
                GLES20.glUseProgram(shaderProgram)
                
                // Set vertex attributes
                setupVertexAttributes()
                
                // Set MVP matrix
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
                
                // Update texture data - always update for camera preview
                if (currentBitmap != null) {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, currentBitmap, 0)
                    GLES20.glUniform1i(textureHandle, 0)
                } else {
                        // If no bitmap, display solid color based on effect
                        val effect = _currentEffect
                        when (effect?.id) {
                            "fisheye" -> GLES20.glClearColor(0.2f, 0.4f, 0.8f, 1.0f) // Blue for fisheye
                            "horizontal_stretch" -> GLES20.glClearColor(0.8f, 0.2f, 0.4f, 1.0f) // Red for horizontal
                            "vertical_stretch" -> GLES20.glClearColor(0.4f, 0.8f, 0.2f, 1.0f) // Green for vertical
                            "twist" -> GLES20.glClearColor(0.8f, 0.8f, 0.2f, 1.0f) // Yellow for twist
                            "bulge" -> GLES20.glClearColor(0.8f, 0.2f, 0.8f, 1.0f) // Magenta for bulge
                            else -> GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f) // Gray default
                        }
                        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                        println("SimpleOpenGLMirrorView: No bitmap available, showing effect color for: ${effect?.name ?: "none"}")
                        return
                    }
                    
                    // Set time uniform (for animation effects)
                    val time = System.currentTimeMillis() / 1000.0f
                    currentTime = time
                    GLES20.glUniform1f(timeHandle, time)
                    
                    // Set unified uniform parameters
                    if (uStrengthHandle >= 0) GLES20.glUniform1f(uStrengthHandle, currentStrength)
                    if (uRadiusHandle >= 0) GLES20.glUniform1f(uRadiusHandle, currentRadius)
                    if (uAspectHandle >= 0) GLES20.glUniform1f(uAspectHandle, currentAspect)
                    if (uTimeHandle >= 0) GLES20.glUniform1f(uTimeHandle, currentTime)
                    if (uFrequencyHandle >= 0) GLES20.glUniform1f(uFrequencyHandle, currentFrequency)
                    if (uSpeedHandle >= 0) GLES20.glUniform1f(uSpeedHandle, currentSpeed)
                    
                    // Set kaleidoscope specific uniforms
                    if (uResolutionHandle >= 0) GLES20.glUniform2f(uResolutionHandle, surfaceWidth.toFloat(), surfaceHeight.toFloat())
                    if (uCenterHandle >= 0) GLES20.glUniform2f(uCenterHandle, surfaceWidth / 2.0f, surfaceHeight / 2.0f)
                    if (uRotationHandle >= 0) GLES20.glUniform1f(uRotationHandle, currentTime * 0.1f)
                    
                    // Draw
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
                    
                    // Disable vertex attributes
                    GLES20.glDisableVertexAttribArray(positionHandle)
                    GLES20.glDisableVertexAttribArray(textureCoordHandle)
                    
                    // Print log every 100 frames
                    if (System.currentTimeMillis() % 1000 < 16) {
                        println("SimpleOpenGLMirrorView: Drawing frame with shader, effect: ${_currentEffect?.name ?: "none"}")
                    }
                } else {
                    // If no shader program, display solid color
                    GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                }
            } catch (e: Exception) {
                Log.e("SimpleOpenGLMirrorView", "Error in onDrawFrame", e)
                println("SimpleOpenGLMirrorView: Error in onDrawFrame: ${e.message}")
            }
        }
        
        private fun create2DTexture(): Int {
            val textureIds = IntArray(1)
            GLES20.glGenTextures(1, textureIds, 0)
            val textureId = textureIds[0]
            
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            
            this@SimpleOpenGLMirrorView.textureId = textureId
            println("SimpleOpenGLMirrorView: 2D texture created: $textureId")
            return textureId
        }
        
        private fun setupVertexAttributes() {
            // Set vertex positions
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
            
            // Set texture coordinates
            GLES20.glEnableVertexAttribArray(textureCoordHandle)
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        }
        
        private fun initializeShaders() {
            // Delete old shader program
            if (shaderProgram != 0) {
                GLES20.glDeleteProgram(shaderProgram)
                shaderProgram = 0
            }
            
            // Create vertex shader
            val vertexShader = createVertexShader()
            val fragmentShader = createFragmentShader()
            
            // Create shader program
            shaderProgram = createShaderProgram(vertexShader, fragmentShader)
            
            // Get attribute locations
            positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
            textureCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "aTextureCoord")
            mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
            textureHandle = GLES20.glGetUniformLocation(shaderProgram, "uTexture")
            timeHandle = GLES20.glGetUniformLocation(shaderProgram, "uTime")
            
            // Get unified uniform interface
            uStrengthHandle = GLES20.glGetUniformLocation(shaderProgram, "uStrength")
            uRadiusHandle = GLES20.glGetUniformLocation(shaderProgram, "uRadius")
            uAspectHandle = GLES20.glGetUniformLocation(shaderProgram, "uAspect")
            uTimeHandle = GLES20.glGetUniformLocation(shaderProgram, "uTime")
            uFrequencyHandle = GLES20.glGetUniformLocation(shaderProgram, "uFrequency")
            uSpeedHandle = GLES20.glGetUniformLocation(shaderProgram, "uSpeed")
            
            // Get kaleidoscope specific uniforms
            uResolutionHandle = GLES20.glGetUniformLocation(shaderProgram, "uResolution")
            uCenterHandle = GLES20.glGetUniformLocation(shaderProgram, "uCenter")
            uRotationHandle = GLES20.glGetUniformLocation(shaderProgram, "uRotation")
            
            println("SimpleOpenGLMirrorView: Shaders initialized for effect: ${_currentEffect?.name ?: "none"}")
        }
        
        private fun createVertexShader(): Int {
            val vertexShaderCode = """
                uniform mat4 uMVPMatrix;
                attribute vec4 aPosition;
                attribute vec2 aTextureCoord;
                varying vec2 vTextureCoord;
                
                void main() {
                    gl_Position = uMVPMatrix * aPosition;
                    vTextureCoord = aTextureCoord;
                }
            """.trimIndent()
            return createShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        }
        
        private fun createFragmentShader(): Int {
            val fragmentShaderCode: String = if (_currentEffect != null) {
                println("SimpleOpenGLMirrorView: Compiling shader for effect id=${_currentEffect!!.id}")
                // Prioritize hardcoded shaders to ensure correct effects
                when (_currentEffect!!.id) {
                    "fisheye" -> {
                        """
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
                            // Fisheye effect: center magnification, edge compression (inverse quadratic)
                            vec2 center = vec2(0.5);
                            vec2 pos = vTextureCoord - center;
                            
                            // Aspect correction
                            pos.x *= uAspect;
                            float r = length(pos);
                            
                            // Parameterized strength mapping: 0-1 -> 0.2-2.0
                            float S = mix(0.2, 2.0, uStrength);
                            float R = mix(0.3, 1.0, uRadius);
                            
                            if (r > 0.0) {
                                // Fisheye distortion: inverse quadratic factor = 1.0 / (1.0 + k * r²)
                                float factor = 1.0 / (1.0 + S * r * r);
                                factor = mix(1.0, factor, smoothstep(0.0, R, r));
                                
                                pos /= uAspect;
                                vec2 newCoord = center + pos * factor;
                                newCoord = clamp(newCoord, vec2(0.0), vec2(1.0));
                                gl_FragColor = texture2D(uTexture, newCoord);
                            } else {
                                gl_FragColor = texture2D(uTexture, vTextureCoord);
                            }
                        }
                        """.trimIndent()
                    }
                    "horizontal_stretch" -> {
                        """
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
                            vec2 coord = vTextureCoord;
                            // Parameterized strength mapping: 0-1 -> 0.1-0.8
                            float S = mix(0.1, 0.8, uStrength);
                            float centerY = 0.5;
                            float distanceFromCenter = abs(coord.y - centerY);
                            float stretchFactor = 1.0 + S * (1.0 - distanceFromCenter * 2.0);
                            coord.x = (coord.x - 0.5) / stretchFactor + 0.5;
                            coord = clamp(coord, 0.0, 1.0);
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                        """.trimIndent()
                    }
                    "vertical_stretch" -> {
                        """
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
                            vec2 coord = vTextureCoord;
                            // Parameterized strength mapping: 0-1 -> 0.1-0.8
                            float S = mix(0.1, 0.8, uStrength);
                            float centerX = 0.5;
                            float distanceFromCenter = abs(coord.x - centerX);
                            float stretchFactor = 1.0 + S * (1.0 - distanceFromCenter * 2.0);
                            coord.y = (coord.y - 0.5) / stretchFactor + 0.5;
                            coord = clamp(coord, 0.0, 1.0);
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                        """.trimIndent()
                    }
                    "twist" -> {
                        """
                        precision mediump float;
                        uniform sampler2D uTexture;
                        uniform float uTime;
                        uniform float uStrength;
                        uniform float uRadius;
                        uniform float uAspect;
                        uniform float uFrequency;
                        uniform float uSpeed;
                        uniform vec2 uResolution;
                        uniform vec2 uCenter;
                        uniform float uRotation;
                        varying vec2 vTextureCoord;
                        
                        void main() {
                            vec2 center = vec2(0.5, 0.5);
                            vec2 pos = vTextureCoord - center;
                            
                            // Apply aspect correction
                            pos.x *= uAspect;
                            
                            float r = length(pos);
                            
                            // Only apply kaleidoscope effect within a certain radius
                            if (r < 0.7) {
                                // 4-fold kaleidoscope with increased spacing
                                float angle = atan(pos.y, pos.x) + uTime * 0.1;
                                
                                // Create 4-fold symmetry with wider spacing
                                float x = abs(pos.x);
                                float y = abs(pos.y);
                                
                                // Rotate by 30 degrees instead of 45 to create wider spacing
                                float cos30 = 0.8660;
                                float sin30 = 0.5000;
                                float newX = x * cos30 - y * sin30;
                                float newY = x * sin30 + y * cos30;
                                
                                // Apply additional scaling to increase spacing
                                newX *= 1.2;
                                newY *= 1.2;
                                
                                // Create mirrored coordinates
                                vec2 mirroredPos = vec2(newX, newY);
                                
                                // Undo aspect correction
                                mirroredPos.x /= uAspect;
                                vec2 sourceUV = center + mirroredPos;
                                
                                // Clamp coordinates
                                sourceUV = clamp(sourceUV, vec2(0.0), vec2(1.0));
                                
                                // Sample texture
                                gl_FragColor = texture2D(uTexture, sourceUV);
                            } else {
                                // Outside kaleidoscope area, show original
                                gl_FragColor = texture2D(uTexture, vTextureCoord);
                            }
                        }
                        """.trimIndent()
                    }
                    "bulge" -> {
                        """
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
                            vec2 center = vec2(0.5);
                            vec2 pos = vTextureCoord - center;
                            
                            // Aspect correction
                            pos.x *= uAspect;
                            float r = length(pos);
                            
                            // Parameterized strength mapping: 0-1 -> 0.1-0.8
                            float S = mix(0.1, 0.8, uStrength);
                            float R = mix(0.2, 0.9, uRadius);
                            
                            if (r > 0.0) {
                                // Bulge effect: positive quadratic factor = 1.0 + k * r²
                                float factor = 1.0 + S * r * r;
                                factor = mix(1.0, factor, smoothstep(0.0, R, r));
                                
                                pos /= uAspect;
                                vec2 newCoord = center + pos * factor;
                                newCoord = clamp(newCoord, vec2(0.0), vec2(1.0));
                                gl_FragColor = texture2D(uTexture, newCoord);
                            } else {
                                gl_FragColor = texture2D(uTexture, vTextureCoord);
                            }
                        }
                        """.trimIndent()
                    }
                    "wave" -> {
                        """
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
                            vec2 coord = vTextureCoord;
                            
                            // Parameterized strength mapping: 0-1 -> 0.05-0.3
                            float amplitude = mix(0.05, 0.3, uStrength);
                            float frequency = mix(4.0, 12.0, uFrequency);
                            float speed = mix(1.0, 4.0, uSpeed);
                            
                            // Enhanced wave effect: multi-directional waves
                            float wave1 = amplitude * sin(coord.y * frequency + uTime * speed);
                            float wave2 = amplitude * 0.5 * sin(coord.x * frequency * 0.7 + uTime * speed * 1.3);
                            
                            // Apply wave distortion
                            coord.x += wave1;
                            coord.y += wave2;
                            
                            // Ensure coordinates are within valid range
                            coord = clamp(coord, 0.0, 1.0);
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                        """.trimIndent()
                    }
                    "barrel" -> {
                        """
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
                            // Barrel effect: edges contract inward, center relatively normal
                            vec2 center = vec2(0.5);
                            vec2 pos = vTextureCoord - center;
                            
                            // Aspect correction
                            pos.x *= uAspect;
                            float r = length(pos);
                            
                            // Parameterized strength mapping: 0-1 -> 0.1-1.5
                            float S = mix(0.1, 1.5, uStrength);
                            float R = mix(0.2, 1.0, uRadius);
                            
                            if (r > 0.0) {
                                // Barrel distortion: farther from center, more obvious contraction
                                float factor = 1.0 - S * r;
                                factor = max(factor, 0.1);
                                factor = mix(1.0, factor, smoothstep(0.0, R, r));
                                
                                pos /= uAspect;
                                vec2 newCoord = center + pos * factor;
                                newCoord = clamp(newCoord, vec2(0.0), vec2(1.0));
                                gl_FragColor = texture2D(uTexture, newCoord);
                            } else {
                                gl_FragColor = texture2D(uTexture, vTextureCoord);
                            }
                        }
                        """.trimIndent()
                    }
                    "pincushion" -> {
                        """
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
                            vec2 center = vec2(0.5);
                            vec2 pos = vTextureCoord - center;
                            
                            // Aspect correction
                            pos.x *= uAspect;
                            float r = length(pos);
                            
                            // Parameterized strength mapping: 0-1 -> 0.1-1.0
                            float S = mix(0.1, 1.0, uStrength);
                            float R = mix(0.2, 1.0, uRadius);
                            
                            if (r > 0.0) {
                                // Pincushion distortion: edges expand outward factor = 1.0 + k * r²
                                float factor = 1.0 + S * r * r;
                                factor = min(factor, 2.0); // Limit maximum expansion
                                factor = mix(1.0, factor, smoothstep(0.0, R, r));
                                
                                pos /= uAspect;
                                vec2 newCoord = center + pos * factor;
                                newCoord = clamp(newCoord, vec2(0.0), vec2(1.0));
                                gl_FragColor = texture2D(uTexture, newCoord);
                            } else {
                                gl_FragColor = texture2D(uTexture, vTextureCoord);
                            }
                        }
                        """.trimIndent()
                    }
                    "ripple" -> {
                        """
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
                            vec2 center = vec2(0.5);
                            vec2 pos = vTextureCoord - center;
                            
                            // Aspect correction
                            pos.x *= uAspect;
                            float r = length(pos);
                            
                            // Super enhanced strength mapping: 0-1 -> 0.3-5.0 (much stronger)
                            float amplitude = mix(0.3, 5.0, uStrength);
                            float frequency = mix(4.0, 20.0, uFrequency);
                            float speed = mix(2.0, 12.0, uSpeed);
                            
                            if (r > 0.0) {
                                // Ripple effect: concentric circular ripples
                                float ripple = amplitude * sin(r * frequency - uTime * speed);
                                
                                // Super enhanced ripple effect to coordinates (much stronger multiplier)
                                vec2 newCoord = vTextureCoord + pos * ripple * 2.0 / max(r, 0.01);
                                
                                // Ensure coordinates are within valid range
                                newCoord = clamp(newCoord, 0.0, 1.0);
                                gl_FragColor = texture2D(uTexture, newCoord);
                            } else {
                                gl_FragColor = texture2D(uTexture, vTextureCoord);
                            }
                        }
                        """.trimIndent()
                    }
                    "slim_face" -> {
                        """
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
                            vec2 coord = vTextureCoord;
                            // Slim face effect: use Gaussian weight to locally compress x coordinates
                            float S = mix(0.1, 0.7, uStrength);
                            float sigma = mix(0.05, 0.15, uRadius); // Stricter sigma limit
                            float centerX = 0.5;
                            float dx = coord.x - centerX;
                            float weight = exp(- (dx * dx) / (2.0 * sigma * sigma)); // Gaussian weight
                            float squeezeFactor = mix(1.0, 1.0 - S, weight);
                            coord.x = (coord.x - centerX) * squeezeFactor + centerX;
                            coord = clamp(coord, vec2(0.0), vec2(1.0));
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                        """.trimIndent()
                    }
                    "stretch" -> {
                        """
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
                            vec2 coord = vTextureCoord;
                            // Center vertical stretch: global vertical stretch using Gaussian weight (maximum in center)
                            float S = mix(0.1, 1.0, uStrength);
                            float sigma = mix(0.08, 0.3, uRadius); // For Gaussian weight
                            float centerY = 0.5;
                            float dy = coord.y - centerY;
                            float weight = exp(- (dy * dy) / (2.0 * sigma * sigma)); // Gaussian weight
                            float stretchFactor = mix(1.0, 1.0 + S, weight);
                            coord.y = (coord.y - centerY) / stretchFactor + centerY;
                            coord = clamp(coord, vec2(0.0), vec2(1.0));
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                        """.trimIndent()
                    }
                    "distort" -> {
                        """
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
                            vec2 coord = vTextureCoord;
                            
                            // Distortion effect: periodic noise-like distortion
                            float S = mix(0.1, 0.4, uStrength);
                            float frequency = mix(4.0, 12.0, uFrequency);
                            
                            coord.x += S * sin(coord.y * frequency + uTime);
                            coord.y += S * cos(coord.x * frequency + uTime);
                            
                            // Ensure coordinates are within valid range
                            coord = clamp(coord, 0.0, 1.0);
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                        """.trimIndent()
                    }
                    "mirror" -> {
                        """
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
                            vec2 coord = vTextureCoord;
                            
                            // Mirror effect: horizontal flip
                            coord.x = 1.0 - coord.x;
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                        """.trimIndent()
                    }
                    else -> {
                        println("SimpleOpenGLMirrorView: Using pass-through shader (no effect)")
                        // If hardcoded shader is also not found, use default pass-through shader
                        """
                        precision mediump float;
                        uniform sampler2D uTexture;
                        uniform float uTime;
                        varying vec2 vTextureCoord;
                        
                        void main() {
                            gl_FragColor = texture2D(uTexture, vTextureCoord);
                        }
                        """.trimIndent()
                    }
                }
            } else {
                // No effect - show black background instead of original preview
                """
                precision mediump float;
                uniform sampler2D uTexture;
                uniform float uTime;
                varying vec2 vTextureCoord;
                
                void main() {
                    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0); // Black background
                }
                """.trimIndent()
            }
            return createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        }
        
        private fun createShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            
            if (compileStatus[0] == 0) {
                val info = GLES20.glGetShaderInfoLog(shader)
                Log.e("SimpleOpenGLMirrorView", "Shader compilation failed: $info")
                GLES20.glDeleteShader(shader)
                return 0
            }
            
            return shader
        }
        
        private fun createShaderProgram(vertexShader: Int, fragmentShader: Int): Int {
            val program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            
            if (linkStatus[0] == 0) {
                val info = GLES20.glGetProgramInfoLog(program)
                Log.e("SimpleOpenGLMirrorView", "Program linking failed: $info")
                GLES20.glDeleteProgram(program)
                return 0
            }
            
            return program
        }
    }
}
