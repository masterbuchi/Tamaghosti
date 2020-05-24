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

import static java.util.concurrent.TimeUnit.SECONDS;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * This is a example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class GltfActivity extends AppCompatActivity {

    private static final String TAG = GltfActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final String MODEL_POSITION = "MODEL_POSITION";
    public static volatile int vIndex;
    private final Set<AnimationInstance> animators = new ArraySet<>();
    private final List<Color> colors =
            Arrays.asList(
                    new Color(0, 0, 0, 1),
                    new Color(1, 0, 0, 1),
                    new Color(0, 1, 0, 1),
                    new Color(0, 0, 1, 1),
                    new Color(1, 1, 0, 1),
                    new Color(0, 1, 1, 1),
                    new Color(1, 0, 1, 1),
                    new Color(1, 1, 1, 1));
    public volatile FilamentAsset filamentAsset;
    public int idle_index = 2;
    public int eat_index = 0;
    public int walk_index = 3;
    public int getPet_index = 1;
    TransformableNode model;
    ProgressBar prgHunger;
    ProgressBar prgEnergy;
    ProgressBar prgSocial;
    ProgressBar prgTraining;
    CheckedTextView hint;
    Button mainAction;
    Button sleep;
    Button social;
    Button training;
    ImageView plus;


    Context context;
    NeedsController needsControl = new NeedsController();

    private String mDragonName;
    private PersistenceManager persistenceManager;



    private AppAnchorState appAnchorState = AppAnchorState.NONE;
    private Anchor anchor;
    // Cloud Anchor auf dem selben Gerät
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private boolean modelSet = false;
    private int animationCount = 0;
    private ArFragment arFragment;
    private Renderable renderable;
    private Handler mainHandler = new Handler();
    //volatile == immer aktuellsten wert, nicht cache
    private volatile boolean stopThread = false;
    private int nextColor = 0;

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
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


        // Cloud Anchor on same device
        prefs = getSharedPreferences("AnchorId", MODE_PRIVATE);
        editor = prefs.edit();

        Intent in = getIntent();
        final int hValue = in.getIntExtra("hungerValue", 0);
        needsControl.setHunger(hValue);
        final int slValue = in.getIntExtra("sleepValue", 0);
        needsControl.setEnergy(slValue);
        final int soValue = in.getIntExtra("socialValue", 0);
        needsControl.setSocial(soValue);
        final int tValue = in.getIntExtra("trainingValue", 0);
        needsControl.setTraining(tValue);

        context = getApplicationContext();

        persistenceManager = new PersistenceManager(getApplicationContext());

        mDragonName = persistenceManager.getString("dragon_name", null);


        Log.d("SleepOverviewDebug", "current sleepValue1 " + needsControl.getEnergy());

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);
        setNeeds();


        hint = findViewById(R.id.hintView);
        hintControl(0);

        mainAction = (Button) findViewById(R.id.mainActionControl);
        sleep = (Button) findViewById(R.id.sleepControl);
        social = (Button) findViewById(R.id.socialControl);
        training = (Button) findViewById(R.id.trainingControl);
        mainAction.setEnabled(false);
        sleep.setEnabled(false);
        social.setEnabled(false);
        training.setEnabled(false);

        //Eat animation, hunger + 10, sleep -10
        mainAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (needsControl.getHunger() <= 90) {
                    needsControl.feed();
                    setNeeds();
                    showPlus();
                    if (model != null) {
                        //Change Animation mit Handler
                        ChangeAnimationMethod(eat_index);
                        // if certain duration needed:
                        float duration = filamentAsset.getAnimator().getAnimationDuration(eat_index);
                        startThread(null, duration);
                    }
                }

            }
        });

