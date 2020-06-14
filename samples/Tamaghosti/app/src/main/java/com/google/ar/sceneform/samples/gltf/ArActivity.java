/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.gltf;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.lang.ref.WeakReference;

/**
 * This is a example activity that uses the Sceneform UX package to make common AR tasks easier.
 */


public class ArActivity extends AppCompatActivity {


    private enum AppAnchorState {
        HOSTED,
        HOSTING,
        NONE
    }

    private static final String TAG = ArActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    //OnSwipeTouchListener onSwipeTouchListener;

    Dragon dragon;
    Node meatNode;
    ProgressBar prgHunger;
    ProgressBar prgEnergy;
    ProgressBar prgSocial;
    ProgressBar prgFun;

    ImageButton meat;
    ImageButton sleep;

    ImageButton play;
    ImageButton needsShow;
    ImageView plus;

    CardView card;


    NeedsController needsControl;
    ToasterLong t;

    private boolean needsShown = true;
    private String mDragonName;


    private AppAnchorState appAnchorState = AppAnchorState.NONE;
    private Anchor anchor;

    private SharedPreferences.Editor editor;

    private FirebaseManager firebaseManager;

    private boolean dragonSet = false;
    private ArFragment arFragment;
    private Renderable renderable;
    private Renderable meatRenderable;


    //volatile == immer aktuellsten wert, nicht cache
    private volatile boolean stopThread = false;

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        firebaseManager = new FirebaseManager();

        needsControl = new NeedsController(getApplicationContext());
        t = new ToasterLong(getApplicationContext());

        // Cloud Anchor on same device
        // Cloud Anchor auf dem selben Gerät
        /*SharedPreferences prefs = getSharedPreferences("AnchorId", MODE_PRIVATE);
        editor = prefs.edit();


         */

        PersistenceManager persistenceManager = new PersistenceManager(getApplicationContext());

        mDragonName = persistenceManager.getString("dragon_name", null);


