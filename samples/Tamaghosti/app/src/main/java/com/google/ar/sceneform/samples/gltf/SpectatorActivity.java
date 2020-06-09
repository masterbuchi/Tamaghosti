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



    private boolean initAnchorListener;
    private boolean initDistanceListener;
    private boolean initAnimationStateListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectator);

        checkForUpdates = false;

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
                        this, R.raw.dino)
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
            } else {

                Toast.makeText(getApplicationContext(), "Dragon created", Toast.LENGTH_SHORT).show();

                assert arFragment.getArSceneView().getSession() != null;
                Anchor resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);

                AnchorNode anchorNode = new AnchorNode(resolvedAnchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                // Place Resolved Anchor
                createDragonNode(anchorNode);

                checkForUpdates = true;

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }


    private void createDragonNode(AnchorNode anchorNode) {
        // Transformable makes it possible to scale and drag the model
        dragon = new Dragon(arFragment);

        // Deactivate Rotation and Translation
        dragon.getTranslationController().setEnabled(false);
        dragon.getRotationController().setEnabled(false);
        //     model.getScaleController().setEnabled(false);

        dragon.setParent(anchorNode);
        dragon.setRenderable(renderable);
        dragon.select();

        dragon.setDragonAnimations();

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