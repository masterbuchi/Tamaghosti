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

import static android.os.SystemClock.elapsedRealtime;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * This is a example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class GltfActivity extends AppCompatActivity {
    private static final String TAG = GltfActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final String MODEL_POSITION = "MODEL_POSITION";
    TransformableNode model;
    private int modelLimit = 1;
    private int modelCounter = 0;
    private int animationCount = 0;
    private ArFragment arFragment;
    private Renderable renderable;
    ProgressBar prgHunger;
    ProgressBar prgEnergy;
    ProgressBar prgSocial;
    ProgressBar prgTraining;

    Button changeAnimation;
    Button sleep;
    Button social;
    Button training;
    ImageView plus;
    private Handler mainHandler = new Handler();
    //volatile == immer aktuellsten wert, nicht cash
    private volatile boolean stopThread = false;
    public volatile FilamentAsset filamentAsset;
    public static volatile int vIndex;

    NeedsControlActivity needsControl = new NeedsControlActivity();


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
    private int nextColor = 0;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent in = getIntent();
        final int hValue = in.getIntExtra("hungerValue", 0);
        needsControl.setHunger(hValue);
        final int slValue = in.getIntExtra("sleepValue", 0);
        needsControl.setEnergy(slValue);
        final int soValue = in.getIntExtra("socialValue", 0);
        needsControl.setSocial(soValue);
        final int tValue = in.getIntExtra("trainingValue", 0);
        needsControl.setTraining(tValue);

        Log.d("SleepOverviewDebug", "current sleepValue1 " + needsControl.getEnergy());





        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }



    /*

    // CloudPoints

    Context newContext = null;
      if(apk.checkAvailability(newContext.CAMERA_SERVICE)) {
          // Yes
     }
      ArCoreApk.Availability available = new ArCoreApk.Availability(Context.CAMERA_SERVICE);
    available.isSupported(Context.CAMERA_SERVICE);
      if(ArCoreApk.Availability.SUPPORTED_INSTALLED) {
      }
    Session session;
      // ArCoreApk.Availability test = new ArCoreApk.Availability();
    //  if () {
      //}
    //Session session = new Session(Context.CAMERA_SERVICE);
//      Frame frame = session.update();
      // Automatically releases point cloud resources at end of try block.
  //    try (PointCloud pointCloud = frame.acquirePointCloud()) {
          // Access point cloud data.
    //  }
      */


        setContentView(R.layout.activity_ux);
        setNeeds();


        changeAnimation = (Button) findViewById(R.id.animationControl);
        changeAnimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (needsControl.getHunger() <= 90) {
                    needsControl.feed();
                    setNeeds();
                    showPlus();
                }

                if (model != null) {
                    //Change Animation mit Handler
                    ChangeAnimationMethod(0);
                    startThread(null);
                }
                /* Chaos zum animationswechsel
              SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:sss");
              Log.d("ANIMATION", "start eating ");
              //Play eat animation
              if (model != null) {
                  FilamentAsset filamentAsset = model.getRenderableInstance().getFilamentAsset();
                  if (filamentAsset.getAnimator().getAnimationCount() >= 2) {
                     // float duration = filamentAsset.getAnimator().getAnimationDuration(0);
                      float duration = 30.70f;
                        float d = duration * 100;
                        int e = (int) d;
                      Log.d("ANIMATION", "duration in int " +e);


                      Calendar cTime = Calendar.getInstance();
                      Calendar dTime = cTime;
                      Log.d("ANIMATION", "duration  " +d);
                      Log.d("ANIMATION", "current time " +cTime);

                      dTime.add(Calendar.MILLISECOND, e);


                      Log.d("ANIMATION", "update time " +cTime);




                      Handler handler1 = new Handler();
                          handler1.post(new Runnable() {

                              @Override
                              public void run() {
                                  while (true) {
                                      animators.add(new AnimationInstance(filamentAsset.getAnimator(), 0, System.nanoTime()));
                                      Log.d("ANIMATION", "new thread huh: ");
                                  }
                              }
                          });


                      /*while (cTime.getTimeInMillis() <= dTime.getTimeInMillis()){
                          cTime = Calendar.getInstance();

                          Log.d("ANIMATION", "still eating " + cTime);

                      }

                      handler1.removeCallbacksAndMessages(null);
                      Log.d("ANIMATION", "finished now ide pls " + System.nanoTime());
                      animators.add(new AnimationInstance(filamentAsset.getAnimator(), 1, System.nanoTime()));

                      //filamentAsset.getAnimator().applyAnimation(0, duration);
                  }
              }
              */


