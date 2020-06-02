package com.google.ar.sceneform.samples.gltf;

import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        PersistenceManager persistenceManager = new PersistenceManager(getApplicationContext());


        // Def-value: Value to return if this preference does not exist
        boolean firstStart = persistenceManager.getBoolean("first_start", true);

        Log.i("TEST", String.valueOf(firstStart));

        if (!firstStart) {

            Button continueGameButton = findViewById(R.id.continueGameButton);

            continueGameButton.setVisibility(View.VISIBLE);

            continueGameButton.setOnClickListener(view -> {

                // Load
                Intent intent = new Intent(MainMenuActivity.this, ArActivity.class);

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

        // Spectator OnClick Logic
        Button spectatorButton = findViewById(R.id.spectatorGameButton);
        spectatorButton.setOnClickListener(view -> {

            Intent intent = new Intent(MainMenuActivity.this, SpectatorActivity.class);
            startActivity(intent);
        });

    }

}
