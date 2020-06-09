package com.google.ar.sceneform.samples.gltf;


import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FirebaseManager {

    private String anchorId;
    private double distance;
    private AnimationState animationState;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    private DatabaseReference anchorReference = createReference("Cloud Anchor");
    private DatabaseReference distanceReference = createReference("Distance");
    private DatabaseReference animationReference = createReference("Animation");


    /*
    private DatabaseReference anchorReference = createReference("Cloud Anchor", ReferenceType.ANCHOR_ID);
    private DatabaseReference distanceReference = createReference("Distance", ReferenceType.DISTANCE);
    private DatabaseReference animationReference = createReference("Animation", ReferenceType.ANIMATION);

     */


    public enum AnimationState {

        WALK,
        PET,
        STAND,
        EAT

    }

    private enum ReferenceType {

        DISTANCE,
        ANCHOR_ID,
        ANIMATION

    }


    public void uploadAnimationState(AnimationState type) {

        animationReference.setValue(type);

    }

    public void uploadAnchor(String id) {

        anchorReference.setValue(id);

    }

    public void uploadDistance(double distance) {

        distanceReference.setValue(distance);

    }

    public AnimationState getAnimationState() {
        return animationState;
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




    /*
    private DatabaseReference createReference(String path, ReferenceType type) {

        DatabaseReference reference = database.getReference(path);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    switch(type) {

                    case DISTANCE:
                        distance = dataSnapshot.getValue(Double.class);
                        break;

                    case ANCHOR_ID:
                        anchorId = dataSnapshot.getValue(String.class);
                        break;

                    case ANIMATION:
                        animationState = dataSnapshot.getValue(AnimationState.class);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + type);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return reference;

    }


     */

}

/*

package com.google.ar.sceneform.samples.gltf;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FirebaseManager {

    String anchorId;
    double distance;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public enum AnimationType {

        WALK,
        PET,
        STAND

    }


    public FirebaseManager() {



    }


    public void uploadAnchorToDatabase(String id) {

        DatabaseReference myRef = database.getReference("Cloud Anchor");

        myRef.setValue(id);

    }

    public void uploadDistance(double distance) {

        DatabaseReference myRef = database.getReference("Distance");

        myRef.setValue(distance);

    }



    public synchronized  DatabaseReference getDistanceReference() {

        return database.getReference("Distance");

    }


    public synchronized DatabaseReference getAnchorReference() {

        return database.getReference("Cloud Anchor");
    }

    public synchronized String getAnchorFromDatabase() {

        DatabaseReference myRef = database.getReference("Cloud Anchor");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);

                anchorId = value;
                Log.d("DATABASE", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("DATABASE", "Failed to read value.", error.toException());
            }
        });

        return anchorId;
    }



}


 */
