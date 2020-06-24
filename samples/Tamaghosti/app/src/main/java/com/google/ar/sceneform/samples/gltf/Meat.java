package com.google.ar.sceneform.samples.gltf;

import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;

/**
 * Class for the Meat Object
 */
public class Meat extends Node {

    ArFragment arFragment;
    Renderable renderable;
    Control control;
    ObjectAnimator meatRotationAnimation = null;

    /**
     * Constructor Meat-object
     *
     * @param arFragment
     * @param renderable
     * @param control
     */
    public Meat(ArFragment arFragment, Renderable renderable, Control control) {

        this.arFragment = arFragment;
        this.renderable = renderable;
        this.control = control;
    }

    /**
     * Based on the Solar-System example to create the Points of Rotation in the Animation
     * https://github.com/google-ar/sceneform-android-sdk/blob/v1.15.0/samples/solarsystem/app/src/main/java/com/google/ar/sceneform/samples/solarsystem/RotatingNode.java
     *
     * @return
     */
    private static ObjectAnimator createAnimator() {

        // For Rotationpoints, could be less
        Quaternion[] orientations = new Quaternion[4];

        // Manually tried the good baseOrientiation, so the rotation looks good
        Quaternion baseOrientation = Quaternion.eulerAngles(new Vector3(-71.508f, 0, -180.000f));

        for (int i = 0; i < orientations.length; i++) {
            float angle = i * 360 / (orientations.length - 1);

            //Rotations Angle is Z-Axis
            Quaternion orientation = Quaternion.axisAngle(new Vector3(0f, 0f, -1.0f), angle);

            orientations[i] = Quaternion.multiply(baseOrientation, orientation);
        }

        // Sets the Rotation of the meat to infinite, until its being thrown
        ObjectAnimator orbitAnimation = new ObjectAnimator();
        orbitAnimation.setObjectValues(orientations);
        orbitAnimation.setPropertyName("localRotation");
        orbitAnimation.setEvaluator(new QuaternionEvaluator());
        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);

