package com.google.ar.sceneform.samples.gltf;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class SpectatorActivity extends AppCompatActivity {

    private ArFragment arFragment;

    private Renderable dragonRenderableOne, dragonRenderableTwo, meatRenderable, ballRenderable;

    Control control;

    private static final String TAG = ArActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private HashMap<String, Object> movePosition;
    private String anchorId;
    private FirebaseManager.AnimationState animationState;

    private Anchor resolvedAnchor;
    private boolean initAnchorListener;
    private boolean initAnimationStateListener;
    private boolean initMovePositionListener;

    private String currentAnchorId;
    private String updatedAnchorId;
    private long time;

    FirebaseManager firebaseManager;

    private enum AppAnchorState {
        SPECTATING,
        RESOLVING,
        NONE
    }

    private AppAnchorState appAnchorState = AppAnchorState.NONE;

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_spectator);

        time = 1;

        control = new Control(this, Control.User.SPECTATOR);

        initAnchorListener = true;
        initAnimationStateListener = true;
        initMovePositionListener = true;

        firebaseManager = new FirebaseManager();

        // Init
       //anchorId = firebaseManager.getAnchorId();
        //animationState = firebaseManager.getAnimationState();



        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.spectator_fragment);


        WeakReference<SpectatorActivity> weakActivity = new WeakReference<>(this);

        ModelRenderable.builder()
                .setSource(
                        this, R.raw.ball)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            SpectatorActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.ballRenderable = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            showToast("while loading an error occurred.");

                            return null;
                        });

        ModelRenderable.builder()
                .setSource(
                        this, R.raw.dragon66_one)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            SpectatorActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.dragonRenderableOne = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            // showToast("while loading an error occurred.");

                            return null;
                        });

        ModelRenderable.builder()
                .setSource(
                        this, R.raw.dragon65_two)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            SpectatorActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.dragonRenderableTwo = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            // showToast("while loading an error occurred.");

                            return null;
                        });
        ModelRenderable.builder()
                .setSource(
                        this, R.raw.meat)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            SpectatorActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.meatRenderable = modelRenderable;
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

            Toast.makeText(getApplicationContext(), "Anchorstate: " + appAnchorState, Toast.LENGTH_SHORT).show();

            appAnchorState = AppAnchorState.NONE;

            if (appAnchorState == AppAnchorState.NONE) {
                resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);
                appAnchorState = AppAnchorState.RESOLVING;
            }

            if (appAnchorState != AppAnchorState.SPECTATING) {

                Toast.makeText(getApplicationContext(), "Please wait a second", Toast.LENGTH_SHORT).show();
            } else if (control.getDragon() == null) {

                Toast.makeText(getApplicationContext(), "Dragon created", Toast.LENGTH_SHORT).show();

                control.createDragon(null, dragonRenderableOne, dragonRenderableTwo);

                currentAnchorId = anchorId;

            } else {






                Toast.makeText(getApplicationContext(), "Dragon is already created", Toast.LENGTH_SHORT).show();
            }

        });




        DatabaseReference anchorReference = firebaseManager.getAnchorReference();

        anchorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Init and Change

                Toast.makeText(getApplicationContext(), "Initalisierung der AnchorID", Toast.LENGTH_SHORT).show();

                anchorId = dataSnapshot.getValue(String.class);

                updatedAnchorId = anchorId;

                if (initAnchorListener) {
                    initAnchorListener = false;
                } else {

                    // Move dragon

                    firebaseManager.setAnchorId(anchorId);

                    if (control.getDragon() != null && !updatedAnchorId.equals(currentAnchorId)) {

                        Toast.makeText(getApplicationContext(), "New Host! Updating Dragon Anchor", Toast.LENGTH_SHORT).show();

                        assert arFragment.getArSceneView().getSession() != null;
                        resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);
                        appAnchorState = AppAnchorState.RESOLVING;



                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Cloud Anchor Update Loop
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {

            CheckIfUploaded(resolvedAnchor, appAnchorState);

        });



        DatabaseReference updatePositionReference = firebaseManager.getUpdatePositionReference();

        updatePositionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    HashMap<String, Object> coordinates = new HashMap<>();

                    dataSnapshot.getChildren().forEach(
                            dataSnapshot1 -> coordinates.put(dataSnapshot1.getKey(), dataSnapshot1.getValue())
                    );

                    firebaseManager.setUpdatePosition(coordinates);

                    movePosition = firebaseManager.getUpdatePosition();


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

                if (initAnimationStateListener) {
                    initAnimationStateListener = false;
                } else {
                    firebaseManager.setAnimationState(animationState);
                    Vector3 oldPosition = new Vector3((float) ((double)movePosition.get("oldPosition_x")),(float) ((double)movePosition.get("oldPosition_y")),(float) ((double)movePosition.get("oldPosition_z")));

                    Vector3 newPosition = new Vector3((float) ((double) movePosition.get("moveTo_x")), (float) ((double)movePosition.get("moveTo_y")), (float) ((double)movePosition.get("moveTo_z")));

                    Vector3 cameraPosition = new Vector3((float) ((double) movePosition.get("camera_x")), (float) ((double)movePosition.get("camera_y")), (float) ((double)movePosition.get("camera_z")));

                    switch (animationState) {

                        case WALK:
                            control.getDragon().setWorldPosition(oldPosition);
                            control.moveDragon(newPosition);

                            break;

                        case IDLE:

                            control.getDragon().updateAnimation(control.getDragon().idle_index);
                            break;

                        case PET:

                            control.getDragon().updateAnimation(control.getDragon().getPet_index);
                            break;

                        case EAT:

                            control.getDragon().updateAnimation(control.getDragon().eat_index);
                            break;

                        case THROW_MEAT:
                            // Play Throw Meat Animation
                            control.setMeatActivated(true);
                            control.getDragon().setWorldPosition(oldPosition);

                            if (control.getMeat() != null) control.getMeat().setRenderable(null);
                            control.setMeat(new Meat(arFragment, meatRenderable, control));

                            control.getMeat().setEnabled(true);

                            control.getMeat().meatThrowAnimation(newPosition, cameraPosition, time);
                            control.startThread((float) time);

                            break;

                        case THROW_BALL:
                            // Play Throw Ball Animation

                            control.setBall(new Ball(arFragment, ballRenderable, control));

                            control.getBall().setEnabled(true);


                            control.getBall().ballAnimation(newPosition, cameraPosition);

                            break;

                        case RESET:

                            // Resetting Animation State
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }


    private void CheckIfUploaded(Anchor anchor, AppAnchorState state) {

        if (state != AppAnchorState.RESOLVING) {
            return;
        }

        Anchor.CloudAnchorState cloudAnchorState = anchor.getCloudAnchorState();

        if (cloudAnchorState.isError()) {

            showToast(cloudAnchorState.toString());

        } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {

            String anchorId = anchor.getCloudAnchorId();

            appAnchorState = AppAnchorState.SPECTATING;
            showToast("Anchor resolved sucessfully. Cloud Anchor Id: " + anchorId);


            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            // Delete old dragon
            if(control.getDragon()!= null) control.getDragon().setRenderable(null);

            control.createDragon(null, dragonRenderableOne, dragonRenderableTwo);

            movePosition = firebaseManager.getUpdatePosition();

            Vector3 oldPosition = new Vector3((float) ((double)movePosition.get("oldPosition_x")),(float) ((double)movePosition.get("oldPosition_y")),(float) ((double)movePosition.get("oldPosition_z")));


            // Might cause the dragon to be placed in a wrong position
            control.getDragon().setWorldPosition(oldPosition);

            currentAnchorId = anchorId;
        }
    }

    public FirebaseManager getFirebaseManager() {
        return firebaseManager;
    }

    public ArFragment getArFragment() {
        return arFragment;
    }

    public Renderable getMeatRenderable() {
        return meatRenderable;
    }

    public Anchor getResolvedAnchor() {
        return resolvedAnchor;
    }

    public String getAnchorId() {
        return anchorId;
    }


    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}