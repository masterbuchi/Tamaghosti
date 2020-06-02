package com.google.ar.sceneform.samples.gltf;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;


public class NeedsController {


    private PersistenceManager persistenceManager;

    private Context context;

    private int hunger;
    private int energy;
    private int social;
    private int training;

    public NeedsController(Context context) {

        this.context = context;

        persistenceManager = new PersistenceManager(context);



    }


    public void feed(){

        setHunger(10);

        persistenceManager.saveInt("hunger", hunger);

        Log.d("SleepDebug", "check Energy in feed " + energy);
        getTired();
    }

    public void getTired(){
        setEnergy(-10);

        persistenceManager.saveInt("sleep", energy);

        Log.d("SleepDebug", "check Energy in tired " + energy);
    }

    public void getHungry(){

        setHunger(-10);

        persistenceManager.saveInt("hunger", hunger);


    }

    public void pet(){
        setSocial(10);

        persistenceManager.saveInt("social", social);

        getTired();
        getHungry();
    }

    public void train(){

        setTraining(10);

        persistenceManager.saveInt("training", training);

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
        return  persistenceManager.getInt("sleep", 0);
    }

    public int getHunger() {
        return  persistenceManager.getInt("hunger", 0);
    }

    public int getSocial() {
        return  persistenceManager.getInt("social", 0);
    }

    public int getTraining() {
        return  persistenceManager.getInt("training", 0);
    }

}


