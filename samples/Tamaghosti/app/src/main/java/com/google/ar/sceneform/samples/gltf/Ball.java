package com.google.ar.sceneform.samples.gltf;

import android.animation.ObjectAnimator;
import android.util.Log;
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

public class Ball extends Node {

    ArFragment arFragment;
    Renderable renderable;
    Control control;
    ObjectAnimator ballRotationAnimation;

    /**
     * Constructor Ball-object
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
     *
     * @param hitResult
     */
    void ballAnimation(HitResult hitResult) {


        Anchor anchor = hitResult.createAnchor();

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        Vector3 oldPosition = getWorldPosition();
        Vector3 newPosition = anchorNode.getWorldPosition();

        control.updatePositions(newPosition);

        newPosition=new Vector3().add(newPosition,new Vector3(0,0.1f,0));

        Vector3 directionVector = new Vector3().subtract(newPosition, oldPosition);

        Vector3 middlePosition = Vector3.add(oldPosition,directionVector.scaled(0.5f));

        setParent(anchorNode);

        throwAnimation(oldPosition,newPosition,directionVector,middlePosition);

    }


    // Spectator version

    void ballAnimation(Vector3 newPosition, Vector3 oldPosition) {

        setRenderable(renderable);
        AnchorNode anchorNode = new AnchorNode();

        anchorNode.setWorldPosition(newPosition);

        anchorNode.setParent(arFragment.getArSceneView().getScene());

        newPosition = new Vector3().add(newPosition,new Vector3(0,0.1f,0));

        Vector3 directionVector = new Vector3().subtract(newPosition, oldPosition);

        Vector3 middlePosition = Vector3.add(oldPosition,directionVector.scaled(0.5f));

        setParent(anchorNode);

        throwAnimation(oldPosition,newPosition,directionVector,middlePosition);

    }

    void throwAnimation(Vector3 oldPosition, Vector3 newPosition, Vector3 directionVector, Vector3 middlePosition) {




        stopAnimation();
        startAnimation(true);
        float x_1 = oldPosition.x;
        float x_2 = newPosition.x;
        float x_3 = middlePosition.x;
        float y_1 = oldPosition.y;
        float y_2 = newPosition.y;
        float y_3 = middlePosition.y+directionVector.scaled(0.2f).length();
        float x;
        float y;
        float z;


        float distance = directionVector.length();

        double velocity = 1;
        long time = (long) ((distance / velocity) * 1000);


        int steps = 200;

        Vector3[] positions = new Vector3[steps];

        Vector3 currentPos = oldPosition;


        // 200 Steps for smooth curve
        for (int i = 0; i < steps; i++) {

            x = currentPos.x + directionVector.x / (float) steps;

            y  = (x*y_2 - x*y_3 + x_2*y_3 - x_3*y_2)/(x_2 - x_3) + ((x - x_2)*(x - x_3)*(y_1 - y_2))/((x_1 - x_2)*(x_2 - x_3)) - ((x - x_2)*(x - x_3)*(y_1 - y_3))/((x_1 - x_3)*(x_2 - x_3));

            z = currentPos.z + directionVector.z / (float) steps;


            currentPos = new Vector3(x, y,z);

            positions[i] = currentPos;

        }

        setWorldPosition(oldPosition);

        setLocalRotation(new Quaternion(0, 180, 180, 0));
        setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));

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

                Vector3 oldPosition = getWorldPosition();
                Vector3 newdirectionVector = new Vector3(directionVector.scaled(0.5f).x,0,directionVector.scaled(0.5f).z);
                Vector3 newPosition = Vector3.add(oldPosition,newdirectionVector);
                Vector3 directionVector = new Vector3().subtract(newPosition, oldPosition);


                Vector3 middlePosition = Vector3.add(oldPosition,directionVector.scaled(0.5f));

                if (directionVector.length() > 0.05) {
                    throwAnimation(oldPosition,newPosition,directionVector,middlePosition);
                } else {

                    Vector3 dragonPosition = control.getDragon().getWorldPosition();
                    Vector3 rotationVect = new Vector3().subtract(oldPosition, dragonPosition);
                    double distance = Math.sqrt(Math.pow(dragonPosition.x - oldPosition.x, 2) + Math.pow(dragonPosition.y - oldPosition.y, 2) + Math.pow(dragonPosition.z - oldPosition.z, 2));

                    Vector3 moveToPosition=new Vector3().add(oldPosition,new Vector3(0,-0.1f,0));

                    long time = control.getDragon().moveTo(moveToPosition, distance, rotationVect);

                    control.startThread((float) time);


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


    void startAnimation(boolean wurf) {
        if (ballRotationAnimation != null) {
            return;
        }

        ballRotationAnimation = createAnimator(wurf);
        ballRotationAnimation.setTarget(this);
        ballRotationAnimation.setDuration(1000 * 360 / 90);
        ballRotationAnimation.start();
    }

    void stopAnimation() {
        if (ballRotationAnimation == null) {
            return;
        }
        ballRotationAnimation.cancel();
        ballRotationAnimation = null;
    }

    /** Returns an ObjectAnimator that makes this node rotate. */
    private static ObjectAnimator createAnimator(boolean wurf) {
        // Node's setLocalRotation method accepts Quaternions as parameters.
        // First, set up orientations that will animate a circle.
        Quaternion[] orientations = new Quaternion[4];
        // Rotation to apply first, to tilt its axis.
        Quaternion baseOrientation = Quaternion.eulerAngles(new Vector3(0f, 0, 0f));
        for (int i = 0; i < orientations.length; i++) {
            float angle = i * 360 / (orientations.length - 1);
            Quaternion orientation;
if (wurf) orientation = Quaternion.axisAngle(new Vector3(1f, 0f, 0f), angle);
else      orientation = Quaternion.axisAngle(new Vector3(0f, 1f, 0f), angle);

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


}
