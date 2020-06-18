package com.google.ar.sceneform.samples.gltf;

import com.google.ar.sceneform.math.Vector3;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private String anchorId;
    private AnimationState animationState;
    private HashMap<String, Object> movePosition;



    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference anchorReference = createReference("Cloud Anchor");
    private DatabaseReference animationReference = createReference("Animation");
    private DatabaseReference movePositionReference = createReference("Move Position");

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

        animationReference.setValue(type);

    }

    public void uploadAnchor(String id) {

        anchorReference.setValue(id);

    }

    public void uploadMovePosition( Vector3 oldPosition, Vector3 movePosition) {

        Map<String,Object> taskMap = new HashMap<>();
        taskMap.put("oldPosition_x", oldPosition.x);
        taskMap.put("oldPosition_y", oldPosition.y);
        taskMap.put("oldPosition_z", oldPosition.z);
        taskMap.put("moveTo_x", movePosition.x);
        taskMap.put("moveTo_y", movePosition.y);
        taskMap.put("moveTo_z", movePosition.z);

        movePositionReference.updateChildren(taskMap);

    }


    public AnimationState getAnimationState() {
        return animationState;
    }

    public void setMovePosition(HashMap<String, Object> movePosition) {
        this.movePosition = movePosition;
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

    public HashMap<String, Object> getMovePosition() {
        return movePosition;
    }

    public DatabaseReference getMovePositionReference() { return movePositionReference; }

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