package com.oney.WebRTCModule;

/*
 *  Copyright 2015 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import org.webrtc.GlShader;
import org.webrtc.GlUtil;
import org.webrtc.RendererCommon;

import java.nio.FloatBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Helper class to draw an opaque quad on the target viewport location. Rotation, mirror, and
 * cropping is specified using a 4x4 texture coordinate transform matrix. The frame input can either
 * be an OES texture or YUV textures in I420 format. The GL state must be preserved between draw
 * calls, this is intentional to maximize performance. The function release() must be called
 * manually to free the resources held by this object.
 */
public class GlDrawer implements RendererCommon.GlDrawer {
    // clang-format off
    // Simple vertex shader, used for both YUV and OES.
    private static final String VERTEX_SHADER_STRING =
            "varying vec2 interp_tc;\n"
                    + "attribute vec4 in_pos;\n"
                    + "attribute vec4 in_tc;\n"
                    + "\n"
                    + "uniform mat4 texMatrix;\n"
                    + "\n"
                    + "void main() {\n"
                    + "    gl_Position = in_pos;\n"
                    + "    interp_tc = (texMatrix * in_tc).xy;\n"
                    + "}\n";

    private static final String YUV_FRAGMENT_SHADER_STRING =
            "precision mediump float;\n"
                    + "varying vec2 interp_tc;\n"
                    + "\n"
                    + "uniform sampler2D y_tex;\n"
                    + "uniform sampler2D u_tex;\n"
                    + "uniform sampler2D v_tex;\n"
                    + "\n"
                    + "void main() {\n"
                    // CSC according to http://www.fourcc.org/fccyvrgb.php
                    + "  float y = texture2D(y_tex, interp_tc).r;\n"
                    + "  float u = texture2D(u_tex, interp_tc).r - 0.5;\n"
                    + "  float v = texture2D(v_tex, interp_tc).r - 0.5;\n"
                    + "  vec4 sourcePixel = vec4(y + 1.403 * v, "
                    + "                      y - 0.344 * u - 0.714 * v, "
                    + "                      y + 1.77 * u, 1);\n"
                    +"  float pixelSat, secondaryComponents, secondaryComponents1;\n"
                    +"  float fmin = min(min(sourcePixel.r, sourcePixel.g), sourcePixel.b);\n"
                    +"  float fmax = max(max(sourcePixel.r, sourcePixel.g), sourcePixel.b);\n"
                    +"  vec4 screen = vec4(0.0,1.0,0.0,1.0);\n"
                    +"	float fmax1 = max(max(screen.r, screen.g), screen.b);\n"
                    +"	float fmin1 = min(min(screen.r, screen.g), screen.b);\n"
                    +"  vec3 screenPrimary = step(fmax1, screen.rgb);\n"
                    +"  vec3 pixelPrimary = step(fmax, sourcePixel.rgb);\n"
                    +"  secondaryComponents = dot(1.0 - pixelPrimary, sourcePixel.rgb);\n"
                    +"  secondaryComponents1 = dot(1.0 - screenPrimary, screen.rgb);\n"
                    +"  float screenSat = fmax1 - mix(secondaryComponents1 - fmin1, secondaryComponents1 / 2.0, 1.0);\n"
                    +"  pixelSat = fmax - mix(secondaryComponents - fmin, secondaryComponents / 2.0, 1.0);\n"
                    +"  float diffPrimary = dot(abs(pixelPrimary - screenPrimary), vec3(1.0));\n"
                    +"  float solid = step(1.0, step(pixelSat, 0.1) + step(fmax, 0.1) + diffPrimary);\n"
                    +"  float alpha = max(0.0, 1.0 - pixelSat / screenSat);\n"
                    +"  alpha = smoothstep(0.0, 1.0, alpha);\n"
                    +"  vec4 semiTransparentPixel = vec4((sourcePixel.rgb - (1.0 - alpha) * screen.rgb * 1.0) / max(0.00001, alpha), alpha);\n"
                    +"  vec4 pixel = mix(semiTransparentPixel, sourcePixel, solid);\n"
                    +"  gl_FragColor=pixel;\n"
                    + "}\n";

