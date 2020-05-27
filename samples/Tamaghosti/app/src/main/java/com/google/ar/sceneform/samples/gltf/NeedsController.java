package com.google.ar.sceneform.samples.gltf;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;


public class NeedsController {


    private int hunger;
    private int energy;
    private int social;
    private int training;

    private PersistenceManager persistenceManager;


    public void feed(Context context){

        setHunger(10);

        persistenceManager = new PersistenceManager(context);
        persistenceManager.saveInt("hunger", hunger);

        Log.d("SleepDebug", "check Energy in feed " + energy);
        getTired(context);
    }

    public void getTired(Context context){
        setEnergy( - 10);

        persistenceManager = new PersistenceManager(context);
        persistenceManager.saveInt("sleep", energy);

        Log.d("SleepDebug", "check Energy in tired " + energy);
    }

    public void getHungry(Context context){

        setHunger( - 10);

        persistenceManager = new PersistenceManager(context);
        persistenceManager.saveInt("hunger", hunger);


    }

    public void pet(Context context){
        setSocial( 10);

        persistenceManager = new PersistenceManager(context);
        persistenceManager.saveInt("social", social);

        getTired(context);
        getHungry(context);
    }

    public void train(Context context){

        setTraining(10);

        persistenceManager = new PersistenceManager(context);
        persistenceManager.saveInt("training", training);

        getTired(context);
        getHungry(context);
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


