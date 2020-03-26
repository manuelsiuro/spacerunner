package com.msa.spacerunner.ui.activities;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PointF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.msa.spacerunner.GameBoard;
import com.msa.spacerunner.GameConstants;
import com.msa.spacerunner.GameData;
import com.msa.spacerunner.GamePreferences;
import com.msa.spacerunner.R;
import com.msa.spacerunner.collision.BoundingBox3D;
import com.msa.spacerunner.engine.GameActor;
import com.msa.spacerunner.engine.GeometryBuilder;
import com.msa.spacerunner.engine.Node;
import com.msa.spacerunner.shaders.ShadersUtils;

import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// https://www.learnopengles.com/android-lesson-four-introducing-basic-texturing/
// https://github.com/learnopengles/Learn-OpenGLES-Tutorials/tree/641fcc25158dc30f45a7b2faaab165ec61ebb54b

public class GameActivity extends Activity implements GLSurfaceView.Renderer {

    //Game state
    public enum GameState {START, PLAYING, WIN, LOSE}
    static GameState _gameState;

    public static final int POSITION_ATTRIBUTE_ID = 0;  //Attribute pointer for shaders
    public static final int TEXTURE_ATTRIBUTE_ID = 1;   // "
    public static final int NORMAL_ATTRIBUTE_ID = 2;    // "

    static final int MAX_NODES_LOADED = 30;             //How many rows of objects can be on screen

    // Ship params, could be change in future
    static final float GRAVITY = -9.8f;                 //Gravitational constant
    static final float TURN_POWER = 20.0f;              //Sideways acceleration
    static final float JUMP_POWER = 3.0f;               //Vertical velocity of jumps
    static final float MAX_SIDE_VELOCITY = 3.0f;        //Upper limit on sideways velocity
    static final float MAX_VELOCITY = 20.0f;            //Top speed 15.0f
    static final float BUMP_VELOCITY = -1.0f;           //Bump
    static final float BOOST_VELOCITY = 10.0f;          //Boost

    static final float SHIP_WIDTH = 0.75f;              //For object collision
    static final float SHIP_LENGTH = 0.75f;

    static final int SHIP_DECREASE_HEALTH = 10;

    //Locations for uniform variables in shaders
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

    //public static int _textureSamplerLoc;
    //public static int _wordsMvmLoc;
    //public static int _wordsProjLoc;

    //Shader program
    static int _program = -1;
    //static int _wordsProgram = -1;

    /*
    //This is a handle to our cube shading program.
    private int mProgramHandle;
    //This is a handle to our texture data.
    private int mTextureDataHandle;
    //This will be used to pass in the transformation matrix.
    private int mMVPMatrixHandle;
    //This will be used to pass in the modelview matrix.
    private int mMVMatrixHandle;
    //This will be used to pass in the light position.
    private int mLightPosHandle;
    //This will be used to pass in the texture.
    private int mTextureUniformHandle;
    //This will be used to pass in model position information.
    private int mPositionHandle;
    //This will be used to pass in model color information.
    private int mColorHandle;
    //This will be used to pass in model normal information.
    private int mNormalHandle;
    //This will be used to pass in model texture coordinate information.
    private int mTextureCoordinateHandle;
*/

    public static int[] _textures = new int[1];    //Loaded textures for text

    static GameBoard _gameBoard;    //Contains all objects on track (squares, tetra, floor, etc.)
    static String _gameName;        //Name of the board (used for keeping records etc)

    //Variables used for keeping track of time in-game, and for calculating distance/velocity
    static long _time;
    static long _timeStarted;
    static long _gameTime;


    boolean noMusic, noSound;   //Options set by user
    SoundPool soundPool;        //Used to play sounds in-game
    int[] soundIds;             //Contains loaded sounds
    MediaPlayer backgroundMusic;    //Used to play the background music in-game

    //Variables to calculate movement between frames
    float _boostVelocity;
    float _velocity, _acceleration, _sideVelocity, _sideAcceleration, _verticalVelocity;
    float _shipPositionX, _shipPositionY, _shipPositionZ;
    float _positionOffset = 3.0f;
    boolean _onGround, _stabilize, _falling, _onBlock;

    int shipHealth = 100;

    //Screen size
    int _width, _height;

    //Matrices used to keep geometry within same coordinate space
    float[] _projectionMatrix;
    public static float[] _wordsProjectionMatrix;
    float[] _modelViewMatrix;

    Node[] _shipNodes;  //Contains geometry for the player's ship

    //Initial values for camera and the lookAt function.
    float[] eye = {0.0f, 0.5f, 3.0f};
    float[] at = {0.0f, 0.0f, 0.0f};

    float theta = 0.0f; //Theta constantly incremented during game play to rotate objects

    int _trackSize;     //Length of the in-game track

    HashMap<Integer, PointF> _activePointers;   //Data structure to maintain multiple screen touches
    int _countDown;         //Used to countdown at the beginning of the game
    int _coinsCollected;    //Keeps track of tetrahedron collected

