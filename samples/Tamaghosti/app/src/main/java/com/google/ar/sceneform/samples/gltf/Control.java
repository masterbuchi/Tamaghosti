package com.google.ar.sceneform.samples.gltf;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import androidx.cardview.widget.CardView;

public class Control {
    private ArActivity arActivity;

    private PersistenceManager pm;

    private Boolean happyAnimation;

    private Boolean meatActivated = false;

    // StatusBooleans
    private Boolean tired, hungry, shy, bored, fit, full, friendly, exited;
    private Boolean[] restrictions;


    private String dragonName;

    private ProgressBar prgHunger, prgEnergy, prgSocial, prgFun;

    private ImageButton showNeeds, hunger, energy, fun;

    private ImageView plus;
    private CardView card;

    private boolean needsShown = true;

    public Control(ArActivity arActivity) {
        this.arActivity = arActivity;

        pm = new PersistenceManager(arActivity.getApplicationContext());

        dragonName = pm.getString("dragon_name", null);

        energy = arActivity.findViewById(R.id.sleepControl);
        hunger = arActivity.findViewById(R.id.meatButton);
        fun = arActivity.findViewById(R.id.playControl);
        showNeeds = arActivity.findViewById(R.id.showNeeds);
        prgHunger = arActivity.findViewById(R.id.progressHunger);
        prgEnergy = arActivity.findViewById(R.id.progressEnergy);
        prgSocial = arActivity.findViewById(R.id.progressSocial);
        prgFun = arActivity.findViewById(R.id.progressFun);

        setButtonListeners();

        updateRestrictions();

        showHint("call");
    }




    public void updateRestrictions() {

        setBooleans();
        calculateRestrictions();
        setProcessBars();

        energy.setEnabled(restrictions[0]);
        hunger.setEnabled(restrictions[1]);
      if (arActivity.getDragon() != null)  arActivity.getDragon().setSocial(restrictions[2]);
        fun.setEnabled(restrictions[3]);

    }

    public Boolean[] getRestrictions() {
        return restrictions;
    }


    public void setBooleans() {

        // Energy
        if (pm.getInt("energy", 0) <= 20) {
            tired = true;
            fit = false;
        } else if (pm.getInt("energy", 0) >= 70) {
            tired = false;
            fit = true;
        } else {
            tired = false;
            fit = false;
        }
        //Hunger
        if (pm.getInt("hunger", 0) <= 20) {
            hungry = true;
            full = false;
        } else if (pm.getInt("hunger", 0) >= 90) {
            hungry = false;
            full = true;
        } else {
            hungry = false;
            full = false;
        }

        //Social
        if (pm.getInt("social", 0) <= 40) {
            shy = true;
            friendly = false;
        } else if (pm.getInt("social", 0) >= 90) {
            shy = false;
            friendly = true;
        } else {
            shy = false;
            friendly = false;
        }

        //Fun
        if (pm.getInt("fun", 0) <= 20) {
            bored = true;
            exited = false;
        } else if (pm.getInt("fun", 0) >= 80) {
            bored = false;
            exited = true;
        } else {
            bored = false;
            exited = false;
        }

    }

    public void calculateRestrictions() {

       restrictions = new Boolean[4];

        // Status
        if (!tired && full && friendly && exited) {

            happyAnimation = true;

            restrictions[0] = true;
            restrictions[1] = false;
            restrictions[2] = true;
            restrictions[3] = true;
        }
        else {
            happyAnimation = false;
            if (tired) {
                restrictions[0] = true;
                restrictions[1] = false;
                restrictions[2] = false;
                restrictions[3] = false;
            } else {
                if (hungry) {
                    restrictions[0] = true;
                    restrictions[1] = true;
                    restrictions[2] = false;
                    restrictions[3] = false;
                } else if (full) {
                    if (shy) {
                        restrictions[0] = true;
                        restrictions[1] = false;
                        restrictions[2] = true;
                        restrictions[3] = false;
                    } else {
                        restrictions[0] = true;
                        restrictions[1] = false;
                        restrictions[2] = true;
                        restrictions[3] = true;
                    }
                } else {
                    if (shy) {
                        restrictions[0] = true;
                        restrictions[1] = true;
                        restrictions[2] = true;
                        restrictions[3] = false;
                    } else {
                        restrictions[0] = true;
                        restrictions[1] = true;
                        restrictions[2] = true;
                        restrictions[3] = true;
                    }
                }
            }

        }

    }

