package com.gf.mirror.core.opengl

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL rendering engine interface
 */
interface MirrorRenderEngine {
    /**
     * Initialize rendering engine
     */
    suspend fun initialize(context: Context): Result<Unit>
    
    /**
     * Set current effect
     */
    fun setEffect(effect: MirrorEffect)
    
    /**
     * Render one frame
     */
    fun renderFrame(textureId: Int, timestamp: Long)
    
    /**
     * Capture current frame as Bitmap
     */
    fun captureFrame(): Bitmap?
    
    /**
     * Release resources
     */
    fun release()
    
    /**
     * Get current effect
     */
    fun getCurrentEffect(): MirrorEffect?
}

/**
 * OpenGL rendering engine implementation
 */
class OpenGLMirrorRenderEngine : MirrorRenderEngine, GLSurfaceView.Renderer {
    
    private var currentEffect: MirrorEffect? = null
    private var shaderProgram: Int = 0
    private var textureHandle: Int = 0
    private var positionHandle: Int = 0
    private var textureCoordHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    
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
    
    override suspend fun initialize(context: Context): Result<Unit> = try {
        // Initialize basic shaders
        initializeShaders()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override fun setEffect(effect: MirrorEffect) {
        currentEffect = effect
        // Recompile shaders
        compileShader(effect.shaderCode)
    }
    
    override fun renderFrame(textureId: Int, timestamp: Long) {
        // This method will be called by GLSurfaceView.Renderer's onDrawFrame
        // Actual rendering logic is implemented in onDrawFrame
    }
    
    override fun captureFrame(): Bitmap? {
        // TODO: Implement frame capture functionality
        return null
    }
    
    override fun release() {
        if (shaderProgram != 0) {
            GLES20.glDeleteProgram(shaderProgram)
            shaderProgram = 0
        }
    }
    
    override fun getCurrentEffect(): MirrorEffect? = currentEffect
    
    // GLSurfaceView.Renderer implementation
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set background color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        
        // Initialize shaders
        initializeShaders()
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        
        // Set projection matrix
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(mvpMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        // Clear color and depth buffers
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        // Use shader program
        GLES20.glUseProgram(shaderProgram)
        
        // Set vertex attributes
        setupVertexAttributes()
        
        // Set MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        
        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        
        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordHandle)
    }
    
    private fun initializeShaders() {
        // Use default vertex shader
        val vertexShader = createVertexShader()
        val fragmentShader = createDefaultFragmentShader()
        
        shaderProgram = createShaderProgram(vertexShader, fragmentShader)
        
        // Get attribute locations
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "aTextureCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        textureHandle = GLES20.glGetUniformLocation(shaderProgram, "uTexture")
    }
    
    private fun compileShader(shaderCode: String) {
        // Compile new shader
        val fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER, shaderCode)
        if (fragmentShader != 0) {
            val vertexShader = createVertexShader()
            val newProgram = createShaderProgram(vertexShader, fragmentShader)
            
            if (newProgram != 0) {
                // Delete old program
                if (shaderProgram != 0) {
                    GLES20.glDeleteProgram(shaderProgram)
                }
                shaderProgram = newProgram
                
                // Re-get attribute locations
                positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
                textureCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "aTextureCoord")
                mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
                textureHandle = GLES20.glGetUniformLocation(shaderProgram, "uTexture")
            }
        }
    }
    
    private fun setupVertexAttributes() {
        // Set vertex positions
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, createFloatBuffer(vertices))
        
        // Set texture coordinates
        GLES20.glEnableVertexAttribArray(textureCoordHandle)
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, createFloatBuffer(textureCoords))
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
    
    private fun createDefaultFragmentShader(): Int {
        val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            varying vec2 vTextureCoord;
            
            void main() {
                gl_FragColor = texture2D(uTexture, vTextureCoord);
            }
        """.trimIndent()
        
        return createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
    }
    
    private fun createShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Shader compilation failed: $error")
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
            val error = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            throw RuntimeException("Program linking failed: $error")
        }
        
        return program
    }
    
    private fun createFloatBuffer(array: FloatArray): java.nio.FloatBuffer {
        val buffer = java.nio.ByteBuffer.allocateDirect(array.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(array)
        buffer.position(0)
        return buffer
    }
}