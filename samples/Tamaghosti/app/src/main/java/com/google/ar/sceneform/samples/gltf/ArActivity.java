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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;


import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
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

    private AppAnchorState appAnchorState = AppAnchorState.NONE;
    private Anchor anchor;
    private FirebaseManager firebaseManager;

    private ArFragment arFragment;
    private Renderable dragonRenderableOne, dragonRenderableTwo, meatRenderable, ballRenderable;


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

        control = new Control(this, Control.User.CREATOR);

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
                        this, R.raw.ball)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            ArActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.ballRenderable = modelRenderable;
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
                    if (control.getDragon() == null) {
                        control.createDragon(hitResult, dragonRenderableOne, dragonRenderableTwo);

                        // Spectator: Create a standing dragon
                        firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.IDLE);

                        // If Dragon exists
                    } else {

                        // If no Moving Animation
                        if (!control.getDragon().moving) {

                            if (control.getMeatActivated()) {

                                long time = control.moveDragon(hitResult);

                                // Throw Animation
                                control.getMeat().meatThrowAnimation(hitResult, time);

                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.THROW_MEAT);

                                control.startThread((float) time);
                            } else if (control.getBallActivated()) {

                                control.getBall().ballAnimation(hitResult);

                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.THROW_BALL);


                                // Thread with walking and Eating duration, set to IDLE afterwards
                               // control.startThread((float) time);
                            } else {
                                long time = control.moveDragon(hitResult);
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


    public FirebaseManager getFirebaseManager() {
        return firebaseManager;
    }

    public ArFragment getArFragment() {
        return arFragment;
    }

    public Renderable getMeatRenderable() {
        return meatRenderable;
    }

    public Renderable getBallRenderable() {
        return ballRenderable;
    }
}




