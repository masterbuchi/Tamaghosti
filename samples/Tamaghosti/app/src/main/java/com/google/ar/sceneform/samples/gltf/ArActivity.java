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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
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
    ProgressBar prgHunger;
    ProgressBar prgEnergy;
    ProgressBar prgSocial;
    ProgressBar prgTraining;
    Button mainAction;
    Button sleep;
    Button social;
    Button training;
    ImageButton needsShow;
    ImageView plus;

    TextView lHunger;
    TextView lEnergy;
    TextView lSocial;
    TextView lTraining;
    CardView card;



    NeedsController needsControl;

    private boolean needsShown = true;
    private String mDragonName;


    private AppAnchorState appAnchorState = AppAnchorState.NONE;
    private Anchor anchor;

    private SharedPreferences.Editor editor;

    private FirebaseManager firebaseManager;

    private boolean dragonSet = false;
    private ArFragment arFragment;
    private Renderable renderable;

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


        // Cloud Anchor on same device
        // Cloud Anchor auf dem selben Gerät
        SharedPreferences prefs = getSharedPreferences("AnchorId", MODE_PRIVATE);
        editor = prefs.edit();


        PersistenceManager persistenceManager = new PersistenceManager(getApplicationContext());
        mDragonName = persistenceManager.getString("dragon_name", null);

        //int hValue = persistenceManager.getInt("hunger", 0);


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


                    } else {


                        // Moving the dragon
                        // Create the Anchor.
                        AnchorNode moveToNode = createAnchor(hitResult);


                        Vector3 rotationVect = new Vector3().subtract(moveToNode.getWorldPosition(), dragon.getWorldPosition());

                    //   Log.d("Rotation", "Dragon World Position: " + dragon.getWorldPosition());

                      //  Log.d("Rotation", "moveToNode.getWorldPosition(): " + moveToNode.getWorldPosition());

                     //   Log.d("Rotation", "rotationVect: " + rotationVect);

                        double distance = Math.sqrt(Math.pow(dragon.getWorldPosition().x - moveToNode.getWorldPosition().x, 2) + Math.pow(dragon.getWorldPosition().y - moveToNode.getWorldPosition().y, 2) + Math.pow(dragon.getWorldPosition().z - moveToNode.getWorldPosition().z, 2));

                       // showToast("Distance: " + distance);
                        double time = dragon.moveTo(moveToNode, distance);


                        dragon.rotateDragon(rotationVect);

                       //     showToast("Time: " + time);





                    }






                });


        // Cloud Anchor Sachen
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {


            if (appAnchorState != AppAnchorState.HOSTING)
                return;
            Anchor.CloudAnchorState cloudAnchorState = anchor.getCloudAnchorState();

            if (cloudAnchorState.isError()) {
                showToast(cloudAnchorState.toString());
            } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {


                appAnchorState = AppAnchorState.HOSTED;
                String anchorId = anchor.getCloudAnchorId();

                firebaseManager.uploadAnchorToDatabase(anchorId);

                editor.putString("anchorId", anchorId);
                editor.apply();
             //   showToast("Anchor hosted sucessfully. Anchor Id: " + anchorId);
            }

        });

    }

    private void createDragon(HitResult hitResult) {
        hintControl(20);
        mainAction.setEnabled(true);
        sleep.setEnabled(true);
        social.setEnabled(true);
        training.setEnabled(true);


       // showToast(mDragonName + " woke up.");

        //This function is not called, after this is set to true
        dragonSet = true;

        // Create the Anchor.
        AnchorNode anchorNode = createAnchor(hitResult);
        // Create the transformable model and add it to the anchorNode.
        dragon = new Dragon(arFragment, anchorNode, renderable);
        //Update current Position window of Dragon
        updateCurrentDragonPositionWindow();


    }


    private void setButtonListeners() {
        mainAction = findViewById(R.id.mainActionControl);
        needsShow =  findViewById(R.id.needsShow);
        sleep = findViewById(R.id.sleepControl);
        social = findViewById(R.id.socialControl);
        training = findViewById(R.id.trainingControl);
        mainAction.setEnabled(false);
        sleep.setEnabled(false);
        social.setEnabled(false);
        training.setEnabled(false);


        card = findViewById(R.id.cardViewNeeds);


        needsShow.setOnClickListener(v -> {
            if(needsShown){
                needsShown = false;
                card.setVisibility(View.INVISIBLE);
            }else{
                needsShown = true;
                card.setVisibility(View.VISIBLE);
            }
        });

        //Eat animation, hunger + 10, sleep -10
        mainAction.setOnClickListener(v -> {
            if (needsControl.getHunger() <= 90) {
                needsControl.feed();
                setNeeds();
                showPlus();
                if (dragon != null) {
                    //Change Animation mit Handler

                    dragon.updateAnimation(dragon.eat_index);

                    // if certain duration needed:
                    float duration = dragon.getFilamentAsset().getAnimator().getAnimationDuration(0);
                    startThread(duration);
                }
            }

        });



        sleep.setOnClickListener(v -> {


            Intent intent = new Intent(ArActivity.this, SleepActivity.class);
            intent.putExtra("hungerValue", needsControl.getHunger());
            intent.putExtra("sleepValue", needsControl.getEnergy());
            intent.putExtra("socialValue", needsControl.getSocial());
            intent.putExtra("trainingValue", needsControl.getTraining());
            startActivity(intent);
        });


        if (needsControl.getHunger() == 100 && needsControl.getEnergy() >= 80) {
            social.setVisibility(View.VISIBLE);
        }
        social.setOnClickListener(v -> {
            if (needsControl.getSocial() <= 90) {
                needsControl.pet();
                setNeeds();
                showPlus();
            }
            if (dragon != null) {
                dragon.updateAnimation(dragon.getPet_index);
            }

        });


        if (needsControl.getHunger() >= 80 && needsControl.getEnergy() >= 80 && needsControl.getSocial() == 100) {
            training.setVisibility(View.VISIBLE);
        }

        training.setOnClickListener(v -> {
            if (needsControl.getTraining() <= 90) {
                needsControl.train();
                setNeeds();
                showPlus();
            }
            Log.d("SocialDebug", "pressed " + needsControl.getTraining());
        });
    }


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
                showToast("Call " + mDragonName + " by tapping on plane");

                break;
            case 1:
                showToast(mDragonName + " is hungry, to feed him hit *ACTIONS*");

                break;
            case 2:
                showToast(mDragonName + " seems tired");

                break;
            case 3:
                showToast(mDragonName + " seems sad, give him some love");

                break;
            case 4:
                showToast(mDragonName + " needs some training");

                break;
            case 20:
                //Control by needs
                if (needsControl.getHunger() <= 50) {
                    hintControl(1);
                    break;
                } else if (needsControl.getEnergy() <= 20) {
                    hintControl(2);
                    break;
                } else if (needsControl.getSocial() <= 20) {
                    hintControl(3);
                    break;
                } else if (needsControl.getTraining() <= 20) {
                    hintControl(4);
                    break;
                }

            default:
                showToast("Care for " + mDragonName);
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
        prgTraining = findViewById(R.id.progressTraining);
        prgTraining.setProgress(needsControl.getTraining());
    }


    public void startThread(float duration) {
        stopThread = false;
        float d = duration * 1000;
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

            mainAction.post(() -> {
                mainAction.setText(R.string.eating);
                mainAction.setEnabled(false);
            });
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                dragon.updateAnimation(dragon.idle_index);
                mainAction.setText(R.string.eatAgain);
                mainAction.setEnabled(true);

            });
        }
    }


}






