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

    final int eat_index = 0;
    final int getPet_index = 1;
    final int idle_index = 2;
    final int walk_index = 3;
    private final Set<AnimationInstance> animators = new ArraySet<>();
    // Abfrage ob animation läuft
    boolean moving;
    boolean pettingAllowed = false;
    Control control;
    Renderable renderableOne, renderableTwo;
    private FirebaseManager firebaseManager = new FirebaseManager();
    private volatile FilamentAsset filamentAsset;
    private ArFragment arFragment;
    private Long startTimeofCurrentAnimation;
    private float currentAnimationDuration;
    private float speedFactor = 4;

    /**
     * Dragon Constructor
     * The Transformationsystem, that could be used to rotate, scale and translate is deactivated to have
     * a better experience "commanding" the dragon
     * @param arFragment
     * @param anchorNode
     * @param renderableOne
     * @param renderableTwo
     * @param control
     */
    Dragon(ArFragment arFragment, AnchorNode anchorNode, Renderable renderableOne, Renderable renderableTwo, Control control) {
        super(arFragment.getTransformationSystem());
        this.arFragment = arFragment;
        this.control = control;
        this.renderableOne = renderableOne;
        this.renderableTwo = renderableTwo;

        // Deactivate Rotation, Translation and Scaling of the Transformationsystem
        getTranslationController().setEnabled(false);
        getRotationController().setEnabled(false);
        getScaleController().setEnabled(false);

        // Needed for petting the dragon while draging the finger, when he is activated
        // Click the dragon once to select him, then drag
        DragPettingController dragPettingController = new DragPettingController(this, this, arFragment.getTransformationSystem().getDragRecognizer(), control);
        addTransformationController(dragPettingController);

        setParent(anchorNode);
        updateRenderable();

        // Imports the Animations of the GLB and stores them in animators
        setDragonAnimations();
    }

    /**
     * Switches between the two Stages of the dragon and its different animations (two different renderables)
     * Only after restart, since the Update would have a big Memory Leak otherwise (Stuttering after a few switches)
     */
    void updateRenderable() {
        if (control.getHappyAnimation()) setRenderable(renderableTwo);
        else setRenderable(renderableOne);
    }

    /**
     * Social Interaction is allowed
     * @param pettingAllowed
     */
    void setSocial(Boolean pettingAllowed) {
        this.pettingAllowed = pettingAllowed;
    }

    /**
     * Needed in DragPettingController to know, if the dragon is allowed to be pet
     * @return
     */
    boolean getPettingAllowed() {
        return pettingAllowed;
    }

    /**
     * @author ARCORE
     * Imports the Animations of the GLB and stores them in animators
     */
    void setDragonAnimations() {

        assert this.getRenderableInstance() != null;
        filamentAsset = this.getRenderableInstance().getFilamentAsset();

        for (int i = 0; i < filamentAsset.getAnimator().getAnimationCount(); i++) {
            animators.add(new AnimationInstance(filamentAsset.getAnimator(), i));
        }
    }

    /**
     * changes the current Animation of the dragon. The animations run in different speeds
     *
     * @param index
     */
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

    /**
     * Needed for some Thread to run exactly until the end of the Animation
     * @return
     */
    public float getAnimationDuration() {

        return currentAnimationDuration;
    }

    /**
     * Start with the Idle-Index
     */
    @Override
    public void onActivate() {
        updateAnimation(idle_index);
    }

    /**
     * The movement Animation
     * The original template of the animation came from somewhere else
     * but was added and changed a lot (its an Objectanimation from Android)
     * @param newPos
     * @param distance
     * @param rotationVector
     * @return
     */
    long moveTo(Vector3 newPos, double distance, Vector3 rotationVector) {

        // Sets the dragon to moving
        moving = true;

        // Subfunction to rotate the dragon at the same time as moving
        rotateDragon(rotationVector);

        // Starts the Walking-Animation (not the actual movement, only the Animation of the GLB)
        updateAnimation(walk_index);

        // Sets the Objectanimator to move to the new Point in linear way
        ObjectAnimator objectAnimation = new ObjectAnimator();
        objectAnimation.setAutoCancel(true);
        objectAnimation.setTarget(this);
        objectAnimation.setObjectValues(this.getWorldPosition(), newPos);
        objectAnimation.setPropertyName("worldPosition");
        objectAnimation.setEvaluator(new Vector3Evaluator());
        objectAnimation.setInterpolator(new LinearInterpolator());

        // calculate the duration of the Animation
        double velocity = 0.1 * speedFactor;
        long time = (long) ((distance / velocity) * 1000);
        // Duration in ms of the animation.
        objectAnimation.setDuration(time);
        objectAnimation.start();

        // Additional Listener to trigger Animations and other activities after the
        // the Dragon is finished walking to the new point
        objectAnimation.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Sets currentPosition to WorldPosition
                setNewPosition(newPos);

                // When the dragon should pick up meat
                if (control.getMeatActivated()) {
                    updateAnimation(eat_index);
                    // When the dragon should pick up the ball
                } else if (control.getBallActivated()) {
                    updateAnimation(eat_index);
                    // When the ball is already picked up and he is at the point of the camera
                    // this is calculating if the Creator walked to far away since the dragon walked
                    // in his direction
                } else if (control.getBallBackActivated()) {


                    Vector3 cameraPosition;

                    // Creator: get the current cameraPosition and set this information to the Spectator
                    if (control.getUser() == Control.User.CREATOR) {
                        cameraPosition = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                        control.updatePositions(getWorldPosition());
                        // Spectator: get the newest CameraPosition of the Creator
                    } else {
                        cameraPosition = control.getCameraPosition();
                    }

                    // Calculate the cameraPosition without the y-Axis
                    cameraPosition = new Vector3(cameraPosition.x, getWorldPosition().y, cameraPosition.z);

                    // Calculate distance
                    double distance = Math.sqrt(Math.pow(getWorldPosition().x - cameraPosition.x, 2) + Math.pow(getWorldPosition().y - cameraPosition.y, 2) + Math.pow(getWorldPosition().z - cameraPosition.z, 2));

                    // If the Creator moved too far since the dragon started walking into his direction
                    // Restart the walking towards him-function
                    if (distance > 2) bringBackBall();
                    else {
                        // finished, go back to idling
                        control.setBallBackActivated(false);
                        updateAnimation(idle_index);
                        moving = false;
                    }

                } else {
                    // If no meat, ball or bringing ballback, then just idle
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

        // Needed for some calculations and threads
        return time;



    }

    /**
     * gets triggered after the Dragon picked up the Ball
     */
    void bringBackBall() {


        // The dragon is the parent of the ball
        control.getBall().setParent(this);
        // Sets the local Position of the Ball into the mouth of the dragon
        control.getBall().setLocalPosition(new Vector3(0, 0.45f, 0.3f));

        Vector3 cameraPosition;
        // Two different cameraPosition, Creator: Normal camera, Spectator: cameraPosition of the Creator
        if (control.getUser() == Control.User.CREATOR) {
            cameraPosition = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
            control.updatePositions(getWorldPosition());
        } else {
            cameraPosition = control.getCameraPosition();
        }


        // If this would not be changed, the dragon would walk UP to the cameraposition.
        // This way it stays on the plane.
        cameraPosition = new Vector3(cameraPosition.x, getWorldPosition().y, cameraPosition.z);

        Vector3 rotationVect = new Vector3().subtract(cameraPosition, getWorldPosition());
        double distance = Math.sqrt(Math.pow(getWorldPosition().x - cameraPosition.x, 2) + Math.pow(getWorldPosition().y - cameraPosition.y, 2) + Math.pow(getWorldPosition().z - cameraPosition.z, 2));

        cameraPosition = new Vector3().add(getWorldPosition(), rotationVect.scaled(0.8f));

        // Switches to bringing the ball back then the dragon starts moving to the dragon
        control.setBallActivated(false);
        control.setBallBackActivated(true);

        moveTo(cameraPosition, distance, rotationVect);


    }

    /**
     * based on another Rotation-function that was used in the solar-System example of ARCore
     * https://github.com/google-ar/sceneform-android-sdk/blob/v1.15.0/samples/solarsystem/app/src/main/java/com/google/ar/sceneform/samples/solarsystem/RotatingNode.java
     * @param distanceVector
     */
    void rotateDragon(Vector3 distanceVector) {

        // If the movement to the new point is too small, do not rotate
        if (distanceVector.length() < 0.1) {
            return;
        }

        // Calculate the RotationAngle out of the two Vectors (The Direction the dragon looks and the new direction-Vector
        // Its "GetBack", because the dragon looks the other way. That way the dragon looks into the direction of the camera
        // when it spawns
        final Vector3 directionA = this.getBack().normalized();
        final Vector3 directionB = distanceVector.normalized();
        float rotationAngle = (float) Math.acos(new Vector3().dot(directionA, directionB));

        // If the RotationAngle is too small, do not rotate
        if (Math.abs(rotationAngle) < 0.01) {
            return;
        }

        // Cross product of the directions would give you the rotations Axis.
        // But this is not perfectly on the plane, so it would rotate weird
        Vector3 rotationAxis = new Vector3().cross(directionA, directionB).normalized();

        // Only choose between two rotation axis
        // If the rotation-axis is y-negative, then it rotates left, otherwise right.
        // That's why you have to differ between those two
        if (rotationAxis.y > 0) rotationAxis = new Vector3(0, 1, 0);
        else rotationAxis = new Vector3(0, -1, 0);

        double radToDegree = rotationAngle * 180.0 / Math.PI;

        // 4 seconds for a full spin
        ObjectAnimator dragonRotation;
        dragonRotation = createRotateAnimator(rotationAxis, (float) radToDegree);
        dragonRotation.setTarget(this);
        dragonRotation.setDuration((long) (1000 * radToDegree / 90));
        dragonRotation.start();


        dragonRotation.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // If the rotation didn't fully worked on the plane, correct the local Rotation, so its perfectly on the plane
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

    /**
     * Based on the Solar-System example to create the Points of Rotation in the Animation
     * https://github.com/google-ar/sceneform-android-sdk/blob/v1.15.0/samples/solarsystem/app/src/main/java/com/google/ar/sceneform/samples/solarsystem/RotatingNode.java
     * @param rotationAxis
     * @param radToDegree
     * @return
     */
    private ObjectAnimator createRotateAnimator(Vector3 rotationAxis, float radToDegree) {
        // Lets have some fun with Quaternions

        // This is needed, since the starting-Rotation is important to be always added to the new Rotation-Quaternions
        Quaternion baseOrientation = this.getLocalRotation();
        Quaternion[] orientations = new Quaternion[2];

        // Only 2 points for the Rotations created, had more before, but it wasn't needed
        for (int i = 0; i < orientations.length; i++) {
            // Calculate the section of the the angle for this Rotation-Quaternion (in this loop)
            float angle = i * radToDegree / (orientations.length - 1);

            // Create one new Rotation-Quaternion
            Quaternion orientation = Quaternion.axisAngle(rotationAxis, angle);

            // Add the orientation to the array
            orientations[i] = Quaternion.multiply(baseOrientation, orientation);
        }

        // create the ObjectAnimator with the array
        ObjectAnimator rotationAnimation = new ObjectAnimator();
        rotationAnimation.setObjectValues(orientations);
        rotationAnimation.setPropertyName("localRotation");
        rotationAnimation.setEvaluator(new QuaternionEvaluator());
        rotationAnimation.setInterpolator(new LinearInterpolator());
        rotationAnimation.setAutoCancel(true);

        return rotationAnimation;
    }

    /**
     * Changes the WorldPosition after the Animation (sometimes needed)
     * @param newPos
     */
    private void setNewPosition(Vector3 newPos) {
        this.setWorldPosition(newPos);
    }

    /**
     * Getter FirebaseManager
     * @return
     */
    public FirebaseManager getFirebaseManager() {
        return firebaseManager;
    }

    /**
     * This is a class for regulating the Animations
     * Part of the ARCORE Example, but thined down
     */
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
}