    public void setButtonListeners() {


        card = arActivity.findViewById(R.id.cardViewNeeds);

        showNeeds.setOnClickListener(v -> {
            if (needsShown) {
                needsShown = false;
                card.setVisibility(View.INVISIBLE);
            } else {
                needsShown = true;
                card.setVisibility(View.VISIBLE);
            }
        });

        hunger.setOnClickListener(v -> {

                if (!arActivity.getDragon().moving) {


                    Node meatNode = arActivity.getMeatNode();

                    this.meatActivated = !this.meatActivated;

                    if (meatActivated) {

                        if (meatNode == null) arActivity.createMeat();
                        else {
                            meatNode.setParent(arActivity.getArFragment().getArSceneView().getScene().getCamera());
                            meatNode.setLocalRotation(new Quaternion(0, 180, 250, 0));
                            meatNode.setLocalPosition(new Vector3(0, -0.3f, -1));
                            meatNode.setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));
                            meatNode.setEnabled(true);
                        }
                    }
                    else {
                        meatNode.setEnabled(false);
                    }

                }

        });

        energy.setOnClickListener(v -> {

            Intent intent = new Intent(arActivity, SleepActivity.class);
            arActivity.startActivityForResult(intent, 1);

        });

        fun.setOnClickListener(v -> {
                // Play Method STILL MISSING

                // Value Change
                setNeed("fun",10);
                setNeed("hunger",-5);
                setNeed("energy",-5);
                showPlus(3000);
        });
    }




    @SuppressLint("SetTextI18n")
    void updateCurrentDragonPositionWindow() {

        Vector3 dragonPosition = arActivity.getDragon().getWorldPosition();
        TextView textView = arActivity.findViewById(R.id.modelPosition);
        textView.setText("");
        textView.setText(dragonPosition.x + "\n" + dragonPosition.y + "\n" + dragonPosition.z);
    }

    Boolean getHappyAnimation() {
        return happyAnimation;
    }

    public void createDragon() {
        updateRestrictions();
        showHint("welcome");
        updateCurrentDragonPositionWindow();
    }


    public void showPlus(int duration) {
        plus = arActivity.findViewById(R.id.plusImage);
        plus.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(() -> plus.setVisibility(View.INVISIBLE), duration);
    }

    public void setProcessBars() {
        prgHunger.setProgress(pm.getInt("hunger",0));
        prgEnergy.setProgress(pm.getInt("energy",0));
        prgSocial.setProgress(pm.getInt("social",0));
        prgFun.setProgress(pm.getInt("fun",0));
    }


    public void startThread(float duration) {

        Dragon dragon = arActivity.getDragon();
        float d = duration + dragon.getAnimationDuration() * 1000;
        int e = (int) d;
        ExampleRunnable runnable = new ExampleRunnable(e);
        new Thread(runnable).start();
    }

    class ExampleRunnable implements Runnable {
        int milliseconds;

        ExampleRunnable(int seconds) {
            this.milliseconds = seconds;
        }

        @Override
        public void run() {

            Log.d("ANIMATION", "new thread count: " + milliseconds);

            hunger.post(() -> {


                hunger.setEnabled(false);
            });
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            arActivity.runOnUiThread(() -> {

                Dragon dragon = arActivity.getDragon();
                FirebaseManager firebaseManager = arActivity.getFirebaseManager();
                Node meatNode = arActivity.getMeatNode();
                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.IDLE);

                showPlus(2000);
                meatActivated = false;

                // Value Change
                setNeed("hunger",20);
                setNeed("energy",-5);


                dragon.updateAnimation(dragon.idle_index);
                meatNode.setEnabled(false);
            });
        }
    }

    public void showHint(String value) {
        //hint is Checked text view and can disappear when checked. not implemented yet

        switch (value) {
            case "call":
                ToasterLong.makeLongToast(arActivity, "Call " + dragonName + " by tapping on plane.", 5000);
                break;
            case "feed":

                ToasterLong.makeLongToast(arActivity, dragonName + " is hungry, select meat to feed.", 8000);
                break;
            case "sleep":

                ToasterLong.makeLongToast(arActivity, dragonName + " seems too tired.", 8000);
                break;
            case "pet":

                ToasterLong.makeLongToast(arActivity, dragonName + " seems sad, show some love and pet your dragon.", 8000);
                break;
            case "play":

                ToasterLong.makeLongToast(arActivity, dragonName + " needs some fun.", 8000);
                break;

            case "welcome":

                ToasterLong.makeLongToast(arActivity, "Welcome to Dragon Care. Check what " + dragonName + " currently needs.", 9000);
            default:
                ;
                break;
        }

    }

    public void setNeed(String key, int value) {

        if (pm.getInt(key, 0) + value < 0) pm.saveInt(key, 0);
        else if (pm.getInt(key, 0) + value > 100) pm.saveInt(key, 100);
        else pm.saveInt(key, pm.getInt(key, 0) + value);

        updateRestrictions();

    }

    public int getNeed(String key) {
        return pm.getInt(key, 0);
    }

    public Boolean getMeatActivated() {
        return meatActivated;
    }

}