        return orbitAnimation;
    }

    /**
     * Set the meat to the camera and corrects the position
     * The meat now moves with the camera
     */
    public void setMeatToCamera() {

        // Set Parent
        setParent(arFragment.getArSceneView().getScene().getCamera());
        // Makes meat visible (if not visible)
        setRenderable(renderable);

        // Corrects Position, Rotation and Scale to fitting size
        setLocalRotation(new Quaternion(0, 180, 250, 0));
        setLocalPosition(new Vector3(0, -0.3f, -1));
        setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));

    }

    /**
     * Sets meatParent to Hitpoint on plane and starts Meat-Throw-Animation
     *
     * @param hitResult
     * @param dragonTime
     */
    void meatThrowAnimation(HitResult hitResult, float dragonTime) {

        // Stop the RotationAnimation that started when it was selected
        stopAnimation();

        Vector3 cameraPosition = getWorldPosition();

        // Creates anchor and node and sets its parent to Scene
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        Vector3 newPosition = anchorNode.getWorldPosition();

        // Tell Spectator current Positions and the wanted new Position
        control.updatePositions(newPosition);

        // Direction vector in the direction of the new Position
        Vector3 directionVector = new Vector3().subtract(newPosition, cameraPosition);

        // Position in the middle of the throw, used as highest point (x and z coordinates) of the parableThrow
        Vector3 middlePoint = Vector3.add(cameraPosition, directionVector.scaled(0.5f));

        // Sets Starting-Numbers
        float x_1 = cameraPosition.x;
        float x_2 = newPosition.x;
        float x_3 = middlePoint.x;
        float y_1 = cameraPosition.y;
        float y_2 = newPosition.y;
        // Adds 20% of the throwing-length as height of the parable
        float y_3 = middlePoint.y + directionVector.scaled(0.2f).length();
        float x;
        float y;
        float z;


        float distance = directionVector.length();
        //interestingly enough its exactly one to look good
        double velocity = 1;
        //calculate throwing time
        float time = (long) ((distance / velocity) * 1000);

        // If the Dragon would be faster then the Meat, let them be the same speed
        if (time > dragonTime) time = dragonTime;

        int steps = 200;

        // Array of position of the parableThrow
        Vector3[] positions = new Vector3[steps];

        // current ball position is the starting point
        Vector3 currentPos = cameraPosition;


        // 200 Steps for smooth curve
        for (int i = 0; i < steps; i++) {

            // add 1/200 of the length of the  to the current position
            x = currentPos.x + directionVector.x / (float) steps;

            // very complicated of the y-Position that of course I did by hand and not with Matlab ;)
            y = (x * y_2 - x * y_3 + x_2 * y_3 - x_3 * y_2) / (x_2 - x_3) + ((x - x_2) * (x - x_3) * (y_1 - y_2)) / ((x_1 - x_2) * (x_2 - x_3)) - ((x - x_2) * (x - x_3) * (y_1 - y_3)) / ((x_1 - x_3) * (x_2 - x_3));

            // add 1/200 of the length of the  to the current position
            z = currentPos.z + directionVector.z / (float) steps;

            // set currentPos
            currentPos = new Vector3(x, y, z);

            // add currentPos to the array
            positions[i] = currentPos;

        }

        // needed for Spectators
        setParent(anchorNode);
        setWorldPosition(cameraPosition);
        setLocalRotation(new Quaternion(0, 180, 180, 0));
        setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));

        // Set the specs for the Movement-Animation
        ObjectAnimator objectAnimation = new ObjectAnimator();
        objectAnimation.setAutoCancel(true);
        objectAnimation.setTarget(this);
        objectAnimation.setObjectValues(positions);
        objectAnimation.setPropertyName("WorldPosition");
        objectAnimation.setEvaluator(new Vector3Evaluator());

        // Linear Movement, not exactly physically correct, but looks okay
        objectAnimation.setInterpolator(new LinearInterpolator());

        // Duration in ms of the animation
        objectAnimation.setDuration((long) time);
        objectAnimation.start();


        objectAnimation.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // After the Throw set the Meat a little bit a higher
                setLocalPosition(new Vector3(0, 0.05f, 0));
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {
            }
        });

    }

    // Spectator Version
    void meatThrowAnimation(Vector3 newPosition, Vector3 cameraPosition, long dragontime) {

        stopAnimation();

        setRenderable(renderable);

        AnchorNode anchorNode = new AnchorNode();

        anchorNode.setWorldPosition(newPosition);

        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // calculate curve
        Vector3 directionVector = new Vector3().subtract(newPosition, cameraPosition);

        Vector3 middlePoint = Vector3.add(cameraPosition, directionVector.scaled(0.5f));

        float x_1 = cameraPosition.x;
        float x_2 = newPosition.x;
        float x_3 = middlePoint.x;
        float y_1 = cameraPosition.y;
        float y_2 = newPosition.y;
        float y_3 = middlePoint.y + directionVector.scaled(0.2f).length();
        float x;
        float y;
        float z;


        float distance = directionVector.length();

        double velocity = 1;
        long time = (long) ((distance / velocity) * 1000);

        if (time > dragontime) time = dragontime;

        int steps = 200;

        Vector3[] positions = new Vector3[steps];

        Vector3 currentPos = cameraPosition;


        // 200 Steps for smooth curve
        for (int i = 0; i < steps; i++) {

            x = currentPos.x + directionVector.x / (float) steps;

            y = (x * y_2 - x * y_3 + x_2 * y_3 - x_3 * y_2) / (x_2 - x_3) + ((x - x_2) * (x - x_3) * (y_1 - y_2)) / ((x_1 - x_2) * (x_2 - x_3)) - ((x - x_2) * (x - x_3) * (y_1 - y_3)) / ((x_1 - x_3) * (x_2 - x_3));

            z = currentPos.z + directionVector.z / (float) steps;


            currentPos = new Vector3(x, y, z);

            positions[i] = currentPos;

        }

        setParent(anchorNode);
        setWorldPosition(cameraPosition);
        setLocalRotation(new Quaternion(0, 180, 180, 0));
        setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));

        ObjectAnimator objectAnimation = new ObjectAnimator();
        objectAnimation.setAutoCancel(true);
        objectAnimation.setTarget(this);
        objectAnimation.setObjectValues(positions);
        objectAnimation.setPropertyName("WorldPosition");
        // The Vector3Evaluator is used to evaluator 2 vector3 and return the next
        // vector3.  The default is to use lerp.
        objectAnimation.setEvaluator(new Vector3Evaluator());
        // This makes the animation linear (smooth and uniform).
        objectAnimation.setInterpolator(new LinearInterpolator());

        // Duration in ms of the animation.
        objectAnimation.setDuration(time);
        objectAnimation.start();


        objectAnimation.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                setLocalPosition(new Vector3(0, 0.05f, 0));

            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {

            }
        });

    }

    void startAnimation() {
        if (meatRotationAnimation != null) {
            return;
        }

        meatRotationAnimation = createAnimator();
        meatRotationAnimation.setTarget(this);
        meatRotationAnimation.setDuration(1000 * 360 / 90);
        meatRotationAnimation.start();
    }

    void stopAnimation() {
        if (meatRotationAnimation == null) {
            return;
        }
        meatRotationAnimation.cancel();
        meatRotationAnimation = null;
    }
}
