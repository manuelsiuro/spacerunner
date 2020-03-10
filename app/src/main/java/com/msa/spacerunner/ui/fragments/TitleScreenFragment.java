package com.msa.spacerunner.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.msa.spacerunner.R;

public class TitleScreenFragment extends Fragment {

    public final static int ACTION_START_GAME = 0;
    public final static int ACTION_SCORES = 1;
    public final static int ACTION_OPTIONS = 2;
    public final static int ACTION_HOW_TO_PLAY = 3;
    public final static int ACTION_EXIT = 4;

    OnActionListener onActionListener;

    private Context context;
    private View view;

    public interface OnActionListener {
        void onAction(int action);
    }

    public void setOnActionListener(OnActionListener osl) {
        this.onActionListener = osl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_title_screen, container, false);

        context = view.getContext();

        view.findViewById(R.id.btnStart).setOnClickListener(v -> runAnimation(ACTION_START_GAME));
        view.findViewById(R.id.btnExit).setOnClickListener(v -> runAnimation(ACTION_EXIT));
        view.findViewById(R.id.btnScore).setOnClickListener(v -> runAnimation(ACTION_SCORES));
        view.findViewById(R.id.btnOptions).setOnClickListener(v -> runAnimation(ACTION_OPTIONS));
        view.findViewById(R.id.btnHowToPlay).setOnClickListener(v -> runAnimation(ACTION_HOW_TO_PLAY));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(view == null) {
            return;
        }

        Animation logoAnimation = AnimationUtils.loadAnimation(context, R.anim.logo_first_time);
        logoAnimation.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {
                Animation fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
                fadeIn.setFillAfter(true);
                fadeIn.setStartOffset(300);

                view.findViewById(R.id.btnStart).startAnimation(fadeIn);
                view.findViewById(R.id.btnExit).startAnimation(fadeIn);
                view.findViewById(R.id.btnScore).startAnimation(fadeIn);
                view.findViewById(R.id.btnOptions).startAnimation(fadeIn);
                view.findViewById(R.id.btnHowToPlay).startAnimation(fadeIn);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.findViewById(R.id.logo).startAnimation(logoAnimation);
    }

    private void runAnimation(int action) {
        Animation startGameAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_down);
        Animation fadOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);

        fadOut.setFillAfter(true);
        fadOut.setStartOffset(300);

        fadOut.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.findViewById(R.id.logo).startAnimation(startGameAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        startGameAnimation.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onActionListener != null) {
                    onActionListener.onAction(action);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        view.findViewById(R.id.btnStart).startAnimation(fadOut);
        view.findViewById(R.id.btnExit).startAnimation(fadOut);
        view.findViewById(R.id.btnScore).startAnimation(fadOut);
        view.findViewById(R.id.btnOptions).startAnimation(fadOut);
        view.findViewById(R.id.btnHowToPlay).startAnimation(fadOut);
    }
}
