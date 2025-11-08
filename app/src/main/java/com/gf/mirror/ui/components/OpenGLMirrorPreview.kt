package com.gf.mirror.ui.components

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import com.gf.mirror.core.opengl.MirrorEffect
import com.gf.mirror.core.opengl.MirrorRenderEngine
import com.gf.mirror.core.opengl.OpenGLMirrorRenderEngine
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL mirror preview component
 * Applies various mirror effects
 */
class OpenGLMirrorPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GLSurfaceView(context, attrs) {
    
    private var renderEngine: MirrorRenderEngine? = null
    private var currentEffect: MirrorEffect? = null
    private var textureId: Int = 0
    
    init {
        try {
            // Set OpenGL ES 2.0
            setEGLContextClientVersion(2)
            
            // Set renderer
            renderEngine = OpenGLMirrorRenderEngine()
            setRenderer(OpenGLRenderer())
            
            // Set render mode to continuous rendering
            renderMode = RENDERMODE_CONTINUOUSLY
        } catch (e: Exception) {
            Log.e("OpenGLMirrorPreview", "Failed to initialize OpenGL", e)
        }
    }
    
    /**
     * Set current effect
     */
    fun setEffect(effect: MirrorEffect) {
        currentEffect = effect
        renderEngine?.setEffect(effect)
        Log.d("OpenGLMirrorPreview", "Effect set: ${effect.name}")
    }
    
    /**
     * Get current effect
     */
    fun getCurrentEffect(): MirrorEffect? = currentEffect
    
    /**
     * Release resources
     */
    fun release() {
        renderEngine?.release()
    }
    
    /**
     * OpenGL renderer
     */
    private inner class OpenGLRenderer : GLSurfaceView.Renderer {
        
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            try {
                Log.d("OpenGLMirrorPreview", "Surface created")
                // Set background color
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            } catch (e: Exception) {
                Log.e("OpenGLMirrorPreview", "Error in onSurfaceCreated", e)
            }
        }
        
        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            try {
                Log.d("OpenGLMirrorPreview", "Surface changed: ${width}x${height}")
                GLES20.glViewport(0, 0, width, height)
            } catch (e: Exception) {
                Log.e("OpenGLMirrorPreview", "Error in onSurfaceChanged", e)
            }
        }
        
        override fun onDrawFrame(gl: GL10?) {
            try {
                // Render different colors based on effect
                val effect = currentEffect
                when (effect?.id) {
                    "fisheye" -> GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f) // Red
                    "horizontal_stretch" -> GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f) // Green
                    "vertical_stretch" -> GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f) // Blue
                    "twist" -> GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f) // Yellow
                    "bulge" -> GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f) // Magenta
                    "wave" -> GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f) // Cyan
                    else -> GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f) // Gray
                }
                
                // Clear color buffer
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            } catch (e: Exception) {
                Log.e("OpenGLMirrorPreview", "Error in onDrawFrame", e)
            }
        }
    }
}
