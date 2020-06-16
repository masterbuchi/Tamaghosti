package com.google.ar.sceneform.samples.gltf;

import com.google.ar.sceneform.math.Vector3;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private String anchorId;
    private double distance;
    private AnimationState animationState;
    private HashMap<String, Object> movePosition;



    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference anchorReference = createReference("Cloud Anchor");
    private DatabaseReference distanceReference = createReference("Distance");
    private DatabaseReference animationReference = createReference("Animation");
    private DatabaseReference movePositionReference = createReference("Move Position");

    public enum AnimationState {

        WALK,
        PET,
        IDLE,
        EAT,
        RESET

    }

    public void uploadAnimationState(AnimationState type) {

        animationReference.setValue(type);

    }

    public void uploadAnchor(String id) {

        anchorReference.setValue(id);

    }

    public void uploadMovePosition(Vector3 movePosition) {

        Map<String,Object> taskMap = new HashMap<>();
        taskMap.put("x", movePosition.x);
        taskMap.put("y", movePosition.y);
        taskMap.put("z", movePosition.z);

        movePositionReference.updateChildren(taskMap);

    }

    public void uploadDistance(double distance) {

        distanceReference.setValue(distance);

    }

    public AnimationState getAnimationState() {
        return animationState;
    }

    public void setMovePosition(HashMap<String, Object> movePosition) {
        this.movePosition = movePosition;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }

    public void setAnimationState(AnimationState animationState) {
        this.animationState = animationState;
    }

    public double getDistance() {

        return distance;

    }

    public String getAnchorId() {

        return anchorId;

    }

    public HashMap<String, Object> getMovePosition() {
        return movePosition;
    }

    public DatabaseReference getMovePositionReference() { return movePositionReference; }

    public DatabaseReference getAnchorReference() {
        return anchorReference;
    }

    public DatabaseReference getDistanceReference() {
        return distanceReference;
    }

    public DatabaseReference getAnimationReference() {
        return animationReference;
    }

    private DatabaseReference createReference(String path) {

        return database.getReference(path);

    }

}