package com.google.ar.sceneform.samples.gltf;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;

import androidx.cardview.widget.CardView;

public class Control {

    // Parent Activity (Either Normal, oder Spectator)
    private ArActivity arActivity;
    private SpectatorActivity spectatorActivity;

    private PersistenceManager pm;

    // Booleans for some States of meat, ball etc.
    private Boolean happyAnimation = false;
    private Boolean meatActivated = false;
    private Boolean ballActivated = false;
    private Boolean ballBackActivated = false;
    private Boolean needsShown = true;

    // StatusBooleans
    private Boolean tired, hungry, shy, bored, fit, full, friendly, exited;

    // A simple "What is allowed?"-Array with four Booleans for hunger, sleep, fun and social
    private Boolean[] restrictions;

    // The objects of the game
    private Dragon dragon;
    private Meat meat;
    private Ball ball;

    private String dragonName;

    // Object on the Screen
    private ProgressBar prgHunger, prgEnergy, prgSocial, prgFun;
    private ImageButton showNeeds, hunger, energy, fun;
    private ImageView plus;
    private CardView card;
    private CardView items;
    private User user;

    /**
     * Constructor for the Creator
     * The Spectator hast a different constructor
     *
     * @param arActivity
     * @param user
     */
    public Control(ArActivity arActivity, User user) {
        this.arActivity = arActivity;

        // Get the PersistenceManager of the app
        pm = new PersistenceManager(arActivity.getApplicationContext());

        // Get the name of the dragon
        dragonName = pm.getString("dragon_name", null);

        // Get the References to the Objects on the screen
        energy = arActivity.findViewById(R.id.sleepControl);
        hunger = arActivity.findViewById(R.id.meatButton);
        fun = arActivity.findViewById(R.id.playControl);
        showNeeds = arActivity.findViewById(R.id.showNeeds);
        prgHunger = arActivity.findViewById(R.id.progressHunger);
        prgEnergy = arActivity.findViewById(R.id.progressEnergy);
        prgSocial = arActivity.findViewById(R.id.progressSocial);
        prgFun = arActivity.findViewById(R.id.progressFun);

        // Set User to Creator
        this.user = user;

        // Set all the Button Listeners
        setButtonListeners();

        // Set restrictions to a four Boolean-Array
        restrictions = new Boolean[4];

        // Sets the Activation of the Need-Buttons, that are allowed with the current stats of the dragon
        updateRestrictions();
        showHint("call");
    }

    /**
     * Constructor for the Spectator
     *
     * @param spectatorActivity
     * @param user
     */
    public Control(SpectatorActivity spectatorActivity, User user) {

        this.spectatorActivity = spectatorActivity;
        // Get the PersistenceManager of the app
        pm = new PersistenceManager(spectatorActivity.getApplicationContext());

        // Get the name of the dragon (why though?)
        dragonName = pm.getString("dragon_name", null);

        // Set User to Spectator
        this.user = user;


    }

    /**
     * Update the Restrictions, meaning what Need-Button is allowed to be pressed
     * That means for example, if the dragon is not friendly enough or is hungry, he can not play with you
     * So you can not press the Ball button
     */
    public void updateRestrictions() {

        // Set the Booleans of the needs, calculated by the values of the needs
        setBooleans();

        // With the Booleans set the four parameters of the needs
        calculateRestrictions();

        // Update the values of the needs
        setProcessBars();

        // Set the Button-Activations with the current restrictions
        energy.setEnabled(restrictions[0]);
        hunger.setEnabled(restrictions[1]);
        if (dragon != null) dragon.setSocial(restrictions[2]);
        fun.setEnabled(restrictions[3]);


    }

    /**
     * Set the Booleans of the needs, calculated by the values of the needs
     */
    public void setBooleans() {

        // Energy
        // if Energy too low
        if (pm.getInt("energy", 0) <= 20) {
            tired = true;
            fit = false;
            // if Energy very high
        } else if (pm.getInt("energy", 0) >= 70) {
            tired = false;
            fit = true;
        } else {
            tired = false;
            fit = false;
        }
        //Hunger
        // if Hunger too low
        if (pm.getInt("hunger", 0) <= 20) {
            hungry = true;
            full = false;
            // if Hunger very high
        } else if (pm.getInt("hunger", 0) >= 90) {
            hungry = false;
            full = true;
        } else {
            hungry = false;
            full = false;
        }

        //Social
        // if Social too low
        if (pm.getInt("social", 0) <= 40) {
            shy = true;
            friendly = false;
            // if Social very high
        } else if (pm.getInt("social", 0) >= 90) {
            shy = false;
            friendly = true;
        } else {
            shy = false;
            friendly = false;
        }

        //Fun
        // if Fun too low
        if (pm.getInt("fun", 0) <= 20) {
            bored = true;
            exited = false;
            // if Fun very high
        } else if (pm.getInt("fun", 0) >= 80) {
            bored = false;
            exited = true;
        } else {
            bored = false;
            exited = false;
        }

    }

