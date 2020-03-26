package com.msa.spacerunner.models;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.msa.spacerunner.R;
import com.msa.spacerunner.common.RawResourceReader;
import com.msa.spacerunner.shaders.ShadersUtils;

import java.nio.FloatBuffer;

public class Plane {

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
    private static int u_AmbientLight;

    // X, Y, Z
    private static final float[] planePositionData = {
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f
    };

    private static final float[] planeColorData = {
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };

    private static final float[] planeNormalData = {
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
    };

    private static final float[] planeTextureCoordinateData = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    private final FloatBuffer fbPositions;
    private final FloatBuffer fbColors;
    private final FloatBuffer fbNormals;
    private final FloatBuffer fbTextureCoordinates;

    private int textureId;

    public Plane(Context context, int tId) {

        fbPositions = ModelUtils.makeFloatBuffer(planePositionData);
        fbColors = ModelUtils.makeFloatBuffer(planeColorData);
        fbNormals = ModelUtils.makeFloatBuffer(planeNormalData);
        fbTextureCoordinates = ModelUtils.makeFloatBuffer(planeTextureCoordinateData);

        textureId = tId;
        initializeGL(context);
    }

    private void initializeGL(Context context) {
        final String vertexShaderSource = RawResourceReader.readTextFileFromRawResource(context, R.raw.vertex_texture_shader);
        final String fragmentShaderSource = RawResourceReader.readTextFileFromRawResource(context, R.raw.fragment_texture_shader);

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
        int u_Texture = GLES20.glGetUniformLocation(_program, "u_Texture");
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
        // Because i create a front plane i rotate here
        Matrix.rotateM(mModelMatrix, 0, -90, 1.0f, 0.0f, 0.0f);

        ModelUtils.glSetVertexAttrib(fbPositions, a_Position, 3);
        ModelUtils.glSetVertexAttrib(fbColors, a_Color, 4);
        ModelUtils.glSetVertexAttrib(fbNormals, a_Normal, 3);
        ModelUtils.glSetVertexAttrib(fbTextureCoordinates, a_TexCoordinate, 2);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        //Light
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, x, y, z);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, _modelViewMatrix, 0, mLightPosInWorldSpace, 0);

        GLES20.glUniform3f(u_LightPos, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        float aLight = 0.1f;
        GLES20.glUniform1f(u_AmbientLight, aLight);

        // Apply all
        Matrix.multiplyMM(mMVPMatrix, 0, _modelViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(u_MVMatrix, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(u_MVPMatrix, 1, false, mMVPMatrix, 0);

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, planePositionData.length/3); // 6 coordinate x, y, z for 2 triangles
    }
}
