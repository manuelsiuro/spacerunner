package com.msa.spacerunner.models;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.msa.spacerunner.R;
import com.msa.spacerunner.common.RawResourceReader;
import com.msa.spacerunner.shaders.ShadersUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Cube {

    private float[] mLightModelMatrix = new float[16];
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInModelSpace = new float[]{0.0f, 4.0f, 0.0f, 1.0f};
    private final float[] mLightPosInEyeSpace = new float[4];

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private static int _program = -1;
    private static final int a_Position_ID = 0;
    private static final int a_Color_ID = 1;
    private static final int a_Normal_ID = 2;
    private static final int a_TexCoordinate_ID = 3;

    private static int u_MVMatrix;
    private static int u_MVPMatrix;

    private static int a_Position;
    private static int a_Color;
    private static int a_Normal;
    private static int a_TexCoordinate;

    private static int u_LightPos;
    private static int u_Texture;
    private static int u_AmbientLight;

    // X, Y, Z
    private static final float[] cubePositionData =
            {
                    // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
                    // if the points are counter-clockwise we are looking at the "front". If not we are looking at
                    // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
                    // usually represent the backside of an object and aren't visible anyways.

                    // Front face
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,

                    // Right face
                    1.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, -1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Back face
                    1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, -1.0f,

                    // Left face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f,

                    // Top face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Bottom face
                    1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
            };

    // R, G, B, A
    private static final float[] cubeColorData =
            {
                    // Front face (red)
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,

                    // Right face (green)
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,

                    // Back face (blue)
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,

                    // Left face (yellow)
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,

                    // Top face (cyan)
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,

                    // Bottom face (magenta)
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f
            };

    // X, Y, Z
    // The normal is used in light calculations and is a vector which points
    // orthogonal to the plane of the surface. For a cube model, the normals
    // should be orthogonal to the points of each face.
    private static final float[] cubeNormalData =
            {
                    // Front face
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,

                    // Right face
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,

                    // Back face
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,

                    // Left face
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,

                    // Top face
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,

                    // Bottom face
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f
            };

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    private static final float[] cubeTextureCoordinateData =
            {
                    // Front face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Right face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Back face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Left face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Top face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Bottom face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f
            };

    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeColors;
    private final FloatBuffer mCubeNormals;
    private final FloatBuffer mCubeTextureCoordinates;

    private int textureId;

    public Cube(Context context, int tId) {
        // Initialize the buffers.
        mCubePositions = makeFloatBuffer(cubePositionData);
        mCubeColors = makeFloatBuffer(cubeColorData);
        mCubeNormals = makeFloatBuffer(cubeNormalData);
        mCubeTextureCoordinates = makeFloatBuffer(cubeTextureCoordinateData);

        textureId = tId;
        initializeGL(context);
    }

    private void initializeGL(Context context) {
        final String vertexShaderSource = RawResourceReader.readTextFileFromRawResource(context, R.raw.per_pixel_vertex_shader);
        final String fragmentShaderSource = RawResourceReader.readTextFileFromRawResource(context, R.raw.per_pixel_fragment_shader);

        _program = ShadersUtils.createProgram(vertexShaderSource, fragmentShaderSource);

        GLES20.glBindAttribLocation(_program, a_Position_ID, "a_Position");
        GLES20.glBindAttribLocation(_program, a_Color_ID, "a_Color");
        GLES20.glBindAttribLocation(_program, a_Normal_ID, "a_Normal");
        GLES20.glBindAttribLocation(_program, a_TexCoordinate_ID, "a_TexCoordinate");

        GLES20.glLinkProgram(_program);

        u_MVPMatrix = GLES20.glGetUniformLocation(_program, "u_MVPMatrix");
        u_MVMatrix = GLES20.glGetUniformLocation(_program, "u_MVMatrix");

        a_Position = GLES20.glGetAttribLocation(_program, "a_Position");
        a_Color = GLES20.glGetAttribLocation(_program, "a_Color");
        a_Normal = GLES20.glGetAttribLocation(_program, "a_Normal");
        a_TexCoordinate = GLES20.glGetAttribLocation(_program, "a_TexCoordinate");

        u_LightPos = GLES20.glGetUniformLocation(_program, "u_LightPos");
        u_Texture = GLES20.glGetUniformLocation(_program, "u_Texture");
        u_AmbientLight = GLES20.glGetUniformLocation(_program, "u_AmbientLight");

        // Texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(u_Texture, textureId);
    }

    public void draw(float[] _modelViewMatrix, float[] mProjectionMatrix, float x, float y, float z, float w, float h, float l) {
        GLES20.glUseProgram(_program);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, y, z);
        Matrix.scaleM(mModelMatrix, 0, w, h, l);

        glSet(mCubePositions, a_Position, 3);
        glSet(mCubeColors, a_Color, 4);
        glSet(mCubeNormals, a_Normal, 3);
        glSet(mCubeTextureCoordinates, a_TexCoordinate, 2);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        //Light
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, -2.0f, -3.0f);
        Matrix.rotateM(mLightModelMatrix, 0, 0, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, _modelViewMatrix, 0, mLightPosInWorldSpace, 0);

        GLES20.glUniform3f(u_LightPos, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        float aLight = 0.8f;
        GLES20.glUniform1f(u_AmbientLight, aLight);

        // Apply all
        Matrix.multiplyMM(mMVPMatrix, 0, _modelViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(u_MVMatrix, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(u_MVPMatrix, 1, false, mMVPMatrix, 0);

        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }

    private void glSet(FloatBuffer fb, int key, int size) {
        fb.position(0);
        GLES20.glVertexAttribPointer(key, size, GLES20.GL_FLOAT, false, 0, fb);
        GLES20.glEnableVertexAttribArray(key);
    }

    public static FloatBuffer makeFloatBuffer(float[] arr) {
        int iSize = arr.length;
        ByteBuffer bb = ByteBuffer.allocateDirect(iSize * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position (0);
        return fb;
    }
}