        Log.d("SleepOverviewDebug", "current sleepValue1 " + needsControl.getEnergy());

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);
        setNeeds();

        hintControl(0);

        setButtonListeners();


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);


        WeakReference<ArActivity> weakActivity = new WeakReference<>(this);


        ModelRenderable.builder()
                .setSource(
                        this, R.raw.meat)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            ArActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.meatRenderable = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            showToast("while loading an error occurred.");

                            return null;
                        });


        ModelRenderable.builder()
                .setSource(
                        this, R.raw.dragon66_one)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            ArActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.renderable = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            showToast("while loading an error occurred.");

                            return null;
                        });


        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (renderable == null) {
                        showToast("model failed to load");
                        return;
                    }

                    if (dragon == null) {

                        createDragon(hitResult);

                        // Create a standing dragon
                        firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.IDLE);


                    } else {

                        if (!dragon.moving) {
                            // Moving the dragon
                            // Create the Anchor.
                            //AnchorNode moveToNode = createAnchor(hitResult);

                            anchor = arFragment.getArSceneView().getSession() != null ? arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor()) : null;
                            appAnchorState = AppAnchorState.HOSTING;

                            AnchorNode moveToNode = new AnchorNode(anchor);
                            moveToNode.setParent(arFragment.getArSceneView().getScene());


                            Vector3 rotationVect = new Vector3().subtract(moveToNode.getWorldPosition(), dragon.getWorldPosition());

                            double distance = Math.sqrt(Math.pow(dragon.getWorldPosition().x - moveToNode.getWorldPosition().x, 2) + Math.pow(dragon.getWorldPosition().y - moveToNode.getWorldPosition().y, 2) + Math.pow(dragon.getWorldPosition().z - moveToNode.getWorldPosition().z, 2));

                            // showToast("Distance: " + distance);
                            // Upload distance to Firebase
                            firebaseManager.uploadDistance(distance);

                            showToast("Distance: " + distance);


                            long time = dragon.moveTo(moveToNode, distance);

                            dragon.rotateDragon(rotationVect);


                            if (meatNode != null && dragon.getEating()) {
                                meatAnimation(hitResult, time);

                                Log.d("Meat", " getWorldPosition: " + meatNode.getWorldPosition());


                                if ((needsControl.getHunger() <= 90)) {

                                    //needsControl.feed();

                                    needsControl.makeDragonEat();


                                    setNeeds();
                                    showPlus();
                                    if (dragon != null) {

                                        // Notify Database!
                                        firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.RESET);
                                        firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.EAT);


                                        // if certain duration needed:

                                        startThread((float) time);
                                    }
                                }


                            }

                            //     showToast("Time: " + time);
                        }
                    }
                });


        // Cloud Anchor Update Loop
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {

            CheckIfUploaded(anchor, appAnchorState);

        });

    }

    private void CheckIfUploaded(Anchor anchor, AppAnchorState state) {

        if (state != AppAnchorState.HOSTING) {
            return;
        }

        Anchor.CloudAnchorState cloudAnchorState = anchor.getCloudAnchorState();

        if (cloudAnchorState.isError()) {

            showToast(cloudAnchorState.toString());

        } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {

            String anchorId = anchor.getCloudAnchorId();

            // Upload Cloud Anchor
            firebaseManager.uploadAnchor(anchorId);


            appAnchorState = AppAnchorState.HOSTED;

            showToast("Anchor hosted sucessfully. Cloud Anchor Id: " + anchorId);

        }

    }


    private void createMeat() {


        //create a new TranformableNode that will carry our object
        meatNode = new Node();
        meatNode.setParent(arFragment.getArSceneView().getScene().getCamera());
        meatNode.setRenderable(meatRenderable);


        meatNode.setLocalRotation(new Quaternion(0, 180, 250, 0));
        meatNode.setLocalPosition(new Vector3(0, -0.3f, -1));
        meatNode.setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));


    }

    private void meatAnimation(HitResult hitResult, long time) {

        Vector3 cameraPosition = meatNode.getWorldPosition();


        AnchorNode anchorNode = createAnchor(hitResult);
        Vector3 newPosition = anchorNode.getWorldPosition();


        // calculate curve
        Vector3 directionVector = new Vector3().subtract(newPosition, cameraPosition);

        float distance = directionVector.length();


        int steps = 200;
        float v_y_0 = 2f;
        float pos_y_0 = cameraPosition.y;
        float d_t = 1f / steps * time;

        float pos_y = 0;

        Log.d("Meat", " time: " + time);

        Log.d("Meat", " d_t: " + d_t);

        Log.d("Meat", " cameraPosition: " + cameraPosition);

        Log.d("Meat", " newPosition: " + newPosition);

        Log.d("Meat", " directionVector: " + directionVector);

        Log.d("Meat", " getParentWorldPosition: " + meatNode.getParent().getWorldPosition());


        Vector3[] positions = new Vector3[steps];

        Vector3 currentPos = cameraPosition;
        float currentPosY = 0;

        float x = 0;
        float y = 0;

        // 20 Steps for smooth curve
        for (int i = 0; i < steps; i++) {


            pos_y = -0.5f * 2 * i * d_t * i * d_t + v_y_0 * (float) Math.sin(45) * i * d_t + pos_y_0;

            x = x + 0.01f;

            if (i < 100) currentPosY = currentPos.y + directionVector.y / (float) steps + 0.01f;
            else currentPosY = currentPos.y + directionVector.y / (float) steps - 0.01f;


            currentPos = new Vector3(currentPos.x + directionVector.x / (float) steps, currentPosY, currentPos.z + directionVector.z / (float) steps);

            // currentPos = new Vector3().add(currentPos, directionVector.scaled(1 / 200f));

            positions[i] = currentPos;

            // Log.d("Meat", " currentPos: " +  currentPos + " i: " + i);
            // Log.d("Meat", " directionVector.scaled(1/200f): " +  directionVector.scaled(1/200f) + " i: " + i);


        }

        meatNode.setParent(anchorNode);

        meatNode.setWorldPosition(cameraPosition);


        Log.d("Meat", " localPos: " + meatNode.getLocalPosition());
        Log.d("Meat", " getWorldPosition: " + meatNode.getWorldPosition());

        Log.d("Meat", " getParentWorldPosition: " + meatNode.getParent().getWorldPosition());


        meatNode.setLocalRotation(new Quaternion(0, 180, 180, 0));


        Log.d("Meat", " getWorldPositionafterLocal: " + meatNode.getWorldPosition());

        meatNode.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));

        Log.d("Meat", " getWorldPositionafterscale: " + meatNode.getWorldPosition());

        ObjectAnimator objectAnimation = new ObjectAnimator();
        objectAnimation.setAutoCancel(true);
        objectAnimation.setTarget(meatNode);
        objectAnimation.setObjectValues(positions);
        objectAnimation.setPropertyName("WorldPosition");
        // The Vector3Evaluator is used to evaluator 2 vector3 and return the next
        // vector3.  The default is to use lerp.
        objectAnimation.setEvaluator(new Vector3Evaluator());
        // This makes the animation linear (smooth and uniform).
        objectAnimation.setInterpolator(new LinearInterpolator());

        // Duration in ms of the animation.
        objectAnimation.setDuration(time - 1000);
        objectAnimation.start();


        objectAnimation.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                meatNode.setLocalPosition(new Vector3(0, 0.05f, 0));

            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {

            }
        });

    }


    private void createDragon(HitResult hitResult) {
       hintControl(20);
        meat.setEnabled(true);
        sleep.setEnabled(true);

        play.setEnabled(false);
        play.setHovered(true);


        // showToast(mDragonName + " woke up.");

        //This function is not called, after this is set to true
        dragonSet = true;

        // Create the Anchor.
        AnchorNode anchorNode = createAnchor(hitResult);
        // Create the transformable model and add it to the anchorNode.
        dragon = new Dragon(arFragment, anchorNode, renderable);
        //Update current Position window of Dragon
        updateCurrentDragonPositionWindow();

        Log.d("Firebase", "DragonPosition " + dragon.getWorldPosition());

        Log.d("Firebase", "DragonRotation " + dragon.getWorldRotation());


    }


    private void setButtonListeners() {


        meat = findViewById(R.id.meatButton);
        needsShow = findViewById(R.id.needsShow);
        sleep = findViewById(R.id.sleepControl);

        play = findViewById(R.id.playControl);
        meat.setEnabled(false);
        sleep.setEnabled(false);

        play.setEnabled(false);


        card = findViewById(R.id.cardViewNeeds);


        needsShow.setOnClickListener(v -> {
            if (needsShown) {
                needsShown = false;
                card.setVisibility(View.INVISIBLE);
            } else {
                needsShown = true;
                card.setVisibility(View.VISIBLE);
            }
        });


        //Eat animation, hunger + 10, sleep -10
        meat.setOnClickListener(v -> {

            if (needsControl.getEnergy() > 10) {

                if (!dragon.moving) {
                    dragon.setEating(true);

                    if (meatNode == null) createMeat();
                    else {
                        meatNode.setParent(arFragment.getArSceneView().getScene().getCamera());
                        meatNode.setLocalRotation(new Quaternion(0, 180, 250, 0));
                        meatNode.setLocalPosition(new Vector3(0, -0.3f, -1));
                        meatNode.setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));
                        meatNode.setEnabled(true);
                    }
                }
            }else {
                hintControl(2);
            }

        });


        sleep.setOnClickListener(v -> {

            /*
            Log.d("Firebase", "DragonPosition " + dragon.getWorldPosition());

            Log.d("Firebase", "DragonRotation " + dragon.getWorldRotation());


             */


            Intent intent = new Intent(ArActivity.this, SleepActivity.class);

            startActivity(intent);
        });


        if (needsControl.getHunger() >= 50 && needsControl.getEnergy() >= 50 && needsControl.getSocial() == 50) {
            play.setEnabled(true);
        }

        play.setOnClickListener(v -> {
            if (needsControl.getFun() <= 90) {
                //needsControl.train();

                needsControl.trainDragon();

                setNeeds();
                showPlus();
            }
            Log.d("SocialDebug", "pressed " + needsControl.getFun());
        });
    }

    // Create Cloud Anchor
    private AnchorNode createAnchor(HitResult hitResult) {
        anchor = arFragment.getArSceneView().getSession() != null ? arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor()) : null;
        appAnchorState = AppAnchorState.HOSTING;

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        return anchorNode;
    }


    @SuppressLint("SetTextI18n")
    void updateCurrentDragonPositionWindow() {
        Vector3 dragonPosition = dragon.getWorldPosition();
        TextView textView = findViewById(R.id.modelPosition);
        textView.setText("");
        textView.setText(dragonPosition.x + "\n" + dragonPosition.y + "\n" + dragonPosition.z);
    }


    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

    }

    public void hintControl(int value) {
        //hint is Checked text view and can disappear when checked. not implemented yet

        switch (value) {
            case 0:
                t.makeLongToast("Call " + mDragonName + " by tapping on plane.", 9000);


                break;
            case 1:

                t.makeLongToast(mDragonName + " is hungry, select meat to fee.d", 8000);
                break;
            case 2:

                t.makeLongToast(mDragonName + " seems too tired.", 8000);
                break;
            case 3:

                t.makeLongToast(mDragonName + " seems sad, show some love and ped your dragon.", 8000);
                break;
            case 4:

                t.makeLongToast(mDragonName + " needs some fun.", 8000);
                break;

            case 20:

                t.makeLongToast("Welcome to Dragon Care. Check what " + mDragonName + " currently needs.", 9000);
            default:
                ;
                break;
        }

    }

    public void showPlus() {
        plus = findViewById(R.id.plusImage);
        plus.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(() -> plus.setVisibility(View.INVISIBLE), 7000);
    }

    public void setNeeds() {
        prgHunger = findViewById(R.id.progressHunger);
        prgHunger.setProgress(needsControl.getHunger());
        prgEnergy = findViewById(R.id.progressEnergy);
        prgEnergy.setProgress(needsControl.getEnergy());
        prgSocial = findViewById(R.id.progressSocial);
        prgSocial.setProgress(needsControl.getSocial());
        prgFun = findViewById(R.id.progressFun);
        prgFun.setProgress(needsControl.getFun());
    }


    public void startThread(float duration) {
        stopThread = false;

        Log.d("Meat", "AnimationDuration " + dragon.getAnimationDuration());
        float d = duration + dragon.getAnimationDuration() * 1000;
        int e = (int) d;
        Log.d("ANIMATION", "duration in millis " + e);
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

            if (stopThread)
                return;
            Log.d("ANIMATION", "new thread count: " + milliseconds);

            meat.post(() -> {
                meat.setEnabled(false);
            });
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {

                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.IDLE);

                dragon.setEating(false);
                dragon.updateAnimation(dragon.idle_index);
                meatNode.setEnabled(false);
                meat.setEnabled(true);

                // Log.d("Meat", " getWorldPosition: " + meatNode.getWorldPosition());

            });
        }
    }


}




