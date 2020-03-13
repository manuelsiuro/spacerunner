package com.msa.spacerunner.ui.activities;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.SeekBar;

import com.msa.spacerunner.GamePreferences;
import com.msa.spacerunner.R;
import com.msa.spacerunner.engine.GeometryBuilder;
import com.msa.spacerunner.engine.Node;
import com.msa.spacerunner.shaders.ShadersUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ShipEditorActivity extends Activity implements GLSurfaceView.Renderer, ScaleGestureDetector.OnScaleGestureListener {

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

    // Editor
    private ScaleGestureDetector scaleGestureDetector;
    private boolean scaleMode;
    private float mPrevX;
    private float mPrevY;
    private final float TOUCH_SCALE_FACTOR = 180.0f/ 320.0f;
    public volatile float mXAngle;
    public volatile float mYAngle;
    public volatile float mZoom;

    private final float[] mAccumulatedRotation = new float[16];
    private final float[] mCurrentRotation = new float[16];
    private final float[] mTemporaryMatrix = new float[16];

    private static final float ZOOM_FACTOR = 0.05f;
    private static final float ZOOM_LIMIT = 4.0f;

    private static final int LEFT_WING_EDIT = 1;
    private static final int BODY_EDIT = 2;
    private static final int RIGHT_WING_EDIT = 3;

    private int current_edit = LEFT_WING_EDIT;

    private float scaleLeftWingX = 0.35f;
    private float scaleLeftWingY = 0.15f;
    private float scaleLeftWingZ = 1.0f;
    private float translateLeftWingX = -0.25f;
    private float translateLeftWingY = 0.0f;
    private float translateLeftWingZ = 0.0f;

    private float scaleBodyX = 0.15f;
    private float scaleBodyY = 0.05f;
    private float scaleBodyZ = 0.35f;
    private float translateBodyX = 0.0f;
    private float translateBodyY = -0.05f;
    private float translateBodyZ = 0.3f;

    private float scaleRightWingX = 0.35f;
    private float scaleRightWingY = 0.15f;
    private float scaleRightWingZ = 1.0f;
    private float translateRightWingX = 0.25f;
    private float translateRightWingY = 0.0f;
    private float translateRightWingZ = 0.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ship_editor_activity);
        hideSystemUI();

        GLSurfaceView surfaceView = findViewById(R.id.game_surface_view);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surfaceView.setRenderer(this);

        //Setup listener for scaling
        scaleGestureDetector = new ScaleGestureDetector(this, this);

        initializeEditorTools();
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

        // Initialize the accumulated rotation matrix
        Matrix.setIdentityM(mAccumulatedRotation, 0);
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

        Matrix.setLookAtM(_modelViewMatrix, 0, eye[0], eye[1], eye[2], at[0], at[1], at[2], 0.0f, 1.0f, 0.0f);
        GLES20.glUniformMatrix4fv(_viewLoc, 1, false, _modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(_projLoc, 1, false, _projectionMatrix, 0);

        // Make zoom before rotate !!!
        Matrix.translateM(_modelViewMatrix, 0, 0, 0, mZoom);

        // Set a matrix that contains the current rotation.
        Matrix.setIdentityM(mCurrentRotation, 0);
        Matrix.rotateM(mCurrentRotation, 0, mXAngle, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, mYAngle, 1.0f, 0.0f, 0.0f);
        mXAngle = 0.0f;
        mYAngle = 0.0f;

        // Multiply the current rotation by the accumulated rotation,
        // and then set the accumulated rotation to the result.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16);

        // Rotate the scene taking the overall rotation into account.
        Matrix.multiplyMM(mTemporaryMatrix, 0, _modelViewMatrix, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, _modelViewMatrix, 0, 16);

        // Now we can draw object ....

        float[] lightPosition = {
                0.0f, 3.0f, 1.0f, 1.0f,
                0.0f, 3.0f, 1.0f, 1.0f,
                0.0f, 3.0f, 1.0f, 1.0f,
                0.0f, 3.0f, 1.0f, 1.0f
        };

        GLES20.glUniform4fv(_lightPosLoc, 4, lightPosition, 0);



        float[] shipAmbient = {0.1f, 0.1f, 0.3f, 1.0f};
        float[] shipDiffuse = {0.2f, 0.3f, 0.5f, 1.0f};

        int parsedColor = Color.parseColor(GamePreferences.getShipColor(this));
        float r = Color.red(parsedColor) / 255f;
        float g = Color.green(parsedColor) / 255f;
        float b = Color.blue(parsedColor) / 255f;
        float[] shipSpecular = {r, g, b, 1.0f};

        float[] bodyModel = _modelViewMatrix.clone();

        //Body of ship ----------------------------------------------------------------
        float[] instanceMatrix = bodyModel.clone();
        Matrix.translateM(instanceMatrix, 0, translateBodyX, translateBodyY, translateBodyZ);
        Matrix.scaleM(instanceMatrix, 0, scaleBodyX, scaleBodyY, scaleBodyZ);

        Node shape = new Node(instanceMatrix);
        shape.setColor(shipAmbient, shipDiffuse, shipSpecular);
        shape.setPoints(GeometryBuilder.getCube());
        shape.setShine(2.0f);
        _shipNodes[0] = shape;

        //Right Wing ----------------------------------------------------------------
        instanceMatrix = bodyModel.clone();
        //Matrix.translateM(instanceMatrix, 0, 0.25f, 0.0f, 0.0f);
        //Matrix.scaleM(instanceMatrix, 0, 0.35f, 0.15f, 1.0f);
        Matrix.translateM(instanceMatrix, 0, translateRightWingX, translateRightWingY, translateRightWingZ);
        Matrix.scaleM(instanceMatrix, 0, scaleRightWingX, scaleRightWingY, scaleRightWingZ);

        shape = new Node(instanceMatrix);
        shape.setColor(shipAmbient, shipDiffuse, shipSpecular);
        shape.setPoints(GeometryBuilder.getRightTetrahedron());
        shape.setShine(2.0f);
        _shipNodes[1] = shape;

        //Left Wing ----------------------------------------------------------------
        instanceMatrix = bodyModel.clone();
        //Matrix.translateM(instanceMatrix, 0, -0.25f, 0.0f, 0.0f);
        //Matrix.scaleM(instanceMatrix, 0, 0.35f, 0.15f, 1.0f);
        Matrix.translateM(instanceMatrix, 0, translateLeftWingX, translateLeftWingY, translateLeftWingZ);
        Matrix.scaleM(instanceMatrix, 0, scaleLeftWingX, scaleLeftWingY, scaleLeftWingZ);
        Matrix.rotateM(instanceMatrix, 0, 90, 0.0f, 0.0f, 1.0f);

        shape = new Node(instanceMatrix);
        shape.setColor(shipAmbient, shipDiffuse, shipSpecular);
        shape.setPoints(GeometryBuilder.getRightTetrahedron());
        shape.setShine(2.0f);
        _shipNodes[2] = shape;

        for (Node shipNode : _shipNodes) {
            shipNode.renderEditor();
        }
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

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f); //Clear color is Black

        GLES20.glEnableVertexAttribArray(POSITION_ATTRIBUTE_ID);
        GLES20.glEnableVertexAttribArray(NORMAL_ATTRIBUTE_ID);

        _mvmLoc = GLES20.glGetUniformLocation(_program, "modelViewMatrix");
        _normalMatrixLoc = GLES20.glGetUniformLocation(_program, "normalMatrix");
        _projLoc = GLES20.glGetUniformLocation(_program, "projectionMatrix");
        _viewLoc = GLES20.glGetUniformLocation(_program, "viewMatrix");
        _lightPosLoc = GLES20.glGetUniformLocation(_program, "lightPosition");

        _ambientLoc = GLES20.glGetUniformLocation(_program, "ambientProduct");
        _diffuseLoc = GLES20.glGetUniformLocation(_program, "diffuseProduct");
        _specularLoc = GLES20.glGetUniformLocation(_program, "specularProduct");
        _emissiveLoc = GLES20.glGetUniformLocation(_program, "emissive");
        _shineLoc = GLES20.glGetUniformLocation(_program, "shine");
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

    private void initializeEditorTools() {

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.left_wing_button).setBackground(getResources().getDrawable(R.drawable.red_button_style));

        findViewById(R.id.left_wing_button).setOnClickListener(v -> {
            current_edit = LEFT_WING_EDIT;

            v.setBackground(getResources().getDrawable(R.drawable.red_button_style));
            findViewById(R.id.body_button).setBackground(getResources().getDrawable(R.drawable.blue_button_style));
            findViewById(R.id.right_wing_button).setBackground(getResources().getDrawable(R.drawable.blue_button_style));

            ((SeekBar)findViewById(R.id.sx)).setProgress(floatToProgress(scaleLeftWingX));
            ((SeekBar)findViewById(R.id.sy)).setProgress(floatToProgress(scaleLeftWingY));
            ((SeekBar)findViewById(R.id.sz)).setProgress(floatToProgress(scaleLeftWingZ));
            ((SeekBar)findViewById(R.id.tx)).setProgress(floatToProgress(translateLeftWingX));
            ((SeekBar)findViewById(R.id.ty)).setProgress(floatToProgress(translateLeftWingY));
            ((SeekBar)findViewById(R.id.tz)).setProgress(floatToProgress(translateLeftWingZ));
        });
        findViewById(R.id.body_button).setOnClickListener(v -> {
            current_edit = BODY_EDIT;

            v.setBackground(getResources().getDrawable(R.drawable.red_button_style));
            findViewById(R.id.left_wing_button).setBackground(getResources().getDrawable(R.drawable.blue_button_style));
            findViewById(R.id.right_wing_button).setBackground(getResources().getDrawable(R.drawable.blue_button_style));

            ((SeekBar)findViewById(R.id.sx)).setProgress(floatToProgress(scaleBodyX));
            ((SeekBar)findViewById(R.id.sy)).setProgress(floatToProgress(scaleBodyY));
            ((SeekBar)findViewById(R.id.sz)).setProgress(floatToProgress(scaleBodyZ));
            ((SeekBar)findViewById(R.id.tx)).setProgress(floatToProgress(translateBodyX));
            ((SeekBar)findViewById(R.id.ty)).setProgress(floatToProgress(translateBodyY));
            ((SeekBar)findViewById(R.id.tz)).setProgress(floatToProgress(translateBodyZ));
        });
        findViewById(R.id.right_wing_button).setOnClickListener(v -> {
            current_edit = RIGHT_WING_EDIT;

            v.setBackground(getResources().getDrawable(R.drawable.red_button_style));
            findViewById(R.id.left_wing_button).setBackground(getResources().getDrawable(R.drawable.blue_button_style));
            findViewById(R.id.body_button).setBackground(getResources().getDrawable(R.drawable.blue_button_style));

            ((SeekBar)findViewById(R.id.sx)).setProgress(floatToProgress(scaleRightWingX));
            ((SeekBar)findViewById(R.id.sy)).setProgress(floatToProgress(scaleRightWingY));
            ((SeekBar)findViewById(R.id.sz)).setProgress(floatToProgress(scaleRightWingZ));
            ((SeekBar)findViewById(R.id.tx)).setProgress(floatToProgress(translateRightWingX));
            ((SeekBar)findViewById(R.id.ty)).setProgress(floatToProgress(translateRightWingY));
            ((SeekBar)findViewById(R.id.tz)).setProgress(floatToProgress(translateRightWingZ));
        });

        // Scale
        ((SeekBar)findViewById(R.id.sx)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (current_edit) {
                    case LEFT_WING_EDIT:
                        scaleLeftWingX = progressToFloat(progress);
                        break;
                    case BODY_EDIT:
                        scaleBodyX = progressToFloat(progress);
                        break;
                    case RIGHT_WING_EDIT:
                        scaleRightWingX = progressToFloat(progress);
                        break;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ((SeekBar)findViewById(R.id.sy)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (current_edit) {
                    case LEFT_WING_EDIT:
                        scaleLeftWingY = progressToFloat(progress);
                        break;
                    case BODY_EDIT:
                        scaleBodyY = progressToFloat(progress);
                        break;
                    case RIGHT_WING_EDIT:
                        scaleRightWingY = progressToFloat(progress);
                        break;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ((SeekBar)findViewById(R.id.sz)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (current_edit) {
                    case LEFT_WING_EDIT:
                        scaleLeftWingZ = progressToFloat(progress);
                        break;
                    case BODY_EDIT:
                        scaleBodyZ = progressToFloat(progress);
                        break;
                    case RIGHT_WING_EDIT:
                        scaleRightWingZ = progressToFloat(progress);
                        break;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Translate
        ((SeekBar)findViewById(R.id.tx)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (current_edit) {
                    case LEFT_WING_EDIT:
                        translateLeftWingX = progressToFloat(progress);
                        break;
                    case BODY_EDIT:
                        translateBodyX = progressToFloat(progress);
                        break;
                    case RIGHT_WING_EDIT:
                        translateRightWingX = progressToFloat(progress);
                        break;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ((SeekBar)findViewById(R.id.ty)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (current_edit) {
                    case LEFT_WING_EDIT:
                        translateLeftWingY = progressToFloat(progress);
                        break;
                    case BODY_EDIT:
                        translateBodyY = progressToFloat(progress);
                        break;
                    case RIGHT_WING_EDIT:
                        translateRightWingY = progressToFloat(progress);
                        break;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ((SeekBar)findViewById(R.id.tz)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (current_edit) {
                    case LEFT_WING_EDIT:
                        translateLeftWingZ = progressToFloat(progress);
                        break;
                    case BODY_EDIT:
                        translateBodyZ = progressToFloat(progress);
                        break;
                    case RIGHT_WING_EDIT:
                        translateRightWingZ = progressToFloat(progress);
                        break;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private float progressToFloat(int progress) {
        float f = 0.0f;

        if(progress == 100) {
            f = 0.0f;
        } else if(progress > 100) {
            f = (progress - 100) / 100f;
        } else {
            f = (progress - 100) / 100f;
        }
        //System.out.println("progressToFloat:" + progress + " " + f);
        return f;
    }

    private int floatToProgress(float f) {
        int i = 0;
        if(f== 0.0f) {
            i = 100;
        } else if (f > 0.0f) {
            i = (int)(f*100) + 100;
        } else {
            i = 100 + (int)(f*100);
        }
        //System.out.println("floatToProgress:" + f + " " + i);
        return i;
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

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        //Pass event to scaler!
        scaleGestureDetector.onTouchEvent(e);

        //Do not handle here if we are scaling!
        if(scaleMode)
            return true;

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPrevX;
                float dy = y - mPrevY;

                // reverse direction of rotation above the mid-line

                if (y > (float)findViewById(R.id.game_surface_view).getHeight() / 2.0f) {
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < (float)findViewById(R.id.game_surface_view).getWidth() / 2.0f) {
                    dy = dy * -1;
                }

                if(Math.abs(dx) > Math.abs(dy)) {
                    mXAngle += (dx) * TOUCH_SCALE_FACTOR;	// = 180.0f / 320
                }
                else {
                    mYAngle += (dy) * TOUCH_SCALE_FACTOR;
                }

                ((GLSurfaceView)findViewById(R.id.game_surface_view)).requestRender();
        }

        mPrevX = x;
        mPrevY = y;

        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        float zoom = detector.getScaleFactor();

        // We are zooming out
        if (zoom > 1f) {
            mZoom += ZOOM_FACTOR;
        } else {
            mZoom -= ZOOM_FACTOR;
        }

        // Make sure zoom is in a reasonable range!
        if (mZoom < -ZOOM_LIMIT) {
            mZoom = -ZOOM_LIMIT;
        } else if (mZoom > ZOOM_LIMIT) {
            mZoom = ZOOM_LIMIT;
        }

        ((GLSurfaceView)findViewById(R.id.game_surface_view)).requestRender();

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        scaleMode = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        scaleMode = false;
    }
}