    private static final String RGB_FRAGMENT_SHADER_STRING =
            "precision mediump float;\n"
                    + "varying vec2 interp_tc;\n"
                    + "\n"
                    + "uniform sampler2D rgb_tex;\n"
                    + "\n"
                    + "void main() {\n"
                    +"  float pixelSat, secondaryComponents, secondaryComponents1;\n"
                    +"  vec4 sourcePixel = texture2D(rgb_tex, interp_tc);\n"
                    +"  float fmin = min(min(sourcePixel.r, sourcePixel.g), sourcePixel.b);\n"
                    +"  float fmax = max(max(sourcePixel.r, sourcePixel.g), sourcePixel.b);\n"
                    +"  vec4 screen = vec4(0.0,1.0,0.0,1.0);\n"
                    +"	float fmax1 = max(max(screen.r, screen.g), screen.b);\n"
                    +"	float fmin1 = min(min(screen.r, screen.g), screen.b);\n"
                    +"  vec3 screenPrimary = step(fmax1, screen.rgb);\n"
                    +"  vec3 pixelPrimary = step(fmax, sourcePixel.rgb);\n"
                    +"  secondaryComponents = dot(1.0 - pixelPrimary, sourcePixel.rgb);\n"
                    +"  secondaryComponents1 = dot(1.0 - screenPrimary, screen.rgb);\n"
                    +"  float screenSat = fmax1 - mix(secondaryComponents1 - fmin1, secondaryComponents1 / 2.0, 1.0);\n"
                    +"  pixelSat = fmax - mix(secondaryComponents - fmin, secondaryComponents / 2.0, 1.0);\n"
                    +"  float diffPrimary = dot(abs(pixelPrimary - screenPrimary), vec3(1.0));\n"
                    +"  float solid = step(1.0, step(pixelSat, 0.1) + step(fmax, 0.1) + diffPrimary);\n"
                    +"  float alpha = max(0.0, 1.0 - pixelSat / screenSat);\n"
                    +"  alpha = smoothstep(0.0, 1.0, alpha);\n"
                    +"  vec4 semiTransparentPixel = vec4((sourcePixel.rgb - (1.0 - alpha) * screen.rgb * 1.0) / max(0.00001, alpha), alpha);\n"
                    +"  vec4 pixel = mix(semiTransparentPixel, sourcePixel, solid);\n"
                    +"  gl_FragColor=pixel;\n"
                    + "}\n";

    private static final String OES_FRAGMENT_SHADER_STRING =
            "#extension GL_OES_EGL_image_external : require\n"
                    + "precision mediump float;\n"
                    + "varying vec2 interp_tc;\n"
                    + "\n"
                    + "uniform samplerExternalOES oes_tex;\n"
                    + "varying mediump float text_alpha_out;\n"
                    + "\n"
                    + " void main() {\n"
                    +"  float pixelSat, secondaryComponents, secondaryComponents1;\n"
                    +"  vec4 sourcePixel = texture2D(oes_tex, interp_tc);\n"
                    +"  float fmin = min(min(sourcePixel.r, sourcePixel.g), sourcePixel.b);\n"
                    +"  float fmax = max(max(sourcePixel.r, sourcePixel.g), sourcePixel.b);\n"
                    +"  vec4 screen = vec4(0.0,1.0,0.0,1.0);\n"
                    +"	float fmax1 = max(max(screen.r, screen.g), screen.b);\n"
                    +"	float fmin1 = min(min(screen.r, screen.g), screen.b);\n"
                    +"  vec3 screenPrimary = step(fmax1, screen.rgb);\n"
                    +"  vec3 pixelPrimary = step(fmax, sourcePixel.rgb);\n"
                    +"  secondaryComponents = dot(1.0 - pixelPrimary, sourcePixel.rgb);\n"
                    +"  secondaryComponents1 = dot(1.0 - screenPrimary, screen.rgb);\n"
                    +"  float screenSat = fmax1 - mix(secondaryComponents1 - fmin1, secondaryComponents1 / 2.0, 1.0);\n"
                    +"  pixelSat = fmax - mix(secondaryComponents - fmin, secondaryComponents / 2.0, 1.0);\n"
                    +"  float diffPrimary = dot(abs(pixelPrimary - screenPrimary), vec3(1.0));\n"
                    +"  float solid = step(1.0, step(pixelSat, 0.1) + step(fmax, 0.1) + diffPrimary);\n"
                    +"  float alpha = max(0.0, 1.0 - pixelSat / screenSat);\n"
                    +"  alpha = smoothstep(0.0, 1.0, alpha);\n"
                    +"  vec4 semiTransparentPixel = vec4((sourcePixel.rgb - (1.0 - alpha) * screen.rgb * 1.0) / max(0.00001, alpha), alpha);\n"
                    +"  vec4 pixel = mix(semiTransparentPixel, sourcePixel, solid);\n"
                    +"  gl_FragColor=pixel;\n"
                    + "}\n";




    // clang-format on

    // Vertex coordinates in Normalized Device Coordinates, i.e. (-1, -1) is bottom-left and (1, 1) is
    // top-right.
    private static final FloatBuffer FULL_RECTANGLE_BUF = GlUtil.createFloatBuffer(new float[]{
            -1.0f, -1.0f, // Bottom left.
            1.0f, -1.0f, // Bottom right.
            -1.0f, 1.0f, // Top left.
            1.0f, 1.0f, // Top right.
    });

