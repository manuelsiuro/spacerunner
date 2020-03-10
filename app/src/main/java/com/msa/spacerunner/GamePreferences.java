package com.msa.spacerunner;

import android.content.Context;

public class GamePreferences {

    private static final String SETTINGS_SHARED_PREFERENCE_FILENAME = "SettingsSharedPreferences";
    private static final String MUSIC_DISABLED = "musicDisabled";
    private static final String SOUND_DISABLED = "soundDisabled";

    private static final String SHIP_COLOR = "shipColor";

    // Music
    public static boolean isMusicDisabled(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCE_FILENAME, Context.MODE_PRIVATE).getBoolean(MUSIC_DISABLED, false);
    }

    public static void setMusicDisabled(Context context, boolean allowed){
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCE_FILENAME, Context.MODE_PRIVATE).edit().putBoolean(MUSIC_DISABLED, allowed).apply();
    }

    // Sound
    public static boolean isSoundDisabled(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCE_FILENAME, Context.MODE_PRIVATE).getBoolean(SOUND_DISABLED, false);
    }

    public static void setSoundDisabled(Context context, boolean allowed){
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCE_FILENAME, Context.MODE_PRIVATE).edit().putBoolean(SOUND_DISABLED, allowed).apply();
    }

    // Ship color
    public static void setShipColor(Context context, String color) {
        context.getSharedPreferences(SETTINGS_SHARED_PREFERENCE_FILENAME, Context.MODE_PRIVATE).edit().putString(SHIP_COLOR, color).apply();
    }

    public static String getShipColor(Context context) {
        return context.getSharedPreferences(SETTINGS_SHARED_PREFERENCE_FILENAME, Context.MODE_PRIVATE).getString(SHIP_COLOR, "#01579B");
    }
}
