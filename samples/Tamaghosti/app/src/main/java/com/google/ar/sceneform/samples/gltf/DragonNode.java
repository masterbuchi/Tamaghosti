package com.google.ar.sceneform.samples.gltf;

import android.animation.ObjectAnimator;
import android.support.v4.app.Fragment;
import android.view.animation.LinearInterpolator;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;


public class DragonNode extends TransformableNode {


    private ObjectAnimator orbitAnimation = null;

    // Rotating test
    private float degreesPerSecond = 90.0f;



    public DragonNode(TransformationSystem transformationSystem) {
        super(transformationSystem);
    }


    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        // Animation hasn't been set up.
        if (orbitAnimation == null) {
            return;
        }

        float animatedFraction = orbitAnimation.getAnimatedFraction();
        orbitAnimation.setDuration(getAnimationDuration());
        orbitAnimation.setCurrentFraction(animatedFraction);


    }


    private long getAnimationDuration() {
        return (long) (1000 * 360 / (degreesPerSecond * 1));
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
