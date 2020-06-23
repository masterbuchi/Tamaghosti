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
 * This is an activity that lets you spawn a dragon and play with it.
 * It uses the Sceneform UX package to make common AR tasks easier and is based on the sceneform-example HelloAR of the version 1.15
 */


public class ArActivity extends AppCompatActivity {

    // Used for checking the Status of the Hosting on Firebase
    private enum AppAnchorState {
        HOSTED,
        HOSTING,
        NONE
    }

    // Needed for Checking current OPENGL-version
    private static final String TAG = ArActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    // Control object, responsible for controlling the communication between the dragon and the other objects
    Control control;

    // No Anchor hosted
    private AppAnchorState appAnchorState = AppAnchorState.NONE;

    // Main Anchor of the Activity
    private Anchor anchor;

    // FirebaseManager, responsible for sending data to the Spectator
    private FirebaseManager firebaseManager;

    // Main Fragment of the Activity, an object similar to a view in Android
    private ArFragment arFragment;

    // The Renderables of the objects in the game
    private Renderable dragonRenderableOne, dragonRenderableTwo, meatRenderable, ballRenderable;


    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //FirebaseManager for managing the Communication between Creator and Spectator
        firebaseManager = new FirebaseManager();

        // Version checking
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        // Necessary for initialising the Scene
        setContentView(R.layout.activity_ux);

        //Create the Control-Object, that handles the movement of the Dragon, the objects and the Needs. Set User to Creator
        control = new Control(this, Control.User.CREATOR);

        /*
         Implements AR Required ArFragment. The Settings (like ENVIRONMENTAL_HDR-Lighting, Cloud-Anchor-Mode and Updatemode are set in the class
         We changed a few of the settings like the Lighting to get realistic lighting
         */
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // Only using WeakReferences to avoid memory leaks
        WeakReference<ArActivity> weakActivity = new WeakReference<>(this);

        // Render all the objects
        renderer(R.raw.meat, "meat", weakActivity);
        renderer(R.raw.ball, "ball", weakActivity);
        renderer(R.raw.dragon66_one, "dragon_one", weakActivity);
        renderer(R.raw.dragon65_two, "dragon_two", weakActivity);

        // When the detected plane is hit with the finger (only when nothing is in the way)
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {

                    // No Dragonmodel was loaded
                    if (dragonRenderableOne == null) {
                        showToast("model failed to load");
                        return;
                    }

                    //First touch on Plane
                    if (control.getDragon() == null) {

                        // forwards the creation of the dragon to the control
                        control.createDragon(hitResult, dragonRenderableOne, dragonRenderableTwo);

                        // Spectator: Create a standing dragon
                        firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.IDLE);

                        // If Dragon exists
                    } else {

                        // Always: if a ball exists, set the render to null (deactivate its Visibility)
                        // this deactivates the ball, when another movement, like meat or normal movement is being used
                        if (control.getBall() != null) control.getBall().setRenderable(null);

                        // If no Moving Animation
                        if (!control.getDragon().moving && !control.getBallBackActivated()) {

                            // if Meat is activated
                            if (control.getMeatActivated()) {
                                // move Dragon to clicked Hitpoint
                                float time = control.moveDragon(hitResult);
                                // Spectator: Move Dragon to Hitpoint
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.RESET);
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.WALK);

                                // Animate Meat Throw
                                control.throwMeat(hitResult, time);
                                // Spectator: Start Meat-throwAnimation
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.RESET);
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.THROW_MEAT);

                            } else if (control.getBallActivated()) {
                                //Animate Ball Throw
                                control.animateBall(hitResult, ballRenderable);
                                // Spectator: Start Ball-throwAnimation
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.RESET);
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.THROW_BALL);

                            } else {
                                // move Dragon to clicked Hitpoint
                                control.moveDragon(hitResult);
                                // Spectator: Move Dragon to Hitpoint
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.RESET);
                                firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.WALK);
                            }

                        }
                    }
                });


        // Cloud Anchor Update Loop
        // Checking if the Anchor is hosted
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> CheckIfUploaded(anchor, appAnchorState));
    }

    /**
     * @param id
     * @param renderable
     * @param weakActivity
     */
    public void renderer(int id, String renderable, WeakReference<ArActivity> weakActivity) {

        /**
         * ARCORE Model Renderer
         * Can be used to import GLTF and GLB files, which is the reason why we used this older version of 1.15
         */
        ModelRenderable.builder()
                .setSource(
                        this, id)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            ArActivity activity = weakActivity.get();
                            if (activity != null) {
                                //saving code, one Method for all renderables
                                switch (renderable) {
                                    case "meat":
                                        activity.meatRenderable = modelRenderable;
                                        break;
                                    case "ball":
                                        activity.ballRenderable = modelRenderable;
                                        break;
                                    case "dragon_one":
                                        activity.dragonRenderableOne = modelRenderable;
                                        break;
                                    case "dragon_two":
                                        activity.dragonRenderableTwo = modelRenderable;
                                        break;
                                }
                            }
                        })
                .exceptionally(
                        throwable -> {
                            showToast("while loading an error occurred.");
                            return null;
                        });
    }

    // When the Sleepingactivity ends, this function is called
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            // this updates the view of the needs after the sleeping session ended
            if (resultCode == Activity.RESULT_CANCELED) {
                control.updateRestrictions();
                control.setProcessBars();
            }
        }
    }

    /**
     * This method checks the status of the anchorUpload
     * If the Upload is finished and the cloudAnchor is set, it shows a message and sets the state to HOSTED
     *
     * @param anchor
     * @param state
     */
    private void CheckIfUploaded(Anchor anchor, AppAnchorState state) {

        // If the Hosting hasn't started yet
        if (state != AppAnchorState.HOSTING) {
            return;
        }

        // get the current status of the cloudAnchor
        Anchor.CloudAnchorState cloudAnchorState = anchor.getCloudAnchorState();

        // show possible error
        if (cloudAnchorState.isError()) {

            showToast(cloudAnchorState.toString());

            // if upload is finished
        } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {

            String anchorId = anchor.getCloudAnchorId();

            // Upload Cloud Anchor
            // Spectator: Sets current AnchorID
            firebaseManager.uploadAnchor(anchorId);
            appAnchorState = AppAnchorState.HOSTED;
            showToast("Anchor hosted sucessfully. Cloud Anchor Id: " + anchorId);
        }
    }


    /**
     * This method creates an anchor and starts the Hosting process
     * @param hitResult
     * @return
     */
    AnchorNode createAnchor(HitResult hitResult) {

        // Main Anchor, that is used in the Activity and send to the Spectator
        anchor = arFragment.getArSceneView().getSession() != null ? arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor()) : null;
        // Start Hosting Process
        appAnchorState = AppAnchorState.HOSTING;
        // Create a node on the created anchor
        AnchorNode anchorNode = new AnchorNode(anchor);
        // set the Scene as a parent of the anchor
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        return anchorNode;
    }

    /**
     * @author ARCORE
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
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

    /**
     * Custom ShowToast-method for shorter Toast-lines
     * @param s
     */
    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * Getter Firebase
     * @return
     */
    public FirebaseManager getFirebaseManager() {
        return firebaseManager;
    }

    /**
     * Getter ArFragment
     * @return
     */
    public ArFragment getArFragment() {
        return arFragment;
    }

    /**
     * Getter MeatRenderable
     * @return
     */
    public Renderable getMeatRenderable() {
        return meatRenderable;
    }

    /**
     * Getter BallRenderable
     * @return
     */
    public Renderable getBallRenderable() {
        return ballRenderable;
    }

}




