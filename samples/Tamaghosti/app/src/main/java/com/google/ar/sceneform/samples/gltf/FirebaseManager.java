package com.google.ar.sceneform.samples.gltf;

import com.google.ar.sceneform.math.Vector3;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private String anchorId;
    private AnimationState animationState;
    private HashMap<String, Object> updatePosition;

    // Connecting to Firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Creating Reference Points which allow us to access their corresponding values
    private DatabaseReference anchorReference = createReference("Cloud Anchor");
    private DatabaseReference animationReference = createReference("Animation");
    private DatabaseReference updatePositionReference = createReference("Move Position");

    /**
     * Types of AnimationStates that can be uploaded to Firebase
     */
    public enum AnimationState {

        WALK,
        PET,
        IDLE,
        EAT,
        RESET,
        THROW_MEAT,
        THROW_BALL,
        HAPPY,
        SAD
    }

    /**
     * Sets Value in Firebase, triggers onDataChange in the Reference Listener
     * @param type
     */
    public void uploadAnimationState(AnimationState type) {
        animationReference.setValue(type);
    }

    /**
     * Sets Value in Firebase, triggers onDataChange in the Reference Listener
     * @param id
     */
    public void uploadAnchor(String id) {
            anchorReference.setValue(id);
    }

    /**
     * Uploading a Hash Map with Old Position, New (Move To) Position) and the Camera Position
     * @param oldPosition
     * @param movePosition
     * @param cameraPosition
     */
    public void uploadUpdatePosition(Vector3 oldPosition, Vector3 movePosition, Vector3 cameraPosition) {

        Map<String,Object> taskMap = new HashMap<>();
        taskMap.put("oldPosition_x", oldPosition.x);
        taskMap.put("oldPosition_y", oldPosition.y);
        taskMap.put("oldPosition_z", oldPosition.z);
        taskMap.put("moveTo_x", movePosition.x);
        taskMap.put("moveTo_y", movePosition.y);
        taskMap.put("moveTo_z", movePosition.z);
        taskMap.put("camera_x", cameraPosition.x);
        taskMap.put("camera_y", cameraPosition.y);
        taskMap.put("camera_z", cameraPosition.z);

        updatePositionReference.updateChildren(taskMap);

    }

    /**
     *
     * @return
     */
    public AnimationState getAnimationState() {
        return animationState;
    }

    /**
     *
     * @param updatePosition
     */
    public void setUpdatePosition(HashMap<String, Object> updatePosition) {
        this.updatePosition = updatePosition;
    }

    /**
     *
     * @param anchorId
     */
    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }

    /**
     *
     * @param animationState
     */
    public void setAnimationState(AnimationState animationState) {
        this.animationState = animationState;
    }

    /**
     *
     * @return
     */
    public String getAnchorId() {

        return anchorId;

    }

    /**
     *
     * @return
     */
    public HashMap<String, Object> getUpdatePosition() {
        return updatePosition;
    }

    /**
     *
     * @return
     */
    public DatabaseReference getUpdatePositionReference() { return updatePositionReference; }

    /**
     *
     * @return
     */
    public DatabaseReference getAnchorReference() {
        return anchorReference;
    }

    /**
     *
     * @return
     */
    public DatabaseReference getAnimationReference() {
        return animationReference;
    }

    /**
     *
     * @param path
     * @return
     */
    private DatabaseReference createReference(String path) {

        return database.getReference(path);

    }

}