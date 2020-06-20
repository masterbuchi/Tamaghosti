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

        PersistenceManager persistenceManager = new PersistenceManager(getApplicationContext());



        startGameButton.setOnClickListener(view -> {

            // Name
            TextView nameField = findViewById(R.id.storyTextField);

            String name = nameField.getText().toString();

            // Input abfangen
            if (name.length() == 0) {

                Context context = getApplicationContext();
                CharSequence text = "Don't forget to name your dragon!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();


            } else {

                persistenceManager.saveString("dragon_name", nameField.getText().toString());
                persistenceManager.saveBoolean("first_start", false);


                // Save for the first time
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

                WorkManager workManager = WorkManager.getInstance(getApplicationContext());

                PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SystemWorker.class, 15, TimeUnit.MINUTES)
                        .build();

                workManager.enqueueUniquePeriodicWork("Background Logic", ExistingPeriodicWorkPolicy.REPLACE, request);


                startActivity(intent);

            }

        });

    }
}
