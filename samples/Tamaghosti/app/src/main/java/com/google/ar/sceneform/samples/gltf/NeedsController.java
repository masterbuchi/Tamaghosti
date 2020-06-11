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


    public void makeDragonEat(){

        hunger = getHunger();

        //setValue(hunger, 10);

        setHunger(10);

        persistenceManager.saveInt("hunger", hunger);

        Log.d("SleepDebug", "check Energy in feed " + energy);

        makeDragonTired();
    }

    public void makeDragonTired(){

        energy = getEnergy();

        //setValue(energy, -10);

        setEnergy(-10);

        persistenceManager.saveInt("sleep", energy);

        Log.d("SleepDebug", "check Energy in tired " + energy);
    }

    public void makeDragonHungry(){

        hunger = getHunger();

        //setValue(hunger, -10);

        setHunger(-10);

        persistenceManager.saveInt("hunger", hunger);


    }

    public void petDragon(){

        social = getSocial();

        //setValue(social, 10);

        setSocial(10);

        persistenceManager.saveInt("social", social);

        makeDragonTired();
        makeDragonHungry();
    }

    public void trainDragon(){

        //setValue(training, 10);

        setTraining(10);

        persistenceManager.saveInt("training", training);

        makeDragonTired();
        makeDragonHungry();
    }

    public void setEnergy(int value) {
        if (energy > 0 && energy < 100 ){

            if(energy + value <= 0) {

                energy = 0;

            } else if(energy + value >= 100) {

                energy = 100;

            } else {
                energy += value;
            }

        }
    }

    public void setHunger(int value) {

        if (hunger > 0 && hunger < 100 ){

            if(hunger + value <= 0) {

                hunger = 0;

            } else if(hunger + value >= 100) {

                hunger = 100;

            } else {
                hunger += value;
            }

        }

    }

    public void setSocial(int value) {
        if (social > 0 && social < 100 ){

            if(social + value <= 0) {

                social = 0;

            } else if(social + value >= 100) {

                social = 100;

            } else {
                social += value;
            }

        }
    }

    public void setTraining(int value) {
        if (training > 0 && training < 100 ){

            if(training + value <= 0) {

                training = 0;

            } else if(training + value >= 100) {

                training = 100;

            } else {
                training += value;
            }

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


