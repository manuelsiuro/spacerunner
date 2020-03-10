package com.msa.spacerunner.ui.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.msa.spacerunner.GameData;
import com.msa.spacerunner.GamePreferences;
import com.msa.spacerunner.GameRecord;
import com.msa.spacerunner.ui.fragments.LevelSelectFragment;
import com.msa.spacerunner.ui.fragments.OptionsFragment;
import com.msa.spacerunner.ui.fragments.TitleScreenFragment;
import com.msa.spacerunner.ui.fragments.TutorialFragment;
import com.msa.spacerunner.ui.fragments.ViewRecordsFragment;

// DOC:
// https://medium.com/@xzan/opengl-le-guide-du-noob-pour-d%C3%A9veloppeur-android-78f069c7214d
// https://bitbucket.org/Xzan/opengl-example/src/master/app/src/main/java/be/appkers/example/opengl/MainActivity.java

// https://github.com/andresoviedo/android-3D-model-viewer

// https://github.com/AlexeyZatsepin/Air-hockey-game/tree/master/app/src/main/java/study/example/azatsepin/airhockey

// GLSL : http://www.shaderific.com/glsl-functions

//https://blogs.perficient.com/2017/09/28/build-graphics-with-opengl-es-2-0/

public class MainActivity extends Activity implements TitleScreenFragment.OnActionListener/*, LevelSelectFragment.OnItemSelectedListener*/ {

    final static int FRAME_ID = 10;
    static boolean inRecordsSelect;
    public static boolean soundOff;
    public static boolean musicOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();

        FrameLayout frame = new FrameLayout(this);
        frame.setId(FRAME_ID);

        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction addTransaction = fragManager.beginTransaction();
        TitleScreenFragment tf = new TitleScreenFragment();
        tf.setOnActionListener(this);

        addTransaction.replace(FRAME_ID, tf);
        addTransaction.commit();

        inRecordsSelect = false;
        soundOff = GamePreferences.isSoundDisabled(this);
        musicOff = GamePreferences.isMusicDisabled(this);

        setContentView(frame);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    public void onAction(int action) {

        switch (action) {
            case TitleScreenFragment.ACTION_START_GAME:
                inRecordsSelect = false;
                activateLevelSelectFragment();

                /*Intent intent = new Intent(this, LessonFourActivity.class);
                startActivity(intent);*/
                break;
            case TitleScreenFragment.ACTION_SCORES:
                inRecordsSelect = true;
                //activateViewRecordsFragment();
                // Todo: handle record
                break;
            case TitleScreenFragment.ACTION_OPTIONS:
                activateOptionsFragment();
                break;
            case TitleScreenFragment.ACTION_HOW_TO_PLAY:
                activateTutorialFragment();
                break;
            case TitleScreenFragment.ACTION_EXIT:
                finishAndRemoveTask();
                break;
        }
    }

    private void activateLevelSelectFragment() {
        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction addTransaction = fragManager.beginTransaction();
        addTransaction.addToBackStack(null);
        LevelSelectFragment levelSelectFragment = new LevelSelectFragment();
        addTransaction.replace(FRAME_ID, levelSelectFragment);
        addTransaction.commit();
    }

    private void activateViewRecordsFragment(GameRecord record) {
        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction addTransaction = fragManager.beginTransaction();
        addTransaction.addToBackStack(null);
        ViewRecordsFragment viewRecordsFragment = new ViewRecordsFragment();
        viewRecordsFragment.setRecord(record);
        addTransaction.replace(FRAME_ID, viewRecordsFragment);
        addTransaction.commit();
    }

    private void activateOptionsFragment() {
        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction addTransaction = fragManager.beginTransaction();
        addTransaction.addToBackStack(null);
        OptionsFragment optionsFragment = new OptionsFragment();
        addTransaction.replace(FRAME_ID, optionsFragment);
        addTransaction.commit();
    }

    private void activateTutorialFragment() {
        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction addTransaction = fragManager.beginTransaction();
        addTransaction.addToBackStack(null);
        TutorialFragment tutorialFragment = new TutorialFragment();
        addTransaction.replace(FRAME_ID, tutorialFragment);
        addTransaction.commit();
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
     * Interface method for the LevelSelectFragment; either starts a game using the selected level,
     *  or views the records for the selected level.
     * @param gameListFragment - not really used
     * @param gameName - Name of the level selected
     */
    /*@Override
    public void onItemSelected(LevelSelectFragment gameListFragment, String gameName) {
        if (inRecordsSelect) {
            activateViewRecordsFragment(GameData.getGameRecord(gameName));
        } else {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("gameName", gameName);
            intent.putExtra("soundOff", soundOff);
            intent.putExtra("musicOff", musicOff);
            startActivity(intent);
        }
    }*/
}
