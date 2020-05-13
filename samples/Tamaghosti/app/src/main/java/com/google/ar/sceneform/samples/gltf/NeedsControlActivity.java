package com.google.ar.sceneform.samples.gltf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;


public class NeedsControlActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    private int hunger;
    private int energy;
    private int social;
    private int training;


    public void feed(){

        this.hunger = this.hunger + 10;
        setHunger(this.hunger);
        Log.d("SleepDebug", "check Energy in feed " + energy);
        getTired();
    }
    public void getTired(){

        this.energy = this.energy - 10;
        setEnergy(this.energy);
        Log.d("SleepDebug", "check Energy in tired " + energy);
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;

    }

    public void setSocial(int social) {
        this.social = social;
    }

    public void setTraining(int training) {
        this.training = training;
    }

    public int getEnergy() {
        return energy;
    }

    public int getHunger() {
        return hunger;
    }

    public int getSocial() {
        return social;
    }

    public int getTraining() {
        return training;
    }

}