    private final static int MAX_STREAM = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_game_activity);
        hideSystemUI();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            _gameName = bundle.getString(GameConstants.GAME_NAME);
            noMusic = bundle.getBoolean(GameConstants.GAME_MUSIC_OFF);
            noSound = bundle.getBoolean(GameConstants.GAME_SOUND_OFF);
        }

        //Touch inputs
        _activePointers = new HashMap<>();

        GLSurfaceView surfaceView = findViewById(R.id.game_surface_view);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surfaceView.setRenderer(this);

        findViewById(R.id.restart_button).setOnClickListener(v -> {
            findViewById(R.id.end_game).setVisibility(View.GONE);
            restartTrack();
        });

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        initializeSound();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!noMusic) backgroundMusic.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!noMusic) backgroundMusic.stop();
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

        initializeBoard();
        initializeGameValues();

        playNoise(Noises.COUNTDOWN);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10Unused, int width, int height) {
        initializeViewport(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        handleButtonPress();

        switch (_gameState) {
            case START:
                _countDown -= updateTime();
                if (_countDown <= 0) {
                    _gameState = GameState.PLAYING;
                }
                break;
            case PLAYING:
                if (_shipPositionZ + _positionOffset > _trackSize) {
                    _gameState = GameState.WIN;
                    clearButtonPresses();
                } else
                    calculateMovement();
                break;
            case WIN:
            case LOSE:
                break;
        }

        drawScene();
    }

    private void drawScene() {
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

        //renders all objects on screen
        drawGameMap(_shipPositionZ);

        //Render the ship last
        for (int i = 0; i < 3; i++)
            _shipNodes[i].render();

        //Now render the text (for score and time)
        /*GLES20.glUseProgram(_wordsProgram);
        drawText(_timeStarted, _time);*/

        runOnUiThread(() -> drawUi(_timeStarted, _time));
    }

    /**
     * Recursive method; will call itself MAX_NODES_LOADED times
     * The track in-game is organized into rows (ActorGroups); this method renders one ActorGroup.
     * After it renders an ActorGroup, the method moves up the track one row and calls itself.
     * <p>
     * Traverse through each object on screen and render it using a model matrix
     * (Not the most efficient way to do things; very quick and dirty.)
     *
     * @param position - where the player is at on the game board
     */
    /*
     * y  z
     * | /
     * |/
     * ---x
     **/
    private void drawGameMap(float position) {
        int pos = (int) position; //Get truncated value of position
        if (pos > _gameBoard.getSize() - 1 || pos - _shipPositionZ > MAX_NODES_LOADED)
            return; //Return if position exceeds board length or max number of nodes

        //upper layer
        for (int i = 0; i < GameBoard.BOARD_WIDTH; i++) {

            GameActor actor = _gameBoard.getBoard().get(pos).getUpperLayer()[i];

            if (actor.getType() == GameActor.ActorType.empty)
                continue;

            float[] instanceMatrix;

            instanceMatrix = _modelViewMatrix.clone();

            Matrix.scaleM(instanceMatrix, 0, 1.0f, 1.0f, 1.0f);
            Matrix.translateM(instanceMatrix, 0, (-2.0f + i), 0.0f, (6.0f - pos + _shipPositionZ));

            if (actor.getType() == GameActor.ActorType.barrier) {
                Matrix.scaleM(instanceMatrix, 0, 1.0f, 1.0f, 1.0f);
                //Matrix.translateM(instanceMatrix, 0, 0.0f, 0.3f, 0.0f);

                // Move up and down with cos
                /*float y = (float)Math.cos(Math.toRadians(theta)) * 0.5f;
                Matrix.translateM(instanceMatrix, 0, 0.0f, y, 0.0f);*/

                // Move by one right and left with sin
                /*float x = (float)Math.sin(Math.toRadians(theta)) * 1.0f;
                Matrix.translateM(instanceMatrix, 0, x, 0.0f, 0.0f);*/

            }

            if (actor.getType() == GameActor.ActorType.move_right) {
                Matrix.scaleM(instanceMatrix, 0, 1.0f, 1.0f, 1.0f);
                float x = (float)Math.sin(Math.toRadians(theta)) * 1.0f;
                Matrix.translateM(instanceMatrix, 0, x, 0.0f, 0.0f);
                actor.boundingBox3D = BoundingBox3D.getBB3D(Math.round(x), actor.boundingBox3D.min.y, actor.boundingBox3D.min.z);
            }

            if (actor.getType() == GameActor.ActorType.move_left) {
                Matrix.scaleM(instanceMatrix, 0, 1.0f, 1.0f, 1.0f);
                float x = (float)Math.sin(Math.toRadians(theta)) * -1.0f;
                Matrix.translateM(instanceMatrix, 0, x, 0.0f, 0.0f);
                actor.boundingBox3D = BoundingBox3D.getBB3D(Math.round(x), actor.boundingBox3D.min.y, actor.boundingBox3D.min.z);
            }

            if (actor.getType() == GameActor.ActorType.move_up) {
                Matrix.scaleM(instanceMatrix, 0, 1.0f, 1.0f, 1.0f);
                float y = (float)Math.cos(Math.toRadians(theta)) * 0.5f;
                Matrix.translateM(instanceMatrix, 0, 0.0f, y, 0.0f);
                actor.boundingBox3D = BoundingBox3D.getBB3D(actor.boundingBox3D.min.x, Math.round(y), actor.boundingBox3D.min.z);
            }

            if (actor.getType() == GameActor.ActorType.move_down) {
                Matrix.scaleM(instanceMatrix, 0, 1.0f, 1.0f, 1.0f);
                float y = (float)Math.cos(Math.toRadians(theta)) * -0.5f;
                Matrix.translateM(instanceMatrix, 0, 0.0f, y, 0.0f);
                actor.boundingBox3D = BoundingBox3D.getBB3D(actor.boundingBox3D.min.x, Math.round(y), actor.boundingBox3D.min.z);
            }

            if (actor.getType() == GameActor.ActorType.coin) {
                Matrix.rotateM(instanceMatrix, 0, theta * 8.0f, 0.0f, 1.0f, 0.0f);
                Matrix.rotateM(instanceMatrix, 0, theta * 2.0f, 1.0f, 0.0f, 0.0f);
                Matrix.scaleM(instanceMatrix, 0, 0.5f, 0.5f, 0.5f);
            }

            if (actor.getType() == GameActor.ActorType.speed) {
                Matrix.rotateM(instanceMatrix, 0, theta * 2.0f, 0.0f, 1.0f, 0.0f);
                Matrix.scaleM(instanceMatrix, 0, 0.4f, 0.4f, 0.4f);
            }

            if (actor.getType() == GameActor.ActorType.slow) {
                Matrix.rotateM(instanceMatrix, 0, theta * 2.0f, 0.0f, 1.0f, 0.0f);
                Matrix.scaleM(instanceMatrix, 0, 0.4f, 0.4f, 0.4f);
            }



            Node shape = new Node(instanceMatrix);
            shape.setPoints(actor.getPoints());
            shape.setColor(actor.ambient, actor.diffuse, actor.specular);
            shape.setShine(actor.shine);
            shape.render();
        }

        //lower layer
        //GLES20.glUseProgram(_program);
        for (int i = 0; i < GameBoard.BOARD_WIDTH; i++) {
            GameActor actor = _gameBoard.getBoard().get(pos).getLowerLayer()[i];

            if (actor.getType() == GameActor.ActorType.empty)
                continue;

            float[] instanceMatrix;
            //Traversal
            instanceMatrix = _modelViewMatrix.clone();
            Matrix.scaleM(instanceMatrix, 0, 1.0f, 1.0f, 1.0f);
            Matrix.translateM(instanceMatrix, 0, (-2.0f + i), -0.5f, (6.0f - pos + _shipPositionZ));

            if (actor.getType() == GameActor.ActorType.pit) {
                Matrix.scaleM(instanceMatrix, 0, 1.0f, 1.0f, 1.0f);
                Matrix.translateM(instanceMatrix, 0, 0.0f, -1.0f, 0.0f);
            }

            Node shape = new Node(instanceMatrix);
            shape.setPoints(actor.getPoints());
            shape.setColor(actor.ambient, actor.diffuse, actor.specular);
            shape.setShine(actor.shine);
            shape.render();
        }

        //Render the next row of objects up the track
        position += 1.0f;
        drawGameMap(position); // Todo: why loop here ? Test
    }

    private void drawUi(long timeStart, long currTime) {
        int mins = (int) Math.floor((currTime - timeStart) / 60000) % 60;
        int secs = (int) Math.floor((currTime - timeStart) / 1000) % 60;
        int hundredths = (int) Math.floor((currTime - timeStart) / 10) % 100;
        String sCoins = (_coinsCollected < 100) ? (_coinsCollected < 10) ? "00" + _coinsCollected : "0" + _coinsCollected : "" + _coinsCollected;
        String sMins = (mins < 10) ? "0" + mins : "" + mins;
        String sSecs = (secs < 10) ? "0" + secs : "" + secs;
        String sHuns = (hundredths < 10) ? "0" + hundredths : "" + hundredths;

        ((TextView)findViewById(R.id.score)).setText(sCoins);
        String timeValue = sMins + ":" + sSecs + ":" + sHuns;
        ((TextView)findViewById(R.id.time)).setText(timeValue);

        ((ProgressBar)findViewById(R.id.health)).setProgress(shipHealth);
        if(shipHealth <= 0) {
            death();
        }

        switch (_gameState) {
            case PLAYING:
                findViewById(R.id.countdown_layout).setVisibility(View.GONE);
                findViewById(R.id.end_game).setVisibility(View.GONE);
                break;
            case WIN:
            case LOSE:
                ((TextView)findViewById(R.id.game_status)).setText(_gameState == GameState.WIN ? R.string.win_status : R.string.loose_status);
                findViewById(R.id.end_game).setVisibility(View.VISIBLE);
                break;
            case START:
                String countValue = String.valueOf(_countDown / 1000);
                ((TextView)findViewById(R.id.counter)).setText(countValue);
                findViewById(R.id.countdown_layout).setVisibility(View.VISIBLE);
                break;
        }
    }

    /*private void drawText(long timeStart, long currTime) {
        //Calculate specific components of the time.
        int mins = (int) Math.floor((currTime - timeStart) / 60000) % 60;
        int secs = (int) Math.floor((currTime - timeStart) / 1000) % 60;
        int hundredths = (int) Math.floor((currTime - timeStart) / 10) % 100;

        //Convert values to strings, with leading zeroes when necessary
        String sCoins = (_coinsCollected < 100) ? (_coinsCollected < 10) ? "00" + _coinsCollected : "0" + _coinsCollected : "" + _coinsCollected;
        String sMins = (mins < 10) ? "0" + mins : "" + mins;
        String sSecs = (secs < 10) ? "0" + secs : "" + secs;
        String sHuns = (hundredths < 10) ? "0" + hundredths : "" + hundredths;

        //display[] contains all the numeral text at top of screen, based on time components and tetra collected
        NumberGraphic[] display = new NumberGraphic[10];
        display[0] = new NumberGraphic(-0.45f, 0.9f, ':', 0.05f); //Triangle figure
        display[1] = new NumberGraphic(-0.4f, 0.9f, sCoins.charAt(0), 0.05f);
        display[2] = new NumberGraphic(-0.35f, 0.9f, sCoins.charAt(1), 0.05f);
        display[3] = new NumberGraphic(-0.3f, 0.9f, sCoins.charAt(2), 0.05f);

        display[4] = new NumberGraphic(0.1f, 0.9f, sMins.charAt(0), 0.05f); //Minutes
        display[5] = new NumberGraphic(0.15f, 0.9f, sMins.charAt(1), 0.05f);

        display[6] = new NumberGraphic(0.225f, 0.9f, sSecs.charAt(0), 0.05f); // Seconds
        display[7] = new NumberGraphic(0.275f, 0.9f, sSecs.charAt(1), 0.05f);

        display[8] = new NumberGraphic(0.35f, 0.9f, sHuns.charAt(0), 0.05f); //Hundredths
        display[9] = new NumberGraphic(0.40f, 0.9f, sHuns.charAt(1), 0.05f);

        //Render the numbers
        for (int i = 0; i < 10; i++) {
            display[i].render();
        }

        //The text for "TIME:"
        StringGraphic timeGraphic = new StringGraphic(0.0f, 0.9f, 0, 0.05f);
        timeGraphic.render();

        //Render the countdown if game is starting
        if (_gameState == GameState.START) {
            NumberGraphic count = new NumberGraphic(0.0f, 0.0f, (char) ((_countDown / 1000) + 49), 0.25f);
            count.render();
        }

        //Render the GAME OVER screen, plus AGAIN and BACK if lose
        if (_gameState == GameState.LOSE) {
            StringGraphic[] loseScreen = new StringGraphic[3];
            loseScreen[0] = new StringGraphic(0.0f, 0.0f, 1, 0.125f);
            loseScreen[1] = new StringGraphic(-0.3f, -0.3f, 3, 0.05f);
            loseScreen[2] = new StringGraphic(0.3f, -0.3f, 4, 0.05f);

            for (int i = 0; i < 3; i++)
                loseScreen[i].render();
        }

        //Render the COMPLETE screen, plus AGAIN and BACK if win
        if (_gameState == GameState.WIN) {
            StringGraphic[] winScreen = new StringGraphic[3];
            winScreen[0] = new StringGraphic(0.0f, 0.0f, 2, 0.125f);
            winScreen[1] = new StringGraphic(-0.3f, -0.3f, 3, 0.05f);
            winScreen[2] = new StringGraphic(0.3f, -0.3f, 4, 0.05f);

            for (int i = 0; i < 3; i++)
                winScreen[i].render();
        }
    }*/


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pIndex = event.getActionIndex();
        int pId = event.getPointerId(pIndex);
        int maskedAction = event.getActionMasked();

        //We only really care about actions down and actions up;
        // if down, store the pointer. if up, remove it.
        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                PointF p = new PointF();
                p.x = event.getX(pIndex);
                p.y = event.getY(pIndex);
                _activePointers.put(pId, p);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                _activePointers.remove(pId);
        }

        return true;
    }

    private void handleButtonPress() {

        switch (_gameState) {
            case START:
                break;
            case PLAYING:
                boolean leftPressed = false;
                boolean rightPressed = false;
                boolean jumpPressed = false;

                //Reset these at start of getting inputs
                _sideAcceleration = 0.0f;
                _stabilize = true;

                int removalKey = -1; //Initial value; flags key to be removed from pointers

                for (Integer key : _activePointers.keySet()) {
                    PointF p = _activePointers.get(key);
                    if (p == null)
                        continue;   //If pointer is no longer there, move on

                    float percentX = (p.x * 100.0f) / _width;
                    float percentY = (p.y * 100.0f) / _height;

                    //Pressing right or left will cancel the other direction
                    if (percentX < 20) {
                        leftPressed = true;
                        rightPressed = false;
                    } else if (percentX > 80) {
                        rightPressed = true;
                        leftPressed = false;
                    } else if (percentY > 50) {
                        jumpPressed = true;
                        removalKey = key;
                    }
                }

                if (removalKey != -1) //If changed, then we remove this key
                    _activePointers.remove(removalKey);

                //Handle different inputs
                if (leftPressed) { //Turn Left
                    _sideAcceleration = -TURN_POWER;
                    _stabilize = false;
                }

                if (rightPressed) { //Turn Right
                    _sideAcceleration = TURN_POWER;
                    _stabilize = false;
                }

                if (jumpPressed) {  //Jump
                    if (_onGround) {
                        _verticalVelocity = JUMP_POWER;
                        _onGround = false;
                    }
                }
                break;
            case WIN:
            case LOSE:
                leftPressed = false;
                rightPressed = false;
                /*for (Integer key : _activePointers.keySet()) {
                    PointF p = _activePointers.get(key);
                    if(p == null) {
                        return;
                    }
                    float percentX = (p.x * 100.0f) / _width;
                    //Pressing right or left will cancel the other direction
                    if (percentX < 50) {
                        leftPressed = true;
                    } else {
                        rightPressed = true;
                    }
                }
                //Again
                if (leftPressed) {
                    restartTrack();
                } else if (rightPressed) {
                    //Back
                    finish();
                }*/
                break;
        }
    }

    private void calculateMovement() {
        //Calculate factor based on time elapsed since last call of this method
        //This keeps ship moving at a steady rate, regardless of rendering or processing time
        float factor = updateTime() / 1000.0f;

        //First, calculate velocity(s)
        _boostVelocity -= 1.0f;
        if (_boostVelocity <= 0.0f) _boostVelocity = 0.0f;

        _velocity += (_acceleration * factor) + _boostVelocity;
        if (_velocity > MAX_VELOCITY && _boostVelocity == 0.0f) _velocity = MAX_VELOCITY;
        if (_velocity <= -5.0f) _velocity = 1.0f;

        //Calculate sideways movement
        //  If turning the opposite direction of movement, or if turning stopped,
        //  then the ship is "stabilizing", meaning it needs to stop moving quickly
        //  to make turning more responsive.
        if ((_sideVelocity > 0 && _sideAcceleration < 0) || (_sideVelocity < 0 && _sideAcceleration > 0) || (_stabilize)) {

            _sideVelocity -= (_sideVelocity / 2);

            if (Math.abs(_sideVelocity) < 0.2) {
                _sideVelocity = 0.0f;
            }
        } else {
            _sideVelocity += (_sideAcceleration * factor); //Otherwise, just move ship horizontally
        }

        //Cap sideways velocity
        if (_sideVelocity > MAX_SIDE_VELOCITY) _sideVelocity = MAX_SIDE_VELOCITY;
        if (_sideVelocity < -MAX_SIDE_VELOCITY) _sideVelocity = -MAX_SIDE_VELOCITY;

        //Move ship down by gravitational constant.
        _verticalVelocity += (GRAVITY * factor);

        //Now we detect collision with objects
        collisionDetection(factor);

        //If low frame rate, position will skip over squares at high velocity
        float tempPosition = _shipPositionZ;
        float tempVelocity = _velocity * factor;
        while (tempVelocity > 1.0f) {
            tempVelocity -= 1.0f;
            _shipPositionZ = tempPosition;
            _shipPositionZ += tempVelocity;
            collisionDetection(factor);
        }
        _shipPositionZ = tempPosition;

        //Forward motion
        _shipPositionZ += (_velocity * factor); //Z position of ship... think "where is ship on the track"

        //Horizontal Movement
        _shipPositionX += (_sideVelocity * factor);

        //Vertical movement
        //Falling flag set in collisionDetection method
        _shipPositionY += (_verticalVelocity * factor);

        if (_shipPositionY < 0.0f && !_falling) {

            _shipPositionY = 0.0f;
            _verticalVelocity = 0.0f;
            _onGround = true;

        } else if (_falling) {

            if (_onGround) {
                _verticalVelocity = -1.0f; //Make falling a little more punishing as well.
                _onGround = false;
            }

            //If fallen too far, we are dead.
            if (_shipPositionY < -3.0f) {
                death();
            }
        }

        //Rotates objects that need to rotate.
        theta += 1.0f;
    }

    // Factor is time elapsed between frames in ms divided by 1000.0f.
    private void collisionDetection(float factor) {

        //Calculate necessary offset for x-coordinate
        float coordCorrection = (0.5f * (_shipPositionX) / Math.abs((_shipPositionX)));

        //Get actual x-coordinate
        int realCoordinate = (int) (_shipPositionX + coordCorrection);
        int realLeftCoordinate = (int) (realCoordinate - 1.0f);
        int realRightCoordinate = (int) (realCoordinate + 1.0f);

        //Horizontal ship padding. Used for offsetting horizontal collision to make it feel better.
        float horizontalShipPad = SHIP_WIDTH / 4.0f;
        horizontalShipPad *= Math.abs(_sideVelocity) > 0 ? ((_sideVelocity) / Math.abs((_sideVelocity))) : 0;

        //Calculate potential coordinates, based on horizontal velocity (ie where the ship COULD end up)
        int potentialRealCoordinate = (int) ((_shipPositionX + horizontalShipPad + ((_sideVelocity + (_sideAcceleration * factor)) * factor)) + coordCorrection);

        //Calculate where the wings sit and where they will sit based on velocity/acceleration
        int leftWingCoordinate = (int) ((_shipPositionX - (SHIP_WIDTH / 3.0f) + coordCorrection));
        float potentialLeftWingCoordinate = (int) ((_shipPositionX - (SHIP_WIDTH / 3.0f) + ((_sideVelocity + (_sideAcceleration * factor)) * factor)) + coordCorrection);
        int rightWingCoordinate = (int) ((_shipPositionX + (SHIP_WIDTH / 3.0f) + coordCorrection));
        float potentialRightWingCoordinate = (int) ((_shipPositionX + (SHIP_WIDTH / 3.0f) + ((_sideVelocity + (_sideAcceleration * factor)) * factor)) + coordCorrection);

        //Position of the ship along the track (ie Z-coordinate)
        int realPosition = (int) (_shipPositionZ + _positionOffset);
        float forwardShipPad = SHIP_LENGTH / 2.0f; //Offset collision of ship's nose
        int potentialRealPosition = (int) ((_shipPositionZ + _positionOffset) + forwardShipPad + ((_velocity + (_acceleration * factor)) * factor));

        //Condition for when we need to check the objects that are positioned diagonally in front of the ship.
        boolean checkCorners = (potentialRealPosition != realPosition && (potentialRealCoordinate != realCoordinate || (leftWingCoordinate != realCoordinate ^ rightWingCoordinate != realCoordinate)));

        //Get current and all adjacent Actors relative to the ship
        GameActor currentUpperSquare = null;
        GameActor currentLowerSquare = null;
        GameActor forwardUpperSquare = null;

        GameActor forwardLowerSquare = null;

        GameActor leftUpperSquare = null;
        GameActor rightUpperSquare = null;

        GameActor leftForwardUpperSquare = null;
        GameActor rightForwardUpperSquare = null;

        if (realCoordinate >= -2 && realCoordinate <= 2) {

            currentUpperSquare = (realPosition >= _trackSize) ? null : _gameBoard.getBoard().get(realPosition).getUpperLayer()[realCoordinate + 2];
            currentLowerSquare = (realPosition >= _trackSize) ? null : _gameBoard.getBoard().get(realPosition).getLowerLayer()[realCoordinate + 2];
            forwardUpperSquare = (realPosition >= _trackSize - 1) ? null : _gameBoard.getBoard().get(realPosition + 1).getUpperLayer()[realCoordinate + 2];

            forwardLowerSquare = (realPosition >= _trackSize - 1) ? null : _gameBoard.getBoard().get(realPosition + 1).getLowerLayer()[realCoordinate + 2];

            if (realLeftCoordinate >= -2) {
                leftUpperSquare = (realPosition >= _trackSize) ? null : _gameBoard.getBoard().get(realPosition).getUpperLayer()[realLeftCoordinate + 2];
                leftForwardUpperSquare = (realPosition >= _trackSize - 1) ? null : _gameBoard.getBoard().get(realPosition + 1).getUpperLayer()[realLeftCoordinate + 2];
            }

            if (realRightCoordinate <= 2) {
                rightUpperSquare = (realPosition >= _trackSize) ? null : _gameBoard.getBoard().get(realPosition).getUpperLayer()[realRightCoordinate + 2];
                rightForwardUpperSquare = (realPosition >= _trackSize - 1) ? null : _gameBoard.getBoard().get(realPosition + 1).getUpperLayer()[realRightCoordinate + 2];
            }
        }

        // Power ups, coin ...
        if (currentUpperSquare != null && _shipPositionY >= 0.0f) {

            GameActor.ActorType type = currentUpperSquare.getType();

            //Coin case
            if (type == GameActor.ActorType.coin) {
                playNoise(Noises.COIN);
                _coinsCollected++;
                _gameBoard.getBoard().get((int) (_shipPositionZ + _positionOffset)).getUpperLayer()[realCoordinate + 2] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(_shipPositionX, _shipPositionY, _shipPositionZ));
            }

            if (type == GameActor.ActorType.speed) {
                playNoise(Noises.SPEED_UP);
                _boostVelocity = BOOST_VELOCITY;
                _gameBoard.getBoard().get((int) (_shipPositionZ + _positionOffset)).getUpperLayer()[realCoordinate + 2] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(_shipPositionX, _shipPositionY, _shipPositionZ));
            }

            if (type == GameActor.ActorType.slow) {
                playNoise(Noises.SPEED_DOWN);
                _velocity = 2.0f;
                _gameBoard.getBoard().get((int) (_shipPositionZ + _positionOffset)).getUpperLayer()[realCoordinate + 2] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(_shipPositionX, _shipPositionY, _shipPositionZ));
            }
        }

        //Barrier cases
        // forwardUpperSquare = (realPosition >= _trackSize - 1) ? null : _gameBoard.getBoard().get(realPosition + 1).getUpperLayer()[realCoordinate + 2];
        /*for (int i = 0; i < 5; i++) {
            if((realPosition >= _trackSize - 1)) {
                return;
            }
            GameActor a = _gameBoard.getBoard().get(realPosition + 1).getUpperLayer()[i];
            GameActor.ActorType type = a.getType();

            if ((type == GameActor.ActorType.barrier
                    || type == GameActor.ActorType.move_right
                    || type == GameActor.ActorType.move_left
                    || type == GameActor.ActorType.move_up
                    || type == GameActor.ActorType.move_down)
            && Math.round(_shipPositionX) == a.boundingBox3D.min.x) {

                if(_shipPositionY >= 0.0f && _shipPositionY <= a.boundingBox3D.max.y) {
                    if(a.boundingBox3D.intersects(BoundingBox3D.getBB3D(_shipPositionX, _shipPositionY, _position + _positionOffset))) {
                        System.out.println("==> Front Collision <==");
                        if (_velocity > 4.0f) {
                            playNoise(Noises.BUMP);
                        }
                        _velocity = BUMP_VELOCITY;
                    }
                }
            }
        }*/

        //Block directly in front
        if (forwardUpperSquare != null && forwardLowerSquare != null && _shipPositionY >= -0.1f) {
            GameActor.ActorType type = forwardUpperSquare.getType();

            /*GameActor.ActorType lowerType = forwardLowerSquare.getType();
            if (type == GameActor.ActorType.empty
                    && lowerType == GameActor.ActorType.floor
                    && potentialRealPosition == realPosition + 1
                    && _shipPositionY < 1.0f) {
                _onBlock = false;
                _onGround = true;
            }*/

            //If the ship could potentially be in the block...
            if ((type == GameActor.ActorType.barrier
                    || type == GameActor.ActorType.move_right
                    || type == GameActor.ActorType.move_left
                    || type == GameActor.ActorType.move_up
                    || type == GameActor.ActorType.move_down)
                    && potentialRealPosition == realPosition + 1) {
                //System.out.println("==> x:" + _shipPositionX + " y:" + _shipPositionY + " z:" + _position + " coordCorrection:" + coordCorrection + " realCoordinate:"+  realCoordinate + " potentialRealCoordinate:" + potentialRealCoordinate + " realPosition:" + realPosition + " potentialRealPosition:" + potentialRealPosition);
                //System.out.println("==> absx:" + Math.round(_shipPositionX) + " absy:" + Math.round(_shipPositionY) + " absz:" + Math.round(_position + _positionOffset));
                //System.out.println("==> Ship x:" + _shipPositionX + " y:" + _shipPositionY + " z:" + _position);
                //System.out.println("==> Block x:" + forwardUpperSquare.boundingBox3D.min.x + "/" + forwardUpperSquare.boundingBox3D.max.x + " y:" + forwardUpperSquare.boundingBox3D.min.y + "/" + forwardUpperSquare.boundingBox3D.max.y + " z:" + forwardUpperSquare.boundingBox3D.min.z + "/" + forwardUpperSquare.boundingBox3D.max.z);
                //System.out.println("==> fmaxx:" + forwardUpperSquare.boundingBox3D.max.x + " fy:" + forwardUpperSquare.boundingBox3D.max.y + " fz:" + forwardUpperSquare.boundingBox3D.max.z);
                //BoundingBox3D.getBB3D(Math.round(_shipPositionX), Math.round(_shipPositionY), Math.round(_position + _positionOffset))

                // Front
                if(_shipPositionY >= 0.0f && _shipPositionY <= forwardUpperSquare.boundingBox3D.max.y) {
                    if(forwardUpperSquare.boundingBox3D.intersects(BoundingBox3D.getBB3D(_shipPositionX, _shipPositionY, _shipPositionZ + _positionOffset))) {
                        //System.out.println("==> Front Collision <==");
                        if (_velocity > 4.0f) {
                            playNoise(Noises.BUMP);
                            shipHealth -= SHIP_DECREASE_HEALTH;
                        }
                        _velocity = BUMP_VELOCITY;
                    }
                }

                // Upper
                if(_shipPositionY > forwardUpperSquare.boundingBox3D.max.y) {
                    if(forwardUpperSquare.boundingBox3D.intersects(BoundingBox3D.getBB3D(_shipPositionX, _shipPositionY, _shipPositionZ + _positionOffset))) {
                        _shipPositionY = forwardUpperSquare.boundingBox3D.max.y;
                        _verticalVelocity = 1.0f;
                        //System.out.println("==> Upper Collision <==");
                        _onBlock = true;
                        _onGround = false;
                    }
                }
            }
        }

        //Block directly to left
        if (leftUpperSquare != null && _shipPositionY >= 0.0f) {
            GameActor.ActorType type = leftUpperSquare.getType();
            if (type == GameActor.ActorType.barrier && (potentialRealCoordinate == realLeftCoordinate || potentialLeftWingCoordinate == realLeftCoordinate)) {
                if (_sideVelocity < 0.0f) {
                    _sideVelocity = 0.0f;
                    potentialRealCoordinate = realCoordinate;
                }
            }
        }

        //Block directly to right
        if (rightUpperSquare != null && _shipPositionY >= 0.0f) {
            GameActor.ActorType type = rightUpperSquare.getType();
            if (type == GameActor.ActorType.barrier && (potentialRealCoordinate == realRightCoordinate || potentialRightWingCoordinate == realRightCoordinate)) {
                if (_sideVelocity > 0.0f) {
                    _sideVelocity = 0.0f;
                    potentialRealCoordinate = realCoordinate;
                }
            }
        }

        //Corner cases; blocks diagonally in front
        if (checkCorners) {

            //Diagonal left
            if (leftForwardUpperSquare != null && _shipPositionY >= 0.0f) {
                GameActor.ActorType type = leftForwardUpperSquare.getType();
                if (type == GameActor.ActorType.barrier && potentialRealPosition == realPosition + 1) {
                    if (potentialLeftWingCoordinate == realLeftCoordinate) {
                        if (_sideVelocity < 0.0f) {
                            _sideVelocity = 0.0f;
                        }
                        if (potentialRealCoordinate == realLeftCoordinate || leftWingCoordinate == realLeftCoordinate) {
                            //Collide with the block
                            if (_velocity > 4.0f) {
                                playNoise(Noises.BUMP);
                                shipHealth -= SHIP_DECREASE_HEALTH;
                            }
                            _velocity = BUMP_VELOCITY;
                        }
                    }
                }
            }

            //Diagonal right
            if (rightForwardUpperSquare != null && _shipPositionY >= 0.0f) {
                GameActor.ActorType type = rightForwardUpperSquare.getType();
                if (type == GameActor.ActorType.barrier && potentialRealPosition == realPosition + 1) {
                    if (potentialRightWingCoordinate == realRightCoordinate) {
                        if (_sideVelocity > 0.0f) {
                            _sideVelocity = 0.0f;
                        }
                        if (potentialRealCoordinate == realRightCoordinate || rightWingCoordinate == realRightCoordinate) {
                            //Collide with the block
                            if (_velocity > 4.0f) {
                                playNoise(Noises.BUMP);
                                shipHealth -= SHIP_DECREASE_HEALTH;
                            }
                            _velocity = BUMP_VELOCITY;
                        }
                    }
                }
            }
        }

        //Hole case
        if (currentLowerSquare != null) {

            GameActor.ActorType type = currentLowerSquare.getType();

            //Check ship Y to give a little leeway to the player
            if (type == GameActor.ActorType.floor && _shipPositionY >= -0.2f) {
                _falling = false;
            } else if (type == GameActor.ActorType.empty || _shipPositionY < -0.2f) {
                _falling = true;
            }

        } else {
            _falling = true;
        }
    }

    private void death() {
        _gameState = GameState.LOSE;
        clearButtonPresses();
    }

    private void restartTrack() {
        initializeBoard();
        initializeGameValues();
        playNoise(Noises.COUNTDOWN);
    }

    private float updateTime() {
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - _time;
        _time = currentTime;
        _gameTime = _time - _timeStarted;

        if (_gameState == GameState.START)
            _timeStarted = currentTime;

        return timeElapsed;
    }

    //clear all touch inputs.
    private void clearButtonPresses() {
        _activePointers.clear();
    }





    /**
     * Get the normal matrix of a given matrix
     *
     * @param mvm - the input matrix
     * @return - a normal matrix of the input matrix
     */
    public static float[] getNormalMatrix(float[] mvm) {
        float[] normalMatrix = new float[mvm.length];
        Matrix.setIdentityM(normalMatrix, 0);
        Matrix.invertM(normalMatrix, 0, mvm, 0);
        float[] normalMatrixTransposed = new float[mvm.length];
        Matrix.transposeM(normalMatrixTransposed, 0, normalMatrix, 0);
        return normalMatrixTransposed;
    }







    //----------------------------------------------------------------------------------------------
    private void initializeBoard() {
        if(_gameName != null) {
            GameBoard gb = GameData.getGameBoard(_gameName);
            if(gb != null) {
                _gameBoard = new GameBoard(GameData.getGameBoard(_gameName));
                _trackSize = _gameBoard.getSize();
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    private void initializeGameValues() {
        //Initialize starting values
        _coinsCollected = 0;
        _shipPositionZ = 0.0f;
        _velocity = 1.0f;
        _verticalVelocity = 0.0f;
        _acceleration = 1.0f;
        _shipPositionX = 0.0f;
        _shipPositionY = 0.0f;
        _sideVelocity = 0.0f;
        _falling = false;
        _onBlock = false;
        _onGround = true;
        _countDown = 3000;
        _timeStarted = System.currentTimeMillis();
        _time = _timeStarted;

        shipHealth = 100;

        _gameState = GameState.START;
    }
    //----------------------------------------------------------------------------------------------
    //https://www.learnopengles.com/tag/ambient-lighting/
    //https://github.com/learnopengles/Learn-OpenGLES-Tutorials/tree/master/android/AndroidOpenGLESLessons/app/src/main/res/raw

    /*protected String getVertexShader() {
        return RawResourceReader.readTextFileFromRawResource(this, R.raw.vertex_texture_shader);
    }*/

    /*protected String getFragmentShader() {
        return RawResourceReader.readTextFileFromRawResource(this, R.raw.fragment_texture_shader);
    }*/

    private void initializeGL() {
        //Read shaders from files
        String vertexShaderSource = ShadersUtils.getShaderFile(this, "vertex-shader.glsl");
        String fragmentShaderSource = ShadersUtils.getShaderFile(this, "fragment-shader.glsl");

        /*String texVertexShaderSource = ShadersUtils.getShaderFile(this, "tex_vertex-shader.glsl");
        String texFragmentShaderSource = ShadersUtils.getShaderFile(this, "tex_fragment-shader.glsl");*/

        //This program used to draw ship and track; includes lighting equations
        _program = ShadersUtils.createProgram(vertexShaderSource, fragmentShaderSource);

        //This program used to draw fonts on screen
        //_wordsProgram = ShadersUtils.createProgram(texVertexShaderSource, texFragmentShaderSource);

        //Bind attribute with the programs
        GLES20.glBindAttribLocation(_program, POSITION_ATTRIBUTE_ID, "position");// attribute vec4 position in 'vertex-shader.glsl'
        GLES20.glBindAttribLocation(_program, NORMAL_ATTRIBUTE_ID, "normal");// attribute vec4 normal in 'vertex-shader.glsl'

        //GLES20.glBindAttribLocation(_wordsProgram, POSITION_ATTRIBUTE_ID, "position");// attribute vec4 position in 'tex_vertex-shader.glsl'
        //GLES20.glBindAttribLocation(_wordsProgram, TEXTURE_ATTRIBUTE_ID, "texture");// attribute vec4 texture in 'tex_vertex-shader.glsl'

        //Link the programs, so we can easily swap between them at little cost
        GLES20.glLinkProgram(_program);
        String programLinkLog = GLES20.glGetProgramInfoLog(_program);
        Log.d("Program Link", programLinkLog + "\n");

        /*GLES20.glLinkProgram(_wordsProgram);
        programLinkLog = GLES20.glGetProgramInfoLog(_wordsProgram);
        Log.d("Program Link", programLinkLog + "\n");*/

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); //Clear color is Black

        //Enable the vertex attribute arrays
        GLES20.glEnableVertexAttribArray(POSITION_ATTRIBUTE_ID);
        GLES20.glEnableVertexAttribArray(NORMAL_ATTRIBUTE_ID);
        GLES20.glEnableVertexAttribArray(TEXTURE_ATTRIBUTE_ID);

        //Uniform locations
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

        /*_wordsMvmLoc = GLES20.glGetUniformLocation(_wordsProgram, "modelViewMatrix");
        _wordsProjLoc = GLES20.glGetUniformLocation(_wordsProgram, "projectionMatrix");
        _textureSamplerLoc = GLES20.glGetUniformLocation(_wordsProgram, "textureSampler");*/

        //Load the font bitmap into textures
        /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nums);
        GLES20.glGenTextures(1, _textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();*/ //always recycle!


        // Program texture
        /*
        final String vertexShader = getVertexShader();
        final String fragmentShader = getFragmentShader();

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"a_Position", "a_Color", "a_Normal", "a_TexCoordinate"});

        // Load the texture
        mTextureDataHandle = TextureHelper.loadTexture(this, R.drawable.bumpy_bricks_public_domain);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        //fragment
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        // vertex
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);
*/


    }
    //----------------------------------------------------------------------------------------------
    private void initializeViewport(int width, int height) {
        //Create the viewport
        GLES20.glViewport(0, 0, width, height);
        _width = width;
        _height = height;

        float ratio = (float) width / height;

        //Initialize and create the perspective matrices
        _projectionMatrix = new float[16];
        Matrix.perspectiveM(_projectionMatrix, 0, 90.0f, ratio, 0.01f, 30.0f);
        //_wordsProjectionMatrix = _projectionMatrix.clone(); //Used for drawing text

        //Move things back a bit
        Matrix.translateM(_projectionMatrix, 0, 0.0f, 0.0f, -3.0f);
        //Matrix.translateM(_wordsProjectionMatrix, 0, 0.0f, 0.0f, -1.0f);
    }
    //----------------------------------------------------------------------------------------------
    private enum Noises {BUMP, COUNTDOWN, COIN, SPEED_UP, SPEED_DOWN}

    private void initializeSound() {
        //Load sounds
        if (!noSound) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                soundPool = new SoundPool.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setMaxStreams(MAX_STREAM)
                        .build();
            } else {
                soundPool = new SoundPool(MAX_STREAM, AudioManager.STREAM_MUSIC, 0);
            }

            soundIds = new int[5];
            soundIds[0] = soundPool.load(this, R.raw.bump_noise, 1);
            soundIds[1] = soundPool.load(this, R.raw.coin_noise, 1);
            soundIds[2] = soundPool.load(this, R.raw.countdown_noise, 1);
            soundIds[3] = soundPool.load(this, R.raw.speed, 1);
            soundIds[4] = soundPool.load(this, R.raw.slow, 1);
        }

        //Load and start playing music
        if (!noMusic) {
            backgroundMusic = MediaPlayer.create(GameActivity.this, R.raw.music);
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(100, 100);
            backgroundMusic.start();
        }
    }

    private void playNoise(Noises type) {
        if (noSound)
            return;

        switch (type) {
            case BUMP:
                soundPool.play(soundIds[0], 1, 1, 1, 0, 1.0f);
                break;
            case COIN:
                soundPool.play(soundIds[1], 1, 1, 1, 0, 1.0f);
                break;
            case COUNTDOWN:
                soundPool.play(soundIds[2], 1, 1, 1, 0, 1.0f);
                break;
            case SPEED_UP:
                soundPool.play(soundIds[3], 1, 1, 1, 0, 1.0f);
                break;
            case SPEED_DOWN:
                soundPool.play(soundIds[4], 1, 1, 1, 0, 1.0f);
                break;
        }
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

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("GL_ERROR", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
