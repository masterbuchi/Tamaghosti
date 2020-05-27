package com.google.ar.sceneform.samples.gltf;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
                int sleepValue = 50;
                int socialValue = 10;
                int trainingValue = 0;

                persistenceManager.saveInt("hunger", hungerValue);
                persistenceManager.saveInt("sleep", sleepValue);
                persistenceManager.saveInt("social", socialValue);
                persistenceManager.saveInt("training", trainingValue);

                // Open GLTFActivity
                Intent intent = new Intent(StoryActivity.this, ArActivity.class);
                intent.putExtra("hungerValue", hungerValue);
                intent.putExtra("sleepValue", sleepValue);
                intent.putExtra("socialValue", socialValue);
                intent.putExtra("trainingValue", trainingValue);

                startActivity(intent);

            }

        });

    }
}
