package com.google.ar.sceneform.samples.gltf;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;


public class SleepActivity extends AppCompatActivity {
    private int energy;
    ProgressBar prgEnergy;
    private Calendar currentTime;
    public volatile int safeEnergy;
    private volatile boolean stopThread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        Intent in = getIntent();
        final int hValue = in.getIntExtra("hungerValue", 0);
        final int slValue = in.getIntExtra("sleepValue", 0);
        final int soValue = in.getIntExtra("socialValue", 0);
        final int tValue = in.getIntExtra("trainingValue", 0);
        Log.d("SLEEP", "sleep value: " + slValue);
        this.energy = slValue;
        prgEnergy = (ProgressBar) findViewById(R.id.progressEnergy2);
        prgEnergy.setProgress((int) slValue);

        Log.d("SLEEP", "energy first: " + energy);


        Handler handlerSleep = new Handler();

        currentTime = Calendar.getInstance();
        Calendar updateTime = currentTime;
        updateTime.add(Calendar.SECOND, 1);







        Button wakeUp = (Button) findViewById(R.id.wkaeUpControl);
        wakeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopThread(null);
                Log.d("SLEEP", "safeEnergy end: " + safeEnergy);
                Intent intent = new Intent(SleepActivity.this, GltfActivity.class);
                intent.putExtra("hungerValue", hValue);
                intent.putExtra("sleepValue", safeEnergy);
                intent.putExtra("socialValue", soValue);
                intent.putExtra("trainingValue", tValue);
                startActivity(intent);
            }
        });

        startThread(null);
    }
    public void startThread(View view){
        stopThread = false;
        ExampleRunnable runnable = new ExampleRunnable(energy);;
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
            for (int a = energy; a < 100; a++) {
                if (stopThread)
                    return;
                if (energy >= 100)
                    return;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        prgEnergy.setProgress(energy);
                        Log.d("SLEEP", "progress is growing: " + energy);
                        energy ++;
                        safeEnergy = energy;
                    }
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


