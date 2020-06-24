package com.google.ar.sceneform.samples.gltf;


import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Every Second the Activity updated the Persistence by 1 Energy and Stops at 100 Energy.
 * You can return with the WakeUp-Button
 */
public class SleepActivity extends AppCompatActivity {

    private int energy;
    private ProgressBar prgEnergy;
    private volatile boolean stopThread = false;
    private TextView progress;

    private PersistenceManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        pm = new PersistenceManager(getApplicationContext());
        energy =  pm.getInt("energy",0);


        prgEnergy = findViewById(R.id.progressEnergy2);
        prgEnergy.setProgress(energy);
        progress = findViewById(R.id.sleepProgress);

        Button wakeUp = findViewById(R.id.wakeUpControl);
        wakeUp.setOnClickListener(v -> {
            stopThread(null);
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        });

        startThread(null);
    }
    public void startThread(View view){
        stopThread = false;
        ExampleRunnable runnable = new ExampleRunnable(energy);
        new Thread(runnable).start();
    }
    public void stopThread(View view) {
        stopThread = true;
    }
    class ExampleRunnable implements Runnable {
        int energy;
        ExampleRunnable(int energy) {
            this.energy = energy;
        }

        @Override
        public void run() {
            for (int a = energy; a <= 100; a++) {
                if (stopThread)
                    return;
                if (energy > 100)
                    return;
                runOnUiThread(() -> {
                    prgEnergy.setProgress(energy);
                    progress.setText(energy + "%");
                    energy ++;
                    pm.saveInt("energy", energy);
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



}


