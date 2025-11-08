package com.gf.mirror.ui.components

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import com.gf.mirror.core.opengl.MirrorEffect
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * OpenGL mirror view
 * Applies real mirror effects to camera preview
 */
class OpenGLMirrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GLSurfaceView(context, attrs) {
    
    private var currentEffect: MirrorEffect? = null
    private var needsShaderRecompile = false
    private var shaderProgram: Int = 0
    private var oesTextureId: Int = 0
    private var surfaceTexture: SurfaceTexture? = null
    private val textureMatrix = FloatArray(16)
    
    // Vertex data
    private val vertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f,  // Bottom left
         1.0f, -1.0f, 0.0f,  // Bottom right
        -1.0f,  1.0f, 0.0f,  // Top left
         1.0f,  1.0f, 0.0f   // Top right
    )
    
    // Texture coordinates
    private val textureCoords = floatArrayOf(
        0.0f, 1.0f,  // Bottom left
        1.0f, 1.0f,  // Bottom right
        0.0f, 0.0f,  // Top left
        1.0f, 0.0f   // Top right
    )
    
    // MVP matrix
    private val mvpMatrix = FloatArray(16)
    
    // Vertex and texture coordinate buffers
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureBuffer: FloatBuffer
    
    // Shader attribute locations
    private var positionHandle: Int = 0
    private var textureCoordHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var textureHandle: Int = 0
    private var textureMatrixHandle: Int = 0
    private var timeHandle: Int = 0
    
    init {
        try {
            // Set OpenGL ES 2.0
            setEGLContextClientVersion(2)
            
            // Initialize buffers
            initBuffers()
            
            // Initialize MVP matrix to identity matrix
            Matrix.setIdentityM(mvpMatrix, 0)
            
            // Set renderer
            setRenderer(OpenGLRenderer())
            
            // Set render mode to continuous rendering
            renderMode = RENDERMODE_CONTINUOUSLY
        } catch (e: Exception) {
            Log.e("OpenGLMirrorView", "Failed to initialize OpenGL", e)
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
    
    private fun createOESTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        val textureId = textureIds[0]
        
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        
        return textureId
    }
    
    fun getSurfaceTexture(): SurfaceTexture? {
        return surfaceTexture
    }
    
    /**
     * Set current effect
     */
    fun setEffect(effect: MirrorEffect) {
        currentEffect = effect
        Log.d("OpenGLMirrorView", "Effect set: ${effect.name} (ID: ${effect.id})")
        println("GFMirror: OpenGLMirrorView - Effect set: ${effect.name} (ID: ${effect.id})")
        
        // Mark need to recompile shaders
        needsShaderRecompile = true
        requestRender()
    }
    
    /**
     * Get current effect
     */
    fun getCurrentEffect(): MirrorEffect? = currentEffect
    
    /**
     * Release resources
     */
    fun release() {
        if (shaderProgram != 0) {
            GLES20.glDeleteProgram(shaderProgram)
            shaderProgram = 0
        }
        surfaceTexture?.release()
    }
    
    /**
     * OpenGL renderer
     */
    private inner class OpenGLRenderer : GLSurfaceView.Renderer {
        
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            try {
                Log.d("OpenGLMirrorView", "Surface created")
                println("GFMirror: OpenGLMirrorView - Surface created")
                // Set background color to black to ensure visibility
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
                
                // Enable depth testing
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                
                // Create OES texture
                oesTextureId = createOESTexture()
                println("GFMirror: OpenGLMirrorView - OES texture created: $oesTextureId")
                
                // Create SurfaceTexture
                surfaceTexture = SurfaceTexture(oesTextureId).apply {
                    setDefaultBufferSize(1280, 720)
                    setOnFrameAvailableListener {
                        requestRender()
                    }
                }
                println("GFMirror: OpenGLMirrorView - SurfaceTexture created")
                
                // Initialize shaders
                initializeShaders()
                println("GFMirror: OpenGLMirrorView - Shaders initialized")
            } catch (e: Exception) {
                Log.e("OpenGLMirrorView", "Error in onSurfaceCreated", e)
                println("GFMirror: OpenGLMirrorView - Error in onSurfaceCreated: ${e.message}")
            }
        }
        
        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            try {
                Log.d("OpenGLMirrorView", "Surface changed: ${width}x${height}")
                GLES20.glViewport(0, 0, width, height)
                
                // Set projection matrix
                val ratio = width.toFloat() / height.toFloat()
                Matrix.frustumM(mvpMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
            } catch (e: Exception) {
                Log.e("OpenGLMirrorView", "Error in onSurfaceChanged", e)
            }
        }
        
        override fun onDrawFrame(gl: GL10?) {
            try {
                // Update SurfaceTexture
                surfaceTexture?.updateTexImage()
                surfaceTexture?.getTransformMatrix(textureMatrix)
                
                // Add SurfaceTexture state debugging
                if (System.currentTimeMillis() % 3000 < 16) { // Print every 3 seconds
                    println("GFMirror: OpenGLMirrorView - SurfaceTexture updated, matrix: ${textureMatrix.joinToString()}")
                }
                
                // Clear color and depth buffers
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                
                // Add debug logs
                if (System.currentTimeMillis() % 2000 < 16) { // Print every 2 seconds
                    println("GFMirror: OpenGLMirrorView - onDrawFrame: oesTextureId=$oesTextureId, surfaceTexture=${surfaceTexture != null}")
                    println("GFMirror: OpenGLMirrorView - onDrawFrame: shaderProgram=$shaderProgram, currentEffect=${currentEffect?.name}")
                }
                
                // Check if shaders need to be recompiled
                if (needsShaderRecompile || shaderProgram == 0) {
                    println("GFMirror: OpenGLMirrorView - Recompiling shaders for effect: ${currentEffect?.name ?: "none"}")
                    if (shaderProgram != 0) {
                        GLES20.glDeleteProgram(shaderProgram)
                        shaderProgram = 0
                    }
                    initializeShaders()
                    needsShaderRecompile = false
                    println("GFMirror: OpenGLMirrorView - Shaders recompiled successfully")
                }
                
                // Use shader program for rendering
                if (shaderProgram != 0) {
                    GLES20.glUseProgram(shaderProgram)
                    
                    // Check OpenGL errors
                    val error = GLES20.glGetError()
                    if (error != GLES20.GL_NO_ERROR) {
                        println("GFMirror: OpenGLMirrorView - OpenGL error after glUseProgram: $error")
                    }
                    
                    // Set vertex attributes
                    setupVertexAttributes()
                    
                    // Check vertex attribute setup
                    val error2 = GLES20.glGetError()
                    if (error2 != GLES20.GL_NO_ERROR) {
                        println("GFMirror: OpenGLMirrorView - OpenGL error after setupVertexAttributes: $error2")
                    }
                    
                    // Set MVP matrix
                    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
                    
                    // Set texture matrix
                    GLES20.glUniformMatrix4fv(textureMatrixHandle, 1, false, textureMatrix, 0)
                    
                    // Bind OES texture
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
                    GLES20.glUniform1i(textureHandle, 0)
                    
                    // Check texture binding state
                    val textureError = GLES20.glGetError()
                    if (textureError != GLES20.GL_NO_ERROR) {
                        println("GFMirror: OpenGLMirrorView - Texture binding error: $textureError")
                    }
                    
                    // Add texture binding debug logs
                    if (System.currentTimeMillis() % 2000 < 16) { // Print every 2 seconds
                        println("GFMirror: OpenGLMirrorView - Texture binding: oesTextureId=$oesTextureId, textureHandle=$textureHandle")
                        println("GFMirror: OpenGLMirrorView - Uniforms: mvpMatrixHandle=$mvpMatrixHandle, textureMatrixHandle=$textureMatrixHandle, timeHandle=$timeHandle")
                    }
                    
                    // Set time uniform (for animation effects)
                    val time = System.currentTimeMillis() / 1000.0f
                    GLES20.glUniform1f(timeHandle, time)
                    
                    // Check uniform setup
                    val error3 = GLES20.glGetError()
                    if (error3 != GLES20.GL_NO_ERROR) {
                        println("GFMirror: OpenGLMirrorView - OpenGL error after setting uniforms: $error3")
                    }
                    
                    // Draw
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
                    
                    // Check draw errors
                    val error4 = GLES20.glGetError()
                    if (error4 != GLES20.GL_NO_ERROR) {
                        println("GFMirror: OpenGLMirrorView - OpenGL error after glDrawArrays: $error4")
                    }
                    
                    // Disable vertex attributes
                    GLES20.glDisableVertexAttribArray(positionHandle)
                    GLES20.glDisableVertexAttribArray(textureCoordHandle)
                    
                    // Print logs every 100 frames
                    if (System.currentTimeMillis() % 1000 < 16) { // Print approximately every second
                        println("GFMirror: OpenGLMirrorView - Drawing frame with shader, effect: ${currentEffect?.name ?: "none"}")
                        println("GFMirror: OpenGLMirrorView - Shader program: $shaderProgram, positionHandle: $positionHandle, textureCoordHandle: $textureCoordHandle")
                    }
                } else {
                    println("GFMirror: OpenGLMirrorView - No shader program available")
                }
            } catch (e: Exception) {
                Log.e("OpenGLMirrorView", "Error in onDrawFrame", e)
                println("GFMirror: OpenGLMirrorView - Error in onDrawFrame: ${e.message}")
            }
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
            textureMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uTexMatrix")
            textureHandle = GLES20.glGetUniformLocation(shaderProgram, "uTexture")
            timeHandle = GLES20.glGetUniformLocation(shaderProgram, "uTime")
            
            // Add uniform acquisition debug logs
            println("GFMirror: OpenGLMirrorView - Uniform locations: positionHandle=$positionHandle, textureCoordHandle=$textureCoordHandle")
            println("GFMirror: OpenGLMirrorView - Uniform locations: mvpMatrixHandle=$mvpMatrixHandle, textureMatrixHandle=$textureMatrixHandle")
            println("GFMirror: OpenGLMirrorView - Uniform locations: textureHandle=$textureHandle, timeHandle=$timeHandle")
            
            println("GFMirror: OpenGLMirrorView - Shaders initialized for effect: ${currentEffect?.name ?: "none"}")
        }
        
        private fun createVertexShader(): Int {
            val vertexShaderCode = """
                uniform mat4 uMVPMatrix;
                uniform mat4 uTexMatrix;
                attribute vec4 aPosition;
                attribute vec2 aTextureCoord;
                varying vec2 vTextureCoord;
                
                void main() {
                    gl_Position = uMVPMatrix * aPosition;
                    vec4 tc = uTexMatrix * vec4(aTextureCoord, 0.0, 1.0);
                    vTextureCoord = tc.xy;
                }
            """.trimIndent()
            
            return createShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        }
        
        private fun createFragmentShader(): Int {
            val effect = currentEffect
            println("GFMirror: OpenGLMirrorView - Creating fragment shader for effect: ${effect?.name ?: "none"} (ID: ${effect?.id ?: "none"})")
            
            val fragmentShaderCode = if (effect != null) {
                // Use OES texture and continuous deformation algorithm
                when (effect.id) {
                    "fisheye" -> {
                        println("GFMirror: OpenGLMirrorView - Using fisheye shader with OES")
                        """
                        #extension GL_OES_EGL_image_external : require
                        precision mediump float;
                        uniform samplerExternalOES uTexture;
                        uniform float uTime;
                        varying vec2 vTextureCoord;
                        
                        void main() {
                            // Simplest test: directly display texture
                            gl_FragColor = texture2D(uTexture, vTextureCoord);
                        }
                    """.trimIndent()
                    }
                    "horizontal_stretch" -> {
                        println("GFMirror: OpenGLMirrorView - Using horizontal_stretch shader with OES")
                        """
                        #extension GL_OES_EGL_image_external : require
                        precision mediump float;
                        uniform samplerExternalOES uTexture;
                        uniform float uTime;
                        varying vec2 vTextureCoord;
                        
                        void main() {
                            // Horizontal stretch effect
                            vec2 coord = vTextureCoord;
                            float stretchFactor = 0.3;
                            
                            // Horizontal stretch distortion
                            coord.x = coord.x + stretchFactor * sin(coord.y * 10.0 + uTime * 2.0);
                            coord.x = mod(coord.x, 1.0);
                            
                            // Use OES texture sampling
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                    """.trimIndent()
                    }
                    "vertical_stretch" -> {
                        println("GFMirror: OpenGLMirrorView - Using vertical_stretch shader with OES")
                        """
                        #extension GL_OES_EGL_image_external : require
                        precision mediump float;
                        uniform samplerExternalOES uTexture;
                        uniform float uTime;
                        varying vec2 vTextureCoord;
                        
                        void main() {
                            // Vertical stretch effect
                            vec2 coord = vTextureCoord;
                            float stretchFactor = 0.3;
                            
                            // Vertical stretch distortion
                            coord.y = coord.y + stretchFactor * sin(coord.x * 10.0 + uTime * 2.0);
                            coord = clamp(coord, 0.0, 1.0);
                            
                            // Use OES texture sampling
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                    """.trimIndent()
                    }
                    "twist" -> {
                        println("GFMirror: OpenGLMirrorView - Using twist shader with OES")
                        """
                        #extension GL_OES_EGL_image_external : require
                        precision mediump float;
                        uniform samplerExternalOES uTexture;
                        uniform float uTime;
                        varying vec2 vTextureCoord;
                        
                        void main() {
                            // Twist effect
                            vec2 center = vec2(0.5, 0.5);
                            vec2 coord = vTextureCoord - center;
                            
                            // Calculate polar coordinates
                            float angle = atan(coord.y, coord.x);
                            float radius = length(coord);
                            
                            // Twist distortion
                            float twistFactor = 2.0;
                            angle += radius * twistFactor + uTime;
                            
                            // Convert back to rectangular coordinates
                            vec2 twistedCoord = center + radius * vec2(cos(angle), sin(angle));
                            twistedCoord = clamp(twistedCoord, 0.0, 1.0);
                            
                            // Use OES texture sampling
                            gl_FragColor = texture2D(uTexture, twistedCoord);
                        }
                    """.trimIndent()
                    }
                    "bulge" -> {
                        println("GFMirror: OpenGLMirrorView - Using bulge shader with OES")
                        """
                        #extension GL_OES_EGL_image_external : require
                        precision mediump float;
                        uniform samplerExternalOES uTexture;
                        uniform float uTime;
                        varying vec2 vTextureCoord;
                        
                        void main() {
                            // Bulge effect
                            vec2 center = vec2(0.5, 0.5);
                            vec2 coord = vTextureCoord - center;
                            float distance = length(coord);
                            
                            // Bulge distortion - add time animation
                            float bulgeFactor = 0.8 + 0.2 * sin(uTime * 1.5);
                            float bulge = 1.0 - distance * distance * bulgeFactor;
                            bulge = max(0.0, bulge);
                            
                            vec2 newCoord = center + coord * bulge;
                            newCoord = clamp(newCoord, 0.0, 1.0);
                            
                            // Use OES texture sampling
                            gl_FragColor = texture2D(uTexture, newCoord);
                        }
                    """.trimIndent()
                    }
                    "wave" -> {
                        println("GFMirror: OpenGLMirrorView - Using wave shader with OES")
                        """
                        #extension GL_OES_EGL_image_external : require
                        precision mediump float;
                        uniform samplerExternalOES uTexture;
                        uniform float uTime;
                        varying vec2 vTextureCoord;
                        
                        void main() {
                            // Wave effect
                            vec2 coord = vTextureCoord;
                            float waveFactor = 0.2;
                            
                            // Wave distortion
                            float wave1 = sin(coord.x * 20.0 + uTime * 3.0) * waveFactor;
                            float wave2 = sin(coord.y * 15.0 + uTime * 2.0) * waveFactor * 0.5;
                            
                            coord.x += wave1;
                            coord.y += wave2;
                            coord = clamp(coord, 0.0, 1.0);
                            
                            // Use OES texture sampling
                            gl_FragColor = texture2D(uTexture, coord);
                        }
                    """.trimIndent()
                    }
                    else -> effect.shaderCode
                }
            } else {
                // Default fragment shader - display gray
                println("GFMirror: OpenGLMirrorView - Using default shader (GRAY)")
                """
                    precision mediump float;
                    uniform float uTime;
                    varying vec2 vTextureCoord;
                    
                    void main() {
                        gl_FragColor = vec4(0.5, 0.5, 0.5, 1.0);
                    }
                """.trimIndent()
            }
            
            println("GFMirror: OpenGLMirrorView - Fragment shader code length: ${fragmentShaderCode.length}")
            return createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        }
        
        private fun createShader(type: Int, shaderCode: String): Int {
            val shaderType = if (type == GLES20.GL_VERTEX_SHADER) "VERTEX" else "FRAGMENT"
            println("GFMirror: OpenGLMirrorView - Creating $shaderType shader")
            
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            
            if (compileStatus[0] == 0) {
                val error = GLES20.glGetShaderInfoLog(shader)
                Log.e("OpenGLMirrorView", "Shader compilation failed: $error")
                println("GFMirror: OpenGLMirrorView - $shaderType shader compilation FAILED: $error")
                GLES20.glDeleteShader(shader)
                throw RuntimeException("Shader compilation failed: $error")
            } else {
                println("GFMirror: OpenGLMirrorView - $shaderType shader compilation SUCCESS")
            }
            
            return shader
        }
        
        private fun createShaderProgram(vertexShader: Int, fragmentShader: Int): Int {
            println("GFMirror: OpenGLMirrorView - Creating shader program")
            val program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            
            if (linkStatus[0] == 0) {
                val error = GLES20.glGetProgramInfoLog(program)
                Log.e("OpenGLMirrorView", "Program linking failed: $error")
                println("GFMirror: OpenGLMirrorView - Program linking FAILED: $error")
                GLES20.glDeleteProgram(program)
                throw RuntimeException("Program linking failed: $error")
            } else {
                println("GFMirror: OpenGLMirrorView - Program linking SUCCESS")
            }
            
            return program
        }
        
        private fun setupVertexAttributes() {
            // Set vertex positions
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
            
            // Set texture coordinates
            GLES20.glEnableVertexAttribArray(textureCoordHandle)
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        }
    }
}
