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
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
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
    TransformableNode model;
  private int modelLimit = 1;
  private int modelCounter = 0;
    private int animationCount = 0;
  private ArFragment arFragment;
  private Renderable renderable;

  private static class AnimationInstance {
    Animator animator;
    Long startTime;
    float duration;
    int index;

    AnimationInstance(Animator animator, int index, Long startTime) {
      this.animator = animator;
      this.startTime = startTime;
      this.duration = animator.getAnimationDuration(index);
      this.index = index;
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





      if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }


    // UI Buttons

      // Button





    /*

    // CloudPoints


      ArCoreApk

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

      Button changeAnimation = (Button) findViewById(R.id.animationControl);
      Log.d("debug", "button found" + changeAnimation);

      changeAnimation.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
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

                    "https://drive.google.com/uc?export=download&id=1nykf4iGZiscHZnhJUndt7Fpdaun7bNGZ"
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

                    if(modelCounter >= modelLimit) {


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

                    for(int i = 0; i < modelPosition.length; i++) {

                      //  Log.i(MODEL_POSITION, i + ": " + modelPosition[i]);

                        textView.setText(textView.getText() + "\n" + modelPosition[i]);

                    }



                    // Create the transformable model and add it to the anchor.
                    model = new TransformableNode(arFragment.getTransformationSystem());
                    model.setParent(anchorNode);
                    model.setRenderable(renderable);
                    model.select();

                    FilamentAsset filamentAsset = model.getRenderableInstance().getFilamentAsset();
                    if (filamentAsset.getAnimator().getAnimationCount() > 2) {
                        animators.add(new AnimationInstance(filamentAsset.getAnimator(), 0, System.nanoTime()));
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


          arFragment
                  .getArSceneView()
                  .getScene()
                  .addOnUpdateListener(
                          frameTime -> {
                              Long time = System.nanoTime();
                              for (AnimationInstance animator : animators) {
                                  animator.animator.applyAnimation(
                                          animator.index,
                                          (float) ((time - animator.startTime) / (double) SECONDS.toNanos(1))
                                                  % animator.duration);
                                  animator.animator.updateBoneMatrices();
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
}
