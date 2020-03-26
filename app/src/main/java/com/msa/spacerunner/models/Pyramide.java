package com.msa.spacerunner.models;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.msa.spacerunner.R;
import com.msa.spacerunner.common.RawResourceReader;
import com.msa.spacerunner.engine.GeometryBuilder;
import com.msa.spacerunner.shaders.ShadersUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Pyramide {

    private float[] mLightModelMatrix = new float[16];
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInModelSpace = new float[]{0.0f, 4.0f, 0.0f, 1.0f};
    private final float[] mLightPosInEyeSpace = new float[4];

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private static int _program = -1;
    private static final int a_Position_ID = 0;
    private static final int a_TexCoordinate_ID = 3;

    private static int u_MVMatrix;
    private static int u_MVPMatrix;

    private static int a_Position;
    private static int a_TexCoordinate;

    private static int u_LightPos;
    private static int u_AmbientLight;

    private FloatBuffer fbPositions;
    private FloatBuffer fbTextureCoordinates;
    private ByteBuffer indexBuffer;
    private int textureId;
    private int verticesCount = 0;

    private static float[] vertices = {
            0.0f, 1.0f, 0.0f, // top
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,

            0.0f, 1.0f, 0.0f, // Right
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,

            0.0f, 1.0f, 0.0f, // Back
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,

            0.0f, 1.0f, 0.0f, // Left
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,

            -1.0f, -1.0f, -1.0f, // Bottom square 2 triangles
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,

            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f
    };

    private static  float[] textureCoordinateData = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f

    };

    public Pyramide(Context context, int tId) {
        textureId = tId;
        createTetrahedron();
        initializeGL(context);
    }

    private void createTetrahedron() {
        //vertices = GeometryBuilder.createCylinderBuffer(1.0f, 4.0f, 8);
        verticesCount = vertices.length/3;
        fbPositions = ModelUtils.makeFloatBuffer(vertices);


        fbTextureCoordinates = ModelUtils.makeFloatBuffer(textureCoordinateData);
    }

    private void initializeGL(Context context) {
        final String vertexShaderSource = RawResourceReader.readTextFileFromRawResource(context, R.raw.vertex_sphere_texture);
        final String fragmentShaderSource = RawResourceReader.readTextFileFromRawResource(context, R.raw.fragment_sphere_texture);

        _program = ShadersUtils.createProgram(vertexShaderSource, fragmentShaderSource);

        GLES20.glBindAttribLocation(_program, a_Position_ID, "a_Position");
        GLES20.glBindAttribLocation(_program, a_TexCoordinate_ID, "a_TexCoordinate");

        GLES20.glLinkProgram(_program);

        u_MVPMatrix = GLES20.glGetUniformLocation(_program, "u_MVPMatrix");
        u_MVMatrix = GLES20.glGetUniformLocation(_program, "u_MVMatrix");

        a_Position = GLES20.glGetAttribLocation(_program, "a_Position");
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
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, y, z);
        Matrix.scaleM(mModelMatrix, 0, w, h, l);

        // Rotate forever test
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        //Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);

        ModelUtils.glSetVertexAttrib(fbPositions, a_Position, 3);
        //ModelUtils.glSetVertexAttrib(fbTextureCoordinates, a_TexCoordinate, 2);

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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verticesCount);
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, verticesCount);
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, verticesCount);

        GLES20.glDisableVertexAttribArray(a_Position);
        GLES20.glDisableVertexAttribArray(a_TexCoordinate);
    }
}
