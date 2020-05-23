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

        setHunger(10);
        Log.d("SleepDebug", "check Energy in feed " + energy);
        getTired();
    }

    public void getTired(){
        setEnergy( - 10);
        Log.d("SleepDebug", "check Energy in tired " + energy);
    }

    public void getHungry(){
        setHunger( - 10);
    }

    public void pet(){
        setSocial( 10);
        getTired();
        getHungry();
    }

    public void train(){
        setTraining(10);
        getTired();
        getHungry();
    }

    public void setEnergy(int value) {
        if (this.energy>=10 || this.energy <=90){
        this.energy += value;
        }
    }

    public void setHunger(int value) {
        if (this.hunger>=10 || this.hunger <=90){
            this.hunger += value;
        }

    }

    public void setSocial(int value) {
        if (this.social>=10 || this.social <=90){
            this.social += value;
        }
    }

    public void setTraining(int value) {
        if (this.training>=10 || this.training <=90){
            this.training += value;
        }
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


