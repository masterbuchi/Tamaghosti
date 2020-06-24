package com.google.ar.sceneform.samples.gltf;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        PersistenceManager persistenceManager = new PersistenceManager(getApplicationContext());

        /**
         * Def-value: Value to return if this preference does not exist
         */
        boolean firstStart = persistenceManager.getBoolean("first_start", true);

        // When the user has already played the game, then this flag will create set the visibility of the
        // continue button to visible. The "firstStart" variable is getting changed to true in the StoryActivity when
        // the Start button is pressed
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

        // Spectator OnClick Logic
        Button spectatorButton = findViewById(R.id.spectatorGameButton);
        spectatorButton.setOnClickListener(view -> {

            Intent intent = new Intent(MainMenuActivity.this, SpectatorActivity.class);
            startActivity(intent);
        });

    }

}
