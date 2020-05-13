package com.google.ar.sceneform.samples.gltf;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;


public class SleepActivity extends AppCompatActivity {
    private int energy;
    ProgressBar prgEnergy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        Intent in = getIntent();
        final int hValue = in.getIntExtra("hungerValue", 0);
        final int slValue = in.getIntExtra("sleepValue", 0);
        final int soValue = in.getIntExtra("socialValue", 0);
        final int tValue = in.getIntExtra("trainingValue", 0);
        Log.d("NeedsDebug", "sleep value: " + slValue);
        this.energy = slValue;
        prgEnergy = (ProgressBar) findViewById(R.id.progressEnergy2);
        prgEnergy.setProgress((int) slValue);

        Log.d("NeedsDebug", "energy first: " + energy);






        Handler handler1 = new Handler();
        for (int a = energy; a < 100; a++) {

            handler1.postDelayed(new Runnable() {

                @Override
                public void run() {
                    energy ++;
                    if (energy == 100){
                        handler1.removeCallbacksAndMessages(null);
                    }
                    prgEnergy.setProgress(energy);
                    Log.d("NeedsDebug", "energy inside: " + energy);
                }
            }, 1000*a);

            Log.d("NeedsDebug", "energy third: " + energy);

        }





        Button wakeUp = (Button) findViewById(R.id.wkaeUpControl);
        wakeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler1.removeCallbacksAndMessages(null);
                Intent intent = new Intent(SleepActivity.this, GltfActivity.class);
                intent.putExtra("hungerValue", hValue);
                intent.putExtra("sleepValue", energy);
                intent.putExtra("socialValue", soValue);
                intent.putExtra("trainingValue", tValue);
                startActivity(intent);
            }
        });

}



}


