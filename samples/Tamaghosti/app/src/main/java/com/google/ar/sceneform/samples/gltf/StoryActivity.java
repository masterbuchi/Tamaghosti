package com.google.ar.sceneform.samples.gltf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class StoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        Button startGameButton = findViewById(R.id.storyButton);


        startGameButton.setOnClickListener(view -> {

            // Name
            TextView nameField = findViewById(R.id.storyTextField);

            String name = nameField.getText().toString();

            // Input abfangen
            if(name.length() == 0) {

                Context context = getApplicationContext();
                CharSequence text = "Don't forget to name your dragon!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();


            } else {

                // Save name

                SharedPreferences preferences = getApplicationContext().getSharedPreferences("preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("dragon_name", nameField.getText().toString());

                // Change first start to false -> Continue Button will be available next start
                editor.putBoolean("first_start", false);

                editor.commit();



                // Open GLTFActivity
                Intent intent = new Intent(StoryActivity.this, GltfActivity.class);
               intent.putExtra("hungerValue", 50);
                intent.putExtra("sleepValue", 50);
                intent.putExtra("socialValue", 10);
                intent.putExtra("trainingValue", 0);
                startActivity(intent);

            }

        });

    }
}
