package com.msa.spacerunner.models;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.msa.spacerunner.R;
import com.msa.spacerunner.common.RawResourceReader;
import com.msa.spacerunner.shaders.ShadersUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class SphereTexture {

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
    private int textureId;
    private int verticesCount = 0;


    public SphereTexture(Context context, int tId) {

        textureId = tId;
        createSphereTexture(1.0f);
        initializeGL(context);
    }

    private void createSphereTexture(float r) {

        final float UNIT_SIZE = 0.5f;
        ArrayList<Float> verticesList = new ArrayList<>();
        final  float angleSpan =  10f ; // The angle at which the ball is unit-divided

        for (float verticalAngle =  90 ; verticalAngle >  - 90 ; verticalAngle = verticalAngle - angleSpan) {
            for ( float horizontalAngle =  360 ; horizontalAngle >  0 ; horizontalAngle = horizontalAngle - angleSpan) {

                double verticalRadian = Math.toRadians(verticalAngle);
                double verticalMinRadian = Math.toRadians(verticalAngle - angleSpan);
                double horizontalRadian = Math.toRadians(horizontalAngle);
                double horizontalMinRadian = Math.toRadians(horizontalAngle - angleSpan);

                double xozLength = r * UNIT_SIZE * Math.cos(verticalRadian);
                float x1 = (float) (xozLength * Math.cos(horizontalRadian));
                float z1 = (float) (xozLength * Math.sin(horizontalRadian));
                float y1 = (float) (r * UNIT_SIZE * Math.sin(verticalRadian));

                xozLength = r * UNIT_SIZE * Math.cos(verticalMinRadian);
                float x2 = (float) (xozLength * Math.cos(horizontalRadian));
                float z2 = (float) (xozLength * Math.sin(horizontalRadian));
                float y2 = (float) (r * UNIT_SIZE * Math.sin(verticalMinRadian));

                xozLength = r * UNIT_SIZE * Math.cos(Math.toRadians(verticalAngle - angleSpan));
                float x3 = (float) (xozLength * Math.cos(horizontalMinRadian));
                float z3 = (float) (xozLength * Math.sin(horizontalMinRadian));
                float y3 = (float) (r * UNIT_SIZE * Math.sin(verticalMinRadian));

                xozLength = r * UNIT_SIZE * Math.cos(verticalRadian);
                float x4 = (float) (xozLength * Math.cos(horizontalMinRadian));
                float z4 = (float) (xozLength * Math.sin(horizontalMinRadian));
                float y4 = (float) (r * UNIT_SIZE * Math.sin(verticalRadian));

                // Build the first triangle
                verticesList.add(x1);
                verticesList.add(y1);
                verticesList.add(z1);
                verticesList.add(x2);
                verticesList.add(y2);
                verticesList.add(z2);
                verticesList.add(x4);
                verticesList.add(y4);
                verticesList.add(z4);

                // Build the second triangle
                verticesList.add(x4);
                verticesList.add(y4);
                verticesList.add(z4);
                verticesList.add(x2);
                verticesList.add(y2);
                verticesList.add(z2);
                verticesList.add(x3);
                verticesList.add(y3);
                verticesList.add(z3);
            }
        }
        // The number of vertices is 1/3 of the number of coordinates, because a vertex has 3 coordinates
        verticesCount = verticesList.size() / 3;

        float[] vertices = new float[verticesCount * 3];
        for (int i = 0; i < verticesList.size(); i++) {
            vertices[i] = verticesList.get(i);
        }

        float[] texturesCoordinate = generateTextureCoordinate((int)(360/angleSpan), (int)(180/angleSpan));

        fbPositions = ModelUtils.makeFloatBuffer(vertices);
        fbTextureCoordinates = ModelUtils.makeFloatBuffer(texturesCoordinate);
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

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, y, z);
        Matrix.scaleM(mModelMatrix, 0, w, h, l);

        // Rotate forever test
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);

        ModelUtils.glSetVertexAttrib(fbPositions, a_Position, 3);
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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verticesCount);
    }

    // Method of automatically splitting texture to generate texture array
    private  float[] generateTextureCoordinate(int bw, int bh) {

        float[] result = new float[bw * bh * 6 * 2];
        float sizeWidth =  1.0f/bw; // number of columns
        float sizeHeight = 1.0f/bh;
        int c = 0;

        for (int i = 0; i < bh; i++) {
            for (int j = 0; j < bw; j++) {

                // Each row and column is a rectangle composed of two triangles with a total of six points and 12 texture coordinates.
                float s = j * sizeWidth;
                float t = i * sizeHeight; // Get the texture coordinate value of the upper left point of the small rectangle in row i and column j

                result[c++] = s;
                result[c++] = t; // Texture coordinate value of the upper left point of the rectangle
                result[c++] = s;
                result[c++] = t + sizeHeight; // Texture coordinate value of the bottom left point of the rectangle
                result[c++] = s + sizeWidth;
                result[c++] = t;             // Texture coordinate value of the upper-right point of the rectangle
                result[c++] = s + sizeWidth;
                result[c++] = t; // Texture coordinate value of the upper-right point of the rectangle
                result[c++] = s;
                result[c++] = t + sizeHeight; // Texture coordinate value of the bottom left point of the rectangle
                result[c++] = s + sizeWidth;
                result[c++] = t + sizeHeight;     // Texture coordinate value of the bottom right point of the rectangle
            }
        }
        return result;
    }
}
