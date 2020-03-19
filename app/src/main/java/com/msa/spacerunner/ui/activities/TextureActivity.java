package com.msa.spacerunner.ui.activities;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.msa.spacerunner.R;
import com.msa.spacerunner.common.RawResourceReader;
import com.msa.spacerunner.common.TextureHelper;
import com.msa.spacerunner.models.Cube;
import com.msa.spacerunner.shaders.ShadersUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// Learn : https://learnopengl.com/Advanced-OpenGL/Cubemaps

public class TextureActivity extends Activity implements GLSurfaceView.Renderer{

    public static final int a_Position_ID = 0;
    public static final int a_Color_ID = 1;
    public static final int a_Normal_ID = 2;
    public static final int a_TexCoordinate_ID = 3;

    static int _program = -1;
    float[] _modelViewMatrix;
    float[] _projectionMatrix;
    float[] eye = {0.0f, 0.5f, 3.0f};
    float[] at = {0.0f, 0.0f, 0.0f};
    float[] up = {0.0f, 1.0f, 0.0f};

    static int u_MVMatrix;
    static int u_MVPMatrix;

    static int a_Position;
    static int a_Color;
    static int a_Normal;
    static int a_TexCoordinate;

    static int u_LightPos;
    static int u_Texture;
    static int u_AmbientLight;

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mLightModelMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInModelSpace = new float[]{0.0f, 4.0f, 0.0f, 1.0f};

    private final float[] mLightPosInEyeSpace = new float[4];

    private float zMvt = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_texture_activity);
        hideSystemUI();

        GLSurfaceView surfaceView = findViewById(R.id.game_surface_view);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surfaceView.setRenderer(this);

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        initializeGL();
        _modelViewMatrix = new float[16];
    }

    @Override
    public void onSurfaceChanged(GL10 gl10Unused, int width, int height) {
        initializeViewport(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        drawScene();
    }

    private void drawScene() {
        GLES20.glUseProgram(_program);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(_modelViewMatrix, 0, eye[0], eye[1], eye[2], at[0], at[1], at[2], up[0], up[1], up[2]);
        //GLES20.glUniformMatrix4fv(u_MVMatrix, 1, false, _modelViewMatrix, 0);
        //GLES20.glUniformMatrix4fv(u_MVPMatrix, 1, false, _projectionMatrix, 0);

        //float[] mModelMatrix = _modelViewMatrix.clone();

        /*long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);*/

        //Matrix.setIdentityM(mModelMatrix, 0);
        zMvt += 0.1f;
        Matrix.translateM(_modelViewMatrix, 0, 0.0f, -3.0f, -5.0f + zMvt);
        //Matrix.scaleM(mModelMatrix, 0, 0.5f, 0.5f, 0.5f);
        //Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        //Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        for (int i = 0; i < 50; i++) {
            drawCube(-4.0f, -3.0f, -5.0f - (i*2.0f), 1.0f, 1.0f, 1.0f);
            drawCube(-2f, -3.0f, -5.0f - (i*2.0f), 1.0f, 1.0f, 1.0f);
            drawCube(0f, -3.0f, -5.0f - (i*2.0f), 1.0f, 1.0f, 1.0f);
            drawCube(2f, -3.0f, -5.0f - (i*2.0f), 1.0f, 1.0f, 1.0f);
            drawCube(4f, -3.0f, -5.0f - (i*2.0f), 1.0f, 1.0f, 1.0f);
        }


        drawCube(-4.0f, -3.0f, -5.0f, 200.0f, 0.2f, 200.0f);

    }

    private void drawCube(float x, float y, float z, float w, float h, float l) {

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, y, z);
        Matrix.scaleM(mModelMatrix, 0, w, h, l);

        Cube cube = new Cube();
        // Pass in the position information
        cube.mCubePositions.position(0);
        GLES20.glVertexAttribPointer(a_Position, 3, GLES20.GL_FLOAT, false,0, cube.mCubePositions);
        GLES20.glEnableVertexAttribArray(a_Position);

        // Pass in the color information
        cube.mCubeColors.position(0);
        GLES20.glVertexAttribPointer(a_Color, 4, GLES20.GL_FLOAT, false, 0, cube.mCubeColors);
        GLES20.glEnableVertexAttribArray(a_Color);

        // Pass in the normal information
        cube.mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(a_Normal, 3, GLES20.GL_FLOAT, false, 0, cube.mCubeNormals);
        GLES20.glEnableVertexAttribArray(a_Normal);

        // Pass in the texture coordinate information
        cube.mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(a_TexCoordinate, 2, GLES20.GL_FLOAT, false, 0, cube.mCubeTextureCoordinates);
        GLES20.glEnableVertexAttribArray(a_TexCoordinate);

        //Light
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, -2.0f, -3.0f);
        Matrix.rotateM(mLightModelMatrix, 0, 0, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, _modelViewMatrix, 0, mLightPosInWorldSpace, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, _modelViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(u_MVMatrix, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(u_MVPMatrix, 1, false, mMVPMatrix, 0);

        GLES20.glUniform3f(u_LightPos, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        float aLight = 0.8f;
        GLES20.glUniform1f(u_AmbientLight, aLight);

        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);


    }

    private void initializeGL() {
        final String vertexShaderSource = RawResourceReader.readTextFileFromRawResource(this, R.raw.per_pixel_vertex_shader);
        final String fragmentShaderSource = RawResourceReader.readTextFileFromRawResource(this, R.raw.per_pixel_fragment_shader);

        _program = ShadersUtils.createProgram(vertexShaderSource, fragmentShaderSource);

        GLES20.glBindAttribLocation(_program, a_Position_ID, "a_Position");
        GLES20.glBindAttribLocation(_program, a_Color_ID, "a_Color");
        GLES20.glBindAttribLocation(_program, a_Normal_ID, "a_Normal");
        GLES20.glBindAttribLocation(_program, a_TexCoordinate_ID, "a_TexCoordinate");

        GLES20.glLinkProgram(_program);
        String programLinkLog = GLES20.glGetProgramInfoLog(_program);
        Log.d("Program Link", programLinkLog + "\n");

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f); //Clear color is Grey

        u_MVPMatrix = GLES20.glGetUniformLocation(_program, "u_MVPMatrix");
        u_MVMatrix = GLES20.glGetUniformLocation(_program, "u_MVMatrix");

        a_Position = GLES20.glGetAttribLocation(_program, "a_Position");
        a_Color = GLES20.glGetAttribLocation(_program, "a_Color");
        a_Normal = GLES20.glGetAttribLocation(_program, "a_Normal");
        a_TexCoordinate = GLES20.glGetAttribLocation(_program, "a_TexCoordinate");

        u_LightPos = GLES20.glGetUniformLocation(_program, "u_LightPos");
        u_Texture = GLES20.glGetUniformLocation(_program, "u_Texture");
        u_AmbientLight = GLES20.glGetUniformLocation(_program, "u_AmbientLight");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        TextureHelper.loadTexture(this, R.drawable.wall);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureHelper.loadTexture(this, R.drawable.bumpy_bricks_public_domain));
        /*GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);*/



        GLES20.glUniform1i(u_Texture, 0);
    }

    private void initializeViewport(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        //Initialize and create the perspective matrices
        //_projectionMatrix = new float[16];
        Matrix.perspectiveM(mProjectionMatrix, 0, 90.0f, ratio, 0.01f, 90.0f);


        //Move things back a bit
        Matrix.translateM(mProjectionMatrix, 0, 0.0f, 0.0f, -3.0f);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        /*final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;*/

        //Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
