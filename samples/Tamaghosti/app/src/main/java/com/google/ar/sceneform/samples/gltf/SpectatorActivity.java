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

    // Allows us to access our Fragment that is using AR Technology
    private ArFragment arFragment;

    // Needed to render our dragon, meat and ball
    private Renderable dragonRenderableOne, dragonRenderableTwo, meatRenderable, ballRenderable;

    // Control class
    Control control;

    // Needed for Checking current OPENGL-version
    private static final String TAG = ArActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    // Firebase Values that will be updated when onData change is called
    private HashMap<String, Object> updatePosition;
    private String anchorId;
    private FirebaseManager.AnimationState animationState;

    // The Anchor that is getting resolved by the Cloud Anchor Id
    private Anchor resolvedAnchor;

    // Bool flags that will be triggered at the initialization of our reference listeners
    private boolean initAnchorListener;
    private boolean initAnimationStateListener;

    private Vector3 cameraPosition;

    // Used to compare currented Anchor Id with the updated one to check if there is a new host who updated a new cloud anchor id
    private String currentAnchorId;
    private String updatedAnchorId;
    private long time = 0;

    // Allowing us to access firebasemanager
    FirebaseManager firebaseManager;

    private enum AppAnchorState {
        SPECTATING,
        RESOLVING,
        NONE
    }

    // Current state of the checkIfUploaded Method
    private AppAnchorState appAnchorState = AppAnchorState.NONE;

    // Check if device supports OpenGL ES 3.0
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

        // Init control
        control = new Control(this, Control.User.SPECTATOR);

        initAnchorListener = true;
        initAnimationStateListener = true;

        firebaseManager = new FirebaseManager();

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.spectator_fragment);

        // Weak Reference needed to create our models
        WeakReference<SpectatorActivity> weakActivity = new WeakReference<>(this);

        // Rendering our models
        renderer(R.raw.meat, "meat", weakActivity);
        renderer(R.raw.ball, "ball", weakActivity);
        renderer(R.raw.dragon66_one, "dragon_one", weakActivity);
        renderer(R.raw.dragon65_two, "dragon_two", weakActivity);


        // Resolve Button Listener
        // Starts the process of resolving the cloud anchor. It might take some time
        Button resolve = findViewById(R.id.resolve);

        resolve.setOnClickListener(view -> {

            // Setting our appAnchorState to NONE allows us to check for an updated Anchor ID manually
            appAnchorState = AppAnchorState.NONE;

            if (appAnchorState == AppAnchorState.NONE) {

                // Gets the cloud anchor based on the anchor Id from the google cloud platform
                resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);
                // Setting the cloud anchor state to resolving which will trigger our update method
                appAnchorState = AppAnchorState.RESOLVING;

            }

            // Gets triggered when the App Anchor State is currently resolving the cloud anchor
            if (appAnchorState != AppAnchorState.SPECTATING) {

                Toast.makeText(getApplicationContext(), "Please wait a second", Toast.LENGTH_SHORT).show();
            } else if (control.getDragon() == null) {

                // If there is no dragon yet, control.createDragon will create a new one
                Toast.makeText(getApplicationContext(), "Dragon created", Toast.LENGTH_SHORT).show();

                // Why is this getting triggered
                control.createDragon(null, dragonRenderableOne, dragonRenderableTwo);

                // Initializing currentAnchorId with the current anchor Id
                currentAnchorId = anchorId;

            } else {


                Toast.makeText(getApplicationContext(), "Dragon is already created", Toast.LENGTH_SHORT).show();
            }

        });

        // Anchor Reference which allows us to observe the firebase database value of our Anchor Id
        // onDataChange will get triggered when it's first initialized and whenever the value is changed
        DatabaseReference anchorReference = firebaseManager.getAnchorReference();

        anchorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Init and Change

                Toast.makeText(getApplicationContext(), "Initializing AnchorID", Toast.LENGTH_SHORT).show();

                // Gets Value from database
                anchorId = dataSnapshot.getValue(String.class);

                updatedAnchorId = anchorId;

                if (initAnchorListener) {
                    // Do nothing on the first initialization
                    initAnchorListener = false;
                } else {

                    // Triggers everytime when the anchor Id is changed


                    // Setting value in firebaseManager. Allows us to reuse the values and avoiding coupling of classes
                    firebaseManager.setAnchorId(anchorId);

                    // Case: Spectator's dragon is already created and the host sets a new dragon
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

        // Observes the Position Hash Map of the Firebase Database
        DatabaseReference updatePositionReference = firebaseManager.getUpdatePositionReference();

        updatePositionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Gets triggered on init and on change

                HashMap<String, Object> coordinates = new HashMap<>();

                // Updating HashMap
                dataSnapshot.getChildren().forEach(
                        dataSnapshot1 -> coordinates.put(dataSnapshot1.getKey(), dataSnapshot1.getValue())
                );

                firebaseManager.setUpdatePosition(coordinates);

                updatePosition = firebaseManager.getUpdatePosition();

                // Casting object -> double -> float, because Vector3 is only accepting floats as values and the values in firebase seem to be doubles
                cameraPosition = new Vector3((float) ((double) updatePosition.get("camera_x")), (float) ((double) updatePosition.get("camera_y")), (float) ((double) updatePosition.get("camera_z")));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // Observes Animation State in Firebase
        DatabaseReference animationReference = firebaseManager.getAnimationReference();

        animationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Init and Change

                // Get Animation State Value
                animationState = dataSnapshot.getValue(FirebaseManager.AnimationState.class);


                if (initAnimationStateListener) {
                    // Do "nothing" when the method is triggered for the first time. The first time that the method is getting triggered
                    // is always when it gets initialized
                    initAnimationStateListener = false;
                } else {

                    firebaseManager.setAnimationState(animationState);

                    // Get current Positions
                    Vector3 oldPosition = new Vector3((float) ((double) updatePosition.get("oldPosition_x")), (float) ((double) updatePosition.get("oldPosition_y")), (float) ((double) updatePosition.get("oldPosition_z")));

                    Vector3 newPosition = new Vector3((float) ((double) updatePosition.get("moveTo_x")), (float) ((double) updatePosition.get("moveTo_y")), (float) ((double) updatePosition.get("moveTo_z")));

                    cameraPosition = new Vector3((float) ((double) updatePosition.get("camera_x")), (float) ((double) updatePosition.get("camera_y")), (float) ((double) updatePosition.get("camera_z")));

                    // If the dragon doesn't exist yet, do nothing
                    if (control.getDragon() != null) {

                        // Delete Ball if it is not used (setRenderable( Null) )
                        if (control.getBall() != null && !control.getBallBackActivated() && !control.getBallActivated()) control.getBall().setRenderable(null);

                        // Update Animation based on the new animationState
                        switch (animationState) {

                            case WALK:
                                control.getDragon().setWorldPosition(oldPosition);
                                time = control.moveDragon(newPosition);
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
                                control.setMeatActivated(true);
                                // If there is another meat that exists already, delete the old one
                                if (control.getMeat() != null)
                                    control.getMeat().setRenderable(null);
                                // Create a new meat
                                control.setMeat(new Meat(arFragment, meatRenderable, control));
                                control.getMeat().setEnabled(true);
                                // Play Meat Throw Animation
                                control.getMeat().meatThrowAnimation(newPosition, cameraPosition, time);
                                // Animation Thread
                                control.startThread((float) time);
                                break;

                            case THROW_BALL:
                                control.setBallActivated(true);
                                if (control.getBall() == null) {
                                    // Create a new ball if there exists none
                                    control.setBall(new Ball(arFragment, ballRenderable, control));
                                } else {
                                    // Update Ball Renderable ( old ball -> new ball)
                                    control.getBall().setRenderable(ballRenderable);
                                }
                                control.getBall().setEnabled(true);
                                // Play Throw Ball Animation
                                control.getBall().ballAnimation(newPosition, cameraPosition);
                                break;

                            case RESET:

                                // Resetting Animation State. OnDataChange is only triggered when the Value changes
                                // e.g.
                                // IDLE -> IDLE (doesn't trigger onDataChange)
                                // IDLE -> WALK (TRIGGERS onDataChange)
                                // Useful if we e.g. pet the dragon multiple times or the dragon changes it direction when walking, because
                                // the user tapped another destination while the dragon is still walking
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    // Renders our models
    public void renderer(int id, String renderable, WeakReference<SpectatorActivity> weakActivity) {

        ModelRenderable.builder()
                .setSource(
                        this, id)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            SpectatorActivity activity = weakActivity.get();
                            if (activity != null) {
                                switch (renderable) {
                                    case "meat":
                                        activity.meatRenderable = modelRenderable;
                                        break;
                                    case "ball":
                                        activity.ballRenderable = modelRenderable;
                                        break;
                                    case "dragon_one":
                                        activity.dragonRenderableOne = modelRenderable;
                                        break;
                                    case "dragon_two":
                                        activity.dragonRenderableTwo = modelRenderable;
                                        break;
                                }
                            }
                        })
                .exceptionally(
                        throwable -> {

                            showToast("while loading an error occurred.");

                            return null;
                        });
    }

    // Function that is triggered by the updated method
    private void CheckIfUploaded(Anchor anchor, AppAnchorState state) {

        // If the anchor isn't resolving, stop function
        if (state != AppAnchorState.RESOLVING) {
            return;
        }

        // else ... get the current state of our cloud anchor
        Anchor.CloudAnchorState cloudAnchorState = anchor.getCloudAnchorState();

        if (cloudAnchorState.isError()) {

            showToast(cloudAnchorState.toString());


        } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {

            // Cloud Anchor State will change to successful, if the cloud anchor could be resolved successfully

            String anchorId = anchor.getCloudAnchorId();

            // Final state
            appAnchorState = AppAnchorState.SPECTATING;
            showToast("Anchor resolved sucessfully. Cloud Anchor Id: " + anchorId);

            // Set Anchor
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            // Delete old dragon
            if (control.getDragon() != null) control.getDragon().setRenderable(null);

            // Create new dragon
            control.createDragon(null, dragonRenderableOne, dragonRenderableTwo);

            updatePosition = firebaseManager.getUpdatePosition();

            Vector3 oldPosition = new Vector3((float) ((double) updatePosition.get("moveTo_x")), (float) ((double) updatePosition.get("moveTo_y")), (float) ((double) updatePosition.get("moveTo_z")));

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


    public Vector3 getCameraPosition() {
        return cameraPosition;
    }
}