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

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;

import androidx.cardview.widget.CardView;

public class Control {
    private ArActivity arActivity;
    private SpectatorActivity spectatorActivity;

    private PersistenceManager pm;

    private Boolean happyAnimation = false;

    private Boolean meatActivated = false;
    private Boolean ballActivated = false;
    private Boolean ballBackActivated = false;

    // StatusBooleans
    private Boolean tired, hungry, shy, bored, fit, full, friendly, exited;
    private Boolean[] restrictions;

    Dragon dragon;
    Meat meat;
    Ball ball;

    private String dragonName;

    private ProgressBar prgHunger, prgEnergy, prgSocial, prgFun;

    private ImageButton showNeeds, hunger, energy, fun;

    private ImageView plus;
    private CardView card;
    private CardView items;
    private boolean needsShown = true;

    public enum User {

        CREATOR,
        SPECTATOR

    }

    User user;


    public Control(ArActivity arActivity, User user) {
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
        this.user = user;

        // Skip these steps for spectator mode


                setButtonListeners();
                updateRestrictions();

        showHint("call");
    }
    public Control(SpectatorActivity spectatorActivity, User user) {

        this.spectatorActivity = spectatorActivity;
        pm = new PersistenceManager(spectatorActivity.getApplicationContext());

        dragonName = pm.getString("dragon_name", null);

        this.user = user;


    }





    public void updateRestrictions() {

        setBooleans();
        calculateRestrictions();
        setProcessBars();

        energy.setEnabled(restrictions[0]);
        hunger.setEnabled(restrictions[1]);
      if (dragon != null)  dragon.setSocial(restrictions[2]);
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

        items=arActivity.findViewById(R.id.Items);
        card = arActivity.findViewById(R.id.cardViewNeeds);

        showNeeds.setOnClickListener(v -> {
            if (needsShown) {
                needsShown = false;
                items.setTranslationY(240);
                card.setTranslationY(240);
               //card.setVisibility(View.INVISIBLE);
            } else {
                needsShown = true;
                items.setTranslationY(0);
                card.setTranslationY(0);
               // card.setVisibility(View.VISIBLE);
            }
        });

        hunger.setOnClickListener(v -> {
            if (dragon != null) {
                if (!dragon.moving) {

                    this.meatActivated = !this.meatActivated; // always true?

                    if (ballActivated)  {
                        ball.stopAnimation();
                        ballActivated = false;
                    }

                    if (meatActivated) {

                        if (meat == null)
                            meat = new Meat(arActivity.getArFragment(), arActivity.getMeatRenderable(), this);

                        meat.setMeatToCamera();
                        meat.setEnabled(true);
                        meat.startAnimation();

                    } else {
                        meat.stopAnimation();
                        meat.setEnabled(false);

                    }

                }
            }

        });

        energy.setOnClickListener(v -> {

            if (dragon != null) {
                Intent intent = new Intent(arActivity, SleepActivity.class);
                arActivity.startActivityForResult(intent, 1);
            }

        });

        fun.setOnClickListener(v -> {

            if (dragon != null) {
                if (!dragon.moving) {

                    this.ballActivated = !this.ballActivated;

                    if (meatActivated) {
                        meat.stopAnimation();
                        meatActivated = false;
                    }


                    if (ballActivated) {

                        if (ball == null)
                            ball = new Ball(arActivity.getArFragment(), arActivity.getBallRenderable(), this);

                        ball.setBallToCamera();
                        ball.setEnabled(true);
                        ball.startAnimation(false);

                    } else {
                        ball.stopAnimation();
                        ball.setEnabled(false);
                    }

                }

                /*// Value Change
                setNeed("fun", 10);
                setNeed("hunger", -5);
                setNeed("energy", -5);
                showPlus(3000);*/

            }
        });
    }




    @SuppressLint("SetTextI18n")
    void updateCurrentDragonPositionWindow() {

        Vector3 dragonPosition = dragon.getWorldPosition();
        TextView textView = arActivity.findViewById(R.id.modelPosition);
        textView.setText("");
        textView.setText(dragonPosition.x + "\n" + dragonPosition.y + "\n" + dragonPosition.z);
    }

    Boolean getHappyAnimation() {
        return happyAnimation;
    }