//Change Animation old
              /*
              Log.d("animDebug", "counter before " +animationCount);
              if (model != null) {
                  if (animationCount >= 3) {
                      animationCount = 0;
                  }
                  FilamentAsset filamentAsset = model.getRenderableInstance().getFilamentAsset();
                  if (filamentAsset.getAnimator().getAnimationCount() >= 2) {
                      animators.add(new AnimationInstance(filamentAsset.getAnimator(), animationCount, System.nanoTime()));
                      Log.d("animDebug", "counter current " + animationCount);
                  }

              }
              animationCount++;
              Log.d("animDebug", "counter after " + animationCount);
*/
            }
        });


        sleep = (Button) findViewById(R.id.sleepControl);
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

        social = (Button) findViewById(R.id.socialControl);
        if (needsControl.getHunger() == 100 && needsControl.getEnergy() == 100) {
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

                Log.d("SocialDebug", "pressed " + needsControl.getSocial());
            }
        });

        training = (Button) findViewById(R.id.trainingControl);
        if (needsControl.getHunger() == 100 && needsControl.getEnergy() == 100 && needsControl.getSocial() == 100) {
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
                        this,
                        Uri.parse(
                                //emulator
                                "https://drive.google.com/uc?export=download&id=11kkfeLIKct6D-0LJQb81WfrC8IxtOTdz"
                                //tisch dino:


                        ))
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
                            Toast toast =
                                    Toast.makeText(this, "Unable to load Model renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });


        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (renderable == null) {

                        Log.i(TAG, "Model not available");

                        return;
                    }

                    if (modelCounter >= modelLimit) {


                        Log.i(TAG, "Reached ModelLimit");

                        return;

                    }

                    Log.i(TAG, "Place Model");

                    modelCounter++;

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
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
                    if (filamentAsset.getAnimator().getAnimationCount() > 2) {
                        animators.add(new AnimationInstance(filamentAsset.getAnimator(), 1, System.nanoTime()));
                    }

                    Color color = colors.get(nextColor);
                    nextColor++;
                    for (int i = 0; i < renderable.getSubmeshCount(); ++i) {
                        Material material = renderable.getMaterial(i);
                        material.setFloat4("baseColorFactor", color);
                    }
                });


        // Print - Test


        Log.i(TAG, "This hopefully works");

        updateAnimation();



    }

    public void updateAnimation(){

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
                                Log.d("ANIMATION", "ANimation in updateANimation vIndex: " + filamentAsset.getAnimator().getAnimationName(vIndex));
                                Log.d("ANIMATION", "ANimation in updateANimation index : " + filamentAsset.getAnimator().getAnimationName(  animator.index));
                            }
                        });
    }

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
        if (filamentAsset.getAnimator().getAnimationCount() >= 2) {
            animators.add(new AnimationInstance(filamentAsset.getAnimator(), vIndex, System.nanoTime()));
            Log.d("ANIMATION", "Animation changed to : " + filamentAsset.getAnimator().getAnimationName(vIndex));
            updateAnimation();

        }
    }

    public void startThread(View view){
        stopThread = false;
        ExampleRunnable runnable = new ExampleRunnable(10);
        new Thread(runnable).start();
    }
    public void stopThread(View view) {
        stopThread = true;
    }

    class ExampleRunnable implements Runnable {
        int seconds;
        ExampleRunnable(int seconds) {
            this.seconds = seconds;
        }
        int checkDuration;
        @Override
        public void run() {
            for (int i = 0; i < seconds; i++) {
                checkDuration = i;
                if (stopThread)
                    return;
                Log.d("ANIMATION", "new thread count: " + i);
                Log.d("ANIMATION", "Animation changed to : " + filamentAsset.getAnimator().getAnimationName(vIndex));
                changeAnimation.post(new Runnable() {
                    @Override
                    public void run() {
                        changeAnimation.setText("HI "+ checkDuration);


                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

                /*Handler threadHandler = new Handler(Looper.getMainLooper());
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ChangeAnimationMethod(1);
                        Log.d("ANIMATION", "Old animation again: ");
                    }
                });*/

          // Unnötig verschachtelt siehe  https://codinginflow.com/tutorials/android/starting-a-background-thread wies richtig geht, aber das ist die einzige version die ein mal funktioneirt hat
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChangeAnimationMethod(1);
                    changeAnimation.post(new Runnable() {
                        @Override
                        public void run() {
                            changeAnimation.setText("HI");
                            ChangeAnimationMethod(1);
                            Log.d("ANIMATION", "Old animation again AGAIN: ");
                        }
                    });

                }
            });
            }
        }


}