    /**
     * With the Booleans set the four parameters of the needs
     */
    public void calculateRestrictions() {

        // Status 2 Other Renderable and other Animations
        if (!tired && full && friendly && exited) {

            // Boolean to get the information what Renderable is needed
            happyAnimation = true;

            // Example: Petting, Playing and Sleep is allowed, Feeding is not, because the dragon is full
            restrictions[0] = true;
            restrictions[1] = false;
            restrictions[2] = true;
            restrictions[3] = true;
            // Status 1 "Normal" Animations
        } else {
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

    /**
     * Set the Button Listeners, including
     */
    public void setButtonListeners() {
        items = arActivity.findViewById(R.id.Items);
        card = arActivity.findViewById(R.id.cardViewNeeds);

        // Move the Items and cards up and down to show or hide the needs
        showNeeds.setOnClickListener(v -> {
            if (needsShown) {
                needsShown = false;
                items.setTranslationY(240);
                card.setTranslationY(240);
            } else {
                needsShown = true;
                items.setTranslationY(0);
                card.setTranslationY(0);
            }
        });

        hunger.setOnClickListener(v -> {

            // If the Dragon exists and not moving
            if (dragon != null) {
                if (!dragon.moving) {

                    // Switching the Status of the MeatActivated
                    this.meatActivated = !this.meatActivated;

                    // If the Ball is activated, stop the animation and deactivate it
                    if (ballActivated) {
                        ball.stopAnimation();
                        ballActivated = false;
                    }

                    // If the Meat should be shown
                    if (meatActivated) {
                        // if the meat doesn't exist, create it
                        if (meat == null) meat = new Meat(arActivity.getArFragment(), arActivity.getMeatRenderable(), this);

                        // Set Meat to Camera, make it visible and activate Rotation
                        meat.setMeatToCamera();
                        meat.setEnabled(true);
                        meat.startAnimation();

                    } else {
                        // Stop the Animation and deactivate it
                        meat.stopAnimation();
                        meat.setEnabled(false);
                    }
                }
            }
        });

        energy.setOnClickListener(v -> {
            // If the Dragon exists and not moving
            if (dragon != null) {
                if (!dragon.moving) {
                    // Start Sleeping actvity with Result to update the need-values after sleeping
                    Intent intent = new Intent(arActivity, SleepActivity.class);
                    arActivity.startActivityForResult(intent, 1);
                }
            }
        });

        fun.setOnClickListener(v -> {
            // If the Dragon exists and not moving
            if (dragon != null) {
                if (!dragon.moving) {
                    // Switching the Status of the BallActivated
                    this.ballActivated = !this.ballActivated;

                    // If the Meat is activated, stop the animation and deactivate it
                    if (meatActivated) {
                        meat.stopAnimation();
                        meatActivated = false;
                    }

                    // If the Ball should be shown
                    if (ballActivated) {
                        // If the Ball doesn't exist, create it
                        if (ball == null) ball = new Ball(arActivity.getArFragment(), arActivity.getBallRenderable(), this);

                        // Set Renderable, set it to Camera and stop the "throwRotationAnimation"
                        ball.setRenderable(arActivity.getBallRenderable());
                        ball.setBallToCamera();
                        ball.setEnabled(true);
                        ball.startAnimation(false);

                    } else {
                        // Stop the Animation and deactivate it
                        ball.stopAnimation();
                        ball.setEnabled(false);
                    }
                }
            }
        });
    }

    /**
     * Method for Creating the Dragon with a different version for Creator and Spectator
     * @param hitResult
     * @param dragonRenderableOne
     * @param dragonRenderableTwo
     */
    public void createDragon(HitResult hitResult, Renderable dragonRenderableOne, Renderable dragonRenderableTwo) {

        // As a Spectator the resolved anchor is the needed Anchor
        if (user == User.SPECTATOR) {

            Anchor resolvedAnchor = spectatorActivity.getResolvedAnchor();
            AnchorNode anchorNode = new AnchorNode(resolvedAnchor);
            anchorNode.setParent(spectatorActivity.getArFragment().getArSceneView().getScene());
            dragon = new Dragon(spectatorActivity.getArFragment(), anchorNode, dragonRenderableOne, dragonRenderableTwo, this);
        } else {
            // As a Creator create the Dragon and update the Position of the Dragon in Firebase
            // Also update the Restrictions
            AnchorNode anchorNode = arActivity.createAnchor(hitResult);
            dragon = new Dragon(arActivity.getArFragment(), anchorNode, dragonRenderableOne, dragonRenderableTwo, this);
            updatePositions(anchorNode.getWorldPosition());
            updateRestrictions();


            showHint("welcome");
        }


    }

    /**
     * Creator Version to calculate the Vectors for the Movement
     * @param hitResult
     * @return
     */
    float moveDragon(HitResult hitResult) {

        // Create Node, DirectionVector and Distance
        AnchorNode moveToNode = new AnchorNode(hitResult.createAnchor());
        Vector3 dragonPosition = dragon.getWorldPosition();
        Vector3 rotationVect = new Vector3().subtract(moveToNode.getWorldPosition(), dragonPosition);
        double distance = rotationVect.length();

        // Upload new Position
        updatePositions(moveToNode.getWorldPosition());

        // Move the Dragon
        float time = dragon.moveTo(moveToNode.getWorldPosition(), distance, rotationVect);

        return time;
    }

    /**
     * Send new Position to FirebaseCloud
     * @param newPosition
     */
    void updatePositions(Vector3 newPosition) {
        arActivity.getFirebaseManager().uploadUpdatePosition(dragon.getWorldPosition(), newPosition, arActivity.getArFragment().getArSceneView().getScene().getCamera().getWorldPosition());
    }

    /**
     *  Spectator Version to calculate the Vectors for the Movement
     * @param position
     * @return
     */
    long moveDragon(Vector3 position) {

        // Create Node, DirectionVector and Distance
        Vector3 dragonPosition = dragon.getWorldPosition();
        Vector3 rotationVect = new Vector3().subtract(position, dragonPosition);
        double distance = rotationVect.length();

        // Move the Dragon
        long time = dragon.moveTo(position, distance, rotationVect);
        return time;
    }

    /**
     * Method for ThrowMeatAnimation from ArActivity
     * @param hitResult
     * @param time
     */
    public void throwMeat(HitResult hitResult, float time) {
        meat.meatThrowAnimation(hitResult, time);
        startThread(time);
    }

    /**
     *  Method for BallAnimation from ArActivity
     * @param hitResult
     * @param ballRenderable
     */
    public void animateBall(HitResult hitResult, Renderable ballRenderable) {
        ball.ballAnimation(hitResult);
        ball.setRenderable(ballRenderable);
    }

    /**
     * Show the Plus, when the Dragon gets a plus for the needs
     * @param duration
     */
    public void showPlus(int duration) {
        plus = arActivity.findViewById(R.id.plusImage);
        Log.d("Plus", String.valueOf(plus));
        plus.setVisibility(View.VISIBLE);
        // after a set Duration deactivate the Plus again
        Handler handler = new Handler();
        handler.postDelayed(() -> plus.setVisibility(View.INVISIBLE), duration);
    }

    /**
     * Set the current needs in ProcessBars
     */
    public void setProcessBars() {
        prgHunger.setProgress(pm.getInt("hunger", 0));
        prgEnergy.setProgress(pm.getInt("energy", 0));
        prgSocial.setProgress(pm.getInt("social", 0));
        prgFun.setProgress(pm.getInt("fun", 0));
    }

    /**
     * Start Thread that activates different Animations after a set duration
     * @param duration
     */
    public void startThread(float duration) {

        float d = duration + dragon.getAnimationDuration() * 1000;
        int e = (int) d;
        EatingAndBallRunnable runnable = new EatingAndBallRunnable(e);
        new Thread(runnable).start();
    }

    /**
     * Show Longer Messages then normal Toasts
     * @param value
     */
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

    /**
     * Update the Need in the Persistence with a new value, capped at 0 and 100
     * @param key
     * @param value
     */
    public void setNeed(String key, int value) {

        if (pm.getInt(key, 0) + value < 0) pm.saveInt(key, 0);
        else if (pm.getInt(key, 0) + value > 100) pm.saveInt(key, 100);
        else pm.saveInt(key, pm.getInt(key, 0) + value);

        updateRestrictions();

    }

    /**
     * Getter MeatActivated
     * @return
     */
    public Boolean getMeatActivated() {
        return meatActivated;
    }

    /**
     * Setter MeatActivated
     * @param meatActivated
     */
    public void setMeatActivated(Boolean meatActivated) {
        this.meatActivated = meatActivated;
    }

    /**
     * Getter BallActivated
     * @return
     */
    public Boolean getBallActivated() {
        return ballActivated;
    }

    /**
     * Setter BallActivated
     * @param ballActivated
     */
    public void setBallActivated(Boolean ballActivated) {
        this.ballActivated = ballActivated;
    }

    /**
     * Getter BallBackActivated
     * @return
     */
    public Boolean getBallBackActivated() {
        return ballBackActivated;
    }

    /**
     * Setter BallBackActivated
     * @param ballBackActivated
     */
    public void setBallBackActivated(Boolean ballBackActivated) {
        this.ballBackActivated = ballBackActivated;
    }

    /**
     * Getter Dragon
     * @return
     */
    public Dragon getDragon() {

        return dragon;
    }

    /**
     * Getter Meat
     * @return
     */
    public Meat getMeat() {
        return meat;
    }

    /**
     * Setter Meat
     * @param meat
     */
    public void setMeat(Meat meat) {
        this.meat = meat;
    }

    /**
     * Getter Ball
     * @return
     */
    public Ball getBall() {
        return ball;
    }

    /**
     * Setter Ball
     * @param ball
     */
    public void setBall(Ball ball) {
        this.ball = ball;
    }

    /**
     * Getter HappyAnimation
     * @return
     */
    Boolean getHappyAnimation() {
        return happyAnimation;
    }

    /**
     * Getter User
     * @return
     */
    public User getUser() {
        return user;
    }

    /**
     * Getter CameraPosition, only for Spectator
     * @return
     */
    public Vector3 getCameraPosition() {
        if (user == User.SPECTATOR) return spectatorActivity.getCameraPosition();
        else return null;
    }

    /**
     * Two Types of Users, Creator (Main player) and Spectator (can only watch)
     */
    public enum User {

        CREATOR,
        SPECTATOR

    }

    /**
     * Runnable that sets Animations after duration, sleeps before that
     */
    class EatingAndBallRunnable implements Runnable {
        int milliseconds;

        EatingAndBallRunnable(int seconds) {
            this.milliseconds = seconds;
        }

        @Override
        public void run() {


            if (user == User.CREATOR) {
                // Deactivate the hunger-Button while this thread is active
                hunger.post(() -> {
                    hunger.setEnabled(false);
                });
            }


            try {
                // Set the Thread sleep
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // After the Thread is finished
            if (user == User.CREATOR) {
                arActivity.runOnUiThread(() -> {
                    // With Meat Activated set Meat to null, update Needs and set Dragon to idle
                    if (meatActivated) {
                        showPlus(2000);
                        setNeed("hunger", 20);
                        setNeed("energy", -5);
                        meatActivated = false;
                        dragon.updateAnimation(dragon.idle_index);
                        meat.setRenderable(null);
                        dragon.moving = false;
                    }
                    // With Ball Activated, update Needs and let the Dragon bring the ball back
                    if (ballActivated) {
                        setNeed("fun", 10);
                        setNeed("hunger", -10);
                        setNeed("energy", -5);
                        showPlus(3000);
                        updateRestrictions();
                        dragon.bringBackBall();
                    }
                });

                // Spectator
            } else {
                spectatorActivity.runOnUiThread(() -> {

                    // Deactivate the meat, let Dragon Idle
                    if (meatActivated) {
                        meatActivated = false;
                        dragon.updateAnimation(dragon.idle_index);
                        meat.setRenderable(null);
                    }

                    // Let the Dragon bring the Ball back
                    if (ballActivated) {
                        dragon.bringBackBall();
                    }


                });

            }
        }
    }
}
