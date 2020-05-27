package com.google.ar.sceneform.samples.gltf;

import android.animation.ObjectAnimator;
import android.support.v4.app.Fragment;
import android.util.ArraySet;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;


import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;


public class Dragon extends TransformableNode {

    public static volatile int vIndex;
    public volatile FilamentAsset filamentAsset;
    private final Set<AnimationInstance> animators = new ArraySet<>();

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



    public int eat_index = 0;
    public int getPet_index = 1;
    public int idle_index = 2;
    public int walk_index = 3;


    private ObjectAnimator orbitAnimation = null;

    // Rotating test
    private float degreesPerSecond = 90.0f;



    public Dragon(TransformationSystem transformationSystem) {
        super(transformationSystem);
    }


    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        // Animation hasn't been set up.
        if (orbitAnimation == null) {
            return;
        }



       /* float animatedFraction = orbitAnimation.getAnimatedFraction();
        orbitAnimation.setDuration(getAnimationDuration());
        orbitAnimation.setCurrentFraction(animatedFraction);*/


    }

   public void setDragonAnimation() {

        // Oben deklariert FilamentAsset filamentAsset = model.getRenderableInstance().getFilamentAsset();
        filamentAsset = this.getRenderableInstance().getFilamentAsset();
        if (filamentAsset.getAnimator().getAnimationCount() > 3) {
            animators.add(new AnimationInstance(filamentAsset.getAnimator(), idle_index, System.nanoTime()));
        }
    }

    public void changeAnimationMethod(int index) {
        vIndex = index;
        if (filamentAsset.getAnimator().getAnimationCount() >= 3) {
            animators.add(new AnimationInstance(filamentAsset.getAnimator(), vIndex, System.nanoTime()));
            Log.d("ANIMATION", "Animation changed to : " + filamentAsset.getAnimator().getAnimationName(vIndex));
            //updateAnimation();

        }
    }



    public void updateAnimation(ArFragment arFragment) {

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


    private long getAnimationDuration() {
        return (long) (1000 * 360 / (degreesPerSecond * 1));
    }

    public FilamentAsset getFilamentAsset() {
        return filamentAsset;
    }


    @Override
    public void onActivate() {
        startAnimation();
    }


    private void startAnimation() {
        if (orbitAnimation != null) {
            return;
        }

        orbitAnimation = createAnimator(true, 0);
        orbitAnimation.setTarget(this);
        orbitAnimation.setDuration(getAnimationDuration());
        orbitAnimation.start();
    }


    //DynamicAnimation dynamicMovement;



    /** Returns an ObjectAnimator that makes this node rotate. */
    private static ObjectAnimator createAnimator(boolean clockwise, float axisTiltDeg) {
        // Node's setLocalRotation method accepts Quaternions as parameters.
        // First, set up orientations that will animate a circle.
        Quaternion[] orientations = new Quaternion[4];
        // Rotation to apply first, to tilt its axis.
        Quaternion baseOrientation = Quaternion.axisAngle(new Vector3(1.0f, 0f, 0.0f), axisTiltDeg);
        for (int i = 0; i < orientations.length; i++) {
            float angle = i * 360 / (orientations.length - 1);
            if (clockwise) {
                angle = 360 - angle;
            }
            Quaternion orientation = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), angle);
            orientations[i] = Quaternion.multiply(baseOrientation, orientation);
        }

        Quaternion[] moveorientations = new Quaternion[4];




        ObjectAnimator movementAnimation = new ObjectAnimator();


        ObjectAnimator orbitAnimation = new ObjectAnimator();
        // Cast to Object[] to make sure the varargs overload is called.
        orbitAnimation.setObjectValues((Object[]) orientations);

        // Next, give it the localRotation property.
        orbitAnimation.setPropertyName("localRotation");

        // Use Sceneform's QuaternionEvaluator.
        orbitAnimation.setEvaluator(new QuaternionEvaluator());

        //  Allow orbitAnimation to repeat forever
        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);

        return orbitAnimation;
    }


}

