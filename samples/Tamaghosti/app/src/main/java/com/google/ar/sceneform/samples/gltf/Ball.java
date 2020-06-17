package com.google.ar.sceneform.samples.gltf;

import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;

public class Ball extends Node {

    ArFragment arFragment;
    Renderable renderable;

    public Ball(ArFragment arFragment, Renderable renderable) {

        this.arFragment = arFragment;
        this.renderable = renderable;
    }


    public void setMeatToCamera() {

        setParent(arFragment.getArSceneView().getScene().getCamera());

        Log.d("Meat", "Position: " + getWorldPosition());


        setRenderable(renderable);


        setLocalRotation(new Quaternion(0, 180, 250, 0));
        setLocalPosition(new Vector3(0, -0.3f, -1));
        setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));

    }

    void meatAnimation(HitResult hitResult, long dragontime) {

        Vector3 cameraPosition = getWorldPosition();

        Anchor anchor = hitResult.createAnchor();

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());


        Vector3 newPosition = anchorNode.getWorldPosition();

        // calculate curve
        Vector3 directionVector = new Vector3().subtract(newPosition, cameraPosition);

        Vector3 middlePoint = cameraPosition.add(cameraPosition,directionVector.scaled(0.5f));

        float x_1 = cameraPosition.x;
        float x_2 = newPosition.x;
        float x_3 = middlePoint.x;
        float y_1 = cameraPosition.y;
        float y_2 = newPosition.y;
        float y_3 = middlePoint.y+directionVector.scaled(0.2f).length();
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

            y  = (x*y_2 - x*y_3 + x_2*y_3 - x_3*y_2)/(x_2 - x_3) + ((x - x_2)*(x - x_3)*(y_1 - y_2))/((x_1 - x_2)*(x_2 - x_3)) - ((x - x_2)*(x - x_3)*(y_1 - y_3))/((x_1 - x_3)*(x_2 - x_3));

            z = currentPos.z + directionVector.z / (float) steps;


            currentPos = new Vector3(x, y,z);

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
}
