package com.google.ar.sceneform.samples.gltf;

import android.animation.ObjectAnimator;
import android.util.ArraySet;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.sceneform.AnchorNode;

import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;


import java.util.Set;


import static java.util.concurrent.TimeUnit.SECONDS;


public class Dragon extends TransformableNode {

    private FirebaseManager firebaseManager = new FirebaseManager();


    private volatile FilamentAsset filamentAsset;
    private final Set<AnimationInstance> animators = new ArraySet<>();
    private ArFragment arFragment;

    private Long startTimeofCurrentAnimation;


    private float currentAnimationDuration;

    // Abfrage ob animation läuft
    boolean moving;
    boolean pettingAllowed = false;

    Control control;

    Renderable renderableOne, renderableTwo;

    final int eat_index = 0;
    final int getPet_index = 1;
    final int idle_index = 2;
    final int walk_index = 3;
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


    Dragon(ArFragment arFragment, AnchorNode anchorNode, Renderable renderableOne, Renderable renderableTwo, Control control) {
        super(arFragment.getTransformationSystem());
        this.arFragment = arFragment;
        this.control = control;
        this.renderableOne = renderableOne;
        this.renderableTwo = renderableTwo;
        // Deactivate Rotation and Translation
        getTranslationController().setEnabled(false);
        getRotationController().setEnabled(false);
        //     model.getScaleController().setEnabled(false);

        DragPettingController dragPettingController = new DragPettingController(this, this, arFragment.getTransformationSystem().getDragRecognizer(), control);
        addTransformationController(dragPettingController);

        setParent(anchorNode);

        updateRenderable();

        setDragonAnimations();
    }

    void updateRenderable() {
        if (control.getHappyAnimation()) setRenderable(renderableTwo);
        else setRenderable(renderableOne);
    }

    void setSocial(Boolean pettingAllowed) {
        this.pettingAllowed = pettingAllowed;
    }

    boolean getPettingAllowed() {
        return pettingAllowed;
    }


    void setDragonAnimations() {

        assert this.getRenderableInstance() != null;
        filamentAsset = this.getRenderableInstance().getFilamentAsset();

        for (int i = 0; i < filamentAsset.getAnimator().getAnimationCount(); i++) {
            animators.add(new AnimationInstance(filamentAsset.getAnimator(), i));
        }
    }


