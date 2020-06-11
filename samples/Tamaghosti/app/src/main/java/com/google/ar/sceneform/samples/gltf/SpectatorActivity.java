package com.google.ar.sceneform.samples.gltf;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;

public class SpectatorActivity extends AppCompatActivity {

    private ArFragment arFragment;

    private Renderable renderable;

    private Dragon dragon;

    private double distance;
    private String anchorId;
    private FirebaseManager.AnimationState animationState;

    private boolean checkForUpdates;

    private boolean dragonCreated;

    private boolean initAnchorListener;
    private boolean initDistanceListener;
    private boolean initAnimationStateListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectator);

        checkForUpdates = false;

        dragonCreated = false;

        initAnchorListener = true;
        initAnimationStateListener = true;
        initDistanceListener = true;

        FirebaseManager firebaseManager = new FirebaseManager();

        // Init
        distance = firebaseManager.getDistance();
        anchorId = firebaseManager.getAnchorId();
        animationState = firebaseManager.getAnimationState();


        Log.i("MOVE", "DISTANCE: " + distance);
        Log.i("MOVE", "ANCHOR ID: " + anchorId);
        Log.i("MOVE", "ANIMATION STATE: " + animationState);


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.spectator_fragment);



        WeakReference<SpectatorActivity> weakActivity = new WeakReference<>(this);

        ModelRenderable.builder()
                .setSource(
                        this, R.raw.dragon66_one)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            SpectatorActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.renderable = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            // showToast("while loading an error occurred.");

                            return null;
                        });


        // Resolve Button Listener
        Button resolve = findViewById(R.id.resolve);

        resolve.setOnClickListener(view -> {

            /*
            distance = firebaseManager.getDistance();
            anchorId = firebaseManager.getAnchorId();
            animationState = firebaseManager.getAnimationState();

             */

            if(anchorId == null) {

                Toast.makeText(getApplicationContext(), "Please wait a second", Toast.LENGTH_SHORT).show();
            } else if(dragonCreated == false) {

                Toast.makeText(getApplicationContext(), "Dragon created", Toast.LENGTH_SHORT).show();

                assert arFragment.getArSceneView().getSession() != null;
                Anchor resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);

                AnchorNode anchorNode = new AnchorNode(resolvedAnchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                // Place Resolved Anchor
                dragon = new Dragon(arFragment, anchorNode, renderable);
                dragon.select();

                dragonCreated = true;

                checkForUpdates = true;

                Log.d("Firebase", "DragonPosition " + dragon.getWorldPosition());

                Log.d("Firebase", "DragonRotation " + dragon.getWorldRotation());


            } else {
                Toast.makeText(getApplicationContext(), "Dragon is already created", Toast.LENGTH_SHORT).show();
                Log.d("Firebase", "DragonPosition " + dragon.getWorldPosition());

                Log.d("Firebase", "DragonRotation " + dragon.getWorldRotation());
            }

        });

        // Alternative... listen for changes!

        DatabaseReference anchorReference = firebaseManager.getAnchorReference();

        anchorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Init and Change

                anchorId = dataSnapshot.getValue(String.class);

                if(initAnchorListener) {
                    initAnchorListener = false;
                } else {

                    // Move dragon

                    firebaseManager.setAnchorId(anchorId);

                    distance = firebaseManager.getDistance();

                    // Create Cloud Anchor

                    assert arFragment.getArSceneView().getSession() != null;
                    Anchor resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);

                    AnchorNode anchorNode = new AnchorNode(resolvedAnchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    Toast.makeText(getApplicationContext(), "Move Dragon", Toast.LENGTH_SHORT).show();

                    Log.i("MOVE", "TRIGGERED");

                    // Move Dragon


                    // NOTE: Messes up the given framework. Array Out of bounce
                    dragon.moveTo(anchorNode, distance);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference distanceReference = firebaseManager.getDistanceReference();

        distanceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Init and Change

                distance = dataSnapshot.getValue(Double.class);

                if(initDistanceListener) {
                    initDistanceListener = false;
                } else {

                    firebaseManager.setDistance(distance);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference animationReference = firebaseManager.getAnimationReference();

        animationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Init and Change

                animationState = dataSnapshot.getValue(FirebaseManager.AnimationState.class);

                if(initAnimationStateListener) {
                    initAnimationStateListener = false;
                } else {
                    firebaseManager.setAnimationState(animationState);

                    switch(animationState) {

                        case IDLE:

                            dragon.updateAnimation(dragon.idle_index);
                            break;

                        case PET:

                            dragon.updateAnimation(dragon.getPet_index);
                            break;

                        case EAT:

                            dragon.updateAnimation(dragon.eat_index);
                            break;

                        case RESET:

                            // Resetting Animation State


                        default:

                            break;

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }



}

        /*
        // Update Loop
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {

            if(checkForUpdates) {

                // Compare old and new stats

                // Move when there is a new cloud anchor
                if(!anchorId.equals(firebaseManager.getAnchorId())) {

                    distance = firebaseManager.getDistance();
                    anchorId = firebaseManager.getAnchorId();

                    // Create Cloud Anchor

                    assert arFragment.getArSceneView().getSession() != null;
                    Anchor resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);

                    AnchorNode anchorNode = new AnchorNode(resolvedAnchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    Toast.makeText(getApplicationContext(), "Move Dragon", Toast.LENGTH_SHORT).show();

                    Log.i("MOVE", "TRIGGERED");

                    // Move Dragon


                    // NOTE: Messes up the given framework. Array Out of bounce
                    //dragon.moveTo(anchorNode, distance);

                }

                // else if compare Animation States

                // Check if eating



                // Check if petting

            }
        });

         */