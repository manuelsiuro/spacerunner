package com.msa.spacerunner.ui.activities;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.View;

import com.msa.spacerunner.R;
import com.msa.spacerunner.common.TextureHelper;
import com.msa.spacerunner.models.Cube;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// Learn : https://learnopengl.com/Advanced-OpenGL/Cubemaps

public class TextureActivity extends Activity implements GLSurfaceView.Renderer {

    private float[] modelViewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];

    private float[] eye = {0.0f, 0.5f, 3.0f};
    private float[] at = {0.0f, 0.0f, 0.0f};
    private float[] up = {0.0f, 1.0f, 0.0f};

    private float zMvt = 0.0f;

    private static int[] textures;
    private static Cube[] cubes;

    private static final int TEXTURE_GLASS = 0;
    private static final int TEXTURE_GRASS = 1;
    private static final int TEXTURE_STEEL = 2;
    private static final int TEXTURE_LIZARD = 3;
    private static final int TEXTURE_WALL = 4;
    private static final int TEXTURE_WALL2 = 5;
    private static final int TEXTURE_WOOD = 6;

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
        int[] t = {
                R.drawable.glass,
                R.drawable.grass,
                R.drawable.steel,
                R.drawable.lezard,
                R.drawable.wall,
                R.drawable.wall2,
                R.drawable.wood
        };
        textures = TextureHelper.loadTexture(this, t);

        cubes = new Cube[t.length];
        for (int i = 0; i < t.length; i++) {
            cubes[i] = new Cube(this, textures[i]);
        }

        // Color Background RGBA Blue
        GLES20.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
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
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(modelViewMatrix, 0, eye[0], eye[1], eye[2], at[0], at[1], at[2], up[0], up[1], up[2]);

        // Move world on z
        zMvt += 0.1f;
        Matrix.translateM(modelViewMatrix, 0, 0.0f, -3.0f, -5.0f + zMvt);

        // Add cube
        for (int i = 0; i < 100; i++) {
            drawCube(cubes[TEXTURE_GLASS],-4.0f, -3.0f, -5.0f - (i*2.0f), 0.5f, 0.5f, 0.5f);
            drawCube(cubes[TEXTURE_GRASS],-2f, -3.0f, -5.0f - (i*2.0f), 0.5f, 0.5f, 0.5f);
            drawCube(cubes[TEXTURE_STEEL],0f, -3.0f, -5.0f - (i*2.0f), 0.5f, 0.5f, 0.5f);
            drawCube(cubes[TEXTURE_LIZARD],2f, -3.0f, -5.0f - (i*2.0f), 0.5f, 0.5f, 0.5f);
            drawCube(cubes[TEXTURE_WALL],4f, -3.0f, -5.0f - (i*2.0f), 0.5f, 0.5f, 0.5f);
        }

        drawCube(cubes[TEXTURE_WOOD], -4.0f, -4.0f, -5.0f, 200.0f, 0.2f, 200.0f);
        drawCube(cubes[TEXTURE_WALL2], -4.0f, 13.0f, -5.0f, 200.0f, 0.2f, 200.0f);

    }

    private void drawCube(Cube c, float x, float y, float z, float w, float h, float l) {
        c.draw(modelViewMatrix, projectionMatrix, x, y, z, w, h, l);
    }

    private void initializeViewport(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        //Initialize and create the perspective matrices
        Matrix.perspectiveM(projectionMatrix, 0, 90.0f, ratio, 0.01f, 90.0f);

        //Move things back a bit
        Matrix.translateM(projectionMatrix, 0, 0.0f, 0.0f, -3.0f);
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
