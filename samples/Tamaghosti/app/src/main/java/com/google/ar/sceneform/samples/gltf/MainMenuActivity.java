package com.google.ar.sceneform.samples.gltf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        PersistenceManager persistenceManager = new PersistenceManager(getApplicationContext());

        // Mode Private: Nur die App kann auf die Daten zugreifen
        //SharedPreferences preferences = getApplicationContext().getSharedPreferences("preferences", MODE_PRIVATE);

        //SharedPreferences.Editor editor = preferences.edit();


        // Def-value: Value to return if this preference does not exist
        boolean firstStart = persistenceManager.getBoolean("first_start", true);

        if (!firstStart) {

            Button continueGameButton = findViewById(R.id.continueGameButton);

            continueGameButton.setVisibility(View.VISIBLE);

            continueGameButton.setOnClickListener(view -> {

                Toast.makeText(getApplicationContext(), "Tapped", Toast.LENGTH_SHORT);

                // Load
                Intent intent = new Intent(MainMenuActivity.this, ArActivity.class);

                intent.putExtra("hungerValue", persistenceManager.getInt("hunger", 0));
                intent.putExtra("sleepValue", persistenceManager.getInt("sleep", 0));
                intent.putExtra("socialValue", persistenceManager.getInt("social", 0));
                intent.putExtra("trainingValue", persistenceManager.getInt("training", 0));

                startActivity(intent);

            });

        }


        // Start Game OnClick Logic
        Button startGameButton = findViewById(R.id.newGameButton);
        startGameButton.setOnClickListener(view -> {

            Intent intent = new Intent(MainMenuActivity.this, StoryActivity.class);
            startActivity(intent);
        });

        // Settings OnClick Logic
        Button settingsButton = findViewById(R.id.settingsGameButton);
        settingsButton.setOnClickListener(view -> {

            Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
            startActivity(intent);

        });

    }

}
