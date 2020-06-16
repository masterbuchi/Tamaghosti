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
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
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

    Control control;

    Dragon dragon;
    Node meatNode;

    private boolean needsShown = true;


    private AppAnchorState appAnchorState = AppAnchorState.NONE;
    private Anchor anchor;

    private SharedPreferences.Editor editor;

    private FirebaseManager firebaseManager;

    private boolean dragonSet = false;
    private ArFragment arFragment;
    private Renderable dragonRenderableOne, dragonRenderableTwo, meatRenderable;


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

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);

        control = new Control(this, Control.User.CREATER);

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
                                activity.dragonRenderableOne = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            showToast("while loading an error occurred.");

                            return null;
                        });

        ModelRenderable.builder()
                .setSource(
                        this, R.raw.dragon65_two)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            ArActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.dragonRenderableTwo = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            showToast("while loading an error occurred.");

                            return null;
                        });


        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (dragonRenderableOne == null) {
                        showToast("model failed to load");
                        return;
                    }

                    //First touch on Plane
                    if (dragon == null) {
                        createDragon(hitResult);

                        // Spectator: Create a standing dragon
                        firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.IDLE);

                        // If Dragon exists
                    } else {

                        // If no Moving Animation
                        if (!dragon.moving) {

                            long time = moveDragon(hitResult);


                            if (control.getMeatActivated()) {

                                meatAnimation(hitResult, time);
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.RESET);
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.EAT);


                                // Thread with walking and Eating duration, set to IDLE afterwards
                                control.startThread((float) time);

                            }

                        }
                    }
                });


        // Cloud Anchor Update Loop
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {

            CheckIfUploaded(anchor, appAnchorState);

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_CANCELED) {
             control.updateRestrictions();
             control.setProcessBars();
            }
        }
    }//onActivityResult

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


    long moveDragon(HitResult hitResult) {
        // GEHT DAS OHNE DEN ANCHOR ÜBER DIE HITPOSITION
        // New CloudAnchor


        //AnchorNode moveToNode = createAnchor(hitResult);

        AnchorNode moveToNode = new AnchorNode(hitResult.createAnchor());

        // Lets give it a shot



        // Upload World Position
        firebaseManager.uploadMovePosition(moveToNode.getWorldPosition());

        /*anchor = arFragment.getArSceneView().getSession() != null ? arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor()) : null;
        // New Node on CloudAnchor
        AnchorNode moveToNode = new AnchorNode(anchor);
        // Set Parent of Node
        moveToNode.setParent(arFragment.getArSceneView().getScene());
        appAnchorState = AppAnchorState.HOSTING;*/

        Vector3 rotationVect = new Vector3().subtract(moveToNode.getWorldPosition(), dragon.getWorldPosition());
        double distance = Math.sqrt(Math.pow(dragon.getWorldPosition().x - moveToNode.getWorldPosition().x, 2) + Math.pow(dragon.getWorldPosition().y - moveToNode.getWorldPosition().y, 2) + Math.pow(dragon.getWorldPosition().z - moveToNode.getWorldPosition().z, 2));

        // showToast("Distance: " + distance);
        // Upload distance to Firebase
        firebaseManager.uploadDistance(distance);

        showToast("Distance: " + distance);


        long time = dragon.moveTo(moveToNode.getWorldPosition(), distance);

        dragon.rotateDragon(rotationVect);

        return time;


    }


    void createMeat() {


        //create a new TranformableNode that will carry our object
        meatNode = new Node();
        meatNode.setParent(arFragment.getArSceneView().getScene().getCamera());
        meatNode.setRenderable(meatRenderable);


        meatNode.setLocalRotation(new Quaternion(0, 180, 250, 0));
        meatNode.setLocalPosition(new Vector3(0, -0.3f, -1));
        meatNode.setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));


    }

    void meatAnimation(HitResult hitResult, long time) {

        Vector3 cameraPosition = meatNode.getWorldPosition();


        //AnchorNode anchorNode = createAnchor(hitResult);
        AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());

        Vector3 newPosition = anchorNode.getWorldPosition();


        // calculate curve
        Vector3 directionVector = new Vector3().subtract(newPosition, cameraPosition);

        float distance = directionVector.length();


        int steps = 200;
        float v_y_0 = 2f;
        float pos_y_0 = cameraPosition.y;
        float d_t = 1f / steps * time;

        float pos_y = 0;
        Vector3[] positions = new Vector3[steps];

        Vector3 currentPos = cameraPosition;
        float currentPosY = 0;

        float x = 0;
        float y = 0;

        // 200 Steps for smooth curve
        for (int i = 0; i < steps; i++) {
            pos_y = -0.5f * 2 * i * d_t * i * d_t + v_y_0 * (float) Math.sin(45) * i * d_t + pos_y_0;

            x = x + 0.01f;

            if (i < 100) currentPosY = currentPos.y + directionVector.y / (float) steps + 0.01f;
            else currentPosY = currentPos.y + directionVector.y / (float) steps - 0.01f;

            currentPos = new Vector3(currentPos.x + directionVector.x / (float) steps, currentPosY, currentPos.z + directionVector.z / (float) steps);

            positions[i] = currentPos;

        }

        meatNode.setParent(anchorNode);

        meatNode.setWorldPosition(cameraPosition);

        meatNode.setLocalRotation(new Quaternion(0, 180, 180, 0));

        meatNode.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));

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

    void createDragon(HitResult hitResult) {

        //This function is not called, after this is set to true
        dragonSet = true;

        // Create the Anchor.
        AnchorNode anchorNode = createAnchor(hitResult);
        // Create the transformable model and add it to the anchorNode.
        dragon = new Dragon(arFragment, anchorNode, dragonRenderableOne, dragonRenderableTwo, control);

        // Configuration for Main Window
        control.createDragon();

    }

// HIER


    // Create Cloud Anchor
    AnchorNode createAnchor(HitResult hitResult) {
        anchor = arFragment.getArSceneView().getSession() != null ? arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor()) : null;
        appAnchorState = AppAnchorState.HOSTING;

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        return anchorNode;
    }

    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public Dragon getDragon() {
        return dragon;
    }

    public Node getMeatNode() {
        return meatNode;
    }

    public FirebaseManager getFirebaseManager() {
        return firebaseManager;
    }

    public ArFragment getArFragment() {
        return arFragment;
    }
}




