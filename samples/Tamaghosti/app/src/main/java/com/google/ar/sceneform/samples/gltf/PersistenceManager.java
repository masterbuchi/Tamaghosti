package com.google.ar.sceneform.samples.gltf;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PersistenceManager {

    // Key Value System

    private Context context;

    private SharedPreferences preferences;

    private SharedPreferences.Editor editor;


    public PersistenceManager(Context context) {

        this.context = context;
        preferences  = context.getSharedPreferences("preferences", MODE_PRIVATE); // Data can only be accessed in the app
        editor = preferences.edit();

    }

    // Save Values on the local device

    public void saveString(String key, String value) {

        editor.putString(key, value);

        editor.apply();
    }

    public void saveInt(String key, int value) {

        editor.putInt(key, value);

        editor.apply();

    }

    public void saveBoolean(String key, boolean value) {

        editor.putBoolean(key, value);

        editor.apply();

    }

    public String getString(String key, String defaultValue) {

        return preferences.getString(key, defaultValue);

    }

    public boolean getBoolean(String key, boolean defaultValue) {

        return preferences.getBoolean(key, defaultValue);

    }

    public int getInt(String key, int defaultValue) {

        return preferences.getInt(key, defaultValue);

    }

}