// if (model != null) noch nicht überall implementiert, um tests zu beschleunigen


        sleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SleepOverviewDebug", "current sleepValue2 " + needsControl.getEnergy());

                Intent intent = new Intent(GltfActivity.this, SleepActivity.class);
                intent.putExtra("hungerValue", needsControl.getHunger());
                intent.putExtra("sleepValue", needsControl.getEnergy());
                intent.putExtra("socialValue", needsControl.getSocial());
                intent.putExtra("trainingValue", needsControl.getTraining());
                startActivity(intent);
            }
        });


        if (needsControl.getHunger() == 100 && needsControl.getEnergy() >= 80) {
            social.setVisibility(View.VISIBLE);
        }
        social.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (needsControl.getSocial() <= 90) {
                    needsControl.pet();
                    setNeeds();
                    showPlus();
                }
                if (model != null) {
                    //Change Animation mit Handler
                    ChangeAnimationMethod(getPet_index);
                }
                Log.d("SocialDebug", "pressed " + needsControl.getSocial());
            }
        });


        if (needsControl.getHunger() >= 80 && needsControl.getEnergy() >= 80 && needsControl.getSocial() == 100) {
            training.setVisibility(View.VISIBLE);
        }

        training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (needsControl.getTraining() <= 90) {
                    needsControl.train();
                    setNeeds();
                    showPlus();
                }
                Log.d("SocialDebug", "pressed " + needsControl.getTraining());
            }
        });


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        WeakReference<GltfActivity> weakActivity = new WeakReference<>(this);

        // Winkemann-link:  //"https://drive.google.com/uc?export=download&id=1eidGtNQDjHZrFC-xQOtFoZgYu7OqMBfU"
        // Beispieldatei aus Googledrive fuer passendes Linkformat
        // https://drive.google.com/uc?export=download&id=

        ModelRenderable.builder()
                .setSource(
                        this, R.raw.dino)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            GltfActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.renderable = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            showToast("Drache konnte nicht geladen werden");

                            return null;
                        });




        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {

                    Toast.makeText(context, "Tapped", Toast.LENGTH_SHORT).show();

                    Log.i("Button: ", "CLICKED!");

                    if (renderable == null) {
                        showToast("Drachendatei ist nicht verfügbar");

                        return;
                    }

                    if (modelSet) {
                        showToast("Drache bereits gesetzt");
                        return;
                    }


                    Log.d("HINT", "hint control 20 should be called");
                    hintControl(20);
                    mainAction.setEnabled(true);
                    sleep.setEnabled(true);
                    social.setEnabled(true);
                    training.setEnabled(true);

                    showToast("Drache gesetzt. Hosting...");
                    modelSet = true;


                    // Create the Anchor.

                    anchor = arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor());
                    appAnchorState = AppAnchorState.HOSTING;

                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());


                    // Get Object Position

                    float[] modelPosition = anchor.getPose().getTranslation();

                    TextView textView = findViewById(R.id.modelPosition);


                    for (int i = 0; i < modelPosition.length; i++) {
                        //  Log.i(MODEL_POSITION, i + ": " + modelPosition[i]);
                        textView.setText(textView.getText() + "\n" + modelPosition[i]);
                    }

                    // Create the transformable model and add it to the anchor.
                    // Transformable makes it possible to scale and drag the model
                    model = new TransformableNode(arFragment.getTransformationSystem());
                    model.setParent(anchorNode);
                    model.setRenderable(renderable);
                    model.select();

                    // Oben deklariert FilamentAsset filamentAsset = model.getRenderableInstance().getFilamentAsset();
                    filamentAsset = model.getRenderableInstance().getFilamentAsset();
                    if (filamentAsset.getAnimator().getAnimationCount() > 3) {
                        animators.add(new AnimationInstance(filamentAsset.getAnimator(), idle_index, System.nanoTime()));
                    }

                    Color color = colors.get(nextColor);
                    nextColor++;
                    for (int i = 0; i < renderable.getSubmeshCount(); ++i) {
                        Material material = renderable.getMaterial(i);
                        material.setFloat4("baseColorFactor", color);
                    }
                    updateAnimation();
                    Log.d("DURATION", "just once huh : ");


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
                editor.putString("anchorId", anchorId);
                editor.apply();
                showToast("Anchor hosted sucessfully. Anchor Id: " + anchorId);
            }

        });

        Button resolve = findViewById(R.id.resolve);
        resolve.setOnClickListener(view -> {
            String anchorId = prefs.getString("anchorId","null");
            if (anchorId.equals("null")) {
                showToast("No anchor found");
                return;
            }

            Anchor resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);
            showToast("Hat geklappt");
        });

    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void hintControl(int value){
        //hint is Checked text view and can disappear when checked. not implememnted yet

        switch(value){
            case 0:
                showToast("Call your Dragon by tapping on plane");

                break;
            case 1:
                hint.setText(mDragonName + " is hungry, to feed him hit *ACTIONS*");

                break;
            case 2:
                hint.setText(mDragonName + " seems tired");

                break;
            case 3:
                hint.setText(mDragonName + " seems sad, give him some love");

                break;
            case 4:
                hint.setText(mDragonName + " needs some training");

                break;
            case 20:
                //Control by needs
                if (needsControl.getHunger() <= 50) {
                    hintControl(1); break;
                } else if (needsControl.getEnergy() <= 20) {
                    hintControl(2); break;
                } else if (needsControl.getSocial() <= 20) {
                    hintControl(3); break;
                } else if (needsControl.getTraining() <= 20) {
                    hintControl(4); break;
                }

                default:
                showToast("care for your dragon");
                break;
        }

    }

    public void showPlus() {
        plus = (ImageView) findViewById(R.id.plusImage);
        plus.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                plus.setVisibility(View.INVISIBLE);

            }
        }, 7000);
    }

    public void setNeeds() {
        prgHunger = (ProgressBar) findViewById(R.id.progressHunger);
        prgHunger.setProgress(needsControl.getHunger());
        prgEnergy = (ProgressBar) findViewById(R.id.progressEnergy);
        prgEnergy.setProgress(needsControl.getEnergy());
        prgSocial = (ProgressBar) findViewById(R.id.progressSocial);
        prgSocial.setProgress(needsControl.getSocial());
        prgTraining = (ProgressBar) findViewById(R.id.progressTraining);
        prgTraining.setProgress(needsControl.getTraining());
    }

    public void ChangeAnimationMethod(int index) {
        vIndex = index;
        if (filamentAsset.getAnimator().getAnimationCount() >= 3) {
            animators.add(new AnimationInstance(filamentAsset.getAnimator(), vIndex, System.nanoTime()));
            Log.d("ANIMATION", "Animation changed to : " + filamentAsset.getAnimator().getAnimationName(vIndex));
            //updateAnimation();

        }
    }

    public void startThread(View view, float duration) {
        stopThread = false;
        float d = duration * 1000;
        int e = (int) d;
        Log.d("ANIMATION", "duration in millis " + e);
        ExampleRunnable runnable = new ExampleRunnable(e);
        new Thread(runnable).start();
    }

    public void stopThread(View view) {
        stopThread = true;
    }

    //Animation siehe https://blog.flexiple.com/build-your-first-android-ar-app-using-arcore-and-sceneform/
    public void updateAnimation() {

        arFragment
                .getArSceneView()
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            Long time = System.nanoTime();
                            for (AnimationInstance animator : animators) {
                                animator.animator.applyAnimation(

                                        animator.index = vIndex,
                                        (float) ((time - animator.startTime) / (double) SECONDS.toNanos(1))
                                                % animator.duration);
                                animator.animator.updateBoneMatrices();
                                Log.d("DURATION", "Duration animation : " + filamentAsset.getAnimator().getAnimationName(vIndex));
                                Log.d("DURATION", "Duration animator : " + animator.duration);
                                Log.d("DURATION", "Duration time : " + (time - animator.startTime) / (double) SECONDS.toNanos(1) % animator.duration);
                            }
                        });
    }

    private enum AppAnchorState {
        NONE,
        HOSTING,
        HOSTED
    }

    private static class AnimationInstance {
        Animator animator;
        Long startTime;
        float duration;
        int index = vIndex;


        AnimationInstance(Animator animator, int index, Long startTime) {
            this.animator = animator;
            this.startTime = startTime;
            this.duration = animator.getAnimationDuration(index);
            vIndex = index;
        }
    }

    //Handler siehe  https://codinginflow.com/tutorials/android/starting-a-background-thread
    class ExampleRunnable implements Runnable {
        int milliseconds;
        int checkDuration;

        ExampleRunnable(int seconds) {
            this.milliseconds = seconds;
        }

        @Override
        public void run() {


            if (stopThread)
                return;
            Log.d("ANIMATION", "new thread count: " + milliseconds);

            mainAction.post(new Runnable() {
                @Override
                public void run() {
                    mainAction.setText("Duration " + milliseconds);
                    mainAction.setEnabled(false);


                }
            });
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChangeAnimationMethod(idle_index);
                    mainAction.setText("eat again");
                    mainAction.setEnabled(true);

                }
            });
        }
    }


}






