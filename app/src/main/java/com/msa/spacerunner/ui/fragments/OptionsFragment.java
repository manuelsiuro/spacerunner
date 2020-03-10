package com.msa.spacerunner.ui.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.msa.spacerunner.GamePreferences;
import com.msa.spacerunner.R;
import com.msa.spacerunner.ui.activities.MainActivity;


public class OptionsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_options, container, false);

        ((SwitchCompat) view.findViewById(R.id.switch_sound)).setChecked(MainActivity.soundOff);
        ((SwitchCompat) view.findViewById(R.id.switch_music)).setChecked(MainActivity.musicOff);

        ((SwitchCompat) view.findViewById(R.id.switch_sound)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            GamePreferences.setSoundDisabled(view.getContext(), !MainActivity.soundOff);
            MainActivity.soundOff = !MainActivity.soundOff;
        });

        ((SwitchCompat) view.findViewById(R.id.switch_music)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            GamePreferences.setMusicDisabled(view.getContext(), !MainActivity.musicOff);
            MainActivity.musicOff = !MainActivity.musicOff;
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> OptionsFragment.this.getFragmentManager().popBackStack());

        setCurrentImageViewColor(view);

        view.findViewById(R.id.picker_color_1).setOnClickListener(v -> setCurrentShipColor(view, R.color.md_light_blue_900));
        view.findViewById(R.id.picker_color_2).setOnClickListener(v -> setCurrentShipColor(view, R.color.md_red_900));
        view.findViewById(R.id.picker_color_3).setOnClickListener(v -> setCurrentShipColor(view, R.color.md_green_900));
        view.findViewById(R.id.picker_color_4).setOnClickListener(v -> setCurrentShipColor(view, R.color.md_orange_900));
        view.findViewById(R.id.picker_color_5).setOnClickListener(v -> setCurrentShipColor(view, R.color.md_pink_900));

        return view;
    }

    private void setCurrentShipColor(View view, int resColor) {
        GamePreferences.setShipColor(view.getContext(), "#" + Integer.toHexString(ContextCompat.getColor(getActivity(), resColor)));
        setCurrentImageViewColor(view);
    }

    private void setCurrentImageViewColor(View view) {
        ImageView current_ship_color = view.findViewById(R.id.current_ship_color);
        int parsedColor = Color.parseColor(GamePreferences.getShipColor(view.getContext()));
        current_ship_color.setBackgroundColor(parsedColor);
    }
}
