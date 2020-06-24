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

    // Types of AnimationStates that can be uploaded to Firebase
    public enum AnimationState {

        WALK,
        PET,
        IDLE,
        EAT,
        RESET,
        THROW_MEAT,
        THROW_BALL
    }


    public void uploadAnimationState(AnimationState type) {

        // Sets Value in Firebase, triggers onDataChange in the Reference Listener
        animationReference.setValue(type);

    }

    public void uploadAnchor(String id) {

        // Sets Value in Firebase, triggers onDataChange in the Reference Listener
        anchorReference.setValue(id);

    }

    public void uploadUpdatePosition(Vector3 oldPosition, Vector3 movePosition, Vector3 cameraPosition) {

        // Uploading a Hash Map with Old Position, New (Move To) Position) and the Camera Position
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


    public AnimationState getAnimationState() {
        return animationState;
    }

    public void setUpdatePosition(HashMap<String, Object> updatePosition) {
        this.updatePosition = updatePosition;
    }


    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }

    public void setAnimationState(AnimationState animationState) {
        this.animationState = animationState;
    }


    public String getAnchorId() {

        return anchorId;

    }

    public HashMap<String, Object> getUpdatePosition() {
        return updatePosition;
    }

    public DatabaseReference getUpdatePositionReference() { return updatePositionReference; }

    public DatabaseReference getAnchorReference() {
        return anchorReference;
    }

    public DatabaseReference getAnimationReference() {
        return animationReference;
    }

    private DatabaseReference createReference(String path) {

        return database.getReference(path);

    }

}