    void updateAnimation(int index) {

        startTimeofCurrentAnimation = System.nanoTime();

        switch (index) {
            case eat_index:
                speedFactor = 1f;
                break;
            case getPet_index:
                speedFactor = 2f;
                break;
            case idle_index:
                speedFactor = 2f;
                break;
            case walk_index:
                speedFactor = 4f;
                break;
        }

        arFragment
                .getArSceneView()
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {

                            Long time = System.nanoTime();
                            for (AnimationInstance animator : animators) {
                                animator.animator.applyAnimation(index, (float) (speedFactor * (time - startTimeofCurrentAnimation) / (double) SECONDS.toNanos(1)) % animator.duration);
                                animator.animator.updateBoneMatrices();
                                currentAnimationDuration = animator.duration;
                            }
                        });
    }


    FilamentAsset getFilamentAsset() {
        return filamentAsset;
    }

    public float getAnimationDuration() {

        return currentAnimationDuration;
    }

    @Override
    public void onActivate() {
        updateAnimation(idle_index);
    }


    long moveTo(Vector3 newPos, double distance, Vector3 rotationVector) {

        moving = true;

        rotateDragon(rotationVector);

        updateAnimation(walk_index);
        ObjectAnimator objectAnimation = new ObjectAnimator();
        objectAnimation.setAutoCancel(true);
        objectAnimation.setTarget(this);
        // All the positions should be world positions
        // The first position is the start, and the second is the end.
        objectAnimation.setObjectValues(this.getWorldPosition(), newPos);
        objectAnimation.setPropertyName("worldPosition");
        // The Vector3Evaluator is used to evaluator 2 vector3 and return the next
        // vector3.  The default is to use lerp.
        objectAnimation.setEvaluator(new Vector3Evaluator());
        // This makes the animation linear (smooth and uniform).
        objectAnimation.setInterpolator(new LinearInterpolator());

        double velocity = 0.1 * speedFactor;
        long time = (long) ((distance / velocity) * 1000);

        // Duration in ms of the animation.
        objectAnimation.setDuration(time);

        objectAnimation.start();


        objectAnimation.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                setNewPosition(newPos);
                if (control.getMeatActivated()) {
                    updateAnimation(eat_index);
                } else if (control.getBallActivated()) {
                    updateAnimation(eat_index);
                } else if (control.getBallBackActivated()) {


                    Vector3 cameraPosition;
                    if (control.getUser() == Control.User.CREATOR) {
                        //FALLUNTERSCHEIDUNG
                        cameraPosition = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                        control.updatePositions(getWorldPosition());
                    } else {
                        cameraPosition = control.getCameraPosition();
                    }

                    cameraPosition = new Vector3(cameraPosition.x, getWorldPosition().y, cameraPosition.z);

                    double distance = Math.sqrt(Math.pow(getWorldPosition().x - cameraPosition.x, 2) + Math.pow(getWorldPosition().y - cameraPosition.y, 2) + Math.pow(getWorldPosition().z - cameraPosition.z, 2));

                    if (distance > 2) bringBackBall();
                    else {
                        control.setBallBackActivated(false);
                        updateAnimation(idle_index);
                        moving = false;
                    }

                } else {
                    updateAnimation(idle_index);
                    moving = false;
                }

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


    void bringBackBall() {


        control.getBall().setParent(this);
        control.getBall().setLocalPosition(new Vector3(0, 0.45f, 0.3f));

        Vector3 cameraPosition;
        if (control.getUser() == Control.User.CREATOR) {
            //FALLUNTERSCHEIDUNG
            cameraPosition = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
            control.updatePositions(getWorldPosition());
        } else {
            cameraPosition = control.getCameraPosition();
        }




        cameraPosition = new Vector3(cameraPosition.x, getWorldPosition().y, cameraPosition.z);

        Vector3 rotationVect = new Vector3().subtract(cameraPosition, getWorldPosition());
        double distance = Math.sqrt(Math.pow(getWorldPosition().x - cameraPosition.x, 2) + Math.pow(getWorldPosition().y - cameraPosition.y, 2) + Math.pow(getWorldPosition().z - cameraPosition.z, 2));

        cameraPosition = new Vector3().add(getWorldPosition(), rotationVect.scaled(0.8f));


        control.setBallActivated(false);
        control.setBallBackActivated(true);

        moveTo(cameraPosition, distance, rotationVect);


    }

    void rotateDragon(Vector3 distanceVector) {

        if (distanceVector.length() < 0.1) {
            Log.d("Rotation", "DistanceVectorlength  too small");
            return;
            //exit - don't do any rotation
            //distance is too small for rotation to be numerically stable
        }

        //Don't actually need to call normalize for directionA - just doing it to indicate that this vector must be normalized.
        final Vector3 directionA = this.getBack().normalized();
        final Vector3 directionB = distanceVector.normalized();

        float rotationAngle = (float) Math.acos(new Vector3().dot(directionA, directionB));


        if (Math.abs(rotationAngle) < 0.01) {
            Log.d("Rotation", "Rotationangle too small");
            return;
            //exit - don't do any rotation
            //angle is too small for rotation to be numerically stable
        }

        Vector3 rotationAxis = new Vector3().cross(directionA, directionB).normalized();

        if (rotationAxis.y > 0) rotationAxis = new Vector3(0, 1, 0);
        else rotationAxis = new Vector3(0, -1, 0);

        double radToDegree = rotationAngle * 180.0 / Math.PI;

        ObjectAnimator dragonRotation;

        dragonRotation = createRotateAnimator(false, rotationAxis, (float) radToDegree);
        dragonRotation.setTarget(this);
        dragonRotation.setDuration((long) (1000 * radToDegree / 90));
        dragonRotation.start();


        dragonRotation.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                setLocalRotation(new Quaternion(0, getLocalRotation().y, 0, getLocalRotation().w));
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {

            }
        });

    }


    private ObjectAnimator createRotateAnimator(boolean clockwise, Vector3 rotationAxis, float radToDegree) {
        // Node's setLocalRotation method accepts Quaternions as parameters.
        // First, set up orientations that will animate a circle.

        Quaternion baseOrientation = this.getLocalRotation();
        Quaternion[] orientations = new Quaternion[2];
        // Rotation to apply first, to tilt its axis.
        for (int i = 0; i < orientations.length; i++) {


            float angle = i * radToDegree / (orientations.length - 1);
            if (clockwise) {
                angle = 360 - angle;
            }
            Quaternion orientation = Quaternion.axisAngle(rotationAxis, angle);


            orientations[i] = Quaternion.multiply(baseOrientation, orientation);

            Log.d("Rotation", "orientations[i]: " + orientations[i] + " i: " + i);
        }

        ObjectAnimator rotationAnimation = new ObjectAnimator();
        // Cast to Object[] to make sure the varargs overload is called.
        rotationAnimation.setObjectValues(orientations);

        // Next, give it the localRotation property.
        rotationAnimation.setPropertyName("localRotation");

        // Use Sceneform's QuaternionEvaluator.
        rotationAnimation.setEvaluator(new QuaternionEvaluator());

        //  Allow rotationAnimation to repeat forever
        //rotationAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        //rotationAnimation.setRepeatMode(ObjectAnimator.RESTART);
        rotationAnimation.setInterpolator(new LinearInterpolator());
        rotationAnimation.setAutoCancel(true);

        return rotationAnimation;
    }

    private void setNewPosition(Vector3 newPos) {
        this.setWorldPosition(newPos);
    }

    public FirebaseManager getFirebaseManager() {
        return firebaseManager;
    }
}

