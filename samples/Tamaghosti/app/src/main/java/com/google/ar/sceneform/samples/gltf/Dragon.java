package com.google.ar.sceneform.samples.gltf;

import android.animation.ObjectAnimator;
import android.util.ArraySet;
import android.view.animation.LinearInterpolator;

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;

import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseTransformableNode;
import com.google.ar.sceneform.ux.TransformableNode;



import java.util.Set;


import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;

import static java.util.concurrent.TimeUnit.SECONDS;


public class Dragon extends TransformableNode  {


    private volatile FilamentAsset filamentAsset;
    private final Set<AnimationInstance> animators = new ArraySet<>();
    private ArFragment parentArFragment;

    private Long startTimeofCurrentAnimation;



    int eat_index = 0;
    int getPet_index = 1;
    int idle_index = 2;
    int walk_index = 3;
    private float speedFactor = 4;


    private static class AnimationInstance {
        Animator animator;
        float duration;
        int index;


        AnimationInstance(Animator animator, int index) {
            this.animator = animator;
            this.duration = animator.getAnimationDuration(index);
            this.index = index;
        }

    }


    Dragon(ArFragment arFragment) {
        super(arFragment.getTransformationSystem());
        parentArFragment = arFragment;

    }


    void setDragonAnimations() {

        assert this.getRenderableInstance() != null;
        filamentAsset = this.getRenderableInstance().getFilamentAsset();

        for (int i = 0; i < filamentAsset.getAnimator().getAnimationCount(); i++) {
            animators.add(new AnimationInstance(filamentAsset.getAnimator(), i));
        }
    }


    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

       /* if (this!=null) {
            Quaternion q1 = this.getLocalRotation();
            Quaternion q2 = Quaternion.axisAngle(new Vector3(0, 1f, 0f), 2f);
            this.setLocalRotation(Quaternion.multiply(q1, q2));
        }*/

    }


    void updateAnimation(int index) {


        startTimeofCurrentAnimation = System.nanoTime();

        parentArFragment
                .getArSceneView()
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {

                            Long time = System.nanoTime();
                            for (AnimationInstance animator : animators) {
                                animator.animator.applyAnimation(index, (float) ( speedFactor * (time - startTimeofCurrentAnimation) / (double) SECONDS.toNanos(1)) % animator.duration);
                                animator.animator.updateBoneMatrices();
                                //Log.d("Animators", Integer.toString(index));

                            }
                        });
    }


    FilamentAsset getFilamentAsset() {
        return filamentAsset;
    }

    @Override
    public void onActivate() {
        updateAnimation(idle_index);


    }


   /* double moveToDynamic(AnchorNode newPos, double distance) {

        float velocityStart = 1;
        float friction = 1;


        //newPos.getWorldPosition : new Position the Dragon goes to
        //this.getWorldPosition : current Position of the dragon
        float distanceInX = Math.abs(newPos.getWorldPosition().x - this.getWorldPosition().x);
        float distanceInY = Math.abs(newPos.getWorldPosition().y - this.getWorldPosition().y);


        FlingAnimation flingX = new FlingAnimation(this, DynamicAnimation.TRANSLATION_X);
        flingX.setStartVelocity(velocityStart)
                //.setMinValue(MIN_TRANSLATION) // minimum translationX property
                //.setMaxValue(maxTranslationX)  // maximum translationX property
                .setFriction(friction)
                .start();

        FlingAnimation flingY = new FlingAnimation(mViewTobeFlung, DynamicAnimation.TRANSLATION_Y);
        flingY.setStartVelocity(velocityStart)
                //.setMinValue(MIN_TRANSLATION) // minimum translationX property
                //.setMaxValue(maxTranslationX)  // maximum translationX property
                .setFriction(friction)
                .start();




        return distance;
    }*/




    double moveTo(AnchorNode newPos, double distance) {

        ObjectAnimator objectAnimation = new ObjectAnimator();
        objectAnimation.setAutoCancel(true);
        objectAnimation.setTarget(this);
        // All the positions should be world positions
        // The first position is the start, and the second is the end.
        objectAnimation.setObjectValues(this.getWorldPosition(), newPos.getWorldPosition());
        objectAnimation.setPropertyName("worldPosition");
        // The Vector3Evaluator is used to evaluator 2 vector3 and return the next
        // vector3.  The default is to use lerp.
        objectAnimation.setEvaluator(new Vector3Evaluator());
        // This makes the animation linear (smooth and uniform).
        objectAnimation.setInterpolator(new LinearInterpolator());

        double velocity = 0.1*speedFactor;
        double time = distance / velocity;

        // Duration in ms of the animation.
        objectAnimation.setDuration((long) (distance  / velocity) * 1000);
        updateAnimation(walk_index);
        objectAnimation.start();


        objectAnimation.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                setNewPosition(newPos);
                updateAnimation(idle_index);

            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {

            }
        });


        return time;
    }


    private void setNewPosition(AnchorNode newPos) {
        this.setWorldPosition(newPos.getWorldPosition());
    }

   /* public boolean moveTo(AnchorNode newPos) {


        float velocityStart = 1;
        float friction = 1;


        //newPos.getWorldPosition : new Position the Dragon goes to
        //this.getWorldPosition : current Position of the dragon
        float distanceInX = Math.abs(newPos.getWorldPosition().x - this.getWorldPosition().x);
        float distanceInY = Math.abs(newPos.getWorldPosition().y - this.getWorldPosition().y);


            FlingAnimation flingX = new FlingAnimation(, DynamicAnimation.TRANSLATION_X);
            flingX.setStartVelocity(velocityStart)
                    //.setMinValue(MIN_TRANSLATION) // minimum translationX property
                    //.setMaxValue(maxTranslationX)  // maximum translationX property
                    .setFriction(friction)
                    .start();

        FlingAnimation flingY = new FlingAnimation(mViewTobeFlung, DynamicAnimation.TRANSLATION_Y);
        flingY.setStartVelocity(velocityStart)
                //.setMinValue(MIN_TRANSLATION) // minimum translationX property
                //.setMaxValue(maxTranslationX)  // maximum translationX property
                .setFriction(friction)
                .start();

        return true;
    }*/


    //Returns an ObjectAnimator that makes this node rotate.
     /*
    private void startAnimation() {
        if (orbitAnimation != null) {
            return;
        }

        orbitAnimation = createAnimator(true, 0);
        orbitAnimation.setTarget(this);
        orbitAnimation.setDuration(getAnimationDuration());
        orbitAnimation.start();
    }

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



        ObjectAnimator orbitAnimation = new ObjectAnimator();
        // Cast to Object[] to make sure the varargs overload is called.
        orbitAnimation.setObjectValues(orientations);

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
*/

}

