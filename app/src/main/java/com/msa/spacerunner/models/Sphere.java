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
import java.nio.ShortBuffer;

public class Sphere {

    private static int _program = -1;

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private ShortBuffer drawListBuffer;

    private int vPosition;
    private int vColor;
    private int vNormal;

    private int lightDir;

    private int uMVPMatrix;
    //private int uNormalMat;
    private int uMVMatrix;

    private int u_ballColor;

    static final int COORDS_PER_VERTEX = 3;

    private float [] vertices;
    private float [] normals;
    static float [] colors;
    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };	// order to draw vertices

    private int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4;	// bytes per vertex

    // set the light direction in the eye coordinate;
    float lightDirection[] = {0.0f, 1.0f, 8.0f};

    private void createSphere(double r, int lats, int longs) {
        int i, j;

        // there are lats*longs number of quads, each requires two triangles with six vertices, each vertex takes 3 floats;
        vertices = new float[lats*longs*6*3];
        normals = new float[lats*longs*6*3];
        colors = new float[lats*longs*6*3];

        vertexCount = vertices.length / COORDS_PER_VERTEX;
        int triIndex = 0;
        for(i = 0; i < lats; i++) {

            double lat0 = Math.PI * (-0.5 + (double) (i) / lats);
            double z0  = Math.sin(lat0);
            double zr0 =  Math.cos(lat0);

            double lat1 = Math.PI * (-0.5 + (double) (i+1) / lats);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);

            for(j = 0; j < longs; j++) {

                double lng = 2 * Math.PI * (double) (j - 1) / longs;
                double x = Math.cos(lng);
                double y = Math.sin(lng);

                lng = 2 * Math.PI * (double) (j) / longs;
                double x1 = Math.cos(lng);
                double y1 = Math.sin(lng);

                // the first triangle
                vertices[triIndex*9 ]       = (float)(x * zr0);    vertices[triIndex*9 + 1 ] = (float)(y * zr0);   vertices[triIndex*9 + 2 ] = (float) z0;
                vertices[triIndex*9 + 3 ]   = (float)(x * zr1);    vertices[triIndex*9 + 4 ] = (float)(y * zr1);   vertices[triIndex*9 + 5 ] = (float) z1;
                vertices[triIndex*9 + 6 ]   = (float)(x1 * zr0);   vertices[triIndex*9 + 7 ] = (float)(y1 * zr0);  vertices[triIndex*9 + 8 ] = (float) z0;

                triIndex ++;
                vertices[triIndex*9 ]       = (float)(x1 * zr0);   vertices[triIndex*9 + 1 ] = (float)(y1 * zr0);  	vertices[triIndex*9 + 2 ] = (float) z0;
                vertices[triIndex*9 + 3 ]   = (float)(x * zr1);    vertices[triIndex*9 + 4 ] = (float)(y * zr1);   	vertices[triIndex*9 + 5 ] = (float) z1;
                vertices[triIndex*9 + 6 ]   = (float)(x1 * zr1);    vertices[triIndex*9 + 7 ] = (float)(y1 * zr1); 	vertices[triIndex*9 + 8 ] = (float) z1;

                // in this case, the normal is the same as the vertex, plus the normalization;
                for (int kk = -9; kk<9 ; kk++) {
                    normals[triIndex*9 + kk] = vertices[triIndex*9+kk];
                    if((triIndex*9 + kk)%3 == 2)
                        colors[triIndex*9 + kk] = 1;
                    else
                        colors[triIndex*9 + kk] = 0;
                }
                triIndex ++;
            }
        }
    }

    public Sphere(Context context, double r, int lats, int longs) {
        createSphere(r, lats, longs);

        vertexBuffer = ModelUtils.makeFloatBuffer(vertices);
        colorBuffer = ModelUtils.makeFloatBuffer(colors);
        normalBuffer = ModelUtils.makeFloatBuffer(normals);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        initializeGL(context);
    }

    private void initializeGL(Context context) {

        final String vertexShaderSource = RawResourceReader.readTextFileFromRawResource(context, R.raw.vertex_ball_shader);
        final String fragmentShaderSource = RawResourceReader.readTextFileFromRawResource(context, R.raw.fragment_ball_shader);

        _program = ShadersUtils.createProgram(vertexShaderSource, fragmentShaderSource);


        GLES20.glLinkProgram(_program);

        vPosition = GLES20.glGetAttribLocation(_program, "vPosition");
        vColor = GLES20.glGetAttribLocation(_program, "vColor");
        vNormal = GLES20.glGetAttribLocation(_program, "vNormal");

        lightDir = GLES20.glGetUniformLocation(_program, "lightDir");
        uMVPMatrix = GLES20.glGetUniformLocation(_program, "uMVPMatrix");
        //uNormalMat = GLES20.glGetUniformLocation(_program, "uNormalMat");
        uMVMatrix = GLES20.glGetUniformLocation(_program, "uMVMatrix");

        u_ballColor = GLES20.glGetUniformLocation(_program, "u_ballColor");
    }

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    public void draw(float [] _modelViewMatrix, float [] mProjectionMatrix, float x, float y, float z, float[] ballColor) {

        GLES20.glUseProgram(_program);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, y, z);
        Matrix.scaleM(mModelMatrix, 0, 0.5f, 0.5f, 0.5f);

        // Ball color
        GLES20.glUniform4fv(u_ballColor, 1, ballColor, 0);


        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glUniform3fv(lightDir, 1, lightDirection, 0);

        GLES20.glEnableVertexAttribArray(vColor);
        GLES20.glVertexAttribPointer(vColor, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, colorBuffer);

        GLES20.glEnableVertexAttribArray(vNormal);
        GLES20.glVertexAttribPointer(vNormal, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, normalBuffer);

        /*GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(uNormalMat, 1, false, _modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVMatrix, 1, false, mProjectionMatrix, 0);*/

        Matrix.multiplyMM(mMVPMatrix, 0, _modelViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVMatrix, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(vColor);
        GLES20.glDisableVertexAttribArray(vNormal);
    }
}
