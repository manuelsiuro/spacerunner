package com.msa.spacerunner.ui.activities;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.msa.spacerunner.GamePreferences;
import com.msa.spacerunner.R;
import com.msa.spacerunner.engine.GeometryBuilder;
import com.msa.spacerunner.engine.Node;
import com.msa.spacerunner.shaders.ShadersUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ShipEditorActivity extends Activity implements GLSurfaceView.Renderer {

    public static final int POSITION_ATTRIBUTE_ID = 0;
    public static final int NORMAL_ATTRIBUTE_ID = 2;

    public static int _mvmLoc;
    static int _viewLoc;
    static int _projLoc;
    public static int _ambientLoc;
    public static int _diffuseLoc;
    public static int _specularLoc;
    public static int _emissiveLoc;
    public static int _shineLoc;
    static int _lightPosLoc;
    public static int _normalMatrixLoc;

    static int _program = -1;

    float[] _modelViewMatrix;
    Node[] _shipNodes;
    float[] eye = {0.0f, 0.5f, 3.0f};
    float[] at = {0.0f, 0.0f, 0.0f};

    int _width, _height;
    float[] _projectionMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ship_editor_activity);
        hideSystemUI();

        GLSurfaceView surfaceView = findViewById(R.id.game_surface_view);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surfaceView.setRenderer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        initializeGL();

        _modelViewMatrix = new float[16];
        _shipNodes = new Node[3];
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
        System.out.println("drawScene");
        //Set program and specific flags
        GLES20.glUseProgram(_program);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(_modelViewMatrix, 0, eye[0], eye[1], eye[2], at[0], at[1], at[2], 0.0f, 1.0f, 0.0f);
        GLES20.glUniformMatrix4fv(_viewLoc, 1, false, _modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(_projLoc, 1, false, _projectionMatrix, 0);

        float offsetY = 4.0f;
        float _shipPositionX = 2.0f;
        float _shipPositionY = 1.0f;
        float _shipPositionZ = 10.0f;
        float _sideVelocity = 1.0f;
        float _verticalVelocity = 1.0f;

        float[] lightPosition = {
                -_shipPositionX, -_shipPositionY + offsetY, (_shipPositionZ % 10) - 20.0f, 1.0f,
                -_shipPositionX, -_shipPositionY + offsetY, (_shipPositionZ % 10) - 10.0f, 1.0f,
                -_shipPositionX, -_shipPositionY + offsetY, (_shipPositionZ % 10), 1.0f,
                -_shipPositionX, -_shipPositionY + offsetY, (_shipPositionZ % 10) + 10.0f, 1.0f,
        };

        GLES20.glUniform4fv(_lightPosLoc, 4, lightPosition, 0);

        float[] bodyModel = _modelViewMatrix.clone();
        Matrix.translateM(_modelViewMatrix, 0, -_shipPositionX, -_shipPositionY, 0.0f);

        float[] shipAmbient = {0.1f, 0.1f, 0.3f, 1.0f};
        float[] shipDiffuse = {0.2f, 0.3f, 0.5f, 1.0f};

        int parsedColor = Color.parseColor(GamePreferences.getShipColor(this));
        float r = Color.red(parsedColor) / 255f;
        float g = Color.green(parsedColor) / 255f;
        float b = Color.blue(parsedColor) / 255f;
        float[] shipSpecular = {r, g, b, 1.0f};

        //Move and draw ship
        Matrix.translateM(bodyModel, 0, 0.0f, 0.0f, 4.0f);
        Matrix.rotateM(bodyModel, 0, (3.0f * -_sideVelocity), 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(bodyModel, 0, (2.0f * _verticalVelocity), 1.0f, 0.0f, 0.0f);

        //Body of ship
        float[] instanceMatrix = bodyModel.clone();
        Matrix.translateM(instanceMatrix, 0, 0.0f, -0.05f, 0.3f);
        Matrix.scaleM(instanceMatrix, 0, 0.15f, 0.05f, 0.35f);
        Node shape = new Node(instanceMatrix);
        shape.setColor(shipAmbient, shipDiffuse, shipSpecular);
        shape.setPoints(GeometryBuilder.getCube());
        shape.setShine(2.0f);
        _shipNodes[0] = shape;

        //Right Wing
        instanceMatrix = bodyModel.clone();
        Matrix.translateM(instanceMatrix, 0, 0.25f, 0.0f, 0.0f);
        Matrix.scaleM(instanceMatrix, 0, 0.35f, 0.15f, 1.0f);
        shape = new Node(instanceMatrix);
        shape.setColor(shipAmbient, shipDiffuse, shipSpecular);
        shape.setPoints(GeometryBuilder.getRightTetrahedron());
        shape.setShine(2.0f);
        _shipNodes[1] = shape;

        //Left Wing
        instanceMatrix = bodyModel.clone();
        Matrix.translateM(instanceMatrix, 0, -0.25f, 0.0f, 0.0f);
        Matrix.scaleM(instanceMatrix, 0, 0.35f, 0.15f, 1.0f);
        Matrix.rotateM(instanceMatrix, 0, 90, 0.0f, 0.0f, 1.0f);
        shape = new Node(instanceMatrix);
        shape.setColor(shipAmbient, shipDiffuse, shipSpecular);
        shape.setPoints(GeometryBuilder.getRightTetrahedron());
        shape.setShine(2.0f);
        _shipNodes[2] = shape;

        drawPlane();

        for (int i = 0; i < 3; i++)
            _shipNodes[i].render();


    }

    private void drawPlane() {
        float[] instanceMatrix;

        instanceMatrix = _modelViewMatrix.clone();

        //floor
        float[] ambient = new float[]{0.2f, 0.2f, 0.2f, 1.0f};
        float[] diffuse = new float[]{0.4f, 0.4f, 0.4f, 1.0f};
        float[] specular = new float[]{1.0f, 0.0f, 0.0f, 1.0f};

        Matrix.scaleM(instanceMatrix, 0, 1.0f, 1.0f, 1.0f);
        Matrix.translateM(instanceMatrix, 0, 0, -0.5f, 6.0f);
        Node shape = new Node(instanceMatrix);
        shape.setColor(ambient, diffuse, specular);
        shape.setPoints(GeometryBuilder.getPlane());
        shape.setShine(2.0f);
        shape.render();

        //drawPlane();
    }

    private void initializeGL() {

        String vertexShaderSource = ShadersUtils.getShaderFile(this, "vertex-shader.glsl");
        String fragmentShaderSource = ShadersUtils.getShaderFile(this, "fragment-shader.glsl");

        _program = ShadersUtils.createProgram(vertexShaderSource, fragmentShaderSource);

        GLES20.glBindAttribLocation(_program, POSITION_ATTRIBUTE_ID, "position");
        GLES20.glBindAttribLocation(_program, NORMAL_ATTRIBUTE_ID, "normal");

        GLES20.glLinkProgram(_program);
        String programLinkLog = GLES20.glGetProgramInfoLog(_program);
        Log.d("Program Link", programLinkLog + "\n");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); //Clear color is Black

        GLES20.glEnableVertexAttribArray(POSITION_ATTRIBUTE_ID);
        GLES20.glEnableVertexAttribArray(NORMAL_ATTRIBUTE_ID);

        _viewLoc = GLES20.glGetUniformLocation(_program, "viewMatrix");
        _projLoc = GLES20.glGetUniformLocation(_program, "projectionMatrix");
        _ambientLoc = GLES20.glGetUniformLocation(_program, "ambientProduct");
        _diffuseLoc = GLES20.glGetUniformLocation(_program, "diffuseProduct");
        _specularLoc = GLES20.glGetUniformLocation(_program, "specularProduct");
        _emissiveLoc = GLES20.glGetUniformLocation(_program, "emissive");
        _shineLoc = GLES20.glGetUniformLocation(_program, "shine");
        _lightPosLoc = GLES20.glGetUniformLocation(_program, "lightPosition");
        _mvmLoc = GLES20.glGetUniformLocation(_program, "modelViewMatrix");
        _normalMatrixLoc = GLES20.glGetUniformLocation(_program, "normalMatrix");
    }

    private void initializeViewport(int width, int height) {
        //Create the viewport
        GLES20.glViewport(0, 0, width, height);
        _width = width;
        _height = height;

        float ratio = (float) width / height;

        //Initialize and create the perspective matrices
        _projectionMatrix = new float[16];
        Matrix.perspectiveM(_projectionMatrix, 0, 90.0f, ratio, 0.01f, 30.0f);


        //Move things back a bit
        Matrix.translateM(_projectionMatrix, 0, 0.0f, 0.0f, -3.0f);
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