    public void createDragon(HitResult hitResult, Renderable dragonRenderableOne, Renderable dragonRenderableTwo) {


        if (user == User.SPECTATOR) {

            Anchor resolvedAnchor = spectatorActivity.getResolvedAnchor();
            AnchorNode anchorNode = new AnchorNode(resolvedAnchor);
            anchorNode.setParent(spectatorActivity.getArFragment().getArSceneView().getScene());
            dragon = new Dragon(spectatorActivity.getArFragment(), anchorNode, dragonRenderableOne, dragonRenderableTwo, this);



        } else {

            AnchorNode anchorNode = arActivity.createAnchor(hitResult);

            dragon = new Dragon(arActivity.getArFragment(), anchorNode, dragonRenderableOne, dragonRenderableTwo, this);

            updatePositions(anchorNode.getWorldPosition());

            updateRestrictions();

            updateCurrentDragonPositionWindow();

            showHint("welcome");
        }



    }

    long moveDragon(HitResult hitResult) {

        AnchorNode moveToNode = new AnchorNode(hitResult.createAnchor());

        // Upload World Position

        updatePositions(moveToNode.getWorldPosition());


        Vector3 dragonPosition = dragon.getWorldPosition();
        Vector3 rotationVect = new Vector3().subtract(moveToNode.getWorldPosition(), dragonPosition);
        double distance = Math.sqrt(Math.pow(dragonPosition.x - moveToNode.getWorldPosition().x, 2) + Math.pow(dragonPosition.y - moveToNode.getWorldPosition().y, 2) + Math.pow(dragonPosition.z - moveToNode.getWorldPosition().z, 2));


        long time = dragon.moveTo(moveToNode.getWorldPosition(), distance, rotationVect);
        return time;
    }

    void updatePositions(Vector3 newPosition) {

        arActivity.getFirebaseManager().uploadUpdatePosition(dragon.getWorldPosition(),newPosition,arActivity.getArFragment().getArSceneView().getScene().getCamera().getWorldPosition());
    }

    // Created for Spectator Activity
    long moveDragon(Vector3 position) {


        Vector3 dragonPosition = dragon.getWorldPosition();
        Vector3 rotationVect = new Vector3().subtract(position, dragonPosition);
        double distance = Math.sqrt(Math.pow(dragonPosition.x - position.x, 2) + Math.pow(dragonPosition.y - position.y, 2) + Math.pow(dragonPosition.z - position.z, 2));


        long time = dragon.moveTo(position, distance, rotationVect);
        return time;
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

            // Nullpointer exception

            if(user == User.CREATOR) {

                hunger.post(() -> {

                    hunger.setEnabled(false);
                });

            }


            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(user == User.CREATOR) {
            arActivity.runOnUiThread(() -> {


                FirebaseManager firebaseManager = arActivity.getFirebaseManager();
                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.IDLE);

                    showPlus(2000);
                    // Value Change
                    setNeed("hunger",20);
                    setNeed("energy",-5);

            if (meatActivated) {
                meatActivated = false;

                dragon.updateAnimation(dragon.idle_index);

                meat.setRenderable(null);
            }
            if (ballActivated) {
                dragon.bringBackBall();
            }
            });
            } else {

                spectatorActivity.runOnUiThread(() -> {

                    spectatorActivity.getFirebaseManager().uploadAnimationState(FirebaseManager.AnimationState.IDLE);

                    if (meatActivated) {
                        meatActivated = false;

                        dragon.updateAnimation(dragon.idle_index);

                        meat.setRenderable(null);
                    }
                    if (ballActivated) {
                        dragon.bringBackBall();
                    }
                });

            }
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

    public void setMeatActivated(Boolean meatActivated) {
        this.meatActivated = meatActivated;
    }

    public Boolean getBallActivated() {
        return ballActivated;
    }

    public void setBallBackActivated(Boolean ballBackActivated) {
        this.ballBackActivated = ballBackActivated;
    }

    public Boolean getBallBackActivated() {
        return ballBackActivated;
    }

    public void setBallActivated(Boolean ballActivated) {
        this.ballActivated = ballActivated;
    }

    public Dragon getDragon() {
        return dragon;
    }

    public Meat getMeat() {
        return meat;
    }

    public void setMeat(Meat meat) {
        this.meat = meat;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
    }

    public Ball getBall() {
        return ball;
    }
}