    // Texture coordinates - (0, 0) is bottom-left and (1, 1) is top-right.
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF = GlUtil.createFloatBuffer(new float[]{
            0.0f, 0.0f, // Bottom left.
            1.0f, 0.0f, // Bottom right.
            0.0f, 1.0f, // Top left.
            1.0f, 1.0f // Top right.
    });

    private static class Shader {
        public final GlShader glShader;
        public final int texMatrixLocation;

        public Shader(String fragmentShader) {
            this.glShader = new GlShader(VERTEX_SHADER_STRING, fragmentShader);
            this.texMatrixLocation = glShader.getUniformLocation("texMatrix");
        }
    }

    // The keys are one of the fragments shaders above.
    private final Map<String, Shader> shaders = new IdentityHashMap<String, Shader>();

    /**
     * Draw an OES texture frame with specified texture transformation matrix. Required resources are
     * allocated at the first call to this function.
     */
    @Override
    public void drawOes(int oesTextureId, float[] texMatrix, int frameWidth, int frameHeight,
                        int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
        prepareShader(OES_FRAGMENT_SHADER_STRING, texMatrix);

        // GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);


        //  GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE,GLES20.GL_ZERO);
        //  GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
//        GLES20.glClearColor ( 0.0f, 0.0f, 0.0f, 1.0f );
//        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
//        GLES20.glEnable(GLES20.GL_BLEND);
//       // GLES20.glEnable(GLES20.GL_BLEND);
//       GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        //  GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        // updateTexImage() may be called from another thread in another EGL context, so we need to
        // bind/unbind the texture in each draw call so that GLES understads it's a new texture.
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId);
        drawRectangle(viewportX, viewportY, viewportWidth, viewportHeight);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        //   GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Draw a RGB(A) texture frame with specified texture transformation matrix. Required resources
     * are allocated at the first call to this function.
     */
    @Override
    public void drawRgb(int textureId, float[] texMatrix, int frameWidth, int frameHeight,
                        int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
        prepareShader(RGB_FRAGMENT_SHADER_STRING, texMatrix);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        drawRectangle(viewportX, viewportY, viewportWidth, viewportHeight);
        // Unbind the texture as a precaution.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    /**
     * Draw a YUV frame with specified texture transformation matrix. Required resources are
     * allocated at the first call to this function.
     */
    @Override
    public void drawYuv(int[] yuvTextures, float[] texMatrix, int frameWidth, int frameHeight,
                        int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
        prepareShader(YUV_FRAGMENT_SHADER_STRING, texMatrix);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        // Bind the textures.
        for (int i = 0; i < 3; ++i) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextures[i]);
        }
        drawRectangle(viewportX, viewportY, viewportWidth, viewportHeight);
        // Unbind the textures as a precaution..
        for (int i = 0; i < 3; ++i) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
    }

    private void drawRectangle(int x, int y, int width, int height) {
        // Draw quad.
        GLES20.glViewport(x, y, width, height);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private void prepareShader(String fragmentShader, float[] texMatrix) {
        final Shader shader;
        if (shaders.containsKey(fragmentShader)) {
            shader = shaders.get(fragmentShader);
        } else {
            // Lazy allocation.
            shader = new Shader(fragmentShader);
            shaders.put(fragmentShader, shader);
            shader.glShader.useProgram();
            // Initialize fragment shader uniform values.
            if (fragmentShader == YUV_FRAGMENT_SHADER_STRING) {
                GLES20.glUniform1i(shader.glShader.getUniformLocation("y_tex"), 0);
                GLES20.glUniform1i(shader.glShader.getUniformLocation("u_tex"), 1);
                GLES20.glUniform1i(shader.glShader.getUniformLocation("v_tex"), 2);
            } else if (fragmentShader == RGB_FRAGMENT_SHADER_STRING) {
                GLES20.glUniform1i(shader.glShader.getUniformLocation("rgb_tex"), 0);
            } else if (fragmentShader == OES_FRAGMENT_SHADER_STRING) {
                GLES20.glUniform1i(shader.glShader.getUniformLocation("oes_tex"), 0);
                //  GLES20.glUniform1f(shader.glShader.getUniformLocation("u_alpha"), 0.5f);
            } else {
                throw new IllegalStateException("Unknown fragment shader: " + fragmentShader);
            }
            GlUtil.checkNoGLES2Error("Initialize fragment shader uniform values.");
            // Initialize vertex shader attributes.
            shader.glShader.setVertexAttribArray("in_pos", 2, FULL_RECTANGLE_BUF);
            shader.glShader.setVertexAttribArray("in_tc", 2, FULL_RECTANGLE_TEX_BUF);
        }
        shader.glShader.useProgram();
        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(shader.texMatrixLocation, 1, false, texMatrix, 0);
    }

    /**
     * Release all GLES resources. This needs to be done manually, otherwise the resources are leaked.
     */
    @Override
    public void release() {
        for (Shader shader : shaders.values()) {
            shader.glShader.release();
        }
        shaders.clear();
    }
}