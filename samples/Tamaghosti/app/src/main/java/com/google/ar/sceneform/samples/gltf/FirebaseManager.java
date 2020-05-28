package com.google.ar.sceneform.samples.gltf;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseManager {

    String anchorId;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public FirebaseManager() {


    }

    public void uploadAnchorToDatabase(String id) {

        DatabaseReference myRef = database.getReference("Cloud Anchor");

        myRef.setValue(id);

    }

    public String getAnchorFromDatabase() {

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


    public void helloWorld() {

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello there!");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("DATABASE", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("DATABASE", "Failed to read value.", error.toException());
            }
        });

    }


}

