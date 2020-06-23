package com.google.ar.sceneform.samples.gltf;

import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;

/**
 * Class for the Ball Object
 */
public class Ball extends Node {

    private ArFragment arFragment;
    private Renderable renderable;
    private Control control;
    private ObjectAnimator ballRotationAnimation;

    /**
     * Constructor Ball-object
     *
     * @param arFragment
     * @param renderable
     * @param control
     */
    public Ball(ArFragment arFragment, Renderable renderable, Control control) {

        this.arFragment = arFragment;
        this.renderable = renderable;
        this.control = control;
    }

    /**
     * Set the ball to the camera and corrects the position
     * The ball now moves with the camera
     */
    void setBallToCamera() {

        // set Parent
        setParent(arFragment.getArSceneView().getScene().getCamera());
        //makes ball visible (if not visible)
        setRenderable(renderable);

        // corrects Position, Rotation and Scale to fitting size
        setLocalRotation(new Quaternion(0, 180, 250, 0));
        setLocalPosition(new Vector3(0, -0.3f, -1));
        setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));

    }

    /**
     * Sets ballparent to Hitpoint on plane and starts Ball-Throw-Animation
     *
     * @param hitResult
     */
    void ballAnimation(HitResult hitResult) {

        // creates anchor and node and sets its parent to Scene
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        Vector3 oldPosition = getWorldPosition();
        Vector3 newPosition = anchorNode.getWorldPosition();

        // Tell Spectator current Positions and the wanted new Position
        control.updatePositions(newPosition);

        // Change height of new Position to compensate the pivotpoint in the middle of the ball (estimated height)
        newPosition = new Vector3().add(newPosition, new Vector3(0, 0.1f, 0));

        // Direction vector in the direction of the new Position
        Vector3 directionVector = new Vector3().subtract(newPosition, oldPosition);

        // Position in the middle of the throw, used as highest point (x and z coordinates) of the parableThrow
        Vector3 middlePosition = Vector3.add(oldPosition, directionVector.scaled(0.5f));

        // Set Parent of Ball to anchorNode
        setParent(anchorNode);

        // Start throw Animation with calculated Vectors
        throwAnimation(oldPosition, newPosition, directionVector, middlePosition);

    }


    /**
     * Spectator Version
     * Sets ballparent to Hitpoint on plane and starts Ball-Throw-Animation
     *
     * @param newPosition
     * @param oldPosition
     */
    void ballAnimation(Vector3 newPosition, Vector3 oldPosition) {

        // Since for the Spectator the ball isn't visible, set the renderable visible (not null)
        setRenderable(renderable);

        // Create AnchorNode, set its Position to the NewPosition-Vector, that is sent by the Creator
        // also set Parent to Scene
        AnchorNode anchorNode = new AnchorNode();
        anchorNode.setWorldPosition(newPosition);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Change height of new Position to compensate the pivotpoint in the middle of the ball (estimated height)
        newPosition = new Vector3().add(newPosition, new Vector3(0, 0.1f, 0));

        // Direction vector in the direction of the new Position
        Vector3 directionVector = new Vector3().subtract(newPosition, oldPosition);

        // Position in the middle of the throw, used as highest point (x and z coordinates) of the parableThrow
        Vector3 middlePosition = Vector3.add(oldPosition, directionVector.scaled(0.5f));

        // Set Parent of Ball to anchorNode
        setParent(anchorNode);

        // Start throw Animation with calculated Vectors
        throwAnimation(oldPosition, newPosition, directionVector, middlePosition);

    }

    void throwAnimation(Vector3 oldPosition, Vector3 newPosition, Vector3 directionVector, Vector3 middlePosition) {

        // Stop Animation (In Creator the Rotation, when activated but not thrown)
        stopAnimation();

        // Start Animation of Rotation of the Ball
        startAnimation(true);

        // Sets Starting-Numbers
        float x_1 = oldPosition.x;
        float x_2 = newPosition.x;
        float x_3 = middlePosition.x;
        float y_1 = oldPosition.y;
        float y_2 = newPosition.y;

        // Adds 20% of the throwing-length as height of the parable
        float y_3 = middlePosition.y + directionVector.scaled(0.2f).length();
        float x;
        float y;
        float z;


        float distance = directionVector.length();

        //interestingly enough its exactly one to look good
        double velocity = 1;

        //calculate throwing time
        long time = (long) ((distance / velocity) * 1000);


        int steps = 200;

        // Array of position of the parableThrow
        Vector3[] positions = new Vector3[steps];

        // current ball position is the starting point
        Vector3 currentPos = oldPosition;


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
        setWorldPosition(oldPosition);
        setLocalRotation(new Quaternion(0, 180, 180, 0));
        setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));


        // Set the specs for the Movement-Animation
        ObjectAnimator objectAnimation = new ObjectAnimator();
        objectAnimation.setAutoCancel(true);
        objectAnimation.setTarget(this);
        objectAnimation.setObjectValues(positions);
        objectAnimation.setPropertyName("WorldPosition");
        objectAnimation.setEvaluator(new Vector3Evaluator());
        // Linear Movement, not exactly physically correct, but looks okay
        objectAnimation.setInterpolator(new LinearInterpolator());

        // Duration in ms of the animation.
        objectAnimation.setDuration(time);
        objectAnimation.start();


        objectAnimation.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
            }

            @Override
            // When the Animation is finished it will check if it should bounce again
            public void onAnimationEnd(android.animation.Animator animation) {

                // calculate new vectors (half of the distance before with fitted direction
                Vector3 oldPosition = getWorldPosition();
                Vector3 newdirectionVector = new Vector3(directionVector.scaled(0.5f).x, 0, directionVector.scaled(0.5f).z);
                Vector3 newPosition = Vector3.add(oldPosition, newdirectionVector);
                Vector3 directionVector = new Vector3().subtract(newPosition, oldPosition);
                Vector3 middlePosition = Vector3.add(oldPosition, directionVector.scaled(0.5f));

                // when the new distance is still long enough to bounce
                if (directionVector.length() > 0.05) {
                    throwAnimation(oldPosition, newPosition, directionVector, middlePosition);
                } else {


                    // set the position, where the dragon should go to
                    Vector3 dragonPosition = control.getDragon().getWorldPosition();
                    Vector3 rotationVect = new Vector3().subtract(oldPosition, dragonPosition);

                    // get distance
                    double distance = rotationVect.length();

                    // set Position to go to minus the previous added y-change of the ball
                    Vector3 moveToPosition = new Vector3().add(oldPosition, new Vector3(0, -0.1f, 0));

                    // Start Movement of the Dragon
                    long time = control.getDragon().moveTo(moveToPosition, distance, rotationVect);

                    // start Thread that starts other actions after Picking up ball
                    control.startThread((float) time);


                    // stop ball rotation of the Throw
                    stopAnimation();
                }


            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {

            }
        });

    }

    /**
     * Start the Rotation-Animation of the Ball
     *
     * @param wurf
     */
    void startAnimation(boolean wurf) {

        // if Animation is already running return
        if (ballRotationAnimation != null) {
            return;
        }

        // Start Ballrotation
        ballRotationAnimation = createAnimator(wurf);
        ballRotationAnimation.setTarget(this);
        ballRotationAnimation.setDuration(1000 * 360 / 90);
        ballRotationAnimation.start();
    }

    /**
     * Stop BallRotationAnimation
     */
    void stopAnimation() {
        // if no Animation running, return
        if (ballRotationAnimation == null) {
            return;
        }
        // cancel and set null
        ballRotationAnimation.cancel();
        ballRotationAnimation = null;
    }

    /**
     * Returns an ObjectAnimator that makes the ball rotate.
     * Some of the code is from the ARCORE-Example of the Solar Star System - Planet Rotation.
     */
    private static ObjectAnimator createAnimator(boolean wurf) {

        // set the rotationsPoints of the Animation
        Quaternion[] orientations = new Quaternion[4];

        // Calculate the rotation-positions
        for (int i = 0; i < orientations.length; i++) {
            float angle = i * 360 / (orientations.length - 1);
            Quaternion orientation;
            // if the Ball is thrown set the Rotation around the x-axis, otherwise set it around y-axis
            if (wurf) orientation = Quaternion.axisAngle(new Vector3(1f, 0f, 0f), angle);
            else orientation = Quaternion.axisAngle(new Vector3(0f, 1f, 0f), angle);
            orientations[i] = orientation;
        }

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


}
