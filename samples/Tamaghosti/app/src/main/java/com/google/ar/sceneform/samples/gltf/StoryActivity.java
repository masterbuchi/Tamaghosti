package com.google.ar.sceneform.samples.gltf;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class StoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        Button startGameButton = findViewById(R.id.storyButton);

        // Creating a persistence manager to define the start values of the dragon "need bars"
        PersistenceManager persistenceManager = new PersistenceManager(getApplicationContext());

        startGameButton.setOnClickListener(view -> {

            TextView nameField = findViewById(R.id.storyTextField);

            String name = nameField.getText().toString();

            // Check if the user did any inputs into the textfield
            if (name.length() == 0) {

                Context context = getApplicationContext();
                CharSequence text = "Don't forget to name your dragon!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();


            } else {

                // If the user entered a name, the persistenceManager will save the name and change the firstStart bool flag
                // to false, which will trigger that the continue button will appear the next time that we open the app

                persistenceManager.saveString("dragon_name", nameField.getText().toString());
                persistenceManager.saveBoolean("first_start", false);

                // Save for the first time and initializing values
                int hungerValue = 50;
                int energyValue = 70;
                int socialValue = 10;
                int funValue = 10;

                persistenceManager.saveInt("hunger", hungerValue);
                persistenceManager.saveInt("energy", energyValue);
                persistenceManager.saveInt("social", socialValue);
                persistenceManager.saveInt("fun", funValue);

                // Open ArActivity
                Intent intent = new Intent(StoryActivity.this, ArActivity.class);

                // When the player is pressing the start button, the game has officially started. This means that we
                // will also start our background logic with the code below. The so called Worker is getting triggered
                // every 15 minutes and it doesn't matter if the app is fully closed. It will stop working when the app is active

                WorkManager workManager = WorkManager.getInstance(getApplicationContext());

                PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SystemWorker.class, 15, TimeUnit.MINUTES).build();

                workManager.enqueueUniquePeriodicWork("Background Logic", ExistingPeriodicWorkPolicy.REPLACE, request);


                startActivity(intent);

            }

        });

    }
}
